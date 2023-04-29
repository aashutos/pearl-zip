/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.testfx;

import com.ntak.pearlzip.ui.UITestSuite;
import com.ntak.pearlzip.ui.util.AbstractPearlZipTestFX;
import com.ntak.pearlzip.ui.util.PearlZipFXUtil;
import com.ntak.pearlzip.ui.util.PearlZipSpecifications;
import com.ntak.testfx.specifications.CommonSpecifications;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import static com.ntak.pearlzip.ui.util.PearlZipFXUtil.*;

public class CompressorArchiveTestFX extends AbstractPearlZipTestFX {

    private static Path tempDirRoot;
    private Path file;
    private Path folder;
    private Path nestedFile;
    private Path outputDir;

    /*
     *  Test cases:
     *  + Nest tarball into the compressor archive and verify contents is as expected
     *  + Extract archive contents from nested tarball generates expected files/folders
     *  + Create single file xz compressor archive
     *  + Create single file GZip compressor archive
     *  + Create single file BZip compressor archive
     *  + Create single file BZip compressor archive from main window
     *  + Open compressor archive and expand nested tarball and test window menu state is as expected
     *    before/after reintegration
     *  + Close parent archive when nested not closed will yield warning dialog
     */

    @BeforeEach
    public void setUp() throws IOException {
        tempDirRoot = Files.createTempDirectory("pz");
        outputDir = Paths.get(tempDirRoot.toAbsolutePath().toString(), "output");
        file = Paths.get(tempDirRoot.toString(), "temp-file");
        folder = Paths.get(tempDirRoot.toString(), "temp-folder");
        nestedFile = Paths.get(tempDirRoot.toString(), "temp-folder", "sub-temp-file");

        Files.createFile(file);
        Files.createDirectories(folder);
        Files.createFile(nestedFile);
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
        UITestSuite.clearDirectory(tempDirRoot);
    }

    @Test
    @DisplayName("Test: Nest tarball into the Gzip compressor archive and verify contents is as expected")
    // GIVEN a new tar.gz (nest-test.tar.gz) archive has been created in PearlZip
    // WHEN nested file (nest-test.tar) opened from PearlZip
    // THEN ensure the number of files in archive = 0
    // WHEN file added to archive (temp-file)
    //     AND folder added to archive (temp-folder)
    //     AND close nested archive and save = true
    //     AND nested file (nest-test.tar) opened from PearlZip
    // THEN ensure the number of files in archive = 3
    //     AND ensure files (temp-file, temp-folder, temp-folder/nested-file) is included in the archive at depth (0,0,1) respectively
    public void testFX_CreateGzipArchiveAndUpdateNestedTarball_Success() {
        // Given
        Path archiveName = PearlZipSpecifications.givenCreateNewArchive(this, "tar.gz","test");

        // When
        final String nestedArchiveName = archiveName.getFileName()
                                          .toString()
                                          .replace(".gz", "");
        PearlZipSpecifications.whenOpenNestedEntry(this, archiveName.toString(), nestedArchiveName);

        // Then
        String nestedArchivePath = lookupArchiveInfo(nestedArchiveName).get().getArchivePath();
        PearlZipSpecifications.thenExpectNumberOfFilesInArchive(Paths.get(nestedArchivePath), 0);

        // When
        simAddFile(this, file, true, archiveName.toString());
        simAddFolder(this, folder, true, archiveName.toString());
        PearlZipSpecifications.whenCloseNestedArchive(this, true);
        PearlZipSpecifications.whenOpenNestedEntry(this, archiveName.toString(), nestedArchiveName);

        // Then
        Path nestedArchive = Paths.get(lookupArchiveInfo(nestedArchiveName).get().getArchivePath());
        PearlZipSpecifications.thenExpectNumberOfFilesInArchive(nestedArchive, 3);
        PearlZipSpecifications.thenExpectFileExistsInArchive(nestedArchive, 0, "temp-file");
        PearlZipSpecifications.thenExpectFileExistsInArchive(nestedArchive, 0, "temp-folder");
        PearlZipSpecifications.thenExpectFileExistsInArchive(nestedArchive, 1, "temp-folder/sub-temp-file");
    }

    @Test
    @DisplayName("Test: Nest tarball into the Gzip compressor archive (shortname) and verify contents is as expected")
    // GIVEN a copy of tar.gz archive (test.tgz) is open in PearlZip
    // WHEN nested file (test.tar) opened from PearlZip
    //     AND file added to archive (temp-file)
    //     AND close nested archive and save = true
    //     AND nested file (test.tar) opened from PearlZip
    // THEN ensure the number of files in archive = 5
    //     AND ensure files (temp-file) is included in the archive at depth (0) respectively
    public void testFX_OpenGzipArchiveAndUpdateNestedTarballShortName_Success() throws IOException {
        // 1. Prepare compressor archive
        Path srcArchive = Paths.get("src", "test", "resources", "empty.tgz");
        Path archive = Paths.get(tempDirRoot.toAbsolutePath()
                                            .toString(), "temp.tgz");
        Files.copy(srcArchive, archive, StandardCopyOption.REPLACE_EXISTING);

        // Load archive
        simOpenArchive(this, archive, true, false);

        // When
        final String nestedArchiveName = archive.getFileName()
                                          .toString()
                                          .replace(".tgz", ".tar");
        PearlZipSpecifications.whenOpenNestedEntry(this, archive.toString(), nestedArchiveName);

        // Then
        String nestedArchivePath = lookupArchiveInfo(nestedArchiveName).get().getArchivePath();
        PearlZipSpecifications.thenExpectNumberOfFilesInArchive(Paths.get(nestedArchivePath), 0);

        // When
        simAddFile(this, file, true, archive.toString());
        simAddFolder(this, folder, true, archive.toString());
        PearlZipSpecifications.whenCloseNestedArchive(this, true);
        PearlZipSpecifications.whenOpenNestedEntry(this, archive.toString(), nestedArchiveName);

        // Then
        Path nestedArchive = Paths.get(lookupArchiveInfo(nestedArchiveName).get().getArchivePath());
        PearlZipSpecifications.thenExpectNumberOfFilesInArchive(nestedArchive, 3);
        PearlZipSpecifications.thenExpectFileExistsInArchive(nestedArchive, 0, "temp-file");
        PearlZipSpecifications.thenExpectFileExistsInArchive(nestedArchive, 0, "temp-folder");
        PearlZipSpecifications.thenExpectFileExistsInArchive(nestedArchive, 1, "temp-folder/sub-temp-file");
    }

    @Test
    @DisplayName("Test: Nest tarball into the BZip compressor archive and verify contents is as expected")
    // GIVEN a new tar.bz2 (nest-test.tar.bz2) archive has been created in PearlZip
    // WHEN nested file (nest-test.tar) opened from PearlZip
    // THEN ensure the number of files in archive = 0
    // WHEN file added to archive (temp-file)
    //     AND folder added to archive (temp-folder)
    //     AND close nested archive and save = true
    //     AND nested file (nest-test.tar) opened from PearlZip
    // THEN ensure the number of files in archive = 3
    //     AND ensure files (temp-file, temp-folder, temp-folder/nested-file) is included in the archive at depth (0,0,1) respectively
    public void testFX_CreateBzipArchiveAndUpdateNestedTarball_Success() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "tar.bz2","nest-test");

        // When
        final String nestedArchiveName = archive.getFileName()
                                                .toString()
                                                .replace(".bz2", "");
        PearlZipSpecifications.whenOpenNestedEntry(this, archive.toString(), nestedArchiveName);

        // Then
        String nestedArchivePath = lookupArchiveInfo(nestedArchiveName).get().getArchivePath();
        PearlZipSpecifications.thenExpectNumberOfFilesInArchive(Paths.get(nestedArchivePath), 0);

        // When
        simAddFile(this, file, true, archive.toString());
        simAddFolder(this, folder, true, archive.toString());
        PearlZipSpecifications.whenCloseNestedArchive(this, true);
        PearlZipSpecifications.whenOpenNestedEntry(this, archive.toString(), nestedArchiveName);

        // Then
        Path nestedArchive = Paths.get(lookupArchiveInfo(nestedArchiveName).get().getArchivePath());
        PearlZipSpecifications.thenExpectNumberOfFilesInArchive(nestedArchive, 3);
        PearlZipSpecifications.thenExpectFileExistsInArchive(nestedArchive, 0, "temp-file");
        PearlZipSpecifications.thenExpectFileExistsInArchive(nestedArchive, 0, "temp-folder");
        PearlZipSpecifications.thenExpectFileExistsInArchive(nestedArchive, 1, "temp-folder/sub-temp-file");
    }

    @Test
    @DisplayName("Test: Nest tarball into the xz compressor archive and verify contents is as expected")
    // GIVEN a new tar.xz (nest-test.tar.xz) archive has been created in PearlZip
    // WHEN nested file (nest-test.tar) opened from PearlZip
    // THEN ensure the number of files in archive = 0
    // WHEN file added to archive (temp-file)
    //     AND folder added to archive (temp-folder)
    //     AND close nested archive and save = true
    //     AND nested file (nest-test.tar) opened from PearlZip
    // THEN ensure the number of files in archive = 3
    //     AND ensure files (temp-file, temp-folder, temp-folder/nested-file) is included in the archive at depth (0,0,1) respectively
    public void testFX_CreateXZArchiveAndUpdateNestedTarball_Success() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "tar.xz","nest-test");

        // When
        final String nestedArchiveName = archive.getFileName()
                                                .toString()
                                                .replace(".xz", "");
        PearlZipSpecifications.whenOpenNestedEntry(this, archive.toString(), nestedArchiveName);

        // Then
        String nestedArchivePath = lookupArchiveInfo(nestedArchiveName).get().getArchivePath();
        PearlZipSpecifications.thenExpectNumberOfFilesInArchive(Paths.get(nestedArchivePath), 0);

        // When
        simAddFile(this, file, true, archive.toString());
        simAddFolder(this, folder, true, archive.toString());
        PearlZipSpecifications.whenCloseNestedArchive(this, true);
        PearlZipSpecifications.whenOpenNestedEntry(this, archive.toString(), nestedArchiveName);

        // Then
        Path nestedArchive = Paths.get(lookupArchiveInfo(nestedArchiveName).get().getArchivePath());
        PearlZipSpecifications.thenExpectNumberOfFilesInArchive(nestedArchive, 3);
        PearlZipSpecifications.thenExpectFileExistsInArchive(nestedArchive, 0, "temp-file");
        PearlZipSpecifications.thenExpectFileExistsInArchive(nestedArchive, 0, "temp-folder");
        PearlZipSpecifications.thenExpectFileExistsInArchive(nestedArchive, 1, "temp-folder/sub-temp-file");
    }

    @Test
    @DisplayName("Test: Extract GZip archive contents from nested tarball generates expected files/folders")
    // GIVEN a new tar.gz (nest-test.tar.gz) archive has been created in PearlZip
    // WHEN nested file (nest-test.tar) opened from PearlZip
    // THEN ensure the number of files in archive = 0
    // WHEN file added to archive (temp-file)
    //     AND folder added to archive (temp-folder)
    //     AND close nested archive and save = true
    //     AND nested file (nest-test.tar) opened from PearlZip
    // THEN ensure the number of files in archive = 3
    // WHEN extract all files in target directory
    // THEN ensure files (temp-file, temp-folder, temp-folder/nested-file) is included in the archive at depth (0,0,1) respectively
    // THEN ensure files (temp-file, temp-folder, temp-folder/nested-file) is included in the output directory at depth (0,0,1) respectively
    public void testFX_CreatePopulatedGzipArchiveExtractAll_MatchExpectations() throws IOException {
        // Preparation
        Path tempDir = Files.createTempDirectory("pz");

        // Given
        Path archiveName = PearlZipSpecifications.givenCreateNewArchive(this, "tar.gz","test");

        // When
        final String nestedArchiveName = archiveName.getFileName()
                                                    .toString()
                                                    .replace(".gz", "");
        PearlZipSpecifications.whenOpenNestedEntry(this, archiveName.toString(), nestedArchiveName);

        // Then
        String nestedArchivePath = lookupArchiveInfo(nestedArchiveName).get().getArchivePath();
        PearlZipSpecifications.thenExpectNumberOfFilesInArchive(Paths.get(nestedArchivePath), 0);

        // When
        simAddFile(this, file, true, archiveName.toString());
        simAddFolder(this, folder, true, archiveName.toString());
        PearlZipSpecifications.whenCloseNestedArchive(this, true);
        PearlZipSpecifications.whenOpenNestedEntry(this, archiveName.toString(), nestedArchiveName);
        simExtractAll(this, tempDir);

        // Then
        Path nestedArchive = Paths.get(lookupArchiveInfo(nestedArchiveName).get().getArchivePath());
        PearlZipSpecifications.thenExpectNumberOfFilesInArchive(nestedArchive, 3);
        PearlZipSpecifications.thenExpectFileExistsInArchive(nestedArchive, 0, "temp-file");
        PearlZipSpecifications.thenExpectFileExistsInArchive(nestedArchive, 0, "temp-folder");
        PearlZipSpecifications.thenExpectFileExistsInArchive(nestedArchive, 1, "temp-folder/sub-temp-file");

        Map<Integer,Map<String,String[]>> expectations = PearlZipFXUtil.genArchiveContentsExpectationsAuto(outputDir);
        checkArchiveFileHierarchy(this, expectations, nestedArchiveName);
        PearlZipSpecifications.thenExpectFileHierarchyInTargetDirectory(tempDir, Paths.get("temp-file"), Paths.get("temp-folder"), Paths.get("temp-folder", "sub-temp-file"));
    }

    @Test
    @DisplayName("Test: Extract Bzip archive contents from nested tarball generates expected files/folders")
    // GIVEN a new tar.bz2 (nest-test.tar.bz2) archive has been created in PearlZip
    // WHEN nested file (nest-test.tar) opened from PearlZip
    // THEN ensure the number of files in archive = 0
    // WHEN file added to archive (temp-file)
    //     AND folder added to archive (temp-folder)
    //     AND close nested archive and save = true
    //     AND nested file (nest-test.tar) opened from PearlZip
    // THEN ensure the number of files in archive = 3
    // WHEN extract all files in archive
    // THEN ensure files (temp-file, temp-folder, temp-folder/nested-file) is included in the archive at depth (0,0,1) respectively
    // THEN ensure files (temp-file, temp-folder, temp-folder/nested-file) is included in the output directory at depth (0,0,1) respectively
    public void testFX_CreatePopulatedBzipArchiveExtractAll_MatchExpectations() throws IOException {
        // Preparation
        Path tempDir = Files.createTempDirectory("pz");

        // Given
        Path archiveName = PearlZipSpecifications.givenCreateNewArchive(this, "tar.bz2","test");

        // When
        final String nestedArchiveName = archiveName.getFileName()
                                                    .toString()
                                                    .replace(".bz2", "");
        PearlZipSpecifications.whenOpenNestedEntry(this, archiveName.toString(), nestedArchiveName);

        // Then
        String nestedArchivePath = lookupArchiveInfo(nestedArchiveName).get().getArchivePath();
        PearlZipSpecifications.thenExpectNumberOfFilesInArchive(Paths.get(nestedArchivePath), 0);

        // When
        simAddFile(this, file, true, archiveName.toString());
        simAddFolder(this, folder, true, archiveName.toString());
        PearlZipSpecifications.whenCloseNestedArchive(this, true);
        PearlZipSpecifications.whenOpenNestedEntry(this, archiveName.toString(), nestedArchiveName);
        simExtractAll(this, tempDir);

        // Then
        Path nestedArchive = Paths.get(lookupArchiveInfo(nestedArchiveName).get().getArchivePath());
        PearlZipSpecifications.thenExpectNumberOfFilesInArchive(nestedArchive, 3);
        PearlZipSpecifications.thenExpectFileExistsInArchive(nestedArchive, 0, "temp-file");
        PearlZipSpecifications.thenExpectFileExistsInArchive(nestedArchive, 0, "temp-folder");
        PearlZipSpecifications.thenExpectFileExistsInArchive(nestedArchive, 1, "temp-folder/sub-temp-file");

        Map<Integer,Map<String,String[]>> expectations = PearlZipFXUtil.genArchiveContentsExpectationsAuto(outputDir);
        checkArchiveFileHierarchy(this, expectations, nestedArchiveName);
        PearlZipSpecifications.thenExpectFileHierarchyInTargetDirectory(tempDir, Paths.get("temp-file"), Paths.get("temp-folder"), Paths.get("temp-folder", "sub-temp-file"));
    }

    @Test
    @DisplayName("Test: Extract xz archive contents from nested tarball generates expected files/folders")
    // GIVEN a new tar.xz (nest-test.tar.xz) archive has been created in PearlZip
    // WHEN nested file (nest-test.tar) opened from PearlZip
    // THEN ensure the number of files in archive = 0
    // WHEN file added to archive (temp-file)
    //     AND folder added to archive (temp-folder)
    //     AND close nested archive and save = true
    //     AND nested file (nest-test.tar) opened from PearlZip
    // THEN ensure the number of files in archive = 3
    // WHEN extract all files in archive
    // THEN ensure files (temp-file, temp-folder, temp-folder/nested-file) is included in the archive at depth (0,0,1) respectively
    public void testFX_CreatePopulatedXZArchiveExtractAll_MatchExpectations() throws IOException {// Preparation
        Path tempDir = Files.createTempDirectory("pz");

        // Given
        Path archiveName = PearlZipSpecifications.givenCreateNewArchive(this, "tar.xz","test");

        // When
        final String nestedArchiveName = archiveName.getFileName()
                                                    .toString()
                                                    .replace(".xz", "");
        PearlZipSpecifications.whenOpenNestedEntry(this, archiveName.toString(), nestedArchiveName);

        // Then
        String nestedArchivePath = lookupArchiveInfo(nestedArchiveName).get().getArchivePath();
        PearlZipSpecifications.thenExpectNumberOfFilesInArchive(Paths.get(nestedArchivePath), 0);

        // When
        simAddFile(this, file, true, archiveName.toString());
        simAddFolder(this, folder, true, archiveName.toString());
        PearlZipSpecifications.whenCloseNestedArchive(this, true);
        PearlZipSpecifications.whenOpenNestedEntry(this, archiveName.toString(), nestedArchiveName);
        simExtractAll(this, tempDir);

        // Then
        Path nestedArchive = Paths.get(lookupArchiveInfo(nestedArchiveName).get().getArchivePath());
        PearlZipSpecifications.thenExpectNumberOfFilesInArchive(nestedArchive, 3);
        PearlZipSpecifications.thenExpectFileExistsInArchive(nestedArchive, 0, "temp-file");
        PearlZipSpecifications.thenExpectFileExistsInArchive(nestedArchive, 0, "temp-folder");
        PearlZipSpecifications.thenExpectFileExistsInArchive(nestedArchive, 1, "temp-folder/sub-temp-file");

        Map<Integer,Map<String,String[]>> expectations = PearlZipFXUtil.genArchiveContentsExpectationsAuto(outputDir);
        checkArchiveFileHierarchy(this, expectations, nestedArchiveName);
        PearlZipSpecifications.thenExpectFileHierarchyInTargetDirectory(tempDir, Paths.get("temp-file"), Paths.get("temp-folder"), Paths.get("temp-folder", "sub-temp-file"));
    }

    @Test
    @DisplayName("Test: Create single file xz compressor archive")
    // GIVEN files (arbitrary-file.txt) have been created
    //     AND new single file compressor archive (arbitrary-file.txt.xz) is created from the main menu in PearlZip
    // THEN check archive arbitrary-file.txt.xz has been created
    //     AND ensure files (arbitrary-file.txt.xz) is included in the archive at depth (0) respectively
    public void testFX_CreateSingleFileXZCompressorArchive_Success() throws IOException {
        // Preparation
        Path tempDir = Files.createTempDirectory("pz");
        Path nestedFile = tempDir.resolve("arbitrary-file.txt");
        Files.createFile(nestedFile);

        // Given
        Path archiveName = PearlZipSpecifications.givenNewSingleFileCompressorArchive(this, "xz", nestedFile, tempDirRoot, true);

        // Then
        PearlZipSpecifications.thenExpectNumberOfFilesInArchive(archiveName, 1);
        PearlZipSpecifications.thenExpectFileExistsInArchive(archiveName, 0, "arbitrary-file.txt");
    }

    @Test
    @DisplayName("Test: Create single file GZip compressor archive")
    // GIVEN New single file compressor archive is selected from the main menu in PearlZip
    //     AND files (arbitrary-file.txt) have been created
    // WHEN combo box #comboArchiveFormat has value set as gz
    //     AND #btnSelectFile clicked on and file selected (arbitrary-file.txt)
    // THEN check archive arbitrary-file.txt.gz has been created
    //     AND ensure files (arbitrary-file.txt.gz) is included in the archive at depth (0) respectively
    public void testFX_CreateSingleFileGZipCompressorArchive_Success() throws IOException {
        // Preparation
        Path tempDir = Files.createTempDirectory("pz");
        Path nestedFile = tempDir.resolve("arbitrary-file.txt");
        Files.createFile(nestedFile);

        // Given
        Path archiveName = PearlZipSpecifications.givenNewSingleFileCompressorArchive(this, "gz", nestedFile, tempDirRoot, true);

        // Then
        PearlZipSpecifications.thenExpectNumberOfFilesInArchive(archiveName, 1);
        PearlZipSpecifications.thenExpectFileExistsInArchive(archiveName, 0, "arbitrary-file.txt");
    }

    @Test
    @DisplayName("Test: Create single file BZip compressor archive")
    // GIVEN New single file compressor archive is selected from the main menu in PearlZip
    //     AND files (arbitrary-file.txt) have been created
    // WHEN combo box #comboArchiveFormat has value set as bz2
    //     AND #btnSelectFile clicked on and file selected (arbitrary-file.txt)
    // THEN check archive arbitrary-file.txt.bz2 has been created
    //     AND ensure files (arbitrary-file.txt.bz2) is included in the archive at depth (0) respectively
    public void testFX_CreateSingleFileBZipCompressorArchive_Success() throws IOException {
        // Preparation
        Path tempDir = Files.createTempDirectory("pz");
        Path nestedFile = tempDir.resolve("arbitrary-file.txt");
        Files.createFile(nestedFile);

        // Given
        Path archiveName = PearlZipSpecifications.givenNewSingleFileCompressorArchive(this, "bz2", nestedFile, tempDirRoot, false);

        // Then
        PearlZipSpecifications.thenExpectNumberOfFilesInArchive(archiveName, 1);
        PearlZipSpecifications.thenExpectFileExistsInArchive(archiveName, 0, "arbitrary-file.txt");
    }

    @Test
    @DisplayName("Test: Create single file BZip compressor archive via main window")
    // GIVEN New single file compressor archive is selected from the main window in PearlZip
    //     AND files (arbitrary-file.txt) have been created
    // WHEN combo box #comboArchiveFormat has value set as bz2
    //     AND #btnSelectFile clicked on and file selected (arbitrary-file.txt)
    // THEN check archive arbitrary-file.txt.bz2 has been created
    //     AND ensure files (arbitrary-file.txt.bz2) is included in the archive at depth (0) respectively
    public void testFX_CreateSingleFileBZipCompressorArchiveMainWindow_Success() throws IOException {
        // Preparation
        Path tempDir = Files.createTempDirectory("pz");
        Path nestedFile = tempDir.resolve("arbitrary-file.txt");
        Files.createFile(nestedFile);

        // Given
        Path archiveName = PearlZipSpecifications.givenNewSingleFileCompressorArchive(this, "bz2", nestedFile, tempDirRoot, true);

        // Then
        PearlZipSpecifications.thenExpectNumberOfFilesInArchive(archiveName, 1);
        PearlZipSpecifications.thenExpectFileExistsInArchive(archiveName, 0, "arbitrary-file.txt");
    }

    @Test
    @DisplayName("Test: Close parent archive when nested not closed will yield warning dialog")
    // GIVEN a tar.gz archive (temp.tar.gz) is open in PearlZip
    // WHEN nested file (temp.tar) opened from PearlZip
    //     AND bring window (temp.tar.gz) to the front
    //     AND close archive (temp.tar.gz)
    // THEN a dialog appears with message like "A nested archive is open, so parent archive will not be closed until it has been reintegrated or disposed of."
    public void testFX_OpenCompressorArchive_CloseParentArchive_Warn() throws IOException {
        // 1. Prepare compressor archive
        Path srcArchive = Paths.get("src", "test", "resources", "empty.tgz");
        Path archive = Paths.get(tempDirRoot.toAbsolutePath()
                                            .toString(), "temp.tar.gz");
        Files.copy(srcArchive, archive, StandardCopyOption.REPLACE_EXISTING);

        // Given
        simOpenArchive(this, archive, true, false);

        // When
        PearlZipSpecifications.whenOpenNestedEntry(this, archive.toString(), "temp.tar");
        Assertions.assertTrue(simWindowSelect(this, archive), "Successfully changed window to parent archive");
        PearlZipSpecifications.whenCloseArchive(this);

        // Then
        CommonSpecifications.thenExpectDialogWithMatchingMessage(this, "A nested archive is open, so parent archive will not be closed until it has been reintegrated or disposed of.");
    }
}
