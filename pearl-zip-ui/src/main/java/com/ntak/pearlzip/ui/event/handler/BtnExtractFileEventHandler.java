/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.event.handler;

import com.ntak.pearlzip.archive.pub.ArchiveReadService;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.util.AlertException;
import com.ntak.pearlzip.ui.util.ArchiveUtil;
import com.ntak.pearlzip.ui.util.CheckEventHandler;
import com.ntak.pearlzip.ui.util.JFXUtil;
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static com.ntak.pearlzip.archive.constants.ArchiveConstants.CURRENT_SETTINGS;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.JFXUtil.raiseAlert;

/**
 *  Event Handler for Extract File functionality.
 *  @author Aashutos Kakshepati
*/
public class BtnExtractFileEventHandler implements CheckEventHandler<ActionEvent> {
    private final TableView<FileInfo> fileContentsView;
    private final FXArchiveInfo fxArchiveInfo;
    private static final Logger LOGGER = LoggerContext.getContext().getLogger(BtnExtractFileEventHandler.class);

    public BtnExtractFileEventHandler(TableView<FileInfo> fileContentsView, FXArchiveInfo fxArchiveInfo) {
        this.fileContentsView = fileContentsView;
        this.fxArchiveInfo = fxArchiveInfo;
    }


    @Override
    public void handleEvent(ActionEvent event) {
        ArchiveReadService readService = fxArchiveInfo.getReadService();

        FileInfo selectedFile = fileContentsView.getSelectionModel().getSelectedItem();
        long sessionId = System.currentTimeMillis();
        if (Objects.isNull(selectedFile)) {
            // LOG: No file has been selected from archive %s
            LOGGER.warn(resolveTextKey(LOG_NO_FILE_SELECTED, fxArchiveInfo.getArchivePath()));
            // TITLE: Information: No file selected
            // HEADER: A file has not been selected
            // BODY: Please select a file.
            raiseAlert(Alert.AlertType.INFORMATION,
                       resolveTextKey(TITLE_NO_FILE_FOLDER_SELECTED),
                       resolveTextKey(HEADER_NO_FILE_FOLDER_SELECTED),
                       resolveTextKey(BODY_NO_FILE_FOLDER_SELECTED),
                       fileContentsView.getScene()
                                       .getWindow()
            );
            return;
        }
        if (selectedFile.isFolder()) {
                // Choose destination directory
                DirectoryChooser extractDirChooser = new DirectoryChooser();
                extractDirChooser.setTitle(resolveTextKey(TITLE_TARGET_DIR_LOCATION));
                Path targetDir = extractDirChooser.showDialog(new Stage()).toPath();

                JFXUtil.executeBackgroundProcess(sessionId, (Stage) fileContentsView.getScene().getWindow(),
                                                 () -> ArchiveUtil.extractDirectory(sessionId, targetDir, fxArchiveInfo,
                                                                                    selectedFile),
                                                 (s)->{
                                                     if (Boolean.parseBoolean(CURRENT_SETTINGS.getProperty(CNS_SHOW_TARGET_FOLDER_EXTRACT_SELECTED,"true"))) {
                                                         InternalContextCache.GLOBAL_CONFIGURATION_CACHE
                                                                             .<HostServices>getAdditionalConfig(CK_HOST_SERVICES)
                                                                             .get()
                                                                             .showDocument(targetDir.toAbsolutePath().toUri().toString());
                                                     }
                                                 }
                );
        } else {
            FileChooser addFileView = new FileChooser();
            // Title: Extract file %s to...
            addFileView.setTitle(resolveTextKey(TITLE_EXTRACT_ARCHIVE_PATTERN, fxArchiveInfo.getArchivePath()));
            addFileView.setInitialFileName(Paths.get(selectedFile.getFileName())
                                                .getFileName()
                                                .toString());
            File destPath = addFileView.showSaveDialog(new Stage());

            if (Objects.nonNull(destPath)) {
                JFXUtil.executeBackgroundProcess(sessionId, (Stage) fileContentsView.getScene().getWindow(),
                                                 ()->readService.extractFile(sessionId, destPath.toPath(),
                                                                             fxArchiveInfo.getArchiveInfo(), selectedFile),
                                                 (s)->{
                                                     if (Boolean.parseBoolean(CURRENT_SETTINGS.getProperty(CNS_SHOW_TARGET_FOLDER_EXTRACT_SELECTED,"true"))) {
                                                         InternalContextCache.GLOBAL_CONFIGURATION_CACHE
                                                                             .<HostServices>getAdditionalConfig(CK_HOST_SERVICES)
                                                                             .get()
                                                                             .showDocument(destPath.toPath().getParent().toUri().toString());
                                                     }
                                                 }
                );
            }
        }
    }

    @Override
    public void check(ActionEvent event) throws AlertException {
        com.ntak.pearlzip.ui.util.ArchiveUtil.checkArchiveExists(fxArchiveInfo);

        if (Objects.isNull(fxArchiveInfo.getReadService())) {
            // LOG: Extract functionality not supported for archive %s
            // TITLE: Warning: Extract functionality not supported
            // HEADER: No Write provider for archive format
            // BODY: Cannot extract file to archive as functionality is not supported for file: %s
            LOGGER.warn(resolveTextKey(LOG_EXT_FUNC_NOT_SUPPORTED, fxArchiveInfo.getArchivePath()));
            throw new AlertException(fxArchiveInfo,
                                     resolveTextKey(LOG_EXT_FUNC_NOT_SUPPORTED, fxArchiveInfo.getArchivePath()),
                                     Alert.AlertType.WARNING,
                                     resolveTextKey(TITLE_EXT_FUNC_NOT_SUPPORTED),
                                     resolveTextKey(HEADER_EXT_FUNC_NOT_SUPPORTED),
                                     resolveTextKey(BODY_EXT_FUNC_NOT_SUPPORTED,
                                     Paths.get(fxArchiveInfo.getArchivePath())
                                          .getFileName()
                                          .toString()),
                                     null,
                                     fileContentsView.getScene().getWindow()
            );
        }
    }
}
