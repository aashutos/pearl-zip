/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.pub;

import com.ntak.pearlzip.ui.constants.ExtensionType;
import com.ntak.pearlzip.ui.constants.InstallState;
import com.ntak.pearlzip.ui.constants.ResourceConstants;
import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.util.ExtensionStoreEntry;
import com.ntak.pearlzip.ui.util.QueryPagination;
import com.ntak.pearlzip.ui.util.StoreRepoDetails;
import com.ntak.pearlzip.ui.util.internal.JFXUtil;
import com.ntak.pearlzip.ui.util.internal.ModuleUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.*;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.CUSTOM_BUNDLE;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.LOG_BUNDLE;
import static com.ntak.pearlzip.archive.util.LoggingUtil.getStackTraceFromException;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.JFXUtil.raiseAlert;
import static com.ntak.pearlzip.ui.util.internal.ModuleUtil.loadModuleFromExtensionPackage;

public class FrmStoreController {

    private static final Logger LOGGER = LoggerContext.getContext()
                                                      .getLogger(FrmStoreController.class);

    @FXML
    private Button btnDetails;
    @FXML
    private Button btnInstall;
    @FXML
    private Button btnPrevious;
    @FXML
    private Button btnNext;
    @FXML
    private Button btnSearch;

    @FXML
    private TableView<ExtensionStoreEntry> tblStore;
    @FXML
    private TableColumn<ExtensionStoreEntry,String> colName;
    @FXML
    private TableColumn<ExtensionStoreEntry,String> colDescription;
    @FXML
    private TableColumn<ExtensionStoreEntry,String> colMinVersion;
    @FXML
    private TableColumn<ExtensionStoreEntry,String> colInstallState;

    @FXML
    private ChoiceBox<String> choiceStore;
    @FXML
    private ChoiceBox<String> choiceInstallState;
    @FXML
    private ChoiceBox<String> choiceExtType;
    @FXML
    private ChoiceBox<Integer> choicePagination;

    @FXML
    private Button btnCancel;

    private AtomicReference<QueryPagination> pgRef = new AtomicReference<>();

    public void initData(Stage stage) {
        // Initialise Combo Boxes
        choiceInstallState.setItems(FXCollections.observableList(Arrays.stream(InstallState.values()).map(InstallState::getValue).collect(Collectors.toList())));
        choiceExtType.setItems(FXCollections.observableList(Arrays.stream(ExtensionType.values()).map(ExtensionType::getValue).collect(Collectors.toList())));
        choiceStore.setItems(FXCollections.observableArrayList(InternalContextCache.INTERNAL_CONFIGURATION_CACHE
                                                                  .<Map<String,StoreRepoDetails>>getAdditionalConfig(CK_STORE_REPO)
                                                                  .get().keySet())
        );
        choicePagination.setItems(FXCollections.observableList(List.of(10,50,100,200)));

        choiceInstallState.getSelectionModel().select(0);
        choiceExtType.getSelectionModel().select(0);
        choiceStore.getSelectionModel().select(0);
        choicePagination.getSelectionModel().select(0);

        // Column mappings
        colName.setCellValueFactory((s)-> new SimpleStringProperty(s.getValue().packageName()));
        colDescription.setCellValueFactory((s)-> new SimpleStringProperty(s.getValue().description()));
        colInstallState.setCellValueFactory((s)-> new SimpleStringProperty(s.getValue().installState()));
        colMinVersion.setCellValueFactory((s)-> new SimpleStringProperty(s.getValue().minVersion()));

        // Cached data load, if available
        pgRef.set(new QueryPagination(choiceStore.getValue(), 0, choicePagination.getValue(), JFXUtil.getExtensionStoreEntryCount(choiceStore.getValue(), System.getProperty(CNS_NTAK_PEARL_ZIP_RAW_VERSION), false)));
        synchronized(pgRef) {
            // Execute query and pull entries for current page
            updatePaginatedTable(pgRef.get(), tblStore, btnNext, btnPrevious, colInstallState, choiceExtType.getSelectionModel().getSelectedItem(),
                                 choiceInstallState.getSelectionModel().getSelectedItem(), false);
        }

        // Initialise buttons
        btnCancel.setOnAction(e -> stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST)));
        btnSearch.setOnAction(e -> {
            // Execute count query and encapsulate in pagination object
            // Assumption: Very slow moving dataset and so cached count will be valid over a long period of time.
            pgRef.set(new QueryPagination(choiceStore.getValue(), 0, choicePagination.getValue(), JFXUtil.getExtensionStoreEntryCount(choiceStore.getValue(), System.getProperty(CNS_NTAK_PEARL_ZIP_RAW_VERSION), true)));

            synchronized(pgRef) {
                // Execute query and pull entries for current page
                updatePaginatedTable(pgRef.get(), tblStore, btnNext, btnPrevious, colInstallState, choiceExtType.getSelectionModel().getSelectedItem(),
                                     choiceInstallState.getSelectionModel().getSelectedItem(), true);
            }
        });
        btnNext.setOnAction((e)-> {
            synchronized(pgRef) {
                final var pagination = pgRef.get();
                if (pagination.isNextPageAvailable()) {
                    pagination.setOffset(pagination.getOffset() + 1);
                    updatePaginatedTable(pagination, tblStore, btnNext, btnPrevious, colInstallState,
                                         choiceExtType.getSelectionModel()
                                                      .getSelectedItem(),
                                         choiceInstallState.getSelectionModel()
                                                           .getSelectedItem(),
                                         true);
                }
            }
        });
        btnPrevious.setOnAction((e) -> {
            synchronized(pgRef) {
                final var pagination = pgRef.get();
                if (pagination.isPreviousPageAvailable()) {
                    pagination.setOffset(pagination.getOffset() - 1);
                    updatePaginatedTable(pagination, tblStore, btnNext, btnPrevious, colInstallState,
                                         choiceExtType.getSelectionModel()
                                                      .getSelectedItem(),
                                         choiceInstallState.getSelectionModel()
                                                           .getSelectedItem(),
                                         true);
                }
            }
        });
        btnInstall.setOnAction((e) -> {
            // Get link and download pzax archive to temp directory
            ExtensionStoreEntry entry = tblStore.getSelectionModel().getSelectedItem();
            String hash = entry.packageHash();

            // TITLE: Confirm installation of Pearl Zip Extension
            // HEADER: New pzax installation file has been detected
            // CONFIRM: Please confirm whether you wish to install the pzax extension: %s
            Optional<ButtonType> response = raiseAlert(Alert.AlertType.CONFIRMATION,
                                                       resolveTextKey(TITLE_CONFIRM_INSTALL_EXTENSION),
                                                       resolveTextKey(HEADER_CONFIRM_INSTALL_EXTENSION),
                                                       resolveTextKey(BODY_CONFIRM_INSTALL_EXTENSION,
                                                                      entry.packageName()),
                                                       null, stage,
                                                       ButtonType.YES, ButtonType.NO);

            if (response.isPresent() && response.get().equals(ButtonType.YES)) {
                try(InputStream is = new URL(entry.packageURL()).openConnection()
                                                                .getInputStream()) {
                    Path pzaxArchive = Files.createTempDirectory(TMP_DIR_PREFIX)
                                            .resolve("tmp.pzax");
                    Files.copy(is, pzaxArchive);

                    // Perform hash check on package
                    MessageDigest digest = MessageDigest.getInstance(ResourceConstants.SHA_512);
                    String actualHash = HexFormat.of()
                                                 .formatHex(digest.digest(Files.readAllBytes(pzaxArchive)));

                    if (actualHash.equals(hash)) {
                        Path RUNTIME_MODULE_PATH = InternalContextCache.INTERNAL_CONFIGURATION_CACHE.<Path>getAdditionalConfig(CK_RUNTIME_MODULE_PATH).get();
                        // Run installation routine
                        loadModuleFromExtensionPackage(pzaxArchive);
                        LOG_BUNDLE = ModuleUtil.loadLangPackDynamic(RUNTIME_MODULE_PATH,
                                                                    System.getProperty(CNS_RES_BUNDLE, "pearlzip"),
                                                                    Locale.getDefault());
                        CUSTOM_BUNDLE = ModuleUtil.loadLangPackDynamic(RUNTIME_MODULE_PATH,
                                                                       System.getProperty(CNS_CUSTOM_RES_BUNDLE,"custom"),
                                                                       Locale.getDefault());
                    } else {
                        // LOG: Calculated hash (%s) does not match the expected
                        // reference (%s)
                        // value. Integrity check failed for library: %s.
                        LOGGER.warn(resolveTextKey(LOG_HASH_INTEGRITY_FAILURE,
                                                   actualHash, hash,
                                                   entry.packageName()));
                    }
                } catch(IOException | NoSuchAlgorithmException ex) {
                    // LOG: Issue downloading and installing package: %s from url: %s.\nStack trace:%s
                    LOGGER.error(resolveTextKey(LOG_ISSUE_DOWNLOADING_PZAX_ARCHIVE, entry.packageName(), entry.packageURL(), getStackTraceFromException(ex)));
                }
            }
        });

        tblStore.setOnMouseClicked((e) -> {
            if (Objects.nonNull(tblStore.getSelectionModel().getSelectedItem())) {
                btnDetails.setDisable(false);
                if (colInstallState.getCellObservableValue(tblStore.getSelectionModel().getSelectedIndex()).getValue().equals(InstallState.INSTALLABLE.getValue())
                    || colInstallState.getCellObservableValue(tblStore.getSelectionModel().getSelectedIndex()).getValue().equals(InstallState.UPDATABLE.getValue())) {
                    btnInstall.setDisable(false);
                } else {
                    btnInstall.setDisable(true);
                }
            } else {
                btnDetails.setDisable(true);
            }
        });

        btnDetails.setOnAction((e) -> {
            // Display details page with contents of selected plugin
            try {
                ExtensionStoreEntry entry = tblStore.getSelectionModel().getSelectedItem();

                // Display Extension Details
                // Initialise Stage
                Stage detailsStage = new Stage();
                AnchorPane root;

                detailsStage.setTitle(resolveTextKey(TITLE_EXTENSION_DETAILS));
                detailsStage.setResizable(false);
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(ZipLauncher.class.getClassLoader()
                                                    .getResource("frmESEntryDetails.fxml"));
                loader.setResources(LOG_BUNDLE);
                root = loader.load();
                FrmESEntryDetailsController controller = loader.getController();
                controller.initData(entry, detailsStage);

                Scene scene = new Scene(root);
                detailsStage.setScene(scene);

                detailsStage.show();
                detailsStage.setAlwaysOnTop(true);
            } catch (Exception exc) {

            }
        });
    }

    private static void updatePaginatedTable(QueryPagination pagination, TableView<ExtensionStoreEntry> tblStore, Button btnNext, Button btnPrevious, TableColumn<ExtensionStoreEntry,String> colInstallState, String extType, String installState,
            boolean isRefreshForced) {
        boolean isFiltered = !extType.equals(ExtensionType.ALL.getValue()) || !installState.equals(InstallState.ALL.getValue());

        // Execute query and pull entries for current page
        List<ExtensionStoreEntry> entries = new LinkedList<>();
        if (!isFiltered) {
            entries = JFXUtil.getExtensionStoreEntries(pagination.getIdentifier(), System.getProperty(CNS_NTAK_PEARL_ZIP_RAW_VERSION), pagination.getOffset(), pagination.getPagination(), isRefreshForced);
        } else { // Filtered data - Quite inefficient process at present...
            int count = JFXUtil.getExtensionStoreEntryCount(pagination.getIdentifier(), System.getProperty(CNS_NTAK_PEARL_ZIP_RAW_VERSION), isRefreshForced);
            int pages = (int)Math.ceil((double)count/pagination.getPagination());

            // Get all page entries...
            for (int i = 0; i < pages; i++) {
                entries.addAll(JFXUtil.getExtensionStoreEntries(pagination.getIdentifier(), System.getProperty(CNS_NTAK_PEARL_ZIP_RAW_VERSION), i, pagination.getPagination(), isRefreshForced));
            }
        }

        // filter by extension type...
        if (!extType.equals(ExtensionType.ALL.getValue())) {
            entries = new LinkedList<>(entries.stream().filter(f -> f.typeName().equals(extType)).toList());
        }

        // Setup table
        tblStore.setItems(FXCollections.observableList(entries));

        // Filter by install state...
        if (!installState.equals(InstallState.ALL.getValue())) {
            entries = new LinkedList<>(entries.stream().filter(f -> f.installState().equals(installState)).toList());
        }

        // Set entries by current page when filtered...
        if (isFiltered) {
            if ((pagination.getOffset() * pagination.getPagination()) < entries.size()) {
                pagination.setCount(entries.size());
                entries = new ArrayList<>(entries).subList(pagination.getOffset() * pagination.getPagination(), Math.min(entries.size(), (pagination.getOffset() + 1) * pagination.getPagination()));
            } else {
                // If filtered entries fit in one page or page is too far ahead...
                pagination.setOffset(0);
            }
        }
        tblStore.setItems(FXCollections.observableList(entries));

        // Update pagination post filters...
        if (entries.size() < pagination.getPagination()) {
            pagination.setCount(pagination.getOffset()*pagination.getPagination() + entries.size());
        }
        tblStore.refresh();

        // Update button state
        btnNext.setDisable(!pagination.isNextPageAvailable());
        btnPrevious.setDisable(!pagination.isPreviousPageAvailable());
    }
}
