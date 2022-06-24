/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.event.handler;

import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.model.ZipState;
import com.ntak.pearlzip.ui.util.AlertException;
import com.ntak.pearlzip.ui.util.CheckEventHandler;
import com.ntak.pearlzip.ui.util.JFXUtil;
import com.ntak.pearlzip.ui.util.internal.ArchiveUtil;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.nio.file.Paths;
import java.util.Objects;

import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
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
        String prefix = fxArchiveInfo.getPrefix();

        JFXUtil.executeBackgroundProcess(sessionId, (Stage) fileContentsView.getScene().getWindow(),
                                         ()-> com.ntak.pearlzip.ui.util.ArchiveUtil.addFile(sessionId, fxArchiveInfo, rawFile, fileName),
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
