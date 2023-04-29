/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.testfx;

import com.ntak.pearlzip.archive.model.PluginInfo;
import com.ntak.pearlzip.archive.pub.ArchiveReadService;
import com.ntak.pearlzip.archive.pub.ArchiveService;
import com.ntak.pearlzip.archive.pub.ArchiveWriteService;
import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.util.*;
import com.ntak.pearlzip.ui.util.internal.ModuleUtil;
import com.ntak.pearlzip.ui.util.internal.QueryResult;
import com.ntak.testfx.FormUtil;
import com.ntak.testfx.TypeUtil;
import com.ntak.testfx.specifications.CommonSpecifications;
import javafx.collections.FXCollections;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.archive.constants.ArchiveConstants.CURRENT_SETTINGS;
import static com.ntak.pearlzip.archive.constants.ArchiveConstants.WORKING_APPLICATION_SETTINGS;
import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.*;
import static com.ntak.pearlzip.ui.UITestSuite.clearDirectory;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.PearlZipFXUtil.simOpenArchive;
import static com.ntak.pearlzip.ui.util.PearlZipFXUtil.simSelectOptionsAdditionalTab;
import static com.ntak.pearlzip.ui.util.internal.JFXUtil.loadStoreRepoDetails;
import static com.ntak.testfx.FormUtil.selectComboBoxEntry;
import static com.ntak.testfx.FormUtil.selectTableViewEntry;
import static com.ntak.testfx.NativeFileChooserUtil.chooseFile;
import static com.ntak.testfx.TestFXConstants.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class OptionsTestFX extends AbstractPearlZipTestFX {
    private Path tempOSDir;
    private Path tempPZDir;
    private Path STORE_ROOT = Paths.get(System.getProperty("user.home"), ".pz");
    private Path STORE_TEMP = STORE_ROOT.resolve("temp");

    /*
     *  Test cases:
     *  + Clear cache with an open temporary archive. Temporary files except for open archive is removed
     *  + Clear cache with a saved archive. All temp files are removed
     *  + Check reserved keys in Bootstrap properties are as expected
     *  + Check the expected providers have been loaded
     *  + Load pzax package successfully
     *  + Check application.properties got updated by a change to the default archive format in the Options dialog
     *  + Set safe mode -> Updates configuration file for next boot up
     *  + Set show notification -> Updates configuration file for next boot up
     *  + Change language pack
     *  + Add new, switch and uninstall new theme via the Options tab
     *  + Incompatible plugin -> lower than minimum version
     *  + Incompatible plugin -> higher than maximum version
     *  + Purge all plugins
     *  + Plugin successful install and replacement of legacy library
     *  + Add Store repository successfully
     *  + Edit Store repository successfully
     */

    @Override
    public void start(Stage stage) throws IOException, TimeoutException {
        super.start(stage);
        InternalContextCache.INTERNAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_POST_PZAX_COMPLETION_CALLBACK, (Runnable)() -> {});

        Path LOCAL_TEMP =
                Paths.get(Optional.ofNullable(System.getenv("TMPDIR"))
                                  .orElse(STORE_ROOT.toString()));
        tempOSDir = Paths.get(LOCAL_TEMP.toAbsolutePath()
                                        .toString(), String.format("pz%d", System.currentTimeMillis()));
        sleep(250);
        tempPZDir = Paths.get(STORE_TEMP.toAbsolutePath()
                                        .toString(), String.format("pz%d", System.currentTimeMillis()));


        Files.createDirectories(tempOSDir);
        Files.createDirectories(tempPZDir);

        Files.createFile(Paths.get(STORE_TEMP.toAbsolutePath().toString(), "a1234567890.zip"));
        Files.createFile(Paths.get(tempOSDir.toAbsolutePath().toString(), "nestedFile.tar"));
        Files.createFile(Paths.get(tempPZDir.toAbsolutePath().toString(), "anotherNestedFile.tar"));

        Assertions.assertEquals(2,
                                Files.list(STORE_TEMP)
                                     .filter(Files::isRegularFile)
                                     .count(),
                                "Initial files have not been setup");

        // Test query result data...
        InternalContextCache.INTERNAL_CONFIGURATION_CACHE
                .<Map<String,QueryResult>>getAdditionalConfig(CK_QUERY_RESULT_CACHE)
                .get()
                .put("test", new QueryResult(Collections.emptyList()));
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        clearDirectory(tempPZDir);
        clearDirectory(tempOSDir);
        Files.list(STORE_TEMP)
             .filter(Files::isRegularFile)
             .forEach(f-> {
                 try {
                     Files.deleteIfExists(f);
                 } catch(IOException e) {
                 }
             });
    }

    @Test
    @DisplayName("Test: Clear cache with an open temporary archive. Temporary files except for open archive is removed")
    // GIVEN ~/.pz/db-cache directory has been initialised
    //      AND ensure directory (temporary) exists
    //      AND ensure file (nestedFile.jar) exists
    // WHEN Options dialog opened
    //      AND node (#tabGeneral) clicked
    //      AND node (#btnClearCache) clicked
    //      AND click Yes on confirmation dialog
    // THEN ensure file (temporary) exists
    //     AND ensure file (a1234567890.zip) does not exist
    //     AND ensure folder (temporary) does not exist
    //     AND ensure file (nestedFile.jar) does not exist
    //     AND ensure folder (db-cache) does not exist
    //     AND ensure map (CK_QUERY_RESULT_CACHE) is empty
    public void testFX_ClearCacheTemporaryArchiveOpen_MatchExpectations() throws IOException {
        // Given
        Path STORE_ROOT = InternalContextCache.GLOBAL_CONFIGURATION_CACHE
                .<Path>getAdditionalConfig(CK_STORE_ROOT)
                .get();
        final var dbCacheDir = STORE_ROOT.toAbsolutePath()
                                                .resolve("db-cache");
        CommonSpecifications.givenDirectoryHasBeenCreated(dbCacheDir);

        Path fileToBeKept =
                Files.list(STORE_TEMP)
                     .filter(f->!f.getFileName().toString().endsWith("a1234567890.zip") && Files.isRegularFile(f))
                     .findFirst()
                     .get();

        Path fileToBeDeleted =
                Files.list(STORE_TEMP)
                     .filter(f->f.getFileName().toString().endsWith("a1234567890.zip"))
                     .findFirst()
                     .get();
        CommonSpecifications.thenExpectFileExists(fileToBeKept);
        CommonSpecifications.thenExpectFileExists(fileToBeDeleted);

        // When
        PearlZipSpecifications.whenOptionDialogOpened(this);
        CommonSpecifications.whenNodeClickedByName(this, "#tabGeneral");
        CommonSpecifications.whenNodeClickedByName(this, "#btnClearCache");
        CommonSpecifications.whenButtonClickedOnDialog(this, ButtonType.YES);

        // Then
        CommonSpecifications.thenExpectFileExists(fileToBeKept);
        CommonSpecifications.thenNotExpectFileExists(fileToBeDeleted);
        CommonSpecifications.thenNotExpectFileExists(tempOSDir);
        CommonSpecifications.thenNotExpectFileExists(tempOSDir.resolve("nestedFile.tar"));
        CommonSpecifications.thenNotExpectFileExists(dbCacheDir);
        Assertions.assertEquals(0, InternalContextCache.INTERNAL_CONFIGURATION_CACHE.<Map<String,QueryResult>>getAdditionalConfig(CK_QUERY_RESULT_CACHE).get().size(), "In-memory cache was not emptied");
    }

    @Test
    @DisplayName("Test: Clear cache with a saved archive. All temp files are removed")
    // GIVEN zip archive (src/test/resources/test.zip) is open in PearlZip
    //      AND ensure no files from directory (~/.pz/temp) is open in PearlZip
    //      AND ensure no files from directory (OS temporary) is open in PearlZip
    //      AND ensure file (nestedFile.jar) exists
    //      AND ensure file (anotherNestedFile.tar) exists
    // WHEN Options dialog opened
    //      AND node (#tabGeneral) clicked
    //      AND node (#btnClearCache) clicked
    //      AND click Yes on confirmation dialog
    // THEN ensure folder (OS temporary) does not exist
    //     AND ensure file (nestedFile.jar) does not exist
    //     AND ensure folder (temporary) does not exist
    //     AND ensure file (anotherNestedFile.jar) does not exist
    //     AND ensure file (test.zip) exists
    public void testFX_ClearCacheSavedArchiveOpen_MatchExpectations() throws IOException {
        // Given
        // Open existing archive in current window
        clickOn(Point2D.ZERO.add(110, 10))
                .clickOn(Point2D.ZERO.add(110, 80));
        final Path archivePath = Paths.get("src", "test", "resources", "test.zip")
                                      .toAbsolutePath();
        // Via Sys menu
        simOpenArchive(this, archivePath, false, false);

        // Check initial state...
        final List<Path> filesToBeDeleted = new LinkedList<>();
        filesToBeDeleted.addAll(Files.list(STORE_TEMP).filter(Files::isRegularFile).filter(f->JFXUtil.getMainStageInstances().stream().noneMatch(s->s.getTitle().contains(f.toAbsolutePath().toString()))).collect(Collectors.toList()));
        filesToBeDeleted.addAll(Files.list(tempPZDir).filter(Files::isRegularFile).filter(f->JFXUtil.getMainStageInstances().stream().noneMatch(s->s.getTitle().contains(f.toAbsolutePath().toString()))).collect(Collectors.toList()));

        for (Path file : filesToBeDeleted) {
            PearlZipSpecifications.thenExpectArchiveNotOpen(file);
        }

        CommonSpecifications.thenExpectFileExists(tempOSDir.resolve("nestedFile.tar"));
        CommonSpecifications.thenExpectFileExists(tempPZDir.resolve("anotherNestedFile.tar"));

        // When
        PearlZipSpecifications.whenOptionDialogOpened(this);
        CommonSpecifications.whenNodeClickedByName(this, "#tabGeneral");
        CommonSpecifications.whenNodeClickedByName(this, "#btnClearCache");
        CommonSpecifications.whenButtonClickedOnDialog(this, ButtonType.YES);
        sleep(250, MILLISECONDS);

        // Then
        CommonSpecifications.thenNotExpectFileExists(tempOSDir.resolve("nestedFile.tar"));
        CommonSpecifications.thenNotExpectFileExists(tempPZDir.resolve("anotherNestedFile.tar"));
        CommonSpecifications.thenNotExpectFileExists(tempPZDir);
        CommonSpecifications.thenExpectFileExists(archivePath);

    }

    @Test
    @DisplayName("Test: Refresh keystore recreates keystore and truststore in .store directory")
    // GIVEN file (keystore.jks) exists with attribute (creationTime)
    //      AND file (truststore.jks) exists with attribute (creationTime)
    // WHEN Options dialog opened
    //      AND node (#tabGeneral) clicked
    //      AND node (#btnRefreshKeystore) clicked
    //      AND click Yes on confirmation dialog
    // THEN ensure timestamp for file (keystore.jks) has changed to a newer value
    //      AND ensure timestamp for file (truststore.jks) has changed to a newer value
    public void testFX_RefreshKeystore_MatchExpectations() throws IOException {
        // Given
        Path keystorePath = STORE_ROOT.resolve(Path.of(".store","keystore.jks"));
        Path truststorePath = STORE_ROOT.resolve(Path.of(".store","truststore.jks"));
        long ksTime = CommonSpecifications.givenFileHasAttribute(keystorePath, "creationTime", FileTime.class).toMillis();
        long tsTime = CommonSpecifications.givenFileHasAttribute(truststorePath, "creationTime", FileTime.class).toMillis();

        // When
        PearlZipSpecifications.whenOptionDialogOpened(this);
        CommonSpecifications.whenNodeClickedByName(this, "#btnRefreshKeystore");
        sleep(MEDIUM_PAUSE, MILLISECONDS);
        CommonSpecifications.whenButtonClickedOnDialog(this, ButtonType.YES);

        // Then
        long ksTimeAfter = CommonSpecifications.givenFileHasAttribute(keystorePath, "creationTime", FileTime.class).toMillis();
        long tsTimeAfter = CommonSpecifications.givenFileHasAttribute(truststorePath, "creationTime", FileTime.class).toMillis();

        Assertions.assertTrue(ksTimeAfter > ksTime, "Keystore was not refreshed");
        Assertions.assertTrue(tsTimeAfter > tsTime, "Truststore was not refreshed");

    }

    @Test
    @DisplayName("Test: Check reserved keys in Bootstrap properties are as expected")
    // GIVEN properties file (/application.properties) read into system properties from classpath
    // WHEN Options dialog opened
    //      AND node (#tabBootstrap) clicked
    // THEN expect all values displayed on table (#tblBootstrap) matches Map extracted (application.properties)
    public void testFX_ReservedKeysBootstrapProperties_MatchExpectations() throws IOException {
        // Given
        Properties props = CommonSpecifications.givenClasspathFileReadIntoSystemProperties("/application.properties");

        // When
        PearlZipSpecifications.whenOptionDialogOpened(this);
        CommonSpecifications.whenNodeClickedByName(this, "#tabBootstrap");

        // Then
        CommonSpecifications.thenTableViewHasValuesMatchingExpectation(this, "#tblBootstrap", (Pair<String,String> p) -> props.getOrDefault(p.getKey(), p.getValue()).equals(p.getValue()));
    }

    @Test
    @DisplayName("Test: Check the expected providers have been loaded")
    // GIVEN properties file (application.properties) read into properties object from classpath
    // WHEN Options dialog opened
    //      AND node (#tabProviders) clicked
    // THEN expect table (#tblProviders) has value of type (ReadService & com.ntak.pearlzip.archive.szjb.pub.SevenZipArchiveService)
    //      AND expect table (#tblProviders) has value of type (ReadService & com.ntak.pearlzip.archive.acc.pub.CommonsCompressArchiveReadService)
    //      AND expect table (#tblProviders) has value of type (WriteService & com.ntak.pearlzip.archive.acc.pub.CommonsCompressArchiveWriteService)
    public void testFX_Providers_MatchExpectations() throws IOException {
        // Given
        Properties props = CommonSpecifications.givenClasspathFileReadIntoSystemProperties("/application.properties");

        // When
        PearlZipSpecifications.whenOptionDialogOpened(this);
        CommonSpecifications.whenNodeClickedByName(this, "#tabProviders");
        sleep(MEDIUM_PAUSE, MILLISECONDS);

        // Then
        CommonSpecifications.thenTableViewHasValuesMatchingExpectation(this, "#tblProviders", (Pair<Boolean,ArchiveService> p) -> p.getValue() instanceof ArchiveReadService && p.getValue().getClass().getCanonicalName().equals("com.ntak.pearlzip.archive.szjb.pub.SevenZipArchiveService"));
        CommonSpecifications.thenTableViewHasValuesMatchingExpectation(this, "#tblProviders", (Pair<Boolean,ArchiveService> p) -> p.getValue() instanceof ArchiveReadService && p.getValue().getClass().getCanonicalName().equals("com.ntak.pearlzip.archive.acc.pub.CommonsCompressArchiveReadService"));
        CommonSpecifications.thenTableViewHasValuesMatchingExpectation(this, "#tblProviders", (Pair<Boolean,ArchiveService> p) -> p.getValue() instanceof ArchiveWriteService && p.getValue().getClass().getCanonicalName().equals("com.ntak.pearlzip.archive.acc.pub.CommonsCompressArchiveWriteService"));
    }

    @Test
    @DisplayName("Test: Load PZAX package successfully")
    // GIVEN system property (configuration.ntak.pearl-zip.provider.priority.com.ntak.pearlzip.archive.zip4j.pub.Zip4jArchiveWriteService) set to (99999)
    //      AND system property (configuration.ntak.pearl-zip.provider.priority.com.ntak.pearlzip.archive.acc.pub.CommonsCompressArchiveWriteService) set to (0)
    //      AND system property (configuration.ntak.pearl-zip.provider.priority.com.ntak.pearlzip.archive.zip4j.pub.Zip4jArchiveReadService) set to (99999)
    //      AND system property (configuration.ntak.pearl-zip.provider.priority.com.ntak.pearlzip.archive.szjb.pub.SevenZipArchiveService) set to (0)
    //      AND system property (configuration.ntak.pearl-zip.version) set to (0.0.4.0)
    //      AND WORKING_APPLICATION_SETTINGS property (configuration.ntak.pearl-zip.version) set to (0.0.4.0)
    //      AND delete folder (~/.pz/manifests)
    //      AND create folder (~/.pz/manifests)
    // WHEN Options dialog opened
    //      AND node (#tabPluginLoader) clicked
    //      AND node (#paneDropArea) double-clicked
    //      AND file (src/test/resources/zip4j-plugin-test.pzax) opened
    //      AND License accepted
    //      AND License accepted
    // THEN a dialog appears with message like "^The library .* has been successfully installed.$"
    //      AND write archive provider is set to (com.ntak.pearlzip.archive.zip4j.pub.Zip4jArchiveWriteService) for zip files
    //      AND read archive provider is set to (com.ntak.pearlzip.archive.zip4j.pub.Zip4jArchiveReadService) for zip files
    public void testFX_LoadPZAXPackage_Success() throws IOException {
        // Given
        CommonSpecifications.givenPropertySet("configuration.ntak.pearl-zip.provider.priority.com.ntak.pearlzip.archive.zip4j.pub.Zip4jArchiveWriteService","99999", System.getProperties());
        CommonSpecifications.givenPropertySet("configuration.ntak.pearl-zip.provider.priority.com.ntak.pearlzip.archive.acc.pub.CommonsCompressArchiveWriteService","0", System.getProperties());
        CommonSpecifications.givenPropertySet("configuration.ntak.pearl-zip.provider.priority.com.ntak.pearlzip.archive.zip4j.pub.Zip4jArchiveReadService","99999", System.getProperties());
        CommonSpecifications.givenPropertySet("configuration.ntak.pearl-zip.provider.priority.com.ntak.pearlzip.archive.szjb.pub.SevenZipArchiveService","0", System.getProperties());
        CommonSpecifications.givenPropertySet("configuration.ntak.pearl-zip.version","0.0.4.0", System.getProperties());
        CommonSpecifications.givenPropertySet("configuration.ntak.pearl-zip.version","0.0.4.0", WORKING_APPLICATION_SETTINGS);

        ArchiveUtil.deleteDirectory(STORE_ROOT.resolve("manifests"), (f) -> false);
        Files.createDirectories(STORE_ROOT.resolve("manifests"));

        // When
        PearlZipSpecifications.whenOptionDialogOpened(this);
        CommonSpecifications.whenNodeClickedByName(this, "#tabPluginLoader");
        CommonSpecifications.whenNodeDoubleClickedByName(this, "#paneDropArea");
        chooseFile( this, Paths.get("src", "test", "resources", "zip4j-plugin-test.pzax").toAbsolutePath());
        PearlZipSpecifications.whenPZAXLicenseAccepted(this);
        PearlZipSpecifications.whenPZAXLicenseAccepted(this);

        // Then
        CommonSpecifications.thenExpectDialogWithMatchingMessage(this, "The library .* has been successfully installed.");

        PearlZipSpecifications.thenExpectActiveWriteService("com.ntak.pearlzip.archive.zip4j.pub.Zip4jArchiveWriteService");
        PearlZipSpecifications.thenExpectActiveReadService("com.ntak.pearlzip.archive.zip4j.pub.Zip4jArchiveReadService");

        // Reset
        CommonSpecifications.givenPropertySet("configuration.ntak.pearl-zip.version","0.0.0.0", System.getProperties());
        CommonSpecifications.givenPropertySet("configuration.ntak.pearl-zip.version","0.0.0.0", WORKING_APPLICATION_SETTINGS);
    }

    @Test
    @DisplayName("Test: Check application.properties got updated by a change to the default archive format in the Options dialog")
    // GIVEN Options dialog opened
    // WHEN combo box "#comboDefaultFormat" has value set as "tar"
    //     AND node (#btnOk) clicked
    //     AND node (#btnNew) clicked
    //     AND node (#mnuNewArchive) clicked
    // THEN expect selected value in combo box (#comboArchiveFormat) is (tar)
    public void testFX_ChangeDefaultArchiveFormat_Success() {
        // Given
        PearlZipSpecifications.whenOptionDialogOpened(this);

        // When
        selectComboBoxEntry(this, lookup("#comboDefaultFormat").queryAs(ComboBox.class), "tar");
        CommonSpecifications.whenNodeClickedByName(this, "#btnOk");
        CommonSpecifications.whenNodeClickedByName(this, "#btnNew");
        CommonSpecifications.whenNodeClickedByName(this, "#mnuNewArchive");

        // Then
        CommonSpecifications.thenPropertyEqualsValue(lookup("#comboArchiveFormat").queryAs(ComboBox.class), ComboBoxBase::getValue, "tar");
    }

    @Test
    @DisplayName("Test: Enable Safe Mode option is updated in properties for next boot up")
    // GIVEN system property (configuration.ntak.pearl-zip.safe-mode) set to (false)
    // WHEN Options dialog opened
    //     AND node (#tabGeneral) clicked
    //     AND node (#checkSafeMode) clicked on
    //     AND node (#btnApply) clicked on
    // THEN expect system property (configuration.ntak.pearl-zip.safe-mode) is set to (true)
    public void testFX_EnableSafeMode_MatchExpectations() {
        // Given
        CommonSpecifications.givenPropertySet("configuration.ntak.pearl-zip.safe-mode","false", System.getProperties());

        // When
        PearlZipSpecifications.whenOptionDialogOpened(this);
        CommonSpecifications.whenNodeClickedByName(this, "#tabGeneral");
        CommonSpecifications.whenNodeClickedByName(this, "#checkSafeMode");
        CommonSpecifications.whenNodeClickedByName(this, "#btnApply");

        // Then
        CommonSpecifications.thenPropertyEqualsValue(System.getProperties(), (p) -> p.getProperty("configuration.ntak.pearl-zip.safe-mode"), "true");
    }

    @Test
    @DisplayName("Test: Enable Show Notification option is updated in properties for next boot up")
    // GIVEN CURRENT_SETTINGS property (configuration.ntak.pearl-zip.show-notification) set to (false)
    // WHEN Options dialog opened
    //     AND node (#tabGeneral) clicked
    //     AND node (#checkShowNotification) clicked
    //     AND node (#btnApply) clicked
    // THEN expect CURRENT_SETTINGS property (configuration.ntak.pearl-zip.show-notification) is set to (true)
    public void testFX_EnableShowNotification_MatchExpectations() {
        // Given
        CommonSpecifications.givenPropertySet("configuration.ntak.pearl-zip.show-notification","false", CURRENT_SETTINGS);

        // When
        PearlZipSpecifications.whenOptionDialogOpened(this);
        CommonSpecifications.whenNodeClickedByName(this, "#tabGeneral");
        CommonSpecifications.whenNodeClickedByName(this, "#checkShowNotification");
        CommonSpecifications.whenNodeClickedByName(this, "#btnApply");

        // Then
        CommonSpecifications.thenPropertyEqualsValue(CURRENT_SETTINGS, (p) -> p.getProperty("configuration.ntak.pearl-zip.show-notification"), "true");

    }

    @Test
    @DisplayName("Test: Change language pack successfully")
    // GIVEN system property (configuration.ntak.pearl-zip.version) set to (0.0.4.0)
    //      AND WORKING_APPLICATION_SETTINGS property (configuration.ntak.pearl-zip.version) set to (0.0.4.0)
    // WHEN Options dialog opened
    //     AND node (#tabPluginLoader) clicked
    //     AND node (#paneDropArea) double-clicked
    //      AND file (src/test/resources/pearl-zip-lang-pack-fr-CA-0.0.4.0.pzax) opened
    //     AND License accepted
    // THEN a dialog appears with message like "The library .* has been successfully installed."
    // WHEN click ok on confirmation dialog
    //     AND Options dialog opened
    //     AND select additional Option tab (LANGUAGES)
    //     AND refresh table (#tblLang) with data
    //     AND select entry (French (Canada)) from table (#tblLang)
    //     AND node (#btnSetLang) clicked
    //     AND click ok on confirmation dialog
    //     AND node (#btnApply) clicked
    //     AND refresh Locale
    // THEN expect system property (configuration.ntak.pearl-zip.locale.lang) is set to (fr)
    //     AND expect system property (configuration.ntak.pearl-zip.locale.country) is set to (CA)
    public void testFX_ChangeLanguagePack_Success() {
        // Given
        CommonSpecifications.givenPropertySet("configuration.ntak.pearl-zip.version","0.0.4.0", System.getProperties());
        CommonSpecifications.givenPropertySet("configuration.ntak.pearl-zip.version","0.0.4.0", WORKING_APPLICATION_SETTINGS);

        // When
        PearlZipSpecifications.whenOptionDialogOpened(this);
        CommonSpecifications.whenNodeClickedByName(this, "#tabPluginLoader");
        CommonSpecifications.whenNodeDoubleClickedByName(this, "#paneDropArea");

        chooseFile( this, Paths.get("src", "test", "resources", "pearl-zip-lang-pack-fr-CA-0.0.4.0.pzax").toAbsolutePath());
        PearlZipSpecifications.whenPZAXLicenseAccepted(this);

        // Then
        CommonSpecifications.thenExpectDialogWithMatchingMessage(this, "The library .* has been successfully installed.");

        // When
        CommonSpecifications.whenButtonClickedOnDialog(this, ButtonType.OK);

        PearlZipSpecifications.whenOptionDialogOpened(this);
        simSelectOptionsAdditionalTab(this, OptionTab.LANGUAGES.getIndex());
        CommonSpecifications.whenTableViewRefreshedWithData(this, "#tblLang", FXCollections.observableArrayList(FXCollections.observableArrayList(InternalContextCache.INTERNAL_CONFIGURATION_CACHE.<Set<Pair<String,Locale>>>getAdditionalConfig(CK_LANG_PACKS).get())));
        FormUtil.selectTableViewEntry(this, (TableView<Pair<String,Locale>>) this.lookup("#tblLang").queryAs(TableView.class), Pair::getKey, "French (Canada)");
        CommonSpecifications.whenNodeClickedByName(this, "#btnSetLang");
        CommonSpecifications.whenButtonClickedOnDialog(this, ButtonType.OK);
        CommonSpecifications.whenNodeClickedByName(this, "#btnApply");
        PearlZipSpecifications.whenPearlZipLocaleIsRefreshed();

        // Then
        CommonSpecifications.thenPropertyEqualsValue(System.getProperties(), (p) -> p.get("configuration.ntak.pearl-zip.locale.lang"), "fr");
        CommonSpecifications.thenPropertyEqualsValue(System.getProperties(), (p) -> p.get("configuration.ntak.pearl-zip.locale.country"), "CA");

        // Reset
        CommonSpecifications.givenPropertySet("configuration.ntak.pearl-zip.version","0.0.0.0", System.getProperties());
        CommonSpecifications.givenPropertySet("configuration.ntak.pearl-zip.version","0.0.0.0", System.getProperties());
    }

    @Test
    @DisplayName("Test: Install plugin with max version less than current version and so fails to install")
    // GIVEN system property (configuration.ntak.pearl-zip.version) set to (0.0.4.0)
    //      AND PearlZip settings (configuration.ntak.pearl-zip.version) set to (0.0.4.0)
    // WHEN Options dialog opened
    //      AND node (#tabPluginLoader) clicked
    //      AND node (#paneDropArea) double-clicked
    //      AND file (src/test/resources/lib-max-version-breach.pzax) selected
    // THEN a dialog appears with message like "Please check the exception stack trace below and ensure the plugin has not been corrupted."
    //      AND dialog exception message contains text like ".*Exception message: PZAX archive requires an older version of PearlZip.*"
    public void testFX_InstallPlugin_OlderVersionRequired_Fail() {
        // Given
        CommonSpecifications.givenPropertySet("configuration.ntak.pearl-zip.version","0.0.4.0", System.getProperties());
        CommonSpecifications.givenPropertySet("configuration.ntak.pearl-zip.version","0.0.4.0", WORKING_APPLICATION_SETTINGS);

        // When
        PearlZipSpecifications.whenOptionDialogOpened(this);
        CommonSpecifications.whenNodeClickedByName(this, "#tabPluginLoader");
        CommonSpecifications.whenNodeDoubleClickedByName(this, "#paneDropArea");
        chooseFile( this, Paths.get("src", "test", "resources", "lib-max-version-breach.pzax").toAbsolutePath());

        // Then
        CommonSpecifications.thenExpectDialogWithMatchingMessage(this, "Please check the exception stack trace below and ensure the plugin has not been corrupted.");
        CommonSpecifications.thenExpectDialogWithMatchingExceptionMessage(this, ".*Exception message: PZAX archive requires an older version of PearlZip.*");
    }

    @Test
    @DisplayName("Test: Install plugin with min version greater than current version and so fails to install")
    // GIVEN system property (configuration.ntak.pearl-zip.version) set to (0.0.4.0)
    //      AND PearlZip settings (configuration.ntak.pearl-zip.version) set to (0.0.4.0)
    // WHEN Options dialog opened
    //      AND node (#tabPluginLoader) clicked
    //      AND node (#paneDropArea) double-clicked
    //      AND file (lib-min-version-breach.pzax) selected
    // THEN a dialog appears with message like "Please check the exception stack trace below and ensure the plugin has not been corrupted."
    //      AND dialog exception message contains text like ".*Exception message: PZAX archive requires a newer version of PearlZip.*"
    public void testFX_InstallPlugin_NewerVersionRequired_Fail() {
        // Given
        CommonSpecifications.givenPropertySet("configuration.ntak.pearl-zip.version","0.0.4.0", System.getProperties());
        CommonSpecifications.givenPropertySet("configuration.ntak.pearl-zip.version","0.0.4.0", WORKING_APPLICATION_SETTINGS);

        // When
        PearlZipSpecifications.whenOptionDialogOpened(this);
        CommonSpecifications.whenNodeClickedByName(this, "#tabPluginLoader");
        CommonSpecifications.whenNodeDoubleClickedByName(this, "#paneDropArea");
        chooseFile( this, Paths.get("src", "test", "resources", "lib-min-version-breach.pzax").toAbsolutePath());

        // Then
        CommonSpecifications.thenExpectDialogWithMatchingMessage(this, "Please check the exception stack trace below and ensure the plugin has not been corrupted.");
        CommonSpecifications.thenExpectDialogWithMatchingExceptionMessage(this, ".*Exception message: PZAX archive requires a newer version of PearlZip.*");
    }

    @Test
    @DisplayName("Test: Install theme, switch theme and uninstall successfully")
    // GIVEN reflectively set static field (PLUGINS_METADATA) in class (ModuleUtil)
    //      AND system property (configuration.ntak.pearl-zip.version) set to (0.0.4.0)
    //      AND WORKING_APPLICATION_SETTINGS settings (configuration.ntak.pearl-zip.version) set to (0.0.4.0)
    // WHEN Options dialog opened
    //      AND node (#tabPluginLoader) clicked
    //      AND node (#paneDropArea) double-clicked
    //      AND file (src/test/resources/modena-orange.pzax) opened
    // THEN a dialog appears with message like "The library .* has been successfully installed."
    // WHEN click ok on confirmation dialog
    //      AND Options dialog opened
    //      AND select additional Option tab (THEMES)
    //      AND select entry (modena-orange) from table (#tblTheme)
    //      AND node (#btnSetTheme) clicked
    // THEN expect system property (configuration.ntak.pearl-zip.theme-name) is set to (modena-orange)
    // WHEN node (#tabProviders) clicked
    //      AND node (#btnPurgePlugin) clicked
    //      AND select entry (modena-orange) from table (#tblManifests)
    //      AND node (#btnPurgeSelected) clicked
    //      AND click Yes on confirmation dialog
    //      AND click ok on confirmation dialog
    //      AND node (#btnCancel) clicked from Node (FrmManifest)
    //      AND node (#btnOk) clicked
    //      AND Options dialog opened
    //      AND select additional Option tab (THEMES)
    // THEN ensure table (#tblTheme) does not contain entry (modena-orange)
    //      AND ensure file (~/.pz/themes/modena-orange) does not exist
    //      AND ensure file (~/.pz/manifests/modena-orange.MF) does not exist
    public void testFX_InstallTheme_Success() throws NoSuchFieldException, IllegalAccessException {
        // Given
        CommonSpecifications.givenSetPrivateStaticField(ModuleUtil.class, "PLUGINS_METADATA", InternalContextCache.INTERNAL_CONFIGURATION_CACHE.<Map<String,PluginInfo>>getAdditionalConfig(CK_PLUGINS_METADATA).get());
        CommonSpecifications.givenPropertySet("configuration.ntak.pearl-zip.version","0.0.4.0", System.getProperties());
        CommonSpecifications.givenPropertySet("configuration.ntak.pearl-zip.version","0.0.4.0", WORKING_APPLICATION_SETTINGS);

        // When
        PearlZipSpecifications.whenOptionDialogOpened(this);
        CommonSpecifications.whenNodeClickedByName(this, "#tabPluginLoader");
        CommonSpecifications.whenNodeDoubleClickedByName(this, "#paneDropArea");
        chooseFile(this, Paths.get("src", "test", "resources", "modena-orange.pzax").toAbsolutePath());
        sleep(LONG_PAUSE, MILLISECONDS);

        // Then
        CommonSpecifications.thenExpectDialogWithMatchingMessage(this, "The library .* has been successfully installed.");

        // When
        CommonSpecifications.whenButtonClickedOnDialog(this, ButtonType.OK);

        PearlZipSpecifications.whenOptionDialogOpened(this);
        simSelectOptionsAdditionalTab(this, OptionTab.THEMES.getIndex());
        selectTableViewEntry(this, this.lookup("#tblTheme").queryAs(TableView.class), (r) -> r, "modena-orange");
        CommonSpecifications.whenNodeClickedByName(this, "#btnSetTheme");
        sleep(LONG_PAUSE, MILLISECONDS);

        // Then
        CommonSpecifications.thenPropertyEqualsValue(System.getProperties(), (p)->p.getProperty("configuration.ntak.pearl-zip.theme-name"), "modena-orange");
        // When
        CommonSpecifications.whenNodeClickedByName(this, "#btnOk");
        PearlZipSpecifications.whenOptionDialogOpened(this);
        CommonSpecifications.whenNodeClickedByName(this, "#tabProviders");
        CommonSpecifications.whenNodeClickedByName(this, "#btnPurgePlugin");
        selectTableViewEntry(this, this.lookup("#tblManifests").queryAs(TableView.class), (r) -> r, "modena-orange");
        CommonSpecifications.whenNodeClickedByName(this, "#btnPurgeSelected");
        CommonSpecifications.whenButtonClickedOnDialog(this, ButtonType.YES);
        CommonSpecifications.whenButtonClickedOnDialog(this, ButtonType.OK);
        CommonSpecifications.whenSubNodeClickedByName(this, () -> this.lookup("#tblManifests").queryAs(TableView.class).getParent(), "#btnCancel");
        CommonSpecifications.whenNodeClickedByName(this, "#btnOk");

        PearlZipSpecifications.whenOptionDialogOpened(this);
        simSelectOptionsAdditionalTab(this, OptionTab.THEMES.getIndex());
        sleep(LONG_PAUSE, MILLISECONDS);

        // Then
        CommonSpecifications.thenTableViewHasValuesNotMatchingExpectation(this, "#tblTheme", (String t) -> t.equals("modena-orange"));
        CommonSpecifications.thenNotExpectFileExists(STORE_ROOT.resolve(Paths.get("themes", "modena-orange")));
        CommonSpecifications.thenNotExpectFileExists(STORE_ROOT.resolve(Paths.get("manifests", "modena-orange.pzax.MF")));
    }

    @Test
    @DisplayName("Test: Purge all plugins successfully")
    // GIVEN system property (configuration.ntak.pearl-zip.version) set to (0.0.4.0)
    //      AND PearlZip settings (configuration.ntak.pearl-zip.version) set to (0.0.4.0)
    // WHEN Options dialog opened
    //     AND node (#tabProviders) clicked
    //     AND node (#btnPurgeAll) double-clicked
    //     AND click Yes on confirmation dialog
    // THEN expect (0) files in the folder (~/.pz/providers)
    public void testFX_PurgeAllPlugin_Success() throws IOException {
        // Given
        CommonSpecifications.givenPropertySet("configuration.ntak.pearl-zip.version","0.0.4.0", System.getProperties());
        CommonSpecifications.givenPropertySet("configuration.ntak.pearl-zip.version","0.0.4.0", WORKING_APPLICATION_SETTINGS);

        // When
        PearlZipSpecifications.whenOptionDialogOpened(this);
        CommonSpecifications.whenNodeClickedByName(this, "#tabProviders");
        CommonSpecifications.whenNodeDoubleClickedByName(this, "#btnPurgeAll");
        CommonSpecifications.whenButtonClickedOnDialog(this, ButtonType.YES);

        // Then
        CommonSpecifications.thenExpectNoFilesInDirectory(STORE_ROOT.resolve(Paths.get("providers")), 0);
    }

    @Test
    @DisplayName("Test: Load PZAX package upgrade successfully")
    // GIVEN system property (configuration.ntak.pearl-zip.version) set to (0.0.4.0)
    //      AND PearlZip settings (configuration.ntak.pearl-zip.version) set to (0.0.4.0)
    // WHEN Options dialog opened
    //      AND node (#tabPluginLoader) clicked
    //      AND node (#paneDropArea) double-clicked
    //      AND file (src/test/resources/pearl-zip-archive-zip4j-INITIAL.pzax) selected
    //      AND License accepted
    //      AND License accepted
    //      AND click ok on confirmation dialog
    // THEN expect (2) files in the folder (~/.pz/providers/zip4j)
    // WHEN Options dialog opened
    //      AND node (#tabPluginLoader) clicked
    //      AND node (#paneDropArea) double-clicked
    //      AND file (src/test/resources/pearl-zip-archive-zip4j-UPGRADE.pzax) selected
    //      AND License accepted
    //      AND License accepted
    //      AND click ok on confirmation dialog
    // THEN expect (2) files in the folder (~/.pz/providers/zip4j)
    //      AND expect file (pearl-zip-archive-zip4j-0.0.0.8.jar) exist in target location
    //      AND expect file (zip4j-2.10.0.jar) exist in target location
    public void testFX_LoadPZAXPackage_Upgrade_Success() throws Exception {
        // Given
        CommonSpecifications.givenPropertySet("configuration.ntak.pearl-zip.version","0.0.4.0", System.getProperties());
        CommonSpecifications.givenPropertySet("configuration.ntak.pearl-zip.version","0.0.4.0", WORKING_APPLICATION_SETTINGS);

        // When
        PearlZipSpecifications.whenOptionDialogOpened(this);
        CommonSpecifications.whenNodeClickedByName(this, "#tabPluginLoader");
        CommonSpecifications.whenNodeDoubleClickedByName(this, "#paneDropArea");
        chooseFile(this, Paths.get("src", "test", "resources", "pearl-zip-archive-zip4j-INITIAL.pzax").toAbsolutePath());
        PearlZipSpecifications.whenPZAXLicenseAccepted(this);
        PearlZipSpecifications.whenPZAXLicenseAccepted(this);
        CommonSpecifications.whenButtonClickedOnDialog(this, ButtonType.OK);

        // Then
        CommonSpecifications.thenExpectNoFilesInDirectory(STORE_ROOT.resolve(Paths.get("providers", "zip4j")), 2);

        // When
        PearlZipSpecifications.whenOptionDialogOpened(this);
        CommonSpecifications.whenNodeClickedByName(this, "#tabPluginLoader");
        CommonSpecifications.whenNodeDoubleClickedByName(this, "#paneDropArea");
        chooseFile(this, Paths.get("src", "test", "resources", "pearl-zip-archive-zip4j-UPGRADE.pzax").toAbsolutePath());
        PearlZipSpecifications.whenPZAXLicenseAccepted(this);
        PearlZipSpecifications.whenPZAXLicenseAccepted(this);
        CommonSpecifications.whenButtonClickedOnDialog(this, ButtonType.OK);

        // Then
        CommonSpecifications.thenExpectNoFilesInDirectory(STORE_ROOT.resolve(Paths.get("providers", "zip4j")), 2);
        CommonSpecifications.thenExpectFileExists(STORE_ROOT.resolve(Paths.get("providers", "zip4j", "pearl-zip-archive-zip4j-0.0.0.8.jar")));
        CommonSpecifications.thenExpectFileExists(STORE_ROOT.resolve(Paths.get("providers", "zip4j", "zip4j-2.10.0.jar")));
    }

    @Test
    @DisplayName("Test: Add store repository successfully")
    // GIVEN Options dialog opened
    //      AND select additional Option tab (STORE)
    // WHEN node (#btnAddStore) clicked
    //      AND node (#txtBoxName) clicked
    //      AND type "test-repo"
    //      AND node (#txtBoxURL) clicked
    //      AND type (system property (CNS_NTAK_PEARL_ZIP_JDBC_URL))
    //      AND node (#txtBoxUsername) clicked
    //      AND type (system property (CNS_NTAK_PEARL_ZIP_JDBC_USER))
    //      AND node (#txtBoxPassword) clicked
    //      AND type (system property (CNS_NTAK_PEARL_ZIP_JDBC_PASSWORD))
    //      AND node (#btnAdd) clicked
    // THEN expect file (test-repo) exist in target location
    public void testFX_AddStore_Success() {
        // Given
        PearlZipSpecifications.whenOptionDialogOpened(this);
        simSelectOptionsAdditionalTab(this, OptionTab.STORE.getIndex());

        // When
        CommonSpecifications.whenNodeClickedByName(this, "#btnAddStore");
        CommonSpecifications.whenNodeClickedByName(this, "#txtBoxName");
        TypeUtil.typeString(this, "test-repo");
        CommonSpecifications.whenNodeClickedByName(this, "#txtBoxURL");
        TypeUtil.typeString(this, System.getProperty(CNS_NTAK_PEARL_ZIP_JDBC_URL));
        CommonSpecifications.whenNodeClickedByName(this, "#txtBoxUsername");
        TypeUtil.typeString(this, System.getProperty(CNS_NTAK_PEARL_ZIP_JDBC_USER));
        CommonSpecifications.whenNodeClickedByName(this, "#txtBoxPassword");
        TypeUtil.typeString(this, System.getProperty(CNS_NTAK_PEARL_ZIP_JDBC_PASSWORD));
        CommonSpecifications.whenSubNodeClickedByName(this, ()->this.lookup("#lblURL").queryAs(Label.class).getParent(), "#btnAdd");
        sleep(LONG_PAUSE, MILLISECONDS);

        // Then
        CommonSpecifications.retryRetrievalForDuration(RETRIEVAL_TIMEOUT_MILLIS, () -> Files.exists(STORE_ROOT.resolve( Paths.get("repository", "test-repo")))?new Object():null);
        CommonSpecifications.thenExpectFileExists(STORE_ROOT.resolve( Paths.get("repository", "test-repo")));
    }

    @Test
    @DisplayName("Test: Edit store repository successfully")
    // GIVEN Copy file (src/test/resources/test-repo) to (~/.pz/repository/test-repo) not using context menu
    //      AND repo (~/.pz/repository/test-repo) loaded into PearlZip
    // WHEN Options dialog opened
    //      AND select additional Option tab (STORE)
    //     AND select entry (test-repo) from table (#tblStore)
    //      AND node (#btnEditStore) clicked
    //      AND node (#txtBoxName) clicked
    //      AND type "2"
    //      AND node (#btnEdit) clicked
    // THEN expect file (test-repo2) exist in target location
    public void testFX_EditStore_Success() throws IOException {
        // Given
        Path targetFile = STORE_ROOT.resolve(Paths.get("repository","test-repo"));
        Files.copy(Paths.get("src","test","resources","test-repo").toAbsolutePath(), targetFile);
        loadStoreRepoDetails(targetFile);

        // When
        PearlZipSpecifications.whenOptionDialogOpened(this);
        simSelectOptionsAdditionalTab(this, OptionTab.STORE.getIndex());
        FormUtil.selectTableViewEntry(this, this.lookup("#tblStore").queryTableView(), StoreRepoDetails::name, "test-repo");
        CommonSpecifications.whenNodeClickedByName(this, "#btnEditStore");
        CommonSpecifications.whenNodeClickedByName(this, "#txtBoxName");
        TypeUtil.typeString(this, "2");
        CommonSpecifications.whenNodeClickedByName(this, "#btnEdit");

        // Then
        CommonSpecifications.thenExpectFileExists(STORE_ROOT.resolve( Paths.get("repository", "test-repo2")));
    }
}
