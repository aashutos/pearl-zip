/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.pub;

import com.ntak.pearlzip.archive.util.LoggingUtil;
import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.event.handler.BtnNewEventHandler;
import com.ntak.pearlzip.ui.event.handler.BtnNewSingleFileEventHandler;
import com.ntak.pearlzip.ui.event.handler.BtnOpenEventHandler;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.model.ZipState;
import com.ntak.pearlzip.ui.util.JFXUtil;
import com.ntak.pearlzip.ui.util.internal.ArchiveUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.ntak.pearlzip.archive.constants.LoggingConstants.LOG_BUNDLE;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.JFXUtil.getActiveStage;
import static com.ntak.pearlzip.ui.util.JFXUtil.raiseAlert;
import static com.ntak.pearlzip.ui.util.internal.ArchiveUtil.addToRecentFile;
import static com.ntak.pearlzip.ui.util.internal.ArchiveUtil.genNewArchivePath;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 *  Controller for the System Menu.
 *  @author Aashutos Kakshepati
*/
public class SysMenuController {

    private static final Logger LOGGER = LoggerContext.getContext().getLogger(SysMenuController.class);

    @FXML
    private MenuItem mnuNew;
    @FXML
    private MenuItem mnuNewSingleFile;
    @FXML
    private MenuItem mnuOpen;
    @FXML
    private Menu mnuOpenRecent;
    @FXML
    private MenuItem mnuSaveAs;
    @FXML
    private MenuItem mnuClose;
    @FXML
    private MenuItem mnuOptions;

    @FXML
    private MenuItem mnuNotifications;
    @FXML
    private MenuItem mnuNewVersion;

    public void initData() {
        Path STORE_ROOT = InternalContextCache.GLOBAL_CONFIGURATION_CACHE
                .<Path>getAdditionalConfig(CK_STORE_ROOT)
                .get();

        InternalContextCache.INTERNAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_RECENT_FILES_MENU, mnuOpenRecent);
        InternalContextCache.GLOBAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_RECENT_FILE, Paths.get(STORE_ROOT.toAbsolutePath()
                                                                                                                  .toString(), "rf"));
        mnuNew.setOnAction((e)->new BtnNewEventHandler().handle(null));

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

        mnuOpen.setOnAction((e)-> {
            Stage stage = getActiveStage().orElse(new Stage());
            new BtnOpenEventHandler(stage).handle(null);
        });
        mnuSaveAs.setOnAction((e)-> {
            Stage stage = getActiveStage().orElse(new Stage());
            FXArchiveInfo fxArchiveInfo = (FXArchiveInfo) stage.getUserData();

            FileChooser saveDialog = new FileChooser();
            // TITLE: Save archive to location...
            saveDialog.setTitle(TITLE_TARGET_ARCHIVE_LOCATION);
            saveDialog.setTitle(resolveTextKey(TITLE_SAVE_ARCHIVE_PATTERN));
            saveDialog.setInitialFileName(Paths.get(fxArchiveInfo.getArchivePath()).getFileName().toString());
            File newArchive = saveDialog.showSaveDialog(stage);

            if (newArchive != null) {
                try {
                    newArchive = genNewArchivePath(newArchive.getAbsolutePath(), "", fxArchiveInfo.getArchiveInfo().getArchiveFormat());
                    Files.copy(Paths.get(fxArchiveInfo.getArchivePath()), newArchive.toPath(), REPLACE_EXISTING);
                    final String absolutePath = newArchive.getAbsolutePath();
                    FXArchiveInfo newArchiveInfo = new FXArchiveInfo(absolutePath,
                                                                     ZipState.getReadArchiveServiceForFile(absolutePath).get(),
                                                                     ZipState.getWriteArchiveServiceForFile(absolutePath).orElse(null)
                    );
                    com.ntak.pearlzip.ui.util.ArchiveUtil.launchMainStage(newArchiveInfo);
                    addToRecentFile(newArchive);

                    fxArchiveInfo.getCloseBypass().set(true);
                    stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));

                } catch (IOException ioe) {}
            }
        });
        mnuClose.setOnAction((e)-> {
            Stage stage = getActiveStage().orElse(new Stage());
            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        });

        ArchiveUtil.refreshRecentFileMenu(mnuOpenRecent);

        mnuOptions.setOnAction((e)-> JFXUtil.runLater(() -> {
        try {
            Stage newStage = new Stage();
            AnchorPane root;

            newStage.setTitle(resolveTextKey(TITLE_OPTIONS_PATTERN));
            newStage.setResizable(false);
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(ZipLauncher.class.getClassLoader()
                                                .getResource("frmOptions.fxml"));
            loader.setResources(LOG_BUNDLE);
            root = loader.load();

            Scene scene = new Scene(root);
            newStage.setScene(scene);
            FrmOptionsController controller = loader.getController();
            controller.initData(newStage);

            newStage.show();
            newStage.setAlwaysOnTop(true);
        } catch (Exception exc) {
            // LOG: Issue creating stage.\nException type: %s\nMessage:%s\nStack trace:\n%s
            LOGGER.warn(resolveTextKey(LOG_ISSUE_CREATING_STAGE, exc.getClass().getCanonicalName(),
                                       exc.getMessage(),
                                       LoggingUtil.getStackTraceFromException(exc)));
            // TITLE: ERROR: Issue creating stage
            // HEADER: There was an issue creating the required dialog
            // BODY: Upon initiating function '%s', an issue occurred on attempting to create the dialog. This
            // function will not proceed any further.
            raiseAlert(Alert.AlertType.ERROR, resolveTextKey(TITLE_ISSUE_CREATING_STAGE),
                       resolveTextKey(HEADER_ISSUE_CREATING_STAGE),
                       resolveTextKey(BODY_ISSUE_CREATING_STAGE, this.getClass().getName()), exc,
                       null);
        }}));

        mnuNotifications.setOnAction((e)-> JFXUtil.runLater(com.ntak.pearlzip.ui.util.internal.JFXUtil::showNotifications));
        mnuNewVersion.setOnAction((e)-> com.ntak.pearlzip.ui.util.internal.JFXUtil.checkNewVersionAvailable());
    }
}
