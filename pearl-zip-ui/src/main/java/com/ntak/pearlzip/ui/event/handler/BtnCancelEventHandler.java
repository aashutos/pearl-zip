/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.event.handler;

import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;

import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.LBL_BUTTON_COPY;
import static com.ntak.pearlzip.ui.constants.ZipConstants.LBL_BUTTON_MOVE;
import static com.ntak.pearlzip.ui.util.internal.JFXUtil.changeButtonPicText;

/**
 *  Event Handler for Cancel Migration functionality.
 *  @author Aashutos Kakshepati
*/
public class BtnCancelEventHandler implements EventHandler<ActionEvent> {

    private final TableView<FileInfo> fileContentsView;
    private final FXArchiveInfo fxArchiveInfo;
    private final MenuItem mnuCopySelected;
    private final MenuItem mnuMoveSelected;
    private final MenuButton copyButton;
    private final MenuButton moveButton;
    private final Button delButton;

    public BtnCancelEventHandler(TableView<FileInfo> fileContentsView, MenuButton copyButton,
            MenuButton moveButton, Button delButton, FXArchiveInfo fxArchiveInfo) {
        this.fileContentsView = fileContentsView;
        this.mnuCopySelected = copyButton.getItems()
                                         .stream()
                                         .filter(m -> m.getId().equals("mnuCopySelected"))
                                         .findFirst()
                                         .get();

        this.mnuMoveSelected = moveButton.getItems()
                                       .stream()
                                       .filter(m -> m.getId().equals("mnuMoveSelected"))
                                       .findFirst()
                                       .get();

        this.copyButton = copyButton;
        this.moveButton = moveButton;
        this.delButton = delButton;
        this.fxArchiveInfo = fxArchiveInfo;
    }

    @Override
    public void handle(ActionEvent event) {
        synchronized(fxArchiveInfo.getMigrationInfo()) {
            // Clear Migration Info, Enable move and delete button, update Copy button look
            fxArchiveInfo.getMigrationInfo()
                         .clear();

            copyButton.setDisable(false);
            moveButton.setDisable(false);
            delButton.setDisable(false);

            changeButtonPicText(copyButton, "copy.png", resolveTextKey(LBL_BUTTON_COPY));
            mnuCopySelected.setText("Copy Selected");
            changeButtonPicText(moveButton, "move.png", resolveTextKey(LBL_BUTTON_MOVE));
            mnuMoveSelected.setText("Move Selected");

            fileContentsView.refresh();
        }
    }
}
