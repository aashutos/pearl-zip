/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.event.handler;

import com.ntak.pearlzip.archive.pub.ArchiveWriteService;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.model.ZipState;
import com.ntak.pearlzip.ui.util.AlertException;
import com.ntak.pearlzip.ui.util.ArchiveUtil;
import com.ntak.pearlzip.ui.util.CheckEventHandler;
import com.ntak.pearlzip.ui.util.JFXUtil;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.KEY_FILE_PATH;
import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.TMP_DIR_PREFIX;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.ArchiveUtil.createBackupArchive;
import static com.ntak.pearlzip.ui.util.ArchiveUtil.restoreBackupArchive;
import static com.ntak.pearlzip.ui.util.JFXUtil.raiseAlert;

/**
 *  Event Handler for Add File functionality.
 *  @author Aashutos Kakshepati
*/
public class BtnAddFileEventHandler implements CheckEventHandler<ActionEvent> {
    private final TableView<FileInfo> fileContentsView;
    private final FXArchiveInfo fxArchiveInfo;
    private static final Logger LOGGER = LoggerContext.getContext().getLogger(BtnAddFileEventHandler.class);

    public BtnAddFileEventHandler(TableView<FileInfo> fileContentsView, FXArchiveInfo fxArchiveInfo) {
        this.fileContentsView = fileContentsView;
        this.fxArchiveInfo = fxArchiveInfo;
    }

    @Override
    public void handleEvent(ActionEvent event) {
        FileChooser addFileView = new FileChooser();
        // Title: Add file to archive %s...
        addFileView.setTitle(resolveTextKey(TITLE_ADD_TO_ARCHIVE_PATTERN, fxArchiveInfo.getArchivePath()));
        File rawFile = addFileView.showOpenDialog(new Stage());

        if (Objects.isNull(rawFile)) {
            return;
        }

        if (rawFile.getAbsolutePath().equals(fxArchiveInfo.getArchivePath())) {
            // LOG: Skipping the addition of this archive within itself...
            // TITLE: Skipping addition of archive in itself
            // HEADER: File %s will not be added
            // BODY: Ignoring the addition of file %s into the archive %s
            LOGGER.warn(resolveTextKey(LOG_SKIP_ADD_SELF));
            raiseAlert(Alert.AlertType.WARNING,
                       resolveTextKey(TITLE_SKIP_ADD_SELF),
                       resolveTextKey(HEADER_SKIP_ADD_SELF, fxArchiveInfo.getArchivePath()),
                       resolveTextKey(BODY_SKIP_ADD_SELF, rawFile.getAbsolutePath(), fxArchiveInfo.getArchivePath()),
                       fileContentsView.getScene().getWindow()
            );
            return;
        }

        String fileName;
        if (fxArchiveInfo.getDepth().get() > 0) {
            fileName = String.format("%s/%s", fxArchiveInfo.getPrefix(),
                                            rawFile.toPath()
                                                   .getFileName()
                                                   .toString());
        } else {
            fileName = rawFile.toPath().getFileName().toString();
        }
        long sessionId = System.currentTimeMillis();
        int depth = fxArchiveInfo.getDepth().get();
        int index = fxArchiveInfo.getFiles().size();
        String prefix = fxArchiveInfo.getPrefix();

        JFXUtil.executeBackgroundProcess(sessionId, (Stage) fileContentsView.getScene().getWindow(),
                                         ()-> {
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
                                                      List<FileInfo> files = ArchiveUtil.handleDirectory(prefix,
                                                                                                         rawFile.toPath().getParent(), rawFile.toPath(), depth+1, index);
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
                                              },
                                         (s)-> JFXUtil.refreshFileView(fileContentsView, fxArchiveInfo, depth, prefix)
        );
    }

    @Override
    public void check(ActionEvent event) throws AlertException {
        ArchiveUtil.checkArchiveExists(fxArchiveInfo);

        if (ZipState.getWriteArchiveServiceForFile(fxArchiveInfo.getArchivePath()).isEmpty()) {
            // LOG: Warning: Add functionality not supported for archive %s
            // TITLE: Warning: Add functionality not supported
            // HEADER: No Write provider for archive format
            // BODY: Cannot add file to archive as functionality is not supported for file: %s
            JFXUtil.refreshFileView(fileContentsView, fxArchiveInfo, fxArchiveInfo.getDepth().get(),
                                    fxArchiveInfo.getPrefix());
            LOGGER.warn(resolveTextKey(LOG_ADD_FUNC_NOT_SUPPORTED, fxArchiveInfo.getArchivePath()));
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
                       fileContentsView.getScene().getWindow()
            );
        }
    }
}
