/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.testfx;

import com.ntak.pearlzip.archive.util.CompressUtil;
import com.ntak.pearlzip.ui.UITestFXSuite;
import com.ntak.pearlzip.ui.UITestSuite;
import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.util.AbstractPearlZipTestFX;
import com.ntak.pearlzip.ui.util.PearlZipFXUtil;
import com.ntak.pearlzip.ui.util.PearlZipSpecifications;
import com.ntak.testfx.TestFXConstants;
import com.ntak.testfx.specifications.CommonSpecifications;
import javafx.scene.control.TableRow;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.ui.constants.ZipConstants.CK_STORE_TEMP;
import static com.ntak.pearlzip.ui.util.PearlZipFXUtil.*;
import static com.ntak.testfx.TestFXConstants.LONG_PAUSE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Tag("fx-test")
public class AddToArchiveTestFX extends AbstractPearlZipTestFX {


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
        // Preparation
        Map<Integer,Map<String,String[]>> expectations = genArchiveContentsExpectationsAuto(dir);

        // Given
        Path archiveName = PearlZipSpecifications.givenCreateNewArchive(this, "zip");

        // When
        simAddFolder(this, dir, false, archiveName.toAbsolutePath().toString());

        // Then
        checkArchiveFileHierarchy(this, expectations, archiveName.toAbsolutePath().toString());
    }

    @Test
    @DisplayName("Test: Add folder using context menu to zip archive and verify contents")
    // GIVEN a new zip archive has been created in PearlZip
    // WHEN folder added using context menu to archive
    // THEN archive contents structure match expectations
    public void testFX_AddFolderCtxMenuToZipArchive_MatchExpectations() throws IOException {
        // Preparation
        Map<Integer,Map<String,String[]>> expectations = genArchiveContentsExpectationsAuto(dir);

        // Given
        Path archiveName = PearlZipSpecifications.givenCreateNewArchive(this, "zip");

        // When
        simAddFolder(this, dir, true, archiveName.toAbsolutePath().toString());

        // Then
        checkArchiveFileHierarchy(this, expectations, archiveName.toAbsolutePath().toString());
    }

    @Test
    @DisplayName("Test: Add folder to tar archive and verify contents")
    // GIVEN a new tar archive has been created in PearlZip
    // WHEN folder added to archive
    // THEN archive contents structure match expectations
    public void testFX_AddFolderToTarArchive_MatchExpectations() throws IOException {
        // Preparation
        Map<Integer,Map<String,String[]>> expectations = genArchiveContentsExpectationsAuto(dir);

        // Given
        Path archiveName = PearlZipSpecifications.givenCreateNewArchive(this, "tar");

        // When
        simAddFolder(this, dir, false, archiveName.toAbsolutePath().toString());

        // Then
        checkArchiveFileHierarchy(this, expectations, archiveName.toAbsolutePath().toString());
    }

    @Test
    @DisplayName("Test: Add folder to jar archive and verify contents")
    // GIVEN a new jar archive has been created in PearlZip
    // WHEN folder added to archive
    // THEN archive contents structure match expectations
    public void testFX_AddFolderToJarArchive_MatchExpectations() throws IOException {
        // Preparation
        Map<Integer,Map<String,String[]>> expectations = genArchiveContentsExpectationsAuto(dir);

        // Given
        Path archiveName = PearlZipSpecifications.givenCreateNewArchive(this, "jar");

        // When
        simAddFolder(this, dir, false, archiveName.toAbsolutePath().toString());

        // Then
        checkArchiveFileHierarchy(this, expectations, archiveName.toAbsolutePath().toString());
    }

    @Test
    @DisplayName("Test: Add symbolic soft link file to zip archive and verify contents")
    // GIVEN a new zip archive has been created in PearlZip
    // WHEN file added to archive (symbolic link)
    // THEN ensure expected file exists in archive (symbolic link is followed and persisted in archive)
    //     AND hash is consistent (between original and new file)
    public void testFX_AddSymSoftLinkFileToZipArchive_MatchExpectations() throws IOException {
        // Preparation
        Path file = Paths.get("src", "test", "resources", "test.lnk")
                         .toAbsolutePath();
        final long sourceHash = CompressUtil.crcHashFile(file.toFile());
        Path targetFile = dir.getParent().resolve("test.zip").toAbsolutePath();

        // Given
        Path archiveName = PearlZipSpecifications.givenCreateNewArchive(this, "zip");

        // When
        simAddFile(this, file);
        push(KeyCode.ENTER);

        // Then
        PearlZipSpecifications.thenExpectFileExistsInCurrentWindow(archiveName, targetFile.getFileName().toString());
        PearlZipSpecifications.thenExpectCRCHashFileEntryMatches(this, sourceHash, archiveName, targetFile.getFileName());
    }

    @Test
    @DisplayName("Test: Add symbolic hard link and document file to tar archive and verify contents")
    // GIVEN a new tar archive has been created in PearlZip
    // WHEN file added to archive (hard link)
    //     AND file added to archive (docx)
    // THEN ensure files are stored in the archive and integrity has been maintained
    public void testFX_AddSymHardLinkFileToTarArchive_MatchExpectations() throws IOException {
        // Preparation
        Path fileHardLink = Paths.get("src", "test", "resources", "test-hard.lnk")
                                 .toAbsolutePath();
        Path fileDoc = Paths.get("src", "test", "resources", "test.docx")
                            .toAbsolutePath();
        Path[] refPaths = {fileHardLink, fileDoc};
        Path[] entries = {fileHardLink.getFileName(), fileDoc.getFileName()};

        // Given
        Path archiveName = PearlZipSpecifications.givenCreateNewArchive(this, "tar");

        // When
        simAddFile(this, fileHardLink);
        push(KeyCode.ENTER);
        simAddFile(this, fileDoc);
        push(KeyCode.ENTER);

        // Then
        PearlZipSpecifications.thenCheckIntegrityOfExpectedFiles(this, refPaths, archiveName, entries);
    }

    @Test
    @DisplayName("Test: Add image file using context menu to zip archive and verify contents")
    // GIVEN a new zip archive has been created in PearlZip
    // WHEN file added to archive (image)
    // THEN ensure files are stored in the archive and integrity has been maintained
   public void testFX_AddImageFileCtxMenuToZipArchive_MatchExpectations() throws IOException {
        // Preparation
        Path file = Paths.get("src", "test", "resources", "img.png")
                         .toAbsolutePath();
        final long sourceHash = CompressUtil.crcHashFile(file.toFile());

        // Given
        Path archiveName = PearlZipSpecifications.givenCreateNewArchive(this, "zip");

        // When
        simAddFile(this, file, true, archiveName.toString());

        // Then
        PearlZipSpecifications.thenExpectFileExistsInCurrentWindow(archiveName, file.getFileName().toString());
        PearlZipSpecifications.thenExpectCRCHashFileEntryMatches(this, sourceHash, archiveName, file.getFileName());
    }

    @Test
    @DisplayName("Test: Add image file to jar archive and verify contents")
    // GIVEN a new jar archive has been created in PearlZip
    // WHEN file added to archive (image)
    // THEN ensure files are stored in the archive and integrity has been maintained
    public void testFX_AddImageFileToJarArchive_MatchExpectations() throws IOException {
        // Preparation
        Path file = Paths.get("src", "test", "resources", "img.png")
                         .toAbsolutePath();
        final long sourceHash = CompressUtil.crcHashFile(file.toFile());

        // Given
        Path archiveName = PearlZipSpecifications.givenCreateNewArchive(this, "jar");

        // When
        simAddFile(this, file, false, archiveName.toString());
        sleep(LONG_PAUSE, MILLISECONDS);

        // Then
        PearlZipSpecifications.thenExpectFileExistsInCurrentWindow(archiveName, file.getFileName().toString());
        PearlZipSpecifications.thenExpectCRCHashFileEntryMatches(this, sourceHash, archiveName, file.getFileName());

    }

    @Test
    @DisplayName("Test: Add long name file to tar archive and verify contents")
    // GIVEN a new tar archive has been created in PearlZip
    // WHEN file added to archive (long filename)
    // THEN ensure files are stored in the archive and integrity has been maintained
    public void testFX_AddLongNameFileToTarArchive_MatchExpectations() throws IOException {
        // Preparation
        Path file = Files.createTempDirectory("pz")
                         .resolve("QuickBrownFoxJumpsOverTheLazyDog01234567890_QuickBrownFoxJumpsOverTheLazyDog01234567890_QuickBrownFoxJumpsOverTheLazyDog01234567890_QuickBrownFoxJumpsOverTheLazyDog01234567890");
        Files.createFile(file);
        final long sourceHash = CompressUtil.crcHashFile(file.toFile());

        // Given
        Path archiveName = PearlZipSpecifications.givenCreateNewArchive(this, "tar");

        // When
        simAddFile(this, file);

        // Then
        PearlZipSpecifications.thenExpectFileExistsInCurrentWindow(archiveName, file.getFileName().toString());
        PearlZipSpecifications.thenExpectCRCHashFileEntryMatches(this, sourceHash, archiveName, file.getFileName());
    }

    @Test
    @DisplayName("Test: Add file to a non-existent archive will raise the appropriate exception alert")
    // GIVEN a new zip archive has been created in PearlZip
    // WHEN archive is deleted
    //     AND file added to archive
    // THEN a dialog appears with message like "Archive .* does not exist. PearlZip will now close the instance."
    public void testFX_AddFileNonExistentArchive_Fail() throws IOException {
        // Given
        Path archiveName = PearlZipSpecifications.givenCreateNewArchive(this, "zip");

        // When
        Files.deleteIfExists(archiveName);
        simAddFile(this, null, false, archiveName.toString());

        // Then
        CommonSpecifications.thenExpectDialogWithMatchingMessage(this, "Archive .* does not exist. PearlZip will now close the instance.");
    }

    @Test
    @DisplayName("Test: Add folder to a non-existent archive will raise the appropriate exception alert")
    // GIVEN a new zip archive has been created in PearlZip
    // WHEN archive is deleted
    //     AND folder added to archive
    // THEN a dialog appears with message like "Archive .* does not exist. PearlZip will now close the instance."
    public void testFX_AddFolderNonExistentArchive_Fail() throws IOException {
        // Given
        Path archiveName = PearlZipSpecifications.givenCreateNewArchive(this, "zip");

        // When
        Files.deleteIfExists(archiveName);
        simAddFolder(this, null, false, archiveName.toString());

        // Then
        CommonSpecifications.thenExpectDialogWithMatchingMessage(this, "Archive .* does not exist. PearlZip will now close the instance.");
    }

    @Test
    @DisplayName("Test: Add self to archive raises warning")
    // GIVEN a new zip archive has been created in PearlZip
    // WHEN file added to archive (self)
    // THEN a dialog appears with message like "Ignoring the addition of file .* into the archive .*"
    public void testFX_AddSelfToArchive_Warn() {
        // Given
        Path archiveName = PearlZipSpecifications.givenCreateNewArchive(this, "zip");

        // When
        simAddFile(this, archiveName, false, archiveName.toString());

        // Then
        CommonSpecifications.thenExpectDialogWithMatchingMessage(this, "Ignoring the addition of file .* into the archive .*");
    }

    @Test
    @DisplayName("Test: Add directory with self to archive. Ignores self on addition")
    // GIVEN a new zip archive has been created in PearlZip
    // WHEN folder added to archive (including self)
    // THEN ensure file (self) is not included in archive
    public void testFX_AddDirectoryWithSelf_Ignore() {
        // Given
        Path archiveName = PearlZipSpecifications.givenCreateNewArchive(this, "zip");

        // When
        simAddFolder(this, archiveName.getParent(), false, archiveName.toString());

        // Then
        PearlZipSpecifications.thenExpectFileNotExistsInCurrentWindow(archiveName, archiveName.getParent().getFileName().resolve(archiveName.getFileName()).toString());
    }

    @Test
    @DisplayName("Test: Nest zip archive into the parent zip archive and verify contents is as expected")
    // GIVEN a new zip archive has been created in PearlZip
    // WHEN file added to archive (another zip archive)
    //     AND nested file (nested-archive.zip) opened from PearlZip
    // THEN ensure the number of files in archive = 1 (nested archive is in correct initial state)
    // WHEN file added to archive (temp file)
    //     AND close nested archive and save = true
    //     AND nested file (nested-archive.zip) opened from PearlZip
    // THEN ensure files (temp file, 1) is included in the archive
    public void testFX_CreateZipArchiveAndUpdateNestedZipArchive_Success() throws IOException {
        // Preparation
        final Path nestedArchivePath = Paths.get("src", "test", "resources", "nested-archive.zip")
                                            .toAbsolutePath();
        final Path file = Files.createTempFile("", "");

        // Given
        Path archiveName = PearlZipSpecifications.givenCreateNewArchive(this, "zip");

        // When
        simAddFile(this, nestedArchivePath, false, archiveName.toString());
        PearlZipSpecifications.whenOpenNestedEntry(this, archiveName.toString(), nestedArchivePath.getFileName().toString());

        // Then
        PearlZipSpecifications.thenExpectNumberOfFilesInCurrentWindow(nestedArchivePath.getFileName(), 1);

        // When
        PearlZipFXUtil.simAddFile(this, file);

        // Then
        PearlZipSpecifications.whenCloseNestedArchive(this, true);

        // When
        PearlZipSpecifications.whenOpenNestedEntry(this, archiveName.toString(), nestedArchivePath.getFileName().toString());

        // Then
        PearlZipSpecifications.thenExpectNumberOfFilesInCurrentWindow(nestedArchivePath.getFileName(), 2);
        PearlZipSpecifications.thenExpectFileExistsInCurrentWindow(nestedArchivePath.getFileName(), "1");
        PearlZipSpecifications.thenExpectFileExistsInCurrentWindow(nestedArchivePath.getFileName(), file.getFileName().toString());
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
        // Preparation
        final Path emptyDir = Files.createTempDirectory("empty");

        try {
            // Given
            Path archiveName = PearlZipSpecifications.givenCreateNewArchive(this, "tar");

            // When
            simAddFolder(this, emptyDir);
            sleep(100, MILLISECONDS).clickOn("#fileContentsView")
                                    .sleep(100, MILLISECONDS);

            simTraversalArchive(this,
                                archiveName.toString(),
                                "#fileContentsView",
                                (r) -> {},
                                true,
                                emptyDir.getFileName()
                                        .toString());
            simAddFolder(this, emptyDir);
            simUp(this);

            simTraversalArchive(this,
                                archiveName.toString(),
                                "#fileContentsView",
                                (r) -> {},
                                true,
                                emptyDir.getFileName()
                                        .toString(),
                                emptyDir.getFileName()
                                        .toString());
            simAddFolder(this, emptyDir);
            simUp(this);
            simUp(this);

            simTraversalArchive(this,
                                archiveName.toString(),
                                "#fileContentsView",
                                (r) -> {},
                                true,
                                emptyDir.getFileName()
                                        .toString(),
                                emptyDir.getFileName()
                                        .toString(),
                                emptyDir.getFileName()
                                        .toString());

            // Then
            PearlZipSpecifications.thenExpectNumberOfFilesInArchive(archiveName, 3);
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
        // Preparation
        final Path tempDirectory = Files.createTempDirectory("pz");
        Path tempADir = Paths.get(tempDirectory.toAbsolutePath()
                                               .toString(), "tempA");
        Path tempBDir = Paths.get(tempDirectory.toAbsolutePath()
                                               .toString(), "tempB");
        Files.createDirectories(tempADir);
        Files.createDirectories(tempBDir);

        Path emptyArchive = Paths.get("src", "test", "resources", "empty-archive.tar")
                                 .toAbsolutePath();

        try {
            // Given
            Path archiveName = PearlZipSpecifications.givenCreateNewArchive(this, "zip", "outer-archive");

            // When
            simAddFolder(this, tempADir);
            simAddFolder(this, tempBDir);

            // Add archive to each folder
            TableRow row = CommonSpecifications.retryRetrievalForDuration(TestFXConstants.RETRIEVAL_TIMEOUT_MILLIS, () -> simTraversalArchive(this,
                                                                                                                                              archiveName.toAbsolutePath()
                                                      .toString(),
                                                                                                                                              "#fileContentsView",
                                                                                                                                              (r) -> {},
                                                                                                                                              "tempA").get());
            doubleClickOn(row);
            simAddFile(this, emptyArchive);
            simUp(this);

            row = CommonSpecifications.retryRetrievalForDuration(TestFXConstants.RETRIEVAL_TIMEOUT_MILLIS, () -> simTraversalArchive(this,
                                      archiveName.toAbsolutePath()
                                             .toString(),
                                      "#fileContentsView",
                                      (r) -> {},
                                      "tempB").get());
            doubleClickOn(row);
            simAddFile(this, emptyArchive);
            simUp(this);

            // Add tempA to first nested archive
            PearlZipSpecifications.whenOpenNestedEntry(this, archiveName.toString(), "tempA", emptyArchive.getFileName().toString());
            simAddFolder(this, tempADir);
            PearlZipSpecifications.whenCloseNestedArchive(this, true);
            simUp(this);

            // Then
            PearlZipSpecifications.whenOpenNestedEntry(this, archiveName.toString(), "tempA", emptyArchive.getFileName().toString());
            PearlZipSpecifications.thenExpectNumberOfFilesInCurrentWindow(emptyArchive.getFileName(), 1);
            PearlZipSpecifications.thenExpectFileExistsInCurrentWindow(emptyArchive.getFileName(), "tempA");
            PearlZipSpecifications.whenCloseNestedArchive(this, false);
            simUp(this);

            // Add tempB to second nested archive
            PearlZipSpecifications.whenOpenNestedEntry(this, archiveName.toString(), "tempB", emptyArchive.getFileName().toString());
            simAddFolder(this, tempBDir);
            PearlZipSpecifications.whenCloseNestedArchive(this, true);
            simUp(this);

            // Then
            PearlZipSpecifications.whenOpenNestedEntry(this, archiveName.toString(), "tempB", emptyArchive.getFileName().toString());
            PearlZipSpecifications.thenExpectNumberOfFilesInCurrentWindow(emptyArchive.getFileName(), 1);
            PearlZipSpecifications.thenExpectFileExistsInCurrentWindow(emptyArchive.getFileName(), "tempB");
            PearlZipSpecifications.whenCloseNestedArchive(this, false);
            simUp(this);
        } finally {
            // Clean up
            UITestSuite.clearDirectory(tempDirectory);
        }
    }

    @Test
    @DisplayName("Test: Open tar or zip directory in zip archive (processing as folder and not an archive)")
    // GIVEN the default zip archive in PearlZip
    //      AND directories (zip,tar) have been created
    // WHEN folder added to archive (zip)
    //      AND folder added to archive (tar)
    //      AND traverse archive (zip)
    //      AND traverse up to root folder in archive
    //      AND traverse archive (tar)
    //      AND traverse up to root folder in archive
    // THEN ensure only 1 main stage instances are open
    public void testFX_OpenTarZipFolderInZipArchive_Success() throws IOException {
            // Preparation
            final Path tempDirectory = Files.createTempDirectory("pz");
            Path zipDir = Paths.get(tempDirectory.toAbsolutePath()
                                                 .toString(), "zip");
            Path tarDir = Paths.get(tempDirectory.toAbsolutePath()
                                                 .toString(), "tar");
            Files.createDirectories(zipDir);
            Files.createDirectories(tarDir);

        try {
            // Given
            String archiveName = PearlZipSpecifications.givenDefaultArchiveDetails();

            // When
            simAddFolder(this, zipDir);
            simAddFolder(this, tarDir);

            PearlZipSpecifications.whenOpenNestedEntry(this, archiveName, zipDir.getFileName().toString());
            simUp(this);

            PearlZipSpecifications.whenOpenNestedEntry(this, archiveName, tarDir.getFileName().toString());
            simUp(this);

            // Then
            PearlZipSpecifications.thenExpectNumberOfMainInstances(1);
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
        // Preparation
        final Path tempDirectory = Files.createTempDirectory("pz");
        final Path tempFile = Paths.get(tempDirectory.toAbsolutePath()
                                                     .toString(), "temp");
        Files.createFile(tempFile);

        final Path nestTarGZ = Paths.get("src", "test", "resources", "test.tar.gz")
                                    .toAbsolutePath();

        final Path nestTar = Paths.get("src", "test", "resources", "empty-archive.tar")
                                  .toAbsolutePath();

        try {
            // Given
            Path archiveName = PearlZipSpecifications.givenCreateNewArchive(this, "zip", "outer-archive");

            // When
            simAddFile(this, nestTarGZ);
            PearlZipSpecifications.whenOpenNestedEntry(this, archiveName.toString(), "test.tar.gz");
            PearlZipSpecifications.whenOpenNestedEntry(this, "test.tar.gz", "test.tar");
            simAddFile(this, nestTar);
            PearlZipSpecifications.whenOpenNestedEntry(this, "test.tar", "empty-archive.tar");
            simAddFile(this, tempFile);
            PearlZipSpecifications.whenCloseNestedArchive(this, true);
            PearlZipSpecifications.whenCloseNestedArchive(this, true);
            PearlZipSpecifications.whenCloseNestedArchive(this, true);

            // Then
            PearlZipSpecifications.thenExpectFileExistsInCurrentWindow(archiveName, "test.tar.gz");
            PearlZipSpecifications.whenOpenNestedEntry(this, archiveName.toString(), "test.tar.gz");

            PearlZipSpecifications.thenExpectFileExistsInCurrentWindow(Paths.get("test.tar.gz"), "test.tar");
            PearlZipSpecifications.whenOpenNestedEntry(this, "test.tar.gz", "test.tar");

            PearlZipSpecifications.thenExpectFileExistsInCurrentWindow(Paths.get("test.tar"), "empty-archive.tar");
            PearlZipSpecifications.whenOpenNestedEntry(this, "test.tar", "empty-archive.tar");

            PearlZipSpecifications.thenExpectFileExistsInCurrentWindow(Paths.get("empty-archive.tar"), tempFile.getFileName().toString());
        } finally {
            UITestSuite.clearDirectory(tempDirectory);
        }
    }

    @Test
    @DisplayName("Test: Adding nested .tgz can be added successfully and opened as an archive")
    // GIVEN the default zip archive in PearlZip
    // WHEN file added to archive (empty.tgz)
    //     AND nested file (empty.tgz) opened from PearlZip
    // THEN ensure only 2 main stage instances are open
    //     AND A main stage instance name exists like '.*empty.tgz.*'
    public void testFX_AddNestedTgzArchive_Success() {
        // Preparation
        Path tgzArchive = Paths.get("src", "test", "resources", "empty.tgz").toAbsolutePath();

        // Given
        String archiveName = PearlZipSpecifications.givenDefaultArchiveDetails();

        // When
        simAddFile(this, tgzArchive);
        PearlZipSpecifications.whenOpenNestedEntry(this, archiveName, tgzArchive.getFileName().toString());

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(2);
        PearlZipSpecifications.thenMainInstanceExistsWithName(tgzArchive.getFileName().toString());
    }

    @Test
    @DisplayName("Test: Open a single file (non-tarball) compressor archive successfully")
    // GIVEN an archive (test.txt.xz) has been created in PearlZip
    // THEN ensure the number of files in archive = 1
    //     AND ensure files (test.txt) is included in the archive
    public void testFX_OpenSingleFileCompressor_Success() {
        // Preparation
        Path xzArchive = Paths.get("src", "test", "resources", "test.txt.xz").toAbsolutePath();

        // Given
        simOpenArchive(this, xzArchive, true, false);

        // Then
        PearlZipSpecifications.thenExpectNumberOfFilesInArchive(xzArchive, 1);
        PearlZipSpecifications.thenExpectFileExistsInCurrentWindow(xzArchive, "test.txt");
    }

    @Test
    @DisplayName("Test: Add file and directory using off-row context menu")
    // GIVEN a new zip archive has been created in PearlZip
    //      AND directories (foo,bar) have been created
    //      AND files (boom,baa) have been created
    // WHEN folder added to archive (foo) using context menu
    //      AND traverse archive (foo)
    //      AND file added to archive (boom) using context menu
    //      AND traverse up to root folder in archive
    //      AND folder added to archive (bar) using context menu
    //      AND traverse archive (bar)
    //      AND file added to archive (baa) using context menu
    // THEN ensure the number of files in archive = 4
    public void testFX_AddFileAddFolderContextMenu_MatchExpectations() throws IOException {
        // Preparation
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
            // Given
            Path archiveName = PearlZipSpecifications.givenCreateNewArchive(this, "zip");

            // When
            simAddFolder(this, emptyDirFoo, true, archiveName.toString());
            PearlZipSpecifications.whenOpenNestedEntry(this, archiveName.toString(), emptyDirFoo.getFileName().toString());
            simAddFile(this, emptyFileBoom, true, archiveName.toString());
            simUp(this);

            simAddFolder(this, emptyDirBar, true, archiveName.toString());
            PearlZipSpecifications.whenOpenNestedEntry(this, archiveName.toString(), emptyDirBar.getFileName().toString());
            simAddFile(this, emptyFileBaa, true, archiveName.toString());
            simUp(this);

            // Then
            PearlZipSpecifications.thenExpectNumberOfFilesInArchive(archiveName, 4);
        } finally {
            Files.deleteIfExists(emptyDirFoo);
            Files.deleteIfExists(emptyDirBar);
            Files.deleteIfExists(emptyFileBoom);
            Files.deleteIfExists(emptyFileBaa);
        }
    }

}
