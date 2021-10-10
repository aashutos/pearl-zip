/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.pub;

import com.ntak.pearlzip.archive.constants.LoggingConstants;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.event.handler.*;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.model.ZipState;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.stage.Stage;

import java.util.Objects;

import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.ArchiveUtil.openExternally;
import static com.ntak.pearlzip.ui.util.JFXUtil.raiseAlert;

/**
 *  Controller for the Context menu.
 *  @author Aashutos Kakshepati
*/
public class ContextMenuController {

    @FXML
    private ContextMenu ctxMenu;
    @FXML
    private MenuItem mnuPeek;
    @FXML
    private MenuItem mnuExtract;
    @FXML
    private MenuItem mnuCopy;
    @FXML
    private MenuItem mnuMove;
    @FXML
    private MenuItem mnuDelete;
    @FXML
    private MenuItem mnuFileInfo;

    public void initData(FXArchiveInfo archiveInfo, TableRow<FileInfo> row) {
        FileInfo fileInfo = row.getItem();
        ctxMenu.setOnShowing((e)->{
            switch(archiveInfo.getMigrationInfo().getType()) {
                case COPY:      mnuMove.setDisable(true);
                                mnuDelete.setDisable(true);
                                mnuMove.setText(resolveTextKey(LBL_BUTTON_MOVE));
                                mnuCopy.setText(resolveTextKey(LBL_BUTTON_PASTE));
                                break;
                case MOVE:      mnuCopy.setDisable(true);
                                mnuDelete.setDisable(true);
                                mnuCopy.setText(resolveTextKey(LBL_BUTTON_COPY));
                                mnuMove.setText(resolveTextKey(LBL_BUTTON_DROP));
                                break;
                case DELETE:    mnuMove.setDisable(true);
                                mnuCopy.setDisable(true);
                                mnuCopy.setText(resolveTextKey(LBL_BUTTON_COPY));
                                mnuMove.setText(resolveTextKey(LBL_BUTTON_MOVE));
                                break;
                default:        if (row.getItem().isFolder()) {
                                    mnuMove.setDisable(true);
                                    mnuCopy.setDisable(true);
                                } else {
                                    mnuMove.setDisable(false);
                                    mnuCopy.setDisable(false);
                                }
                                mnuCopy.setText(resolveTextKey(LBL_BUTTON_COPY));
                                mnuMove.setText(resolveTextKey(LBL_BUTTON_MOVE));
            }

            if (ZipState.getCompressorArchives().contains(archiveInfo.getArchivePath().substring(archiveInfo.getArchivePath().lastIndexOf(".")+1))) {
                mnuCopy.setDisable(true);
                mnuMove.setDisable(true);
                mnuDelete.setDisable(true);
            }
        });

        FrmMainController controller;
        if (archiveInfo.getController().isPresent()) {
            controller = archiveInfo.getController().get();
            mnuMove.setOnAction((e)->Platform.runLater(()->new BtnMoveSelectedEventHandler(row.getTableView(),
                                                                               controller.getBtnCopy(),
                                                                               controller.getBtnMove(),
                                                                               controller.getBtnDelete(),
                                                                               archiveInfo).handle(e))
            );
            mnuCopy.setOnAction((e)->Platform.runLater(()->new BtnCopySelectedEventHandler(row.getTableView(),
                                                                              controller.getBtnCopy(),
                                                                     controller.getBtnMove(),
                                                                     controller.getBtnDelete(),
                                                                     archiveInfo).handle(e))
            );
            mnuDelete.setOnAction((e)->Platform.runLater(()->new BtnDeleteEventHandler(row.getTableView(),
                                                                                      archiveInfo).handle(null)));
        } else {
            mnuCopy.setDisable(true);
            mnuMove.setDisable(true);
            mnuDelete.setDisable(true);
        }

        mnuPeek.setOnAction((e)->{
            if (Objects.isNull(fileInfo) || fileInfo.isFolder()) {
                // LOG: No file has been selected from archive %s
                LoggingConstants.ROOT_LOGGER.warn(resolveTextKey(LOG_NO_FILE_SELECTED, archiveInfo.getArchivePath()));
                // TITLE: Information: No file selected
                // HEADER: A file has not been selected
                // BODY: Please select a file.
                raiseAlert(Alert.AlertType.INFORMATION,
                           resolveTextKey(TITLE_NO_FILE_SELECTED),
                           resolveTextKey(HEADER_NO_FILE_SELECTED),
                           resolveTextKey(BODY_NO_FILE_SELECTED),
                           row.getScene().getWindow()
                );
            } else {
                openExternally(System.currentTimeMillis(),
                               (Stage) row.getScene()
                                          .getWindow(),
                               archiveInfo,
                               fileInfo);
            }
        });
        mnuExtract.setOnAction((e)->new BtnExtractFileEventHandler(row.getTableView(), archiveInfo).handle(e));
        mnuFileInfo.setOnAction((e)->new BtnFileInfoEventHandler(row.getTableView(),archiveInfo).handle(null));
    }
}
