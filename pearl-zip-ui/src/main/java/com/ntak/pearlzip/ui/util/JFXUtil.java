/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.util;

import com.jfoenix.controls.JFXSnackbar;
import com.ntak.pearlzip.archive.pub.ArchiveService;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.archive.pub.ProgressMessage;
import com.ntak.pearlzip.ui.constants.ZipConstants;
import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.archive.constants.LoggingConstants.*;
import static com.ntak.pearlzip.archive.util.LoggingUtil.getStackTraceFromException;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ResourceConstants.DSV;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.model.ZipState.LOCK_POLL_TIMEOUT;
import static com.ntak.pearlzip.ui.util.internal.ArchiveUtil.launchProgress;
import static javafx.scene.control.ProgressIndicator.INDETERMINATE_PROGRESS;

/**
 *  Exposed utility methods used by Pearl Zip and downstream plugins to perform common JavaFX UI routines.
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
        ExecutorService PRIMARY_EXECUTOR_SERVICE = InternalContextCache.INTERNAL_CONFIGURATION_CACHE
                .<ExecutorService>getAdditionalConfig(CK_PRIMARY_EXECUTOR_SERVICE)
                .get();

        PRIMARY_EXECUTOR_SERVICE.submit(()-> {
            Lock readLock = InternalContextCache.INTERNAL_CONFIGURATION_CACHE
                                                .<ReadWriteLock>getAdditionalConfig(CK_LCK_CLEAR_CACHE)
                                                .get()
                                                .readLock();
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

    public static void toastMessage(JFXSnackbar toast, String message) {
        try {
            toast.enqueue(new JFXSnackbar.SnackbarEvent(new TextField(message),
                                                        Duration.millis(Double.parseDouble(System.getProperty(
                                                                CNS_NTAK_PEARL_ZIP_TOAST_DURATION, "500")))));
        } catch (Exception e) {
            if (Objects.nonNull(toast)) {
                toast.enqueue(new JFXSnackbar.SnackbarEvent(new TextField(message),
                                                            Duration.millis(500.0)));
            }
        }
    }

    public static Optional<String> getActiveWindowFromMenu() {
        Menu WINDOW_MENU =
                InternalContextCache.INTERNAL_CONFIGURATION_CACHE.<Menu>getAdditionalConfig(CK_WINDOW_MENU).get();
        synchronized(WINDOW_MENU) {
            return WINDOW_MENU
                    .getItems()
                    .stream()
                    .filter(f -> f.getText()
                                  .contains(WINDOW_FOCUS_SYMBOL)
                    )
                    .map(f -> f.getText()
                               .replace(ZipConstants.WINDOW_FOCUS_SYMBOL, ""))
                    .findFirst();
        }
    }

    public static List<String> getWindowsFromMenu() {
        Menu WINDOW_MENU =
                InternalContextCache.INTERNAL_CONFIGURATION_CACHE.<Menu>getAdditionalConfig(CK_WINDOW_MENU).get();
        synchronized(WINDOW_MENU) {
            return WINDOW_MENU.getItems()
                              .stream()
                              .map(m -> m.getText()
                                         .replace(ZipConstants.WINDOW_FOCUS_SYMBOL, "")
                              )
                    .collect(Collectors.toList());
        }
    }

    // Untested functionality...
    public static List<String> getRecentFilesFromMenu() {
        final Menu RECENT_FILES_MENU = InternalContextCache.INTERNAL_CONFIGURATION_CACHE
                .<Menu>getAdditionalConfig(CK_RECENT_FILES_MENU)
                .get();
        synchronized(RECENT_FILES_MENU) {
            return RECENT_FILES_MENU
                    .getItems()
                    .stream()
                    .sequential()
                    .map(m -> DSV.split(m.getText())[1].trim())
                    .collect(Collectors.toList());
        }
    }
}
