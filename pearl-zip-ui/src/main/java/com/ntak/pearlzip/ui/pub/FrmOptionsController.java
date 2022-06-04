/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.pub;

import com.ntak.pearlzip.archive.constants.ConfigurationConstants;
import com.ntak.pearlzip.archive.pub.ArchiveReadService;
import com.ntak.pearlzip.archive.pub.ArchiveService;
import com.ntak.pearlzip.archive.pub.ArchiveWriteService;
import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.model.ZipState;
import com.ntak.pearlzip.ui.util.ArchiveUtil;
import com.ntak.pearlzip.ui.util.ClearCacheRunnable;
import com.ntak.pearlzip.ui.util.JFXUtil;
import com.ntak.pearlzip.ui.util.ModuleUtil;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Pair;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.archive.constants.ArchiveConstants.*;
import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.CNS_CUSTOM_RES_BUNDLE;
import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.CNS_RES_BUNDLE;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.CUSTOM_BUNDLE;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.LOG_BUNDLE;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ResourceConstants.PATTERN_FXID_OPTIONS;
import static com.ntak.pearlzip.ui.constants.ResourceConstants.PATTERN_TEXTFIELD_TABLE_CELL_STYLE;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.mac.MacPearlZipApplication.genFrmAbout;
import static com.ntak.pearlzip.ui.mac.MacPearlZipApplication.loadMenusFromPlugins;
import static com.ntak.pearlzip.ui.util.ArchiveUtil.initialiseApplicationSettings;
import static com.ntak.pearlzip.ui.util.JFXUtil.*;
import static com.ntak.pearlzip.ui.util.ModuleUtil.loadModuleFromExtensionPackage;

/**
 * Controller for the Options dialog.
 *
 * @author Aashutos Kakshepati
 */
public class FrmOptionsController {
    private static final Logger LOGGER = LoggerContext.getContext()
                                                      .getLogger(FrmOptionsController.class);

    ///// General /////
    @FXML
    private TabPane tabPaneOptions;

    @FXML
    private Button btnClearCache;

    @FXML
    private ComboBox<String> comboDefaultFormat;

    @FXML
    private CheckBox checkShowTargetFolderExtractAll;

    @FXML
    private CheckBox checkShowTargetFolderExtractSelected;

    @FXML
    private CheckBox checkShowNotification;

    @FXML
    private CheckBox checkSafeMode;

    ///// Bootstrap Properties /////
    @FXML
    private TableView<Pair<String,String>> tblBootstrap;
    @FXML
    private TableColumn<Pair<String,String>,String> key;
    @FXML
    private TableColumn<Pair<String,String>,String> value;

    ///// Provider Properties /////
    @FXML
    private TableView<Pair<Boolean,ArchiveService>> tblProviders;
    @FXML
    private TableColumn<Pair<Boolean,ArchiveService>,String> name;
    @FXML
    private TableColumn<Pair<Boolean,ArchiveService>,Pair<Boolean,ArchiveService>> readCapability;
    @FXML
    private TableColumn<Pair<Boolean,ArchiveService>,Pair<Boolean,ArchiveService>> writeCapability;
    @FXML
    private TableColumn<Pair<Boolean,ArchiveService>,String> supportedFormat;
    @FXML
    private TableColumn<Pair<Boolean,ArchiveService>,Pair<String,Number>> priority;
    @FXML
    private Button btnPurgePlugin;
    @FXML
    private Button btnPurgeAll;

    ///// Load Plugin Properties /////
    @FXML
    private Pane paneDropArea;

    @FXML
    private Button btnOk;
    @FXML
    private Button btnApply;
    @FXML
    private Button btnCancel;

    ///// Theme Properties /////
    @FXML
    private Tab tabTheme;
    @FXML
    private TableView<String> tblTheme;
    @FXML
    private TableColumn<String,String> colTheme;
    @FXML
    private Button btnSetTheme;

    ///// Language Pack Properties /////
    @FXML
    private Tab tabLangs;
    @FXML
    private TableView<Pair<String,Locale>> tblLang;
    @FXML
    private TableColumn<Pair<String,Locale>,String> colLang;
    @FXML
    private Button btnSetLang;

    @FXML
    public void initialize() {
        initialiseApplicationSettings();

        ///// Bootstrap Properties /////
        key.setCellValueFactory(new PropertyValueFactory<>("Key"));
        value.setCellValueFactory(new PropertyValueFactory<>("Value"));
        tblBootstrap.setItems(FXCollections.observableArrayList(System.getProperties()
                                                                      .entrySet()
                                                                      .stream()
                                                                      .map(e -> new Pair<>((String) e.getKey(),
                                                                                           (String) e.getValue()))
                                                                      .collect(Collectors.toList()))
        );

        // COMBO BOX - DEFAULT FORMAT
        Set<String> aliasFormats = ZipState.getRawSupportedCompressorWriteFormats()
                .stream()
                .filter(f -> !ZipState.getSupportedCompressorWriteFormats().contains(f))
                        .collect(Collectors.toSet());
        comboDefaultFormat.setItems(FXCollections.observableArrayList(ZipState.supportedWriteArchives()
                                                                              .stream()
                                                                              .filter(f->!aliasFormats.contains(f))
                                                                              .collect(Collectors.toSet()))
        );
        if (ZipState.supportedWriteArchives()
                    .stream()
                    .anyMatch(f -> f.equals(WORKING_APPLICATION_SETTINGS.getProperty(CNS_DEFAULT_FORMAT)))
        ) {
            comboDefaultFormat.getSelectionModel()
                              .select(
                                      WORKING_APPLICATION_SETTINGS.getProperty(
                                              CNS_DEFAULT_FORMAT,
                                              "zip")
                              );
        }

        comboDefaultFormat.setOnAction(e -> {
            synchronized(WORKING_APPLICATION_SETTINGS) {
                WORKING_APPLICATION_SETTINGS.put(CNS_DEFAULT_FORMAT,
                                                 comboDefaultFormat.getValue());
            }


        });

        // CHECK BOX - Show target folder on extract selected
        try {
            checkShowTargetFolderExtractSelected.setSelected(Boolean.parseBoolean(CURRENT_SETTINGS.getProperty(CNS_SHOW_TARGET_FOLDER_EXTRACT_SELECTED,
                                                                                                "true")));
        } catch (Exception e) {
            checkShowTargetFolderExtractSelected.setSelected(true);
        }

        checkShowTargetFolderExtractSelected.setOnAction(e -> {
            synchronized(WORKING_SETTINGS) {
                WORKING_SETTINGS.put(CNS_SHOW_TARGET_FOLDER_EXTRACT_SELECTED,
                                     checkShowTargetFolderExtractSelected.isSelected()?"true":"false");
            }
        });

        // CHECK BOX - Show target folder on extract all
        try {
            checkShowTargetFolderExtractAll.setSelected(Boolean.parseBoolean(CURRENT_SETTINGS.getProperty(CNS_SHOW_TARGET_FOLDER_EXTRACT_ALL,
                                                                                                "true")));
        } catch (Exception e) {
            checkShowTargetFolderExtractAll.setSelected(true);
        }

        checkShowTargetFolderExtractAll.setOnAction(e -> {
            synchronized(WORKING_SETTINGS) {
                WORKING_SETTINGS.put(CNS_SHOW_TARGET_FOLDER_EXTRACT_ALL,
                                     checkShowTargetFolderExtractAll.isSelected()?"true":"false");
            }
        });

        // CHECK BOX - SHOW NOTIFICATION
        try {
            checkShowNotification.setSelected(Boolean.parseBoolean(CURRENT_SETTINGS.getProperty(CNS_SHOW_NOTIFICATION,
                                                                                                "true")));
        } catch (Exception e) {
            checkShowNotification.setSelected(true);
        }
        checkShowNotification.setOnAction(e -> {
            synchronized(WORKING_SETTINGS) {
                WORKING_SETTINGS.put(CNS_SHOW_NOTIFICATION,
                                                 checkShowNotification.isSelected()?"true":"false");
            }
        });

        // CHECK BOX - SAFE MODE
        try {
            checkSafeMode.setSelected(Boolean.parseBoolean(System.getProperty(CNS_NTAK_PEARL_ZIP_SAFE_MODE,
                                                                                                "true")));
        } catch (Exception e) {
            checkSafeMode.setSelected(true);
        }
        checkSafeMode.setOnAction(e -> {
            synchronized(WORKING_SETTINGS) {
                WORKING_APPLICATION_SETTINGS.put(CNS_NTAK_PEARL_ZIP_SAFE_MODE,
                                                 checkSafeMode.isSelected()?"true":"false");
            }
        });
        
        ///// Provider Properties /////
        name.setCellValueFactory((s) -> new SimpleStringProperty(s.getValue()
                                                                  .getValue()
                                                                  .getClass()
                                                                  .getCanonicalName()));
        readCapability.setCellValueFactory((s) -> new SimpleObjectProperty<>(s.getValue()));
        readCapability.setCellFactory((c) -> new TableCell<>() {
            @Override
            public void updateItem(Pair<Boolean,ArchiveService> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    CheckBox checkBox = new CheckBox();
                    checkBox.setSelected(!item.getKey());
                    checkBox.setDisable(true);
                    this.setGraphic(checkBox);
                }
            }
        });
        writeCapability.setCellValueFactory((s)-> new SimpleObjectProperty<>(s.getValue()));
        writeCapability.setCellFactory((c)-> new TableCell<>() {
            @Override
            public void updateItem(Pair<Boolean,ArchiveService> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    CheckBox checkBox = new CheckBox();
                    checkBox.setSelected(item.getKey());
                    checkBox.setDisable(true);
                    this.setGraphic(checkBox);
                }
            }
        });
        supportedFormat.setCellValueFactory((s)-> {
            try {
                Pair pair = s.getValue();
                if (Objects.nonNull(pair)) {
                    if (pair.getValue() instanceof ArchiveWriteService) {
                        return new SimpleStringProperty(((ArchiveWriteService) pair.getValue()).supportedWriteFormats()
                                                                                            .toString()
                                                                                            .replaceAll("(\\[|\\])",
                                                                                                        ""));
                    }

                    if (pair.getValue() instanceof ArchiveReadService) {
                        return new SimpleStringProperty(((ArchiveReadService) pair.getValue()).supportedReadFormats()
                                                                                              .toString()
                                                                                              .replaceAll("(\\[|\\])",
                                                                                                          ""));
                    }
                }
            } catch(Exception e) {
            }

            return new SimpleStringProperty("N/A");
        });

        priority.setCellFactory((c) -> new TableCell<>() {
            @Override
            public void updateItem(Pair<String,Number> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    TextField textField = new TextField();
                    textField.setText(String.valueOf(item.getValue()));
                    String color = this.getTableRow().isSelected() ? "white" : "black";
                    textField.setStyle(String.format(PATTERN_TEXTFIELD_TABLE_CELL_STYLE, color));

                    textField.setOnKeyTyped(e -> {
                        char charEntered = e.getCode()
                                            .getChar()
                                            .charAt(0);
                        int position = textField.getCaretPosition();
                        if (!Character.isDigit(charEntered)) {
                            textField.setText(textField.getText()
                                                       .replaceAll("[^0-9]", ""));
                            textField.positionCaret(Math.max(Math.min(position, textField.getLength()), 0));
                        }
                        String key = String.format(CNS_PROVIDER_PRIORITY_ROOT_KEY, item.getKey());

                        synchronized(WORKING_APPLICATION_SETTINGS) {
                            WORKING_APPLICATION_SETTINGS.put(key, textField.getText());
                        }
                    });
                    this.setGraphic(textField);
                }
            }
        });

        priority.setCellValueFactory((s) -> {
            String priorityStr = System.getProperty(String.format(CNS_PROVIDER_PRIORITY_ROOT_KEY,
                                                                  s.getValue()
                                                                   .getValue()
                                                                   .getClass()
                                                                   .getCanonicalName()));

            int priority;
            try {
                priority = Integer.parseInt(priorityStr);
            } catch(Exception e) {
                priority = 0;
            }

            return new SimpleObjectProperty<Pair<String,Number>>(new Pair(s.getValue()
                                                                           .getValue()
                                                                           .getClass()
                                                                           .getCanonicalName(), priority));
        });
    }

    @FXML
    public void initData(Stage stage) {
        ///// Load settings file /////
        synchronized(CURRENT_SETTINGS) {
            try(InputStream settingsIStream = Files.newInputStream(SETTINGS_FILE)) {
                CURRENT_SETTINGS.load(settingsIStream);
                WORKING_SETTINGS.clear();
                WORKING_SETTINGS.putAll(CURRENT_SETTINGS);
            } catch(IOException e) {
            }
        }

        ///// Initialize plugin options /////
        List<Pair<Boolean,ArchiveService>> services = new ArrayList<>();
        services.addAll(ZipState.getWriteProviders()
                                .stream()
                                .map(s -> new Pair<Boolean,ArchiveService>(true, s))
                                .collect(Collectors.toList()));
        services.addAll(ZipState.getReadProviders()
                                .stream()
                                .map(s -> new Pair<Boolean,ArchiveService>(false, s))
                                .collect(Collectors.toList()));
        tblProviders.setItems(FXCollections.observableArrayList(services));
        tblProviders.setOnMouseClicked(e->{
            final int focusedIndex = tblProviders.getSelectionModel()
                                                     .getFocusedIndex();
            for (int i = 0; i < tblProviders.getItems().size(); i++) {
                final int curIdx = i;
                JFXUtil.getTableCellForColumnRow(tblProviders, i, "Priority").ifPresent(
                        (tabCell) -> {
                            String colour = curIdx == focusedIndex ? "white":"black";
                            tabCell.getGraphic().setStyle(String.format(PATTERN_TEXTFIELD_TABLE_CELL_STYLE,
                                                                        colour));
                        }
                );
            }
        });

        // Purge plugin functionality
        btnPurgePlugin.setOnAction((e) -> {
            try {
                Stage stgPurgePlugin = new Stage();

                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(ArchiveUtil.class.getClassLoader()
                                                    .getResource("frmPurgePlugin.fxml"));
                loader.setResources(LOG_BUNDLE);
                AnchorPane pane = loader.load();
                final Scene scene = new Scene(pane);
                stgPurgePlugin.setScene(scene);
                stgPurgePlugin.setTitle(resolveTextKey(TITLE_SELECT_PLUGINS_PURGE));

                stgPurgePlugin.initOwner(stage);
                stgPurgePlugin.initModality(Modality.WINDOW_MODAL);
                
                stgPurgePlugin.setResizable(false);
                stgPurgePlugin.setAlwaysOnTop(true);
                stgPurgePlugin.showAndWait();
            } catch (Exception exc) {

            }

        });

        btnPurgeAll.setOnAction((e) -> {
            Optional<ButtonType> response = Optional.empty();
            try {
                // TITLE: Confirmation: Purge all plugins
                // BODY: Do you wish to purge all plugins?
                response = raiseAlert(Alert.AlertType.CONFIRMATION,
                           resolveTextKey(TITLE_CONFIRM_PURGE_ALL),
                           null,
                           resolveTextKey(BODY_CONFIRM_PURGE_ALL),
                           null,
                           stage,
                           ButtonType.YES,
                           ButtonType.NO);
                if (response.isPresent() && response.get().equals(ButtonType.YES)) {
                    ModuleUtil.purgeAllLibraries();
                }
            } catch(IOException ex) {
            } finally {
                if (response.isPresent() && response.get().equals(ButtonType.YES)) {
                    // TITLE: Purge Complete
                    // BODY: Purge has completed. The effects of purge will come into effect after restart.
                    raiseAlert(Alert.AlertType.INFORMATION,
                               resolveTextKey(TITLE_PURGE_COMPLETE),
                               null,
                               resolveTextKey(BODY_PURGE_COMPLETE),
                               null,
                               stage
                    );
                }
            }
        });

        // Plugin option loading...
        for (ArchiveService service : services.stream()
                                              .map(Pair::getValue)
                                              .collect(Collectors.toList())) {

            if (service.getFXFormByIdentifier(ArchiveService.OPTIONS).isPresent()) {
                ArchiveService.FXForm tab = service.getFXFormByIdentifier(ArchiveService.OPTIONS)
                                                   .get();

                Tab customTab = new Tab();
                customTab.setText(tab.getName());
                customTab.setId(String.format(PATTERN_FXID_OPTIONS,
                                              service.getClass()
                                                     .getCanonicalName()));
                customTab.setContent(tab.getContent());
                tabPaneOptions.getTabs()
                              .add(customTab);
            }
        }

        btnClearCache.setOnAction((e) -> {
            Lock writeLock = LCK_CLEAR_CACHE.writeLock();
            if (!writeLock.tryLock()) {
                raiseAlert(Alert.AlertType.WARNING, resolveTextKey(TITLE_CLEAR_CACHE_BLOCKED), "",
                           resolveTextKey(BODY_CLEAR_CACHE_BLOCKED), stage);
                return;
            }

            try {
                btnClearCache.setDisable(true);
                long sessionId = System.currentTimeMillis();
                // TITLE: Confirmation: Clear Cache
                // HEADER: Do you wish to clear the PearlZip cache?
                // BODY: Press 'Yes' to clear the cache otherwise press 'No'.
                Optional<ButtonType> response = raiseAlert(Alert.AlertType.CONFIRMATION,
                                                           resolveTextKey(TITLE_CLEAR_CACHE),
                                                           resolveTextKey(HEADER_CLEAR_CACHE),
                                                           resolveTextKey(BODY_CLEAR_CACHE),
                                                           null, stage,
                                                           ButtonType.YES, ButtonType.NO);

                if (response.isPresent() && response.get()
                                                    .getButtonData()
                                                    .equals(ButtonBar.ButtonData.YES)) {
                    executeBackgroundProcess(sessionId, stage, new ClearCacheRunnable(sessionId, false),
                                             LOGGER::error,
                                             (s) -> {});
                }
            } finally {
                LCK_CLEAR_CACHE.writeLock().unlock();
                btnClearCache.setDisable(false);
            }
        });

        // Theme related functionality
        final Path themesPath = Paths.get(STORE_ROOT.toAbsolutePath()
                                              .toString(), "themes");
        tabTheme.setOnSelectionChanged( (ev) -> {
             try {
                 tblTheme.setItems(FXCollections.observableArrayList(Files.list(themesPath)
                                                                          .filter(Files::isDirectory)
                                                                          .map(p -> p.getFileName()
                                                                                     .toString())
                                                                          .collect(Collectors.toList()))
                 );
                 tblTheme.refresh();
             } catch(Exception e) {

             }
        });

        colTheme.setCellValueFactory((t) -> new SimpleStringProperty(t.getValue()));

        btnSetTheme.setOnAction((e)-> {
            String name = tblTheme.getSelectionModel().getSelectedItem();
            initialiseTheme(themesPath, name);
            WORKING_APPLICATION_SETTINGS.setProperty(CNS_THEME_NAME, name);
        });

        // Language Pack related functionality...
        tabLangs.setOnSelectionChanged( (ev) -> {
            try {
                tblLang.setItems(FXCollections.observableArrayList(LANG_PACKS));
                tblTheme.refresh();
            } catch(Exception e) {

            }
        });
        tblLang.setItems(FXCollections.observableArrayList(LANG_PACKS));
        colLang.setCellValueFactory((p) -> new SimpleStringProperty(p.getValue().getKey()));
        btnSetLang.setOnAction((e) -> {
            Locale locale = tblLang.getSelectionModel().getSelectedItem().getValue();
            WORKING_APPLICATION_SETTINGS.setProperty(ConfigurationConstants.CNS_LOCALE_LANG, locale.getLanguage());
            WORKING_APPLICATION_SETTINGS.setProperty(ConfigurationConstants.CNS_LOCALE_COUNTRY, locale.getCountry());
            WORKING_APPLICATION_SETTINGS.setProperty(ConfigurationConstants.CNS_LOCALE_VARIANT, locale.getVariant());

            // TITLE: Information: Changing Language Pack
            // HEADER: Language pack will be changed after restart
            // BODY: The selected language for PearlZip has been updated to %s. This will come into effect the next
            // time you start up PearlZip.
            raiseAlert(Alert.AlertType.INFORMATION,
                       resolveTextKey(TITLE_CHANGE_LANG_PACK),
                       resolveTextKey(HEADER_CHANGE_LANG_PACK),
                       resolveTextKey(BODY_CHANGE_LANG_PACK, tblLang.getSelectionModel().getSelectedItem().getKey()),
                       stage
                       );
        });
        // Button bar functionality
        btnApply.setOnMouseClicked(e -> {
            synchronized(CURRENT_SETTINGS) {
                CURRENT_SETTINGS.clear();
                CURRENT_SETTINGS.putAll(WORKING_SETTINGS);
                try(OutputStream settingsOutputStream = Files.newOutputStream(SETTINGS_FILE)) {
                    CURRENT_SETTINGS.store(settingsOutputStream, String.format("PearlZip Settings File Generated @ %s",
                                                                               LocalDateTime.now()));
                } catch(IOException exc) {
                }
            }

            synchronized(WORKING_APPLICATION_SETTINGS) {
                try(OutputStream settingsOutputStream = Files.newOutputStream(APPLICATION_SETTINGS_FILE)) {
                    WORKING_APPLICATION_SETTINGS.store(settingsOutputStream,
                                                       String.format(CNS_PROP_HEADER,
                                                                     LocalDateTime.now()));
                    // Reloading providers and cached System settings into PearlZip
                    WORKING_APPLICATION_SETTINGS.keySet()
                                                .stream()
                                                .filter(k -> !RK_KEYS.contains(k))
                                                .forEach(k -> System.setProperty(k.toString(),
                                                                                 WORKING_APPLICATION_SETTINGS.getProperty(
                                                                                         k.toString()))
                                                );
                    new LinkedList<>(ZipState.getWriteProviders()).forEach(ZipState::addArchiveProvider);
                    new LinkedList<>(ZipState.getReadProviders()).forEach(ZipState::addArchiveProvider);

                    // Load custom menus from plugins
                    Stage aboutStage = genFrmAbout();
                    List<javafx.scene.control.Menu> customMenus = loadMenusFromPlugins();
                    InternalContextCache.INTERNAL_CONFIGURATION_CACHE
                                        .<PearlZipApplication>getAdditionalConfig(CK_APP)
                                        .get()
                                        .createSystemMenu(aboutStage, customMenus);
                } catch(IOException exc) {
                }  finally {
                    JFXUtil.getMainStageInstances().forEach(s -> JFXUtil.setSafeModeTitles(Boolean.parseBoolean(WORKING_APPLICATION_SETTINGS.getProperty(CNS_NTAK_PEARL_ZIP_SAFE_MODE,"false")), s));
                }
            }
        });

        btnOk.setOnMouseClicked(e->{
            btnApply.getOnMouseClicked().handle(e);
            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        });

        btnCancel.setOnMouseClicked(e -> {
            synchronized(CURRENT_SETTINGS) {
                WORKING_SETTINGS.clear();
                WORKING_SETTINGS.putAll(CURRENT_SETTINGS);
            }

            synchronized(WORKING_APPLICATION_SETTINGS) {
                WORKING_APPLICATION_SETTINGS.clear();
            }

            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
            JFXUtil.getMainStageInstances().forEach(s -> JFXUtil.setSafeModeTitles(Boolean.parseBoolean(System.getProperty(CNS_NTAK_PEARL_ZIP_SAFE_MODE, "false")),
                                                                                       s));
        });

        paneDropArea.setOnDragOver(e -> e.acceptTransferModes(TransferMode.COPY));
        paneDropArea.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            if (db.hasFiles()) {
                List<File> pzaxArchives =
                        db.getFiles()
                          .stream()
                          .filter(f -> f.isFile() && f.getName()
                                                      .endsWith(".pzax"))
                          .collect(Collectors.toList());

                for (File pzaxArchive : pzaxArchives) {
                    // TITLE: Confirm installation of Pearl Zip Extension
                    // HEADER: New pzax installation file has been detected
                    // CONFIRM: Please confirm whether you wish to install the pzax extension: %s
                    Optional<ButtonType> response = raiseAlert(Alert.AlertType.CONFIRMATION,
                                                               resolveTextKey(TITLE_CONFIRM_INSTALL_EXTENSION),
                                                               resolveTextKey(HEADER_CONFIRM_INSTALL_EXTENSION),
                                                               resolveTextKey(BODY_CONFIRM_INSTALL_EXTENSION,
                                                                              pzaxArchive),
                                                               null, stage,
                                                               ButtonType.YES, ButtonType.NO);

                    if (response.isPresent() && response.get()
                                                        .equals(ButtonType.YES)) {
                        loadModuleFromExtensionPackage(pzaxArchive.toPath());
                        LOG_BUNDLE = ModuleUtil.loadLangPackDynamic(RUNTIME_MODULE_PATH,
                                                                    System.getProperty(CNS_RES_BUNDLE, "pearlzip"),
                                                                    Locale.getDefault());
                        CUSTOM_BUNDLE = ModuleUtil.loadLangPackDynamic(RUNTIME_MODULE_PATH,
                                                                       System.getProperty(CNS_CUSTOM_RES_BUNDLE,"custom"),
                                                                       Locale.getDefault());
                    }
                }
            }
        });
        paneDropArea.setOnMouseClicked((e) -> {
            try {
                if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                    stage.setAlwaysOnTop(false);

                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle(resolveTextKey(TITLE_SELECT_INSTALL_EXTENSION));
                    fileChooser.getExtensionFilters()
                               .add(new FileChooser.ExtensionFilter("PearlZip Extensions (*.pzax)",
                                                                    "*.pzax"));
                    File pzaxArchive = fileChooser.showOpenDialog(new Stage());
                    if (Objects.nonNull(pzaxArchive)) {
                        try {
                            ((Stage) tabPaneOptions.getScene()
                                                   .getWindow()).setAlwaysOnTop(false);
                            loadModuleFromExtensionPackage(pzaxArchive.toPath());
                            LOG_BUNDLE = ModuleUtil.loadLangPackDynamic(RUNTIME_MODULE_PATH,
                                                                        System.getProperty(CNS_RES_BUNDLE, "pearlzip"),
                                                                        Locale.getDefault());
                            CUSTOM_BUNDLE = ModuleUtil.loadLangPackDynamic(RUNTIME_MODULE_PATH,
                                                                           System.getProperty(CNS_CUSTOM_RES_BUNDLE,"custom"),
                                                                           Locale.getDefault());
                        } finally {
                            ((Stage) tabPaneOptions.getScene()
                                                   .getWindow()).setAlwaysOnTop(true);
                        }
                    }
                }
            } finally {
                stage.setAlwaysOnTop(true);
            }
        });
    }
}