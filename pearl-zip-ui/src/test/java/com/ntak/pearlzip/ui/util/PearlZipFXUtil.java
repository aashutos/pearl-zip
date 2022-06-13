/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.util;

import com.ntak.pearlzip.archive.model.PluginInfo;
import com.ntak.pearlzip.archive.pub.ArchiveReadService;
import com.ntak.pearlzip.archive.pub.ArchiveWriteService;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.archive.util.LoggingUtil;
import com.ntak.pearlzip.ui.constants.ZipConstants;
import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.mac.MacPearlZipApplication;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.model.ZipState;
import com.ntak.pearlzip.ui.pub.FrmAboutController;
import com.ntak.pearlzip.ui.pub.FrmMainController;
import com.ntak.pearlzip.ui.pub.PearlZipApplication;
import com.ntak.pearlzip.ui.pub.SysMenuController;
import com.ntak.pearlzip.ui.rules.*;
import com.ntak.testfx.ExpectationFileVisitor;
import com.ntak.testfx.FormUtil;
import com.ntak.testfx.NativeFileChooserUtil;
import de.jangassen.MenuToolkit;
import javafx.fxml.FXMLLoader;
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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ntak.pearlzip.archive.constants.ArchiveConstants.CURRENT_SETTINGS;
import static com.ntak.pearlzip.archive.constants.ArchiveConstants.WORKING_SETTINGS;
import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.CNS_RES_BUNDLE;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.CUSTOM_BUNDLE;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.LOG_BUNDLE;
import static com.ntak.pearlzip.archive.util.LoggingUtil.genLocale;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ResourceConstants.DSV;
import static com.ntak.pearlzip.ui.constants.ResourceConstants.SSV;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.mac.MacZipConstants.MENU_TOOLKIT;
import static com.ntak.pearlzip.ui.util.ArchiveUtil.initialiseApplicationSettings;
import static com.ntak.testfx.NativeFileChooserUtil.chooseFile;
import static com.ntak.testfx.TestFXConstants.PLATFORM;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.jupiter.api.Assertions.fail;

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
            robot.clickOn("#mnuNewArchive", MouseButton.PRIMARY);
        }

        final String[] nameSplit = DSV.split(archive.getFileName()
                                                   .toString());
        final String archiveFormat = nameSplit[nameSplit.length-1];
        robot.sleep(100, MILLISECONDS);
        ComboBox<String> cmbArchiveFormat = FormUtil.lookupNode(s -> s.isShowing() && s.getTitle().equals("Create new archive..."), "#comboArchiveFormat");
        FormUtil.selectComboBoxEntry(robot, cmbArchiveFormat, archiveFormat);

        robot.clickOn("#btnCreate", MouseButton.PRIMARY);
        robot.sleep(50, MILLISECONDS);

        Files.deleteIfExists(archive);
        chooseFile(PLATFORM, robot, archive);
        robot.sleep(50, MILLISECONDS);

        Assertions.assertTrue(Files.exists(archive), "Archive was not created");
    }

    public static void simAddFolder(FxRobot robot, Path folder, boolean useContextMenu, String archiveName) {
        if (!useContextMenu) {
            robot.clickOn("#btnAdd", MouseButton.PRIMARY);
            robot.sleep(50, MILLISECONDS);

            robot.clickOn("#mnuAddDir", MouseButton.PRIMARY);
            robot.sleep(50, MILLISECONDS);

            NativeFileChooserUtil.chooseFolder(PLATFORM, robot, folder);
            robot.sleep(50, MILLISECONDS);
        } else {
            TableView<FileInfo> fileContentsView = FormUtil.lookupNode(s->s.getTitle().contains(archiveName),
                                                                       "#fileContentsView");
            robot.clickOn(fileContentsView, MouseButton.SECONDARY);
            robot.sleep(50, MILLISECONDS);

            robot.clickOn("#mnuAddDir");
            robot.sleep(50, MILLISECONDS);

            NativeFileChooserUtil.chooseFolder(PLATFORM, robot, folder);
            robot.sleep(50, MILLISECONDS);
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
            robot.sleep(50, MILLISECONDS);

            robot.clickOn("#mnuAddFile", MouseButton.PRIMARY);
            robot.sleep(50, MILLISECONDS);

            chooseFile(PLATFORM, robot, file);
            robot.sleep(50, MILLISECONDS);
        } else {
            TableView<FileInfo> fileContentsView = FormUtil.lookupNode(s->s.getTitle().contains(archiveName),
                                                                       "#fileContentsView");
            robot.clickOn(fileContentsView, MouseButton.SECONDARY);
            robot.sleep(50, MILLISECONDS);

            robot.clickOn("#mnuAddFile");
            robot.sleep(50, MILLISECONDS);

            NativeFileChooserUtil.chooseFile(PLATFORM, robot, file);
            robot.sleep(50, MILLISECONDS);
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
            String tableName, Consumer<TableRow<FileInfo>> callback, String... identifiers) {
        return simTraversalArchive(robot, archiveName, tableName, "", callback, identifiers);
    }

    public static Optional<TableRow<FileInfo>> simTraversalArchive(FxRobot robot, String archiveName,
            String tableName, String root, Consumer<TableRow<FileInfo>> callback, String... identifiers) {
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

        chooseFile(PLATFORM, robot, file);
        robot.sleep(50, MILLISECONDS);
    }

    public static void simExtractAll(FxRobot robot, Path targetDir) {
        robot.clickOn("#btnExtract", MouseButton.PRIMARY);
        robot.sleep(50, MILLISECONDS);

        robot.clickOn("#mnuExtractAll", MouseButton.PRIMARY);
        robot.sleep(50, MILLISECONDS);

        chooseFile(PLATFORM, robot, targetDir);
        robot.sleep(50, MILLISECONDS);
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
            String root = String.format("%s/", archiveInfo.getPrefix());

            for (String transition : transitions) {
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
                robot.clickOn("#mnuMove");
            } else {
                robot.clickOn("#btnMove");
                robot.clickOn("#mnuMoveSelected");
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
                robot.clickOn("#mnuMove");
            } else {
                robot.clickOn("#btnMove");
                robot.clickOn("#mnuMoveSelected");
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
            robot.sleep(5, MILLISECONDS);
        }
        chooseFile(PLATFORM, robot, archive);

        Map<Boolean,List<Node>> buttonLookup =
                robot.lookup(".button-bar")
                     .queryAs(ButtonBar.class).getButtons()
                     .stream()
                     .collect(Collectors.partitioningBy((b)->((Button)b).getText().equals("Open in New Window")));
        Button response = (Button)buttonLookup.get(inNewWindow).get(0);
        robot.clickOn(response);
        robot.sleep(50, MILLISECONDS);
    }

    public static void simTestArchive(FxRobot robot) {
        robot.clickOn("#btnTest");
        robot.sleep(5, MILLISECONDS);
    }

    public static void simDelete(FxRobot robot) {
        robot.clickOn("#btnDelete");
        robot.sleep(5, MILLISECONDS);
    }

    public static void simFileInfo(FxRobot robot) {
        robot.clickOn("#btnInfo");
        robot.sleep(5, MILLISECONDS);
    }

    public static void initialise(Stage stage, List<ArchiveWriteService> writeServices,
            List<ArchiveReadService> readServices) throws IOException, TimeoutException {
        initialise(stage, writeServices, readServices, Paths.get(Files.createTempDirectory("pz").toString(), "temp.zip"));
    }

    public static void initialise(Stage stage, List<ArchiveWriteService> writeServices,
            List<ArchiveReadService> readServices, Path initialFile) throws IOException, TimeoutException {

        InternalContextCache.INTERNAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_APP,Mockito.mock(PearlZipApplication.class));

        RUNTIME_MODULE_PATH = Paths.get(System.getProperty("user.home"), ".pz", "providers");
        POST_PZAX_COMPLETION_CALLBACK = ()->{};
        // Set up global constants
        ZipConstants.PRIMARY_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);
        InternalContextCache.GLOBAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_RECENT_FILE, Paths.get(System.getProperty("user.home"),
                                                                                  ".pz", "rf"));
        String appName = System.getProperty(CNS_NTAK_PEARL_ZIP_APP_NAME, "PearlZip");
        String version = System.getProperty(CNS_NTAK_PEARL_ZIP_VERSION, "0.0.0.0");

        APPLICATION_SETTINGS_FILE = Paths.get(System.getProperty("user.home"), ".pz", "application.properties");
        RK_KEYS = new HashSet<>();
        RK_KEYS.add("configuration.ntak.pearl-zip.app-name");
        RK_KEYS.add("configuration.ntak.pearl-zip.version");
        RK_KEYS.add("configuration.ntak.pearl-zip.copyright");
        RK_KEYS.add("configuration.ntak.pearl-zip.weblink");
        RK_KEYS.add("configuration.ntak.pearl-zip.license-location");
        RK_KEYS.add("configuration.ntak.pearl-zip.license-override-location");

        initialiseApplicationSettings();

        // Set local directories...
        final Path STORE_ROOT = Paths.get(System.getProperty("user.home"), ".pz");
        InternalContextCache.GLOBAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_STORE_ROOT, STORE_ROOT);
        System.setProperty(CNS_NTAK_PEARL_ZIP_NO_FILES_HISTORY, "5");
        System.setProperty(String.format(CNS_PROVIDER_PRIORITY_ROOT_KEY,
                                         "com.ntak.pearlzip.archive.zip4j.pub.Zip4jArchiveReadService"), "5");
        System.setProperty(String.format(CNS_PROVIDER_PRIORITY_ROOT_KEY,
                                         "com.ntak.pearlzip.archive.zip4j.pub.Zip4jArchiveWriteService"), "5");
        InternalContextCache.GLOBAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_LOCAL_TEMP,
                Paths.get(Optional.ofNullable(System.getenv("TMPDIR"))
                                  .orElse(STORE_ROOT.toString()))
        );
        InternalContextCache.GLOBAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_STORE_TEMP,
                                                                            Paths.get(STORE_ROOT.toAbsolutePath()
                                                      .toString(), "temp")
        );

        // Loading plugin manifests...
        LOCAL_MANIFEST_DIR = Paths.get(STORE_ROOT.toAbsolutePath().toString(), "manifests");
        if (!Files.exists(LOCAL_MANIFEST_DIR)) {
            Files.createDirectories(LOCAL_MANIFEST_DIR);
        }

        Files.list(LOCAL_MANIFEST_DIR)
             .filter(m -> m.getFileName()
                           .toString()
                           .toUpperCase()
                           .endsWith(".MF"))
             .forEach(m -> {
                 try {
                     Optional<PluginInfo> optInfo = ModuleUtil.parseManifest(m);
                     if (optInfo.isPresent()) {
                         PluginInfo info = optInfo.get();
                         synchronized(PLUGINS_METADATA) {
                             PLUGINS_METADATA.put(info.getName(), info);
                         }
                     }
                 } catch(Exception e) {
                 }
             });

        Path SETTINGS_FILE = Paths.get(System.getProperty(CNS_SETTINGS_FILE, Paths.get(STORE_ROOT.toString(),
                                                                                       "settings.properties").toString()));
        InternalContextCache.GLOBAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_SETTINGS_FILE, SETTINGS_FILE);
        if (!Files.exists(SETTINGS_FILE)) {
            Files.createFile(SETTINGS_FILE);
        }
        try(InputStream settingsIStream = Files.newInputStream(SETTINGS_FILE)) {
            CURRENT_SETTINGS.load(settingsIStream);
            CURRENT_SETTINGS.setProperty(CNS_SHOW_TARGET_FOLDER_EXTRACT_SELECTED, "false");
            CURRENT_SETTINGS.setProperty(CNS_SHOW_TARGET_FOLDER_EXTRACT_ALL, "false");

            WORKING_SETTINGS.load(settingsIStream);
            CURRENT_SETTINGS.setProperty(CNS_SHOW_TARGET_FOLDER_EXTRACT_SELECTED, "false");
            CURRENT_SETTINGS.setProperty(CNS_SHOW_TARGET_FOLDER_EXTRACT_ALL, "false");
        }

        // Themes

        // Copy over and overwrite core themes...
        for (String theme : CORE_THEMES) {
            Path defThemePath = Paths.get(STORE_ROOT.toAbsolutePath()
                                                    .toString(), "themes", theme);
            Files.createDirectories(defThemePath);
            Stream<Path> themeFiles;
            try {
                themeFiles = Files.list(Paths.get(PearlZipFXUtil.class.getClassLoader()
                                                            .getResource(theme)
                                                            .getPath()));
            } catch (Exception e) {
                try {
                    themeFiles = Files.list(JRT_FILE_SYSTEM.getPath("modules", "com.ntak.pearlzip.ui",
                                                                    theme)
                                                           .toAbsolutePath());
                } catch(Exception exc) {
                    final String themePath = PearlZipFXUtil.class.getClassLoader()
                                                            .getResource(theme)
                                                            .getPath();
                    Path jarArchive =
                        Paths.get(themePath.substring(0, themePath.indexOf('!'))
                                        .replaceAll("file:",""));

                    Path tempDir = Files.createTempDirectory("pz");
                    Path srcThemePath =
                            Paths.get(tempDir.toAbsolutePath().toString(),
                                      themePath.substring(themePath.indexOf('!')+1));

                    try (JarFile jar = new JarFile(jarArchive.toFile())) {
                        for (JarEntry entry : jar.stream().toList()) {
                            if (entry.isDirectory()) {
                                Path entryDest = tempDir.resolve(entry.getName());

                                if (entry.isDirectory()) {
                                    Files.createDirectory(entryDest);
                                    continue;
                                }

                                Files.copy(jar.getInputStream(entry), entryDest);
                            }
                        }
                    }

                    themeFiles = Files.list(srcThemePath);
                }
            }

            themeFiles.forEach(f -> {
                                   try {
                                       Files.copy(f,
                                                  Paths.get(defThemePath.toAbsolutePath()
                                                                        .toString(),
                                                            f.getFileName()
                                                             .toString()
                                                  ),
                                                  StandardCopyOption.REPLACE_EXISTING);
                                   } catch(IOException e) {
                                   }
                               }
            );
        }

        // Loading rules...
        MANIFEST_RULES.add(new MinVersionManifestRule());
        MANIFEST_RULES.add(new MaxVersionManifestRule());
        MANIFEST_RULES.add(new LicenseManifestRule());
        MANIFEST_RULES.add(new CheckLibManifestRule());
        MANIFEST_RULES.add(new RemovePatternManifestRule());
        MANIFEST_RULES.add(new ThemeManifestRule());

        // Setting Locale
        Locale.setDefault(genLocale(new Properties()));
        LOG_BUNDLE = ModuleUtil.loadLangPackDynamic(RUNTIME_MODULE_PATH,
                                                    System.getProperty(CNS_RES_BUNDLE, "pearlzip"),
                                                    Locale.getDefault());
        CUSTOM_BUNDLE = ModuleUtil.loadLangPackDynamic(RUNTIME_MODULE_PATH,
                                                       System.getProperty(CNS_RES_BUNDLE,"custom"),
                                                       Locale.getDefault());

        // Load services
        for (ArchiveReadService readService : readServices) {
            ZipState.addArchiveProvider(readService);
        }

        for (ArchiveWriteService writeService : writeServices) {
            ZipState.addArchiveProvider(writeService);
        }

        initialiseMenu();

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
        FXArchiveInfo fxArchiveInfo = initFxArchiveInfo(initialFile);
        controller.initData(stage, fxArchiveInfo);
        fxArchiveInfo.setMainController(controller);
        stage.setTitle(resolveTextKey(TITLE_FILE_PATTERN, appName, version,
                                      fxArchiveInfo.getArchivePath()));
        stage.show();
        stage.toFront();

        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS, stage.showingProperty());
    }

    public static void initialiseMenu() throws IOException {
        if (!ZipConstants.getAdditionalConfig(MENU_TOOLKIT).isPresent()) {
            ZipConstants.setAdditionalConfig(MENU_TOOLKIT, MenuToolkit.toolkit(Locale.getDefault()));
        }

        MenuToolkit menuToolkit = (MenuToolkit)ZipConstants.getAdditionalConfig(MENU_TOOLKIT).get();

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
            robot.clickOn(200, 0)
                 .sleep(100, MILLISECONDS)
                 .clickOn(200,
                          ((1 + index) * 30));
            robot.sleep(250, MILLISECONDS);
            return true;
        } else {
            return false;
        }
    }
}
