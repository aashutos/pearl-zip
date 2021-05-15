/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.pub;

import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.cell.*;
import com.ntak.pearlzip.ui.event.handler.*;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.model.ZipState;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;

import java.util.stream.Collectors;

/**
 *  Controller for the Main display dialog.
 *  @author Aashutos Kakshepati
*/
public class FrmMainController {

    @FXML
    private TableView<FileInfo> fileContentsView;
    @FXML
    private TableColumn<FileInfo, FileInfo> name;
    @FXML
    private TableColumn<FileInfo, FileInfo> size;
    @FXML
    private TableColumn<FileInfo, FileInfo> packedSize;
    @FXML
    private TableColumn<FileInfo, FileInfo> modified;
    @FXML
    private TableColumn<FileInfo, FileInfo> created;
    @FXML
    private TableColumn<FileInfo, FileInfo> hash;
    @FXML
    private TableColumn<FileInfo, FileInfo> comments;

    @FXML
    private Button btnNew;
    @FXML
    private Button btnOpen;
    @FXML
    private MenuButton btnAdd;
    @FXML
    private MenuButton btnExtract;
    @FXML
    private Button btnTest;
    @FXML
    private MenuButton btnCopy;
    @FXML
    private MenuButton btnMove;
    @FXML
    private Button btnDelete;
    @FXML
    private Button btnInfo;
    @FXML
    private Button btnUp;

    private FXArchiveInfo FXArchiveInfo;

    @FXML
    public void initialize()
    {
        name.setCellFactory(new NameHighlightFileInfoCellCallback());
        name.setCellValueFactory(new PropertyValueFactory<>("Self"));

        size.setCellFactory(new SizeHighlightFileInfoCellCallback());
        size.setCellValueFactory(new PropertyValueFactory<>("Self"));

        packedSize.setCellFactory(new PackedSizeHighlightFileInfoCellCallback());
        packedSize.setCellValueFactory(new PropertyValueFactory<>("Self"));

        modified.setCellFactory(new ModifiedHighlightFileInfoCellCallback());
        modified.setCellValueFactory(new PropertyValueFactory<>("Self"));

        created.setCellFactory(new CreatedHighlightFileInfoCellCallback());
        created.setCellValueFactory(new PropertyValueFactory<>("Self"));

        hash.setCellFactory(new HashHighlightFileInfoCellCallback());
        hash.setCellValueFactory(new PropertyValueFactory<>("Self"));

        comments.setCellFactory(new CommentsHighlightFileInfoCellCallback());
        comments.setCellValueFactory(new PropertyValueFactory<>("Self"));
    }

    public void initData(Stage stage, FXArchiveInfo fxArchiveInfo) {
        if (fxArchiveInfo != null) {
            this.FXArchiveInfo = fxArchiveInfo;
            stage.setUserData(fxArchiveInfo);
            // TODO: Handle multiple rows
            fileContentsView.getSelectionModel()
                            .setSelectionMode(SelectionMode.SINGLE);
            fileContentsView.setItems(FXCollections.observableArrayList(fxArchiveInfo.getFiles()
                                                                                     .stream()
                                                                                     .filter(f -> f.getLevel() == 0)
                                                                                     .collect(Collectors.toList())));
            fileContentsView.setRowFactory(tv -> {
                TableRow<FileInfo> row = new TableRow<>();
                row.setOnMouseClicked(new FileInfoRowEventHandler(fileContentsView, btnUp, row, fxArchiveInfo));

                return row;
            });

            fileContentsView.setOnDragOver(e->e.acceptTransferModes(TransferMode.COPY));
            fileContentsView.setOnDragDropped(new FileContentsDragDropRowEventHandler(fileContentsView, fxArchiveInfo));

            btnNew.setOnMouseClicked(new BtnNewEventHandler());
            btnOpen.setOnMouseClicked(new BtnOpenEventHandler(stage));

            btnAdd.getItems().stream().filter(m -> m.getId().equals("mnuAddFile")).forEach(m -> m.setOnAction(new BtnAddFileEventHandler(
                    fileContentsView, fxArchiveInfo)));
            btnAdd.getItems().stream().filter(m -> m.getId().equals("mnuAddDir")).forEach(m -> m.setOnAction(new BtnAddDirEventHandler(
                    fileContentsView, fxArchiveInfo)));

            btnCopy.getItems().stream().filter(m -> m.getId().equals("mnuCopySelected")).forEach(m -> m.setOnAction(new BtnCopySelectedEventHandler(
                    fileContentsView, btnCopy, btnMove, btnDelete, fxArchiveInfo)));
            btnCopy.getItems().stream().filter(m -> m.getId().equals("mnuCancelCopy")).forEach(m -> m.setOnAction(new BtnCancelEventHandler(
                    fileContentsView, btnCopy, btnMove, btnDelete, fxArchiveInfo)));

            btnMove.getItems().stream().filter(m -> m.getId().equals("mnuMoveSelected")).forEach(m -> m.setOnAction(new BtnMoveSelectedEventHandler(
                    fileContentsView, btnCopy, btnMove, btnDelete, fxArchiveInfo)));
            btnMove.getItems().stream().filter(m -> m.getId().equals("mnuCancelMove")).forEach(m -> m.setOnAction(new BtnCancelEventHandler(
                    fileContentsView, btnCopy, btnMove, btnDelete, fxArchiveInfo)));

            btnTest.setOnMouseClicked(new BtnTestEventHandler(stage, fxArchiveInfo));

            btnExtract.getItems().stream().filter(m -> m.getId().equals("mnuExtractSelectedFile")).forEach(m -> m.setOnAction(new BtnExtractFileEventHandler(
                                                                                             fileContentsView, fxArchiveInfo)));
            btnExtract.getItems().stream().filter(m -> m.getId().equals("mnuExtractAll")).forEach(m -> m.setOnAction(new BtnExtractAllEventHandler(fileContentsView, fxArchiveInfo)));

            btnDelete.setOnMouseClicked(new BtnDeleteEventHandler(fileContentsView, fxArchiveInfo));
            btnInfo.setOnMouseClicked(new BtnFileInfoEventHandler(fileContentsView, fxArchiveInfo));
            btnUp.setOnMouseClicked(new BtnUpEventHandler(fileContentsView, fxArchiveInfo, btnUp));

            if (ZipState.getCompressorArchives().contains(fxArchiveInfo.getArchivePath().substring(fxArchiveInfo.getArchivePath().lastIndexOf(".")+1))) {
                btnAdd.setDisable(true);
                btnCopy.setDisable(true);
                btnMove.setDisable(true);
                btnDelete.setDisable(true);
            }

            stage.setOnCloseRequest(new ConfirmCloseEventHandler(stage, fxArchiveInfo));
        }
    }

    public Button getBtnNew() {
        return btnNew;
    }

    public Button getBtnOpen() {
        return btnOpen;
    }

    public MenuButton getBtnAdd() {
        return btnAdd;
    }

    public MenuButton getBtnExtract() {
        return btnExtract;
    }

    public Button getBtnTest() {
        return btnTest;
    }

    public MenuButton getBtnCopy() {
        return btnCopy;
    }

    public MenuButton getBtnMove() {
        return btnMove;
    }

    public Button getBtnDelete() {
        return btnDelete;
    }

    public Button getBtnInfo() {
        return btnInfo;
    }

    public Button getBtnUp() {
        return btnUp;
    }

    public FXArchiveInfo getFXArchiveInfo() {
        return FXArchiveInfo;
    }
}
