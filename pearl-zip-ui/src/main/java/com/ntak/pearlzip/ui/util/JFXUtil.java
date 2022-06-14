/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.util;

import com.jfoenix.controls.JFXSnackbar;
import com.ntak.pearlzip.archive.constants.LoggingConstants;
import com.ntak.pearlzip.archive.pub.ArchiveService;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.archive.pub.ProgressMessage;
import com.ntak.pearlzip.archive.util.LoggingUtil;
import com.ntak.pearlzip.ui.constants.ZipConstants;
import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.mac.MacPearlZipApplication;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.pub.FrmLicenseDetailsController;
import com.ntak.pearlzip.ui.pub.ZipLauncher;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Parent;
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
import javafx.util.Duration;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.*;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.*;
import static com.ntak.pearlzip.archive.util.LoggingUtil.getStackTraceFromException;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ResourceConstants.*;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.model.ZipState.LOCK_POLL_TIMEOUT;
import static com.ntak.pearlzip.ui.util.ArchiveUtil.initialiseApplicationSettings;
import static com.ntak.pearlzip.ui.util.ArchiveUtil.launchProgress;
import static javafx.scene.control.ProgressIndicator.INDETERMINATE_PROGRESS;

/**
 *  Utility methods used by Pearl Zip to perform common JavaFX UI routines.
 *  @author Aashutos Kakshepati
*/
public class JFXUtil {
    private static final Logger LOGGER = LoggerContext.getContext().getLogger(JFXUtil.class);

    // NOTE: External
    public static Optional<ButtonType> raiseAlert(Alert.AlertType type, String title, String header, String body,
            Window stage) {
        return raiseAlert(type, title, header, body, null, stage);
    }

    // NOTE: External
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

    // NOTE: Internal
    public static void changeButtonPicText(ButtonBase button, String imgResource, String labelText) {
        ((ImageView)button.getGraphic()).setImage(new Image(String.valueOf(JFXUtil.class.getClassLoader().getResource(imgResource))));
        button.setText(labelText);
    }

    // NOTE: Internal
    public static void highlightCellIfMatch(TableCell cell, FileInfo row, FileInfo ref, BackgroundFill backgroundColor) {
        if (row.getFileName().equals(ref.getFileName())) {
            cell.setBackground(new Background(backgroundColor));
        }
    }

    // NOTE: External
    public static Optional<Stage> getActiveStage() {
        return (Stage.getWindows()
                     .stream()
                     .filter(Window::isFocused)
                     .map(Stage.class::cast))
                .findFirst();
    }

    // NOTE: External
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

    // NOTE: External
    public static Optional<Stage> getMainStageByArchivePath(String archivePath) {
        return getMainStageInstances()
                .stream()
                .filter(m -> m.getTitle()
                              .contains(archivePath))
                .findFirst();
    }

    // NOTE: External
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

    // NOTE: External
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

    // NOTE: External
    public static void executeBackgroundProcess(long sessionId, Stage parent, CaughtRunnable process,
            Consumer<Stage> callback) {
        executeBackgroundProcess(sessionId, parent, process, (e)->{}, callback);
    }

    // NOTE: External
    public static void executeBackgroundProcess(long sessionId, Stage parent, CaughtRunnable process,
            Consumer<Throwable> handler, Consumer<Stage> callback) {
        CountDownLatch latch = new CountDownLatch(1);
        ExecutorService PRIMARY_EXECUTOR_SERVICE = InternalContextCache.INTERNAL_CONFIGURATION_CACHE
                .<ExecutorService>getAdditionalConfig(CK_PRIMARY_EXECUTOR_SERVICE)
                .get();

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

    // NOTE: Internal
    public static void loadPreOpenDialog(Stage stage, Node root) {
        AnchorPane pane = new AnchorPane(root);
        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.initStyle(StageStyle.UNDECORATED);

        stage.toFront();
        stage.setAlwaysOnTop(true);
        stage.showAndWait();
    }

    // NOTE: External
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

    // NOTE: Internal
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

    // NOTE: Internal
    public static void checkWebEngineScrollToBottom(WebEngine engine, Consumer<Boolean> callback) {
        int scrollY = (Integer) engine.executeScript("window.scrollY");
        int innerHeight = (Integer) engine.executeScript("window.innerHeight");
        int scrollHeight = (Integer) engine.executeScript("document.documentElement.scrollHeight");
        int offsetHeight = (Integer) engine.executeScript("document.documentElement.offsetHeight");
        boolean isScrollBottom = scrollY + innerHeight + (innerHeight * (innerHeight / offsetHeight)) >= scrollHeight;
        callback.accept(isScrollBottom);
    }

    // NOTE: Internal
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

    // NOTE: Internal
    public static void showNotifications() {
        try {
            Stage notificationStage = new Stage();

            // Load notification form
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MacPearlZipApplication.class.getResource("/frmNotifications.fxml"));
            loader.setResources(LOG_BUNDLE);
            Parent root = loader.load();
            notificationStage.setScene(new Scene(root));

            notificationStage.setResizable(false);
            notificationStage.setAlwaysOnTop(true);
            notificationStage.toFront();
            notificationStage.show();
        } catch (Exception e) {
        }
    }

    // NOTE: Internal
    public static boolean checkNewVersionAvailable() {
        List<NotificationEntry> entries = getNotifications("PearlZip Version");
        Optional<NotificationEntry> optVersion = entries.stream()
                                                        .max(Comparator.comparingInt(NotificationEntry::id));
        if (optVersion.isPresent()) {
            NotificationEntry version = optVersion.get();
            String message = version.message();
            String[] identifiers = PSV.split(message);
            if (identifiers.length == 2) {
                String versionLine = ESV.split(identifiers[0])[1];
                String hashLine = ESV.split(identifiers[1])[1];
                // If a newer version string or a different hash of the same version is detected, a new version has
                // been released. I expect a version bump, however whenever a commit change occurs.
                if (VersionComparator.getInstance().compare(versionLine,System.getProperty(CNS_NTAK_PEARL_ZIP_RAW_VERSION)) >= 0
                        && !hashLine.equals(System.getProperty(CNS_NTAK_PEARL_ZIP_COMMIT_HASH))) {
                    // TITLE: New Version of PearlZip is available
                    // BODY: A newer version of PearlZip (version %s) is available for download. Please visit %s to
                    //       download the latest version.
                    JFXUtil.runLater(()-> raiseAlert(Alert.AlertType.INFORMATION,
                                                     resolveTextKey(TITLE_NEW_VERSION_AVAILABLE),
                                                     null,
                                                     resolveTextKey(BODY_NEW_VERSION_AVAILABLE, versionLine, System.getProperty(CNS_NTAK_PEARL_ZIP_WEBLINK)),
                                                     null));

                    return true;
                }
            }
        }

        // TITLE: PearlZip is up to date
        // BODY: You are using the latest version of PearlZip.
        JFXUtil.runLater(()-> raiseAlert(Alert.AlertType.INFORMATION,
                                         resolveTextKey(TITLE_LATEST_VERSION),
                                         null,
                                         resolveTextKey(BODY_LATEST_VERSION),
                                         null));
        return false;
    }

    // NOTE: Internal
    public static List<NotificationEntry> getNotifications(String... filters) {
        List<NotificationEntry> entries = new CopyOnWriteArrayList<>();

        try (Connection conn = DriverManager.getConnection(
                System.getProperty(CNS_NTAK_PEARL_ZIP_JDBC_URL), System.getProperty(CNS_NTAK_PEARL_ZIP_JDBC_USER), System.getProperty(CNS_NTAK_PEARL_ZIP_JDBC_PASSWORD))) {
            if (conn != null) {
                PreparedStatement ps = conn.prepareStatement(
                  String.format(
                   """
                   SELECT ID, TOPIC, MESSAGE, CREATIONTIMESTAMP
                   FROM PUBLIC.PearlZipNotifications
                   WHERE TOPIC IN (%s)
                   """,
                  Arrays.stream(filters)
                        .map(v -> "?")
                        .collect(Collectors.joining(", ")))
                );

                for (int i = 0; i < filters.length; i++) {
                    ps.setString(i+1, filters[i]);
                }

                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    entries.add(new NotificationEntry(rs.getInt("ID"),
                                                      rs.getString("TOPIC"),
                                                      rs.getString("MESSAGE"),
                                                      rs.getTimestamp("CREATIONTIMESTAMP").toLocalDateTime()
                      )
                    );
                }
            }
        } catch (SQLException e) {
            // LOG: SQL Exception occurred upon trying to retrieve notifications. SQL State: %s\nStack trace:\n%s
            LOGGER.error(resolveTextKey(LOG_NOTIFICATIONS_SQL_ISSUE,
                         e.getSQLState(),
                         LoggingUtil.getStackTraceFromException(e)));
        } catch (Exception e) {
            // LOG: Exception raised upon trying to retrieve notifications.\nStack trace:\n%s
            LOGGER.error(resolveTextKey(LOG_NOTIFICATIONS_ISSUE,
                         LoggingUtil.getStackTraceFromException(e))
            );
        }

        return entries;
    }

    // NOTE: Internal
    public static Properties initialiseBootstrapProperties() throws IOException {
        Properties props = new Properties();
        props.load(MacPearlZipApplication.class.getClassLoader()
                                               .getResourceAsStream("application.properties"));
        final Path STORE_ROOT = Paths.get(System.getProperty(CNS_STORE_ROOT,
                                                        String.format("%s/.pz",
                                                                      System.getProperty("user.home"))));
        InternalContextCache.GLOBAL_CONFIGURATION_CACHE
                .setAdditionalConfig(CK_STORE_ROOT, STORE_ROOT
                );
        InternalContextCache.GLOBAL_CONFIGURATION_CACHE
                            .setAdditionalConfig(CK_LOCAL_TEMP,
                                    Paths.get(Optional.ofNullable(System.getenv("TMPDIR"))
                                                      .orElse(STORE_ROOT.toString())
                                    )
        );
        Path APPLICATION_SETTINGS_FILE = Paths.get(STORE_ROOT.toString(), "application.properties");
        InternalContextCache.GLOBAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_APPLICATION_SETTINGS_FILE,
                                                                            APPLICATION_SETTINGS_FILE);
        initialiseApplicationSettings();

        String defaultModulePath = Path.of(STORE_ROOT.toAbsolutePath().toString(), "providers").toString();
        Path RUNTIME_MODULE_PATH =
                Paths.get(System.getProperty(CNS_NTAK_PEARL_ZIP_MODULE_PATH, defaultModulePath)).toAbsolutePath();
        InternalContextCache.INTERNAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_RUNTIME_MODULE_PATH, RUNTIME_MODULE_PATH);

        // Overwrite with external properties file
        // Reserved properties are kept as per internal key definition
        Map<String,String> reservedKeyMap = new HashMap<>();
        Path tmpRK = Paths.get(STORE_ROOT.toString(), "rk");
        try (FileOutputStream fileOutputStream = new FileOutputStream(tmpRK.toString());
             FileChannel channel = fileOutputStream.getChannel();
             FileLock lock = channel.lock()) {
            // Standard resource case
            Path reservedKeys = Paths.get(MacPearlZipApplication.class.getClassLoader()
                                                                      .getResource("reserved-keys")
                                                                      .getPath());
            LoggingConstants.ROOT_LOGGER.info(reservedKeys);

            // standard/jar resource case
            if (Objects.nonNull(reservedKeys)) {

                try (InputStream is = MacPearlZipApplication.class.getClassLoader()
                                                                  .getResourceAsStream("reserved-keys")) {
                    Files.copy(is, tmpRK, StandardCopyOption.REPLACE_EXISTING);
                }
            } else if (!Files.exists(reservedKeys)) {
                reservedKeys = JRT_FILE_SYSTEM.getPath("modules", "com.ntak.pearlzip.ui", "reserved-keys");
                Files.copy(reservedKeys, tmpRK, StandardCopyOption.REPLACE_EXISTING);
            }

            Files.lines(tmpRK)
                 .filter(k -> Objects.nonNull(k) && Objects.nonNull(props.getProperty(k)))
                 // LOG: Locking in key: %s with value: %s
                 .peek(k -> LoggingConstants.ROOT_LOGGER.info(resolveTextKey(LOG_LOCKING_IN_PROPERTY,
                                                                             k,
                                                                             props.getProperty(k))))
                 .forEach(k -> reservedKeyMap.put(k, props.getProperty(k)));
        }

        if (Files.exists(APPLICATION_SETTINGS_FILE)) {
            props.load(Files.newBufferedReader(APPLICATION_SETTINGS_FILE));
        }

        RK_KEYS = reservedKeyMap.keySet();
        props.putAll(System.getProperties());
        props.putAll(reservedKeyMap);
        System.setProperties(props);
        LoggingConstants.ROOT_LOGGER.info(props);
        return props;
    }

    // NOTE: External
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

    // NOTE: Internal
    public static void setSafeModeTitles(boolean isSafeMode, Stage stage) {
            String appName = System.getProperty(CNS_NTAK_PEARL_ZIP_APP_NAME, "PearlZip");
            if (isSafeMode) {
                if (!stage.getTitle().contains(resolveTextKey(TITLE_SAFE_MODE_PATTERN, appName))) {
                    stage.setTitle(stage.getTitle()
                                        .replace(appName, resolveTextKey(TITLE_SAFE_MODE_PATTERN, appName)));
                }
            } else {
                stage.setTitle(stage.getTitle()
                            .replace(resolveTextKey(TITLE_SAFE_MODE_PATTERN, appName), appName));
            }
        }

    // NOTE: Internal
    public static void initialiseTheme(Path themesPath, String themeName) {
        String themePath = String.format(PATTERN_CSS_THEME_PATH,
                                         themesPath.toAbsolutePath(),
                                         themeName,
                                         themeName);
        if (!Files.exists(Paths.get(URI.create(themePath).getPath()))) {
            themeName = "modena";
            themePath = String.format(PATTERN_CSS_THEME_PATH,
                                      themesPath.toAbsolutePath(),
                                      themeName,
                                      themeName);
        }
        Application.setUserAgentStylesheet(themePath);
        System.setProperty(CNS_THEME_NAME, themeName);
    }

    // NOTE: External
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

    // NOTE: External
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

    // NOTE: External
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
