/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.testfx;

import com.ntak.pearlzip.ui.UITestSuite;
import com.ntak.pearlzip.ui.util.AbstractPearlZipTestFX;
import com.ntak.pearlzip.ui.util.PearlZipSpecifications;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

@Tag("fx-test")
public class NewArchiveTestFX extends AbstractPearlZipTestFX {

    /*
     *  Test cases:
     *  + Create zip archive from main window
     *  + Create tar archive from main window
     *  + Create jar archive from main window
     *  + Create zip archive from System menu
     *  + Create tar archive from System menu
     *  + Create jar archive from System menu
     */

    @AfterEach
    public void tearDown() throws IOException {
        for (Path dir :
                Files.list(Files.createTempDirectory("pz")
                                .getParent()
                                .getParent())
                     .filter(p->p.getFileName()
                                 .toString()
                                 .startsWith("pz"))
                     .collect(Collectors.toList())) {
            UITestSuite.clearDirectory(dir);
        }
    }

    @Test
    @DisplayName("Test: Create new zip archive successfully from the main window")
    // GIVEN a new zip archive has been created in PearlZip
    // THEN ensure file exists
    public void testFX_CreateNewZipArchiveMainWindow_Success() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "zip", "testzip");

        // Then
        Assertions.assertTrue(Files.exists(archive), String.format("File %s does not exist.", archive));
    }

    @Test
    @DisplayName("Test: Create new tar archive successfully from the main window")
    // GIVEN a new tar archive has been created in PearlZip
    // THEN ensure file exists
    public void testFX_CreateNewTarArchiveMainWindow_Success() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "tar", "testtar");

        // Then
        Assertions.assertTrue(Files.exists(archive), String.format("File %s does not exist.", archive));
    }

    @Test
    @DisplayName("Test: Create new jar archive successfully from the main window")
    // GIVEN a new jar archive has been created in PearlZip
    // THEN ensure file exists
    public void testFX_CreateNewJarArchiveMainWindow_Success() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "jar", "testjar");

        // Then
        Assertions.assertTrue(Files.exists(archive), String.format("File %s does not exist.", archive));
    }

    @Test
    @DisplayName("Test: Create new xz tarball archive successfully from the main window")
    // GIVEN a new xz archive has been created in PearlZip
    // THEN ensure file exists
    public void testFX_CreateNewXzArchiveMainWindow_Success() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "xz", "testxz.tar");

        // Then
        Assertions.assertTrue(Files.exists(archive), String.format("File %s does not exist.", archive));
    }

    @Test
    @DisplayName("Test: Create new bzip tarball archive successfully from the main window")
    // GIVEN a new bz2 archive has been created in PearlZip
    // THEN ensure file exists
    public void testFX_CreateNewBzipArchiveMainWindow_Success() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "bz2", "testbz2.tar");

        // Then
        Assertions.assertTrue(Files.exists(archive), String.format("File %s does not exist.", archive));
    }

    @Test
    @DisplayName("Test: Create new gzip tarball archive successfully from the main window")
    // GIVEN a new gz archive has been created in PearlZip
    // THEN ensure file exists
    public void testFX_CreateNewGzipArchiveMainWindow_Success() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "gz", "testgz.tar");

        // Then
        Assertions.assertTrue(Files.exists(archive), String.format("File %s does not exist.", archive));
    }

    @Test
    @DisplayName("Test: Create new zip archive successfully from the system menu")
    // GIVEN a new zip archive has been created in PearlZip (via system menu)
    // THEN ensure file exists
    public void testFX_CreateNewZipArchiveSystemMenu_Success() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "zip", "testzip", true);

        // Then
        Assertions.assertTrue(Files.exists(archive), String.format("File %s does not exist.", archive));
    }

    @Test
    @DisplayName("Test: Create new jar archive successfully from the system menu")
    // GIVEN a new jar archive has been created in PearlZip (via system menu)
    // THEN ensure file exists
    public void testFX_CreateNewJarArchiveSystemMenu_Success() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "jar", "testjar", true);

        // Then
        Assertions.assertTrue(Files.exists(archive), String.format("File %s does not exist.", archive));
    }

    @Test
    @DisplayName("Test: Create new tar archive successfully from the system menu")
    // GIVEN a new tar archive has been created in PearlZip (via system menu)
    // THEN ensure file exists
    public void testFX_CreateNewTarArchiveSystemMenu_Success() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "tar", "testtar", true);

        // Then
        Assertions.assertTrue(Files.exists(archive), String.format("File %s does not exist.", archive));
    }

    @Test
    @DisplayName("Test: Create new Gzip archive successfully from the system menu")
    // GIVEN a new gz archive has been created in PearlZip (via system menu)
    // THEN ensure file exists
    public void testFX_CreateNewGzipArchiveSystemMenu_Success() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "gz", "testgz.tar", true);

        // Then
        Assertions.assertTrue(Files.exists(archive), String.format("File %s does not exist.", archive));
    }


    @Test
    @DisplayName("Test: Create new xz archive successfully from the system menu")
    // GIVEN a new xz archive has been created in PearlZip (via system menu)
    // THEN ensure file exists
    public void testFX_CreateNewXzArchiveSystemMenu_Success() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "xz", "testxz.tar", true);

        // Then
        Assertions.assertTrue(Files.exists(archive), String.format("File %s does not exist.", archive));
    }

    @Test
    @DisplayName("Test: Create new Bzip archive successfully from the system menu")
    // GIVEN a new bz2 archive has been created in PearlZip (via system menu)
    // THEN ensure file exists
    public void testFX_CreateNewBzipArchiveSystemMenu_Success() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "bz2", "testbz2.tar", true);

        // Then
        Assertions.assertTrue(Files.exists(archive), String.format("File %s does not exist.", archive));
    }
}
