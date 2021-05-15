/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.testfx;

import com.ntak.pearlzip.ui.util.AbstractPearlZipTestFX;
import com.ntak.pearlzip.ui.util.PearlZipFXUtil;
import javafx.geometry.Point2D;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    @Test
    @DisplayName("Test: Create new zip archive successfully from the main window")
    public void testFX_CreateNewZipArchiveMainWindow_Success() throws IOException {
        final String archiveFormat = "zip";
        final Path fileToSave = Paths.get(System.getProperty("user.home"), ".pz", "temp", String.format("test%s.%s", archiveFormat, archiveFormat));
        PearlZipFXUtil.simNewArchive(this, fileToSave);
    }

    @Test
    @DisplayName("Test: Create new tar archive successfully from the main window")
    public void testFX_CreateNewTarArchiveMainWindow_Success() throws IOException {
        final String archiveFormat = "tar";
        final Path fileToSave = Paths.get(System.getProperty("user.home"), ".pz", "temp", String.format("test%s.%s", archiveFormat, archiveFormat));
        PearlZipFXUtil.simNewArchive(this, fileToSave);
    }

    @Test
    @DisplayName("Test: Create new jar archive successfully from the main window")
    public void testFX_CreateNewJarArchiveMainWindow_Success() throws IOException {
        final String archiveFormat = "jar";
        final Path fileToSave = Paths.get(System.getProperty("user.home"), ".pz", "temp", String.format("test%s.%s", archiveFormat, archiveFormat));
        PearlZipFXUtil.simNewArchive(this, fileToSave);
    }

    @Test
    @DisplayName("Test: Create new xz tarball archive successfully from the main window")
    public void testFX_CreateNewXzArchiveMainWindow_Success() throws IOException {
        final String archiveFormat = "tar.xz";
        final Path fileToSave = Paths.get(System.getProperty("user.home"), ".pz", "temp", String.format("testxz.%s",
                                                                                                        archiveFormat));
        PearlZipFXUtil.simNewArchive(this, fileToSave);
    }

    @Test
    @DisplayName("Test: Create new bzip tarball archive successfully from the main window")
    public void testFX_CreateNewBzipArchiveMainWindow_Success() throws IOException {
        final String archiveFormat = "tar.bz2";
        final Path fileToSave = Paths.get(System.getProperty("user.home"), ".pz", "temp", String.format("testbz2.%s",
                                                                                                        archiveFormat));
        PearlZipFXUtil.simNewArchive(this, fileToSave);
    }

    @Test
    @DisplayName("Test: Create new gzip tarball archive successfully from the main window")
    public void testFX_CreateNewGzipArchiveMainWindow_Success() throws IOException {
        final String archiveFormat = "tar.gz";
        final Path fileToSave = Paths.get(System.getProperty("user.home"), ".pz", "temp", String.format("testgz.%s",
                                                                                                        archiveFormat));
        PearlZipFXUtil.simNewArchive(this, fileToSave);
    }

    @Test
    @DisplayName("Test: Create new zip archive successfully from the system menu")
    public void testFX_CreateNewZipArchiveSystemMenu_Success() throws IOException {
        final String archiveFormat = "zip";
        final Path fileToSave = Paths.get(System.getProperty("user.home"), ".pz", "temp", String.format("test%s.%s", archiveFormat, archiveFormat));

        // Hard coded movement to new MenuItem
        clickOn(Point2D.ZERO.add(110,10)).clickOn(Point2D.ZERO.add(110,30));
        PearlZipFXUtil.simNewArchive(this, fileToSave, false);
    }

    @Test
    @DisplayName("Test: Create new jar archive successfully from the system menu")
    public void testFX_CreateNewJarArchiveSystemMenu_Success() throws IOException {
        final String archiveFormat = "jar";
        final Path fileToSave = Paths.get(System.getProperty("user.home"), ".pz", "temp", String.format("test%s.%s", archiveFormat, archiveFormat));

        // Hard coded movement to new MenuItem
        clickOn(Point2D.ZERO.add(110,10)).clickOn(Point2D.ZERO.add(110,30));
        PearlZipFXUtil.simNewArchive(this, fileToSave, false);
    }

    @Test
    @DisplayName("Test: Create new tar archive successfully from the system menu")
    public void testFX_CreateNewTarArchiveSystemMenu_Success() throws IOException {
        final String archiveFormat = "tar";
        final Path fileToSave = Paths.get(System.getProperty("user.home"), ".pz", "temp", String.format("test%s.%s", archiveFormat, archiveFormat));

        // Hard coded movement to new MenuItem
        clickOn(Point2D.ZERO.add(110,10)).clickOn(Point2D.ZERO.add(110,30));
        PearlZipFXUtil.simNewArchive(this, fileToSave, false);
    }

    @Test
    @DisplayName("Test: Create new Gzip archive successfully from the system menu")
    public void testFX_CreateNewGzipArchiveSystemMenu_Success() throws IOException {
        final String archiveFormat = "tar.gz";
        final Path fileToSave = Paths.get(System.getProperty("user.home"), ".pz", "temp", String.format("testGz.%s",
                                                                                                        archiveFormat));

        // Hard coded movement to new MenuItem
        clickOn(Point2D.ZERO.add(110,10)).clickOn(Point2D.ZERO.add(110,30));
        PearlZipFXUtil.simNewArchive(this, fileToSave, false);
    }

    @Test
    @DisplayName("Test: Create new xz archive successfully from the system menu")
    public void testFX_CreateNewXzArchiveSystemMenu_Success() throws IOException {
        final String archiveFormat = "tar.xz";
        final Path fileToSave = Paths.get(System.getProperty("user.home"), ".pz", "temp", String.format("testXz.%s",
                                                                                                        archiveFormat));

        // Hard coded movement to new MenuItem
        clickOn(Point2D.ZERO.add(110,10)).clickOn(Point2D.ZERO.add(110,30));
        PearlZipFXUtil.simNewArchive(this, fileToSave, false);
    }

    @Test
    @DisplayName("Test: Create new Bzip archive successfully from the system menu")
    public void testFX_CreateNewBzipArchiveSystemMenu_Success() throws IOException {
        final String archiveFormat = "tar.bz2";
        final Path fileToSave = Paths.get(System.getProperty("user.home"), ".pz", "temp", String.format("testBz2.%s",
                                                                                                        archiveFormat));

        // Hard coded movement to new MenuItem
        clickOn(Point2D.ZERO.add(110,10)).clickOn(Point2D.ZERO.add(110,30));
        PearlZipFXUtil.simNewArchive(this, fileToSave, false);
    }
}
