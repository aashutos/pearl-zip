/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.event.handler;

import com.ntak.pearlzip.archive.pub.ArchiveInfo;
import com.ntak.pearlzip.archive.pub.ArchiveReadService;
import com.ntak.pearlzip.archive.pub.ArchiveWriteService;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.model.ZipState;
import com.ntak.pearlzip.ui.pub.ContextMenuController;
import com.ntak.pearlzip.ui.pub.ZipLauncher;
import com.ntak.pearlzip.ui.util.ArchiveUtil;
import com.ntak.pearlzip.ui.util.JFXUtil;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.archive.constants.LoggingConstants.LOG_BUNDLE;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.model.ZipState.CONTEXT_MENU_INSTANCES;
import static com.ntak.pearlzip.ui.model.ZipState.ROW_TRIGGER;
import static com.ntak.pearlzip.ui.util.ArchiveUtil.checkPreOpenDialog;
import static com.ntak.pearlzip.ui.util.ArchiveUtil.launchMainStage;
import static com.ntak.pearlzip.ui.util.JFXUtil.isFileInArchiveLevel;
import static com.ntak.pearlzip.ui.util.JFXUtil.raiseAlert;

/**
 *  Event Handler for processing archive file-entry click events.
 *  @author Aashutos Kakshepati
*/
public class FileInfoRowEventHandler implements  EventHandler<MouseEvent> {

    private static final Logger LOGGER = LoggerContext.getContext().getLogger(FileInfoRowEventHandler.class);
    private final TableView<FileInfo> fileContentsView;
    private final Button btnUp;
    private final TableRow<FileInfo> row;
    private final FXArchiveInfo fxArchiveInfo;

    public FileInfoRowEventHandler(TableView<FileInfo> fileContentsView, Button btnUp, TableRow<FileInfo> row,
            FXArchiveInfo fxArchiveInfo) {
        super();
        this.fileContentsView = fileContentsView;
        this.btnUp = btnUp;
        this.row = row;
        this.fxArchiveInfo = fxArchiveInfo;
    }

    @Override
    public void handle(MouseEvent event) {
            if (Objects.nonNull(row.getItem())) {
                ROW_TRIGGER.set(true);
            }

            if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY
                    && event.getClickCount() == 2) {

                FileInfo clickedRow = row.getItem();
                LOGGER.debug(resolveTextKey(LOG_CLICKED_ROW, clickedRow.getFileName()));

                final Path selectedFile = Paths.get(clickedRow.getFileName());
                // An archive that can be opened by this application...
                long sessionId = System.currentTimeMillis();
                final Stage thisStage = (Stage) fileContentsView.getScene()
                                                             .getWindow();
                // Extract tar ball into temp location from wrapped zip
                final Path nestedArchive = Paths.get(STORE_TEMP.toAbsolutePath()
                                                               .toString(), String.format("pz%d",
                                                                                          sessionId),
                                                     selectedFile
                                                             .getFileName().toString());

                if (ZipState.supportedReadArchives().stream().anyMatch(e -> clickedRow.getFileName().endsWith(String.format(".%s", e)))) {
                    JFXUtil.executeBackgroundProcess(sessionId, thisStage,
                                                     ()-> {
                                                         JFXUtil.runLater(() -> row.setDisable(true));

                                                         // LOG: An archive which can be extracted...
                                                         LOGGER.debug(resolveTextKey(LOG_ARCHIVE_CAN_EXTRACT));

                                                         // Prepare target temporary location for nested archive
                                                         Files.createDirectories(nestedArchive.getParent());
                                                         Files.deleteIfExists(nestedArchive);

                                                         // Extract nested archive to the temp location
                                                         ArchiveReadService parentArchiveReadService =
                                                                 ZipState.getReadArchiveServiceForFile(fxArchiveInfo.getArchivePath())
                                                                         .get();
                                                         parentArchiveReadService.extractFile(sessionId, nestedArchive,
                                                                                    fxArchiveInfo.getArchiveInfo(),
                                                                                    clickedRow
                                                         );

                                                         // Open nested archive.
                                                         ArchiveWriteService nestedArchiveWriteService =
                                                                 ZipState.getWriteArchiveServiceForFile(clickedRow.getFileName())
                                                                         .get();
                                                         ArchiveReadService nestedArchiveReadService =
                                                                 ZipState.getReadArchiveServiceForFile(nestedArchive.getFileName().toString()).get();
                                                         ArchiveInfo nestedArchiveInfo =
                                                                 nestedArchiveReadService.generateArchiveMetaData(nestedArchive.toAbsolutePath().toString());

                                                         // Check for any pre-open dialogs then launch and process as
                                                         // necessary
                                                         checkPreOpenDialog(nestedArchiveReadService,
                                                                            nestedArchiveInfo);

                                                         FXArchiveInfo archiveInfo =
                                                                 new FXArchiveInfo(fxArchiveInfo.getArchiveInfo(),
                                                                                   nestedArchiveInfo.getArchivePath(),
                                                                                   nestedArchiveReadService,
                                                                                   nestedArchiveWriteService,
                                                                                   nestedArchiveInfo,
                                                                                   clickedRow);
                                                         fxArchiveInfo.getController()
                                                                      .get()
                                                                      .getWrapper()
                                                                      .setDisable(true);
                                                         JFXUtil.runLater(() -> launchMainStage(archiveInfo));

                                                     },
                                                     (e)-> {
                                                         // LOG: %s occurred on trying to open nested tar ball. Message: %s
                                                         LOGGER.error(resolveTextKey(LOG_ERR_OPEN_NESTED_TARBALL,
                                                                                     e.getClass()
                                                                                      .getCanonicalName(),
                                                                                     e.getMessage()));
                                                         // TITLE: Error: On extracting tarball
                                                         // HEADER: Issue extracting tarball
                                                         // BODY: An issue occurred on loading tar file: %s
                                                         JFXUtil.runLater(
                                                                 () -> raiseAlert(Alert.AlertType.WARNING,
                                                                                  resolveTextKey(
                                                                                          TITLE_ERR_OPEN_NESTED_TARBALL),
                                                                                  resolveTextKey(
                                                                                          HEADER_ERR_OPEN_NESTED_TARBALL),
                                                                                  resolveTextKey(
                                                                                          BODY_ERR_OPEN_NESTED_TARBALL,
                                                                                          clickedRow.getFileName()),
                                                                                  (Exception) e,
                                                                                  fileContentsView.getScene()
                                                                                                  .getWindow()));
                                                     },
                                                     (s)->{
                                                         final KeyFrame step1 = new KeyFrame(Duration.millis(300),
                                                                                             e -> {row.setDisable(false);
                                                                                             thisStage.toFront();
                                                                                             Stage currentStage =
                                                                                                     Stage.getWindows()
                                                                                                          .stream()
                                                                                                          .map(Stage.class::cast)
                                                                                                          .filter(stg -> stg.getTitle() != null && stg.getTitle().contains(nestedArchive.toAbsolutePath().toString()))
                                                                                                          .findFirst()
                                                                                                          .orElse(null);
                                                                                             if (Objects.nonNull(currentStage)) {
                                                                                                 currentStage.toFront();
                                                                                             }
                                                         });
                                                         final Timeline timeline = new Timeline(step1);
                                                         JFXUtil.runLater(timeline::play);
                                                     }
                    );
                    return;
                }

                if (clickedRow.isFolder()) {
                    fxArchiveInfo.getDepth()
                            .incrementAndGet();
                    fxArchiveInfo.setPrefix(clickedRow.getFileName());
                    fileContentsView.setItems(FXCollections.observableArrayList(fxArchiveInfo.getFiles()
                                                                                        .stream()
                                                                                        .filter(isFileInArchiveLevel(fxArchiveInfo))
                                                                                        .collect(
                                                                                                Collectors.toList())));
                    fileContentsView.refresh();
                    if (fxArchiveInfo.getDepth()
                                .get() > 0) {
                        btnUp.setVisible(true);
                    }
                } else { // Open file externally?
                    ArchiveUtil.openExternally(sessionId, thisStage,
                                               fxArchiveInfo, clickedRow);
                }
            }
            else if (!row.isEmpty() && event.getButton() == MouseButton.SECONDARY) {
                try {
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(ZipLauncher.class.getClassLoader()
                                                        .getResource("contextmenu.fxml"));
                    loader.setResources(LOG_BUNDLE);
                    ContextMenu root = loader.load();
                    ContextMenuController controller = loader.getController();
                    controller.initData(fxArchiveInfo, row);
                    root.show(row, event.getScreenX(), event.getScreenY());
                    synchronized(CONTEXT_MENU_INSTANCES) {
                        CONTEXT_MENU_INSTANCES.forEach(ContextMenu::hide);
                        CONTEXT_MENU_INSTANCES.clear();
                        CONTEXT_MENU_INSTANCES.add(root);
                    }
                } catch (Exception e) {

                }
            }
    }

}
