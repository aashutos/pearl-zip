/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.pub;

import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.event.handler.BtnAddDirEventHandler;
import com.ntak.pearlzip.ui.event.handler.BtnAddFileEventHandler;
import com.ntak.pearlzip.ui.event.handler.BtnCopySelectedEventHandler;
import com.ntak.pearlzip.ui.event.handler.BtnMoveSelectedEventHandler;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.model.FXMigrationInfo;
import com.ntak.pearlzip.ui.util.JFXUtil;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;

import static com.ntak.pearlzip.ui.model.ZipState.ROW_TRIGGER;

/**
 *  Controller for the Table Context Menu.
 *  @author Aashutos Kakshepati
 */
public class TableContextMenuController {

    @FXML
    public ContextMenu tblCtxMenu;
    @FXML
    private MenuItem mnuPaste;
    @FXML
    private MenuItem mnuDrop;
    @FXML
    private MenuItem mnuAddFile;
    @FXML
    private MenuItem mnuAddDir;

    public void initData(FXArchiveInfo archiveInfo) {
        mnuPaste.setVisible(false);
        mnuDrop.setVisible(false);

        if (archiveInfo.getMigrationInfo().getType() == FXMigrationInfo.MigrationType.COPY) {
            mnuPaste.setVisible(true);
        }

        if (archiveInfo.getMigrationInfo().getType() == FXMigrationInfo.MigrationType.MOVE) {
            mnuDrop.setVisible(true);
        }

        FrmMainController controller;
        if (archiveInfo.getController().isPresent()) {
            controller = archiveInfo.getController().get();
            TableView<FileInfo> fileContentsView = controller.getFileContentsView();
            mnuDrop.setOnAction((e) -> {
                try {
                    JFXUtil.runLater(() ->
                                             new BtnMoveSelectedEventHandler(fileContentsView,
                                                                             controller.getBtnCopy(),
                                                                             controller.getBtnMove(),
                                                                             controller.getBtnDelete(),
                                                                             archiveInfo).handle(e));
                } finally {
                    ROW_TRIGGER.set(false);
                }
            });
            mnuPaste.setOnAction((e) -> {
                try {
                    JFXUtil.runLater(() -> new BtnCopySelectedEventHandler(fileContentsView,
                                                                           controller.getBtnCopy(),
                                                                           controller.getBtnMove(),
                                                                           controller.getBtnDelete(),
                                                                           archiveInfo).handle(e));
                } finally {
                    ROW_TRIGGER.set(false);
                }
            }
            );

            mnuAddFile.setOnAction(e-> {
                try {
                    new BtnAddFileEventHandler(fileContentsView, archiveInfo).handle(e);
                } finally {
                    ROW_TRIGGER.set(false);
                }
            });
            mnuAddDir.setOnAction(e-> {
                try {
                    new BtnAddDirEventHandler(fileContentsView, archiveInfo).handle(e);
                } finally {
                    ROW_TRIGGER.set(false);
                }
            });
        }
    }

}
