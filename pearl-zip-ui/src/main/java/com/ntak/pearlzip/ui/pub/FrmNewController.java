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
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.ntak.pearlzip.archive.constants.ArchiveConstants.WORKING_APPLICATION_SETTINGS;
import static com.ntak.pearlzip.ui.constants.ResourceConstants.PATTERN_FXID_NEW_OPTIONS;
import static com.ntak.pearlzip.ui.constants.ZipConstants.CNS_DEFAULT_FORMAT;

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

    private final Map<Class,Tab> CLASS_TAB_MAP = new ConcurrentHashMap<>();
    private final ArchiveInfo archiveInfo = new ArchiveInfo();

    @FXML
    public void initialize() {
        Set<String> supportedWriteFormats = new HashSet<>(ZipState.supportedWriteArchives());

        for (ArchiveService service : ZipState.getWriteProviders()) {
            supportedWriteFormats.removeAll(service.getAliasFormats());
        }

        comboArchiveFormat.setItems(FXCollections.observableArrayList(supportedWriteFormats));
        String extension = WORKING_APPLICATION_SETTINGS.getProperty(CNS_DEFAULT_FORMAT, "zip");
        if (supportedWriteFormats.contains(extension)) {
            comboArchiveFormat.getSelectionModel()
                              .select(extension);
        } else {
            comboArchiveFormat.getSelectionModel()
                              .selectFirst();
        }

        for (ArchiveWriteService service : ZipState.getWriteProviders()) {
            if (service.getFXFormByIdentifier(ArchiveWriteService.CREATE_OPTIONS).isPresent()) {
                ArchiveService.FXForm tab = service.getFXFormByIdentifier(ArchiveWriteService.CREATE_OPTIONS)
                                                   .get();

                Tab customTab = new Tab();
                customTab.setText(tab.getName());
                customTab.setId(String.format(PATTERN_FXID_NEW_OPTIONS,
                                              service.getClass()
                                                     .getCanonicalName()));
                customTab.setContent(tab.getContent());
                tab.getContent().setUserData(archiveInfo);
                tabsNew.getTabs()
                       .add(customTab);

                CLASS_TAB_MAP.put(service.getClass(), customTab);
            }
        }

        setTabVisibilityByFormat(comboArchiveFormat.getSelectionModel()
                                                   .getSelectedItem());
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
            tabsNew.getTabs()
                   .remove(1,
                           tabsNew.getTabs()
                                  .size());

            final Tab tab = CLASS_TAB_MAP.get(ZipState.getWriteArchiveServiceForFile(String.format(".%s", format))
                                                    .get()
                                                    .getClass());
            if (Objects.nonNull(tab)) {
                tabsNew.getTabs()
                       .add(tab);
            }
        }
    }
}
