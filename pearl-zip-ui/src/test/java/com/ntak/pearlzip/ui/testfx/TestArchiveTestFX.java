/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.testfx;

import com.ntak.pearlzip.archive.acc.pub.CommonsCompressArchiveReadService;
import com.ntak.pearlzip.archive.acc.pub.CommonsCompressArchiveWriteService;
import com.ntak.pearlzip.archive.szjb.pub.SevenZipArchiveService;
import com.ntak.pearlzip.ui.util.AbstractPearlZipTestFX;
import com.ntak.pearlzip.ui.util.ArchiveUtil;
import com.ntak.pearlzip.ui.util.PearlZipFXUtil;
import com.ntak.pearlzip.ui.util.PearlZipSpecifications;
import com.ntak.testfx.specifications.CommonSpecifications;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.ntak.pearlzip.ui.util.PearlZipFXUtil.*;

public class TestArchiveTestFX extends AbstractPearlZipTestFX {

    /*
     *  Test cases:
     *  + Successful integrity check of archive
     *  + Failure on integrity check of archive
     *  + Successful integrity check of empty tar (Bug: PZ-97)
     */

    @Override
    public void start(Stage stage) throws IOException, TimeoutException {
        Path STORE_TEMP = localWorkspace.resolve("temp");

        // Setup workspace prior to launch
        if (Files.exists(backupLocalWorkspace)) {
            ArchiveUtil.deleteDirectory(backupLocalWorkspace, (p)->false);
        }
        if (Files.exists(localWorkspace)) {
            Files.move(localWorkspace, backupLocalWorkspace);
        }

        // Initialise PearlZip Application
        final var file = Files.createTempDirectory("pz").resolve("temp.tar");
        Files.createDirectories(file.getParent());
        Files.createFile(file);
        PearlZipFXUtil.initialise(stage, List.of(new CommonsCompressArchiveWriteService()), List.of(new SevenZipArchiveService(), new CommonsCompressArchiveReadService()), file);

        // Save Properties...
        if (!Files.exists(applicationProps)) {
            Files.createFile(applicationProps);
        }
        System.getProperties().store(Files.newBufferedWriter(applicationProps), "PearlZip Automated Test");
    }

    @AfterEach
    public void tearDown() throws IOException {

    }


    ////////////////////////////////////////////////////////////////////////////////
    ////////// TEST ARCHIVE - SUCCESS //////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    @Test
    @DisplayName("Test: test valid zip archive returns success alert")
    // GIVEN zip archive (test.zip) is open in PearlZip
    // WHEN test archive is initiated
    // THEN a dialog appears with message like "Parsed archive .* successfully"
    public void testFX_testValidZipArchive_Alert() {
        // Given
        final Path archive = Paths.get("src",  "test", "resources", "test.zip").toAbsolutePath();
        simOpenArchive(this, archive, true, false);

        // When
        simTestArchive(this);

        // Then
        CommonSpecifications.thenExpectDialogWithMatchingMessage(this, "Parsed archive .* successfully");
    }

    @Test
    @DisplayName("Test: test valid jar archive returns success alert")
    // GIVEN zip archive (test.jar) is open in PearlZip
    // WHEN test archive is initiated
    // THEN a dialog appears with message like "Parsed archive .* successfully"
    public void testFX_testValidJarArchive_Alert() {
        // Given
        final Path archive = Paths.get("src",  "test", "resources", "test.jar").toAbsolutePath();
        simOpenArchive(this, archive, true, false);

        // When
        simTestArchive(this);

        // Then
        CommonSpecifications.thenExpectDialogWithMatchingMessage(this, "Parsed archive .* successfully");
    }

    @Test
    @DisplayName("Test: test valid tar archive returns success alert")
    // GIVEN zip archive (test.tar) is open in PearlZip
    // WHEN test archive is initiated
    // THEN a dialog appears with message like "Parsed archive .* successfully"
    public void testFX_testValidTarArchive_Alert() {
        // Given
        final Path archive = Paths.get("src",  "test", "resources", "test.tar").toAbsolutePath();
        simOpenArchive(this, archive, true, false);

        // When
        simTestArchive(this);

        // Then
        CommonSpecifications.thenExpectDialogWithMatchingMessage(this, "Parsed archive .* successfully");
    }

    @Test
    @DisplayName("Test: test valid empty tar archive returns success alert")
    // GIVEN zip archive (empty-archive.tar) is open in PearlZip
    // WHEN test archive is initiated
    // THEN a dialog appears with message like "Parsed archive .* successfully"
    public void testFX_testValidEmptyTarArchive_Alert() {
        // Given
        final Path archive = Paths.get("src",  "test", "resources", "empty-archive.tar").toAbsolutePath();
        simOpenArchive(this, archive, true, false);

        // When
        simTestArchive(this);

        // Then
        CommonSpecifications.thenExpectDialogWithMatchingMessage(this, "Parsed archive .* successfully");
    }

    @Test
    @DisplayName("Test: test valid cab archive returns success alert")
    // GIVEN zip archive (test.cab) is open in PearlZip
    // WHEN test archive is initiated
    // THEN a dialog appears with message like "Parsed archive .* successfully"
    public void testFX_testValidCabArchive_Alert() {
        // Given
        final Path archive = Paths.get("src",  "test", "resources", "test.cab").toAbsolutePath();
        simOpenArchive(this, archive, true, false);

        // When
        simTestArchive(this);

        // Then
        CommonSpecifications.thenExpectDialogWithMatchingMessage(this, "Parsed archive .* successfully");
    }

    @Test
    @DisplayName("Test: test valid iso archive returns success alert")
    // GIVEN zip archive (test.iso) is open in PearlZip
    // WHEN test archive is initiated
    // THEN a dialog appears with message like "Parsed archive .* successfully"
    public void testFX_testValidIsoArchive_Alert() {
        // Given
        final Path archive = Paths.get("src",  "test", "resources", "test.iso").toAbsolutePath();
        simOpenArchive(this, archive, true, false);

        // When
        simTestArchive(this);

        // Then
        CommonSpecifications.thenExpectDialogWithMatchingMessage(this, "Parsed archive .* successfully");
    }

    @Test
    @DisplayName("Test: test valid rar archive returns success alert")
    // GIVEN zip archive (test.rar) is open in PearlZip
    // WHEN test archive is initiated
    // THEN a dialog appears with message like "Parsed archive .* successfully"
    public void testFX_testValidRarArchive_Alert() {
        // Given
        final Path archive = Paths.get("src",  "test", "resources", "test.rar").toAbsolutePath();
        simOpenArchive(this, archive, true, false);

        // When
        simTestArchive(this);

        // Then
        CommonSpecifications.thenExpectDialogWithMatchingMessage(this, "Parsed archive .* successfully");
    }

    @Test
    @DisplayName("Test: test valid 7zip archive returns success alert")
    // GIVEN zip archive (test.7z) is open in PearlZip
    // WHEN test archive is initiated
    // THEN a dialog appears with message like "Parsed archive .* successfully"
    public void testFX_testValid7zipArchive_Alert() {
        // Given
        final Path archive = Paths.get("src",  "test", "resources", "test.7z").toAbsolutePath();
        simOpenArchive(this, archive, true, false);

        // When
        simTestArchive(this);

        // Then
        CommonSpecifications.thenExpectDialogWithMatchingMessage(this, "Parsed archive .* successfully");
    }

    @Test
    @DisplayName("Test: test valid Bzip archive returns success alert")
    // GIVEN zip archive (test.tar.bz2) is open in PearlZip
    // WHEN test archive is initiated
    // THEN a dialog appears with message like "Parsed archive .* successfully"
    public void testFX_testValidBzipArchive_Alert() {
        // Given
        final Path archive = Paths.get("src",  "test", "resources", "test.tar.bz2").toAbsolutePath();
        simOpenArchive(this, archive, true, false);

        // When
        simTestArchive(this);

        // Then
        CommonSpecifications.thenExpectDialogWithMatchingMessage(this, "Parsed archive .* successfully");
    }

    @Test
    @DisplayName("Test: test valid Gzip archive returns success alert")
    // GIVEN zip archive (test.tar.gz) is open in PearlZip
    // WHEN test archive is initiated
    // THEN a dialog appears with message like "Parsed archive .* successfully"
    public void testFX_testValidGzipArchive_Alert() {
        // Given
        final Path archive = Paths.get("src",  "test", "resources", "test.tar.gz").toAbsolutePath();
        simOpenArchive(this, archive, true, false);

        // When
        simTestArchive(this);

        // Then
        CommonSpecifications.thenExpectDialogWithMatchingMessage(this, "Parsed archive .* successfully");
    }

    @Test
    @DisplayName("Test: test valid xz archive returns success alert")
    // GIVEN zip archive (test.tar.xz) is open in PearlZip
    // WHEN test archive is initiated
    // THEN a dialog appears with message like "Parsed archive .* successfully"
    public void testFX_testValidXZArchive_Alert() {
        // Given
        final Path archive = Paths.get("src",  "test", "resources", "test.tar.xz").toAbsolutePath();
        simOpenArchive(this, archive, true, false);

        // When
        simTestArchive(this);

        // Then
        CommonSpecifications.thenExpectDialogWithMatchingMessage(this, "Parsed archive .* successfully");
    }

    ////////////////////////////////////////////////////////////////////////////////
    ////////// TEST ARCHIVE - FAILURE //////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    @Test
    @DisplayName("Test: test invalid xz archive returns failure alert")
    // GIVEN zip archive (broken.tar.xz) is open in PearlZip
    // WHEN test archive is initiated
    // THEN a dialog appears with message like "Parsing of archive file .* failed. Check log output for more information."
    public void testFX_testInvalidXZArchive_Alert() throws IOException {
        // Given
        final Path archive = Paths.get("src",  "test", "resources", "broken.tar.xz").toAbsolutePath();
        Path tmpArchive = PearlZipSpecifications.givenCreateNewArchive(this, "xz", "broken.tar");
        simOpenArchive(this, tmpArchive, true, false);
        Files.copy(archive, tmpArchive, StandardCopyOption.REPLACE_EXISTING);

        // When
        simTestArchive(this);

        // Then
        CommonSpecifications.thenExpectDialogWithMatchingMessage(this, "Parsing of archive file .* failed. Check log output for more information.");
    }


    @Test
    @DisplayName("Test: test invalid zip archive returns success alert")
    // GIVEN zip archive (test.zip) is open in PearlZip
    // WHEN archive is deleted
    //     AND test archive is initiated
    // THEN a dialog appears with message like "Archive .* does not exist. PearlZip will now close the instance."
    public void testFX_testNonExistentArchive_Alert() throws IOException {
        // Given
        final Path srcPath = Paths.get("src", "test", "resources", "test.zip")
                                  .toAbsolutePath();
        final Path archivePath = Paths.get(Files.createTempDirectory("pz").toAbsolutePath().toString(), "test.zip")
                                      .toAbsolutePath();
        Files.copy(srcPath, archivePath);

        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // When
        Files.deleteIfExists(archivePath);
        simTestArchive(this);

        // Then
        CommonSpecifications.thenExpectDialogWithMatchingMessage(this, "Archive .* does not exist. PearlZip will now close the instance.");
    }

}
