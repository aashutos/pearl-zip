/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.event.handler;

import com.ntak.pearlzip.archive.pub.ArchiveWriteService;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.model.ZipState;
import com.ntak.pearlzip.ui.util.ClearCacheRunnable;
import com.ntak.pearlzip.ui.util.JFXUtil;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.KEY_FILE_PATH;
import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.TMP_DIR_PREFIX;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.ArchiveUtil.*;
import static com.ntak.pearlzip.ui.util.JFXUtil.*;

/**
 *  Event Handler functionality, which confirms the save of temporary archives on closure.
 *  @author Aashutos Kakshepati
*/
public class ConfirmCloseEventHandler implements EventHandler<WindowEvent> {
    private static final Logger LOGGER = LoggerContext.getContext().getLogger(ConfirmCloseEventHandler.class);
    private final Stage stage;
    private final FXArchiveInfo fxArchiveInfo;

    public ConfirmCloseEventHandler(Stage stage, FXArchiveInfo fxArchiveInfo) {
        this.stage = stage;
        this.fxArchiveInfo = fxArchiveInfo;
    }

    @Override
    public void handle(WindowEvent event) {
        try {
            final Path archivePath = Paths.get(fxArchiveInfo.getArchivePath());
            if (fxArchiveInfo.getArchivePath()
                             .startsWith(STORE_TEMP.toString()) && !fxArchiveInfo.getCloseBypass().get() && Files.exists(
                    archivePath)) {
                // If a nested file from a parent archive an option is given to update it
                final String archiveFilePath = fxArchiveInfo.getArchivePath();
                final String parentFilePath = fxArchiveInfo.getParentPath();

                if (Objects.nonNull(parentFilePath)) {
                        ArchiveWriteService archiveWriteService =
                                ZipState.getWriteArchiveServiceForFile(parentFilePath)
                                        .orElse(null);
                        if (Objects.nonNull(archiveWriteService)) {
                            // Nested archive - Ask user to update parent archive
                            // TITLE: Confirmation: Reintegrate archive changes in parent archive
                            // HEADER: Do you wish to reintegrate the nested archive changes into %s
                            // BODY: Please specify if you wish to persist the changes of the nested archive %s into %s.
                            Optional<ButtonType> response = raiseAlert(Alert.AlertType.CONFIRMATION,
                                                                       resolveTextKey(TITLE_CONFIRM_SAVE_NESTED_ARCHIVE),
                                                                       resolveTextKey(HEADER_CONFIRM_SAVE_NESTED_ARCHIVE,
                                                                                      parentFilePath),
                                                                       resolveTextKey(BODY_CONFIRM_SAVE_NESTED_ARCHIVE,
                                                                                      archiveFilePath,
                                                                                      parentFilePath),
                                                                       null,
                                                                       stage,
                                                                       ButtonType.YES, ButtonType.NO);

                            final FXArchiveInfo parentFXArchiveInfo = lookupArchiveInfo(this.fxArchiveInfo.getParentArchiveInfo()
                                                                                                    .getArchivePath()).get();
                            if (response.get()
                                        .getButtonData() == ButtonBar.ButtonData.YES) {
                                long sessionId = System.currentTimeMillis();
                                AtomicReference<Path> parentTempArchive = new AtomicReference<>();
                                // Expects the archive to already exist in the parent archive otherwise something really
                                // wrong...
                                FileInfo existingFileInfo = fxArchiveInfo.getNestedFileInfoParent();
                                final FileInfo nestedArchiveFileInfo = new FileInfo(
                                        existingFileInfo.getIndex(),
                                        existingFileInfo.getLevel(),
                                        existingFileInfo.getFileName(),
                                        0,
                                        0,
                                        0,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        0,
                                        "updated via PearlZip",
                                        false,
                                        false,
                                        Collections.singletonMap(
                                                KEY_FILE_PATH,
                                                archiveFilePath)
                                );
                                AtomicBoolean success = new AtomicBoolean(false);
                                final Path parentPath = Paths.get(parentFilePath);
                                JFXUtil.executeBackgroundProcess(sessionId, stage,
                                                                 () -> {
                                                                     // Check if a compressor archive
                                                                     if (ZipState.getRawSupportedCompressorWriteFormats()
                                                                                 .contains(this.fxArchiveInfo.getParentArchiveInfo()
                                                                                                             .getArchiveFormat()
                                                                                                             .toLowerCase())) {
                                                                         // Back up archive...
                                                                         parentTempArchive.set(createBackupArchive(
                                                                                 lookupArchiveInfo(this.fxArchiveInfo.getParentPath()).get(),
                                                                                 Files.createTempDirectory(
                                                                                         TMP_DIR_PREFIX)));

                                                                         // Create new single-file compressor archive...
                                                                         Files.deleteIfExists(parentPath);
                                                                         archiveWriteService.createArchive(sessionId,
                                                                                                           parentFXArchiveInfo.getArchiveInfo(),
                                                                                                           nestedArchiveFileInfo);

                                                                         // Check to ensure integrity of the newly created archive
                                                                         if (Files.exists(parentPath)) {
                                                                             success.set(true);
                                                                         }
                                                                     } else {
                                                                         // Non-compressor archive
                                                                         parentTempArchive.set(createBackupArchive(
                                                                                 lookupArchiveInfo(this.fxArchiveInfo.getParentPath()).get(),
                                                                                 Files.createTempDirectory(
                                                                                         TMP_DIR_PREFIX)));

                                                                         // Expect the archive to pre-exist in wrapper archive, so removing prior to re-add
                                                                         archiveWriteService.deleteFile(sessionId,
                                                                                                        this.fxArchiveInfo.getParentArchiveInfo(), nestedArchiveFileInfo);
                                                                         success.set(archiveWriteService.addFile(
                                                                                 sessionId,
                                                                                 this.fxArchiveInfo.getParentArchiveInfo(),
                                                                                 nestedArchiveFileInfo));
                                                                     }
                                                                 },
                                                                 (s) -> {
                                                                     try {
                                                                         if (!success.get()) {
                                                                             // Restore back up
                                                                             if (Files.exists(parentTempArchive.get())) {
                                                                                 Files.move(parentTempArchive.get(),
                                                                                            parentPath,
                                                                                            StandardCopyOption.REPLACE_EXISTING);
                                                                             }

                                                                             // LOG: Error integrating changes from %s to %s
                                                                             throw new IOException(resolveTextKey(
                                                                                     LOG_ISSUE_INTEGRATING_CHANGES,
                                                                                     parentTempArchive.get(),
                                                                                     parentPath));
                                                                         }
                                                                     } catch(IOException e) {
                                                                         // LOG: Error integrating changes from %s to %s
                                                                         // TITLE: ERROR: Issue integrating nested archive
                                                                         // HEADER: Error integrating changes from %s to %s
                                                                         // BODY: Archive has been reverted to the last stable state.

                                                                         LOGGER.error(resolveTextKey(
                                                                                 LOG_ISSUE_INTEGRATING_CHANGES,
                                                                                 parentTempArchive.get(),
                                                                                 parentPath));

                                                                         raiseAlert(Alert.AlertType.ERROR,
                                                                                    resolveTextKey(TITLE_ISSUE_INTEGRATING_CHANGES),
                                                                                    resolveTextKey(resolveTextKey(
                                                                                            HEADER_ISSUE_INTEGRATING_CHANGES,
                                                                                            parentTempArchive.get(),
                                                                                            parentPath)),
                                                                                    resolveTextKey(BODY_ISSUE_INTEGRATING_CHANGES),
                                                                                    e,
                                                                                    null
                                                                         );
                                                                     } finally {
                                                                         // Enable parent archive (Handle success/failure on reintegration)
                                                                         parentFXArchiveInfo.getController()
                                                                                            .get()
                                                                                            .getWrapper()
                                                                                            .setDisable(false);
                                                                     }
                                                                 }
                                );
                            } else {
                                    // Enable parent archive (Handle no reintegration scenario)
                                    parentFXArchiveInfo.getController()
                                                       .get()
                                                       .getWrapper()
                                                       .setDisable(false);
                            }
                        }
                    return;
                }

                // Archive is found in temporary storage and so prompt to save or delete
                // TITLE: Confirmation: Save temporary archive before exit
                // HEADER: Do you wish to save the open archive %s
                // BODY: Please specify if you wish to save the archive %s. If you do not wish to save the archive, it
                // will be removed from temporary storage.
                final String archiveFileName = archivePath
                                      .getFileName()
                                      .toString();
                Optional<ButtonType> response = raiseAlert(Alert.AlertType.CONFIRMATION,
                                                           resolveTextKey(TITLE_CONFIRM_SAVE_ARCHIVE),
                                                           resolveTextKey(HEADER_CONFIRM_SAVE_ARCHIVE,
                                                                          archiveFileName),
                                                           resolveTextKey(BODY_CONFIRM_SAVE_ARCHIVE,
                                                                          archiveFileName),
                                                           null,
                                                           stage,
                                                           ButtonType.YES, ButtonType.NO);
                if (response.isPresent()) {
                    if (response.get()
                                .equals(ButtonType.YES)) {
                        FileChooser saveDialog = new FileChooser();
                        // TITLE: Save archive to location...
                        saveDialog.setTitle(TITLE_TARGET_ARCHIVE_LOCATION);
                        String archiveName = archivePath
                                .getFileName()
                                .toString();
                        saveDialog.setInitialFileName(archiveName);
                        // Save archive %s
                        saveDialog.setTitle(resolveTextKey(TITLE_SAVE_ARCHIVE_PATTERN, archiveName));
                        File savedFile = saveDialog.showSaveDialog(new Stage());

                        if (Objects.nonNull(savedFile)) {
                            Files.move(archivePath, savedFile.toPath(), StandardCopyOption.ATOMIC_MOVE,
                                       StandardCopyOption.REPLACE_EXISTING);
                            addToRecentFile(savedFile);
                        }
                    }
                }
                removeBackupArchive(archivePath);
            }
        } catch(IOException e) {
            // Issue with IO Process when saving down archive %s
            LOGGER.warn(resolveTextKey(LOG_ISSUE_SAVE_ARCHIVE, fxArchiveInfo.getArchivePath()));
        } finally {
            // If there are no other PearlZip instances apart from this open (Last PearlZip instance open)
            if (JFXUtil.getMainStageInstances()
                       .stream()
                       .map(Stage.class::cast)
                       .filter(s -> s.getTitle() != null && !s.getTitle()
                                                              .matches(String.format(".*%s$", fxArchiveInfo.getArchivePath())))
                       .count() == 0) {
                // Clear up temporary files if on final exit
                long sessionId = System.currentTimeMillis();
                executeBackgroundProcess(sessionId, stage, new ClearCacheRunnable(sessionId, true),
                                         LOGGER::error,
                                         (s) -> {});
            }
        }
    }
}
