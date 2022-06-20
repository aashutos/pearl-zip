/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.pub;

import com.jfoenix.controls.JFXSnackbar;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.cell.*;
import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.event.handler.*;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.model.ZipState;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.archive.constants.LoggingConstants.LOG_BUNDLE;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.model.ZipState.CONTEXT_MENU_INSTANCES;
import static com.ntak.pearlzip.ui.model.ZipState.ROW_TRIGGER;
import static com.ntak.pearlzip.ui.util.JFXUtil.*;

/**
 *  Controller for the Main display dialog.
 *  @author Aashutos Kakshepati
*/
public class FrmMainController {

    @FXML
    private VBox wrapper;
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
    private JFXSnackbar toast;

    @FXML
    private MenuButton btnNew;
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
        name.setComparator(Comparator.comparing(FileInfo::getFileName));

        size.setCellFactory(new SizeHighlightFileInfoCellCallback());
        size.setCellValueFactory(new PropertyValueFactory<>("Self"));
        size.setComparator(Comparator.comparing(FileInfo::getRawSize));

        packedSize.setCellFactory(new PackedSizeHighlightFileInfoCellCallback());
        packedSize.setCellValueFactory(new PropertyValueFactory<>("Self"));
        packedSize.setComparator(Comparator.comparing(FileInfo::getPackedSize));

        modified.setCellFactory(new ModifiedHighlightFileInfoCellCallback());
        modified.setCellValueFactory(new PropertyValueFactory<>("Self"));
        modified.setComparator(Comparator.comparing(v -> Optional.ofNullable(v.getLastWriteTime())
                                                                 .orElse(LocalDateTime.MIN)));

        created.setCellFactory(new CreatedHighlightFileInfoCellCallback());
        created.setCellValueFactory(new PropertyValueFactory<>("Self"));
        created.setComparator(Comparator.comparing(v -> Optional.ofNullable(v.getCreationTime())
                                                                .orElse(LocalDateTime.MIN)));

        hash.setCellFactory(new HashHighlightFileInfoCellCallback());
        hash.setCellValueFactory(new PropertyValueFactory<>("Self"));
        hash.setComparator(Comparator.comparing(v -> Long.toHexString(v.getCrcHash())
                                                         .toUpperCase()));
    }

    public void initData(Stage stage, FXArchiveInfo fxArchiveInfo) {
        try {
            stage.setMinWidth(Double.parseDouble(System.getProperty(CNS_NTAK_PEARL_ZIP_DEFAULT_MIN_WIDTH, "816")));
            stage.setMinHeight(Double.parseDouble(System.getProperty(CNS_NTAK_PEARL_ZIP_DEFAULT_MIN_HEIGHT, "500")));
        } catch (Exception e) {
            stage.setMinWidth(816.0);
            stage.setMinHeight(500.0);
        }

        stage.widthProperty().addListener((l) -> toast.setPrefWidth(stage.getWidth()));

        if (fxArchiveInfo != null) {
            this.FXArchiveInfo = fxArchiveInfo;
            stage.setUserData(fxArchiveInfo);

            comments.setCellFactory(new CommentsHighlightFileInfoCellCallback(fxArchiveInfo));
            comments.setCellValueFactory(new PropertyValueFactory<>("Self"));
            comments.setComparator(Comparator.comparing(v -> Optional.ofNullable(v.getComments())
                                                                     .orElse("")));

            // TODO: Handle multiple rows
            fileContentsView.getSelectionModel()
                            .setSelectionMode(SelectionMode.SINGLE);
            fileContentsView.setItems(FXCollections.observableArrayList(fxArchiveInfo.getFiles()
                                                                                     .stream()
                                                                                     .filter(f -> f.getLevel() == 0)
                                                                                     .collect(Collectors.toList())));
            fileContentsView.setRowFactory(tv -> {
                TableRow<FileInfo> row = new TableRow<>();
                row.setOnMouseClicked(new FileInfoRowEventHandler(fileContentsView, btnUp, row, fxArchiveInfo, toast));

                return row;
            });

            fileContentsView.setOnDragDetected((e) -> {
                try {
                    FileInfo info = fileContentsView.getSelectionModel()
                                                    .getSelectedItem();
                    Dragboard db = fileContentsView.startDragAndDrop(TransferMode.COPY);
                    Path tempDir = Files.createTempDirectory("pz");
                    final ClipboardContent content = new ClipboardContent();
                    if (!info.isFolder()) {
                        final Path path = tempDir.resolve(Paths.get(info.getFileName())
                                                                  .getFileName());
                        long MAX_SIZE_DRAG_OUT = InternalContextCache.INTERNAL_CONFIGURATION_CACHE.<Long>getAdditionalConfig(CK_MAX_SIZE_DRAG_OUT).get();
                        if (info.getRawSize() > MAX_SIZE_DRAG_OUT) {
                            // TITLE: Warning: Drag out functionality not supported for file
                            // HEADER: File too big
                            // BODY: Drag out functionality is currently not supported for large files > %s Bytes.
                            //       Please use the extract file option in the toolbar.
                            raiseAlert(Alert.AlertType.WARNING,
                                       resolveTextKey(TITLE_CANNOT_DRAG_OUT_FILE),
                                       resolveTextKey(HEADER_CANNOT_DRAG_OUT_FILE),
                                       resolveTextKey(BODY_CANNOT_DRAG_OUT_FILE, MAX_SIZE_DRAG_OUT),
                                       stage
                            );

                            return;
                        }

                        if (fxArchiveInfo.getReadService().extractFile(System.currentTimeMillis(),
                                                                       path,
                                                                       fxArchiveInfo.getArchiveInfo(),
                                                                       info
                        )) {
                            // Add file to clipboard
                            content.putFiles(List.of(path.toFile()));
                            db.setContent(content);
                        }
                    }  else {
                        // TITLE: WARNING: Cannot drag out folder
                        // HEADER: Folder drag out not supported
                        // BODY: Please utilise extract directory button as folders cannot be dragged out. This is
                        // unsupported by PearlZip at this present time.
                        raiseAlert(Alert.AlertType.WARNING,
                                   resolveTextKey(TITLE_CANNOT_DRAG_OUT_FOLDER),
                                   resolveTextKey(HEADER_CANNOT_DRAG_OUT_FOLDER),
                                   resolveTextKey(BODY_CANNOT_DRAG_OUT_FOLDER),
                                   stage
                        );
                    }
                } catch (Exception exc) {

                }
            });
            fileContentsView.setOnDragOver(e->e.acceptTransferModes(TransferMode.COPY));
            fileContentsView.setOnDragDropped(new FileContentsDragDropRowEventHandler(fileContentsView, fxArchiveInfo));
            fileContentsView.setOnMouseClicked(e->{
                if (e.getButton() == MouseButton.PRIMARY && !ROW_TRIGGER.get()) {
                    synchronized(CONTEXT_MENU_INSTANCES) {
                        CONTEXT_MENU_INSTANCES.forEach(ContextMenu::hide);
                        CONTEXT_MENU_INSTANCES.clear();
                    }
                    fileContentsView.refresh();
                    return;
                }

                if (e.getButton() == MouseButton.SECONDARY && !ROW_TRIGGER.get()) {
                    try {
                        FXMLLoader loader = new FXMLLoader();
                        loader.setLocation(ZipLauncher.class.getClassLoader()
                                                            .getResource("tablecontextmenu.fxml"));
                        loader.setResources(LOG_BUNDLE);
                        ContextMenu root = loader.load();
                        TableContextMenuController controller = loader.getController();
                        controller.initData(fxArchiveInfo);
                        root.show(fileContentsView, e.getScreenX(), e.getScreenY());

                        synchronized(CONTEXT_MENU_INSTANCES) {
                            CONTEXT_MENU_INSTANCES.forEach(ContextMenu::hide);
                            CONTEXT_MENU_INSTANCES.clear();
                            CONTEXT_MENU_INSTANCES.add(root);
                        }
                    } catch (Exception exc) {
                        exc.printStackTrace();
                    }
                }
                ROW_TRIGGER.set(false);
            });

            MenuItem mnuNewSingleFile =
                    btnNew.getItems().stream().filter(m -> m.getId().equals("mnuNewSingleFileCompressor")).findFirst().orElse(null);
            btnNew.getItems().stream().filter(m -> m.getId().equals("mnuNewArchive")).forEach(m -> m.setOnAction((e)->new BtnNewEventHandler().handle(null)));
            if (ZipState.getSupportedCompressorWriteFormats().size() == 0) {
                mnuNewSingleFile.setDisable(true);
                // TITLE: Warning: No write service available
                // BODY: This functionality is disabled as no compressor write service is available.
                mnuNewSingleFile.setOnAction((e) -> raiseAlert(Alert.AlertType.WARNING,
                                                               resolveTextKey(TITLE_NO_COMPRESSOR_WRITE_SERVICES),
                                                               "",
                                                               resolveTextKey(BODY_NO_COMPRESSOR_WRITE_SERVICES),
                                                               getActiveStage().orElse(new Stage())));
            } else {
                mnuNewSingleFile.setOnAction((e)->new BtnNewSingleFileEventHandler().handle(null));
            }


            btnOpen.setOnMouseClicked(new BtnOpenEventHandler(stage));

            btnAdd.getItems().stream().filter(m -> m.getId().equals("mnuAddFile")).forEach(m -> m.setOnAction(new BtnAddFileEventHandler(
                    fileContentsView, fxArchiveInfo)));
            btnAdd.getItems().stream().filter(m -> m.getId().equals("mnuAddDir")).forEach(m -> m.setOnAction(new BtnAddDirEventHandler(
                    fileContentsView, fxArchiveInfo)));

            btnCopy.getItems().stream().filter(m -> m.getId().equals("mnuCopySelected")).forEach(m -> m.setOnAction(new BtnCopySelectedEventHandler(
                    fileContentsView, btnCopy, btnMove, btnDelete, fxArchiveInfo)));
            btnCopy.getItems()
                   .stream()
                   .filter(m -> m.getId()
                                 .equals("mnuCancelCopy"))
                   .forEach(m -> m.setOnAction(new BtnCancelEventHandler(
                           fileContentsView, btnCopy, btnMove, btnDelete, fxArchiveInfo)));

            btnMove.getItems()
                   .stream()
                   .filter(m -> m.getId()
                                 .equals("mnuMoveSelected"))
                   .forEach(m -> m.setOnAction(new BtnMoveSelectedEventHandler(
                           fileContentsView, btnCopy, btnMove, btnDelete, fxArchiveInfo)));
            btnMove.getItems()
                   .stream()
                   .filter(m -> m.getId()
                                 .equals("mnuCancelMove"))
                   .forEach(m -> m.setOnAction(new BtnCancelEventHandler(
                           fileContentsView, btnCopy, btnMove, btnDelete, fxArchiveInfo)));

            btnTest.setOnMouseClicked(new BtnTestEventHandler(stage, fxArchiveInfo));

            btnExtract.getItems()
                      .stream()
                      .filter(m -> m.getId()
                                    .equals("mnuExtractSelectedFile"))
                      .forEach(m -> m.setOnAction(new BtnExtractFileEventHandler(
                              fileContentsView, fxArchiveInfo)));
            btnExtract.getItems()
                      .stream()
                      .filter(m -> m.getId()
                                    .equals("mnuExtractAll"))
                      .forEach(m -> m.setOnAction(new BtnExtractAllEventHandler(fileContentsView, fxArchiveInfo)));

            btnDelete.setOnMouseClicked(new BtnDeleteEventHandler(fileContentsView, fxArchiveInfo));
            btnInfo.setOnMouseClicked(new BtnFileInfoEventHandler(fileContentsView, fxArchiveInfo));
            btnUp.setOnMouseClicked(new BtnUpEventHandler(fileContentsView, fxArchiveInfo, btnUp, toast));

            if (ZipState.getCompressorArchives()
                        .contains(fxArchiveInfo.getArchivePath()
                                               .substring(fxArchiveInfo.getArchivePath()
                                                                       .lastIndexOf(".") + 1))) {
                btnAdd.setDisable(true);
                btnCopy.setDisable(true);
                btnMove.setDisable(true);
                btnDelete.setDisable(true);
            }

            stage.focusedProperty()
                 .addListener((ObservableValue<? extends Boolean> observable,
                                      Boolean oldValue,
                                      Boolean newValue) -> {
                     final Menu WINDOW_MENU =
                             InternalContextCache.INTERNAL_CONFIGURATION_CACHE
                                                 .<Menu>getAdditionalConfig(CK_WINDOW_MENU)
                                                 .get();
                     final Optional<MenuItem> optMenuItem = WINDOW_MENU.getItems()
                                                                       .stream()
                                                                       .filter(m -> m.getText()
                                                                                     .contains(fxArchiveInfo.getArchivePath()))
                                                                       .findFirst();
                     if (!oldValue && newValue) {
                         synchronized(WINDOW_MENU) {
                             if (optMenuItem.isEmpty()) {
                                 MenuItem archiveMenuItem = new MenuItem(String.format("%s%s",
                                                                                       fxArchiveInfo.getArchivePath(),
                                                                                       WINDOW_FOCUS_SYMBOL));
                                 WINDOW_MENU.getItems()
                                            .add(archiveMenuItem);
                                 archiveMenuItem.setOnAction(e -> getMainStageByArchivePath(fxArchiveInfo.getArchivePath())
                                         .get()
                                         .toFront());
                             } else {
                                 optMenuItem.get()
                                            .setText(String.format("%s%s",
                                                                   optMenuItem.get()
                                                                              .getText(),
                                                                   WINDOW_FOCUS_SYMBOL));
                             }
                         }
                     }

                     if (oldValue && !newValue) {
                         synchronized(WINDOW_MENU) {
                             optMenuItem.ifPresent(m -> m.setText(m.getText()
                                                                   .replaceAll(WINDOW_FOCUS_SYMBOL
                                                                                         , "")));
                         }
                     }
                 });
            stage.setOnCloseRequest(new ConfirmCloseEventHandler(stage, fxArchiveInfo));
        }
    }

    public VBox getWrapper() {
        return wrapper;
    }

    public MenuButton getBtnNew() {
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

    public TableColumn<FileInfo,FileInfo> getComments() {
        return comments;
    }

    public FXArchiveInfo getFXArchiveInfo() {
        return FXArchiveInfo;
    }

    public TableView<FileInfo> getFileContentsView() {
        return fileContentsView;
    }
}
