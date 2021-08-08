/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.pub;

import com.ntak.pearlzip.archive.constants.ArchiveConstants;
import com.ntak.pearlzip.archive.pub.ArchiveInfo;
import com.ntak.pearlzip.archive.pub.ArchiveService;
import com.ntak.pearlzip.archive.pub.ArchiveWriteService;
import com.ntak.pearlzip.ui.event.handler.BtnCreateEventHandler;
import com.ntak.pearlzip.ui.model.ZipState;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.ui.constants.ResourceConstants.PATTERN_FXID_NEW_OPTIONS;

/**
 *  Controller for the New Archive dialog.
 *  @author Aashutos Kakshepati
*/
public class FrmNewController {

    @FXML
    private Button btnCreate;
    @FXML
    private Button btnCancel;

    @FXML
    private ComboBox<String> comboArchiveFormat;
    @FXML
    private TabPane tabsNew;

    private final Map<String,List<Tab>> CUSTOM_TAB_MAP = new ConcurrentHashMap<>();
    private final ArchiveInfo archiveInfo = new ArchiveInfo();

    @FXML
    public void initialize() {
        Set<String> supportedWriteFormats = new HashSet<>(ZipState.supportedWriteArchives());

        for (ArchiveService service : ZipState.getWriteProviders()) {
            supportedWriteFormats.removeAll(service.getAliasFormats());
        }

        comboArchiveFormat.setItems(FXCollections.observableArrayList(supportedWriteFormats));
        comboArchiveFormat.getSelectionModel().selectFirst();

        for (ArchiveWriteService service : ZipState.getWriteProviders()) {
            if ((service.getCreateArchiveOptionsPane()).isPresent()) {
                Pair<String,Node> tab = service.getCreateArchiveOptionsPane()
                                               .get();

                Tab customTab = new Tab();
                customTab.setText(tab.getKey());
                customTab.setId(String.format(PATTERN_FXID_NEW_OPTIONS,
                                              service.getClass()
                                                     .getCanonicalName()));
                customTab.setContent(tab.getValue());
                tab.getValue().setUserData(archiveInfo);
                tabsNew.getTabs()
                       .add(customTab);

                for (String format : service.supportedWriteFormats()) {
                    List<Tab> tabs = CUSTOM_TAB_MAP.getOrDefault(format, new LinkedList<>());
                    tabs.add(customTab);
                    CUSTOM_TAB_MAP.put(format, tabs);
                }
            }
        }
    }

    public void initData(Stage stage, AtomicBoolean isRendered) {
        btnCancel.setOnMouseClicked(e-> {
            try {
                stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
            } finally {
                isRendered.getAndSet(false);
            }
        });

        archiveInfo.setArchiveFormat(comboArchiveFormat.getSelectionModel().getSelectedItem());
        comboArchiveFormat.setOnAction((a) -> {
                                           archiveInfo.setArchiveFormat(comboArchiveFormat.getSelectionModel()
                                                                                          .getSelectedItem());
                                           setTabVisibilityByFormat(comboArchiveFormat.getSelectionModel()
                                                                                      .getSelectedItem());
                                       }
        );

        btnCreate.setOnMouseClicked((e)->{
            boolean failed = ArchiveConstants.NEW_ARCHIVE_VALIDATORS
                                             .stream()
                                             .anyMatch((v)->!v.test(archiveInfo));
            if (!failed) {
                new BtnCreateEventHandler(stage, isRendered, archiveInfo).handle(e);
            }
        });
    }

    private void setTabVisibilityByFormat(String format) {
        synchronized(tabsNew) {
            List<Tab> tabsToEnable = CUSTOM_TAB_MAP.getOrDefault(format, Collections.emptyList())
                                                   .stream()
                                                   .filter(Objects::nonNull)
                                                   .collect(Collectors.toList());
            tabsNew.getTabs()
                   .remove(1,
                           tabsNew.getTabs()
                                  .size());

            tabsNew.getTabs()
                   .addAll(tabsToEnable);
        }
    }
}
