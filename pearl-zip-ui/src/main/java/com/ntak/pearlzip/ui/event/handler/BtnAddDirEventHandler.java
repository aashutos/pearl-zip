/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.event.handler;

import com.ntak.pearlzip.archive.pub.ArchiveWriteService;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.archive.util.LoggingUtil;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.util.AlertException;
import com.ntak.pearlzip.ui.util.ArchiveUtil;
import com.ntak.pearlzip.ui.util.CheckEventHandler;
import com.ntak.pearlzip.ui.util.JFXUtil;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.KEY_FILE_PATH;
import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.TMP_DIR_PREFIX;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.ArchiveUtil.*;
import static com.ntak.pearlzip.ui.util.JFXUtil.raiseAlert;

/**
 *  Event Handler for Add Directory functionality.
 *  @author Aashutos Kakshepati
*/
public class BtnAddDirEventHandler implements CheckEventHandler<ActionEvent> {

    private static final Logger LOGGER = LoggerContext.getContext().getLogger(BtnAddDirEventHandler.class);

    private final TableView<FileInfo> fileContentsView;
    private final FXArchiveInfo fxArchiveInfo;

    public BtnAddDirEventHandler(TableView<FileInfo> fileContentsView, FXArchiveInfo fxArchiveInfo) {
        this.fileContentsView = fileContentsView;
        this.fxArchiveInfo = fxArchiveInfo;
    }

    @Override
    public void handleEvent(ActionEvent event) {
        ArchiveWriteService archiveWriteService;
        try {
            if (Objects.nonNull(archiveWriteService = fxArchiveInfo.getWriteService())) {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                // TITLE: Select source directory location for augmentation...
                directoryChooser.setTitle(resolveTextKey(TITLE_SOURCE_DIR_LOCATION));
                final File dir = directoryChooser.showDialog(new Stage());

                if (Objects.isNull(dir)) {
                    return;
                }

                final Path dirPath = dir.toPath();

                int depth = fxArchiveInfo.getDepth().get();
                int index = fxArchiveInfo.getFiles().size();
                String prefix = fxArchiveInfo.getPrefix();

                long sessionId = System.currentTimeMillis();
                JFXUtil.executeBackgroundProcess(sessionId, (Stage) fileContentsView.getScene().getWindow(),
                                                 ()-> {
                        Path tempDir = Files.createTempDirectory(TMP_DIR_PREFIX);
                        Path tempArchive = createBackupArchive(fxArchiveInfo, tempDir);

                        List<FileInfo> files = ArchiveUtil.handleDirectory(prefix, dirPath.getParent(), dirPath, depth+1, index);
                        files.add(new FileInfo((index+1), depth,
                                                depth>0?String.format("%s/%s",prefix,
                                                                      dirPath.getFileName().toString()):dirPath.getFileName().toString(),
                                                -1, 0,
                                                0, null,
                                                null, null,
                                                "", "", 0, "",
                                                true, false,
                                                Collections.singletonMap(KEY_FILE_PATH, dirPath.toString())));

                        if (files.removeIf(f -> f.getAdditionalInfoMap().getOrDefault(KEY_FILE_PATH,"").equals(fxArchiveInfo.getArchivePath()))) {
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
                            LOGGER.error(resolveTextKey(LOG_ISSUE_ADDING_DIR, dir.getAbsolutePath()));
                            raiseAlert(Alert.AlertType.ERROR,
                                       resolveTextKey(TITLE_ISSUE_ADDING_DIR),
                                       resolveTextKey(HEADER_ISSUE_ADDING_DIR),
                                       resolveTextKey(BODY_ISSUE_ADDING_DIR),
                                       null
                            );
                        }

                        removeBackupArchive(tempArchive);
                    },
                    (s)->JFXUtil.refreshFileView(fileContentsView, fxArchiveInfo, depth, prefix)
                );
            }
        } catch (Exception e) {
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

    @Override
    public void check(ActionEvent event) throws AlertException {
        ArchiveUtil.checkArchiveExists(fxArchiveInfo);

        if (Objects.isNull(fxArchiveInfo.getWriteService())) {
            // LOG: Warning: Add functionality not supported for archive %s
            // TITLE: Warning: Add functionality not supported
            // HEADER: No Write provider for archive format
            // BODY: Cannot add file to archive as functionality is not supported for file: %s
            LOGGER.warn(resolveTextKey(LOG_ADD_FUNC_NOT_SUPPORTED, fxArchiveInfo.getArchivePath()));
            JFXUtil.refreshFileView(fileContentsView,
                                    fxArchiveInfo,
                                    fxArchiveInfo.getDepth()
                                                 .get(),
                                    fxArchiveInfo.getPrefix());
            throw new AlertException(fxArchiveInfo,
                                     resolveTextKey(LOG_ADD_FUNC_NOT_SUPPORTED, fxArchiveInfo.getArchivePath()),
                                     Alert.AlertType.WARNING,
                                     resolveTextKey(TITLE_ADD_FUNC_NOT_SUPPORTED),
                                     resolveTextKey(HEADER_ADD_FUNC_NOT_SUPPORTED),
                                     resolveTextKey(BODY_ADD_FUNC_NOT_SUPPORTED,
                                     Paths.get(fxArchiveInfo.getArchivePath())
                                          .getFileName()
                                          .toString()),
                                     null,
                                     fileContentsView.getScene()
                                                     .getWindow()
            );
        }
    }

}
