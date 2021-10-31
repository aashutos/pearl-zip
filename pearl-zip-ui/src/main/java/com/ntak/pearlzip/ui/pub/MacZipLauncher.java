/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.pub;

import com.ntak.pearlzip.archive.constants.LoggingConstants;
import com.ntak.pearlzip.archive.pub.ArchiveReadService;
import com.ntak.pearlzip.archive.pub.ArchiveService;
import com.ntak.pearlzip.archive.pub.ArchiveWriteService;
import com.ntak.pearlzip.archive.util.LoggingUtil;
import com.ntak.pearlzip.ui.constants.ResourceConstants;
import com.ntak.pearlzip.ui.constants.ZipConstants;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.model.ZipState;
import com.ntak.pearlzip.ui.util.ErrorAlertConsumer;
import com.ntak.pearlzip.ui.util.JFXUtil;
import com.ntak.pearlzip.ui.util.ProgressMessageTraceLogger;
import de.jangassen.model.AppearanceMode;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
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
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.ntak.pearlzip.archive.constants.ArchiveConstants.COM_BUS_EXECUTOR_SERVICE;
import static com.ntak.pearlzip.archive.constants.ArchiveConstants.WORKING_APPLICATION_SETTINGS;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.LOG_BUNDLE;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.ROOT_LOGGER;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.pub.ZipLauncher.OS_FILES;
import static com.ntak.pearlzip.ui.util.ArchiveUtil.addToRecentFile;
import static com.ntak.pearlzip.ui.util.ArchiveUtil.launchMainStage;

/**
 * Loads the main UI screen for the Zip Application.
 *
 * @author Aashutos Kakshepati
 */
public class MacZipLauncher extends Application {

    static {
        Desktop.getDesktop()
               .setOpenFileHandler((e) -> e.getFiles()
                                           .stream()
                                           .peek(LoggingConstants.ROOT_LOGGER::info)
                                           .map(f -> f.toPath()
                                                      .toAbsolutePath()
                                                      .toString())
                                           .forEach(f -> JFXUtil.runLater(() -> {
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
                                               Optional<Node> optNode;
                                               if ((optNode = readService.getOpenArchiveOptionsPane(fxArchiveInfo.getArchiveInfo())).isPresent()) {
                                                   Stage preOpenStage = new Stage();
                                                   Node root = optNode.get();
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
        APP = this;

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
        ZipConstants.STORE_TEMP = Paths.get(STORE_ROOT.toAbsolutePath()
                                                      .toString(), "temp");
        if (!Files.exists(STORE_TEMP)) {
            Files.createDirectories(STORE_TEMP);
        }

        // Providers
        Path providerPath = Paths.get(STORE_ROOT.toAbsolutePath()
                                                .toString(), "providers");
        Files.createDirectories(providerPath);

        // Recent files
        RECENT_FILE = Paths.get(STORE_ROOT.toAbsolutePath()
                                          .toString(), "rf");
        if (!Files.exists(RECENT_FILE)) {
            Files.createFile(RECENT_FILE);
        }

        ////////////////////////////////////////////
        ///// Create System Menu //////////////////
        //////////////////////////////////////////

        // Create a new System Menu
        String appName = System.getProperty(CNS_NTAK_PEARL_ZIP_APP_NAME, "PearlZip");
        MenuBar sysMenu = new MenuBar();

        // Setting about form...
        FXMLLoader aboutLoader = new FXMLLoader();
        aboutLoader.setLocation(MacZipLauncher.class.getClassLoader()
                                                    .getResource("frmAbout.fxml"));
        aboutLoader.setResources(LOG_BUNDLE);
        VBox abtRoot = aboutLoader.load();
        FrmAboutController abtController = aboutLoader.getController();
        Scene abtScene = new Scene(abtRoot);
        Stage aboutStage = new Stage();
        abtController.initData(aboutStage);
        aboutStage.setScene(abtScene);
        aboutStage.initStyle(StageStyle.UNDECORATED);

        sysMenu.setUseSystemMenuBar(true);
        sysMenu.getMenus()
               .add(MENU_TOOLKIT.createDefaultApplicationMenu(appName, aboutStage));

        // Add some more Menus...
        FXMLLoader menuLoader = new FXMLLoader();
        menuLoader.setLocation(MacZipLauncher.class.getClassLoader()
                                                   .getResource("sysmenu.fxml"));
        menuLoader.setResources(LOG_BUNDLE);
        MenuBar additionalMenu = menuLoader.load();
        SysMenuController menuController = menuLoader.getController();
        menuController.initData();
        sysMenu.getMenus()
               .addAll(additionalMenu.getMenus());
        ResourceConstants.WINDOW_MENU =
                sysMenu.getMenus()
                       .stream()
                       .filter(m -> m.getText()
                                     .equals(LoggingUtil.resolveTextKey(CNS_SYSMENU_WINDOW_TEXT)))
                       .findFirst()
                       .get();

        // Use the menu sysMenu for all stages including new ones
        MENU_TOOLKIT.setAppearanceMode(AppearanceMode.DARK);
        MENU_TOOLKIT.setMenuBar(sysMenu);

        // Initialise archive information
        readyLatch.countDown();
        FXArchiveInfo fxArchiveInfo;
        String archivePath;
        if (APP.getParameters()
               .getRaw()
               .size() > 0 && Files.exists(Paths.get(APP.getParameters()
                                                        .getRaw()
                                                        .get(0)))) {
            archivePath = APP.getParameters()
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
        Optional<Node> optNode;
        if ((optNode = readService.getOpenArchiveOptionsPane(fxArchiveInfo.getArchiveInfo())).isPresent()) {
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.show();

            Stage preOpenStage = new Stage();
            Node root = optNode.get();
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
    }

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
