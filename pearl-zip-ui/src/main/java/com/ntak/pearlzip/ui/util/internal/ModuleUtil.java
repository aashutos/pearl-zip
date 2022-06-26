/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.util.internal;

import com.ntak.pearlzip.archive.constants.ConfigurationConstants;
import com.ntak.pearlzip.archive.constants.LoggingConstants;
import com.ntak.pearlzip.archive.model.PluginInfo;
import com.ntak.pearlzip.archive.pub.*;
import com.ntak.pearlzip.archive.pub.profile.component.GeneralComponent;
import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.model.ZipState;
import com.ntak.pearlzip.ui.pub.FrmLicenseDetailsController;
import com.ntak.pearlzip.ui.util.JFXUtil;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Pair;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.spi.ResourceBundleProvider;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.archive.constants.ArchiveConstants.WORKING_APPLICATION_SETTINGS;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.ROOT_LOGGER;
import static com.ntak.pearlzip.archive.constants.internal.LoggingConstants.PLUGIN_BUNDLES;
import static com.ntak.pearlzip.archive.util.LoggingUtil.*;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.ArchiveUtil.deleteDirectory;
import static com.ntak.pearlzip.ui.util.ArchiveUtil.extractToDirectory;
import static com.ntak.pearlzip.ui.util.JFXUtil.raiseAlert;
import static com.ntak.pearlzip.ui.util.internal.JFXUtil.loadLicenseDetails;
import static java.nio.file.FileVisitResult.CONTINUE;

/**
 *  Utility methods within this class are utilised by PearlZip to load Archive Service implementations.
 */
public class ModuleUtil {

    private static Map<String, PluginInfo> PLUGINS_METADATA = InternalContextCache.INTERNAL_CONFIGURATION_CACHE.<Map<String, PluginInfo> >getAdditionalConfig(CK_PLUGINS_METADATA).get();

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
                         .filter(s -> s.isEnabled() && s.getArchiveServiceProfile()
                                                        .getComponent(GeneralComponent.class)
                                                        .orElse(new GeneralComponent(Collections.emptySet(), Collections.emptySet(), null))
                                                        .getResourceBundle()
                                                        .isPresent())
                         .forEach(s -> PLUGIN_BUNDLES.add(s.getArchiveServiceProfile()
                                                           .getComponent(GeneralComponent.class)
                                                           .orElse(new GeneralComponent(Collections.emptySet(), Collections.emptySet(), null))
                                                           .getResourceBundle()
                                                           .get()));

        serviceWriteLoader.stream()
                          .map(ServiceLoader.Provider::get)
                          .filter(s -> s.isEnabled() && s.getArchiveServiceProfile()
                                                         .getComponent(GeneralComponent.class)
                                                         .orElse(new GeneralComponent(Collections.emptySet(), Collections.emptySet(), null))
                                                         .getResourceBundle()
                                                         .isPresent())
                          .forEach(s -> PLUGIN_BUNDLES.add(s.getArchiveServiceProfile()
                                                            .getComponent(GeneralComponent.class)
                                                            .orElse(new GeneralComponent(Collections.emptySet(), Collections.emptySet(), null))
                                                            .getResourceBundle()
                                                            .get()));
    }

    /**
     *   Loads services that come as default with additionally the ones specified in the given module path.
     *
     *   @param modulePath The directory to scan for java modules
     */
    public static void loadModulesDynamic(Path modulePath) {
        // Safe mode execution...
        if (System.getProperty(CNS_NTAK_PEARL_ZIP_SAFE_MODE,"false").equals("true")) {
            loadModulesStatic();
            return;
        }

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
                             .filter(s -> s.isEnabled() && s.getArchiveServiceProfile()
                                                            .getComponent(GeneralComponent.class)
                                                            .orElse(new GeneralComponent(Collections.emptySet(), Collections.emptySet(), null))
                                                            .getResourceBundle()
                                                            .isPresent())
                             .forEach(s -> PLUGIN_BUNDLES.add(s.getArchiveServiceProfile()
                                                               .getComponent(GeneralComponent.class)
                                                               .orElse(new GeneralComponent(Collections.emptySet(), Collections.emptySet(), null))
                                                               .getResourceBundle()
                                                               .get()));

            serviceWriteLoader.stream()
                              .map(ServiceLoader.Provider::get)
                              .filter(s -> s.isEnabled() && s.getArchiveServiceProfile()
                                                             .getComponent(GeneralComponent.class)
                                                             .orElse(new GeneralComponent(Collections.emptySet(), Collections.emptySet(), null))
                                                             .getResourceBundle()
                                                             .isPresent())
                              .forEach(s -> PLUGIN_BUNDLES.add(s.getArchiveServiceProfile()
                                                                .getComponent(GeneralComponent.class)
                                                                .orElse(new GeneralComponent(Collections.emptySet(), Collections.emptySet(), null))
                                                                .getResourceBundle()
                                                                .get()));
        } catch(Exception e) {
            Path APPLICATION_SETTINGS_FILE =
                    InternalContextCache.GLOBAL_CONFIGURATION_CACHE
                            .<Path>getAdditionalConfig(CK_APPLICATION_SETTINGS_FILE)
                            .get();
            System.setProperty(CNS_NTAK_PEARL_ZIP_SAFE_MODE, "true");
            WORKING_APPLICATION_SETTINGS.setProperty(CNS_NTAK_PEARL_ZIP_SAFE_MODE, "true");
            try(OutputStream bw = Files.newOutputStream(APPLICATION_SETTINGS_FILE)) {
                WORKING_APPLICATION_SETTINGS.store(bw, String.format(CNS_PROP_HEADER,
                                                                     LocalDateTime.now()));
            } catch(IOException ex) {
            }

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
        final Path STORE_ROOT = InternalContextCache.GLOBAL_CONFIGURATION_CACHE
                                                    .<Path>getAdditionalConfig(CK_STORE_ROOT)
                                                    .get();
        final Path LOCAL_MANIFEST_DIR = InternalContextCache.GLOBAL_CONFIGURATION_CACHE
                                                            .<Path>getAdditionalConfig(CK_LOCAL_MANIFEST_DIR)
                                                            .get();

        // pzax package checks
        final long startTime = System.currentTimeMillis();
        Path tempDir = Paths.get(InternalContextCache.GLOBAL_CONFIGURATION_CACHE
                                         .<Path>getAdditionalConfig(CK_LOCAL_TEMP)
                                         .get()
                                         .toAbsolutePath()
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
            final Path srcMF = Paths.get(targetDir.toAbsolutePath()
                                                  .toString(), MANIFEST_FILE_NAME);
            PluginInfo info = parseManifest(srcMF).get();
            String name = info.getName();
            checkManifest(info, targetDir);

            // Confirm licenses with user
            List<Path> licenses =
                    info.getLicenses()
                        .stream()
                        .map(l -> Paths.get(targetDir.toAbsolutePath().toString(), l))
                        .collect(Collectors.toList());

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
            List<Path> libs = info.getDependencies()
                                  .stream()
                                  .map(l -> Paths.get(targetDir.toAbsolutePath().toString(), l))
                                  .collect(Collectors.toList());
            Path moduleDirectory = Path.of(STORE_ROOT.toAbsolutePath()
                                                     .toString(), "providers");

            for (Path lib : libs) {
                // Delete older version libs - if automatically managed
                if (Objects.isNull(info.getProperties().get(KEY_MANIFEST_DELETED))) {
                    purgeLibrary(moduleDirectory, lib, name);
                }

                // Copy files across
                Files.copy(lib,
                           Paths.get(moduleDirectory.toAbsolutePath()
                                                    .toString(),
                                     lib.getFileName()
                                        .toString()),
                           StandardCopyOption.REPLACE_EXISTING);

                // Delete old version and current version manifests from previous installs
                String rootMF = Paths.get(LOCAL_MANIFEST_DIR.toAbsolutePath()
                                                          .toString(),
                                           lib.getFileName().toString()
                                              .replaceAll("\\d(\\.\\d)+\\.*.jar", ".*")
                                      )
                                      .toString();
                Files.list(LOCAL_MANIFEST_DIR)
                     .filter(m -> m.toAbsolutePath()
                                   .toString()
                                   .matches(rootMF)
                     )
                     .forEach(ModuleUtil::safeDeletePath);
            }

            // Copy themes to local directory
            for (String theme : info.getThemes()) {
                Path localThemeDir = Paths.get(STORE_ROOT.toAbsolutePath().toString(), "themes");
                if (Files.exists(localThemeDir.resolve(theme))) {
                    Files.walkFileTree(localThemeDir.resolve(theme), new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                            Files.deleteIfExists(dir);
                            return CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            Files.deleteIfExists(file);
                            return CONTINUE;
                        }
                    });
                }
                Files.createDirectories(localThemeDir);
                Files.copy(targetDir.resolve(theme),
                           localThemeDir.resolve(theme),
                           StandardCopyOption.REPLACE_EXISTING);

                // N.B. SimpleFileVisitor implementation taken from javadoc
                Files.walkFileTree(Paths.get(targetDir.toAbsolutePath().toString(), theme), new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                            throws IOException {
                        Path targetdir = localThemeDir.resolve(targetDir.relativize(dir));
                        try {
                            Files.copy(dir, targetdir);
                        } catch(FileAlreadyExistsException e) {
                            if (!Files.isDirectory(targetdir)) {
                                throw e;
                            }
                        }
                        return CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                            throws IOException {
                        Files.copy(file,
                                   localThemeDir.resolve(targetDir.relativize(file)),
                                   StandardCopyOption.REPLACE_EXISTING);
                        return CONTINUE;
                    }
                });
            }

             Files.createDirectories(LOCAL_MANIFEST_DIR);
            final Path localMF = Paths.get(LOCAL_MANIFEST_DIR.toString(),
                                      String.format("%s.%s",
                                                    pzaxArchive.getFileName()
                                                               .toString(),
                                                    MANIFEST_FILE_NAME));
            Files.copy(srcMF,
                       localMF
                    , StandardCopyOption.REPLACE_EXISTING);
            synchronized(PLUGINS_METADATA) {
                PLUGINS_METADATA.put(info.getName(), info);
            }

            // Reload provider modules into PearlZip
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
                JFXUtil.runLater(InternalContextCache.INTERNAL_CONFIGURATION_CACHE.<Runnable>getAdditionalConfig(CK_POST_PZAX_COMPLETION_CALLBACK).get());
            }
        }
    }

    public static void checkManifest(PluginInfo info, Path targetDir) throws Exception {
        for (CheckManifestRule rule : InternalContextCache.INTERNAL_CONFIGURATION_CACHE.<List<CheckManifestRule>>getAdditionalConfig(CK_MANIFEST_RULES).get()) {
            rule.processManifest(info, targetDir);
        }
    }

    public static void purgeLibrary(Path moduleDirectory, Path lib, String pluginName) throws IOException {
        String rootLib = Paths.get(moduleDirectory.toAbsolutePath()
                                                  .toString(),
                                   lib.getFileName().toString()
                                      .replaceAll("\\d+(\\.\\d+)+\\.jar", ".*")
                              )
                              .toString();

        synchronized(PLUGINS_METADATA) {
            Set<Path> dependencies = PLUGINS_METADATA.entrySet()
                                                     .stream()
                                                     .filter(e -> !e.getKey()
                                                                    .equals(pluginName))
                                                     .map(Map.Entry::getValue)
                                                     .flatMap(l -> l.getDependencies()
                                                                    .stream())
                                                     .map(s -> Paths.get(moduleDirectory.toAbsolutePath()
                                                                                        .toString(), s))
                                                     .filter(p -> p.toAbsolutePath()
                                                                   .toString()
                                                                   .matches(rootLib))
                                                     .collect(Collectors.toSet());

            Files.list(moduleDirectory)
                 .filter(f -> f.toAbsolutePath()
                               .toString()
                               .matches(rootLib) && !dependencies.contains(f))
                 .forEach(ModuleUtil::safeDeletePath);
        }
    }

    public static void purgeLibraries(String moduleDirectory, Set<String> names) throws IOException {
        final Path STORE_ROOT = InternalContextCache.GLOBAL_CONFIGURATION_CACHE
                                                    .<Path>getAdditionalConfig(CK_STORE_ROOT)
                                                    .get();
        final Path LOCAL_MANIFEST_DIR = InternalContextCache.GLOBAL_CONFIGURATION_CACHE
                .<Path>getAdditionalConfig(CK_LOCAL_MANIFEST_DIR)
                .get();

        // Remove libraries...
        synchronized(PLUGINS_METADATA) {
            PLUGINS_METADATA.values()
                            .stream()
                            .filter(m -> names.contains(m.getName()))
                            .forEach(m -> {
                                try {
                                    for (String dependency : m.getDependencies()) {
                                        purgeLibrary(Paths.get(moduleDirectory),
                                                     Paths.get(moduleDirectory, dependency),
                                                     m.getName());
                                    }

                                    // Remove themes by unique key...
                                    for (String theme : m.getThemes()) {
                                        Path themePath = Paths.get(STORE_ROOT.toAbsolutePath()
                                                                             .toString(),
                                                                   "themes",
                                                                   theme
                                        );

                                        deleteDirectory(themePath, (p) -> false);
                                    }
                                } catch(IOException ex) {
                                }
                            });
        }

        // Remove manifests
        Files.list(LOCAL_MANIFEST_DIR)
             .forEach(m -> {
                 try {
                     final Optional<PluginInfo> optPluginInfo = parseManifest(m);
                     if (optPluginInfo.isPresent() && names.contains(optPluginInfo.get().getName())) {
                         Files.deleteIfExists(m);

                         synchronized(PLUGINS_METADATA) {
                             PLUGINS_METADATA.remove(optPluginInfo.get()
                                                                  .getName());
                         }
                     }
                 } catch(IOException ex) {
                 }
             });
    }

    public static void purgeAllLibraries() throws IOException {
        final Path STORE_ROOT = InternalContextCache.GLOBAL_CONFIGURATION_CACHE
                                                    .<Path>getAdditionalConfig(CK_STORE_ROOT)
                                                    .get();
        final Path LOCAL_MANIFEST_DIR = InternalContextCache.GLOBAL_CONFIGURATION_CACHE
                .<Path>getAdditionalConfig(CK_LOCAL_MANIFEST_DIR)
                .get();

        Files.list(Path.of(STORE_ROOT.toAbsolutePath()
                                     .toString(), "providers"))
             .forEach(ModuleUtil::safeDeletePath);

        Files.list(LOCAL_MANIFEST_DIR)
             .forEach(ModuleUtil::safeDeletePath);

        Path themesPath = Paths.get(STORE_ROOT.toAbsolutePath()
                                             .toString(),
                                   "themes"
        );

        Files.list(themesPath)
             .filter(d -> Files.isDirectory(d) && !CORE_THEMES.contains(d.getFileName().toString()))
             .forEach(d -> deleteDirectory(d, (p) -> false));

        synchronized(PLUGINS_METADATA) {
            PLUGINS_METADATA.clear();
        }
    }

    public static Optional<PluginInfo> parseManifest(Path manifestFile) {
        final Pattern csv = Pattern.compile(Pattern.quote(":"));
        try(Scanner scanner = new Scanner(manifestFile)) {
            String name = "";
            String minVersion = "";
            String maxVersion = "";
            List<String> hashFormats = new LinkedList<>();
            List<String> licenses = new LinkedList<>();
            List<String> dependencies = new LinkedList<>();
            List<String> themes = new LinkedList<>();
            Map<String,String> properties = new HashMap<>();

            while (scanner.hasNextLine()) {
                String[] config = csv.split(scanner.nextLine());

                if (config.length > 0) {
                    switch(config[0]) {
                        case "name":
                            name = config[1];
                            properties.put("name", name);
                            break;
                        case "min-version":
                            minVersion = config[1];
                            properties.put("min-version", minVersion);
                            break;

                        case "max-version":
                            maxVersion = config[1];
                            properties.put("max-version", maxVersion);
                            break;

                        case "license":
                            licenses.add(config[1]);
                            properties.put("license",
                                           String.join(",", licenses));
                            break;

                        case "theme":
                            themes.add(config[1]);
                            properties.put("theme",
                                           String.join(",", themes));
                            break;

                        case "lib-file":
                            String hashFormat = config[1];
                            String lib = config[2];

                            if ( !dependencies.contains(lib)) {
                                hashFormats.add(hashFormat);
                                dependencies.add(lib);
                            }
                            break;
                        default:
                            // Handle non-standard keys and remove-pattern key...
                            if (properties.containsKey(config[0])) {
                                // colon split items are concat together with commas
                                for (int i = 1; i < config.length; i++) {
                                    properties.put(config[0], String.join(",", properties.get(config[0]), config[i]));
                                }
                            } else {
                                properties.put(config[0], config[1]);
                            }
                    }
                }
            }

            properties.put("lib-file",
                           String.join(",", dependencies));
            return Optional.of(new PluginInfo(name, minVersion, maxVersion, licenses, dependencies, hashFormats, themes,
                                              properties));
        } catch(Exception e) {
        }
        return Optional.empty();
    }

    public static void safeDeletePath(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch(IOException e) {
        }
    }

    /**
     *  Utilise module system to load resource bundles using providers supplied by installed plugins. If there are
     *  multiple suitable plugins containing Resource Bundle, the bundle is taken in a non-deterministic manner.
     *
     *  @param modulePath
     *  @param baseName Base name that corresponds with resource bundle file
     *  @param locale
     *  @return ResourceBundle - Key-Value pairs holding i18n strings from resource file
     */
    public static ResourceBundle loadLangPackDynamic(Path modulePath, String baseName, Locale locale) {
        ResourceBundle bundle = null;
        Locale defaultLocale = genLocale(new Properties());
        ResourceBundle enGBBundle = ResourceBundle.getBundle(baseName,
                                          defaultLocale);

        // Safe mode execution...
        if (System.getProperty(CNS_NTAK_PEARL_ZIP_SAFE_MODE,"false").equals("true")) {
            // Use default en_GB bundle
            bundle = enGBBundle;
            return bundle;
        }

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
            ServiceLoader<PearlZipResourceBundleProvider> resourceBundleLoader = ServiceLoader.load(moduleLayer,
                                                                                                    PearlZipResourceBundleProvider.class);
            resourceBundleLoader.forEach(b -> FXCollections.observableArrayList(InternalContextCache.INTERNAL_CONFIGURATION_CACHE.<Set<Pair<String,Locale>>>getAdditionalConfig(CK_LANG_PACKS).get().addAll(b.providedLanguages())));
            for (ResourceBundleProvider prov : resourceBundleLoader) {
                final Set<String> diffLoggingKeys = new HashSet<>(enGBBundle.keySet());
                if (Objects.nonNull(bundle = prov.getBundle(baseName, locale))) {
                    diffLoggingKeys.removeAll(bundle.keySet());
                    if (diffLoggingKeys.size() == 0) {
                        return bundle;
                    } else {
                        // Warn of potential missing keys. If UI key is missing, the program may not start...
                        if (!bundle.getLocale().toString().isEmpty()) {
                            ROOT_LOGGER.warn(resolveTextKey(LOG_MISSING_KEYS_LANG_PACK, diffLoggingKeys, locale));
                            return bundle;
                        }

                        bundle = null;
                    }
                }
            }

            // Use default bundle if none found...
            if (Objects.isNull(bundle)) {
                throw new Exception();
            }
        } catch (Exception e) {
            // Use default en_GB bundle
            bundle = enGBBundle;

            // Set locale for application.
            WORKING_APPLICATION_SETTINGS.setProperty(ConfigurationConstants.CNS_LOCALE_LANG, "en");
            WORKING_APPLICATION_SETTINGS.setProperty(ConfigurationConstants.CNS_LOCALE_COUNTRY, "GB");
            WORKING_APPLICATION_SETTINGS.remove(ConfigurationConstants.CNS_LOCALE_VARIANT);
            Path APPLICATION_SETTINGS_FILE =
                    InternalContextCache.GLOBAL_CONFIGURATION_CACHE
                            .<Path>getAdditionalConfig(CK_APPLICATION_SETTINGS_FILE)
                            .get();
            try (OutputStream wr = Files.newOutputStream(APPLICATION_SETTINGS_FILE, StandardOpenOption.WRITE)){
                WORKING_APPLICATION_SETTINGS.store(wr, String.format(CNS_PROP_HEADER,
                                                                     LocalDateTime.now()));
            } catch(IOException exc) {
            }

            System.setProperty(ConfigurationConstants.CNS_LOCALE_LANG, "en");
            System.setProperty(ConfigurationConstants.CNS_LOCALE_COUNTRY, "GB");
        }

        return bundle;
    }
}
