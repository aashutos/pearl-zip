/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.util.internal;

import com.ntak.pearlzip.archive.constants.LoggingConstants;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.mac.MacPearlZipApplication;
import com.ntak.pearlzip.ui.pub.FrmLicenseDetailsController;
import com.ntak.pearlzip.ui.pub.ZipLauncher;
import com.ntak.pearlzip.ui.util.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.web.WebEngine;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.*;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import static com.ntak.pearlzip.archive.constants.LoggingConstants.LOG_BUNDLE;
import static com.ntak.pearlzip.archive.util.LoggingUtil.getStackTraceFromException;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ResourceConstants.*;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.internal.ArchiveUtil.initialiseApplicationSettings;

/**
 *  Utility methods used by Pearl Zip to perform common JavaFX UI routines.
 *  @author Aashutos Kakshepati
 */
public class JFXUtil {
    private static final Logger LOGGER = LoggerContext.getContext().getLogger(JFXUtil.class);

    public static void changeButtonPicText(ButtonBase button, String imgResource, String labelText) {
        ((ImageView)button.getGraphic()).setImage(new Image(String.valueOf(com.ntak.pearlzip.ui.util.JFXUtil.class.getClassLoader().getResource(imgResource))));
        button.setText(labelText);
    }

    public static void highlightCellIfMatch(TableCell cell, FileInfo row, FileInfo ref, BackgroundFill backgroundColor) {
        if (row.getFileName().equals(ref.getFileName())) {
            cell.setBackground(new Background(backgroundColor));
        }
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

    public static boolean checkNewVersionAvailable() {
        List<NotificationEntry> entries = getNotifications("version-check", true, "PearlZip Version");
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
                if (VersionComparator.getInstance().compare(versionLine, System.getProperty(CNS_NTAK_PEARL_ZIP_RAW_VERSION)) >= 0
                        && !hashLine.equals(System.getProperty(CNS_NTAK_PEARL_ZIP_COMMIT_HASH))) {
                    // TITLE: New Version of PearlZip is available
                    // BODY: A newer version of PearlZip (version %s) is available for download. Please visit %s to
                    //       download the latest version.
                    com.ntak.pearlzip.ui.util.JFXUtil.runLater(()-> com.ntak.pearlzip.ui.util.JFXUtil.raiseAlert(Alert.AlertType.INFORMATION,
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
        com.ntak.pearlzip.ui.util.JFXUtil.runLater(()-> com.ntak.pearlzip.ui.util.JFXUtil.raiseAlert(Alert.AlertType.INFORMATION,
                                                                                                     resolveTextKey(TITLE_LATEST_VERSION),
                                                                                                     null,
                                                                                                     resolveTextKey(BODY_LATEST_VERSION),
                                                                                                     null));
        return false;
    }

    public static List<NotificationEntry> getNotifications(boolean isRefreshForced, String... filters) {
        return getNotifications("get-notifications", isRefreshForced, filters);
    }

    public static List<NotificationEntry> getNotifications(String purpose, boolean isRefreshForced, String... filters) {
        List<NotificationEntry> entries = new CopyOnWriteArrayList<>();

        try {
            QueryExecutor executor = new QueryExecutor.QueryExecutorBuilder()
                    .withCacheIdentifier(purpose)
                    .withQueryByIdentifier("notification-query")
                    .withRefreshForced(isRefreshForced)
                    .withParameter("topics", List.of(filters), (p) -> String.join("','", (List<String>)p))
                    .build();
            executor.execute();
            Optional<QueryResult> optRes;
            if ((optRes = executor.getQueryResult()).isPresent()) {
                QueryResult result = optRes.get();
                return result.mapResult((r,i) -> new NotificationEntry(r.getInt(i,"id"), r.getString(i,"topic").orElse(""), r.getString(i,"message").orElse(""), r.getTimestamp(i,"creationtimestamp", "yyyy-MM-dd HH:mm:ss.SSSSSS").orElse(null)));
            }
        } catch (Exception e) {
            // LOG: Exception raised upon trying to retrieve notifications.\nStack trace:\n%s
            LOGGER.error(resolveTextKey(LOG_NOTIFICATIONS_ISSUE), getStackTraceFromException(e));
        }
        return entries;
    }

    public static int getExtensionStoreEntryCount(String connectionId, String version, boolean isRefreshForced) {
        StoreRepoDetails storeRepoDetails = InternalContextCache.INTERNAL_CONFIGURATION_CACHE
                .<Map<String,StoreRepoDetails>>getAdditionalConfig(CK_STORE_REPO)
                .get().get(connectionId);
        try(Connection connection = DriverManager.getConnection(storeRepoDetails.url(), storeRepoDetails.username(), storeRepoDetails.password())) {
            QueryExecutor executor = new QueryExecutor.QueryExecutorBuilder()
                    .withCacheIdentifier("store-count-query")
                    .withQueryByIdentifier("store-count-query")
                    .withRefreshForced(isRefreshForced)
                    .withParameter("version", version)
                    .build();
            executor.setConnection(connection);
            executor.execute();
            Optional<QueryResult> optRes;
            if ((optRes = executor.getQueryResult()).isPresent()) {
                QueryResult result = optRes.get();
                return result.mapResult((r,i) -> r.getInt(i, "count")).get(0);
            }
        } catch (Exception e) {
            // LOG: Exception raised upon trying to retrieve extension store count.\nStack trace:\n%s
            LOGGER.error(resolveTextKey(LOG_EXTENSION_STORE_COUNT_ISSUE), getStackTraceFromException(e));
        }
        return 0;
    }

    public static List<ExtensionStoreEntry> getExtensionStoreEntries(String connectionId, String version, int offset, int pagination, boolean isRefreshForced) {

        List<ExtensionStoreEntry> results = new LinkedList<>();
        StoreRepoDetails storeRepoDetails = InternalContextCache.INTERNAL_CONFIGURATION_CACHE
                .<Map<String,StoreRepoDetails>>getAdditionalConfig(CK_STORE_REPO)
                .get().get(connectionId);
        try(Connection connection = DriverManager.getConnection(storeRepoDetails.url(), storeRepoDetails.username(), storeRepoDetails.password())) {
            QueryExecutor executor = new QueryExecutor.QueryExecutorBuilder()
                    .withCacheIdentifier(String.format("store-extension-query#%d", offset))
                    .withQueryByIdentifier("store-extension-query")
                    .withRefreshForced(isRefreshForced)
                    .withParameter("version", version)
                    .withParameter("lowerBound", offset * pagination)
                    .withParameter("upperBound", (offset + 1) * pagination)
                    .build();
            executor.setConnection(connection);
            executor.execute();
            Optional<QueryResult> optRes;
            if ((optRes = executor.getQueryResult()).isPresent()) {
                QueryResult result = optRes.get();
                return result.mapResult((r,i) -> new ExtensionStoreEntry(r.getInt(i,"id"),
                                                                         r.getString(i, "packagename").get(),
                                                                         r.getString(i, "packageurl").get(),
                                                                         r.getString(i, "packagehash").get(),
                                                                         r.getString(i, "description").get(),
                                                                         r.getString(i, "minversion").get(),
                                                                         r.getString(i, "maxversion").get(),
                                                                         r.getString(i, "typename").get(),
                                                                         r.getString(i, "providername").get(),
                                                                         r.getString(i, "about").get())
                );
            }
        } catch (Exception e) {
            // LOG: Exception raised upon trying to retrieve extension store records.\nStack trace:\n%s
            LOGGER.error(resolveTextKey(LOG_EXTENSION_STORE_ISSUE), getStackTraceFromException(e));
        }
        return results;
    }

    public static Properties initialiseBootstrapProperties(Path storeRoot, Path bootstrapPropertiesFile) throws IOException {
        Properties props = new Properties();
        props.load(MacPearlZipApplication.class.getClassLoader()
                                               .getResourceAsStream("application.properties"));

        initialiseApplicationSettings();

        String defaultModulePath = Path.of(storeRoot.toAbsolutePath().toString(), "providers").toString();
        Path RUNTIME_MODULE_PATH =
                Paths.get(System.getProperty(CNS_NTAK_PEARL_ZIP_MODULE_PATH, defaultModulePath)).toAbsolutePath();
        InternalContextCache.INTERNAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_RUNTIME_MODULE_PATH, RUNTIME_MODULE_PATH);

        // Overwrite with external properties file
        // Reserved properties are kept as per internal key definition
        Map<String,String> reservedKeyMap = new HashMap<>();
        Path tmpRK = Paths.get(storeRoot.toString(), "rk");
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
                reservedKeys = InternalContextCache.INTERNAL_CONFIGURATION_CACHE
                                                   .<FileSystem>getAdditionalConfig(CK_JRT_FILE_SYSTEM)
                                                   .get()
                                                   .getPath("modules", "com.ntak.pearlzip.ui", "reserved-keys");
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

        if (Files.exists(bootstrapPropertiesFile)) {
            props.load(Files.newBufferedReader(bootstrapPropertiesFile));
        }

        InternalContextCache.INTERNAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_RK_KEYS, reservedKeyMap.keySet());
        props.putAll(System.getProperties());
        props.putAll(reservedKeyMap);
        System.setProperties(props);
        LoggingConstants.ROOT_LOGGER.info(props);
        return props;
    }

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

    public static void extractResources(Path targetDirectory, String moduleName, String resource) throws IOException {
        Files.createDirectories(targetDirectory);

        Stream<Path> queryFiles;
        try {
            queryFiles = Files.list(Paths.get(JFXUtil.class.getClassLoader()
                                                                  .getResource(resource)
                                                                  .getPath()));
        } catch (Exception e) {
            try {
                queryFiles = Files.list(InternalContextCache.INTERNAL_CONFIGURATION_CACHE
                                                .<FileSystem>getAdditionalConfig(CK_JRT_FILE_SYSTEM)
                                                .get()
                                                .getPath("modules", moduleName, resource)
                                                .toAbsolutePath());
            } catch(Exception exc) {
                final String resPath = JFXUtil.class.getClassLoader()
                                                             .getResource(resource)
                                                             .getPath();
                Path jarArchive =
                        Paths.get(resPath.substring(0, resPath.indexOf('!'))
                                           .replaceAll("file:",""));

                Path tempDir = Files.createTempDirectory("pz");
                Path srcQueryPath =
                        Paths.get(tempDir.toAbsolutePath().toString(),
                                  resPath.substring(resPath.indexOf('!')+1));

                try (JarFile jar = new JarFile(jarArchive.toFile())) {
                    for (JarEntry entry : jar.stream().toList()) {
                        if (entry.isDirectory()) {
                            Path entryDest = tempDir.resolve(entry.getName());

                            if (entry.isDirectory()) {
                                Files.createDirectory(entryDest);
                                continue;
                            }

                            Files.copy(jar.getInputStream(entry), entryDest);
                        }
                    }
                }

                queryFiles = Files.list(srcQueryPath);
            }
        }

        queryFiles.forEach(f -> {
                               try {
                                   Files.copy(f,
                                              Paths.get(targetDirectory.toAbsolutePath()
                                                                    .toString(),
                                                        f.getFileName()
                                                         .toString()
                                              ),
                                              StandardCopyOption.REPLACE_EXISTING);
                               } catch(IOException e) {
                               }
                           }
        );
    }

    public static void persistStoreRepoDetails(StoreRepoDetails storeRepoDetails, Path repoFile) {
        try {
            Files.deleteIfExists(repoFile);
        } catch(IOException e) {
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(repoFile.toString()))) {
            oos.writeObject(storeRepoDetails);
            oos.flush();
        } catch (IOException ioe) {
            // LOG: Issue persisting Repository metadata (%s). Message: %s\nStack Trace:\n%s
            LOGGER.error(resolveTextKey(LOG_ISSUE_PERSISTING_REPO, storeRepoDetails.name(), getStackTraceFromException(ioe)));
        }
    }

    public static void loadStoreRepoDetails(Path repoFile) {
        try (ObjectInputStream oos = new ObjectInputStream(new FileInputStream(repoFile.toString()))) {
            Object o = oos.readObject();
            if (o instanceof StoreRepoDetails repo) {
                InternalContextCache.INTERNAL_CONFIGURATION_CACHE.<Map<String,StoreRepoDetails>>getAdditionalConfig(CK_STORE_REPO).get()
                                                                 .put(repo.name(), repo);
            }
        } catch (Exception e) {
            // LOG: Issue parsing Repository metadata (%s). Message: %s\nStack Trace:\n%s
            LOGGER.error(resolveTextKey(LOG_ISSUE_PARSING_REPO, repoFile.getFileName(), getStackTraceFromException(e)));
        }
    }
}
