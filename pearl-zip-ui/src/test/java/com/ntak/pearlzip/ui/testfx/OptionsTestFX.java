/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.testfx;

import com.ntak.pearlzip.archive.acc.pub.CommonsCompressArchiveReadService;
import com.ntak.pearlzip.archive.acc.pub.CommonsCompressArchiveWriteService;
import com.ntak.pearlzip.archive.pub.ArchiveReadService;
import com.ntak.pearlzip.archive.pub.ArchiveService;
import com.ntak.pearlzip.archive.pub.ArchiveWriteService;
import com.ntak.pearlzip.archive.szjb.pub.SevenZipArchiveService;
import com.ntak.pearlzip.ui.constants.ZipConstants;
import com.ntak.pearlzip.ui.model.ZipState;
import com.ntak.pearlzip.ui.util.AbstractPearlZipTestFX;
import com.ntak.pearlzip.ui.util.JFXUtil;
import com.ntak.pearlzip.ui.util.PearlZipFXUtil;
import com.ntak.testfx.FormUtil;
import javafx.geometry.Point2D;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.web.WebView;
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
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.ui.UITestSuite.clearDirectory;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.PearlZipFXUtil.lookupArchiveInfo;
import static com.ntak.pearlzip.ui.util.PearlZipFXUtil.simOpenArchive;
import static com.ntak.testfx.NativeFileChooserUtil.chooseFile;
import static com.ntak.testfx.TestFXConstants.PLATFORM;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class OptionsTestFX extends AbstractPearlZipTestFX {
    private Path tempOSDir;
    private Path tempPZDir;

    /*
     *  Test cases:
     *  + Clear cache with an open temporary archive. Temporary files except for open archive is removed
     *  + Clear cache with a saved archive. All temp files are removed
     *  + Check reserved keys in Bootstrap properties are as expected
     *  + Check the expected providers have been loaded
     *  + Load pzax package successfully
     *  + Check application.properties got updated by a change to the default archive format in the Options dialog
     */

    @Override
    public void start(Stage stage) throws IOException, TimeoutException {
        ZipConstants.STORE_ROOT = Paths.get(System.getProperty("user.home"), ".pz");
        System.setProperty(CNS_NTAK_PEARL_ZIP_NO_FILES_HISTORY, "5");
        System.setProperty(String.format(CNS_PROVIDER_PRIORITY_ROOT_KEY,
                                         "com.ntak.pearlzip.archive.zip4j.pub.Zip4jArchiveReadService"), "5");
        System.setProperty(String.format(CNS_PROVIDER_PRIORITY_ROOT_KEY,
                                         "com.ntak.pearlzip.archive.zip4j.pub.Zip4jArchiveWriteService"), "5");
        ZipConstants.LOCAL_TEMP =
                Paths.get(Optional.ofNullable(System.getenv("TMPDIR"))
                                  .orElse(STORE_ROOT.toString()));
        ZipConstants.STORE_TEMP = Paths.get(STORE_ROOT.toAbsolutePath()
                                                      .toString(), "temp");

        Files.list(STORE_TEMP)
             .filter(Files::isRegularFile)
             .forEach(f -> {
                 try {
                     Files.deleteIfExists(f);
                 } catch(IOException e) {
                 }
             });

        PearlZipFXUtil.initialise(stage,
                                  List.of(new CommonsCompressArchiveWriteService()),
                                  List.of(new SevenZipArchiveService(), new CommonsCompressArchiveReadService()),
                                  Paths.get(STORE_TEMP.toAbsolutePath().toString(), String.format("a%d.zip",
                                                                                                  System.currentTimeMillis()))
        );

        tempOSDir = Paths.get(LOCAL_TEMP.toAbsolutePath()
                                        .toString(), String.format("pz%d", System.currentTimeMillis()));
        sleep(250);
        tempPZDir = Paths.get(STORE_TEMP.toAbsolutePath()
                                        .toString(), String.format("pz%d", System.currentTimeMillis()));

        Files.createFile(Paths.get(STORE_TEMP.toAbsolutePath().toString(), "a1234567890.zip"));

        Files.createDirectories(tempOSDir);
        Files.createDirectories(tempPZDir);

        Files.createFile(Paths.get(tempOSDir.toAbsolutePath().toString(), "nestedFile.tar"));
        Files.createFile(Paths.get(tempPZDir.toAbsolutePath().toString(), "anotherNestedFile.tar"));
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
    public void testFX_ClearCacheTemporaryArchiveOpen_MatchExpectations() throws IOException {
        // Verify initial state
        Assertions.assertEquals(2,
                                Files.list(STORE_TEMP)
                                     .filter(Files::isRegularFile)
                                     .count(),
                                "Initial files have not been setup");

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

        Assertions.assertTrue(Files.exists(tempOSDir), "Temp directory was not initialised");
        Assertions.assertTrue(Files.exists(Paths.get(tempOSDir.toAbsolutePath().toString(), "nestedFile.tar")),
                              "Temp file was not initialised");

        // Navigate to the clear cache option
        this.clickOn(Point2D.ZERO.add(160, 10))
            .clickOn(Point2D.ZERO.add(160, 30))
            .clickOn("#tabGeneral")
            .clickOn("#btnClearCache");

        DialogPane dialogPane = lookup(".dialog-pane").query();
        clickOn(dialogPane.lookupButton(ButtonType.YES));
        sleep(50, MILLISECONDS);

        // Check the outcomes are as expected
        Assertions.assertTrue(Files.exists(fileToBeKept), String.format("File %s was deleted unexpectedly", fileToBeKept));
        Assertions.assertFalse(Files.exists(fileToBeDeleted), String.format("File %s was kept unexpectedly", fileToBeDeleted));
        Assertions.assertFalse(Files.exists(tempOSDir), "Temporary directory in OS ephemeral store was not deleted");
        Assertions.assertFalse(Files.exists(Paths.get(tempOSDir.toAbsolutePath().toString(), "nestedFile.tar")),
                               "temp file in OS ephemeral storage was not deleted");
    }

    @Test
    @DisplayName("Test: Clear cache with a saved archive. All temp files are removed")
    public void testFX_ClearCacheSavedArchiveOpen_MatchExpectations() throws IOException {
        // Verify initial state
        final List<Path> filesToBeDeleted = new LinkedList<>();
        filesToBeDeleted.addAll(Files.list(STORE_TEMP).filter(Files::isRegularFile).filter(f->JFXUtil.getMainStageInstances().stream().noneMatch(s->s.getTitle().contains(f.toAbsolutePath().toString()))).collect(Collectors.toList()));
        filesToBeDeleted.addAll(Files.list(tempPZDir).filter(Files::isRegularFile).filter(f->JFXUtil.getMainStageInstances().stream().noneMatch(s->s.getTitle().contains(f.toAbsolutePath().toString()))).collect(Collectors.toList()));
        Assertions.assertEquals(2, filesToBeDeleted.size(), "Initial files have not been setup");

        Assertions.assertTrue(Files.exists(tempOSDir), "Temp directory was not initialised");
        Assertions.assertTrue(Files.exists(Paths.get(tempOSDir.toAbsolutePath().toString(), "nestedFile.tar")),
                              "Temp file was not initialised");
        Assertions.assertTrue(Files.exists(tempPZDir), "Temp directory (.pz) was not initialised");
        Assertions.assertTrue(Files.exists(Paths.get(tempPZDir.toAbsolutePath().toString(), "anotherNestedFile.tar")),
                              "Temp file (.pz) was not initialised");

        // Open existing archive in current window
        clickOn(Point2D.ZERO.add(110, 10))
                .clickOn(Point2D.ZERO.add(110, 80));
        final Path archivePath = Paths.get("src", "test", "resources", "test.zip")
                                      .toAbsolutePath();
        // Via Sys menu
        simOpenArchive(this, archivePath, false, false);
        sleep(500, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(),"Expected archive " +
                "was not present");

        DialogPane dialogPane = lookup(".dialog-pane").query();
        clickOn(dialogPane.lookupButton(ButtonType.NO));
        sleep(250, MILLISECONDS);

        // Navigate to the clear cache option
        this.clickOn(Point2D.ZERO.add(160, 10))
            .clickOn(Point2D.ZERO.add(160, 30))
            .clickOn("#tabGeneral")
            .clickOn("#btnClearCache");

        dialogPane = lookup(".dialog-pane").query();
        clickOn(dialogPane.lookupButton(ButtonType.YES));
        sleep(250, MILLISECONDS);

        // Check the outcomes are as expected
        Assertions.assertTrue(filesToBeDeleted.stream().noneMatch(Files::exists), "Some temp files were not deleted");
        Assertions.assertFalse(Files.exists(tempOSDir), "Temporary directory in OS ephemeral store was not deleted");
        Assertions.assertFalse(Files.exists(Paths.get(tempOSDir.toAbsolutePath().toString(), "nestedFile.tar")),
                               "temp file in pz ephemeral storage was not deleted");
        Assertions.assertFalse(Files.exists(tempPZDir), "Temporary directory in OS ephemeral store was not " +
                "deleted");
        Assertions.assertFalse(Files.exists(Paths.get(tempPZDir.toAbsolutePath().toString(), "anotherNestedFile.tar")),
                               "temp file in pz ephemeral storage was not deleted");

    }

    @Test
    @DisplayName("Test: Check reserved keys in Bootstrap properties are as expected")
    public void testFX_ReservedKeysBootstrapProperties_MatchExpectations() throws IOException {
        // Initialise properties
        Properties bootstrap = new Properties();
        bootstrap.load(OptionsTestFX.class.getResourceAsStream("/application.properties"));
        bootstrap.entrySet().stream().forEach(e->System.setProperty(e.getKey().toString(), e.getValue().toString()));

        // Navigate to the Bootstrap properties tab
        this.clickOn(Point2D.ZERO.add(160, 10))
            .clickOn(Point2D.ZERO.add(160, 30))
            .clickOn("#tabBootstrap");

        // Retrieve properties TableView
        TableView<Pair<String,String>> propsGrid = lookup("#tblBootstrap").queryAs(TableView.class);
        List<Pair<String,String>> props = propsGrid.getItems();

        // Validate keys have expected values
        Map<String,String> actuals = new HashMap<>();
        props.stream().forEach(p->actuals.put(p.getKey(), p.getValue()));

        Files.lines(Paths.get(System.getProperty("user.home"), ".pz", "rk"))
             .forEach(k->Assertions.assertTrue(actuals.containsKey(k), String.format("Key %s does not exist in actuals",
                                                                                     k)));

        Files.lines(Paths.get(OptionsTestFX.class.getResource("/application.properties").getPath()));
    }

    @Test
    @DisplayName("Test: Check the expected providers have been loaded")
    public void testFX_Providers_MatchExpectations() throws IOException {
        // Initialise properties
        Properties bootstrap = new Properties();
        bootstrap.load(OptionsTestFX.class.getResourceAsStream("/application.properties"));
        bootstrap.entrySet().stream().forEach(e->System.setProperty(e.getKey().toString(), e.getValue().toString()));

        // Navigate to the Providers properties tab
        this.clickOn(Point2D.ZERO.add(160, 10))
            .clickOn(Point2D.ZERO.add(160, 30))
            .clickOn("#tabProviders");
        sleep(50, MILLISECONDS);

        // Retrieve properties TableView
        TableView<Pair<Boolean,ArchiveService>> propsGrid = lookup("#tblProviders").queryAs(TableView.class);
        List<Pair<Boolean,ArchiveService>> props = propsGrid.getItems();

        // Validate expected services
        Assertions.assertTrue(props.stream()
                                   .map(Pair::getValue)
                                   .anyMatch(s -> s instanceof ArchiveReadService && s.getClass()
                                                                                      .getCanonicalName()
                                                                                      .equals("com.ntak.pearlzip.archive.szjb.pub.SevenZipArchiveService")),
                              "No 7-Zip Read Service");
        Assertions.assertTrue(props.stream()
                                   .map(Pair::getValue)
                                   .anyMatch(s -> s instanceof ArchiveReadService && s.getClass()
                                                                                      .getCanonicalName()
                                                                                      .equals("com.ntak.pearlzip.archive.acc.pub.CommonsCompressArchiveReadService")),
                              "No Apache Read Service");
        Assertions.assertTrue(props.stream()
                                   .map(Pair::getValue)
                                   .anyMatch(s -> s instanceof ArchiveWriteService && s.getClass()
                                                                                       .getCanonicalName()
                                                                                       .equals("com.ntak.pearlzip.archive.acc.pub.CommonsCompressArchiveWriteService")),
                              "No Write Service");
    }

    @Test
    @DisplayName("Test: Load PZAX package successfully")
    public void testFX_LoadPZAXPackage_Success() throws IOException {
        Path providersDir = Paths.get(STORE_ROOT.toAbsolutePath()
                                                .toString(), "providers");

        // Back up existing libraries
        Files.list(providersDir)
             .filter(f -> f.getFileName()
                           .toString()
                           .matches("pearl-zip-archive-zip4j-.*.jar") || f.getFileName()
                                                                          .toString()
                                                                          .matches("zip4j-.*.jar"))
             .collect(Collectors.toList())
             .forEach(p -> {
                 try {
                     Files.move(p,
                                Paths.get(String.format("%s.backup", p.toAbsolutePath())),
                                StandardCopyOption.REPLACE_EXISTING);
                 } catch(IOException e) {
                 }
             });

        Path pzaxPackage = Paths.get("src", "test", "resources", "zip4j-plugin-test.pzax")
                                .toAbsolutePath();

        this.clickOn(Point2D.ZERO.add(160, 10))
            .clickOn(Point2D.ZERO.add(160, 30))
            .clickOn("#tabPluginLoader")
            .sleep(50, MILLISECONDS)
            .doubleClickOn("#paneDropArea");

        chooseFile(PLATFORM, this, pzaxPackage);

        WebView webView = lookup("#webLicense").queryAs(WebView.class);
        //ScrollBar scroll = (ScrollBar)webView.lookup(".scroll-bar:vertical");
        this.drag("#webLicense", MouseButton.PRIMARY) // First license agreement
            .moveBy(0, 400)
            .sleep(2500, MILLISECONDS)
            .drop()
            .clickOn("#btnAccept")
            .sleep(150, MILLISECONDS)
            .drag("#webLicense", MouseButton.PRIMARY) // Second license agreement
            .moveBy(0, 400)
            .sleep(1500, MILLISECONDS)
            .drop()
            .clickOn("#btnAccept")
            .sleep(300, MILLISECONDS);

        DialogPane dialogPane = lookup(".dialog-pane").queryAs(DialogPane.class);
        Assertions.assertEquals(dialogPane.getContentText(),
                                String.format("The library %s has been successfully installed.",
                                              pzaxPackage.toAbsolutePath()));

        // Check loaded providers
        final String zip4jWriteCanonicalName = "com.ntak.pearlzip.archive.zip4j.pub.Zip4jArchiveWriteService";
        Assertions.assertTrue(
                ZipState.getWriteProviders()
                        .stream()
                        .map(ArchiveWriteService::getClass)
                        .anyMatch(k -> k.getCanonicalName()
                                        .equals(zip4jWriteCanonicalName)),
                "New library write service was not detected successfully");

        final String zip4jReadCanonicalName = "com.ntak.pearlzip.archive.zip4j.pub.Zip4jArchiveReadService";
        Assertions.assertTrue(
                ZipState.getReadProviders()
                        .stream()
                        .map(ArchiveReadService::getClass)
                        .anyMatch(k -> k.getCanonicalName()
                                        .equals(zip4jReadCanonicalName)),
                "New library read service was not detected successfully");

        Assertions.assertEquals(zip4jWriteCanonicalName,
                                ZipState.getWriteArchiveServiceForFile("arbitrary.zip")
                                        .get()
                                        .getClass()
                                        .getCanonicalName(),
                                "Write service priority was not as expected");
        Assertions.assertEquals(zip4jReadCanonicalName,
                                ZipState.getReadArchiveServiceForFile("arbitrary.zip")
                                        .get()
                                        .getClass()
                                        .getCanonicalName(),
                                "Read service priority was not as expected");

        // Delete installed libraries
        Files.list(providersDir)
             .filter(f -> f.getFileName()
                           .toString()
                           .matches("pearl-zip-archive-zip4j-.*.jar") || f.getFileName()
                                                                          .toString()
                                                                          .matches("zip4j-.*.jar"))
             .collect(Collectors.toList())
             .forEach(p -> {
                 try {
                     Files.deleteIfExists(p);
                 } catch(IOException e) {
                 }
             });

        // Restore original libraries
        Files.list(providersDir)
             .filter(f -> f.getFileName()
                           .toString()
                           .matches("pearl-zip-archive-zip4j-.*.jar.backup") || f.getFileName()
                                                                                 .toString()
                                                                                 .matches("zip4j-.*.jar.backup"))
             .collect(Collectors.toList())
             .forEach(p -> {
                 try {
                     Files.move(p,
                                Paths.get(p.toAbsolutePath()
                                           .toString()
                                           .replace(".backup", "")),
                                StandardCopyOption.REPLACE_EXISTING);
                 } catch(IOException e) {
                 }
             });
    }

    @Test
    @DisplayName("Test: Check application.properties got updated by a change to the default archive format in the Options dialog")
    public void testFX_ChangeDefaultArchiveFormat_Success() throws IOException {
        //  Back up existing application.properties file
        Path appPropsPath = Paths.get(STORE_ROOT.toAbsolutePath()
                                                .toString(), "application.properties");
        Path tempPropsPath = Paths.get(STORE_ROOT.toAbsolutePath()
                                                 .toString(), "application.properties.backup");
        Files.copy(appPropsPath, tempPropsPath, StandardCopyOption.REPLACE_EXISTING);

        try {
            this.clickOn(Point2D.ZERO.add(160, 10))
                .clickOn(Point2D.ZERO.add(160, 30));

            ComboBox<String> combo = lookup("#comboDefaultFormat").queryAs(ComboBox.class);
            FormUtil.selectComboBoxEntry(this, combo, "tar");

            this.clickOn("#btnOk")
                .sleep(250, MILLISECONDS);

            Assertions.assertTrue(Files.readAllLines(appPropsPath)
                                       .stream()
                                       .anyMatch(l -> l.equals("configuration.ntak.pearl-zip.default-format=tar")),
                                  "Properties file was not updated as expected"
            );

            this.clickOn("#btnNew")
                .sleep(150, MILLISECONDS)
                .clickOn("#mnuNewArchive")
                .sleep(150, MILLISECONDS);

            Assertions.assertEquals("tar",
                                    this.lookup("#comboArchiveFormat")
                                        .queryAs(ComboBox.class)
                                        .getSelectionModel()
                                        .getSelectedItem(),
                                    "Tar archive was not selected by default on the new page");

        } finally {
            Files.move(tempPropsPath, appPropsPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
