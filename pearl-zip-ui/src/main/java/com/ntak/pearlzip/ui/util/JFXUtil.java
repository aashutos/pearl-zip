/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.util;

import com.ntak.pearlzip.archive.pub.ArchiveService;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.archive.pub.ProgressMessage;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.pub.FrmLicenseDetailsController;
import com.ntak.pearlzip.ui.pub.ZipLauncher;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.archive.constants.LoggingConstants.*;
import static com.ntak.pearlzip.archive.util.LoggingUtil.getStackTraceFromException;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.model.ZipState.LOCK_POLL_TIMEOUT;
import static com.ntak.pearlzip.ui.util.ArchiveUtil.launchProgress;
import static javafx.scene.control.ProgressIndicator.INDETERMINATE_PROGRESS;

/**
 *  Utility methods used by Pearl Zip to perform common JavaFX UI routines.
 *  @author Aashutos Kakshepati
*/
public class JFXUtil {
    private static final Logger LOGGER = LoggerContext.getContext().getLogger(JFXUtil.class);

    public static Optional<ButtonType> raiseAlert(Alert.AlertType type, String title, String header, String body,
            Window stage) {
        return raiseAlert(type, title, header, body, null, stage);
    }

    public static Optional<ButtonType> raiseAlert(Alert.AlertType type, String title, String header, String body,
            Exception exception, Window stage, ButtonType... buttons) {
        Alert alert = new Alert(type, body, buttons);
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(body);

        if (Objects.nonNull(exception)) {
            VBox vbox = new VBox();

            Label lblStackTrace = new Label("Stacktrace:");
            TextArea taTrace = new TextArea(getStackTraceFromException(exception));
            taTrace.setEditable(false);
            taTrace.setWrapText(true);

            taTrace.setMaxWidth(Double.MAX_VALUE);
            taTrace.setMaxHeight(Double.MAX_VALUE);

            vbox.getChildren().addAll(lblStackTrace,taTrace);
            alert.getDialogPane().setExpandableContent(vbox);
        }
        ((Stage)alert.getDialogPane().getScene().getWindow()).setAlwaysOnTop(true);
        try {
            return alert.showAndWait();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static void changeButtonPicText(ButtonBase button, String imgResource, String labelText) {
        ((ImageView)button.getGraphic()).setImage(new Image(String.valueOf(JFXUtil.class.getClassLoader().getResource(imgResource))));
        button.setText(labelText);
    }

    public static void highlightCellIfMatch(TableCell cell, FileInfo row, FileInfo ref, BackgroundFill backgroundColor) {
        if (row.getFileName().equals(ref.getFileName())) {
            cell.setBackground(new Background(backgroundColor));
        }
    }

    public static Optional<Stage> getActiveStage() {
        return (Stage.getWindows()
                     .stream()
                     .filter(Window::isFocused)
                     .map(Stage.class::cast))
                .findFirst();
    }

    public static List<Stage> getMainStageInstances() {
        return Stage.getWindows()
                    .stream()
                    .map(Stage.class::cast)
                    .filter(s -> s.isShowing() && Optional.ofNullable(s.getTitle())
                                                          .orElse("")
                                                          .matches(resolveTextKey(TITLE_FILE_PATTERN,
                                                                                  ".*",
                                                                                  ".*",
                                                                                  ".*")))
                    .collect(Collectors.toList());
    }

    public static Optional<Stage> getMainStageByArchivePath(String archivePath) {
        return getMainStageInstances()
                .stream()
                .filter(m -> m.getTitle()
                              .contains(archivePath))
                .findFirst();
    }

    public static void refreshFileView(TableView<FileInfo> fileInfoTableView, FXArchiveInfo fxArchiveInfo, int depth,
            String prefix) {
        fxArchiveInfo.refresh();
        fxArchiveInfo.getDepth()
                     .set(depth);
        fxArchiveInfo.setPrefix(prefix);
        fileInfoTableView.setItems(FXCollections.observableArrayList(fxArchiveInfo.getFiles()
                                                                                  .stream()
                                                                                  .filter(isFileInArchiveLevel(
                                                                                          fxArchiveInfo))
                                                                                  .collect(
                                                                                         Collectors.toList())));
        fileInfoTableView.refresh();
    }

    public static Predicate<FileInfo> isFileInArchiveLevel(FXArchiveInfo fxArchiveInfo) {
        return f -> {
            final boolean sameDepth = f.getLevel() == fxArchiveInfo.getDepth()
                                                                   .get();
            if (fxArchiveInfo.getDepth()
                             .get() > 0) {
                return sameDepth
                        && f.getFileName()
                            .startsWith(
                                    String.format("%s%s",
                                                  fxArchiveInfo.getPrefix(),
                                                  File.separator)
                            );
            } else {
                return sameDepth;
            }
        };
    }

    public static void executeBackgroundProcess(long sessionId, Stage parent, CaughtRunnable process,
            Consumer<Stage> callback) {
        executeBackgroundProcess(sessionId, parent, process, (e)->{}, callback);
    }

    public static void executeBackgroundProcess(long sessionId, Stage parent, CaughtRunnable process,
            Consumer<Throwable> handler, Consumer<Stage> callback) {
        CountDownLatch latch = new CountDownLatch(1);

        PRIMARY_EXECUTOR_SERVICE.submit(()-> {
            Lock readLock = LCK_CLEAR_CACHE.readLock();
            try {
                latch.await();
                ArchiveService.DEFAULT_BUS.post(new ProgressMessage(sessionId, PROGRESS,
                                                                    resolveTextKey(LBL_PROGRESS_LOADING),
                                                                    INDETERMINATE_PROGRESS, 1));

                // Multiple permits of read are allowed (as a shared lock), so multiple zip processes can effectively occur in
                // parallel.
                // When write is captured by clear cache process. Read lock acquisition will be blocked (tryLock) due to
                // exclusiveness of write lock.

                while (!readLock.tryLock()) {
                    try {
                        Thread.sleep(LOCK_POLL_TIMEOUT);
                    } catch(Exception exc) {
                    }
                }

                process.run();
            } catch (Exception e) {
                handler.accept(e);
                // LOG: %s thrown running a background process. \nMessage: %s\nStack trace:\n%s
                LOGGER.error(resolveTextKey(LOG_ISSUE_RUNNING_BACKGROUND_PROCESS), e.getClass().getCanonicalName(),
                             e.getMessage(),
                             getStackTraceFromException(e));
            } finally {
                ArchiveService.DEFAULT_BUS.post(new ProgressMessage(sessionId, COMPLETED, COMPLETED, 1, 1));
                try {
                    readLock.unlock();
                } catch (Exception e) {
                }
            }
        });

        launchProgress(sessionId, parent, latch, callback);
    }

    public static void loadPreOpenDialog(Stage stage, Node root) {
        AnchorPane pane = new AnchorPane(root);
        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.initStyle(StageStyle.UNDECORATED);

        stage.toFront();
        stage.setAlwaysOnTop(true);
        stage.showAndWait();
    }

    public static Optional<FXArchiveInfo> lookupArchiveInfo(String archiveName) {
        return Optional.of((FXArchiveInfo) Stage.getWindows()
                                                .stream()
                                                .map(Stage.class::cast)
                                                .filter(s -> s.getTitle() != null)
                                                .filter((s) -> s.getTitle()
                                                                .matches(String.format(".*%s$", archiveName)))
                                                .findFirst()
                                                .get()
                                                .getUserData());
    }

    public static void runLater(final Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }

    public static FrmLicenseDetailsController loadLicenseDetails(String licensePath, String content,
            boolean withAcceptDecline) throws IOException {
        Stage licDetailsStage = new Stage();

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(ZipLauncher.class.getClassLoader()
                                            .getResource("frmLicenseDetails.fxml"));
        loader.setResources(LOG_BUNDLE);
        AnchorPane root = loader.load();

        FrmLicenseDetailsController controller = loader.getController();
        controller.initData(licDetailsStage, content, withAcceptDecline);

        Scene scene = new Scene(root);
        // License Details : %s
        licDetailsStage.setTitle(resolveTextKey(TITLE_LICENSE_DETAILS, licensePath));
        licDetailsStage.setScene(scene);
        licDetailsStage.setResizable(false);

        licDetailsStage.initModality(Modality.APPLICATION_MODAL);
        licDetailsStage.setAlwaysOnTop(true);
        licDetailsStage.showAndWait();

        return controller;
    }

    public static void checkWebEngineScrollToBottom(WebEngine engine, Consumer<Boolean> callback) {
        int scrollY = (Integer) engine.executeScript("window.scrollY");
        int innerHeight = (Integer) engine.executeScript("window.innerHeight");
        int scrollHeight = (Integer) engine.executeScript("document.documentElement.scrollHeight");
        int offsetHeight = (Integer) engine.executeScript("document.documentElement.offsetHeight");
        boolean isScrollBottom = scrollY + innerHeight + (innerHeight * (innerHeight / offsetHeight)) >= scrollHeight;
        callback.accept(isScrollBottom);
    }

    public static <S> Optional<TableCell<S,?>> getTableCellForColumnRow(TableView<S> table, int rowIndex,
            String columnName) {
        Integer columnIndex = table.getColumns()
                    .indexOf(table.getColumns()
                                         .stream()
                                         .filter(c -> c.getText().equals(columnName))
                                         .findFirst()
                                         .orElse(null));
        if (Objects.nonNull(columnIndex)) {
            Object obj = table.queryAccessibleAttribute(AccessibleAttribute.CELL_AT_ROW_COLUMN, rowIndex, columnIndex);

            if (obj instanceof TableCell tc) {
                return Optional.ofNullable((TableCell<S,?>) tc);
            }
        }

        return Optional.empty();
    }
}
