/*
 * Copyright © 2022 92AK
 */

package com.ntak.pearlzip.ui.pub;

import com.ntak.pearlzip.archive.model.PluginInfo;
import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.util.ModuleUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.JFXUtil.raiseAlert;

public class FrmPurgePluginController {
    @FXML
    private TableView<String> tblManifests;

    @FXML
    private TableColumn<String, String> colName;

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnPurgeSelected;

    @FXML
    public void initialize() {
        Map<String,PluginInfo> PLUGINS_METADATA = InternalContextCache.INTERNAL_CONFIGURATION_CACHE
                                                                      .<Map<String,PluginInfo>>getAdditionalConfig(CK_PLUGINS_METADATA)
                                                                      .get();

        synchronized(PLUGINS_METADATA) {
            tblManifests.setItems(FXCollections.observableArrayList(PLUGINS_METADATA.keySet()));
            tblManifests.refresh();
            tblManifests.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            colName.setCellValueFactory(s -> new SimpleStringProperty(s.getValue()));
        }

        btnCancel.setOnAction((e)->btnCancel.getScene()
                                       .getWindow()
                                       .fireEvent(new WindowEvent(btnCancel.getScene().getWindow(),
                                                                  WindowEvent.WINDOW_CLOSE_REQUEST))
        );

        btnPurgeSelected.setOnAction((e)-> {
            Optional<ButtonType> response = Optional.empty();
            try {
                List<String> selectedManifests = tblManifests.getSelectionModel().getSelectedItems();
                // TITLE: Confirmation: Purge plugin(s)
                // BODY: Do you wish to purge the following plugin(s): %s?
                response = raiseAlert(Alert.AlertType.CONFIRMATION,
                                      resolveTextKey(TITLE_CONFIRM_PURGE_SELECTED),
                                      null,
                                      resolveTextKey(BODY_CONFIRM_PURGE_SELECTED, String.join(",", selectedManifests)),
                                      null,
                                      btnPurgeSelected.getScene().getWindow(),
                                      ButtonType.YES,
                                      ButtonType.NO);
                if (response.isPresent() && response.get().equals(ButtonType.YES)) {
                    final String moduleDirectory = Path.of(InternalContextCache.GLOBAL_CONFIGURATION_CACHE
                                                                               .<Path>getAdditionalConfig(CK_STORE_ROOT)
                                                                               .get()
                                                                               .toAbsolutePath()
                                                                               .toString(),
                                                           "providers")
                                                       .toString();
                    ModuleUtil.purgeLibraries(moduleDirectory, new HashSet<>(selectedManifests));
                }
            } catch(IOException ioException) {
            } finally {
                synchronized(PLUGINS_METADATA) {
                    tblManifests.setItems(FXCollections.observableArrayList(PLUGINS_METADATA.keySet()));
                    tblManifests.refresh();
                }

                if (response.isPresent() && response.get()
                                                    .equals(ButtonType.YES)) {
                    // TITLE: Purge Complete
                    // BODY: Purge has completed. The effects of purge will come into effect after restart.
                    raiseAlert(Alert.AlertType.INFORMATION,
                               resolveTextKey(TITLE_PURGE_COMPLETE),
                               null,
                               resolveTextKey(BODY_PURGE_COMPLETE),
                               null,
                               btnPurgeSelected.getScene().getWindow()
                    );
                }
            }
        });
    }

}
