/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.pub;

import com.ntak.pearlzip.ui.util.JFXUtil;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Controller for the License Details dialog.
 *
 * @author Aashutos Kakshepati
 */
public class FrmLicenseDetailsController {

    @FXML
    private Pane pane;
    @FXML
    private WebView webLicense;
    @FXML
    private ToolBar tbBottom;
    @FXML
    private Button btnAccept;
    @FXML
    private Button btnDecline;

    private ButtonType selectedButton = ButtonType.NO;

    public void initData(Stage stage, String data, boolean withAcceptDecline) {
        webLicense.getEngine()
                  .setJavaScriptEnabled(true);
        webLicense.setOnMouseReleased(e -> JFXUtil.checkWebEngineScrollToBottom(webLicense.getEngine(),
                                                                                (b) -> btnAccept.setDisable(!b)));

        webLicense.setOnScroll(e -> JFXUtil.checkWebEngineScrollToBottom(webLicense.getEngine(),
                                                                         (b) -> btnAccept.setDisable(!b)));

        btnAccept.setOnMouseClicked(e -> {
            selectedButton = ButtonType.YES;
            stage.fireEvent(new WindowEvent(stage,
                                            WindowEvent.WINDOW_CLOSE_REQUEST));
        });

        btnDecline.setOnMouseClicked(e -> {
            selectedButton = ButtonType.NO;
            stage.fireEvent(new WindowEvent(stage,
                                            WindowEvent.WINDOW_CLOSE_REQUEST));
        });

        try {
            webLicense.getEngine()
                      .getLoadWorker()
                      .stateProperty()
                      .addListener((observable, oldState, newState) -> {
                          if (newState == Worker.State.SUCCEEDED) {
                              int innerHeight = (Integer) webLicense.getEngine()
                                                                    .executeScript("window.innerHeight");
                              int scrollHeight = (Integer) webLicense.getEngine()
                                                                     .executeScript(
                                                                             "document.documentElement.scrollHeight");

                              if (innerHeight == scrollHeight) {
                                  btnAccept.setDisable(false);
                              }
                          }
                      });

            webLicense.setContextMenuEnabled(false);
            webLicense.getEngine()
                      .loadContent(data);

            if (withAcceptDecline) {
                tbBottom.setLayoutY(pane.getPrefHeight());
                tbBottom.setVisible(true);
            }

            stage.setOnShown((e) -> stage.toFront());
        } catch(Exception e) {

        }
    }

    public ButtonType getSelectedButton() {
        return selectedButton;
    }

}
