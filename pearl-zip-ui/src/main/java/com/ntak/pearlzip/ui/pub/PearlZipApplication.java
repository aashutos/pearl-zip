/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.pub;

import com.ntak.pearlzip.archive.constants.LoggingConstants;
import com.ntak.pearlzip.archive.pub.ArchiveReadService;
import com.ntak.pearlzip.archive.pub.ArchiveService;
import com.ntak.pearlzip.archive.pub.ArchiveWriteService;
import com.ntak.pearlzip.archive.util.LoggingUtil;
import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.model.ZipState;
import com.ntak.pearlzip.ui.util.ArchiveUtil;
import com.ntak.pearlzip.ui.util.ErrorAlertConsumer;
import com.ntak.pearlzip.ui.util.JFXUtil;
import com.ntak.pearlzip.ui.util.ProgressMessageTraceLogger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Pair;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ntak.pearlzip.archive.constants.ArchiveConstants.*;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.LOG_BUNDLE;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.ROOT_LOGGER;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.mac.MacZipConstants.*;
import static com.ntak.pearlzip.ui.pub.ZipLauncher.OS_FILES;
import static com.ntak.pearlzip.ui.util.ArchiveUtil.addToRecentFile;
import static com.ntak.pearlzip.ui.util.ArchiveUtil.launchMainStage;

/**
 * Loads the main UI screen for the Zip Application.
 *
 * @author Aashutos Kakshepati
 */
public abstract class PearlZipApplication extends Application {

    static {
        Desktop.getDesktop()
               .setOpenFileHandler((e) -> e.getFiles()
                                           .stream()
                                           .peek(LoggingConstants.ROOT_LOGGER::info)
                                           .map(f -> f.toPath()
                                                      .toAbsolutePath()
                                                      .toString())
                                           .forEach(f -> JFXUtil.runLater(() -> {
                                               if (f.endsWith(".pzax")) {
                                                   try {
                                                       ArchiveUtil.loadPzaxPackage(f);
                                                   } catch(Exception exc) {
                                                   }
                                                   return;
                                               }
                                               Stage stage = new Stage();

                                               ArchiveReadService readService = ZipState.getReadArchiveServiceForFile(f)
                                                                                        .get();
                                               ArchiveWriteService writeService = ZipState.getWriteArchiveServiceForFile(
                                                                                                  f)
                                                                                          .orElse(null);
                                               final FXArchiveInfo fxArchiveInfo = new FXArchiveInfo(f,
                                                                                                     readService,
                                                                                                     writeService);

                                               // Generates PreOpen dialog, if required
                                               Optional<ArchiveService.FXForm> optNode;
                                               if ((optNode =
                                                       readService.getFXFormByIdentifier(ArchiveReadService.OPEN_ARCHIVE_OPTIONS,
                                                                                                fxArchiveInfo.getArchiveInfo())).isPresent()) {
                                                   Stage preOpenStage = new Stage();
                                                   Node root = optNode.get().getContent();
                                                   JFXUtil.loadPreOpenDialog(preOpenStage, root);

                                                   Pair<AtomicBoolean,String> result = (Pair<AtomicBoolean,String>) root.getUserData();

                                                   if (Objects.nonNull(result) && Objects.nonNull(result.getKey()) && !result.getKey()
                                                                                                                             .get()) {
                                                       // LOG: Issue occurred when opening archive %s. Issue reason: %s
                                                       ROOT_LOGGER.error(resolveTextKey(
                                                               LOG_INVALID_ARCHIVE_SETUP,
                                                               fxArchiveInfo.getArchivePath(),
                                                               result.getValue()));

                                                       JFXUtil.runLater(() -> stage.fireEvent(new WindowEvent(
                                                               stage,
                                                               WindowEvent.WINDOW_CLOSE_REQUEST)));
                                                       return;
                                                   }
                                               }
                                               stage.initStyle(StageStyle.DECORATED);
                                               launchMainStage(stage, fxArchiveInfo);
                                           })));
    }

    @Override
    public void start(Stage stage) throws IOException, InterruptedException {
        try {
            InternalContextCache.INTERNAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_APP, this);
            InternalContextCache.GLOBAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_HOST_SERVICES, this.getHostServices());
            InternalContextCache.GLOBAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_PARAMETERS, this.getParameters());

            CountDownLatch readyLatch = new CountDownLatch(1);

            // Loading additional EventBus consumers
            MESSAGE_TRACE_LOGGER = ProgressMessageTraceLogger.getMessageTraceLogger();
            ArchiveService.DEFAULT_BUS.register(MESSAGE_TRACE_LOGGER);

            ERROR_ALERT_CONSUMER = ErrorAlertConsumer.getErrorAlertConsumer();
            ArchiveService.DEFAULT_BUS.register(ERROR_ALERT_CONSUMER);

            ////////////////////////////////////////////
            ///// Create files and dir structure //////
            //////////////////////////////////////////

            // Create temporary store folder
            Path STORE_ROOT = InternalContextCache.GLOBAL_CONFIGURATION_CACHE
                                                  .<Path>getAdditionalConfig(CK_STORE_ROOT)
                                                  .get();
            Path STORE_TEMP = Paths.get(STORE_ROOT.toAbsolutePath()
                                                          .toString(), "temp");
            InternalContextCache.GLOBAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_STORE_TEMP, STORE_TEMP);

            if (!Files.exists(STORE_TEMP)) {
                Files.createDirectories(STORE_TEMP);
            }

            // Providers
            Path providerPath = Paths.get(STORE_ROOT.toAbsolutePath()
                                                    .toString(), "providers");
            Files.createDirectories(providerPath);

            // Themes
            Path themesPath = Paths.get(STORE_ROOT.toAbsolutePath()
                                                  .toString(), "themes");

            // Copy over and overwrite core themes...
            for (String theme : CORE_THEMES) {
                Path defThemePath = Paths.get(STORE_ROOT.toAbsolutePath()
                                                        .toString(), "themes", theme);
                Files.createDirectories(defThemePath);
                Stream<Path> themeFiles;
                try {
                    themeFiles = Files.list(Paths.get(getClass().getClassLoader()
                                                   .getResource(theme)
                                                   .getPath()));
                } catch (Exception e) {
                    themeFiles = Files.list(JRT_FILE_SYSTEM.getPath("modules", "com.ntak.pearlzip.ui",
                                            theme).toAbsolutePath());
                }
                themeFiles.forEach(f -> {
                                        try {
                                            Files.copy(f,
                                                       Paths.get(defThemePath.toAbsolutePath()
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

            // Initialise theme...
            String themeName = System.getProperty(CNS_THEME_NAME, "modena");
            JFXUtil.initialiseTheme(themesPath, themeName);

            // Initialise drag out constants...
            try {
                long maxSize = Long.parseLong(System.getProperty(CNS_NTAK_PEARL_ZIP_DEFAULT_MAX_SIZE_DRAG_OUT));
                MAX_SIZE_DRAG_OUT = maxSize;
            } catch (Exception e) {

            }

            // Recent files
            RECENT_FILE = Paths.get(STORE_ROOT.toAbsolutePath()
                                              .toString(), "rf");
            if (!Files.exists(RECENT_FILE)) {
                Files.createFile(RECENT_FILE);
            }

            // Setting about form...
            Stage aboutStage = genFrmAbout();

            // Load custom menus from plugins
            List<javafx.scene.control.Menu> customMenus = loadMenusFromPlugins();
            createSystemMenu(aboutStage, customMenus);

            readyLatch.countDown();

            // Show Notifications dialog
            try {
                if (OS_FILES.size() == 0 && Boolean.parseBoolean(CURRENT_SETTINGS.getProperty(CNS_SHOW_NOTIFICATION,
                                                                                              "true"))) {
                    JFXUtil.runLater(JFXUtil::showNotifications);
                }
            } catch(Exception e) {

            }

            // Initialise archive information
            FXArchiveInfo fxArchiveInfo;
            String archivePath;
            if (this.getParameters()
                    .getRaw()
                    .size() > 0 && Files.exists(Paths.get(this.getParameters()
                                                            .getRaw()
                                                            .get(0)))) {
                archivePath = this.getParameters()
                                 .getRaw()
                                 .get(0);
                addToRecentFile(new File(archivePath));
            } else if (OS_FILES.size() > 0) {
                // LOG: OS Trigger detected...
                LoggingConstants.ROOT_LOGGER.info(resolveTextKey(LOG_OS_TRIGGER_DETECTED));

                while (OS_FILES.size() < 1) {
                    Thread.sleep(250);
                }

                archivePath = OS_FILES.remove(0);

                if (archivePath.endsWith(".pzax")) {
                    ArchiveUtil.loadPzaxPackage(archivePath);
                    return;
                }
            } else {
                String extension = WORKING_APPLICATION_SETTINGS.getProperty(CNS_DEFAULT_FORMAT, "zip");
                if (ZipState.getCompressorArchives()
                            .contains(extension)) {
                    archivePath = Paths.get(STORE_TEMP.toString(),
                                            String.format("a%s.tar.%s", System.currentTimeMillis(),
                                                          extension))
                                       .toAbsolutePath()
                                       .toString();
                } else if (ZipState.supportedWriteArchives()
                                   .contains(extension)) {
                    archivePath = Paths.get(STORE_TEMP.toString(),
                                            String.format("a%s.%s", System.currentTimeMillis(),
                                                          extension))
                                       .toAbsolutePath()
                                       .toString();
                } else {
                    archivePath = Paths.get(STORE_TEMP.toString(),
                                            String.format("a%s.zip", System.currentTimeMillis()))
                                       .toAbsolutePath()
                                       .toString();
                }
                ZipState.getWriteArchiveServiceForFile(archivePath)
                        .get()
                        .createArchive(System.currentTimeMillis(), archivePath);
            }

            ArchiveReadService readService = ZipState.getReadArchiveServiceForFile(archivePath)
                                                     .get();
            ArchiveWriteService writeService = ZipState.getWriteArchiveServiceForFile(archivePath)
                                                       .orElse(null);
            fxArchiveInfo = new FXArchiveInfo(archivePath,
                                              readService, writeService);

            // Generates PreOpen dialog, if required
            Optional<ArchiveService.FXForm> optFXForm;
            if ((optFXForm = readService.getFXFormByIdentifier(ArchiveReadService.OPEN_ARCHIVE_OPTIONS,
                                                               fxArchiveInfo.getArchiveInfo())).isPresent()) {
                stage.initStyle(StageStyle.TRANSPARENT);
                stage.show();

                Stage preOpenStage = new Stage();
                Node root = optFXForm.get()
                                     .getContent();
                JFXUtil.loadPreOpenDialog(preOpenStage, root);

                Pair<AtomicBoolean,String> result = (Pair<AtomicBoolean,String>) root.getUserData();

                if (Objects.nonNull(result) && Objects.nonNull(result.getKey()) && !result.getKey()
                                                                                          .get()) {
                    // LOG: Issue occurred when opening archive %s. Issue reason: %s
                    ROOT_LOGGER.error(resolveTextKey(LOG_INVALID_ARCHIVE_SETUP, fxArchiveInfo.getArchivePath(),
                                                     result.getValue()));

                    JFXUtil.runLater(() -> stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST)));
                    return;
                }

                JFXUtil.runLater(() -> {
                    launchMainStage(new Stage(), fxArchiveInfo);
                    stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
                });
            } else {
                launchMainStage(stage, fxArchiveInfo);
            }
        } catch (Exception e) {
            ROOT_LOGGER.error(LoggingUtil.getStackTraceFromException(e));
            throw e;
        }
    }

    public static Stage genFrmAbout() throws IOException {
        FXMLLoader aboutLoader = new FXMLLoader();
        aboutLoader.setLocation(PearlZipApplication.class.getClassLoader()
                                                         .getResource("frmAbout.fxml"));
        aboutLoader.setResources(LOG_BUNDLE);
        VBox abtRoot = aboutLoader.load();
        FrmAboutController abtController = aboutLoader.getController();
        Scene abtScene = new Scene(abtRoot);
        Stage aboutStage = new Stage();
        abtController.initData(aboutStage);
        aboutStage.setScene(abtScene);
        aboutStage.initStyle(StageStyle.UNDECORATED);
        return aboutStage;
    }

    public static List<javafx.scene.control.Menu> loadMenusFromPlugins() {
        List<javafx.scene.control.Menu> customMenus = new LinkedList<>();
        Set<ArchiveService> archiveServices = new HashSet<>();
        archiveServices.addAll(ZipState.supportedWriteArchives()
                .stream()
                .map(f-> ZipState.getWriteArchiveServiceForFile(String.format(".%s",f)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList()));
        archiveServices.addAll(ZipState.supportedReadArchives()
                                       .stream()
                                       .map(f-> ZipState.getReadArchiveServiceForFile(String.format(".%s",f)))
                                       .filter(Optional::isPresent)
                                       .map(Optional::get)
                                       .collect(Collectors.toList()));

        for (ArchiveService service : archiveServices) {
            Optional<ArchiveService.FXForm> optMenu = service.getFXFormByIdentifier(ArchiveService.CUSTOM_MENUS);
            if (optMenu.isPresent() && optMenu.get().getContent() instanceof MenuBar menuBar) {
                javafx.scene.control.Menu menu = new javafx.scene.control.Menu();
                if (menuBar.getMenus().size() > 1) {
                    menu.getItems()
                        .addAll(menuBar.getMenus());
                    menu.setText(optMenu.get()
                                        .getName());
                } else if (menuBar.getMenus().size() == 1) {
                    menu.getItems()
                        .addAll(menuBar.getMenus().get(0).getItems());
                    menu.setText(menuBar.getMenus().get(0).getText());
                }
                customMenus.add(menu);
            }
        }

        customMenus.sort(Comparator.comparing(MenuItem::getText));
        return customMenus;
    }

    public abstract void createSystemMenu(Stage aboutStage, List<javafx.scene.control.Menu> customMenus) throws IOException;

    @Override
    public void stop() {
        COM_BUS_EXECUTOR_SERVICE.shutdown();
        PRIMARY_EXECUTOR_SERVICE.shutdown();
    }

    public static void main(String[] args) {
        try {
            ZipLauncher.initialize();
            LoggingConstants.ROOT_LOGGER.debug(Arrays.toString(args));
            launch(args);
        } catch(Exception e) {
            LoggingConstants.ROOT_LOGGER.error(e.getMessage());
            LoggingConstants.ROOT_LOGGER.error(LoggingUtil.getStackTraceFromException(e));
        }
    }

}
