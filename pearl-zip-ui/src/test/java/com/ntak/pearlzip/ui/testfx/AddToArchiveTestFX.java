/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.testfx;

import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.archive.util.CompressUtil;
import com.ntak.pearlzip.ui.UITestFXSuite;
import com.ntak.pearlzip.ui.UITestSuite;
import com.ntak.pearlzip.ui.constants.ZipConstants;
import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.util.AbstractPearlZipTestFX;
import com.ntak.pearlzip.ui.util.JFXUtil;
import com.ntak.pearlzip.ui.util.PearlZipFXUtil;
import com.ntak.testfx.FormUtil;
import com.ntak.testfx.NativeFileChooserUtil;
import javafx.geometry.Point2D;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.ui.constants.ZipConstants.CK_LOCAL_TEMP;
import static com.ntak.pearlzip.ui.constants.ZipConstants.CK_STORE_TEMP;
import static com.ntak.pearlzip.ui.util.PearlZipFXUtil.*;
import static com.ntak.testfx.TestFXConstants.PLATFORM;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Tag("fx-test")
public class AddToArchiveTestFX extends AbstractPearlZipTestFX {

    private static Path tempDirRoot;
    private static Path dir;

    /*
     * Test cases:
     * + Add folder to zip archive
     * + Add folder to tar archive
     * + Add folder to jar archive
     * + Add symbolic soft link file to zip archive
     * + Add symbolic hard link file and document file to tar archive
     * + Add image file to jar archive
     * + Add long name file to tar archive
     * + Table context menu Add File
     * + Table context menu Add Directory
     * + Add file to a no longer existing archive
     * + Add folder to a no longer existing archive
     * + Add self to archive raises warning
     * + Add directory with self to archive. Ignores self on addition
     * + Add nested empty directory
     * + Add identical nested archives on sister directories. Ensure changes to each archive are independent
     * + Open tar and zip folder in archive
     * + Nesting archives one after the other in a chain works in the expected manner
     * + Adding nested .tgz can be added successfully and opened as an archive
     * + Open a single file (non-tarball) compressor archive successfully
     * + Add file and directory using off-row context menu
     */

    @BeforeEach
    public void setUp() {
        try {
            tempDirRoot = Files.createTempDirectory("pz");
            dir = UITestFXSuite.genSourceDataSet();
        } catch(IOException e) {
        }
    }

    @AfterEach
    public void tearDown() {
        try {
            for (Path dir :
                    Files.list(dir.getParent()
                                  .getParent())
                         .filter(p -> p.getFileName()
                                       .toString()
                                       .startsWith("pz"))
                         .collect(
                                 Collectors.toSet())) {
                UITestSuite.clearDirectory(dir);
            }
            Files.list(InternalContextCache.GLOBAL_CONFIGURATION_CACHE
                                           .<Path>getAdditionalConfig(CK_STORE_TEMP)
                                           .get())
                 .forEach((d)->{
                try {
                    UITestSuite.clearDirectory(d);
                } catch(IOException e) {
                }
            });
        } catch(Exception e) {
        }
    }

    @Test
    @DisplayName("Test: Add folder to zip archive and verify contents")
    // GIVEN a new zip archive has been created in PearlZip
    // WHEN folder added to archive
    // THEN archive contents structure match expectations
    public void testFX_AddFolderToZipArchive_MatchExpectations() throws IOException {
        final String archiveFormat = "zip";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);
        simAddDirectoryToNewNonCompressorArchive(this, archive, dir, false);
    }

    @Test
    @DisplayName("Test: Add folder using context menu to zip archive and verify contents")
    // GIVEN a new zip archive has been created in PearlZip
    // WHEN folder added using context menu to archive
    // THEN archive contents structure match expectations
    public void testFX_AddFolderCtxMenuToZipArchive_MatchExpectations() throws IOException {
        final String archiveFormat = "zip";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);
        simAddDirectoryToNewNonCompressorArchive(this, archive, dir, true);
    }

    @Test
    @DisplayName("Test: Add folder to tar archive and verify contents")
    // GIVEN a new tar archive has been created in PearlZip
    // WHEN folder added to archive
    // THEN archive contents structure match expectations
    public void testFX_AddFolderToTarArchive_MatchExpectations() throws IOException {
        final String archiveFormat = "tar";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);
        simAddDirectoryToNewNonCompressorArchive(this, archive, dir, false);
    }

    @Test
    @DisplayName("Test: Add folder to jar archive and verify contents")
    // GIVEN a new jar archive has been created in PearlZip
    // WHEN folder added to archive
    // THEN archive contents structure match expectations
    public void testFX_AddFolderToJarArchive_MatchExpectations() throws IOException {
        final String archiveFormat = "jar";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);
        simAddDirectoryToNewNonCompressorArchive(this, archive, dir, false);
    }

    @Test
    @DisplayName("Test: Add symbolic soft link file to zip archive and verify contents")
    // GIVEN a new zip archive has been created in PearlZip
    // WHEN file added to archive (symbolic link)
    // THEN ensure symbolic link is followed and original file is stored in the archive
    public void testFX_AddSymSoftLinkFileToZipArchive_MatchExpectations() throws IOException {
        final String archiveFormat = "zip";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        Path file = Paths.get("src", "test", "resources", "test.lnk")
                         .toAbsolutePath();
        final long sourceHash = CompressUtil.crcHashFile(file.toFile());
        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);
        simNewArchive(this, archive);
        simAddFile(this, file);
        push(KeyCode.ENTER);
        TableView<FileInfo> fileContentsView = FormUtil.lookupNode(s -> s.getTitle()
                                                                         .contains(archiveName),
                                                                   "#fileContentsView");
        Assertions.assertEquals("test.zip",
                                fileContentsView.getItems()
                                                .get(0)
                                                .getFileName(),
                                "Shortcut was not followed to original file");

        // Extract file and check consistency
        Path targetFile = Paths.get(dir.getParent()
                                       .toAbsolutePath()
                                       .toString(),
                                    file.getFileName()
                                        .toString())
                               .toAbsolutePath();
        FormUtil.selectTableViewEntry(this,
                                      fileContentsView,
                                      FileInfo::getFileName,
                                      file.getFileName()
                                          .toString());
        simExtractFile(this, targetFile);
        final long targetHash = CompressUtil.crcHashFile(targetFile.toFile());

        Assertions.assertEquals(sourceHash, targetHash, "File hashes were not identical");
    }

    @Test
    @DisplayName("Test: Add symbolic hard link and document file to tar archive and verify contents")
    // GIVEN a new tar archive has been created in PearlZip
    // WHEN file added to archive (hard link)
    //     AND file added to archive (docx)
    // THEN ensure files are stored in the archive and integrity has been maintained
    public void testFX_AddSymHardLinkFileToTarArchive_MatchExpectations() throws IOException {
        final String archiveFormat = "tar";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        Path fileHardLink = Paths.get("src", "test", "resources", "test-hard.lnk")
                                 .toAbsolutePath();
        Path fileDoc = Paths.get("src", "test", "resources", "test.docx")
                            .toAbsolutePath();
        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);
        simNewArchive(this, archive);
        simAddFile(this, fileHardLink);
        push(KeyCode.ENTER);
        simAddFile(this, fileDoc);
        push(KeyCode.ENTER);

        TableView<FileInfo> fileContentsView = FormUtil.lookupNode(s -> s.getTitle()
                                                                         .contains(archiveName),
                                                                   "#fileContentsView");
        final List<String> files = fileContentsView.getItems()
                                                   .stream()
                                                   .map(FileInfo::getFileName)
                                                   .collect(Collectors.toList());
        Assertions.assertTrue(files.contains("test-hard.lnk"),
                              "Hard link was not found in archive");
        Assertions.assertTrue(files.contains("test.docx"), "Document was not found in archive");

        // Extract file and check consistency
        for (Path file : Arrays.asList(fileDoc, fileHardLink)) {
            final long sourceHash = CompressUtil.crcHashFile(file.toFile());
            Path targetFile = Paths.get(dir.getParent()
                                           .toAbsolutePath()
                                           .toString(),
                                        file.getFileName()
                                            .toString())
                                   .toAbsolutePath();
            FormUtil.selectTableViewEntry(this,
                                          fileContentsView,
                                          FileInfo::getFileName,
                                          file.getFileName()
                                              .toString());
            simExtractFile(this, targetFile);
            final long targetHash = CompressUtil.crcHashFile(targetFile.toFile());

            Assertions.assertEquals(sourceHash, targetHash, "File hashes were not identical");
        }
    }

    @Test
    @DisplayName("Test: Add image file using context menu to jar archive and verify contents")
    // GIVEN a new zip archive has been created in PearlZip
    // WHEN file added to archive (image)
    // THEN ensure files are stored in the archive and integrity has been maintained
    public void testFX_AddImageFileCtxMenuToZipArchive_MatchExpectations() throws IOException {
        final String archiveFormat = "jar";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        Path file = Paths.get("src", "test", "resources", "img.png")
                         .toAbsolutePath();
        final long sourceHash = CompressUtil.crcHashFile(file.toFile());
        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);
        simNewArchive(this, archive);
        simAddFile(this, file, true, archiveName);

        TableView<FileInfo> fileContentsView = FormUtil.lookupNode(s -> s.getTitle()
                                                                         .contains(archiveName),
                                                                   "#fileContentsView");
        final List<String> files = fileContentsView.getItems()
                                                   .stream()
                                                   .map(FileInfo::getFileName)
                                                   .collect(Collectors.toList());
        Assertions.assertTrue(files.contains("img.png"),
                              "Image was not found in archive");

        // Extract file and check consistency
        Path targetFile = Paths.get(dir.getParent()
                                       .toAbsolutePath()
                                       .toString(),
                                    file.getFileName()
                                        .toString())
                               .toAbsolutePath();
        FormUtil.selectTableViewEntry(this,
                                      fileContentsView,
                                      FileInfo::getFileName,
                                      file.getFileName()
                                          .toString());
        simExtractFile(this, targetFile);
        final long targetHash = CompressUtil.crcHashFile(targetFile.toFile());

        Assertions.assertEquals(sourceHash, targetHash, "File hashes were not identical");
    }

    @Test
    @DisplayName("Test: Add image file to jar archive and verify contents")
    // GIVEN a new jar archive has been created in PearlZip
    // WHEN file added to archive (image)
    // THEN ensure files are stored in the archive and integrity has been maintained
    public void testFX_AddImageFileToJarArchive_MatchExpectations() throws IOException {
        final String archiveFormat = "jar";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        Path file = Paths.get("src", "test", "resources", "img.png")
                         .toAbsolutePath();
        final long sourceHash = CompressUtil.crcHashFile(file.toFile());
        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);
        simNewArchive(this, archive);
        simAddFile(this, file);

        TableView<FileInfo> fileContentsView = FormUtil.lookupNode(s -> s.getTitle()
                                                                         .contains(archiveName),
                                                                   "#fileContentsView");
        final List<String> files = fileContentsView.getItems()
                                                   .stream()
                                                   .map(FileInfo::getFileName)
                                                   .collect(Collectors.toList());
        Assertions.assertTrue(files.contains("img.png"),
                              "Image was not found in archive");

        // Extract file and check consistency
        Path targetFile = Paths.get(dir.getParent()
                                       .toAbsolutePath()
                                       .toString(),
                                    file.getFileName()
                                        .toString())
                               .toAbsolutePath();
        FormUtil.selectTableViewEntry(this,
                                      fileContentsView,
                                      FileInfo::getFileName,
                                      file.getFileName()
                                          .toString());
        simExtractFile(this, targetFile);
        final long targetHash = CompressUtil.crcHashFile(targetFile.toFile());

        Assertions.assertEquals(sourceHash, targetHash, "File hashes were not identical");
    }

    @Test
    @DisplayName("Test: Add long name file to tar archive and verify contents")
    // GIVEN a new tar archive has been created in PearlZip
    // WHEN file added to archive (long filename)
    // THEN ensure files are stored in the archive and integrity has been maintained
    public void testFX_AddLongNameFileToTarArchive_MatchExpectations() throws IOException {
        final String archiveFormat = "tar";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        Path file =
                Paths.get(ZipConstants.GLOBAL_INTERNAL_CACHE
                                      .<Path>getAdditionalConfig(CK_LOCAL_TEMP)
                                      .get()
                                      .toAbsolutePath()
                                      .toString(),
                          "QuickBrownFoxJumpsOverTheLazyDog01234567890_QuickBrownFoxJumpsOverTheLazyDog01234567890_QuickBrownFoxJumpsOverTheLazyDog01234567890_QuickBrownFoxJumpsOverTheLazyDog01234567890");
        Files.deleteIfExists(file);
        Files.createFile(file);
        final long sourceHash = CompressUtil.crcHashFile(file.toFile());
        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);
        simNewArchive(this, archive);
        simAddFile(this, file);

        TableView<FileInfo> fileContentsView = FormUtil.lookupNode(s -> s.getTitle()
                                                                         .contains(archiveName),
                                                                   "#fileContentsView");
        final List<String> files = fileContentsView.getItems()
                                                   .stream()
                                                   .map(FileInfo::getFileName)
                                                   .collect(Collectors.toList());
        Assertions.assertTrue(files.contains(file.getFileName()
                                                 .toString()),
                              "File was not found in archive");

        // Extract file and check consistency
        Path targetFile = Paths.get(dir.getParent()
                                       .toAbsolutePath()
                                       .toString(),
                                    file.getFileName()
                                        .toString())
                               .toAbsolutePath();
        FormUtil.selectTableViewEntry(this,
                                      fileContentsView,
                                      FileInfo::getFileName,
                                      file.getFileName()
                                          .toString()
                                          .substring(0, 100));
        simExtractFile(this, targetFile);
        final long targetHash = CompressUtil.crcHashFile(targetFile.toFile());

        Assertions.assertEquals(sourceHash, targetHash, "File hashes were not identical");
    }

    @Test
    @DisplayName("Test: Add file to a non-existent archive will raise the appropriate exception alert")
    // GIVEN a new zip archive has been created in PearlZip
    // WHEN archive is deleted
    //     AND file added to archive
    // THEN a dialog appears with message like "Archive .* does not exist. PearlZip will now close the instance."
    public void testFX_AddFileNonExistentArchive_Fail() throws IOException {
        final String archiveFormat = "zip";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);
        simNewArchive(this, archive);
        Files.deleteIfExists(archive);

        clickOn("#btnAdd", MouseButton.PRIMARY);
        sleep(50, MILLISECONDS);

        clickOn("#mnuAddFile", MouseButton.PRIMARY);
        sleep(50, MILLISECONDS);

        DialogPane dialogPane = lookup(".dialog-pane").queryAs(DialogPane.class);
        Assertions.assertTrue(dialogPane.getContentText()
                                        .matches("Archive .* does not exist. PearlZip will now close the instance."),
                              "The text in warning dialog was not matched as expected");
    }

    @Test
    @DisplayName("Test: Add folder to a non-existent archive will raise the appropriate exception alert")
    // GIVEN a new zip archive has been created in PearlZip
    // WHEN archive is deleted
    //     AND folder added to archive
    // THEN a dialog appears with message like "Archive .* does not exist. PearlZip will now close the instance."
    public void testFX_AddFolderNonExistentArchive_Fail() throws IOException {
        final String archiveFormat = "zip";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);
        simNewArchive(this, archive);
        Files.deleteIfExists(archive);

        clickOn("#btnAdd", MouseButton.PRIMARY);
        sleep(50, MILLISECONDS);

        clickOn("#mnuAddDir", MouseButton.PRIMARY);
        sleep(50, MILLISECONDS);

        DialogPane dialogPane = lookup(".dialog-pane").queryAs(DialogPane.class);
        Assertions.assertTrue(dialogPane.getContentText()
                                        .matches("Archive .* does not exist. PearlZip will now close the instance."),
                              "The text in warning dialog was not matched as expected");
    }

    @Test
    @DisplayName("Test: Add self to archive raises warning")
    // GIVEN a new zip archive has been created in PearlZip
    // WHEN file added to archive (self)
    // THEN a dialog appears with message like "Ignoring the addition of file .* into the archive .*"
    public void testFX_AddSelfToArchive_Warn() throws IOException {
        // Set up archive information
        final Path tempDirectory = Files.createTempDirectory("pz")
                                        .toAbsolutePath();
        try {
            Path archive = Paths.get(tempDirectory.toString(), "empty.zip");
            simNewArchive(this, archive);

            FXArchiveInfo archiveInfo = JFXUtil.lookupArchiveInfo("empty.zip")
                                               .get();
            simAddFile(this, archive);
            sleep(250, MILLISECONDS);

            // Check failure
            DialogPane dialogPane = lookup(".dialog-pane").query();
            Assertions.assertTrue(dialogPane.getContentText()
                                            .matches("Ignoring the addition of file .* into the archive .*"),
                                  "The text in warning dialog was not matched as expected");
            clickOn(dialogPane.lookupButton(ButtonType.OK));
            sleep(250, MILLISECONDS);
            Assertions.assertEquals(0,
                                    archiveInfo.getFiles()
                                               .size(),
                                    "Archive was not empty");
        } finally {
            UITestSuite.clearDirectory(tempDirectory);
        }
    }

    @Test
    @DisplayName("Test: Add directory with self to archive. Ignores self on addition")
    // GIVEN a new zip archive has been created in PearlZip
    // WHEN folder added to archive (including self)
    // THEN ensure file (self) is not included in archive
    public void testFX_AddDirectoryWithSelf_Ignore() throws IOException {
        final String archiveFormat = "zip";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);
        simNewArchive(this, archive);
        simAddFolder(this, archive.getParent());
        sleep(100, MILLISECONDS);

        Optional<FXArchiveInfo> optArchiveInfo = lookupArchiveInfo(archiveName);
        Assertions.assertTrue(optArchiveInfo.isPresent(), "Archive window not open");
        Assertions.assertTrue(optArchiveInfo.get()
                                            .getFiles()
                                            .stream()
                                            .noneMatch(f -> f.getFileName()
                                                             .endsWith(archiveName)), "Archive was added unexpectedly");
    }

    @Test
    @DisplayName("Test: Nest zip archive into the parent zip archive and verify contents is as expected")
    // GIVEN a new zip archive has been created in PearlZip
    // WHEN file added to archive (another zip archive)
    //     AND nested file (nested-archive.zip) opened from PearlZip
    // THEN ensure the number of files in archive = 1
    // WHEN file added to archive (temp file)
    //     AND close nested archive and save = true
    //     AND nested file (nested-archive.zip) opened from PearlZip
    // THEN ensure files (temp file, 1) is included in the archive
    public void testFX_CreateZipArchiveAndUpdateNestedZipArchive_Success() throws IOException {
        // Create archive
        String archiveFormat = "zip";
        final String archiveName = String.format("nest-test.%s", archiveFormat);
        Path archivePath = Paths.get(tempDirRoot.toAbsolutePath()
                                                .toString(), archiveName);
        final String nestedArchiveName = "nested-archive.zip";
        final Path nestedArchivePath = Paths.get("src", "test", "resources", nestedArchiveName)
                                            .toAbsolutePath();
        final Path file = Files.createTempFile("", "");
        Files.deleteIfExists(file);
        Files.createFile(file);
        PearlZipFXUtil.simNewArchive(this, archivePath);

        // Add nested archive
        PearlZipFXUtil.simAddFile(this, nestedArchivePath);
        sleep(50, MILLISECONDS);

        // Open nested zip archive
        TableRow row = PearlZipFXUtil.simTraversalArchive(this, archiveName, "#fileContentsView", (r) -> {},
                                                          nestedArchiveName)
                                     .get();
        sleep(250, MILLISECONDS);
        doubleClickOn(row);

        // Verify nested archive is empty
        FXArchiveInfo archiveInfo = PearlZipFXUtil.lookupArchiveInfo(nestedArchiveName)
                                                  .get();
        Assertions.assertEquals(1,
                                archiveInfo.getFiles()
                                           .size(),
                                "The nested archive was not in the expected state");

        // Add file and folder to archive
        PearlZipFXUtil.simAddFile(this, file);
        sleep(50, MILLISECONDS);

        // Exit nested archive and save archive into parent archive
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 160));
        sleep(50, MILLISECONDS);
        DialogPane dialogPane = lookup(".dialog-pane").query();
        Assertions.assertTrue(dialogPane.getContentText()
                                        .startsWith(
                                                "Please specify if you wish to persist the changes of the nested archive"));
        clickOn(dialogPane.lookupButton(ButtonType.YES));
        sleep(250, MILLISECONDS);

        // Open nested archive and verify existence of files/folders
        doubleClickOn(row);
        Assertions.assertEquals(2,
                                archiveInfo.getFiles()
                                           .size(),
                                "The nested archive has not stored the expected files");
        Assertions.assertTrue(archiveInfo.getFiles()
                                         .stream()
                                         .anyMatch(f -> f.getLevel() == 0 && f.getFileName()
                                                                              .equals(file.getFileName()
                                                                                          .toString()) && !f.isFolder()),
                              "Expected top-level file was not found");
        Assertions.assertTrue(archiveInfo.getFiles()
                                         .stream()
                                         .anyMatch(f -> f.getLevel() == 0 && f.getFileName()
                                                                              .equals("1") && !f.isFolder()),
                              "Expected pre-existing top-level file was not found");
        sleep(50, MILLISECONDS);

        Files.deleteIfExists(file);
    }

    @Test
    @DisplayName("Test: Add nested empty directory to tar archive one after the other")
    // GIVEN a new tar archive has been created in PearlZip
    // WHEN folder added to archive (empty directory)
    //     AND traverse archive (empty)
    //     AND folder added to archive (empty directory)
    //     AND traverse archive (empty)
    //     AND folder added to archive (empty directory)
    //     AND traverse up to root folder in archive
    //     AND traverse archive (empty/empty/empty)
    // THEN ensure the number of files in archive = 3
    public void testFX_AddNestedEmptyDirectoryTarArchive_MatchExpectations() throws IOException {
        final String archiveFormat = "tar";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);
        final Path emptyDir = Files.createTempDirectory("empty");
        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);
        try {
            simNewArchive(this, archive);
            simAddFolder(this, emptyDir);
            sleep(100, MILLISECONDS).clickOn("#fileContentsView")
                                    .sleep(100, MILLISECONDS);
            simTraversalArchive(this,
                                archiveName,
                                "#fileContentsView",
                                (r) -> {},
                                emptyDir.getFileName()
                                        .toString());
            sleep(100, MILLISECONDS).doubleClickOn(MouseButton.PRIMARY);
            simAddFolder(this, emptyDir);
            simUp(this);
            simTraversalArchive(this,
                                archiveName,
                                "#fileContentsView",
                                (r) -> {},
                                emptyDir.getFileName()
                                        .toString(),
                                emptyDir.getFileName()
                                        .toString());
            sleep(100, MILLISECONDS).doubleClickOn(MouseButton.PRIMARY);
            simAddFolder(this, emptyDir);
            simUp(this);
            simUp(this);
            simTraversalArchive(this,
                                archiveName,
                                "#fileContentsView",
                                (r) -> {},
                                emptyDir.getFileName()
                                        .toString(),
                                emptyDir.getFileName()
                                        .toString(),
                                emptyDir.getFileName()
                                        .toString());
            sleep(100, MILLISECONDS).doubleClickOn(MouseButton.PRIMARY);
            Optional<FXArchiveInfo> optArchiveInfo = lookupArchiveInfo(archiveName);
            Assertions.assertEquals(3,
                                    optArchiveInfo.get()
                                                  .getFiles()
                                                  .size(),
                                    "The expected number of files was not added");
        } finally {
            Files.deleteIfExists(emptyDir);
        }
    }

    @Test
    @DisplayName("Test: Add identical nested archives on sister directories. Ensure changes to each archive are independent")
    // GIVEN a new (outer-archive.zip) zip archive has been created in PearlZip
    // WHEN folder added to archive (tempA)
    //      AND folder added to archive (tempB)
    //      AND traverse archive (tempA)
    //      AND file added to archive (empty-archive.tar)
    //      AND traverse up to root folder in archive
    //      AND traverse archive (tempB)
    //      AND file added to archive (empty-archive.tar)
    //      AND traverse up to root folder in archive
    //      AND traverse archive (tempA)

    //      AND nested file (tempA/empty-archive.tar) opened from PearlZip
    //      AND folder added to archive (tempA)
    //      AND close nested archive and save = true
    //      AND nested file (tempA/empty-archive.tar) opened from PearlZip
    // THEN ensure the number of files in archive = 1
    //      AND ensure files (tempA) is included in the archive

    //      AND traverse up to root folder in archive
    //      AND nested file (tempB/empty-archive.tar) opened from PearlZip
    //      AND folder added to archive (tempB)
    //      AND close nested archive and save = true
    //      AND nested file (tempA/empty-archive.tar) opened from PearlZip
    // THEN ensure the number of files in archive = 1
    //      AND ensure files (tempB) is included in the archive
    public void testFX_IdenticalNestedArchivesSisterDirectories_MatchExpectations() throws IOException {
        // Create archive
        final Path tempDirectory = Files.createTempDirectory("pz");
        Path archive = Paths.get(tempDirectory.toAbsolutePath()
                                              .toString(), "outer-archive.zip");
        simNewArchive(this, archive);

        // Create temp folders
        Path tempADir = Paths.get(tempDirectory.toAbsolutePath()
                                               .toString(), "tempA");
        Path tempBDir = Paths.get(tempDirectory.toAbsolutePath()
                                               .toString(), "tempB");
        Files.createDirectories(tempADir);
        Files.createDirectories(tempBDir);

        try {
            // Add temp folders
            simAddFolder(this, tempADir);
            simAddFolder(this, tempBDir);

            // Create temp files
            Path emptyArchive = Paths.get("src", "test", "resources", "empty-archive.tar")
                                     .toAbsolutePath();

            // Add archive to each folder
            TableRow row = simTraversalArchive(this,
                                               archive.toAbsolutePath()
                                                      .toString(),
                                               "#fileContentsView",
                                               (r) -> {},
                                               "tempA").get();
            doubleClickOn(row);
            simAddFile(this, emptyArchive);
            simUp(this);
            row = simTraversalArchive(this,
                                      archive.toAbsolutePath()
                                             .toString(),
                                      "#fileContentsView",
                                      (r) -> {},
                                      "tempB").get();
            doubleClickOn(row);
            simAddFile(this, emptyArchive);
            simUp(this);

            // Open each nested directory and add unique file

            // FIRST ARCHIVE
            row = simTraversalArchive(this,
                                      archive.toAbsolutePath()
                                             .toString(),
                                      "#fileContentsView",
                                      (r) -> {},
                                      "tempA",
                                      "empty-archive.tar").get();
            doubleClickOn(row);
            simAddFolder(this, tempADir);

            // Exit tarball instance and save archive into compressor
            clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 160));
            sleep(50, MILLISECONDS);
            DialogPane dialogPane = lookup(".dialog-pane").query();
            Assertions.assertTrue(dialogPane.getContentText()
                                            .startsWith(
                                                    "Please specify if you wish to persist the changes of the nested archive"));
            clickOn(dialogPane.lookupButton(ButtonType.YES));
            sleep(250, MILLISECONDS);

            // Open nested tarball archive and verify existence of files/folders
            doubleClickOn(row);
            sleep(250, MILLISECONDS);
            FXArchiveInfo archiveInfo = JFXUtil.lookupArchiveInfo("empty-archive.tar")
                                               .get();
            Assertions.assertEquals(1,
                                    archiveInfo.getFiles()
                                               .size(),
                                    "The nested archive has not stored the expected files");
            Assertions.assertEquals("tempA",
                                    archiveInfo.getFiles()
                                               .get(0)
                                               .getFileName(),
                                    "Folder added was not as expected");

            // Exit tarball instance and save archive into compressor
            clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 160));
            sleep(50, MILLISECONDS);
            dialogPane = lookup(".dialog-pane").query();
            Assertions.assertTrue(dialogPane.getContentText()
                                            .startsWith(
                                                    "Please specify if you wish to persist the changes of the nested archive"));
            clickOn(dialogPane.lookupButton(ButtonType.YES));
            sleep(250, MILLISECONDS);

            // Traverse to root
            simUp(this);

            // SECOND ARCHIVE
            row = simTraversalArchive(this,
                                      archive.toAbsolutePath()
                                             .toString(),
                                      "#fileContentsView",
                                      (r) -> {},
                                      "tempB",
                                      "empty-archive.tar").get();
            doubleClickOn(row);
            simAddFolder(this, tempBDir);

            // Exit tarball instance and save archive into compressor
            clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 160));
            sleep(50, MILLISECONDS);
            dialogPane = lookup(".dialog-pane").query();
            Assertions.assertTrue(dialogPane.getContentText()
                                            .startsWith(
                                                    "Please specify if you wish to persist the changes of the nested archive"));
            clickOn(dialogPane.lookupButton(ButtonType.YES));
            sleep(250, MILLISECONDS);

            // Open nested tarball archive and verify existence of files/folders
            doubleClickOn(row);
            sleep(250, MILLISECONDS);
            archiveInfo = JFXUtil.lookupArchiveInfo("empty-archive.tar")
                                 .get();
            Assertions.assertEquals(1,
                                    archiveInfo.getFiles()
                                               .size(),
                                    "The nested archive has not stored the expected files");
            Assertions.assertEquals("tempB",
                                    archiveInfo.getFiles()
                                               .get(0)
                                               .getFileName(),
                                    "Folder added was not as expected");

            // Exit tarball instance and save archive into compressor
            clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 160));
            sleep(50, MILLISECONDS);
            dialogPane = lookup(".dialog-pane").query();
            Assertions.assertTrue(dialogPane.getContentText()
                                            .startsWith(
                                                    "Please specify if you wish to persist the changes of the nested archive"));
            clickOn(dialogPane.lookupButton(ButtonType.YES));
        } finally {
            // Clean up
            UITestSuite.clearDirectory(tempDirectory);
        }
    }


    @Test
    @DisplayName("Test: Open tar or zip directory in zip archive (processing as folder and not an archive)")
    // GIVEN a new zip archive has been created in PearlZip
    //      AND directories (zip,tar) have been created
    // WHEN folder added to archive (zip)
    //      AND folder added to archive (tar)
    //      AND traverse archive (zip)
    //      AND traverse up to root folder in archive
    //      AND traverse archive (tar)
    //      AND traverse up to root folder in archive
    // THEN ensure only 1 main stage instances are open
    public void testFX_OpenTarZipFolderInZipArchive_Success() throws IOException {
        // Create archive
        final Path tempDirectory = Files.createTempDirectory("pz");
        Path archive = Path.of(JFXUtil.lookupArchiveInfo(".zip.*")
                                      .get()
                                      .getArchivePath());

        // Create temp folders
        Path zipDir = Paths.get(tempDirectory.toAbsolutePath()
                                             .toString(), "zip");
        Path tarDir = Paths.get(tempDirectory.toAbsolutePath()
                                             .toString(), "tar");
        Files.createDirectories(zipDir);
        Files.createDirectories(tarDir);

        try {
            // Add directories zip and tar
            simAddFolder(this, zipDir);
            simAddFolder(this, tarDir);

            // Navigate into each and ensure no new archive window is raised
            TableRow row = simTraversalArchive(this,
                                               archive.toAbsolutePath()
                                                      .toString(),
                                               "#fileContentsView",
                                               (r) -> {},
                                               "tar").get();
            doubleClickOn(row);
            simUp(this);

            row = simTraversalArchive(this,
                                      archive.toAbsolutePath()
                                             .toString(),
                                      "#fileContentsView",
                                      (r) -> {},
                                      "zip").get();
            doubleClickOn(row);
            simUp(this);

            Assertions.assertEquals(1,
                                    JFXUtil.getMainStageInstances()
                                           .size(),
                                    "New main windows instance unexpectedly created");
        } finally {
            UITestSuite.clearDirectory(tempDirectory);
        }
    }

    @Test
    @DisplayName("Test: Nesting archives one after the other in a chain works in the expected manner")
    // GIVEN a new zip archive has been created in PearlZip
    //      AND files (temp) have been created
    // WHEN file added to archive (test.tar.gz)
    //      AND nested file (test.tar.gz) opened from PearlZip
    //      AND nested file (test.tar) opened from PearlZip
    //      AND file added to archive (empty-archive.tar)
    //      AND nested file (empty-archive.tar) opened from PearlZip
    //      AND file added to archive (temp)
    //      AND close nested archive and save = true
    //      AND close nested archive and save = true
    //      AND close nested archive and save = true

    // THEN ensure files (test.tar.gz) is included in the archive

    // WHEN nested file (test.tar.gz) opened from PearlZip
    // THEN ensure files (test.tar) is included in the archive

    // WHEN nested file (test.tar) opened from PearlZip
    // THEN ensure files (empty-archive.tar) is included in the archive

    // WHEN nested file (empty-archive.tar) opened from PearlZip
    // THEN ensure files (temp) is included in the archive
    public void testFX_NestedArchiveChain_MatchExpectations() throws IOException {
        // Create temp file
        final Path tempDirectory = Files.createTempDirectory("pz");
        final Path tempFile = Paths.get(tempDirectory.toAbsolutePath()
                                                     .toString(), "temp");
        Files.createFile(tempFile);

        try {
            // Set up archive information
            FXArchiveInfo archiveInfo = JFXUtil.lookupArchiveInfo(".zip.*")
                                               .get();
            Path archive = Paths.get(archiveInfo.getArchivePath());

            // Nest compressor archive
            final Path nestTarGZ = Paths.get("src", "test", "resources", "test.tar.gz")
                                        .toAbsolutePath();
            simAddFile(this, nestTarGZ);
            TableRow row = simTraversalArchive(this,
                                               archive.toAbsolutePath()
                                                      .toString(),
                                               "#fileContentsView",
                                               (r) -> {},
                                               "test.tar.gz").get();
            doubleClickOn(row).sleep(250, MILLISECONDS);
            TableRow rowGZTar = simTraversalArchive(this,
                                                    nestTarGZ.getFileName()
                                                             .toString(),
                                                    "#fileContentsView",
                                                    (r) -> {},
                                                    "test.tar").get();
            doubleClickOn(rowGZTar).sleep(250, MILLISECONDS);

            // Nest non-compressor archive
            final Path nestTar = Paths.get("src", "test", "resources", "empty-archive.tar")
                                      .toAbsolutePath();
            simAddFile(this, nestTar);
            FXArchiveInfo nestedGZArchiveInfo = lookupArchiveInfo("test.tar").get();
            TableRow rowTar = simTraversalArchive(this,
                                                  nestedGZArchiveInfo.getArchivePath(),
                                                  "#fileContentsView",
                                                  (r) -> {},
                                                  "empty-archive.tar").get();
            doubleClickOn(rowTar).sleep(250, MILLISECONDS);

            // Add arbitrary file
            simAddFile(this, tempFile);
            sleep(250, MILLISECONDS);

            // Save down all changes...
            clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 160));
            sleep(50, MILLISECONDS);
            DialogPane dialogPane = lookup(".dialog-pane").query();
            Assertions.assertTrue(dialogPane.getContentText()
                                            .startsWith(
                                                    "Please specify if you wish to persist the changes of the nested archive"));
            clickOn(dialogPane.lookupButton(ButtonType.YES));
            sleep(250, MILLISECONDS);

            clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 160));
            sleep(50, MILLISECONDS);
            dialogPane = lookup(".dialog-pane").query();
            Assertions.assertTrue(dialogPane.getContentText()
                                            .startsWith(
                                                    "Please specify if you wish to persist the changes of the nested archive"));
            clickOn(dialogPane.lookupButton(ButtonType.YES));
            sleep(250, MILLISECONDS);

            clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 160));
            sleep(50, MILLISECONDS);
            dialogPane = lookup(".dialog-pane").query();
            Assertions.assertTrue(dialogPane.getContentText()
                                            .startsWith(
                                                    "Please specify if you wish to persist the changes of the nested archive"));
            clickOn(dialogPane.lookupButton(ButtonType.YES));
            sleep(250, MILLISECONDS);

            // Check hierarchy (temp.zip -> test.tar.gz -> test.tar -> empty-archive.tar)
            // 1) temp.zip - Check to ensure gzip file is present
            Assertions.assertTrue(archiveInfo.getFiles()
                                             .stream()
                                             .anyMatch(f -> f.getFileName()
                                                             .contains("test.tar.gz")),
                                  "G-Zip archive is not present");
            doubleClickOn(row);
            sleep(250, MILLISECONDS);
            rowGZTar = simTraversalArchive(this,
                                           nestTarGZ.getFileName()
                                                    .toString(),
                                           "#fileContentsView",
                                           (r) -> {},
                                           "test.tar").get();
            doubleClickOn(rowGZTar);
            sleep(250, MILLISECONDS);

            // 2) temp.tar - Check to ensure tar is present
            nestedGZArchiveInfo = lookupArchiveInfo("test.tar").get();
            Assertions.assertTrue(nestedGZArchiveInfo.getFiles()
                                                     .stream()
                                                     .anyMatch(f -> f.getFileName()
                                                                     .contains("empty-archive.tar")),
                                  "Tar archive is not present");
            rowTar = simTraversalArchive(this,
                                         nestedGZArchiveInfo.getArchivePath(),
                                         "#fileContentsView",
                                         (r) -> {},
                                         "empty-archive.tar").get();
            doubleClickOn(rowTar);

            // 3) empty-archive.tar - Check to ensure temp file is persisted
            FXArchiveInfo nestedTarArchiveInfo = JFXUtil.lookupArchiveInfo("empty-archive.tar")
                                                        .get();
            Assertions.assertTrue(nestedTarArchiveInfo.getFiles()
                                                      .stream()
                                                      .anyMatch(f -> f.getFileName()
                                                                      .contains("temp")),
                                  "temp file archive is not present");
        } finally {
            UITestSuite.clearDirectory(tempDirectory);
        }
    }

    @Test
    @DisplayName("Test: Adding nested .tgz can be added successfully and opened as an archive")
    // GIVEN a new zip archive has been created in PearlZip
    // WHEN file added to archive (empty.tgz)
    //     AND nested file (empty.tgz) opened from PearlZip
    // THEN ensure only 2 main stage instances are open
    //     AND A main stage instance name exists like '.*empty.tgz.*'
    public void testFX_AddNestedTgzArchive_Success() {
        // Archive set up
        FXArchiveInfo archiveInfo = JFXUtil.lookupArchiveInfo(".zip.*")
                                           .get();
        Path tgzArchive = Paths.get("src", "test", "resources", "empty.tgz").toAbsolutePath();

        // add tgz archive
        simAddFile(this, tgzArchive);
        sleep(250, MILLISECONDS);

        TableRow archive = simTraversalArchive(this, archiveInfo.getArchivePath(), "#fileContentsView", (r)->{},
                                              "empty.tgz").get();
        sleep(250, MILLISECONDS)
                .doubleClickOn(archive)
                .sleep(250, MILLISECONDS);

        // Checks
        Assertions.assertEquals(2, JFXUtil.getMainStageInstances().size(), "Two archive instances are not open");
        Assertions.assertTrue(JFXUtil.getMainStageInstances().stream().anyMatch((f)->f.getTitle().contains("empty.tgz")),
                              "The tgz archive was not opened successfully by PearlZip");
    }

    @Test
    @DisplayName("Test: Open a single file (non-tarball) compressor archive successfully")
    // GIVEN an archive (test.txt.xz) has been created in PearlZip
    // THEN ensure the number of files in archive = 1
    //     AND ensure files (test.txt) is included in the archive
    public void testFX_OpenSingleFileCompressor_Success() {
        // Archive set up
        Path xzArchive = Paths.get("src", "test", "resources", "test.txt.xz").toAbsolutePath();

        // Open archive
        simOpenArchive(this, xzArchive, true, false);
        sleep(250, MILLISECONDS);

        // Check contents
        FXArchiveInfo archiveInfo = JFXUtil.lookupArchiveInfo("test.txt.xz")
                                           .get();
        Assertions.assertEquals(1, archiveInfo.getFiles().size(), "Not a compressor archive");
        Assertions.assertTrue(archiveInfo.getFiles().get(0).getFileName().equals("test.txt"),
                              "Not the expected text file compressor");

        // Close archive...
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 160));
        sleep(250, MILLISECONDS);
    }

    @Test
    @DisplayName("Test: Add file and directory using off-row context menu")
    // GIVEN a new zip archive has been created in PearlZip
    //      AND directories (foo,bar) have been created
    //      AND files (boom,baa) have been created
    // WHEN folder added to archive (foo) using context menu
    //      AND traverse archive (foo)
    //      AND folder added to archive (boom) using context menu
    //      AND traverse up to root folder in archive
    //      AND folder added to archive (bar) using context menu
    //      AND traverse archive (bar)
    //      AND folder added to archive (baa) using context menu
    // THEN nsure the number of files in archive = 4
    public void testFX_AddFileAddFolderContextMenu_MatchExpectations() throws IOException {
        // Create temp file
        final Path tempDirectory = Files.createTempDirectory("pz");
        final Path emptyDirFoo = Paths.get(tempDirectory.toAbsolutePath()
                                                        .toString(), "foo");
        final Path emptyDirBar = Paths.get(tempDirectory.toAbsolutePath()
                                                        .toString(), "bar");
        final Path emptyFileBoom = Paths.get(tempDirectory.toAbsolutePath()
                                                          .toString(), "boom");
        final Path emptyFileBaa = Paths.get(tempDirectory.toAbsolutePath()
                                                         .toString(), "baa");

        Files.createDirectories(emptyDirFoo);
        Files.createDirectories(emptyDirBar);
        Files.createFile(emptyFileBoom);
        Files.createFile(emptyFileBaa);

        try {
            // Archive setup
            FXArchiveInfo archiveInfo = JFXUtil.lookupArchiveInfo(".zip.*")
                                               .get();

            // Add folders
            clickOn(archiveInfo.getController().get().getFileContentsView(), MouseButton.SECONDARY)
               .clickOn("#mnuAddDir");
            NativeFileChooserUtil.chooseFile(PLATFORM, this, emptyDirFoo);
            sleep(250,MILLISECONDS);
            simAddFolder(this, emptyDirBar);

            // Add file boom
            TableRow selectedRow = simTraversalArchive(this,
                                                       archiveInfo.getArchivePath(),
                                                       "#fileContentsView",
                                                       (r) -> {},
                                                       "foo").get();

            doubleClickOn(selectedRow).sleep(250, MILLISECONDS)
                                      .clickOn(selectedRow.getTableView(), MouseButton.SECONDARY)
                                      .clickOn("#mnuAddFile");
            NativeFileChooserUtil.chooseFile(PLATFORM, this, emptyFileBoom);

            // Navigate back to root
            simUp(this);

            // Add file baa
            selectedRow = simTraversalArchive(this, archiveInfo.getArchivePath(), "#fileContentsView", (r) -> {},
                                              "bar").get();

            doubleClickOn(selectedRow).sleep(250, MILLISECONDS)
                                      .clickOn(selectedRow.getTableView(), MouseButton.SECONDARY)
                                      .clickOn("#mnuAddFile");
            NativeFileChooserUtil.chooseFile(PLATFORM, this, emptyFileBaa);
        } finally {
            Files.deleteIfExists(emptyDirFoo);
            Files.deleteIfExists(emptyDirBar);
            Files.deleteIfExists(emptyFileBoom);
            Files.deleteIfExists(emptyFileBaa);
        }
    }
}
