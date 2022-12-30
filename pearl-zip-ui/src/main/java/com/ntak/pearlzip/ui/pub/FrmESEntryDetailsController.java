/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.pub;

import com.ntak.pearlzip.ui.util.ExtensionStoreEntry;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.Objects;

public class FrmESEntryDetailsController {

    @FXML
    private Label lblPkgNameValue;

    @FXML
    private Label lblPkgUrlValue;

    @FXML
    private Label lblPkgHashValue;

    @FXML
    private Label lblDescription;

    @FXML
    private Label lblMinVersionValue;

    @FXML
    private Label lblMaxVersionValue;

    @FXML
    private Label lblProviderNameValue;

    @FXML
    private Label lblAboutValue;

    @FXML
    private Button btnClose;

    public void initData(ExtensionStoreEntry entry, Stage stage) {
        if (Objects.nonNull(entry)) {
            lblPkgNameValue.setText(entry.packageName());
            lblPkgUrlValue.setText(entry.packageURL());
            lblPkgHashValue.setText(entry.packageHash());
            lblDescription.setText(entry.description());
            lblMinVersionValue.setText(entry.minVersion());
            lblMaxVersionValue.setText(entry.maxVersion());
            lblProviderNameValue.setText(entry.providerName());
            lblAboutValue.setText(entry.providerDescription());
        }

        btnClose.setOnAction(e-> stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST)));
    }
}
