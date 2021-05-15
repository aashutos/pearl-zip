/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.util;

import com.ntak.pearlzip.archive.pub.ArchiveReadService;
import com.ntak.pearlzip.archive.pub.ArchiveWriteService;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.constants.ZipConstants;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.model.ZipState;
import com.ntak.pearlzip.ui.pub.FrmAboutController;
import com.ntak.pearlzip.ui.pub.FrmMainController;
import com.ntak.pearlzip.ui.pub.SysMenuController;
import com.ntak.pearlzip.ui.pub.ZipLauncher;
import com.ntak.testfx.ExpectationFileVisitor;
import com.ntak.testfx.FormUtil;
import com.ntak.testfx.NativeFileChooserUtil;
import de.jangassen.MenuToolkit;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TableRow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.junit.jupiter.api.Assertions;
import org.testfx.api.FxRobot;
import org.testfx.util.WaitForAsyncUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static com.ntak.pearlzip.archive.constants.LoggingConstants.LOG_BUNDLE;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ResourceConstants.DSV;
import static com.ntak.pearlzip.ui.constants.ResourceConstants.SSV;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.testfx.TestFXConstants.PLATFORM;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.fail;

public class PearlZipFXUtil {
    public static void simUp(FxRobot robot) {
        robot.clickOn("#btnUp", MouseButton.PRIMARY);
        robot.sleep(50, MILLISECONDS);
    }
    
    public static void simNewArchive(FxRobot robot, Path archive) throws IOException {
        simNewArchive(robot, archive, true);
    }
    public static void simNewArchive(FxRobot robot, Path archive, boolean init) throws IOException {
        if (init) {
            robot.clickOn("#btnNew", MouseButton.PRIMARY);
            robot.sleep(50, MILLISECONDS);
        }

        final String[] nameSplit = DSV.split(archive.getFileName()
                                                   .toString());
        final String archiveFormat = nameSplit[nameSplit.length-1];
        ComboBox<String> cmbArchiveFormat = FormUtil.lookupNode(s -> s.isShowing() && s.getTitle().equals("Create new archive..."), "#comboArchiveFormat");
        FormUtil.selectComboBoxEntry(robot, cmbArchiveFormat, archiveFormat);

        robot.clickOn("#btnCreate", MouseButton.PRIMARY);
        robot.sleep(50, MILLISECONDS);

        Files.deleteIfExists(archive);
        NativeFileChooserUtil.chooseFile(PLATFORM, robot, archive);
        robot.sleep(50, MILLISECONDS);

        Assertions.assertTrue(Files.exists(archive), "Archive was not created");
    }

    public static void simAddFolder(FxRobot robot, Path folder) {
        robot.clickOn("#btnAdd", MouseButton.PRIMARY);
        robot.sleep(50, MILLISECONDS);

        robot.clickOn("#mnuAddDir", MouseButton.PRIMARY);
        robot.sleep(50, MILLISECONDS);

        NativeFileChooserUtil.chooseFolder(PLATFORM, robot, folder);
        robot.sleep(50, MILLISECONDS);
    }

    public static void simAddFile(FxRobot robot, Path file) {
        robot.clickOn("#btnAdd", MouseButton.PRIMARY);
        robot.sleep(50, MILLISECONDS);

        robot.clickOn("#mnuAddFile", MouseButton.PRIMARY);
        robot.sleep(50, MILLISECONDS);

        NativeFileChooserUtil.chooseFile(PLATFORM, robot, file);
        robot.sleep(50, MILLISECONDS);
    }

    public static Optional<TableRow<FileInfo>> simTraversalArchive(FxRobot robot, String archiveName,
            String tableName, Consumer<TableRow<FileInfo>> callback,
            String... identifiers) {
        String root = "";
        for (int i = 0; i < identifiers.length; i++) {
            Optional<TableRow<FileInfo>> selectedRow = FormUtil.selectTableViewEntry(robot,
                                                                                     FormUtil.lookupNode((s) -> s.getScene()
                                                                                                                 .lookup(tableName) != null && s.getTitle().contains(archiveName),
                                                                                                         tableName),
                                                                                     FileInfo::getFileName,
                                                                                     String.format("%s%s", root,
                                                                                                   identifiers[i]));
            Assertions.assertTrue(selectedRow.isPresent(), "No row was selected");
            if (identifiers.length == i+1) {
                robot.sleep(50, MILLISECONDS);
                callback.accept(selectedRow.get());
                return selectedRow;
            }
            robot.doubleClickOn(selectedRow.get(), MouseButton.PRIMARY);
            root += String.format("%s/",identifiers[i]);
            robot.sleep(50, MILLISECONDS);
        }

        return Optional.empty();
    }

    public static void simExtractFile(FxRobot robot, Path file) {
        robot.clickOn("#btnExtract", MouseButton.PRIMARY);
        robot.sleep(50, MILLISECONDS);

        robot.clickOn("#mnuExtractSelectedFile", MouseButton.PRIMARY);
        robot.sleep(50, MILLISECONDS);

        NativeFileChooserUtil.chooseFile(PLATFORM, robot, file);
        robot.sleep(50, MILLISECONDS);
    }

    public static void simExtractAll(FxRobot robot, Path targetDir) {
        robot.clickOn("#btnExtract", MouseButton.PRIMARY);
        robot.sleep(50, MILLISECONDS);

        robot.clickOn("#mnuExtractAll", MouseButton.PRIMARY);
        robot.sleep(50, MILLISECONDS);

        NativeFileChooserUtil.chooseFile(PLATFORM, robot, targetDir);
        robot.sleep(50, MILLISECONDS);
    }

    public static void initialise(Stage stage, List<ArchiveWriteService> writeServices,
            List<ArchiveReadService> readServices) throws IOException, TimeoutException {

        // Set up global constants
        ZipConstants.PRIMARY_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);
        ZipConstants.RECENT_FILE = Paths.get(System.getProperty("user.home"), ".pz", "rf");
        String appName = System.getProperty(CNS_NTAK_PEARL_ZIP_APP_NAME, "PearlZip");
        String version = System.getProperty(CNS_NTAK_PEARL_ZIP_VERSION, "0.0.0.0");

        initialiseMenu();

        // Load services
        for (ArchiveReadService readService : readServices) {
            ZipState.addArchiveProvider(readService);
        }

        for (ArchiveWriteService writeService : writeServices) {
            ZipState.addArchiveProvider(writeService);
        }

        // Load main form
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(ZipLauncher.class.getResource("/frmMain.fxml"));
        loader.setResources(LOG_BUNDLE);
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        FrmMainController controller = loader.getController();

        // Set up initial archive
        Path initialFile = Paths.get(Files.createTempDirectory("pz").toString(), "temp.zip");
        FXArchiveInfo fxArchiveInfo = initFxArchiveInfo(initialFile);
        controller.initData(stage, fxArchiveInfo);
        fxArchiveInfo.setMainController(controller);
        stage.setTitle(resolveTextKey(TITLE_FILE_PATTERN, appName, version,
                                      fxArchiveInfo.getArchivePath()));
        stage.show();
        stage.toFront();

        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS, stage.showingProperty());
    }

    private static FXArchiveInfo initFxArchiveInfo(Path archive) throws IOException {
        Files.deleteIfExists(archive);
        Files.createFile(archive);
        ArchiveReadService readService = ZipState.getReadArchiveServiceForFile(archive.getFileName().toString()).get();
        ArchiveWriteService writeService =
                ZipState.getWriteArchiveServiceForFile(archive.getFileName().toString()).orElse(null);

        return new FXArchiveInfo(archive.toAbsolutePath().toString(), readService,
                                 writeService);
    }

    public static void initialiseMenu() throws IOException {
        MenuToolkit MENU_TOOLKIT = MenuToolkit.toolkit(Locale.getDefault());

        // Create a new System Menu
        String appName = System.getProperty(CNS_NTAK_PEARL_ZIP_APP_NAME, "PearlZip");
        MenuBar sysMenu = new MenuBar();

        // Setting about form...
        FXMLLoader aboutLoader = new FXMLLoader();
        aboutLoader.setLocation(ZipLauncher.class.getClassLoader().getResource("frmAbout.fxml"));
        aboutLoader.setResources(LOG_BUNDLE);
        VBox abtRoot = aboutLoader.load();
        FrmAboutController abtController = aboutLoader.getController();
        Scene abtScene = new Scene(abtRoot);
        Stage aboutStage = new Stage();
        abtController.initData(aboutStage);
        aboutStage.setScene(abtScene);
        aboutStage.initStyle(StageStyle.UNDECORATED);

        sysMenu.setUseSystemMenuBar(true);
        sysMenu.getMenus().add(MENU_TOOLKIT.createDefaultApplicationMenu(appName, aboutStage));

        // Add some more Menus...
        FXMLLoader menuLoader = new FXMLLoader();
        menuLoader.setLocation(ZipLauncher.class.getClassLoader().getResource("sysmenu.fxml"));
        menuLoader.setResources(LOG_BUNDLE);
        MenuBar additionalMenu = menuLoader.load();
        SysMenuController menuController = menuLoader.getController();
        menuController.initData();
        sysMenu.getMenus().addAll(additionalMenu.getMenus());
        sysMenu.setId("MenuBar");

        // Use the menu sysMenu for all stages including new ones
        MENU_TOOLKIT.setGlobalMenuBar(sysMenu);
    }

    public static Optional<FXArchiveInfo> lookupArchiveInfo(String archiveName) {
        return Optional.of((FXArchiveInfo) Stage.getWindows()
                       .stream()
                       .map(Stage.class::cast)
                       .filter((s)->s.getTitle().contains(archiveName))
                       .findFirst()
                       .get()
                       .getUserData());
    }

    public static Map<Integer,Map<String,String[]>> genArchiveContentsExpectationsAuto(Path dir) throws IOException {
        final ExpectationFileVisitor expectationFileVisitor = new ExpectationFileVisitor(dir);
        Files.walkFileTree(dir, expectationFileVisitor);
        return expectationFileVisitor.getExpectations();
    }

    public static Map<Integer,Map<String,String[]>> genArchiveContentsExpectations(String[] rootExpectation,
            Map<String,String[]>... expectations) {
        Map<Integer,Map<String,String[]>> expectationMap = new HashMap<>();

        expectationMap.put(0,Collections.singletonMap("",rootExpectation));
        for (int i = 0; i < expectations.length; i++) {
            expectationMap.put(i+1, expectations[i]);
        }

        return expectationMap;
    }

    public static void simAddDirectoryToNewNonCompressorArchive(FxRobot robot, Path archive, Path dir) throws IOException {
        // Generate expectations from directory to be added...
        Map<Integer,Map<String,String[]>> expectations = genArchiveContentsExpectationsAuto(dir);
        String archiveName = archive.getFileName().toString();

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(robot, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");
        simAddFolder(robot, dir);

        // Exhaustively Breath first search file tree to check all files have been found from that of what was added...
        for (int i = 0; i < expectations.size(); i++) {
            for (String root : expectations.get(i)
                                           .keySet()) {
                List<String> expectationList = new LinkedList<>();
                final String[] sisterFiles = expectations.get(i)
                                               .get(root);
                for (String sibling : sisterFiles) {
                    Consumer<TableRow<FileInfo>> rowConsumer =
                            (t) -> t.getTableView()
                                    .getItems()
                                    .stream()
                                    .filter(f->f.getFileName().matches(String.format("%s$", sibling)))
                                    .peek(f->System.out.println(f.getFileName() + " vs. " + sibling))
                                    .filter(f ->  root.isEmpty() ?
                                            !String.format("%s", sibling).equals(f.getFileName()) :
                                            !String.format("%s/%s", root, sibling).equals(f.getFileName()))
                                    .map(FileInfo::getFileName)
                                    .forEach(f -> fail(String.format("The file %s was not found", f)));
                    if (!root.isEmpty()) {
                        expectationList.addAll(Arrays.asList(SSV.split(root)));
                    }
                    expectationList.add(sibling);
                    simTraversalArchive(robot, archiveName, "#fileContentsView", rowConsumer,
                                        expectationList.toArray(new String[0]));
                    expectationList.clear();
                    for (int j = i; j > 0; j--) {
                        PearlZipFXUtil.simUp(robot);
                    }
                }
            }
        }
    }
}
