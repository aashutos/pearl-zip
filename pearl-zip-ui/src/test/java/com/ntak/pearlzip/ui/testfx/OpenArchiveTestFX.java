/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.testfx;

import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.util.AbstractPearlZipTestFX;
import com.ntak.pearlzip.ui.util.PearlZipSpecifications;
import javafx.scene.control.TableColumn;
import javafx.scene.input.MouseButton;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static com.ntak.pearlzip.ui.constants.ZipConstants.CK_RECENT_FILE;
import static com.ntak.pearlzip.ui.util.PearlZipFXUtil.*;
import static com.ntak.testfx.TestFXConstants.LONG_PAUSE;
import static com.ntak.testfx.specifications.CommonSpecifications.*;

public class OpenArchiveTestFX extends AbstractPearlZipTestFX {
    /*
     * Test cases:
     * + Open archive - new window
     * + Open archive - current window
     * + Open recent files up (non-replacing entries: 1-5)
     * + Open sixth recent file which replaces the oldest entry in rf file
     * + Double click text file to open externally
     * + Item ordering in archive
     * + Save opened archive as another file with no extension will append extension automatically
     */

    @AfterEach
    public void tearDown() {

    }

    ////////////////////////////////////////////////////////////////////////////////
    ////////// NEW WINDOW //////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    @Test
    @DisplayName("Test: Open zip archive successfully (new window)")
    // GIVEN zip archive (test.zip) is open in PearlZip by System menu
    // THEN ensure only 1 main stage instances are open
    // GIVEN zip archive (test.zip) is open in PearlZip by Main window
    // THEN ensure only 2 main stage instances are open
    public void testFX_OpenZipArchiveNewWindow_Success() throws IOException {
        // Given
        final Path srcPath = Paths.get("src", "test", "resources", "test.zip")
                                  .toAbsolutePath();
        final Path archivePath = Paths.get(Files.createTempDirectory("pz").toAbsolutePath().toString(), "test.zip")
                                      .toAbsolutePath();
        Files.copy(srcPath, archivePath);

        simOpenArchiveBySysMenu(this, archivePath, false);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(1);

        // Given
        simOpenArchive(this, archivePath, true, true);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(2);
    }

    @Test
    @DisplayName("Test: Open jar archive successfully (new window)")
    // GIVEN zip archive (test.jar) is open in PearlZip by System menu
    // THEN ensure only 1 main stage instances are open
    // GIVEN zip archive (test.jar) is open in PearlZip by Main window
    // THEN ensure only 2 main stage instances are open
    public void testFX_OpenJarArchiveNewWindow_Success() throws IOException {
        // Given
        final Path srcPath = Paths.get("src", "test", "resources", "test.jar")
                                  .toAbsolutePath();
        final Path archivePath = Paths.get(Files.createTempDirectory("pz").toAbsolutePath().toString(), "test.jar")
                                      .toAbsolutePath();
        Files.copy(srcPath, archivePath);

        simOpenArchiveBySysMenu(this, archivePath, false);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(1);

        // Given
        simOpenArchive(this, archivePath, true, true);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(2);
    }

    @Test
    @DisplayName("Test: Open 7z archive successfully (new window)")
    // GIVEN zip archive (test.7z) is open in PearlZip by System menu
    // THEN ensure only 1 main stage instances are open
    // GIVEN zip archive (test.7z) is open in PearlZip by Main window
    // THEN ensure only 2 main stage instances are open
    public void testFX_Open7zArchiveNewWindow_Success() throws IOException {
        // Given
        final Path srcPath = Paths.get("src", "test", "resources", "test.7z")
                                  .toAbsolutePath();
        final Path archivePath = Paths.get(Files.createTempDirectory("pz").toAbsolutePath().toString(), "test.7z")
                                      .toAbsolutePath();
        Files.copy(srcPath, archivePath);

        simOpenArchiveBySysMenu(this, archivePath, false);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(1);

        // Given
        simOpenArchive(this, archivePath, true, true);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(2);
    }

    @Test
    @DisplayName("Test: Open cab archive successfully (new window)")
    // GIVEN zip archive (test.cab) is open in PearlZip by System menu
    // THEN ensure only 1 main stage instances are open
    // GIVEN zip archive (test.cab) is open in PearlZip by Main window
    // THEN ensure only 2 main stage instances are open
    public void testFX_OpenCabArchiveNewWindow_Success() throws IOException {
        // Given
        final Path srcPath = Paths.get("src", "test", "resources", "test.cab")
                                  .toAbsolutePath();
        final Path archivePath = Paths.get(Files.createTempDirectory("pz").toAbsolutePath().toString(), "test.cab")
                                      .toAbsolutePath();
        Files.copy(srcPath, archivePath);

        simOpenArchiveBySysMenu(this, archivePath, false);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(1);

        // Given
        simOpenArchive(this, archivePath, true, true);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(2);
    }

    @Test
    @DisplayName("Test: Open iso archive successfully (new window)")
    // GIVEN zip archive (test.iso) is open in PearlZip by System menu
    // THEN ensure only 1 main stage instances are open
    // GIVEN zip archive (test.iso) is open in PearlZip by Main window
    // THEN ensure only 2 main stage instances are open
    public void testFX_OpenIsoArchiveNewWindow_Success() throws IOException {
        // Given
        final Path srcPath = Paths.get("src", "test", "resources", "test.iso")
                                  .toAbsolutePath();
        final Path archivePath = Paths.get(Files.createTempDirectory("pz").toAbsolutePath().toString(), "test.iso")
                                      .toAbsolutePath();
        Files.copy(srcPath, archivePath);

        simOpenArchiveBySysMenu(this, archivePath, false);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(1);

        // Given
        simOpenArchive(this, archivePath, true, true);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(2);
    }

    @Test
    @DisplayName("Test: Open rar archive successfully (new window)")
    // GIVEN zip archive (test.rar) is open in PearlZip by System menu
    // THEN ensure only 1 main stage instances are open
    // GIVEN zip archive (test.rar) is open in PearlZip by Main window
    // THEN ensure only 2 main stage instances are open
    public void testFX_OpenRarArchiveNewWindow_Success() throws IOException {
        // Given
        final Path srcPath = Paths.get("src", "test", "resources", "test.rar")
                                  .toAbsolutePath();
        final Path archivePath = Paths.get(Files.createTempDirectory("pz").toAbsolutePath().toString(), "test.rar")
                                      .toAbsolutePath();
        Files.copy(srcPath, archivePath);

        simOpenArchiveBySysMenu(this, archivePath, false);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(1);

        // Given
        simOpenArchive(this, archivePath, true, true);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(2);
    }

    @Test
    @DisplayName("Test: Open tar archive successfully (new window)")
    // GIVEN zip archive (test.tar) is open in PearlZip by System menu
    // THEN ensure only 1 main stage instances are open
    // GIVEN zip archive (test.tar) is open in PearlZip by Main window
    // THEN ensure only 2 main stage instances are open
    public void testFX_OpenTarArchiveNewWindow_Success() throws IOException {
        // Given
        final Path srcPath = Paths.get("src", "test", "resources", "test.tar")
                                  .toAbsolutePath();
        final Path archivePath = Paths.get(Files.createTempDirectory("pz").toAbsolutePath().toString(), "test.tar")
                                      .toAbsolutePath();
        Files.copy(srcPath, archivePath);

        simOpenArchiveBySysMenu(this, archivePath, false);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(1);

        // Given
        simOpenArchive(this, archivePath, true, true);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(2);
    }

    @Test
    @DisplayName("Test: Open BZip archive successfully (new window)")
    // GIVEN zip archive (test.tar.bz2) is open in PearlZip by System menu
    // THEN ensure only 1 main stage instances are open
    // GIVEN zip archive (test.tar.bz2) is open in PearlZip by Main window
    // THEN ensure only 2 main stage instances are open
    public void testFX_OpenBzipArchiveNewWindow_Success() throws IOException {
        // Given
        final Path srcPath = Paths.get("src", "test", "resources", "test.tar.bz2")
                                  .toAbsolutePath();
        final Path archivePath = Paths.get(Files.createTempDirectory("pz").toAbsolutePath().toString(), "test.tar.bz2")
                                      .toAbsolutePath();
        Files.copy(srcPath, archivePath);

        simOpenArchiveBySysMenu(this, archivePath, false);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(1);

        // Given
        simOpenArchive(this, archivePath, true, true);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(2);
    }

    @Test
    @DisplayName("Test: Open GZip archive successfully (new window)")
    // GIVEN zip archive (test.tar.gz) is open in PearlZip by System menu
    // THEN ensure only 1 main stage instances are open
    // GIVEN zip archive (test.tar.gz) is open in PearlZip by Main window
    // THEN ensure only 2 main stage instances are open
    public void testFX_OpenGzipArchiveNewWindow_Success() throws IOException {
        // Given
        final Path srcPath = Paths.get("src", "test", "resources", "test.tar.gz")
                                  .toAbsolutePath();
        final Path archivePath = Paths.get(Files.createTempDirectory("pz").toAbsolutePath().toString(), "test.tar.gz")
                                      .toAbsolutePath();
        Files.copy(srcPath, archivePath);

        simOpenArchiveBySysMenu(this, archivePath, false);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(1);

        // Given
        simOpenArchive(this, archivePath, true, true);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(2);
    }

    @Test
    @DisplayName("Test: Open xz archive successfully (new window)")
    // GIVEN zip archive (test.tar.xz) is open in PearlZip by System menu
    // THEN ensure only 1 main stage instances are open
    // GIVEN zip archive (test.tar.xz) is open in PearlZip by Main window
    // THEN ensure only 2 main stage instances are open
    public void testFX_OpenXZArchiveNewWindow_Success() throws IOException {
        // Given
        final Path srcPath = Paths.get("src", "test", "resources", "test.tar.xz")
                                  .toAbsolutePath();
        final Path archivePath = Paths.get(Files.createTempDirectory("pz").toAbsolutePath().toString(), "test.tar.xz")
                                      .toAbsolutePath();
        Files.copy(srcPath, archivePath);

        simOpenArchiveBySysMenu(this, archivePath, false);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(1);

        // Given
        simOpenArchive(this, archivePath, true, true);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(2);
    }

    ////////////////////////////////////////////////////////////////////////////////
    ////////// CURRENT WINDOW //////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    @Test
    @DisplayName("Test: Open zip archive successfully (current window)")
    // GIVEN zip archive (test.zip) is open in PearlZip by System menu
    //      AND zip archive (test.zip) is open in PearlZip by Main window (current window)
    // THEN ensure only 1 main stage instances are open
    public void testFX_OpenZipArchiveCurrentWindow_Success() throws IOException {
        // Given
        final Path srcPath = Paths.get("src", "test", "resources", "test.zip")
                                  .toAbsolutePath();
        final Path archivePath = Paths.get(Files.createTempDirectory("pz").toAbsolutePath().toString(), "test.zip")
                                      .toAbsolutePath();
        Files.copy(srcPath, archivePath);

        simOpenArchiveBySysMenu(this, archivePath, false);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(1);

        // Given
        simOpenArchive(this, archivePath, true, false);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(1);
    }

    @Test
    @DisplayName("Test: Open jar archive successfully (current window)")
    // GIVEN zip archive (test.jar) is open in PearlZip by System menu
    //      AND zip archive (test.jar) is open in PearlZip by Main window (current window)
    // THEN ensure only 1 main stage instances are open
    public void testFX_OpenJarArchiveCurrentWindow_Success() throws IOException {
        // Given
        final Path srcPath = Paths.get("src", "test", "resources", "test.jar")
                                  .toAbsolutePath();
        final Path archivePath = Paths.get(Files.createTempDirectory("pz").toAbsolutePath().toString(), "test.jar")
                                      .toAbsolutePath();
        Files.copy(srcPath, archivePath);

        simOpenArchiveBySysMenu(this, archivePath, false);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(1);

        // Given
        simOpenArchive(this, archivePath, true, false);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(1);
    }

    @Test
    @DisplayName("Test: Open 7z archive successfully (current window)")
    // GIVEN zip archive (test.7z) is open in PearlZip by System menu
    //      AND zip archive (test.7z) is open in PearlZip by Main window (current window)
    // THEN ensure only 1 main stage instances are open
    public void testFX_Open7zArchiveCurrentWindow_Success() throws IOException {
        // Given
        final Path srcPath = Paths.get("src", "test", "resources", "test.7z")
                                  .toAbsolutePath();
        final Path archivePath = Paths.get(Files.createTempDirectory("pz").toAbsolutePath().toString(), "test.7z")
                                      .toAbsolutePath();
        Files.copy(srcPath, archivePath);

        simOpenArchiveBySysMenu(this, archivePath, false);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(1);

        // Given
        simOpenArchive(this, archivePath, true, false);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(1);
    }

    @Test
    @DisplayName("Test: Open cab archive successfully (current window)")
    // GIVEN zip archive (test.cab) is open in PearlZip by System menu
    //      AND zip archive (test.cab) is open in PearlZip by Main window (current window)
    // THEN ensure only 1 main stage instances are open
    public void testFX_OpenCabArchiveCurrentWindow_Success() throws IOException {
        // Given
        final Path srcPath = Paths.get("src", "test", "resources", "test.cab")
                                  .toAbsolutePath();
        final Path archivePath = Paths.get(Files.createTempDirectory("pz").toAbsolutePath().toString(), "test.cab")
                                      .toAbsolutePath();
        Files.copy(srcPath, archivePath);

        simOpenArchiveBySysMenu(this, archivePath, false);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(1);

        // Given
        simOpenArchive(this, archivePath, true, false);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(1);
    }

    @Test
    @DisplayName("Test: Open iso archive successfully (current window)")
    // GIVEN zip archive (test.iso) is open in PearlZip by System menu
    //      AND zip archive (test.iso) is open in PearlZip by Main window (current window)
    // THEN ensure only 1 main stage instances are open
    public void testFX_OpenIsoArchiveCurrentWindow_Success() throws IOException {
        // Given
        final Path srcPath = Paths.get("src", "test", "resources", "test.iso")
                                  .toAbsolutePath();
        final Path archivePath = Paths.get(Files.createTempDirectory("pz").toAbsolutePath().toString(), "test.iso")
                                      .toAbsolutePath();
        Files.copy(srcPath, archivePath);

        simOpenArchiveBySysMenu(this, archivePath, false);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(1);

        // Given
        simOpenArchive(this, archivePath, true, false);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(1);
    }

    @Test
    @DisplayName("Test: Open rar archive successfully (current window)")
    // GIVEN zip archive (test.rar) is open in PearlZip by System menu
    //      AND zip archive (test.rar) is open in PearlZip by Main window (current window)
    // THEN ensure only 1 main stage instances are open
    public void testFX_OpenRarArchiveCurrentWindow_Success() throws IOException {
        // Given
        final Path srcPath = Paths.get("src", "test", "resources", "test.rar")
                                  .toAbsolutePath();
        final Path archivePath = Paths.get(Files.createTempDirectory("pz").toAbsolutePath().toString(), "test.rar")
                                      .toAbsolutePath();
        Files.copy(srcPath, archivePath);

        simOpenArchiveBySysMenu(this, archivePath, false);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(1);

        // Given
        simOpenArchive(this, archivePath, true, false);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(1);
    }

    @Test
    @DisplayName("Test: Open tar archive successfully (current window)")
    // GIVEN zip archive (test.tar) is open in PearlZip by System menu
    //      AND zip archive (test.tar) is open in PearlZip by Main window (current window)
    // THEN ensure only 1 main stage instances are open
    public void testFX_OpenTarArchiveCurrentWindow_Success() throws IOException {
        // Given
        final Path srcPath = Paths.get("src", "test", "resources", "test.tar")
                                  .toAbsolutePath();
        final Path archivePath = Paths.get(Files.createTempDirectory("pz").toAbsolutePath().toString(), "test.tar")
                                      .toAbsolutePath();
        Files.copy(srcPath, archivePath);

        simOpenArchiveBySysMenu(this, archivePath, false);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(1);

        // Given
        simOpenArchive(this, archivePath, true, false);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(1);
    }

    @Test
    @DisplayName("Test: Open BZip archive successfully (current window)")
    // GIVEN zip archive (test.tar.bz2) is open in PearlZip by System menu
    //      AND zip archive (test.tar.bz2) is open in PearlZip by Main window (current window)
    // THEN ensure only 1 main stage instances are open
    public void testFX_OpenBzipArchiveCurrentWindow_Success() throws IOException {
        // Given
        final Path srcPath = Paths.get("src", "test", "resources", "test.tar.bz2")
                                  .toAbsolutePath();
        final Path archivePath = Paths.get(Files.createTempDirectory("pz").toAbsolutePath().toString(), "test.tar.bz2")
                                      .toAbsolutePath();
        Files.copy(srcPath, archivePath);

        simOpenArchiveBySysMenu(this, archivePath, false);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(1);

        // Given
        simOpenArchive(this, archivePath, true, false);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(1);
    }

    @Test
    @DisplayName("Test: Open GZip archive successfully (current window)")
    // GIVEN zip archive (test.tar.gz) is open in PearlZip by System menu
    //      AND zip archive (test.tar.gz) is open in PearlZip by Main window (current window)
    // THEN ensure only 1 main stage instances are open
    public void testFX_OpenGzipArchiveCurrentWindow_Success() throws IOException {
        // Given
        final Path srcPath = Paths.get("src", "test", "resources", "test.tar.gz")
                                  .toAbsolutePath();
        final Path archivePath = Paths.get(Files.createTempDirectory("pz").toAbsolutePath().toString(), "test.tar.gz")
                                      .toAbsolutePath();
        Files.copy(srcPath, archivePath);

        simOpenArchiveBySysMenu(this, archivePath, false);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(1);

        // Given
        simOpenArchive(this, archivePath, true, false);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(1);
    }

    @Test
    @DisplayName("Test: Open xz archive successfully (current window)")
    // GIVEN zip archive (test.tar.xz) is open in PearlZip by System menu
    //      AND zip archive (test.tar.xz) is open in PearlZip by Main window (current window)
    // THEN ensure only 1 main stage instances are open
    public void testFX_OpenXZArchiveCurrentWindow_Success() throws IOException {
        // Given
        final Path srcPath = Paths.get("src", "test", "resources", "test.tar.xz")
                                  .toAbsolutePath();
        final Path archivePath = Paths.get(Files.createTempDirectory("pz").toAbsolutePath().toString(), "test.tar.xz")
                                      .toAbsolutePath();
        Files.copy(srcPath, archivePath);

        simOpenArchiveBySysMenu(this, archivePath, false);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(1);

        // Given
        simOpenArchive(this, archivePath, true, false);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(1);
    }

    @Test
    @DisplayName("Test: Opening the 5 files will update the recent files menu appropriately with new entries")
    // GIVEN RECENT_FILE deleted
    //      AND zip archive (test.zip) is open in PearlZip by System menu (in same window)
    //      AND zip archive (test.jar) is open in PearlZip by System menu (in same window)
    //      AND zip archive (test.tar) is open in PearlZip by System menu (in same window)
    //      AND zip archive (test.iso) is open in PearlZip by System menu (in same window)
    //      AND zip archive (test.cab) is open in PearlZip by System menu (in same window)
    // THEN ensure the line count for file RECENT_FILE = 5
    //      AND ensure the file RECENT_FILE contains line matching pattern (src/test/resources/test.zip)
    //      AND ensure the file RECENT_FILE contains line matching pattern (src/test/resources/test.jar)
    //      AND ensure the file RECENT_FILE contains line matching pattern (src/test/resources/test.tar)
    //      AND ensure the file RECENT_FILE contains line matching pattern (src/test/resources/test.iso)
    //      AND ensure the file RECENT_FILE contains line matching pattern (src/test/resources/test.cab)
    public void testFX_OpenRecentFilesFirst5Files_MatchExpectations() throws IOException {
        // Given
        final Path RECENT_FILE = InternalContextCache.GLOBAL_CONFIGURATION_CACHE.<Path>getAdditionalConfig(CK_RECENT_FILE)
                                                                                .get();
        Files.deleteIfExists(RECENT_FILE);

        for (String extension : Arrays.asList("zip", "jar", "tar", "iso", "cab")) {
            final Path srcPath = Paths.get("src", "test", "resources", String.format("test.%s", extension))
                                      .toAbsolutePath();
            simOpenArchiveBySysMenu(this, srcPath, false);
            sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        }

        // Then
        thenExpectNumberLinesInFile(RECENT_FILE, 5);
        thenExpectLinePatternInFile(RECENT_FILE, "src/test/resources/test.zip");
        thenExpectLinePatternInFile(RECENT_FILE, "src/test/resources/test.jar");
        thenExpectLinePatternInFile(RECENT_FILE, "src/test/resources/test.tar");
        thenExpectLinePatternInFile(RECENT_FILE, "src/test/resources/test.iso");
        thenExpectLinePatternInFile(RECENT_FILE, "src/test/resources/test.cab");
    }

    @Test
    @DisplayName("Test: Opening the 6th file will update the recent files menu by overwriting the oldest entry")
    // GIVEN RECENT_FILE deleted
    //      AND zip archive (test.zip) is open in PearlZip by System menu (in same window)
    //      AND zip archive (test.jar) is open in PearlZip by System menu (in same window)
    //      AND zip archive (test.tar) is open in PearlZip by System menu (in same window)
    //      AND zip archive (test.iso) is open in PearlZip by System menu (in same window)
    //      AND zip archive (test.cab) is open in PearlZip by System menu (in same window)
    //      AND zip archive (test.tar.gz) is open in PearlZip by System menu (in same window)
    // THEN ensure the line count for file RECENT_FILE = 5
    //      AND ensure the file RECENT_FILE does not contains line matching pattern (src/test/resources/test.zip)
    //      AND ensure the file RECENT_FILE contains line matching pattern (src/test/resources/test.jar)
    //      AND ensure the file RECENT_FILE contains line matching pattern (src/test/resources/test.tar)
    //      AND ensure the file RECENT_FILE contains line matching pattern (src/test/resources/test.iso)
    //      AND ensure the file RECENT_FILE contains line matching pattern (src/test/resources/test.cab)
    //      AND ensure the file RECENT_FILE contains line matching pattern (src/test/resources/test.tar.gz)
    public void testFX_OpenRecentFilesSixthFileOverwrite_MatchExpectations() throws IOException {
        // Given
        final Path RECENT_FILE = InternalContextCache.GLOBAL_CONFIGURATION_CACHE.<Path>getAdditionalConfig(CK_RECENT_FILE)
                                                                                .get();
        Files.deleteIfExists(RECENT_FILE);

        for (String extension : Arrays.asList("zip", "jar", "tar", "iso", "cab", "tar.gz")) {
            final Path srcPath = Paths.get("src", "test", "resources", String.format("test.%s", extension))
                                      .toAbsolutePath();
            simOpenArchiveBySysMenu(this, srcPath, false);
            sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        }

        // Then
        thenExpectNumberLinesInFile(RECENT_FILE, 5);
        thenNotExpectLinePatternInFile(RECENT_FILE, "src/test/resources/test.zip");
        thenExpectLinePatternInFile(RECENT_FILE, "src/test/resources/test.jar");
        thenExpectLinePatternInFile(RECENT_FILE, "src/test/resources/test.tar");
        thenExpectLinePatternInFile(RECENT_FILE, "src/test/resources/test.iso");
        thenExpectLinePatternInFile(RECENT_FILE, "src/test/resources/test.cab");
        thenExpectLinePatternInFile(RECENT_FILE, "src/test/resources/test.tar.gz");
    }

    @Test
    @DisplayName("Test: Open file in archive using OS defined software successfully")
    // GIVEN zip archive (test.zip) is open in PearlZip by System menu
    // WHEN select file (first-file)
    // THEN a dialog appears with message like "^Choosing yes will open a temporary copy of the selected file in an external application.*"
    public void testFX_OpenFileInArchiveExternally_Success() throws IOException {
        // Given
        final Path srcPath = Paths.get("src", "test", "resources", "test.zip")
                                  .toAbsolutePath();
        final Path archivePath = Paths.get(Files.createTempDirectory("pz").toAbsolutePath().toString(), "test.zip")
                                      .toAbsolutePath();
        Files.copy(srcPath, archivePath);

        simOpenArchiveBySysMenu(this, archivePath, false);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // When
        simTraversalArchive(this, archivePath.toString(), "#fileContentsView", (r)->{}, "first-file");
        doubleClickOn(MouseButton.PRIMARY);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);

        // Then
        thenExpectDialogWithMatchingMessage(this, "^Choosing yes will open a temporary copy of the selected file in an external application.*");
    }

    ////////////////////////////////////////////////////////////////////////////////
    ////////// OTHER TEST CASES ////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    @Test
    @DisplayName("Test: Open zip archive and check ordering functionality of items in main screen")
    // GIVEN zip archive (order-test.zip) is open in PearlZip by System menu
    // WHEN select name header in main window
    // THEN expect files in the following order (FILE-1,FILE-1.md5,nested-dir)
    // WHEN select name header in main window
    // THEN expect files in the following order (nested-dir,FILE-1.md5,FILE-1)
    // WHEN select file (nested-dir/.DS_Store)
    //     AND select size header in main window
    // THEN expect files in the following order (nested-dir/FILE-2,nested-dir/FILE-3.md5,nested-dir/FILE-3,nested-dir/.DS_Store)
    // WHEN select size header in main window
    // THEN expect files in the following order (nested-dir/.DS_Store,nested-dir/FILE-3,nested-dir/FILE-3.md5,nested-dir/FILE-2)
    // WHEN select modified header in main window
    // THEN expect files in the following order (nested-dir/.DS_Store,nested-dir/FILE-2,nested-dir/FILE-3,nested-dir/FILE-3.md5)
    // WHEN select modified header in main window
    // THEN expect files in the following order (nested-dir/FILE-3.md5,nested-dir/FILE-3,nested-dir/FILE-2,nested-dir/.DS_Store)
    public void testFX_OpenZipArchiveCheckOrdering_Success() {
        // Given
        final Path archivePath = Paths.get("src", "test", "resources", "order-test.zip")
                                      .toAbsolutePath();
        simOpenArchiveBySysMenu(this, archivePath, false);

        // When
        TableColumn<FileInfo,?> column = whenColumnExtractedFromTable(this, "#fileContentsView", "Name");
        this.clickOn(column.getStyleableNode())
            .sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);

        // Then
        PearlZipSpecifications.thenExpectFilesInOrderInCurrentWindow(archivePath.toString(), Arrays.asList("FILE-1","FILE-1.md5","nested-dir"));

        // When
        this.clickOn(column.getStyleableNode())
            .sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);

        // Then
        PearlZipSpecifications.thenExpectFilesInOrderInCurrentWindow(archivePath.toString(), Arrays.asList("nested-dir","FILE-1.md5","FILE-1"));


        // When
        simTraversalArchive(this, archivePath.toString(), "#fileContentsView", (r)->{}, "nested-dir",".DS_Store");
        sleep(250,TimeUnit.MILLISECONDS);

        column = whenColumnExtractedFromTable(this, "#fileContentsView", "Size");
        this.clickOn(column.getStyleableNode())
            .sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);

        // Then
        PearlZipSpecifications.thenExpectFilesInOrderInCurrentWindow(archivePath.toString(), Arrays.asList("nested-dir/FILE-2","nested-dir/FILE-3.md5","nested-dir/FILE-3","nested-dir/.DS_Store"));

        // When
        column = whenColumnExtractedFromTable(this, "#fileContentsView", "Size");
        this.clickOn(column.getStyleableNode())
            .sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);

        // Then
        PearlZipSpecifications.thenExpectFilesInOrderInCurrentWindow(archivePath.toString(), Arrays.asList("nested-dir/.DS_Store","nested-dir/FILE-3","nested-dir/FILE-3.md5","nested-dir/FILE-2"));


        // When
        column = whenColumnExtractedFromTable(this, "#fileContentsView", "Modified");
        this.clickOn(column.getStyleableNode())
            .sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);

        // Then
        PearlZipSpecifications.thenExpectFilesInOrderInCurrentWindow(archivePath.toString(), Arrays.asList("nested-dir/.DS_Store","nested-dir/FILE-2","nested-dir/FILE-3","nested-dir/FILE-3.md5"));

        // When
        column = whenColumnExtractedFromTable(this, "#fileContentsView", "Modified");
        this.clickOn(column.getStyleableNode())
            .sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);

        // Then
        PearlZipSpecifications.thenExpectFilesInOrderInCurrentWindow(archivePath.toString(), Arrays.asList("nested-dir/FILE-3.md5","nested-dir/FILE-3","nested-dir/FILE-2","nested-dir/.DS_Store"));
    }

    ////////////////////////////////////////////////////////////////////////////////
    ////////// SAVE OPENED ARCHIVE /////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    @Test
    @DisplayName("Test: Save opened archive as another file with no extension will append extension automatically")
    // GIVEN zip archive (test.zip) is open in PearlZip by System menu
    // WHEN save archive as (test)
    // THEN expect file (test.zip) exist in target location
    public void testFX_SaveOpenArchiveAs_NoExtension_Success() throws IOException {
        // Given
        final Path srcPath = Paths.get("src", "test", "resources", "test.zip")
                                  .toAbsolutePath();

        simOpenArchiveBySysMenu(this, srcPath, false);
        sleep(LONG_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(srcPath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // When
        final Path tempDirectory = Files.createTempDirectory("pz");
        simSaveAsBySysMenu(this, tempDirectory.resolve("test"));

        // Then
        thenExpectFileExists(srcPath);
    }
}
