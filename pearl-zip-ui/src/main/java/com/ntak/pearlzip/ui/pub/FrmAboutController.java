/*
 * Copyright © 2024 92AK
 */
package com.ntak.pearlzip.ui.pub;

import com.ntak.pearlzip.archive.util.LoggingUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.time.LocalDate;

import static com.ntak.pearlzip.archive.constants.LoggingConstants.LOG_BUNDLE;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ResourceConstants.DTF_YYYY;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.JFXUtil.raiseAlert;

/**
 *  Controller for the About dialog.
 *  @author Aashutos Kakshepati
*/
public class FrmAboutController {

    private static Logger LOGGER = LoggerContext.getContext().getLogger(FrmAboutController.class);

    @FXML
    private VBox vbAbout;

    @FXML
    private Label lblAppName;
    @FXML
    private Label lblVersion;
    @FXML
    private Label lblCopyright;
    @FXML
    private Label lblWeblink;

    @FXML
    private Button btnLicenseInfo;

    @FXML
    public void initialize() {
        lblAppName.setText(System.getProperty(CNS_NTAK_PEARL_ZIP_APP_NAME, "PearlZip"));
        lblVersion.setText(System.getProperty(CNS_NTAK_PEARL_ZIP_VERSION, "0.0.0.0"));
        lblCopyright.setText(String.format(System.getProperty(CNS_NTAK_PEARL_ZIP_COPYRIGHT, "© %s 92AK\nProgram written by Aashutos Kakshepati"),
                                                              LocalDate.now().format(
                                                                      DTF_YYYY)));
        lblWeblink.setText(System.getProperty(CNS_NTAK_PEARL_ZIP_WEBLINK, "https://pearlzip.92ak.co.uk"));
    }

    public void initData(Stage stage) {
        vbAbout.setOnMouseClicked(e->stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST)));
        btnLicenseInfo.setOnMouseClicked((e)->{
            if (e.getButton() == MouseButton.PRIMARY) {
                try {
                    Stage licStage = new Stage();

                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(ZipLauncher.class.getClassLoader()
                                                        .getResource("frmLicenseOverview.fxml"));
                    loader.setResources(LOG_BUNDLE);
                    AnchorPane root = loader.load();

                    Scene scene = new Scene(root);
                    licStage.setScene(scene);
                    FrmLicenseOverviewController controller = loader.getController();
                    controller.initData(stage, licStage);
                    licStage.setResizable(false);

                    stage.setAlwaysOnTop(false);
                    licStage.initStyle(StageStyle.UNDECORATED);
                    licStage.initModality(Modality.APPLICATION_MODAL);
                    licStage.showAndWait();
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
                }
            }
        });
        stage.setAlwaysOnTop(true);
    }
}
