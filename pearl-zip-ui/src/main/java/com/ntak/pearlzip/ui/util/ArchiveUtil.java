/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.util;

import com.ntak.pearlzip.archive.pub.*;
import com.ntak.pearlzip.ui.constants.ZipConstants;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.model.ZipState;
import com.ntak.pearlzip.ui.pub.FrmMainController;
import com.ntak.pearlzip.ui.pub.FrmProgressController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Pair;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.archive.constants.ArchiveConstants.WORKING_APPLICATION_SETTINGS;
import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.*;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.LOG_BUNDLE;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ResourceConstants.NO_FILES_HISTORY;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.JFXUtil.raiseAlert;

/**
 *  Useful utility methods used by the UI to trigger and manage archiving processes.
 *  @author Aashutos Kakshepati
*/
public class ArchiveUtil {

    private static final Logger LOGGER = LoggerContext.getContext().getLogger(ArchiveUtil.class);

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
                if (!backupArchive.toString().equals(targetLocation)) {
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
        if (tempArchive.getParent().getFileName().toString().matches(REGEX_TIMESTAMP_DIR) && Files.list(tempArchive.getParent()).count() == 0) {
            Files.deleteIfExists(tempArchive.getParent());
        }
    }

    public static List<FileInfo> handleDirectory(String prefix, Path root, Path directory, int depth, int index) throws IOException {
        List<FileInfo> files = new ArrayList<>();
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
            for (Path path : dirStream) {
                if (Files.isDirectory(path)) {
                    final List<FileInfo> subDirFiles = handleDirectory(prefix, root, path, (depth + 1), index);
                    files.addAll(subDirFiles);
                    index += subDirFiles.size();
                    if (files.stream()
                             .noneMatch(f -> f.getFileName()
                                              .equals(root.relativize(path)
                                                          .toString()))) {
                        files.add(new FileInfo(index++, depth,
                                               Paths.get(prefix,
                                                         root.relativize(path)
                                                             .toString())
                                                    .toString(), -1, 0
                                , 0, null,
                                               null, null, "", "", 0, "", true, false,
                                               Collections.singletonMap(KEY_FILE_PATH, path.toString())));
                    }
                    continue;
                }
                files.add(new FileInfo(index++, depth,
                                       Paths.get(prefix,
                                                 root.relativize(path)
                                                     .toString())
                                            .toString(),
                                       -1, 0,
                                       0, null,
                                       null, null,
                                       "", "", 0, "",
                                       false, false,
                                       Collections.singletonMap(KEY_FILE_PATH, path.toString())));
            }
        }
        return files;
    }

    public static void addToRecentFile(File file) {
        final int size = Math.min(NO_FILES_HISTORY, 15);
        String[] files = new String[size];
        try {
            List<String> currentHistory = Files.lines(RECENT_FILE)
                                               .sequential()
                                               .filter(f->!f.equals(file.getAbsolutePath()))
                                               .filter(f->Files.exists(Paths.get(f)))
                                               .limit(size-1)
                                               .collect(Collectors.toList());
            for (int i = 0; i < currentHistory.size(); i++) {
                files[i+1] = currentHistory.get(i);
            }
        } catch(IOException e) {
        }
        files[0] = file.getAbsolutePath();
        try (PrintWriter writer = new PrintWriter(Files.newOutputStream(RECENT_FILE), true)
        ) {
            for (String line : files) {
                if (Objects.nonNull(line)) {
                    writer.println(line);
                }
            }
        } catch(IOException e) {
        }
    }

    public static void refreshRecentFileMenu(Menu mnuOpenRecent) {
        Stage stage = (Stage)Stage.getWindows().stream().filter(Window::isFocused).findFirst().orElse(new Stage());
        mnuOpenRecent.getItems().clear();
        try (Scanner scanner = new Scanner(Files.newInputStream(RECENT_FILE))) {
            int i = 1;
            while (scanner.hasNext()) {
                String filePath = scanner.nextLine();
                if (Files.exists(Path.of(filePath))) {
                    MenuItem mnuFilePath = new MenuItem();
                    mnuFilePath.setText(String.format("%d. %s", i++, filePath));
                    mnuFilePath.setOnAction((e) -> {
                        final Path path = Paths.get(filePath);
                        if (Files.exists(path)) {
                            openFile(path.toFile());
                        } else {
                            // TITLE: Warning: File does not exist
                            // HEADER: The selected file does not exist
                            // BODY: The chosen file %s does not exist. It will be removed from the list.
                            raiseAlert(Alert.AlertType.WARNING,
                                       resolveTextKey(TITLE_FILE_NOT_EXIST),
                                       resolveTextKey(HEADER_FILE_NOT_EXIST),
                                       resolveTextKey(BODY_FILE_NOT_EXIST, path.toAbsolutePath().toString()),
                                       stage
                            );
                            mnuOpenRecent.hide();
                        }
                    });
                    mnuOpenRecent.getItems()
                                 .add(mnuFilePath);
                }
            }
        } catch(IOException e) {
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
        addToRecentFile(archive);
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

            checkPreOpenDialog(readService, newFxArchiveInfo.getArchiveInfo());

            JFXUtil.runLater(() -> launchMainStage(newFxArchiveInfo));
            addToRecentFile(file);

            return true;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return false;
        }
    }

    public static void checkPreOpenDialog(ArchiveReadService readService, ArchiveInfo archiveInfo) throws InterruptedException, IOException {
        Optional<ArchiveService.FXForm> optFXForm;
        if ((optFXForm = readService.getFXFormByIdentifier(ArchiveService.OPEN_ARCHIVE_OPTIONS,
                                                           archiveInfo)).isPresent()) {
            Node root = optFXForm.get().getContent();

            CountDownLatch latch = new CountDownLatch(1);
            JFXUtil.runLater(() -> {
                Stage preOpenStage = new Stage();
                JFXUtil.loadPreOpenDialog(preOpenStage, root);
                latch.countDown();
            });
            latch.await();
            Pair<AtomicBoolean,String> result = (Pair<AtomicBoolean, String>) root.getUserData();

            if (Objects.nonNull(result) && Objects.nonNull(result.getKey()) && !result.getKey().get()) {
                // LOG: Issue occurred when opening archive %s. Issue reason: %s
                // TITLE: Invalid Archive Setup
                // HEADER: Archive could not be open
                // BODY: There were issues opening the archive. Reason given by plugin for issue: %s. Check logs
                // for further details.
                LOGGER.error(resolveTextKey(LOG_INVALID_ARCHIVE_SETUP, archiveInfo.getArchivePath(),
                                            result.getValue()));

                throw new IOException(resolveTextKey(LOG_INVALID_ARCHIVE_SETUP, archiveInfo.getArchivePath(),
                                                     result.getValue()));
            }
        }
    }

    public static Stage launchMainStage(FXArchiveInfo fxArchiveInfo) {
        return launchMainStage(new Stage(), fxArchiveInfo);
    }

    public static Stage launchMainStage(Stage stage, FXArchiveInfo fxArchiveInfo) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(ArchiveUtil.class.getClassLoader()
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
            loader.setLocation(ArchiveUtil.class.getClassLoader()
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
                ZipConstants.APP.getHostServices()
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

    public static void initialiseApplicationSettings() {
        synchronized(WORKING_APPLICATION_SETTINGS) {
            try(InputStream settingsIStream = Files.newInputStream(APPLICATION_SETTINGS_FILE)) {
                WORKING_APPLICATION_SETTINGS.clear();
                WORKING_APPLICATION_SETTINGS.load(settingsIStream);
            } catch(IOException e) {
            }
        }
    }

    public static void loadPzaxPackage(String f) {
        // TITLE: Confirmation: Load Providers Module
        // BODY: Do you wish to load PZAX Module: %s?
        ButtonType response =
                raiseAlert(Alert.AlertType.CONFIRMATION,
                           resolveTextKey(TITLE_CONFIRM_LOAD_PROVIDER_MODULE),
                           null,
                           resolveTextKey(BODY_CONFIRM_LOAD_PROVIDER_MODULE, f),
                           null,
                           null,
                           ButtonType.YES,
                           ButtonType.NO
                ).get();

        if (response.equals(ButtonType.YES)) {
            ModuleUtil.loadModuleFromExtensionPackage(Paths.get(f));
        }
    }
}
