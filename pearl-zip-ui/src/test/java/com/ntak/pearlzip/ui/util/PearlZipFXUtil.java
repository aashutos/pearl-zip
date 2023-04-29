/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.util;

import com.ntak.pearlzip.archive.pub.ArchiveReadService;
import com.ntak.pearlzip.archive.pub.ArchiveWriteService;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.archive.util.LoggingUtil;
import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.mac.MacPearlZipApplication;
import com.ntak.pearlzip.ui.mac.MacZipConstants;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.model.ZipState;
import com.ntak.pearlzip.ui.pub.FrmAboutController;
import com.ntak.pearlzip.ui.pub.FrmMainController;
import com.ntak.pearlzip.ui.pub.SysMenuController;
import com.ntak.pearlzip.ui.stages.jfx.JFXThemesStartupStage;
import com.ntak.testfx.ExpectationFileVisitor;
import com.ntak.testfx.FormUtil;
import com.ntak.testfx.NativeFileChooserUtil;
import com.ntak.testfx.specifications.CommonSpecifications;
import de.jangassen.MenuToolkit;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.testfx.api.FxRobot;
import org.testfx.util.WaitForAsyncUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.archive.constants.LoggingConstants.LOG_BUNDLE;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ResourceConstants.DSV;
import static com.ntak.pearlzip.ui.constants.ResourceConstants.SSV;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.testfx.FormUtil.lookupStage;
import static com.ntak.testfx.NativeFileChooserUtil.chooseFile;
import static com.ntak.testfx.TestFXConstants.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.jupiter.api.Assertions.fail;

public class PearlZipFXUtil {
    public static void simUp(FxRobot robot) {
        robot.clickOn("#btnUp", MouseButton.PRIMARY);
        robot.sleep(SHORT_PAUSE, MILLISECONDS);
    }
    
    public static void simNewArchive(FxRobot robot, Path archive) throws IOException {
        simNewArchive(robot, archive, true);
    }

    public static void simNewArchive(FxRobot robot, Path archive, boolean init) throws IOException {
        if (init) {
            robot.clickOn("#btnNew", MouseButton.PRIMARY);
            robot.sleep(SHORT_PAUSE, MILLISECONDS);
            robot.clickOn("#mnuNewArchive", MouseButton.PRIMARY);
        }

        final String[] nameSplit = DSV.split(archive.getFileName()
                                                   .toString());
        final String archiveFormat = nameSplit[nameSplit.length-1];
        robot.sleep(MEDIUM_PAUSE, MILLISECONDS);
        ComboBox<String> cmbArchiveFormat = FormUtil.lookupNode(s -> s.isShowing() && s.getTitle().equals("Create new archive..."), "#comboArchiveFormat");
        FormUtil.selectComboBoxEntry(robot, cmbArchiveFormat, archiveFormat);

        robot.clickOn("#btnCreate", MouseButton.PRIMARY);
        robot.sleep(SHORT_PAUSE, MILLISECONDS);

        Files.deleteIfExists(archive);
        chooseFile(PLATFORM, robot, archive);
        robot.sleep(SHORT_PAUSE, MILLISECONDS);

        Assertions.assertTrue(Files.exists(archive), "Archive was not created");
    }

    public static void simAddFolder(FxRobot robot, Path folder, boolean useContextMenu, String archiveName) {
        if (!useContextMenu) {
            robot.clickOn("#btnAdd", MouseButton.PRIMARY);
            robot.sleep(SHORT_PAUSE, MILLISECONDS);

            robot.clickOn("#mnuAddDir", MouseButton.PRIMARY);
            robot.sleep(SHORT_PAUSE, MILLISECONDS);

            if (Objects.nonNull(folder)) {
                NativeFileChooserUtil.chooseFolder(PLATFORM, robot, folder);
                robot.sleep(SHORT_PAUSE, MILLISECONDS);
            }
        } else {
            TableView<FileInfo> fileContentsView = FormUtil.lookupNode(s->s.getTitle().contains(archiveName),
                                                                       "#fileContentsView");
            robot.clickOn(fileContentsView, MouseButton.SECONDARY);
            robot.sleep(SHORT_PAUSE, MILLISECONDS);

            robot.clickOn("#mnuAddDir");
            robot.sleep(SHORT_PAUSE, MILLISECONDS);

            if (Objects.nonNull(folder)) {
                NativeFileChooserUtil.chooseFolder(PLATFORM, robot, folder);
                robot.sleep(SHORT_PAUSE, MILLISECONDS);
            }
        }
    }

    public static void simAddFolder(FxRobot robot, Path folder) {
        simAddFolder(robot, folder, false, null);
    }

    public static void simAddFile(FxRobot robot, Path file) {
        simAddFile(robot, file, false, null);
    }

    public static void simAddFile(FxRobot robot, Path file, boolean useContextMenu, String archiveName) {
        if (!useContextMenu) {
            robot.clickOn("#btnAdd", MouseButton.PRIMARY);
            robot.sleep(SHORT_PAUSE, MILLISECONDS);

            robot.clickOn("#mnuAddFile", MouseButton.PRIMARY);
            robot.sleep(SHORT_PAUSE, MILLISECONDS);

            if (Objects.nonNull(file)) {
                chooseFile(PLATFORM, robot, file);
                robot.sleep(SHORT_PAUSE, MILLISECONDS);
            }
        } else {
            TableView<FileInfo> fileContentsView = FormUtil.lookupNode(s->s.getTitle().contains(archiveName),
                                                                       "#fileContentsView");
            robot.clickOn(fileContentsView, MouseButton.SECONDARY);
            robot.sleep(SHORT_PAUSE, MILLISECONDS);

            robot.clickOn("#mnuAddFile");
            robot.sleep(SHORT_PAUSE, MILLISECONDS);

            if (Objects.nonNull(file)) {
                NativeFileChooserUtil.chooseFile(PLATFORM, robot, file);
                robot.sleep(SHORT_PAUSE, MILLISECONDS);
            }
        }
    }

    public static void simAddDirectoryToNewNonCompressorArchive(FxRobot robot, Path archive, Path dir,
            boolean useContextMenu) throws IOException {
        // Generate expectations from directory to be added...
        Map<Integer,Map<String,String[]>> expectations = genArchiveContentsExpectationsAuto(dir);
        String archiveName = archive.getFileName().toString();

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(robot, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");
        simAddFolder(robot, dir, useContextMenu, archiveName);

        checkArchiveFileHierarchy(robot, expectations, archiveName);
    }

    public static void checkArchiveFileHierarchy(FxRobot robot, Map<Integer,Map<String,String[]>> expectations,
            String archiveName) {
        // Exhaustively Breath first search file tree to check all files have been found from that of what was added...
        for (int i = 0; i < expectations.size(); i++) {
            for (String root : expectations.get(i)
                                           .keySet()) {
                List<String> expectationList = new LinkedList<>();
                final String[] sisterFiles = expectations.get(i)
                                                         .get(root);
                if (!root.isEmpty()) {
                    var parentFolder = simTraversalArchive(robot, archiveName, "#fileContentsView", (t) -> {},
                                        Arrays.asList(SSV.split(root))
                                              .toArray(new String[0])).get();
                    robot.doubleClickOn(parentFolder);
                }
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
                        expectationList.add(String.format("%s/%s",root,sibling));
                    }

                    simTraversalArchive(robot, archiveName, "#fileContentsView", rowConsumer,
                                        expectationList.toArray(new String[0]));
                    expectationList.clear();
                }
                for (int j = i; j > 0; j--) {
                    PearlZipFXUtil.simUp(robot);
                }
            }
        }
    }

    public static Optional<TableRow<FileInfo>> simTraversalArchive(FxRobot robot, String archiveName,
            String tableName, Consumer<TableRow<FileInfo>> callback, boolean selectLast, String... identifiers) {
        Optional<TableRow<FileInfo>> row = simTraversalArchive(robot, archiveName, tableName, "", callback, identifiers);

        if (selectLast) {
            robot.sleep(MEDIUM_PAUSE, MILLISECONDS).doubleClickOn(MouseButton.PRIMARY);
        }

        return row;
    }

    public static Optional<TableRow<FileInfo>> simTraversalArchive(FxRobot robot, String archiveName,
            String tableName, Consumer<TableRow<FileInfo>> callback, String... identifiers) {
        return simTraversalArchive(robot, archiveName, tableName, "", callback, identifiers);
    }

    public static Optional<TableRow<FileInfo>> simTraversalArchive(FxRobot robot, String archiveName,
            String tableName, String root, Consumer<TableRow<FileInfo>> callback, String... identifiers) {
        final FXArchiveInfo archiveInfo = lookupArchiveInfo(archiveName).get();
        String archivePath = archiveInfo.getArchivePath();
        for (int i = 0; i < identifiers.length; i++) {
            Optional<TableRow<FileInfo>> selectedRow = FormUtil.selectTableViewEntry(robot,
                                                                                     FormUtil.lookupNode((s) -> s.getScene()
                                                                                                                 .lookup(tableName) != null && s.getTitle().contains(archivePath),
                                                                                                         tableName),
                                                                                     FileInfo::getFileName,
                                                                                     (archiveInfo.getDepth().get() == 0)?identifiers[i]:String.format("%s%s", root,
                                                                                                   identifiers[i]));
            Assertions.assertTrue(selectedRow.isPresent(), "No row was selected");
            if (identifiers.length == i+1) {
                robot.sleep(SHORT_PAUSE, MILLISECONDS);
                callback.accept(selectedRow.get());
                return selectedRow;
            }
            robot.doubleClickOn(selectedRow.get(), MouseButton.PRIMARY);
            root += String.format("%s/",identifiers[i]);
            robot.sleep(SHORT_PAUSE, MILLISECONDS);
        }

        return Optional.empty();
    }

    public static void simExtractFile(FxRobot robot, Path file) {
        robot.clickOn("#btnExtract", MouseButton.PRIMARY);
        robot.sleep(SHORT_PAUSE, MILLISECONDS);

        robot.clickOn("#mnuExtractSelectedFile", MouseButton.PRIMARY);
        robot.sleep(SHORT_PAUSE, MILLISECONDS);

        chooseFile(PLATFORM, robot, file);
        robot.sleep(SHORT_PAUSE, MILLISECONDS);
    }

    public static void simExtractAll(FxRobot robot, Path targetDir) {
        robot.clickOn("#btnExtract", MouseButton.PRIMARY);
        robot.sleep(SHORT_PAUSE, MILLISECONDS);

        robot.clickOn("#mnuExtractAll", MouseButton.PRIMARY);
        robot.sleep(SHORT_PAUSE, MILLISECONDS);

        chooseFile(PLATFORM, robot, targetDir);
        robot.sleep(SHORT_PAUSE, MILLISECONDS);
    }

    public static void simCopyFile(FxRobot robot, boolean useContextMenu, String archiveName, String tableName,
            Path path, String... transitions) {
        TableView<FileInfo> fileContentsView = FormUtil.lookupNode((s) -> s.getScene()
                                                                           .lookup(tableName) != null && s.getTitle().contains(archiveName),
                                                                   tableName);

        Consumer<TableRow<FileInfo>> copyConsumer = (r) -> {
            // Initiate copy
            if (useContextMenu) {
                robot.clickOn(r, MouseButton.SECONDARY);
                robot.clickOn("#mnuCopy");
            } else {
                robot.clickOn("#btnCopy");
                robot.clickOn("#mnuCopySelected");
            }

            FXArchiveInfo archiveInfo = lookupArchiveInfo(archiveName).get();

            for (String transition : transitions) {
                String root = String.format("%s/", archiveInfo.getPrefix());
                switch (transition) {
                    case "..":  simUp(robot);
                                break;

                    case ".":   break;

                    default: {
                        simTraversalArchive(robot, archiveName, tableName, root, (e) -> {}, transition);
                        int i;
                        for (i = 0; i < fileContentsView.getItems()
                                                        .size(); i++) {
                            if (fileContentsView.getItems()
                                                .get(i)
                                                .getFileName()
                                                .endsWith(transition)) {
                                break;
                            }
                        }
                        TableRow<FileInfo> row =
                                ((TableCell) fileContentsView.queryAccessibleAttribute(AccessibleAttribute.CELL_AT_ROW_COLUMN,
                                                                                       i, 0)).getTableRow();
                        robot.doubleClickOn(row);
                    }
                }
            }

            // Initiate paste
            if (useContextMenu) {
                FileInfo file = fileContentsView.getItems().stream().findFirst().get();
                FormUtil.selectTableViewEntry(robot,  fileContentsView, FileInfo::getFileName, file.getFileName());
                TableRow<FileInfo> row =
                        ((TableCell)fileContentsView.queryAccessibleAttribute(AccessibleAttribute.CELL_AT_ROW_COLUMN,
                                                                                   fileContentsView.getSelectionModel().getSelectedIndex(), 0)).getTableRow();
                robot.clickOn(row, MouseButton.SECONDARY);
                robot.clickOn("#mnuCopy");
            } else {
                robot.clickOn("#btnCopy");
                robot.clickOn("#mnuCopySelected");
            }

            Assertions.assertTrue(fileContentsView.getItems()
                                                  .stream()
                                                  .map(FileInfo::getFileName)
                                                  .noneMatch(f->f.equals(path.getFileName().toString())),
                                  "File was not copied successfully");
        };
        simTraversalArchive(robot, archiveName, tableName, copyConsumer, SSV.split(path.toString()));
    }

    public static void simMoveFile(FxRobot robot, boolean useContextMenu, String archiveName, String tableName,
            Path path, String... transitions) {
        TableView<FileInfo> fileContentsView = FormUtil.lookupNode((s) -> s.getScene()
                                                                           .lookup(tableName) != null && s.getTitle().contains(archiveName),
                                                                   tableName);

        Consumer<TableRow<FileInfo>> copyConsumer = (r) -> {
            // Initiate copy
            if (useContextMenu) {
                robot.clickOn(r, MouseButton.SECONDARY);
                robot.sleep(LONG_PAUSE, MILLISECONDS);
                robot.clickOn("#mnuMove");
                robot.sleep(LONG_PAUSE, MILLISECONDS);
            } else {
                robot.clickOn("#btnMove");
                robot.sleep(LONG_PAUSE, MILLISECONDS);
                robot.clickOn("#mnuMoveSelected");
                robot.sleep(LONG_PAUSE, MILLISECONDS);
            }

            FXArchiveInfo archiveInfo = lookupArchiveInfo(archiveName).get();
            String root = String.format("%s/", archiveInfo.getPrefix());

            for (String transition : transitions) {
                switch (transition) {
                    case "..":  simUp(robot);
                        break;

                    case ".":   break;

                    default:    {
                        simTraversalArchive(robot, archiveName, tableName, root, (e) -> {}, transition);
                        int i;
                        for (i = 0; i < fileContentsView.getItems()
                                                        .size(); i++) {
                            if (fileContentsView.getItems()
                                                .get(i)
                                                .getFileName()
                                                .endsWith(transition)) {
                                break;
                            }
                        }
                        TableRow<FileInfo> row =
                                ((TableCell) fileContentsView.queryAccessibleAttribute(AccessibleAttribute.CELL_AT_ROW_COLUMN,
                                                                                       i, 0)).getTableRow();
                        robot.doubleClickOn(row);
                    }
                }
            }

            // Initiate paste
            if (useContextMenu) {
                FileInfo file = fileContentsView.getItems().stream().findFirst().get();
                FormUtil.selectTableViewEntry(robot,  fileContentsView, FileInfo::getFileName, file.getFileName());
                TableRow<FileInfo> row =
                        ((TableCell)fileContentsView.queryAccessibleAttribute(AccessibleAttribute.CELL_AT_ROW_COLUMN,
                                                                              fileContentsView.getSelectionModel().getSelectedIndex(), 0)).getTableRow();
                robot.clickOn(row, MouseButton.SECONDARY);
                robot.sleep(LONG_PAUSE, MILLISECONDS);
                robot.clickOn("#mnuMove");
                robot.sleep(LONG_PAUSE, MILLISECONDS);
            } else {
                robot.clickOn("#btnMove");
                robot.sleep(LONG_PAUSE, MILLISECONDS);
                robot.clickOn("#mnuMoveSelected");
                robot.sleep(LONG_PAUSE, MILLISECONDS);
            }

            Assertions.assertTrue(fileContentsView.getItems()
                                                  .stream()
                                                  .map(FileInfo::getFileName)
                                                  .noneMatch(f->f.equals(path.getFileName().toString())),
                                  "File was not moved successfully");
        };
        simTraversalArchive(robot, archiveName, tableName, copyConsumer, SSV.split(path.toString()));
    }

    public static void simOpenArchive(FxRobot robot, Path archive, boolean init, boolean inNewWindow) {
        if (init) {
            robot.clickOn("#btnOpen");
            robot.sleep(SHORT_PAUSE, MILLISECONDS);
        }
        chooseFile(PLATFORM, robot, archive);

        Map<Boolean,List<Node>> buttonLookup = CommonSpecifications.retryRetrievalForDuration(RETRIEVAL_TIMEOUT_MILLIS, () ->
                robot.lookup(".button-bar")
                     .queryAs(ButtonBar.class).getButtons()
                     .stream()
                     .collect(Collectors.partitioningBy((b)->((Button)b).getText().equals("Open in New Window"))));
        Button response = (Button)buttonLookup.get(inNewWindow).get(0);
        robot.clickOn(response);
        robot.sleep(LONG_PAUSE, MILLISECONDS);

        try {
            robot.clickOn(robot.lookup(".button-bar")
                               .queryAs(ButtonBar.class)
                               .getButtons()
                               .stream()
                               .filter(b -> ((Button) b).getText()
                                                        .equals("No"))
                               .findFirst()
                               .get());
        } catch (Exception e) {
            // ignore...
        }
    }

    public static void simOpenArchiveBySysMenu(FxRobot robot, Path archivePath, boolean inNewWindow) {
        robot.clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 80));
        simOpenArchive(robot, archivePath, false, inNewWindow);
    }

    public static void simSaveAsBySysMenu(FxRobot robot, Path archivePath) {
        robot.clickOn(Point2D.ZERO.add(110, 10))
             .clickOn(Point2D.ZERO.add(110, 140));
        chooseFile(PLATFORM, robot, archivePath);
        robot.sleep(LONG_PAUSE, MILLISECONDS);
    }

    public static void simTestArchive(FxRobot robot) {
        robot.clickOn("#btnTest");
        robot.sleep(SHORT_PAUSE, MILLISECONDS);
    }

    public static void simDelete(FxRobot robot) {
        robot.clickOn("#btnDelete");
        robot.sleep(SHORT_PAUSE, MILLISECONDS);
    }

    public static void simFileInfo(FxRobot robot) {
        robot.clickOn("#btnInfo");
        robot.sleep(SHORT_PAUSE, MILLISECONDS);
    }

    public static void initialise(Stage stage, List<ArchiveWriteService> writeServices,
            List<ArchiveReadService> readServices, Path initFile) throws IOException, TimeoutException {

        for (AbstractSeedStartupStage ss : SEED_STARTUP_STAGE) {
            ss.execute();
        }

        for (AbstractStartupStage ss : STARTUP_STAGE) {
            ss.execute();
        }

        MacPearlZipApplication mockApplication = Mockito.mock(MacPearlZipApplication.class);
        List<AbstractJFXStartupStage> JFX_STARTUP_STAGE = Arrays.asList(new JFXThemesStartupStage(mockApplication));
        InternalContextCache.INTERNAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_APP, mockApplication);


        for (AbstractJFXStartupStage startupStage : JFX_STARTUP_STAGE) {
            startupStage.execute();
        }

        // Load services
        for (ArchiveReadService readService : readServices) {
            ZipState.addArchiveProvider(readService);
        }

        for (ArchiveWriteService writeService : writeServices) {
            ZipState.addArchiveProvider(writeService);
        }

        PearlZipFXUtil.initialiseMenu();
        // Load main form
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(MacPearlZipApplication.class.getResource("/frmMain.fxml"));
        loader.setResources(LOG_BUNDLE);
        Parent root = loader.load();
        if (!stage.getStyle().equals(StageStyle.DECORATED))
            stage.initStyle(StageStyle.DECORATED);
        stage.setScene(new Scene(root));
        FrmMainController controller = loader.getController();

        // Set up initial archive
        FXArchiveInfo fxArchiveInfo = initFxArchiveInfo(initFile);
        controller.initData(stage, fxArchiveInfo);
        fxArchiveInfo.setMainController(controller);
        stage.setTitle(resolveTextKey(TITLE_FILE_PATTERN, "PearlZip", "0.0.0.0",
                                      fxArchiveInfo.getArchivePath()));
        stage.show();
        stage.toFront();

        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS, stage.showingProperty());

        Path STORE_ROOT = InternalContextCache.GLOBAL_CONFIGURATION_CACHE.<Path>getAdditionalConfig(CK_STORE_ROOT).get();
        Path LOCAL_TEMP = Paths.get(Optional.ofNullable(System.getenv("TMPDIR"))
                                       .orElse(STORE_ROOT.toString()));
        InternalContextCache.GLOBAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_LOCAL_TEMP, LOCAL_TEMP);
        InternalContextCache.GLOBAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_STORE_TEMP,
                                                                            Paths.get(System.getProperty("user.home"), ".pz", "temp"));
    }

    public static void initialiseMenu() throws IOException {
        if (!InternalContextCache.INTERNAL_CONFIGURATION_CACHE.getAdditionalConfig(MacZipConstants.CK_MENU_TOOLKIT).isPresent()) {
            InternalContextCache.INTERNAL_CONFIGURATION_CACHE.setAdditionalConfig(MacZipConstants.CK_MENU_TOOLKIT, MenuToolkit.toolkit(Locale.getDefault()));
        }

        MenuToolkit menuToolkit = InternalContextCache.INTERNAL_CONFIGURATION_CACHE.<MenuToolkit>getAdditionalConfig(MacZipConstants.CK_MENU_TOOLKIT).get();

        // Create a new System Menu
        String appName = System.getProperty(CNS_NTAK_PEARL_ZIP_APP_NAME, "PearlZip");
        MenuBar sysMenu = new MenuBar();

        // Setting about form...
        FXMLLoader aboutLoader = new FXMLLoader();
        aboutLoader.setLocation(MacPearlZipApplication.class.getClassLoader().getResource("frmAbout.fxml"));
        aboutLoader.setResources(LOG_BUNDLE);
        VBox abtRoot = aboutLoader.load();
        FrmAboutController abtController = aboutLoader.getController();
        Scene abtScene = new Scene(abtRoot);
        Stage aboutStage = new Stage();
        abtController.initData(aboutStage);
        aboutStage.setScene(abtScene);
        aboutStage.initStyle(StageStyle.UNDECORATED);

        sysMenu.setUseSystemMenuBar(true);
        sysMenu.getMenus()
               .add(menuToolkit.createDefaultApplicationMenu(appName, aboutStage));

        // Add some more Menus...
        FXMLLoader menuLoader = new FXMLLoader();
        menuLoader.setLocation(MacPearlZipApplication.class.getClassLoader()
                                                           .getResource("sysmenu.fxml"));
        menuLoader.setResources(LOG_BUNDLE);
        MenuBar additionalMenu = menuLoader.load();
        SysMenuController menuController = menuLoader.getController();
        menuController.initData();
        sysMenu.getMenus()
               .addAll(additionalMenu.getMenus());
        sysMenu.setId("MenuBar");

        // Use the menu sysMenu for all stages including new ones
        menuToolkit.setGlobalMenuBar(sysMenu);

        // Set Windows menu variable...
        InternalContextCache.INTERNAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_WINDOW_MENU,
                      sysMenu.getMenus()
                             .stream()
                             .filter(m -> m.getText()
                                           .equals(LoggingUtil.resolveTextKey(CNS_SYSMENU_WINDOW_TEXT)))
                             .findFirst()
                             .get()
        );
    }

    private static FXArchiveInfo initFxArchiveInfo(Path archive) throws IOException {
        Files.deleteIfExists(archive);
        ArchiveReadService readService = ZipState.getReadArchiveServiceForFile(archive.getFileName().toString()).get();
        ArchiveWriteService writeService =
                ZipState.getWriteArchiveServiceForFile(archive.getFileName().toString()).orElse(null);
        writeService.createArchive(System.currentTimeMillis(), archive.toString());

        return new FXArchiveInfo(archive.toAbsolutePath().toString(), readService,
                                 writeService);
    }

    public static Optional<FXArchiveInfo> lookupArchiveInfo(String archiveName) {
        return Optional.of((FXArchiveInfo) Stage.getWindows()
                       .stream()
                       .map(Stage.class::cast)
                       .filter(s->s.getTitle() != null)
                       .filter((s)->s.getTitle().matches(String.format(".*%s$", archiveName)))
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

        expectationMap.put(0, Collections.singletonMap("", rootExpectation));
        for (int i = 0; i < expectations.length; i++) {
            expectationMap.put(i + 1, expectations[i]);
        }

        return expectationMap;
    }

    public static boolean simWindowSelect(FxRobot robot, Path archive) {
        Menu WINDOW_MENU = InternalContextCache.INTERNAL_CONFIGURATION_CACHE
                                               .<Menu>getAdditionalConfig(CK_WINDOW_MENU)
                                               .get();

        int index =
                WINDOW_MENU.getItems()
                           .stream()
                           .map(MenuItem::getText)
                           .collect(Collectors.toList())
                           .indexOf(WINDOW_MENU.getItems()
                                               .stream()
                                               .map(MenuItem::getText)
                                               .filter(s -> s.contains(
                                                       archive.toAbsolutePath()
                                                              .toString()))
                                               .findFirst()
                                               .orElse(""));
        if (index >= 0) {
            System.out.println(String.format("Clicking on file: %s. Index = %d...", archive.getFileName().toString(), index));
            TableView view = FormUtil.lookupNode(s->s.getTitle().contains(archive.toString()),
                                                 "#fileContentsView");
            robot.clickOn(200, 0)
                 .sleep(MEDIUM_PAUSE, MILLISECONDS)
                 .moveTo(200,
                          (25 + ((index+1) * 17)))
                .sleep(LONG_PAUSE, MILLISECONDS)
                .clickOn(MouseButton.PRIMARY)
                .clickOn(view);

            return true;
        } else {
            System.out.println("Index < 0...");
            return false;
        }
    }

    public static Stage lookupStageChecked(String regExPattern) {
        Optional<Stage> optStage = lookupStage(regExPattern);
        Assertions.assertTrue(optStage.isPresent(), String.format("Dialog matching %s not found", regExPattern));

        return optStage.get();
    }

    public static void simSelectOptionsAdditionalTab(FxRobot fxRobot, int i) {
        Stage frmOptions = lookupStageChecked("PearlZip Options");

        double x = frmOptions.getX();
        double y = frmOptions.getY();
        double width = frmOptions.getWidth();

        fxRobot.clickOn(Point2D.ZERO.add(x + width - 10, y + 50));

        fxRobot.clickOn(Point2D.ZERO.add(x + width - 10, y + (22 * (i)) + 62))
                     .sleep(MEDIUM_PAUSE, MILLISECONDS);
    }
}
