/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.util;

import com.ntak.pearlzip.archive.pub.*;
import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.model.ZipState;
import com.ntak.pearlzip.ui.pub.FrmMainController;
import com.ntak.pearlzip.ui.pub.FrmProgressController;
import com.ntak.pearlzip.ui.pub.ZipLauncher;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.archive.constants.ArchiveConstants.WORKING_APPLICATION_SETTINGS;
import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.*;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.*;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.JFXUtil.raiseAlert;

/**
 *  Useful utility methods used by the UI (and accessible by downstream) to trigger and manage archiving processes.
 *  @author Aashutos Kakshepati
 */
public class ArchiveUtil {
    private static final Logger LOGGER = LoggerContext.getContext().getLogger(ArchiveUtil.class);

    public static void extractToDirectory(long sessionId, FXArchiveInfo fxArchiveInfo, File dir) {
        ArchiveReadService archiveReadService = fxArchiveInfo.getReadService();

        if (Objects.nonNull(dir) && dir.exists()) {
            Map<Integer,List<FileInfo>> mapFiles =
                    fxArchiveInfo.getFiles().stream().collect(Collectors.groupingBy(FileInfo::getLevel));

            for (int level : mapFiles.keySet().stream().sorted().collect(Collectors.toList())) {
                List<FileInfo> files = mapFiles.getOrDefault(level, Collections.emptyList());
                files.stream().filter(FileInfo::isFolder).forEach(f-> {
                    try {
                        Files.createDirectory(Paths.get(dir.getAbsolutePath(), f.getFileName()));
                    } catch(IOException e) {
                    }
                });

                files.stream().filter(f -> !f.isFolder()).forEach(f -> archiveReadService.extractFile(sessionId,
                                                Paths.get(dir.getAbsolutePath(),
                                                         Paths.get(f.getFileName()).toString()),
                                                fxArchiveInfo.getArchiveInfo(),
                                                f)
                );
            }
        }
    }

    public static Path createBackupArchive(FXArchiveInfo fxArchiveInfo, Path tempDir) throws IOException {
        Path backupArchive =  Paths.get(tempDir.toString(),
                                     Paths.get(fxArchiveInfo.getArchivePath()).getFileName().toString());
        Files.copy(Path.of(fxArchiveInfo.getArchivePath()), backupArchive, StandardCopyOption.REPLACE_EXISTING);
        return backupArchive;
    }

    public static boolean restoreBackupArchive(Path backupArchive, Path targetLocation) {
        try {
            if (Objects.nonNull(backupArchive) && Objects.nonNull(targetLocation) && Files.exists(backupArchive)) {
                Files.copy(backupArchive, targetLocation, StandardCopyOption.REPLACE_EXISTING);
                if (!backupArchive.equals(targetLocation)) {
                    Files.deleteIfExists(backupArchive);
                }
                return true;
            }

            return false;
        } catch(Exception e) {
            return false;
        }
    }

    public static void removeBackupArchive(Path tempArchive) throws IOException {
        Files.deleteIfExists(tempArchive);
        if (tempArchive.getParent().getFileName().toString().matches(REGEX_TIMESTAMP_DIR) && Files.list(tempArchive.getParent())
                                                                                                  .findAny()
                                                                                                  .isEmpty()) {
            Files.deleteIfExists(tempArchive.getParent());
        }
    }

    public static void deleteDirectory(Path d, Predicate<Path> exclusionPattern) {
        try {
            // Delete all files in directory
            Files.walk(d)
                 .filter(p -> !Files.isDirectory(p) && !exclusionPattern.test(p))
                 .forEach(p -> {
                     try {
                         Files.deleteIfExists(p);
                     } catch(IOException ioException) {
                     }
                 });

            // Delete nested directories
            Files.walk(d)
                 .filter(Files::isDirectory)
                 .sorted(Comparator.comparingInt((Path f) -> f.toAbsolutePath()
                                                              .toString()
                                                              .length()).reversed())
                 .forEach(p -> {
                     try {
                         Files.deleteIfExists(p);
                     } catch(IOException ioException) {
                     }
                 });

            // Delete top-level directory itself
            Files.deleteIfExists(d);
        } catch(IOException ioe) {
        }
    }

    public static void addDirectory(long sessionId, FXArchiveInfo fxArchiveInfo, File dirToAdd) throws IOException {
        int depth = fxArchiveInfo.getDepth()
                                 .get();
        int index = fxArchiveInfo.getFiles()
                                 .size();
        String prefix = fxArchiveInfo.getPrefix();
        Path dirPath = dirToAdd.toPath();
        ArchiveWriteService archiveWriteService = fxArchiveInfo.getWriteService();

        Path tempDir = Files.createTempDirectory(TMP_DIR_PREFIX);
        Path tempArchive = createBackupArchive(fxArchiveInfo, tempDir);

        List<FileInfo> files = com.ntak.pearlzip.ui.util.internal.ArchiveUtil.handleDirectory(prefix, dirPath.getParent(), dirPath, depth + 1, index);
        files.add(new FileInfo((index + 1), depth,
                               depth > 0 ? String.format("%s/%s", prefix,
                                                         dirPath.getFileName()
                                                                .toString()) : dirPath.getFileName()
                                                                                      .toString(),
                               -1, 0,
                               0, null,
                               null, null,
                               "", "", 0, "",
                               true, false,
                               Collections.singletonMap(KEY_FILE_PATH, dirPath.toString())));

        if (files.removeIf(f -> f.getAdditionalInfoMap()
                                 .getOrDefault(KEY_FILE_PATH, "")
                                 .equals(fxArchiveInfo.getArchivePath()))) {
            // LOG: Skipping the addition of this archive within itself...
            LOGGER.warn(resolveTextKey(LOG_SKIP_ADD_SELF));
        }

        boolean success = archiveWriteService.addFile(sessionId, fxArchiveInfo.getArchiveInfo(),
                                                      files.toArray(new FileInfo[0]));
        if (!success) {
            restoreBackupArchive(tempArchive,
                                 Paths.get(fxArchiveInfo.getArchivePath()));
            JFXUtil.runLater(fxArchiveInfo::refresh);

            // LOG: Issue adding directory %s
            // TITLE: ERROR: Failed to add directory to archive
            // HEADER: Directory %s could not be added to archive %s
            // BODY: Archive has been reverted to the last stable state.
            LOGGER.error(resolveTextKey(LOG_ISSUE_ADDING_DIR, dirToAdd.getAbsolutePath()));
            raiseAlert(Alert.AlertType.ERROR,
                       resolveTextKey(TITLE_ISSUE_ADDING_DIR),
                       resolveTextKey(HEADER_ISSUE_ADDING_DIR),
                       resolveTextKey(BODY_ISSUE_ADDING_DIR),
                       null
            );
        }

        removeBackupArchive(tempArchive);
    }

    public static void addFile(long sessionId, FXArchiveInfo fxArchiveInfo, File rawFile, String fileName) throws IOException {
        int depth = fxArchiveInfo.getDepth().get();
        int index = fxArchiveInfo.getFiles().size();
        String prefix = fxArchiveInfo.getPrefix();

        Path tempDir = Files.createTempDirectory(TMP_DIR_PREFIX);
        Path tempArchive = createBackupArchive(fxArchiveInfo, tempDir);

        ArchiveWriteService service = ZipState.getWriteArchiveServiceForFile(
               fxArchiveInfo.getArchivePath()).get();
        boolean success;
        if (rawFile.isFile()) {
            FileInfo fileToAdd = new FileInfo(fxArchiveInfo.getFiles()
                                                           .size(),
                                              fxArchiveInfo.getDepth()
                                                           .get(),
                                              fileName,
                                              -1,
                                              0,
                                              rawFile.getTotalSpace(),
                                              LocalDateTime.ofInstant(Instant.ofEpochMilli(
                                                                              rawFile.lastModified()),
                                                                      ZoneId.systemDefault()),
                                              null,
                                              null,
                                              null,
                                              null,
                                              0,
                                              "",
                                              !rawFile.isFile(),
                                              false,
                                              Collections.singletonMap(
                                                      KEY_FILE_PATH,
                                                      rawFile.getAbsoluteFile()
                                                             .getPath()));
            success = service.addFile(sessionId,
                                      fxArchiveInfo.getArchiveInfo(),
                                      fileToAdd);
        } else { // Mac App is a directory
            List<FileInfo> files = com.ntak.pearlzip.ui.util.internal.ArchiveUtil.handleDirectory(prefix,
                                                                                                  rawFile.toPath().getParent(), rawFile.toPath(), depth +1,
                                                                                                  index);
            success = service.addFile(sessionId,
                                      fxArchiveInfo.getArchiveInfo(),
                                      files.toArray(new FileInfo[0]));
        }

        if (!success) {
            restoreBackupArchive(tempArchive,
                                 Paths.get(fxArchiveInfo.getArchivePath()));
            JFXUtil.runLater(fxArchiveInfo::refresh);

            // LOG: Issue adding file %s
            // TITLE: ERROR: Failed to add file to archive
            // HEADER: File %s could not be added to archive %s
            // BODY: Archive has been reverted to the last stable state.
            LOGGER.error(resolveTextKey(LOG_ISSUE_ADDING_FILE,
                                                                                       rawFile.getAbsolutePath()));
            raiseAlert(Alert.AlertType.ERROR,
                       resolveTextKey(TITLE_ISSUE_ADDING_FILE),
                       resolveTextKey(HEADER_ISSUE_ADDING_FILE),
                       resolveTextKey(BODY_ISSUE_ADDING_FILE),
                       null
            );
        }
    }

    public static Stage launchMainStage(FXArchiveInfo fxArchiveInfo) {
        return launchMainStage(new Stage(), fxArchiveInfo);
    }

    public static void checkArchiveExists(FXArchiveInfo archiveInfo) throws AlertException {
        // File locked...
        final Path archivePath = Path.of(archiveInfo.getArchivePath());
        if (!Files.exists(archivePath) && !Files.notExists(archivePath)) {
            // LOG: Archive %s is locked and cannot be accessed.
            // TITLE: ERROR: Archive Locked
            // HEADER: Cannot process archive
            // BODY: Archive %s is locked by another process. PearlZip will now close the instance.
            LOGGER.error(resolveTextKey(LOG_ARCHIVE_LOCKED, archivePath.toAbsolutePath().toString()));
            throw new AlertException(archiveInfo,
                                     resolveTextKey(LOG_ARCHIVE_LOCKED, archivePath.toAbsolutePath().toString()),
                                     Alert.AlertType.ERROR,
                                     resolveTextKey(TITLE_ARCHIVE_LOCKED),
                                     resolveTextKey(HEADER_ARCHIVE_LOCKED),
                                     resolveTextKey(BODY_ARCHIVE_LOCKED, archivePath.toAbsolutePath()),
                                     null,
                                     archiveInfo.getController().get().getFileContentsView().getScene().getWindow());
        }

        // File does not exist...
        if (!Files.exists(archivePath)) {
            // LOG: Archive %s does not exist.
            // TITLE: ERROR: Archive not present
            // HEADER: Cannot process archive
            // BODY: Archive %s does not exist. PearlZip will now close the instance.
            LOGGER.error(resolveTextKey(LOG_ARCHIVE_DOES_NOT_EXIST, archivePath.toAbsolutePath().toString()));
            throw new AlertException(archiveInfo,
                                     resolveTextKey(LOG_ARCHIVE_DOES_NOT_EXIST, archivePath.toAbsolutePath().toString()),
                                     Alert.AlertType.ERROR,
                                     resolveTextKey(TITLE_ARCHIVE_DOES_NOT_EXIST),
                                     resolveTextKey(HEADER_ARCHIVE_DOES_NOT_EXIST),
                                     resolveTextKey(BODY_ARCHIVE_DOES_NOT_EXIST, archivePath.toAbsolutePath()),
                                     null,
                                     archiveInfo.getController().get().getFileContentsView().getScene().getWindow());
        }
    }

    public static void newArchive(long sessionId, ArchiveInfo archiveInfo, File archive) {
        // LOG: Creating file: %s
        LOGGER.info(resolveTextKey(LOG_CREATE_ARCHIVE, archive));
        archiveInfo.setArchivePath(archive.getAbsolutePath());

        final ArchiveWriteService writeService = ZipState.getWriteArchiveServiceForFile(archive.getName())
                                                         .get();
        writeService.createArchive(sessionId, archiveInfo);
        FXArchiveInfo fxArchiveInfo = new FXArchiveInfo(null,
                                                        archive.getAbsolutePath(),
                                                        ZipState.getReadArchiveServiceForFile(archive.getName()).get(),
                                                        writeService,
                                                        archiveInfo,
                                                        null);
        JFXUtil.runLater(() -> launchMainStage(fxArchiveInfo));
        com.ntak.pearlzip.ui.util.internal.ArchiveUtil.addToRecentFile(archive);
    }

    public static boolean openFile(File file) {
        try {
            // Initialise Stage
            final ArchiveReadService readService = ZipState.getReadArchiveServiceForFile(file.getName())
                                                           .get();
            FXArchiveInfo newFxArchiveInfo = new FXArchiveInfo(file.getAbsolutePath(),
                                                               readService,
                                                               ZipState.getWriteArchiveServiceForFile(file.getName()).orElse(null)
            );

            if (!readService.testArchive(System.currentTimeMillis(), file.getAbsolutePath())) {
                throw new Exception(resolveTextKey(LOG_ARCHIVE_TEST_FAILED, file.getAbsolutePath()));
            }

            com.ntak.pearlzip.ui.util.internal.ArchiveUtil.checkPreOpenDialog(readService, newFxArchiveInfo.getArchiveInfo());

            JFXUtil.runLater(() -> launchMainStage(newFxArchiveInfo));
            com.ntak.pearlzip.ui.util.internal.ArchiveUtil.addToRecentFile(file);

            return true;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return false;
        }
    }

    public static Stage launchMainStage(Stage stage, FXArchiveInfo fxArchiveInfo) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(com.ntak.pearlzip.ui.util.internal.ArchiveUtil.class.getClassLoader()
                                                                                   .getResource("frmMain.fxml"));
            loader.setResources(LOG_BUNDLE);
            VBox root = loader.load();

            FrmMainController controller = loader.getController();
            controller.initData(stage, fxArchiveInfo);
            fxArchiveInfo.setMainController(controller);

            Scene scene = new Scene(root, ZipState.WIDTH, ZipState.HEIGHT);
            stage.setScene(scene);
            stage.setResizable(Boolean.parseBoolean(System.getProperty(CNS_NTAK_PEARL_ZIP_RESIZEABLE, "false")));
            String appName = System.getProperty(CNS_NTAK_PEARL_ZIP_APP_NAME, "PearlZip");
            String version = System.getProperty(CNS_NTAK_PEARL_ZIP_VERSION, "0.0.0.0");

            stage.setTitle(resolveTextKey(TITLE_FILE_PATTERN, appName, version,
                                                           fxArchiveInfo.getArchivePath()));

            stage.show();
            stage.toFront();
        } catch (Exception e) {
        } finally {
            // Safe mode enabled warning
            // TITLE: Safe Mode Enabled
            // BODY: There was an issue in start up so safe mode has been enabled. Some plugins may need to be removed.
            if (JFXUtil.getMainStageInstances().size() == 1 &&
                    WORKING_APPLICATION_SETTINGS.getProperty(CNS_NTAK_PEARL_ZIP_SAFE_MODE,"false").equals("true")) {
                JFXUtil.runLater(() -> {
                    ROOT_LOGGER.error(WORKING_APPLICATION_SETTINGS.getProperty(CNS_NTAK_PEARL_ZIP_SAFE_MODE, "false"));
                    raiseAlert(Alert.AlertType.WARNING,
                               resolveTextKey(TITLE_SAFE_MODE_ENABLED), null,
                               resolveTextKey(BODY_SAFE_MODE_ENABLED), stage);
                });

                com.ntak.pearlzip.ui.util.internal.JFXUtil.setSafeModeTitles(true, stage);
            }
        }

        return stage;
    }

    public static CountDownLatch launchProgress(long sessionId, Stage parent, CountDownLatch latch,
            Consumer<Stage> callback) {
        try {
            Stage progressStage = new Stage();
            progressStage.initOwner(parent);
            progressStage.initModality(Modality.WINDOW_MODAL);

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(com.ntak.pearlzip.ui.util.internal.ArchiveUtil.class.getClassLoader()
                                                                                   .getResource("frmProgress.fxml"));
            loader.setResources(LOG_BUNDLE);
            AnchorPane pane = loader.load();
            final Scene scene = new Scene(pane);
            progressStage.setScene(scene);
            progressStage.setX(parent.getX() + (parent.getWidth()/2) - (pane.getMinWidth()/2));
            progressStage.setY(parent.getY() + (parent.getHeight()/2) - (pane.getMinHeight()/2));

            FrmProgressController controller = loader.getController();
            controller.initData(progressStage, latch, callback, sessionId);
            progressStage.initStyle(StageStyle.UNDECORATED);

            JFXUtil.runLater(progressStage::show);
        } catch (Exception e) {
        }

        return  latch;
    }

    public static void openExternally(long sessionId, Stage stage, FXArchiveInfo fxArchiveInfo, FileInfo clickedRow) {
        Path selectedFile = Paths.get(clickedRow.getFileName());

        // TITLE: Confirmation: Open file externally
        // HEADER: Do you wish to open file in an external application?
        // BODY: Choosing yes will open a temporary copy of the selected file in an external application
        // as configured by the
        // Operating System.
        ButtonType response = raiseAlert(Alert.AlertType.CONFIRMATION,
                                         resolveTextKey(TITLE_OPEN_EXT_FILE),
                                         resolveTextKey(HEADER_OPEN_EXT_FILE),
                                         resolveTextKey(BODY_OPEN_EXT_FILE),
                                         null, stage,
                                         ButtonType.YES,
                                         ButtonType.NO
                                         ).orElse(null);

        if (response != null && response.equals(ButtonType.YES)) {
            try {
                Path destPath = Paths.get(Files.createTempDirectory(TMP_DIR_PREFIX).toString(),
                                          selectedFile.getFileName()
                                                      .toString());
                fxArchiveInfo.getReadService()
                             .extractFile(sessionId, destPath, fxArchiveInfo.getArchivePath(),
                                          clickedRow);
                InternalContextCache.GLOBAL_CONFIGURATION_CACHE
                                    .<HostServices>getAdditionalConfig(CK_HOST_SERVICES)
                                    .get()
                                    .showDocument(destPath.toUri()
                                                          .toString());
            } catch (Exception e) {
                // TITLE: Error: Issue opening file
                // HEADER: Could not open the selected file externally
                // BODY: An issue occurred when trying to open file %s.
                raiseAlert(Alert.AlertType.ERROR,
                           resolveTextKey(TITLE_ERR_OPEN_FILE),
                           resolveTextKey(HEADER_ERR_OPEN_FILE),
                           resolveTextKey(BODY_ERR_OPEN_FILE, selectedFile.getFileName()),
                           e,
                           stage);
            }
        }
    }

    public static File genNewArchivePath(String path, String timestamp, String archiveFormat) {
        path = path.replaceFirst(String.format("(\\.%s|\\.tar\\.%s)", archiveFormat, archiveFormat),"");

        if (ZipState.getCompressorArchives().contains(archiveFormat)
        ) {
            // tar.<ext> file format
            return new File(String.format("%s%s.tar.%s",
                                                path,
                                                timestamp,
                                                archiveFormat));
        } else {
            return new File(String.format("%s%s.%s",
                                                path,
                                                timestamp,
                                                archiveFormat));
        }
    }

    public static void extractDirectory(long sessionId, Path targetDir, FXArchiveInfo fxArchiveInfo,
            FileInfo selectedFile) {
        try {

            // List files and folders under the current directory
            Map<Boolean,List<FileInfo>> files = fxArchiveInfo.getFiles()
                                                             .stream()
                                                             .filter(f -> f.getFileName()
                                                                           .startsWith(selectedFile.getFileName()))
                                                             .collect(Collectors.partitioningBy(FileInfo::isFolder));

            long total =
                    files.values()
                         .stream()
                         .flatMap(Collection::stream)
                         .parallel()
                         .mapToLong(FileInfo::getRawSize)
                         .sum();

            // Create directories
            files.get(Boolean.TRUE)
                 .forEach(d -> {
                              try {
                                  // LOG: Creating directory %s...
                                  ArchiveService.DEFAULT_BUS.post(new ProgressMessage(sessionId,
                                                                                      PROGRESS,
                                                                                      resolveTextKey(LOG_CREATE_DIRECTORY,
                                                                                                     Paths.get(d.getFileName()).getFileName().toString()),
                                                                                      0,
                                                                                      total
                                  ));
                                  Files.createDirectories(Paths.get(targetDir.toAbsolutePath()
                                                                           .toString(),
                                                                    Paths.get(selectedFile.getFileName()).getFileName().toString(),
                                                                    Paths.get(selectedFile.getFileName())
                                                                         .relativize(Paths.get(d.getFileName()))
                                                                         .toString()));
                              } catch(IOException ex) {
                              }
                          }
                 );
            // Create files
            files.get(Boolean.FALSE)
                 .forEach(f -> fxArchiveInfo.getReadService()
                                            .extractFile(sessionId,
                                                         Paths.get(targetDir.toAbsolutePath()
                                                                          .toString().replace("/" + selectedFile.getFileName() + "/",
                                                                                          "/"),
                                                                   Paths.get(selectedFile.getFileName()).getFileName().toString(),
                                                                   Paths.get(selectedFile.getFileName())
                                                                        .relativize(Paths.get(f.getFileName()))
                                                                                         .toString()
                                                         ),
                                                         fxArchiveInfo.getArchiveInfo(),
                                                         f
                                            )
                 );
        } catch (Exception e) {

        } finally {
            // LOG: Extraction of directory %s has completed.
            ArchiveService.DEFAULT_BUS.post(new ProgressMessage(sessionId,
                                                                COMPLETED,
                                                                resolveTextKey(LOG_DIR_EXTRACT_COMPLETE,
                                                                              selectedFile.getFileName()),
                                                                -1,
                                                                0
            ));
        }
    }


    /**
     *  Utility method, which tries to retrieve a file stream using the given methods:
     *
     *  <ol>
     *      <li>ClassLoader retrieval</li>
     *      <li>Module based retrieval</li>
     *      <li>JRT Module based retrieval</li>
     *      <li>JAR extraction based retrieval</li>
     *  </ol>
     *
     *  Ideally used in try-with-resource constructs.
     *
     *  @param moduleName
     *  @param resourcePath
     *
     *  @return InputStream : Opened input stream for resource or null if unsuccessful
     */
    public static InputStream retrieveResourceStream(String moduleName, String resourcePath) {
        InputStream resource = null;

        // 1. Try standard resource retrieval...
        try {
            if ((resource = ArchiveUtil.class.getClassLoader()
                                             .getResourceAsStream(resourcePath)) != null) {
                return resource;
            }
        } catch (Exception e) {
        }

        // 2. Try Module Layer resource retrieval...
        try {
            if ((resource = ModuleLayer.boot()
                                       .findModule(moduleName)
                                       .get()
                                       .getResourceAsStream(resourcePath)) != null) {
                return resource;
            }
        } catch (Exception e) {
        }

        // 3. Try JRT resource retrieval...
        try {
            if ((resource = Files.newInputStream(InternalContextCache.INTERNAL_CONFIGURATION_CACHE
                                                         .<FileSystem>getAdditionalConfig(CK_JRT_FILE_SYSTEM)
                                                         .get()
                                                         .getPath("modules", "com.ntak.pearlzip.ui", "queries")
                                                         .toAbsolutePath())) != null) {
                return resource;
            }
        } catch (Exception e) {
        }

        // 4. Try jar extraction based retrieval...
        try {
            final String srcResPath = ZipLauncher.class.getClassLoader()
                                                      .getResource(resourcePath)
                                                      .getPath();
            Path jarArchive =
                    Paths.get(srcResPath.substring(0, srcResPath.indexOf('!'))
                                       .replaceAll("file:",""));

            Path tempDir = Files.createTempDirectory("pz");
            resource = Files.newInputStream(tempDir.resolve(resourcePath));

            try (JarFile jar = new JarFile(jarArchive.toFile())) {

                for (JarEntry entry : jar.stream()
                                         .filter(e -> e.getName().startsWith(resourcePath))
                                         .sorted(Comparator.comparingInt(a -> a.getName().length()))
                                         .toList()
                ) {
                    if (entry.isDirectory()) {
                        Path entryDest = tempDir.resolve(entry.getName());

                        if (entry.isDirectory()) {
                            Files.createDirectory(entryDest);
                            continue;
                        }

                        Files.copy(jar.getInputStream(entry), entryDest);
                    }
                }

            }
        } catch (Exception e) {
        }

        return resource;
    }
}
