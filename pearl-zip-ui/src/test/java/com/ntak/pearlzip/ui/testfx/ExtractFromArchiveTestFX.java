/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.testfx;

import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.util.AbstractPearlZipTestFX;
import com.ntak.pearlzip.ui.util.PearlZipSpecifications;
import com.ntak.testfx.specifications.CommonSpecifications;
import javafx.scene.control.TableRow;
import javafx.scene.input.MouseButton;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.ui.UITestSuite.clearDirectory;
import static com.ntak.pearlzip.ui.util.PearlZipFXUtil.*;
import static com.ntak.testfx.TestFXConstants.SHORT_PAUSE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class ExtractFromArchiveTestFX extends AbstractPearlZipTestFX {
    private Path LOCAL_TEMP;

    /*
     *  Test cases:
     *  + Extract single file
     *  + Extract directory
     *  + Extract all files
     *  + Extract single file - non existent archive
     *  + Extract all files - non existent archive
     */

    @BeforeEach
    public void setUp() {
        LOCAL_TEMP = Path.of(System.getProperty("user.home"), ".pz", "temp");
    }

    @AfterEach
    public void tearDown() throws IOException {
        clearDirectory(Paths.get(LOCAL_TEMP.toAbsolutePath()
                                                        .toString(), "output"));
        Files.deleteIfExists(Paths.get(LOCAL_TEMP.toAbsolutePath().toString(), "1151.txt"));
        Files.deleteIfExists(Paths.get(LOCAL_TEMP.toAbsolutePath().toString(), "first-file"));
    }

    ////////////////////////////////////////////////////////////////////////////////
    ////////// EXTRACT FILE ////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    @Test
    @DisplayName("Test: Extract single file from zip archive")
    // GIVEN zip archive (test.zip) is open in PearlZip
    // WHEN select file (first-file)
    //     AND file (first-file) is extracted from archive to (temp folder) location
    // THEN ensure file exists in the target location
    public void testFX_extractSingleFileZipArchive_Success() throws IOException {
        final Path archivePath = Paths.get("src", "test", "resources", "test.zip")
                                      .toAbsolutePath();
        final Path targetLocation = LOCAL_TEMP.resolve("first-file");

        // Given
        simOpenArchive(this, archivePath.toAbsolutePath(), true, false);

        // When
        PearlZipSpecifications.whenEntrySelectedInCurrentWindow(this, "first-file");
        PearlZipSpecifications.whenFileExtracted(this, targetLocation);

        // Then
        Files.exists(targetLocation);
    }

    @Test
    @DisplayName("Test: Extract single file from tar archive")
    // GIVEN zip archive (test.tar) is open in PearlZip
    // WHEN select file (1151.txt)
    //     AND file (1151.txt) is extracted from archive to (temp folder) location
    // THEN ensure file exists in the target location
    public void testFX_extractSingleFileTarArchive_Success() throws IOException {
        final Path archivePath = Paths.get("src", "test", "resources", "test.tar")
                                      .toAbsolutePath();
        final Path targetLocation = LOCAL_TEMP.resolve("1151.txt");

        // Given
        simOpenArchive(this, archivePath.toAbsolutePath(), true, false);

        // When
        PearlZipSpecifications.whenEntrySelectedInCurrentWindow(this, "1151.txt");
        PearlZipSpecifications.whenFileExtracted(this, targetLocation);

        // Then
        Files.exists(targetLocation);
    }

    @Test
    @DisplayName("Test: Extract single file from jar archive")
    // GIVEN zip archive (test.jar) is open in PearlZip
    // WHEN select file (first-file)
    //     AND file (first-file) is extracted from archive to (temp folder) location
    // THEN ensure file exists in the target location
    public void testFX_extractSingleFileJarArchive_Success() throws IOException {
        final Path archivePath = Paths.get("src", "test", "resources", "test.jar")
                                      .toAbsolutePath();
        final Path targetLocation = LOCAL_TEMP.resolve("first-file");

        // Given
        simOpenArchive(this, archivePath.toAbsolutePath(), true, false);

        // When
        PearlZipSpecifications.whenEntrySelectedInCurrentWindow(this, "first-file");
        PearlZipSpecifications.whenFileExtracted(this, targetLocation);

        // Then
        Files.exists(targetLocation);
    }

    @Test
    @DisplayName("Test: Extract single file from 7zip archive")
    // GIVEN zip archive (test.7z) is open in PearlZip
    // WHEN select file (1151.txt)
    //     AND file (1151.txt) is extracted from archive to (temp folder) location
    // THEN ensure file exists in the target location
    public void testFX_extractSingleFile7zArchive_Success() throws IOException {
        final Path archivePath = Paths.get("src", "test", "resources", "test.7z")
                                      .toAbsolutePath();
        final Path targetLocation = LOCAL_TEMP.resolve("1151.txt");

        // Given
        simOpenArchive(this, archivePath.toAbsolutePath(), true, false);

        // When
        PearlZipSpecifications.whenEntrySelectedInCurrentWindow(this, "1151.txt");
        PearlZipSpecifications.whenFileExtracted(this, targetLocation);

        // Then
        Files.exists(targetLocation);
    }

    @Test
    @DisplayName("Test: Extract single file from cab archive")
    // GIVEN zip archive (test.cab) is open in PearlZip
    // WHEN select file (lala/1151.txt)
    //     AND file (1151.txt) is extracted from archive to (temp folder) location
    // THEN ensure file exists in the target location
    public void testFX_extractSingleFileCabArchive_Success() throws IOException {
        final Path archivePath = Paths.get("src", "test", "resources", "test.cab")
                                      .toAbsolutePath();
        final Path targetLocation = LOCAL_TEMP.resolve("1151.txt");

        // Given
        simOpenArchive(this, archivePath.toAbsolutePath(), true, false);

        // When
        TableRow<FileInfo> row =
                simTraversalArchive(this, archivePath.toString(), "#fileContentsView", (r)->{}, "lala", "1151.txt").get();
        PearlZipSpecifications.whenFileExtracted(this, targetLocation);

        // Then
        Files.exists(targetLocation);
    }

    @Test
    @DisplayName("Test: Extract single file from iso archive")
    // GIVEN zip archive (test.iso) is open in PearlZip
    // WHEN select file (1151.txt)
    //     AND file (1151.txt) is extracted from archive to (temp folder) location
    // THEN ensure file exists in the target location
    public void testFX_extractSingleFileIsoArchive_Success() throws IOException {
        final Path archivePath = Paths.get("src", "test", "resources", "test.iso")
                                      .toAbsolutePath();
        final Path targetLocation = LOCAL_TEMP.resolve("1151.txt");

        // Given
        simOpenArchive(this, archivePath.toAbsolutePath(), true, false);

        // When
        PearlZipSpecifications.whenEntrySelectedInCurrentWindow(this, "1151.txt");
        PearlZipSpecifications.whenFileExtracted(this, targetLocation);

        // Then
        Files.exists(targetLocation);
    }

    @Test
    @DisplayName("Test: Extract single file from rar archive")
    // GIVEN zip archive (test.rar) is open in PearlZip
    // WHEN select file (1151.txt)
    //     AND file (1151.txt) is extracted from archive to (temp folder) location
    // THEN ensure file exists in the target location
    public void testFX_extractSingleFileRarArchive_Success() throws IOException {
        final Path archivePath = Paths.get("src", "test", "resources", "test.rar")
                                      .toAbsolutePath();
        final Path targetLocation = LOCAL_TEMP.resolve("1151.txt");

        // Given
        simOpenArchive(this, archivePath.toAbsolutePath(), true, false);

        // When
        PearlZipSpecifications.whenEntrySelectedInCurrentWindow(this, "1151.txt");
        PearlZipSpecifications.whenFileExtracted(this, targetLocation);

        // Then
        Files.exists(targetLocation);
    }

    @Test
    @DisplayName("Test: Extract single file from a non existent archive. Yield expected alert.")
    // GIVEN a copy of zip archive (test.zip) is open in PearlZip
    // WHEN select file (first-file)
    //     AND archive is deleted
    //     AND attempt to extract file (first-file)
    // THEN a dialog appears with message like "Archive .* does not exist. PearlZip will now close the instance."
    public void testFX_extractSingleFileNonExistentArchive_Alert() throws IOException {
        // Given
        final Path srcArchivePath = Paths.get("src", "test", "resources", "test.zip")
                                      .toAbsolutePath();
        final Path archivePath = LOCAL_TEMP.resolve("test.zip");
        Files.copy(srcArchivePath, archivePath, StandardCopyOption.REPLACE_EXISTING);
        simOpenArchive(this, archivePath.toAbsolutePath(), true, false);

        // When
        PearlZipSpecifications.whenEntrySelectedInCurrentWindow(this, "first-file");
        Files.deleteIfExists(archivePath);
        this.clickOn("#btnExtract")
            .sleep(SHORT_PAUSE, MILLISECONDS)
            .clickOn("#mnuExtractSelectedFile", MouseButton.PRIMARY);

        // Then
        CommonSpecifications.thenExpectDialogWithMatchingMessage(this, "Archive .* does not exist. PearlZip will now close the instance.");
    }

    @Test
    @DisplayName("Test: Extract folder from jar archive")
    // GIVEN a copy of zip archive (test.jar) is open in PearlZip
    // WHEN select file (first-folder)
    //     AND file (first-folder) is extracted from archive to (temp folder) location
    // THEN expect files (first-folder, first-folder/first-nested-file, first-folder/.DS_Store) in target folder (temp folder)
    public void testFX_extractFolderJarArchive_Success() throws IOException {
        final Path archivePath = Paths.get("src", "test", "resources", "test.jar")
                                      .toAbsolutePath();

        // Given
        simOpenArchive(this, archivePath.toAbsolutePath(), true, false);

        // When
        PearlZipSpecifications.whenEntrySelectedInCurrentWindow(this, "first-folder");
        PearlZipSpecifications.whenFileExtracted(this, LOCAL_TEMP);

        // Then
        PearlZipSpecifications.thenExpectFileHierarchyInTargetDirectory(LOCAL_TEMP, Paths.get("first-folder"), Paths.get("first-folder","first-nested-file"), Paths.get("first-folder",".DS_Store"));
    }

    @Test
    @DisplayName("Test: Extract nested folder from zip archive")
    // GIVEN zip archive (dd.zip) is open
    // WHEN select file (a/b/c)
    //     AND file (a/b/c) is extracted from archive to (temp folder) location
    // THEN ensure files (c, c/temp-file) in target folder (temp folder)
    // WHEN select file (a)
    //     AND file (a) is extracted from archive to (temp folder) location
    // THEN ensure files (a, a/b, a/b/c, a/b/c/temp-file, c, c/temp-file) in target folder (temp folder)
    public void testFX_extractNestedFolderZipJArchive_Success() throws IOException {
        // Given
        final Path archivePath = Paths.get("src", "test", "resources", "dd.zip")
                                      .toAbsolutePath();
        simOpenArchive(this, archivePath, true, false);

        // When
        simTraversalArchive(this, archivePath.toString(), "#fileContentsView", (r)->{}, "a", "b", "c").get();
        simExtractFile(this, LOCAL_TEMP);

        // Then
        PearlZipSpecifications.thenExpectFileHierarchyInTargetDirectory(LOCAL_TEMP, Paths.get("c"), Paths.get("c","temp-file"));

        // When
        simUp(this);
        simUp(this);
        PearlZipSpecifications.whenEntrySelectedInCurrentWindow(this, "a");
        simExtractFile(this, LOCAL_TEMP);

        // Then
        PearlZipSpecifications.thenExpectFileHierarchyInTargetDirectory(LOCAL_TEMP, Paths.get("a"), Paths.get("a", "b"), Paths.get("a", "b", "c"), Paths.get("a", "b", "c", "temp-file"), Paths.get("c"), Paths.get("c","temp-file"));

    }

    ////////////////////////////////////////////////////////////////////////////////
    ////////// EXTRACT ALL /////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    @Test
    @DisplayName("Test: Extract whole zip archive")
    // GIVEN zip archive (test.zip) is open in PearlZip
    // WHEN extract all is initiated
    // THEN ensure all files in archive exists in the target location (temp folder)
    public void testFX_extractAllFilesZipArchive_MatchExpectations() {
        final Path archivePath = Paths.get("src", "test", "resources", "test.zip")
                                      .toAbsolutePath();

        // Given
        simOpenArchive(this, archivePath.toAbsolutePath(), true, false);

        // When
        simExtractAll(this, LOCAL_TEMP);

        // Then
        PearlZipSpecifications.thenExpectFileHierarchyInTargetDirectory(LOCAL_TEMP, lookupArchiveInfo(archivePath.toString()).get().getFiles().stream().map(e -> Paths.get(e.getFileName())).collect(Collectors.toList()).toArray(new Path[0]));
    }

    @Test
    @DisplayName("Test: Extract whole tar archive")
    // GIVEN zip archive (test.tar) is open in PearlZip
    // WHEN extract all is initiated
    // THEN ensure all files in archive exists in the target location (temp folder)
    public void testFX_extractAllFilesTarArchive_MatchExpectations() {
        final Path archivePath = Paths.get("src", "test", "resources", "test.tar")
                                      .toAbsolutePath();

        // Given
        simOpenArchive(this, archivePath.toAbsolutePath(), true, false);

        // When
        simExtractAll(this, LOCAL_TEMP);

        // Then
        PearlZipSpecifications.thenExpectFileHierarchyInTargetDirectory(LOCAL_TEMP, lookupArchiveInfo(archivePath.toString()).get().getFiles().stream().map(e -> Paths.get(e.getFileName())).collect(Collectors.toList()).toArray(new Path[0]));
    }

    @Test
    @DisplayName("Test: Extract whole jar archive")
    // GIVEN zip archive (test.jar) is open in PearlZip
    // WHEN extract all is initiated
    // THEN ensure all files in archive exists in the target location (temp folder)
    public void testFX_extractAllFilesJarArchive_MatchExpectations() {
        final Path archivePath = Paths.get("src", "test", "resources", "test.jar")
                                      .toAbsolutePath();

        // Given
        simOpenArchive(this, archivePath.toAbsolutePath(), true, false);

        // When
        simExtractAll(this, LOCAL_TEMP);

        // Then
        PearlZipSpecifications.thenExpectFileHierarchyInTargetDirectory(LOCAL_TEMP, lookupArchiveInfo(archivePath.toString()).get().getFiles().stream().map(e -> Paths.get(e.getFileName())).collect(Collectors.toList()).toArray(new Path[0]));
    }

    @Test
    @DisplayName("Test: Extract whole 7zip archive")
    // GIVEN zip archive (test.7z) is open in PearlZip
    // WHEN extract all is initiated
    // THEN ensure all files in archive exists in the target location (temp folder)
    public void testFX_extractAllFiles7zArchive_MatchExpectations() {
        final Path archivePath = Paths.get("src", "test", "resources", "test.7z")
                                      .toAbsolutePath();

        // Given
        simOpenArchive(this, archivePath.toAbsolutePath(), true, false);

        // When
        simExtractAll(this, LOCAL_TEMP);

        // Then
        PearlZipSpecifications.thenExpectFileHierarchyInTargetDirectory(LOCAL_TEMP, lookupArchiveInfo(archivePath.toString()).get().getFiles().stream().map(e -> Paths.get(e.getFileName())).collect(Collectors.toList()).toArray(new Path[0]));
    }

    @Test
    @DisplayName("Test: Extract whole cab archive")
    // GIVEN zip archive (test.cab) is open in PearlZip
    // WHEN extract all is initiated
    // THEN ensure all files in archive exists in the target location (temp folder)
    public void testFX_extractAllFilesCabArchive_MatchExpectations() {
        final Path archivePath = Paths.get("src", "test", "resources", "test.cab")
                                      .toAbsolutePath();

        // Given
        simOpenArchive(this, archivePath.toAbsolutePath(), true, false);

        // When
        simExtractAll(this, LOCAL_TEMP);

        // Then
        PearlZipSpecifications.thenExpectFileHierarchyInTargetDirectory(LOCAL_TEMP, lookupArchiveInfo(archivePath.toString()).get().getFiles().stream().map(e -> Paths.get(e.getFileName())).collect(Collectors.toList()).toArray(new Path[0]));
    }

    @Test
    @DisplayName("Test: Extract whole iso archive")
    // GIVEN zip archive (test.iso) is open in PearlZip
    // WHEN extract all is initiated
    // THEN ensure all files in archive exists in the target location (temp folder)
    public void testFX_extractAllFilesIsoArchive_MatchExpectations() {
        final Path archivePath = Paths.get("src", "test", "resources", "test.iso")
                                      .toAbsolutePath();

        // Given
        simOpenArchive(this, archivePath.toAbsolutePath(), true, false);

        // When
        simExtractAll(this, LOCAL_TEMP);

        // Then
        PearlZipSpecifications.thenExpectFileHierarchyInTargetDirectory(LOCAL_TEMP, lookupArchiveInfo(archivePath.toString()).get().getFiles().stream().map(e -> Paths.get(e.getFileName())).collect(Collectors.toList()).toArray(new Path[0]));
    }

    @Test
    @DisplayName("Test: Extract whole rar archive")
    // GIVEN zip archive (test.rar) is open in PearlZip
    // WHEN extract all is initiated
    // THEN ensure all files in archive exists in the target location (temp folder)
    public void testFX_extractAllFilesRarArchive_MatchExpectations() {
        final Path archivePath = Paths.get("src", "test", "resources", "test.rar")
                                      .toAbsolutePath();

        // Given
        simOpenArchive(this, archivePath.toAbsolutePath(), true, false);

        // When
        simExtractAll(this, LOCAL_TEMP);

        // Then
        PearlZipSpecifications.thenExpectFileHierarchyInTargetDirectory(LOCAL_TEMP, lookupArchiveInfo(archivePath.toString()).get().getFiles().stream().map(e -> Paths.get(e.getFileName())).collect(Collectors.toList()).toArray(new Path[0]));
    }

    @Test
    @DisplayName("Test: Extract all files from a non existent archive. Yield expected alert.")
    // GIVEN a copy of zip archive (test.zip) is open in PearlZip
    // WHEN archive is deleted
    //     AND extract all is initiated
    // THEN a dialog appears with message like "Archive .* does not exist. PearlZip will now close the instance."
    public void testFX_extractAllFilesNonExistentArchive_Alert() throws IOException {
        // Given
        final Path srcArchivePath = Paths.get("src", "test", "resources", "test.zip")
                                         .toAbsolutePath();
        final Path archivePath = LOCAL_TEMP.resolve("test.zip");
        Files.copy(srcArchivePath, archivePath, StandardCopyOption.REPLACE_EXISTING);
        simOpenArchive(this, archivePath.toAbsolutePath(), true, false);

        // When
        Files.deleteIfExists(archivePath);
        this.clickOn("#btnExtract")
            .sleep(SHORT_PAUSE, MILLISECONDS)
            .clickOn("#mnuExtractAll", MouseButton.PRIMARY);

        // Then
        CommonSpecifications.thenExpectDialogWithMatchingMessage(this, "Archive .* does not exist. PearlZip will now close the instance.");
    }
}
