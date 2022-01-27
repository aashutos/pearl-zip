/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.util;

import com.ntak.pearlzip.archive.constants.LoggingConstants;
import com.ntak.pearlzip.archive.pub.ArchiveReadService;
import com.ntak.pearlzip.archive.pub.ArchiveService;
import com.ntak.pearlzip.archive.pub.ArchiveWriteService;
import com.ntak.pearlzip.ui.constants.ZipConstants;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.model.ZipState;
import com.ntak.pearlzip.ui.pub.FrmLicenseDetailsController;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.archive.constants.LoggingConstants.PLUGIN_BUNDLES;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.ROOT_LOGGER;
import static com.ntak.pearlzip.archive.util.LoggingUtil.getStackTraceFromException;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ResourceConstants.COLONSV;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.ArchiveUtil.deleteDirectory;
import static com.ntak.pearlzip.ui.util.ArchiveUtil.extractToDirectory;
import static com.ntak.pearlzip.ui.util.JFXUtil.loadLicenseDetails;
import static com.ntak.pearlzip.ui.util.JFXUtil.raiseAlert;

/**
 *  Utility methods within this class are utilised by PearlZip to load Archive Service implementations.
 */
public class ModuleUtil {

    /**
     *  Loads services that come as default bundled with the application.
     */
    public static void loadModulesStatic() {
        ServiceLoader<ArchiveWriteService> serviceWriteLoader = ServiceLoader.load(ArchiveWriteService.class);
        ServiceLoader<ArchiveReadService> serviceReadLoader = ServiceLoader.load(ArchiveReadService.class);

        // LOG: ArchiveReadService implementation identified: %s
        LoggingConstants.ROOT_LOGGER.info(resolveTextKey(LOG_READ_SERVICES_IDENTIFIED,
                                                         serviceReadLoader.stream()
                                                                          .collect(Collectors.toList())));

        // LOG: ArchiveWriteService implementation identified: %s
        LoggingConstants.ROOT_LOGGER.info(resolveTextKey(LOG_WRITE_SERVICES_IDENTIFIED,
                                                         serviceWriteLoader.stream()
                                                                           .collect(Collectors.toList())));

        // Load Archive Services
        serviceReadLoader.stream()
                         .map(ServiceLoader.Provider::get)
                         .filter(ArchiveService::isEnabled)
                         .forEach(ZipState::addArchiveProvider);

        serviceWriteLoader.stream()
                          .map(ServiceLoader.Provider::get)
                          .filter(ArchiveService::isEnabled)
                          .forEach(ZipState::addArchiveProvider);

        // Adding resource bundles
        serviceReadLoader.stream()
                         .map(ServiceLoader.Provider::get)
                         .filter(s -> s.isEnabled() && s.getResourceBundle().isPresent())
                         .forEach(s -> PLUGIN_BUNDLES.add(s.getResourceBundle().get()));

        serviceWriteLoader.stream()
                          .map(ServiceLoader.Provider::get)
                          .filter(s -> s.isEnabled() && s.getResourceBundle().isPresent())
                          .forEach(s -> PLUGIN_BUNDLES.add(s.getResourceBundle().get()));
    }

    /**
     *   Loads services that come as default with additionally the ones specified in the given module path.
     *
     *   @param modulePath The directory to scan for java modules
     */
    public static void loadModulesDynamic(Path modulePath) {
        try {
            URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{modulePath.toUri().toURL()});
            ModuleFinder moduleFinder = ModuleFinder.of(modulePath);
            Configuration moduleConfig = Configuration.resolveAndBind(moduleFinder,
                                                                      List.of(ModuleLayer.boot()
                                                                                         .configuration()),
                                                                      moduleFinder,
                                                                      moduleFinder.findAll()
                                                                                  .stream()
                                                                                  .map(m -> m.descriptor()
                                                                                             .name())
                                                                                  .collect(
                                                                                          Collectors.toSet()));

            ModuleLayer moduleLayer =
                    ModuleLayer.defineModulesWithOneLoader(moduleConfig, List.of(ModuleLayer.boot()), urlClassLoader)
                               .layer();
            ServiceLoader<ArchiveWriteService> serviceWriteLoader = ServiceLoader.load(moduleLayer,
                                                                                       ArchiveWriteService.class);
            ServiceLoader<ArchiveReadService> serviceReadLoader = ServiceLoader.load(moduleLayer,
                                                                                     ArchiveReadService.class);

            // LOG: ArchiveReadService implementation identified: %s
            LoggingConstants.ROOT_LOGGER.info(resolveTextKey(LOG_READ_SERVICES_IDENTIFIED,
                                                             serviceReadLoader.stream()
                                                                              .map(s->s.get().getClass().getCanonicalName())
                                                                              .collect(Collectors.toList())));

            // LOG: ArchiveWriteService implementation identified: %s
            LoggingConstants.ROOT_LOGGER.info(resolveTextKey(LOG_WRITE_SERVICES_IDENTIFIED,
                                                             serviceWriteLoader.stream()
                                                                               .map(s->s.get().getClass().getCanonicalName())
                                                                               .collect(Collectors.toList())));

            // Load Archive Services
            serviceReadLoader.stream()
                             .map(ServiceLoader.Provider::get)
                             .filter(ArchiveService::isEnabled)
                             .forEach(ZipState::addArchiveProvider);

            serviceWriteLoader.stream()
                              .map(ServiceLoader.Provider::get)
                              .filter(ArchiveService::isEnabled)
                              .forEach(ZipState::addArchiveProvider);

            // Adding resource bundles
            serviceReadLoader.stream()
                             .map(ServiceLoader.Provider::get)
                             .filter(s -> s.isEnabled() && s.getResourceBundle()
                                                            .isPresent())
                             .forEach(s -> PLUGIN_BUNDLES.add(s.getResourceBundle()
                                                               .get()));

            serviceWriteLoader.stream()
                              .map(ServiceLoader.Provider::get)
                              .filter(s -> s.isEnabled() && s.getResourceBundle()
                                                             .isPresent())
                              .forEach(s -> PLUGIN_BUNDLES.add(s.getResourceBundle()
                                                                .get()));
        } catch(Exception e) {
            loadModulesStatic();
        }
    }

    /**
     * Process:
     * <ol>
     *     <li>Run checks on pzax file
     *     <ol type="I">
     *         <li>Expand pzax file as zip archive to temp location</li>
     *         <li>Check manifest file exists</li>
     *         <li>Check files in manifest exist in extracted archive</li>
     *         <li>Check hashes, if exists against hashed file</li>
     *     </ol>
     * </li>
     * <li>Present licenses for providers for user to agree (Reuse License Details fxml)</li>
     * <li>Load modules into classpath</li>
     * </ol>
     */
    public static void loadModuleFromExtensionPackage(Path pzaxArchive) {
        // pzax package checks
        final long startTime = System.currentTimeMillis();
        Path tempDir = Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath()
                                                        .toString(),
                                 String.format("pz%d", startTime));
        Path tempArchive = Paths.get(tempDir.toAbsolutePath()
                                            .toString(),
                                     pzaxArchive.getFileName()
                                                .toString()
                                                .replace(".pzax", ".zip"));
        Path targetDir = Paths.get(tempDir.toAbsolutePath()
                                          .toString(), "output");
        try {
            Files.createDirectories(targetDir);
            // Copy and extract files to temporary location
            Files.copy(pzaxArchive, tempArchive, StandardCopyOption.REPLACE_EXISTING);
            Optional<ArchiveReadService> optReadService = ZipState.getReadArchiveServiceForFile(tempArchive.toString());
            Optional<ArchiveWriteService> optWriteService =
                    ZipState.getWriteArchiveServiceForFile(tempArchive.toString());

            if (optReadService.isPresent() && optWriteService.isPresent()) {
                ArchiveReadService readService = optReadService.get();
                ArchiveWriteService writeService = optWriteService.get();
                FXArchiveInfo fxArchiveInfo = new FXArchiveInfo(tempArchive.toAbsolutePath()
                                                                           .toString(), readService,
                                                                writeService);
                extractToDirectory(startTime, fxArchiveInfo, targetDir.toFile());
            }

            // Check manifest file
            Map<String,List<Path>> files = checkManifestFile(targetDir);

            // Confirm licenses with user
            List<Path> licenses = files.get("LICENSE");
            for (Path license : licenses) {
                FrmLicenseDetailsController controller = loadLicenseDetails(license.toAbsolutePath()
                                                                                   .toString(),
                                                                            String.join("<br/>",
                                                                                        Files.readAllLines(license)),
                                                                            true);
                if (controller.getSelectedButton()
                              .equals(ButtonType.NO)) {
                    // TITLE: License declined
                    // BODY:  User declined license agreement. Library %s will not be installed.
                    raiseAlert(Alert.AlertType.INFORMATION,
                               resolveTextKey(TITLE_LICENSE_DENIED),
                               null,
                               resolveTextKey(BODY_LICENSE_DENIED, pzaxArchive),
                               null);
                    return;
                }
            }

            // Try loading libraries
            List<Path> libs = files.get("LIB");
            Path moduleDirectory = Path.of(STORE_ROOT.toAbsolutePath()
                                                     .toString(), "providers");
            for (Path lib : libs) {
                Files.copy(lib,
                           Paths.get(moduleDirectory.toAbsolutePath()
                                                    .toString(),
                                     lib.getFileName()
                                        .toString()),
                           StandardCopyOption.REPLACE_EXISTING);
            }

            loadModulesDynamic(moduleDirectory);

            // TITLE: Library installed successfully
            // BODY:  The library %s has been successfully installed.
            Optional<ButtonType> response = raiseAlert(Alert.AlertType.INFORMATION,
                                                       resolveTextKey(TITLE_LIB_INSTALLED),
                                                       null,
                                                       resolveTextKey(BODY_LIB_INSTALLED, pzaxArchive),
                                                       null);

            if (response.isPresent()) {
                Stage.getWindows()
                     .stream()
                     .filter(Stage.class::isInstance)
                     .filter(s -> ((Stage) s).getTitle()
                                             .equals(
                                                     resolveTextKey(TITLE_OPTIONS_PATTERN)))
                     .findFirst()
                     .ifPresent(window -> window
                             .fireEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSE_REQUEST)));
            }
        } catch(Exception e) {
            // LOG: Issue loading PZAX archive: %s. \nMessage: %s\nStack trace:\n%s
            // TITLE: ERROR: Issue ingesting PZAX archive
            // HEADER: PZAX Archive %s was not consumed successfully
            // BODY: Please check the exception stack trace below and ensure the plugin has not been corrupted.

            ROOT_LOGGER.error(resolveTextKey(LOG_ISSUE_LOAD_LIB,
                                             pzaxArchive,
                                             e.getMessage(),
                                             getStackTraceFromException(e)));

            JFXUtil.runLater(() -> raiseAlert(Alert.AlertType.ERROR, resolveTextKey(TITLE_ISSUE_LOAD_LIB),
                                              resolveTextKey(HEADER_ISSUE_LOAD_LIB, pzaxArchive),
                                              resolveTextKey(BODY_ISSUE_LOAD_LIB),
                                              e,
                                              null));
        } finally {
            deleteDirectory(tempDir, (b) -> false);
            if (Stage.getWindows().size() == 0) {
                JFXUtil.runLater(POST_PZAX_COMPLETION_CALLBACK);
            }
        }
    }

    public static Map<String,List<Path>> checkManifestFile(Path targetDir) throws Exception {
        Map<String,List<Path>> files = new HashMap<>();
        Path manifestFile = Paths.get(targetDir.toAbsolutePath()
                                               .toString(), "MF");
        try(Scanner scanner = new Scanner(manifestFile)) {
            while (scanner.hasNextLine()) {
                String[] config = COLONSV.split(scanner.nextLine());

                if (config.length > 0) {
                    switch(config[0]) {
                        case "min-version":
                            int result = VersionComparator.getInstance()
                                                          .compare(config[1],
                                                                   System.getProperty(CNS_NTAK_PEARL_ZIP_VERSION, "0.0.0.0")
                                                          );
                            if (result > 0) {
                                // LOG: PZAX archive requires a newer version of PearlZip (Minimum version supported: %s)
                                throw new Exception(resolveTextKey(LOG_VERSION_MIN_VERSION_BREACH, config[1]));
                            }
                            break;

                        case "max-version":
                            result = VersionComparator.getInstance()
                                                          .compare(config[1],
                                                                   System.getProperty(CNS_NTAK_PEARL_ZIP_VERSION, "0.0.0.0")
                                                          );
                            if (result < 0) {
                                // LOG: PZAX archive requires an older version of PearlZip (Maximum version supported: %s)
                                throw new Exception(resolveTextKey(LOG_VERSION_MAX_VERSION_BREACH, config[1]));
                            }
                            break;

                        case "license":
                            Path licenseFile = Paths.get(targetDir.toAbsolutePath()
                                                                  .toString(),
                                                         config[1]);
                            if (!Files.exists(licenseFile)) {
                                // LOG: Required license file (%s) does not exist.
                                throw new Exception(resolveTextKey(LOG_REQUIRED_LICENSE_FILE_NOT_EXIST, licenseFile));
                            }
                            files.putIfAbsent("LICENSE", new LinkedList<>());
                            List<Path> licenses = files.get("LICENSE");
                            licenses.add(licenseFile);
                            break;

                        case "lib-file":
                            Path libFile = Paths.get(targetDir.toAbsolutePath()
                                                              .toString(),
                                                     config[2]);
                            String hashFormat = config[1];

                            // Check hash, if required
                            if (!hashFormat.equals("N/A")) {
                                Path digestFile = Paths.get(targetDir.toAbsolutePath()
                                                                     .toString(),
                                                            config[2].replace(".jar",
                                                                              String.format(".%s",
                                                                                            hashFormat)));
                                MessageDigest digest = MessageDigest.getInstance(hashFormat);
                                String calculatedHash = HexFormat.of()
                                                                 .formatHex(digest.digest(Files.readAllBytes(libFile)));
                                String referenceHash = Files.readString(digestFile);
                                if (!calculatedHash.equals(referenceHash.trim())) {
                                    // LOG: Calculated hash (%s) does not match the expected
                                    // reference (%s)
                                    // value. Integrity check failed for library: %s.
                                    throw new Exception(resolveTextKey(LOG_HASH_INTEGRITY_FAILURE,
                                                                       calculatedHash, referenceHash,
                                                                       libFile));
                                }
                            }
                            files.putIfAbsent("LIB", new LinkedList<>());
                            List<Path> libs = files.get("LIB");
                            libs.add(libFile);
                            break;
                        default:
                    }
                }
            }
        }

        return files;
    }
}
