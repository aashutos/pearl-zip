/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.testfx;

import com.ntak.pearlzip.ui.util.AbstractPearlZipTestFX;
import javafx.scene.control.DialogPane;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.ntak.pearlzip.ui.util.PearlZipFXUtil.simOpenArchive;
import static com.ntak.pearlzip.ui.util.PearlZipFXUtil.simTestArchive;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class TestArchiveTestFX extends AbstractPearlZipTestFX {

    /*
     *  Test cases:
     *  + Successful integrity check of archive
     *  + Failure on integrity check of archive
     *  + Successful integrity check of empty tar (Bug: PZ-97)
     */

    @AfterEach
    public void tearDown() throws IOException {

    }

    ////////////////////////////////////////////////////////////////////////////////
    ////////// TEST ARCHIVE - SUCCESS //////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    @Test
    @DisplayName("Test: test valid zip archive returns success alert")
    public void testFX_testValidZipArchive_Alert() {
        final String archiveFormat = "zip";
        final String archiveName = String.format("test.%s", archiveFormat);

        final Path archive = Paths.get("src",  "test", "resources", archiveName).toAbsolutePath();

        // Open archive...
        simOpenArchive(this, archive, true, false);

        // Test archive
        simTestArchive(this);

        // Interrogate alert dialog
        DialogPane dialogPane = lookup(".dialog-pane").query();
        Assertions.assertTrue(dialogPane.getContentText().matches("Parsed archive .* successfully"),
                              "The archive was not valid");
        sleep(50, MILLISECONDS);
    }

    @Test
    @DisplayName("Test: test valid jar archive returns success alert")
    public void testFX_testValidJarArchive_Alert() {
        final String archiveFormat = "jar";
        final String archiveName = String.format("test.%s", archiveFormat);

        final Path archive = Paths.get("src",  "test", "resources", archiveName).toAbsolutePath();

        // Open archive...
        simOpenArchive(this, archive, true, false);

        // Test archive
        simTestArchive(this);

        // Interrogate alert dialog
        DialogPane dialogPane = lookup(".dialog-pane").query();
        Assertions.assertTrue(dialogPane.getContentText().matches("Parsed archive .* successfully"),
                              "The archive was not valid");
        sleep(50, MILLISECONDS);
    }

    @Test
    @DisplayName("Test: test valid tar archive returns success alert")
    public void testFX_testValidTarArchive_Alert() {
        final String archiveFormat = "tar";
        final String archiveName = String.format("test.%s", archiveFormat);

        final Path archive = Paths.get("src",  "test", "resources", archiveName).toAbsolutePath();

        // Open archive...
        simOpenArchive(this, archive, true, false);

        // Test archive
        simTestArchive(this);

        // Interrogate alert dialog
        DialogPane dialogPane = lookup(".dialog-pane").query();
        Assertions.assertTrue(dialogPane.getContentText().matches("Parsed archive .* successfully"),
                              "The archive was not valid");
        sleep(50, MILLISECONDS);
    }

    @Test
    @DisplayName("Test: test valid empty tar archive returns success alert")
    public void testFX_testValidEmptyTarArchive_Alert() {
        final String archiveFormat = "tar";
        final String archiveName = String.format("empty-archive.%s", archiveFormat);

        final Path archive = Paths.get("src",  "test", "resources", archiveName).toAbsolutePath();

        // Open archive...
        simOpenArchive(this, archive, true, false);

        // Test archive
        simTestArchive(this);

        // Interrogate alert dialog
        DialogPane dialogPane = lookup(".dialog-pane").query();
        Assertions.assertTrue(dialogPane.getContentText().matches("Parsed archive .* successfully"),
                              "The archive was not valid");
        sleep(50, MILLISECONDS);
    }

    @Test
    @DisplayName("Test: test valid cab archive returns success alert")
    public void testFX_testValidCabArchive_Alert() {
        final String archiveFormat = "cab";
        final String archiveName = String.format("test.%s", archiveFormat);

        final Path archive = Paths.get("src",  "test", "resources", archiveName).toAbsolutePath();

        // Open archive...
        simOpenArchive(this, archive, true, false);

        // Test archive
        simTestArchive(this);

        // Interrogate alert dialog
        DialogPane dialogPane = lookup(".dialog-pane").query();
        Assertions.assertTrue(dialogPane.getContentText().matches("Parsed archive .* successfully"),
                              "The archive was not valid");
        sleep(50, MILLISECONDS);
    }

    @Test
    @DisplayName("Test: test valid iso archive returns success alert")
    public void testFX_testValidIsoArchive_Alert() {
        final String archiveFormat = "iso";
        final String archiveName = String.format("test.%s", archiveFormat);

        final Path archive = Paths.get("src",  "test", "resources", archiveName).toAbsolutePath();

        // Open archive...
        simOpenArchive(this, archive, true, false);

        // Test archive
        simTestArchive(this);

        // Interrogate alert dialog
        DialogPane dialogPane = lookup(".dialog-pane").query();
        Assertions.assertTrue(dialogPane.getContentText().matches("Parsed archive .* successfully"),
                              "The archive was not valid");
        sleep(50, MILLISECONDS);
    }

    @Test
    @DisplayName("Test: test valid rar archive returns success alert")
    public void testFX_testValidRarArchive_Alert() {
        final String archiveFormat = "rar";
        final String archiveName = String.format("test.%s", archiveFormat);

        final Path archive = Paths.get("src",  "test", "resources", archiveName).toAbsolutePath();

        // Open archive...
        simOpenArchive(this, archive, true, false);

        // Test archive
        simTestArchive(this);

        // Interrogate alert dialog
        DialogPane dialogPane = lookup(".dialog-pane").query();
        Assertions.assertTrue(dialogPane.getContentText().matches("Parsed archive .* successfully"),
                              "The archive was not valid");
        sleep(50, MILLISECONDS);
    }

    @Test
    @DisplayName("Test: test valid 7zip archive returns success alert")
    public void testFX_testValid7zipArchive_Alert() {
        final String archiveFormat = "7z";
        final String archiveName = String.format("test.%s", archiveFormat);

        final Path archive = Paths.get("src",  "test", "resources", archiveName).toAbsolutePath();

        // Open archive...
        simOpenArchive(this, archive, true, false);

        // Test archive
        simTestArchive(this);

        // Interrogate alert dialog
        DialogPane dialogPane = lookup(".dialog-pane").query();
        Assertions.assertTrue(dialogPane.getContentText().matches("Parsed archive .* successfully"),
                              "The archive was not valid");
        sleep(50, MILLISECONDS);
    }

    @Test
    @DisplayName("Test: test valid Bzip archive returns success alert")
    public void testFX_testValidBzipArchive_Alert() {
        final String archiveFormat = "tar.bz2";
        final String archiveName = String.format("test.%s", archiveFormat);

        final Path archive = Paths.get("src",  "test", "resources", archiveName).toAbsolutePath();

        // Open archive...
        simOpenArchive(this, archive, true, false);

        // Test archive
        simTestArchive(this);

        // Interrogate alert dialog
        DialogPane dialogPane = lookup(".dialog-pane").query();
        Assertions.assertTrue(dialogPane.getContentText().matches("Parsed archive .* successfully"),
                              "The archive was not valid");
        sleep(50, MILLISECONDS);
    }

    @Test
    @DisplayName("Test: test valid Gzip archive returns failure alert")
    public void testFX_testValidGzipArchive_Alert() {
        final String archiveFormat = "tar.gz";
        final String archiveName = String.format("test.%s", archiveFormat);

        final Path archive = Paths.get("src",  "test", "resources", archiveName).toAbsolutePath();

        // Open archive...
        simOpenArchive(this, archive, true, false);

        // Test archive
        simTestArchive(this);

        // Interrogate alert dialog
        DialogPane dialogPane = lookup(".dialog-pane").query();
        Assertions.assertTrue(dialogPane.getContentText().matches("Parsed archive .* successfully"),
                              "The archive was not valid");
        sleep(50, MILLISECONDS);
    }

    @Test
    @DisplayName("Test: test valid xz archive returns success alert")
    public void testFX_testValidXZArchive_Alert() {
        final String archiveFormat = "tar.xz";
        final String archiveName = String.format("test.%s", archiveFormat);

        final Path archive = Paths.get("src",  "test", "resources", archiveName).toAbsolutePath();

        // Open archive...
        simOpenArchive(this, archive, true, false);

        // Test archive
        simTestArchive(this);

        // Interrogate alert dialog
        DialogPane dialogPane = lookup(".dialog-pane").query();
        Assertions.assertTrue(dialogPane.getContentText().matches("Parsed archive .* successfully"),
                              "The archive was not valid");
        sleep(50, MILLISECONDS);
    }

    ////////////////////////////////////////////////////////////////////////////////
    ////////// TEST ARCHIVE - FAILURE //////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    @Test
    @DisplayName("Test: test invalid xz archive returns success alert")
    public void testFX_testInvalidXZArchive_Alert() {
        final String archiveFormat = "tar.xz";
        final String archiveName = String.format("broken.%s", archiveFormat);

        final Path archive = Paths.get("src",  "test", "resources", archiveName).toAbsolutePath();

        // Open archive...
        simOpenArchive(this, archive, true, false);

        // Test archive
        simTestArchive(this);

        // Interrogate alert dialog
        DialogPane dialogPane = lookup(".dialog-pane").query();
        Assertions.assertTrue(dialogPane.getContentText()
                                        .matches("Parsing of archive file .* failed. Check log output for more information."),
                              "The archive was valid unexpectedly");
        sleep(50, MILLISECONDS);
    }
}
