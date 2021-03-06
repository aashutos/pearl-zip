/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.testfx;

import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.UITestSuite;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.util.AbstractPearlZipTestFX;
import com.ntak.testfx.FormUtil;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.ui.UITestFXSuite.genSourceDataSet;
import static com.ntak.pearlzip.ui.constants.ResourceConstants.SSV;
import static com.ntak.pearlzip.ui.util.PearlZipFXUtil.*;
import static com.ntak.testfx.FormUtil.lookupNode;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Tag("fx-test")
public class CopyInArchiveTestFX extends AbstractPearlZipTestFX {

    private static Path dir;
    private static Path emptyDir;

   /*
    *  Test cases:
    *  + Copy up button and menu
    *  + Copy down button and menu
    *  + Copy disabled for compressor archives
    *  + Copy cancel
    *  + Copy no file selected
    *  + Copy folder fail
    *  + Table context menu paste
    *  + Copy into empty folder
    *  + Copy from within a non-existent archive
    */

    @BeforeEach
    public void setUp() throws IOException {
        dir = genSourceDataSet();
        emptyDir = Paths.get(dir.toAbsolutePath().getParent().toString(), "empty-dir");
        Files.createDirectories(emptyDir);
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
        for (Path dir :
             Files.list(dir.getParent().getParent()).filter(p->p.getFileName().toString().startsWith("pz")).collect(
             Collectors.toList())) {
            UITestSuite.clearDirectory(dir);
        }
    }

    @Test
    @DisplayName("Test: Copy file down by context menu and button in application within a zip archive successfully")
    public void testFX_copyFileDownByMenuAndButtonZip_MatchExpectations() throws IOException {
        final String archiveFormat = "zip";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(this, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");
        simAddFolder(this, dir);

        Path file = Paths.get("root", "level1c", "COPY_DOWN.txt");

        final String tableName = "#fileContentsView";
        simCopyFile(this, false, archiveName, tableName, file, "level1c1");
        simUp(this);
        simUp(this);
        simUp(this);
        file = Paths.get("root", "level1c", "level1c1", "COPY_DOWN.txt");
        simCopyFile(this, true, archiveName, tableName, file, "level2b");

        FXArchiveInfo info = lookupArchiveInfo(archiveName).get();
        Assertions.assertEquals(3,
                                info.getFiles().stream().filter(f->f.getFileName().contains("COPY_DOWN.txt")).count(),
                                "File was not copied");
    }

    @Test
    @DisplayName("Test: Copy file by table context menu and button in application within a zip archive successfully")
    public void testFX_copyFileByTableContextMenuAndButtonZip_MatchExpectations() throws IOException {
        final String archiveFormat = "zip";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(this, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");
        simAddFolder(this, dir);

        Path file = Paths.get("root", "level1c", "level1c1", "level2b", "COPY_UP.txt");

        final String tableName = "#fileContentsView";
        TableView<FileInfo> fileContentsView = FormUtil.lookupNode(s->s.getTitle().contains(archiveName),
                                                                   "#fileContentsView");
        simTraversalArchive(this, archiveName, tableName, (r)->{}, SSV.split(file.toString()));
        clickOn("#btnCopy");
        clickOn("#mnuCopySelected");
        simUp(this);

        clickOn(fileContentsView, MouseButton.SECONDARY);
        sleep(50, MILLISECONDS);
        clickOn("#mnuPaste");
        sleep(50, MILLISECONDS);

        FXArchiveInfo info = lookupArchiveInfo(archiveName).get();
        Assertions.assertEquals(2,
                                info.getFiles().stream().filter(f->f.getFileName().contains("COPY_UP.txt")).count(),
                                "File was not copied");
    }

    @Test
    @DisplayName("Test: Copy file down by context menu and button in application within a tar archive successfully")
    public void testFX_copyFileDownByMenuAndButtonTar_MatchExpectations() throws IOException {
        final String archiveFormat = "tar";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(this, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");
        simAddFolder(this, dir);

        Path file = Paths.get("root", "level1c", "COPY_DOWN.txt");

        final String tableName = "#fileContentsView";
        simCopyFile(this, false, archiveName, tableName, file, "level1c1");
        simUp(this);
        simUp(this);
        simUp(this);
        file = Paths.get("root", "level1c", "level1c1", "COPY_DOWN.txt");
        simCopyFile(this, true, archiveName, tableName, file, "level2b");

        FXArchiveInfo info = lookupArchiveInfo(archiveName).get();
        Assertions.assertEquals(3,
                                info.getFiles().stream().filter(f->f.getFileName().contains("COPY_DOWN.txt")).count(),
                                "File was not copied");
    }

    @Test
    @DisplayName("Test: Copy file down by context menu and button in application within a jar archive successfully")
    public void testFX_copyFileDownByMenuAndButtonJar_MatchExpectations() throws IOException {
        final String archiveFormat = "jar";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(this, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");
        simAddFolder(this, dir);

        Path file = Paths.get("root", "level1c", "COPY_DOWN.txt");

        final String tableName = "#fileContentsView";
        simCopyFile(this, false, archiveName, tableName, file, "level1c1");
        simUp(this);
        simUp(this);
        simUp(this);
        file = Paths.get("root", "level1c", "level1c1", "COPY_DOWN.txt");
        simCopyFile(this, true, archiveName, tableName, file, "level2b");

        FXArchiveInfo info = lookupArchiveInfo(archiveName).get();
        Assertions.assertEquals(3,
                                info.getFiles().stream().filter(f->f.getFileName().contains("COPY_DOWN.txt")).count(),
                                "File was not copied");
    }

    @Test
    @DisplayName("Test: Copy file up by context menu and button in application within a zip archive successfully")
    public void testFX_copyFileUpByMenuAndButtonZip_MatchExpectations() throws IOException {
        final String archiveFormat = "zip";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(this, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");
        simAddFolder(this, dir);

        Path file = Paths.get("root", "level1c", "level1c1", "level2b", "COPY_UP.txt");

        final String tableName = "#fileContentsView";
        simCopyFile(this, true, archiveName, tableName, file, "..", "..");
        simUp(this);
        simUp(this);
        file = Paths.get("root", "level1c", "COPY_UP.txt");
        simCopyFile(this, false, archiveName, tableName, file, "..");

        FXArchiveInfo info = lookupArchiveInfo(archiveName).get();
        Assertions.assertEquals(3,
                                info.getFiles().stream().filter(f->f.getFileName().contains("COPY_UP.txt")).count(),
                                "File was not copied");
    }

    @Test
    @DisplayName("Test: Copy file up by context menu and button in application within a tar archive successfully")
    public void testFX_copyFileUpByMenuAndButtonTar_MatchExpectations() throws IOException {
        final String archiveFormat = "tar";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(this, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");
        simAddFolder(this, dir);

        Path file = Paths.get("root", "level1c", "level1c1", "level2b", "COPY_UP.txt");

        final String tableName = "#fileContentsView";
        simCopyFile(this, true, archiveName, tableName, file, "..", "..");
        simUp(this);
        simUp(this);
        file = Paths.get("root", "level1c", "COPY_UP.txt");
        simCopyFile(this, false, archiveName, tableName, file, "..");

        FXArchiveInfo info = lookupArchiveInfo(archiveName).get();
        Assertions.assertEquals(3,
                                info.getFiles().stream().filter(f->f.getFileName().contains("COPY_UP.txt")).count(),
                                "File was not copied");
    }

    @Test
    @DisplayName("Test: Copy file up by context menu and button in application within a jar archive successfully")
    public void testFX_copyFileUpByMenuAndButtonJar_MatchExpectations() throws IOException {
        final String archiveFormat = "jar";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(this, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");
        simAddFolder(this, dir);

        Path file = Paths.get("root", "level1c", "level1c1", "level2b", "COPY_UP.txt");

        final String tableName = "#fileContentsView";
        simCopyFile(this, true, archiveName, tableName, file, "..", "..");
        simUp(this);
        simUp(this);
        file = Paths.get("root", "level1c", "COPY_UP.txt");
        simCopyFile(this, false, archiveName, tableName, file, "..");

        FXArchiveInfo info = lookupArchiveInfo(archiveName).get();
        Assertions.assertEquals(3,
                                info.getFiles().stream().filter(f->f.getFileName().contains("COPY_UP.txt")).count(),
                                "File was not copied");
    }

    @Test
    @DisplayName("Test: Copy file in application within a Gzip archive is blocked (Single file compressor)")
    public void testFX_copyFileUpByMenuGzip_Blocked() throws IOException {
        final String archiveFormat = "tar.gz";
        final String archiveName = String.format("testgz.%s", archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(this, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");

        clickOn("#btnCopy", MouseButton.PRIMARY);
        sleep(50, MILLISECONDS);

        Assertions.assertTrue(lookupNode(s->s.getTitle().contains(archiveName), "#btnCopy")
                                                .isDisable(),
                              "Copy is not disabled for compressor archives");
    }

    @Test
    @DisplayName("Test: Copy file in application within a Bzip archive is blocked (Single file compressor)")
    public void testFX_copyFileUpByMenuBzip_Blocked() throws IOException {
        final String archiveFormat = "tar.bz2";
        final String archiveName = String.format("testbzip.%s", archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(this, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");

        clickOn("#btnCopy", MouseButton.PRIMARY);
        sleep(50, MILLISECONDS);

        Assertions.assertTrue(lookupNode(s->s.getTitle().contains(archiveName), "#btnCopy")
                                                .isDisable(),
                              "Copy is not disabled for compressor archives");
    }

    @Test
    @DisplayName("Test: Copy file in application within a xz archive is blocked (Single file compressor)")
    public void testFX_copyFileUpByMenuXz_Blocked() throws IOException {
        final String archiveFormat = "tar.gz";
        final String archiveName = String.format("testxz.%s", archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(this, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");

        clickOn("#btnCopy", MouseButton.PRIMARY);
        sleep(50, MILLISECONDS);

        Assertions.assertTrue(lookupNode(s->s.getTitle().contains(archiveName), "#btnCopy")
                                                .isDisable(),
                              "Copy is not disabled for compressor archives");
    }

    @Test
    @DisplayName("Test: Copy file and cancel")
    public void testFX_copyFileCancel() throws IOException {
        final String archiveFormat = "jar";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(this, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");
        simAddFolder(this, dir);

        Path file = Paths.get("root", "level1c", "level1c1", "level2b", "COPY_UP.txt");

        final String tableName = "#fileContentsView";
        simTraversalArchive(this, archiveName, tableName, (r)->{}, SSV.split(file.toString()));
        clickOn("#btnCopy");
        sleep(5, MILLISECONDS);
        clickOn("#mnuCopySelected");
        sleep(5, MILLISECONDS);
        clickOn("#btnCopy");
        sleep(5, MILLISECONDS);
        clickOn("#mnuCancelCopy");

        FXArchiveInfo info = lookupArchiveInfo(archiveName).get();
        Assertions.assertEquals(1,
                                info.getFiles().stream().filter(f->f.getFileName().equals(file.toString())).count(),
                                "File was copied unexpectedly");
    }

    @Test
    @DisplayName("Test: Copy with no file selected raises alert")
    public void testFX_copyNoFileSelected_Alert() throws IOException {
        final String archiveFormat = "jar";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(this, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");
        simAddFolder(this, dir);

        Path file = Paths.get("root", "level1c", "level1c1", "level2b");

        final String tableName = "#fileContentsView";
        simTraversalArchive(this, archiveName, tableName, (r)->{}, SSV.split(file.toString())).get();
        simUp(this);
        clickOn("#btnCopy");
        sleep(5, MILLISECONDS);
        clickOn("#mnuCopySelected");

        DialogPane dialogPane = lookup(".dialog-pane").query();
        Assertions.assertTrue(dialogPane.getContentText().startsWith("Copy could not be initiated"), "The text in warning dialog was not matched as expected");
    }

    @Test
    @DisplayName("Test: Copy on folder raises alert")
    public void testFX_copyFolderSelected_Alert() throws IOException {
        final String archiveFormat = "jar";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(this, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");
        simAddFolder(this, dir);

        Path file = Paths.get("root", "level1c", "level1c1", "level2b");

        final String tableName = "#fileContentsView";
        simTraversalArchive(this, archiveName, tableName, (r)->{}, SSV.split(file.toString())).get();
        clickOn("#btnCopy");
        sleep(5, MILLISECONDS);
        clickOn("#mnuCopySelected");

        DialogPane dialogPane = lookup(".dialog-pane").query();
        Assertions.assertTrue(dialogPane.getContentText().startsWith("Copy could not be initiated"), "The text in warning dialog was not matched as expected");
    }

    @Test
    @DisplayName("Test: Copy file into empty directory")
    public void testFX_copyFileIntoEmptyDirectory_Success() throws IOException {
        final String archiveFormat = "jar";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(this, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");
        simAddFolder(this, dir);
        simAddFolder(this, emptyDir);

        Path file = Paths.get("root", "level1c", "level1c1", "level2b", "COPY_UP.txt");

        final String tableName = "#fileContentsView";
        simTraversalArchive(this, archiveName, tableName, (r)->{}, SSV.split(file.toString()));
        clickOn("#btnCopy");
        sleep(5, MILLISECONDS);
        clickOn("#mnuCopySelected");
        sleep(5, MILLISECONDS);
        simUp(this);
        sleep(5, MILLISECONDS);
        simUp(this);
        sleep(5, MILLISECONDS);
        simUp(this);
        sleep(5, MILLISECONDS);
        simUp(this);
        sleep(5, MILLISECONDS);
        simTraversalArchive(this, archiveName, tableName, this::doubleClickOn, "empty-dir");
        TableView<FileInfo> fileContentsView = FormUtil.lookupNode(s->s.getTitle().contains(archiveName),
                                                                   tableName);
        clickOn(fileContentsView, MouseButton.SECONDARY);
        sleep(5, MILLISECONDS);
        clickOn("#mnuPaste");
        sleep(5, MILLISECONDS);

        FXArchiveInfo info = lookupArchiveInfo(archiveName).get();
        Assertions.assertEquals(1,
                                info.getFiles().stream().filter(f->f.getFileName().equals(Paths.get("empty-dir",
                                                                                                    "COPY_UP.txt").toString())).count(),
                                "File was not copied");
    }

    @Test
    @DisplayName("Test: Copy file within a non-existent archive will yield the appropriate alert")
    public void testFX_copyFileNonExistentArchive_Alert() throws IOException {
        final Path srcPath = Paths.get("src", "test", "resources", "test.zip")
                                  .toAbsolutePath();
        final Path archivePath = Paths.get(Files.createTempDirectory("pz").toAbsolutePath().toString(), "test.zip")
                                      .toAbsolutePath();
        Files.copy(srcPath, archivePath);

        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Select file to copy...
        TableView<FileInfo> fileContentsView = lookup("#fileContentsView").queryAs(TableView.class);
        sleep(100, MILLISECONDS);
        FormUtil.selectTableViewEntry(this, fileContentsView, FileInfo::getFileName,
                                      "first-file").get();

        // Delete archive...
        Files.deleteIfExists(archivePath);

        // Try to extract...
        clickOn("#btnCopy", MouseButton.PRIMARY);
        sleep(50, MILLISECONDS);

        clickOn("#mnuCopySelected", MouseButton.PRIMARY);
        sleep(50, MILLISECONDS);

        // Assert alert expectations
        DialogPane dialogPane = lookup(".dialog-pane").queryAs(DialogPane.class);
        Assertions.assertTrue(dialogPane.getContentText().matches("Archive .* does not exist. PearlZip will now close the instance."), "The text in warning dialog was not matched as expected");
    }
}
