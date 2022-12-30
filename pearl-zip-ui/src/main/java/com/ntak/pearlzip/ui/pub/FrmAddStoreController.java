/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.pub;

import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.util.StoreRepoDetails;
import com.ntak.pearlzip.ui.util.internal.JFXUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.JFXUtil.raiseAlert;

public class FrmAddStoreController {

    private static final Logger LOGGER = LoggerContext.getContext().getLogger(FrmAddStoreController.class);

    @FXML
    private TextField txtBoxName;
    @FXML
    private TextField txtBoxURL;
    @FXML
    private TextField txtBoxUsername;
    @FXML
    private TextField txtBoxPassword;

    @FXML
    private Button btnAdd;
    @FXML
    private Button btnCancel;

    @FXML
    public void initialize() {
    }

    public void initData(TableView tblStore, Stage stage) {
        btnCancel.setOnAction(e-> stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST)));
        btnAdd.setOnAction((e)-> {
           // Create Repository Entry Object and add to Map
            StoreRepoDetails storeRepoDetails = new StoreRepoDetails(txtBoxName.getText(), txtBoxURL.getText(), txtBoxUsername.getText(), txtBoxPassword.getText());

           // Validate fields
           // 1. URL Connection validation - Create connection and run isValid() to check connectivity
           try (Connection connection = DriverManager.getConnection(storeRepoDetails.url(), storeRepoDetails.username(), storeRepoDetails.password())) {
               if (!connection.isValid(Integer.parseInt(System.getProperty(CNS_DB_CONNECTION_TIMEOUT, "300")))) {
                   throw new SQLException(resolveTextKey(LOG_ISSUE_CONNECTING_DB, storeRepoDetails.url()));
               }
           } catch(SQLException se) {
               // Raise alert
               // LOG: Issue connection to db: '%s'.
               // TITLE: ERROR: DB Connection test failed
               // BODY: Issue connecting to DB host. Please check the defined host.
               LOGGER.error(se.getMessage());
               raiseAlert(Alert.AlertType.ERROR, resolveTextKey(TITLE_ISSUE_CONNECTING_DB), null, resolveTextKey(BODY_ISSUE_CONNECTING_DB), btnAdd.getScene().getWindow());
               return;
           } finally {
               stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
           }

            // Persist down to repository cache and add to cache
            InternalContextCache.INTERNAL_CONFIGURATION_CACHE.<Map<String,StoreRepoDetails>>getAdditionalConfig(CK_STORE_REPO).get()
                                                             .put(storeRepoDetails.name(), storeRepoDetails);
            tblStore.setItems(FXCollections.observableArrayList(InternalContextCache.INTERNAL_CONFIGURATION_CACHE.<Map<String,StoreRepoDetails>>getAdditionalConfig(CK_STORE_REPO).map(Map::values).orElse(Collections.emptyList())));

            Path repoFile = InternalContextCache.GLOBAL_CONFIGURATION_CACHE
                    .<Path>getAdditionalConfig(CK_REPO_ROOT)
                    .get().resolve(storeRepoDetails.name());
            JFXUtil.persistStoreRepoDetails(storeRepoDetails, repoFile);

            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        });
    }
}
