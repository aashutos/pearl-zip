/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.event.handler;

import com.jfoenix.controls.JFXSnackbar;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.util.JFXUtil;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;

import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.LOG_TOAST_CURRENT_DIRECTORY;
import static com.ntak.pearlzip.ui.util.JFXUtil.isFileInArchiveLevel;

/**
 *  Event Handler for Parent Archive Directory Navigation functionality.
 *  @author Aashutos Kakshepati
*/
public class BtnUpEventHandler implements EventHandler<MouseEvent> {
    private final TableView<FileInfo> fileContentsView;
    private final FXArchiveInfo fxArchiveInfo;
    private final Button btnUp;
    private final JFXSnackbar toast;

    public BtnUpEventHandler(TableView<FileInfo> fileContentsView, FXArchiveInfo fxArchiveInfo, Button btnUp,
            JFXSnackbar toast) {
        this.fileContentsView = fileContentsView;
        this.fxArchiveInfo = fxArchiveInfo;
        this.btnUp = btnUp;
        this.toast = toast;
    }

    @Override
    public void handle(MouseEvent event) {
        if (fxArchiveInfo.getDepth().decrementAndGet() == 0) {
            btnUp.setVisible(false);
        }
        fxArchiveInfo.setPrefix(Optional.ofNullable(Paths.get(fxArchiveInfo.getPrefix()).getParent()).orElse(Paths.get("")).toString());
        fileContentsView.setItems(FXCollections.observableArrayList(fxArchiveInfo.getFiles()
                                                                            .stream()
                                                                            .filter(isFileInArchiveLevel(fxArchiveInfo))
                                                                            .collect(
                                                                                    Collectors.toList())));

        // LOG: Current directory: /%s
        JFXUtil.toastMessage(toast, resolveTextKey(LOG_TOAST_CURRENT_DIRECTORY, fxArchiveInfo.getPrefix()));

        fileContentsView.refresh();
    }
}
