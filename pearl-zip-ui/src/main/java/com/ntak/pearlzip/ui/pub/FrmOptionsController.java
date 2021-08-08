/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.pub;

import com.ntak.pearlzip.archive.pub.ArchiveReadService;
import com.ntak.pearlzip.archive.pub.ArchiveService;
import com.ntak.pearlzip.archive.pub.ArchiveWriteService;
import com.ntak.pearlzip.archive.pub.ProgressMessage;
import com.ntak.pearlzip.ui.constants.ZipConstants;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.model.FXMigrationInfo;
import com.ntak.pearlzip.ui.model.ZipState;
import com.ntak.pearlzip.ui.util.ArchiveUtil;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Pair;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.archive.constants.ArchiveConstants.CURRENT_SETTINGS;
import static com.ntak.pearlzip.archive.constants.ArchiveConstants.WORKING_SETTINGS;
import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.REGEX_TIMESTAMP_DIR;
import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.TMP_DIR_PREFIX;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.PROGRESS;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ResourceConstants.PATTERN_FXID_NEW_OPTIONS;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.JFXUtil.*;
import static javafx.scene.control.ProgressIndicator.INDETERMINATE_PROGRESS;

/**
 *  Controller for the Options dialog.
 *  @author Aashutos Kakshepati
*/
public class FrmOptionsController {
    private static final Logger LOGGER = LoggerContext.getContext().getLogger(FrmOptionsController.class);

    ///// General /////
    @FXML
    private TabPane tabPaneOptions;

    @FXML
    private Button btnClearCache;

    ///// Bootstrap Properties /////
    @FXML
    private TableView<Pair<String,String>> tblBootstrap;
    @FXML
    private TableColumn<Pair<String,String>, String> key;
    @FXML
    private TableColumn<Pair<String,String>, String> value;

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
    private Button btnOk;
    @FXML
    private Button btnApply;
    @FXML
    private Button btnCancel;

    @FXML
    public void initialize() {
        ///// Bootstrap Properties /////
        key.setCellValueFactory(new PropertyValueFactory<>("Key"));
        value.setCellValueFactory(new PropertyValueFactory<>("Value"));
        tblBootstrap.setItems(FXCollections.observableArrayList(System.getProperties()
                                                                          .entrySet()
                                                                          .stream()
                                                                          .map(e->new Pair<>((String)e.getKey(), (String)e.getValue()))
                                                                          .collect(Collectors.toList()))
        );

        ///// Provider Properties /////
        name.setCellValueFactory((s)-> new SimpleStringProperty(s.getValue().getValue().getClass().getCanonicalName()));
        readCapability.setCellValueFactory((s)-> new SimpleObjectProperty<>(s.getValue()));
        readCapability.setCellFactory((c)-> new TableCell<>() {
            @Override
            public void updateItem(Pair<Boolean,ArchiveService> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    CheckBox checkBox = new CheckBox();
                    checkBox.setSelected(!item.getKey());
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
            } catch (Exception e) {
            }

            return new SimpleStringProperty("N/A");
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
        services.addAll(ZipState.getWriteProviders().stream().map(s->new Pair<Boolean,ArchiveService>(true, s)).collect(Collectors.toList()));
        services.addAll(ZipState.getReadProviders().stream().map(s->new Pair<Boolean,ArchiveService>(false,s)).collect(Collectors.toList()));
        tblProviders.setItems(FXCollections.observableArrayList(services));

        for (ArchiveService service : services.stream().map(Pair::getValue).collect(Collectors.toList())) {
            if ((service.getOptionsPane()).isPresent()) {
                Pair<String,Node> tab = service.getOptionsPane()
                                               .get();

                Tab customTab = new Tab();
                customTab.setText(tab.getKey());
                customTab.setId(String.format(PATTERN_FXID_NEW_OPTIONS,
                                              service.getClass()
                                                     .getCanonicalName()));
                customTab.setContent(tab.getValue());
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
                    executeBackgroundProcess(sessionId, stage, () -> {
                                                 // Clearing up temporary storage location...
                                                 ArchiveService.DEFAULT_BUS.post(new ProgressMessage(sessionId, PROGRESS,
                                                                                                     resolveTextKey(LBL_CLEAR_UP_TEMP_STORAGE),
                                                                                                     INDETERMINATE_PROGRESS, 1));
                                                 List<String> openFiles =
                                                         getMainStageInstances().stream()
                                                                                .map(s -> ((FXArchiveInfo) s.getUserData()).getArchivePath())
                                                                                .collect(
                                                                                        Collectors.toList());
                                                 Files.newDirectoryStream(ZipConstants.STORE_TEMP,
                                                                          (f) -> !openFiles.contains(f.toAbsolutePath().toString()))
                                                      .forEach(f -> {
                                                          try {
                                                              Files.deleteIfExists(f);
                                                          } catch(IOException ioException) {
                                                          }
                                                      });

                                                 // Cleaning up OS temporary data..
                                                 long activeMigrationsCount =
                                                         getMainStageInstances().stream()
                                                                                .map(s -> ((FXArchiveInfo) s.getUserData()).getMigrationInfo().getType())
                                                         .filter(t->!t.equals(FXMigrationInfo.MigrationType.NONE))
                                                         .count();
                                                 if (activeMigrationsCount == 0) {
                                                     ArchiveService.DEFAULT_BUS.post(new ProgressMessage(sessionId,
                                                                                                         PROGRESS,
                                                                                                         resolveTextKey(
                                                                                                                 LBL_CLEAR_UP_OS_TEMP),
                                                                                                         INDETERMINATE_PROGRESS,
                                                                                                         1));
                                                     LinkedList<Path> tempDirectories = new LinkedList<>();
                                                     try (DirectoryStream<Path> dirs =
                                                                  Files.newDirectoryStream(ZipConstants.LOCAL_TEMP,
                                                                              (f) -> f.getFileName()
                                                                                      .toString()
                                                                                      .startsWith(TMP_DIR_PREFIX) || f.getFileName().toString().matches(
                                                                                      REGEX_TIMESTAMP_DIR))) {
                                                          dirs.forEach(tempDirectories::add);
                                                     }

                                                     // Remove nested pz directory in .pz/temp directory
                                                     Files.newDirectoryStream(ZipConstants.STORE_TEMP,
                                                                              f -> f.getFileName()
                                                                                    .toString()
                                                                                    .startsWith(TMP_DIR_PREFIX) || f.getFileName().toString().matches(
                                                                                      REGEX_TIMESTAMP_DIR))
                                                          .forEach(tempDirectories::add);

                                                     // LOG: Temporary directories to be deleted: %s
                                                     LOGGER.debug(resolveTextKey(LOG_TEMP_DIRS_TO_DELETE,
                                                                                 tempDirectories));
                                                     tempDirectories.stream()
                                                                    .forEach(ArchiveUtil::deleteDirectory);
                                                 } else {
                                                     ArchiveService.DEFAULT_BUS.post(new ProgressMessage(sessionId,
                                                                                                         PROGRESS,
                                                                                                         resolveTextKey(
                                                                                                                 LBL_SKIP_OS_TEMP_CLEAN),
                                                                                                         INDETERMINATE_PROGRESS,
                                                                                                         1));
                                                 }

                                                 // Clearing up recently open files...
                                                 ArchiveService.DEFAULT_BUS.post(new ProgressMessage(sessionId, PROGRESS,
                                                                                                     resolveTextKey(LBL_CLEAR_UP_RECENTS),
                                                                                                     INDETERMINATE_PROGRESS, 1));
                                                 Files.deleteIfExists(ZipConstants.RECENT_FILE);
                                             },
                                             System.out::println,
                                             (s) -> {});
                }
            } finally {
                LCK_CLEAR_CACHE.writeLock().unlock();
                btnClearCache.setDisable(false);
            }
        });

        btnApply.setOnMouseClicked(e->{
            synchronized(CURRENT_SETTINGS) {
                CURRENT_SETTINGS.clear();
                CURRENT_SETTINGS.putAll(WORKING_SETTINGS);
                try (OutputStream settingsOutputStream = Files.newOutputStream(SETTINGS_FILE)) {
                    CURRENT_SETTINGS.store(settingsOutputStream, String.format("PearlZip Settings File Generated @ %s",
                                                                               LocalDateTime.now()));
                } catch (IOException exc) {
                }
            }
        });

        btnOk.setOnMouseClicked(e->{
            btnApply.getOnMouseClicked().handle(e);
            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        });

        btnCancel.setOnMouseClicked(e->{
            synchronized(CURRENT_SETTINGS) {
                WORKING_SETTINGS.clear();
                WORKING_SETTINGS.putAll(CURRENT_SETTINGS);
            }
            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        });
    }
}