/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.event.handler;

import com.ntak.pearlzip.archive.pub.ArchiveService;
import com.ntak.pearlzip.archive.pub.ArchiveWriteService;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.archive.pub.ProgressMessage;
import com.ntak.pearlzip.archive.util.LoggingUtil;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.model.FXMigrationInfo;
import com.ntak.pearlzip.ui.model.ZipState;
import com.ntak.pearlzip.ui.util.JFXUtil;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.TMP_DIR_PREFIX;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.LBL_PROGRESS_LOADING;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.PROGRESS;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.ArchiveUtil.*;
import static com.ntak.pearlzip.ui.util.JFXUtil.raiseAlert;
import static javafx.scene.control.ProgressIndicator.INDETERMINATE_PROGRESS;

/**
 *  Event Handler for Delete functionality.
 *  @author Aashutos Kakshepati
*/
public class BtnDeleteEventHandler implements EventHandler<MouseEvent> {
    private final TableView<FileInfo> fileContentsView;
    private final FXArchiveInfo fxArchiveInfo;
    private static final Logger LOGGER = LoggerContext.getContext()
                                                      .getLogger(BtnDeleteEventHandler.class);

    public BtnDeleteEventHandler(TableView<FileInfo> fileContentsView, FXArchiveInfo fxArchiveInfo) {
        this.fileContentsView = fileContentsView;
        this.fxArchiveInfo = fxArchiveInfo;
    }

    @Override
    public void handle(MouseEvent event) {
        try {
            if (ZipState.getWriteArchiveServiceForFile(fxArchiveInfo.getArchivePath())
                        .isEmpty()) {
                // LOG: Delete functionality not supported for archive %s
                LOGGER.warn(resolveTextKey(LOG_DEL_FUNC_NOT_SUPPORTED, fxArchiveInfo.getArchivePath()));
                // TITLE: Warning: Delete functionality not supported
                // HEADER: No Write provider for archive format
                // BODY: Cannot delete file to archive as functionality is not supported for file: %s
                raiseAlert(Alert.AlertType.WARNING,
                           resolveTextKey(TITLE_DEL_FUNC_NOT_SUPPORTED),
                           resolveTextKey(HEADER_DEL_FUNC_NOT_SUPPORTED),
                           resolveTextKey(BODY_DEL_FUNC_NOT_SUPPORTED,
                                          Paths.get(fxArchiveInfo.getArchivePath())
                                               .getFileName()
                                               .toString()),
                           fileContentsView.getScene()
                                           .getWindow()
                );
                return;
            }

            ArchiveWriteService writeService = fxArchiveInfo.getWriteService();
            FileInfo fileToDelete = fileContentsView.getSelectionModel()
                                                    .getSelectedItem();

            if (Objects.isNull(fileToDelete)) {
                return;
            }

            long sessionId = System.currentTimeMillis();
            AtomicReference<Path> tempArchive = new AtomicReference<>();
            JFXUtil.executeBackgroundProcess(sessionId, (Stage)fileContentsView.getScene().getWindow(),
                                             () -> {
                    try {
                        fxArchiveInfo.getMigrationInfo().initMigration(FXMigrationInfo.MigrationType.DELETE, fileToDelete);
                        Path tempDir = Files.createTempDirectory(TMP_DIR_PREFIX);
                        tempArchive.set(createBackupArchive(fxArchiveInfo, tempDir));
                        boolean success = writeService.deleteFile(sessionId, fxArchiveInfo.getArchivePath(),
                                                                  fileToDelete);

                        // Await deletion confirmation
                        if (!success) {
                            // TITLE: Error: Issue deleting file from archive
                            // HEADER: File could not be removed from the archive
                            // BODY: File %s has not been removed from the archive. The backup of archive has been restored.
                            Platform.runLater(()-> raiseAlert(Alert.AlertType.ERROR,
                                                              resolveTextKey(TITLE_ISSUE_DELETE_FILE),
                                                              resolveTextKey(HEADER_ISSUE_DELETE_FILE),
                                                              resolveTextKey(BODY_ISSUE_DELETE_FILE, fileToDelete.getFileName()),
                                                              fileContentsView.getScene().getWindow())
                            );
                            ArchiveService.DEFAULT_BUS.post(new ProgressMessage(sessionId, PROGRESS,
                                                                                resolveTextKey(LBL_PROGRESS_LOADING),
                                                                                INDETERMINATE_PROGRESS, 1));
                            restoreBackupArchive(tempArchive.get(), Paths.get(fxArchiveInfo.getArchivePath()));
                        }
                    } finally {
                        fxArchiveInfo.getMigrationInfo().clear();
                        removeBackupArchive(tempArchive.get());
                    }
               },
                                             (s)->{
                    int depth = fxArchiveInfo.getDepth().get();
                    String prefix = fxArchiveInfo.getPrefix();
                    JFXUtil.refreshFileView(fileContentsView, fxArchiveInfo, depth, prefix);
               }
            );
        } catch(Exception e) {
            // LOG: Issue creating stage.\nException type: %s\nMessage:%s\nStack trace:\n%s
            LOGGER.warn(resolveTextKey(LOG_ISSUE_CREATING_STAGE, e.getClass().getCanonicalName(),
                                       e.getMessage(),
                                       LoggingUtil.getStackTraceFromException(e)));
            // TITLE: ERROR: Issue creating stage
            // HEADER: There was an issue creating the required dialog
            // BODY: Upon initiating function '%s', an issue occurred on attempting to create the dialog. This
            // function will not proceed any further.
            raiseAlert(Alert.AlertType.ERROR, resolveTextKey(TITLE_ISSUE_CREATING_STAGE),
                       resolveTextKey(HEADER_ISSUE_CREATING_STAGE),
                       resolveTextKey(BODY_ISSUE_CREATING_STAGE, this.getClass().getName()), e,
                       fileContentsView.getScene().getWindow());
        }
    }
}
