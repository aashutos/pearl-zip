/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.testfx;

import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.util.AbstractPearlZipTestFX;
import com.ntak.testfx.FormUtil;
import javafx.geometry.Point2D;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.ntak.pearlzip.ui.constants.ZipConstants.CK_RECENT_FILE;
import static com.ntak.pearlzip.ui.util.PearlZipFXUtil.*;
import static com.ntak.testfx.NativeFileChooserUtil.chooseFile;
import static com.ntak.testfx.TestFXConstants.PLATFORM;

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
    public void testFX_OpenZipArchiveNewWindow_Success() {
        // Hard coded movement to open MenuItem
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 80));
        final Path archivePath = Paths.get("src", "test", "resources", "test.zip")
                                  .toAbsolutePath();
        // Via Sys menu
        simOpenArchive(this, archivePath, false, true);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(),"Expected archive " +
                "was not present");

        // Via main window
        simOpenArchive(this, archivePath, true, true);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertEquals(2,
                                Stage.getWindows().stream().map(Stage.class::cast).filter(s->s.getTitle() != null).filter(s->s.getTitle().contains(archivePath.getFileName().toString())).count(),
                                "The expected number of windows was not open");
    }

    @Test
    @DisplayName("Test: Open jar archive successfully (new window)")
    public void testFX_OpenJarArchiveNewWindow_Success() {
        // Hard coded movement to open MenuItem
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 80));
        final Path archivePath = Paths.get("src", "test", "resources", "test.jar")
                                      .toAbsolutePath();
        // Via Sys menu
        simOpenArchive(this, archivePath, false, true);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(),"Expected archive " +
                "was not present");

        // Via main window
        simOpenArchive(this, archivePath, true, true);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertEquals(2,
                                Stage.getWindows().stream().map(Stage.class::cast).filter(s->s.getTitle() != null).filter(s->s.getTitle().contains(archivePath.getFileName().toString())).count(),
                                "The expected number of windows was not open");
    }

    @Test
    @DisplayName("Test: Open 7z archive successfully (new window)")
    public void testFX_Open7zArchiveNewWindow_Success() {
        // Hard coded movement to open MenuItem
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 80));
        final Path archivePath = Paths.get("src", "test", "resources", "test.7z")
                                      .toAbsolutePath();
        // Via Sys menu
        simOpenArchive(this, archivePath, false, true);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(),"Expected archive " +
                "was not present");

        // Via main window
        simOpenArchive(this, archivePath, true, true);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertEquals(2,
                                Stage.getWindows().stream().map(Stage.class::cast).filter(s->s.getTitle() != null).filter(s->s.getTitle().contains(archivePath.getFileName().toString())).count(),
                                "The expected number of windows was not open");
    }

    @Test
    @DisplayName("Test: Open cab archive successfully (new window)")
    public void testFX_OpenCabArchiveNewWindow_Success() {
        // Hard coded movement to open MenuItem
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 80));
        final Path archivePath = Paths.get("src", "test", "resources", "test.cab")
                                      .toAbsolutePath();
        // Via Sys menu
        simOpenArchive(this, archivePath, false, true);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(),"Expected archive " +
                "was not present");

        // Via main window
        simOpenArchive(this, archivePath, true, true);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertEquals(2,
                                Stage.getWindows().stream().map(Stage.class::cast).filter(s->s.getTitle() != null).filter(s->s.getTitle().contains(archivePath.getFileName().toString())).count(),
                                "The expected number of windows was not open");
    }

    @Test
    @DisplayName("Test: Open iso archive successfully (new window)")
    public void testFX_OpenIsoArchiveNewWindow_Success() {
        // Hard coded movement to open MenuItem
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 80));
        final Path archivePath = Paths.get("src", "test", "resources", "test.iso")
                                      .toAbsolutePath();
        // Via Sys menu
        simOpenArchive(this, archivePath, false, true);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(),"Expected archive " +
                "was not present");

        // Via main window
        simOpenArchive(this, archivePath, true, true);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertEquals(2,
                                Stage.getWindows().stream().map(Stage.class::cast).filter(s->s.getTitle() != null).filter(s->s.getTitle().contains(archivePath.getFileName().toString())).count(),
                                "The expected number of windows was not open");
    }

    @Test
    @DisplayName("Test: Open rar archive successfully (new window)")
    public void testFX_OpenRarArchiveNewWindow_Success() {
        // Hard coded movement to open MenuItem
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 80));
        final Path archivePath = Paths.get("src", "test", "resources", "test.iso")
                                      .toAbsolutePath();
        // Via Sys menu
        simOpenArchive(this, archivePath, false, true);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(),"Expected archive " +
                "was not present");

        // Via main window
        simOpenArchive(this, archivePath, true, true);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertEquals(2,
                                Stage.getWindows().stream().map(Stage.class::cast).filter(s->s.getTitle() != null).filter(s->s.getTitle().contains(archivePath.getFileName().toString())).count(),
                                "The expected number of windows was not open");
    }

    @Test
    @DisplayName("Test: Open tar archive successfully (new window)")
    public void testFX_OpenTarArchiveNewWindow_Success() {
        // Hard coded movement to open MenuItem
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 80));
        final Path archivePath = Paths.get("src", "test", "resources", "test.tar")
                                      .toAbsolutePath();
        // Via Sys menu
        simOpenArchive(this, archivePath, false, true);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(),"Expected archive " +
                "was not present");

        // Via main window
        simOpenArchive(this, archivePath, true, true);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertEquals(2,
                                Stage.getWindows().stream().map(Stage.class::cast).filter(s->s.getTitle() != null).filter(s->s.getTitle().contains(archivePath.getFileName().toString())).count(),
                                "The expected number of windows was not open");
    }

    @Test
    @DisplayName("Test: Open BZip archive successfully (new window)")
    public void testFX_OpenBzipArchiveNewWindow_Success() {
        // Hard coded movement to open MenuItem
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 80));
        final Path archivePath = Paths.get("src", "test", "resources", "test.tar.bz2")
                                      .toAbsolutePath();
        // Via Sys menu
        simOpenArchive(this, archivePath, false, true);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(),"Expected archive " +
                "was not present");

        // Via main window
        simOpenArchive(this, archivePath, true, true);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertEquals(2,
                                Stage.getWindows().stream().map(Stage.class::cast).filter(s->s.getTitle() != null).filter(s->s.getTitle().contains(archivePath.getFileName().toString())).count(),
                                "The expected number of windows was not open");
    }

    @Test
    @DisplayName("Test: Open GZip archive successfully (new window)")
    public void testFX_OpenGzipArchiveNewWindow_Success() {
        // Hard coded movement to open MenuItem
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 80));
        final Path archivePath = Paths.get("src", "test", "resources", "test.tar.gz")
                                      .toAbsolutePath();
        // Via Sys menu
        simOpenArchive(this, archivePath, false, true);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(),"Expected archive " +
                "was not present");

        // Via main window
        simOpenArchive(this, archivePath, true, true);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertEquals(2,
                                Stage.getWindows().stream().map(Stage.class::cast).filter(s->s.getTitle() != null).filter(s->s.getTitle().contains(archivePath.getFileName().toString())).count(),
                                "The expected number of windows was not open");
    }

    @Test
    @DisplayName("Test: Open xz archive successfully (new window)")
    public void testFX_OpenXZArchiveNewWindow_Success() {
        // Hard coded movement to open MenuItem
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 80));
        final Path archivePath = Paths.get("src", "test", "resources", "test.tar.xz")
                                      .toAbsolutePath();
        // Via Sys menu
        simOpenArchive(this, archivePath, false, true);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(),"Expected archive " +
                "was not present");

        // Via main window
        simOpenArchive(this, archivePath, true, true);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertEquals(2,
                                Stage.getWindows().stream().map(Stage.class::cast).filter(s->s.getTitle() != null).filter(s->s.getTitle().contains(archivePath.getFileName().toString())).count(),
                                "The expected number of windows was not open");
    }

    ////////////////////////////////////////////////////////////////////////////////
    ////////// CURRENT WINDOW //////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    @Test
    @DisplayName("Test: Open zip archive successfully (current window)")
    public void testFX_OpenZipArchiveCurrentWindow_Success() {
        // Hard coded movement to open MenuItem
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 80));
        final Path archivePath = Paths.get("src", "test", "resources", "test.zip")
                                      .toAbsolutePath();
        // Via Sys menu
        simOpenArchive(this, archivePath, false, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(),"Expected archive " +
                "was not present");

        // Via main window
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertEquals(1,
                                Stage.getWindows().stream().map(Stage.class::cast).filter(s->s.getTitle() != null).filter(s->s.getTitle().contains(archivePath.getFileName().toString())).count(), "The " +
                                        "expected " +
                                        "number of windows was " +
                                        "not open");
    }

    @Test
    @DisplayName("Test: Open jar archive successfully (current window)")
    public void testFX_OpenJarArchiveCurrentWindow_Success() {
        // Hard coded movement to open MenuItem
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 80));
        final Path archivePath = Paths.get("src", "test", "resources", "test.jar")
                                      .toAbsolutePath();
        // Via Sys menu
        simOpenArchive(this, archivePath, false, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(),"Expected archive " +
                "was not present");

        // Via main window
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertEquals(1,
                                Stage.getWindows().stream().map(Stage.class::cast).filter(s->s.getTitle() != null).filter(s->s.getTitle().contains(archivePath.getFileName().toString())).count(),
                                "The expected number of windows was not open");
    }

    @Test
    @DisplayName("Test: Open 7z archive successfully (current window)")
    public void testFX_Open7zArchiveCurrentWindow_Success() {
        // Hard coded movement to open MenuItem
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 80));
        final Path archivePath = Paths.get("src", "test", "resources", "test.7z")
                                      .toAbsolutePath();
        // Via Sys menu
        simOpenArchive(this, archivePath, false, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(),"Expected archive " +
                "was not present");

        // Via main window
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertEquals(1,
                                Stage.getWindows().stream().map(Stage.class::cast).filter(s->s.getTitle() != null).filter(s->s.getTitle().contains(archivePath.getFileName().toString())).count(),
                                "The expected number of windows was not open");
    }

    @Test
    @DisplayName("Test: Open cab archive successfully (current window)")
    public void testFX_OpenCabArchiveCurrentWindow_Success() {
        // Hard coded movement to open MenuItem
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 80));
        final Path archivePath = Paths.get("src", "test", "resources", "test.cab")
                                      .toAbsolutePath();
        // Via Sys menu
        simOpenArchive(this, archivePath, false, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(),"Expected archive " +
                "was not present");

        // Via main window
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertEquals(1,
                                Stage.getWindows().stream().map(Stage.class::cast).filter(s->s.getTitle() != null).filter(s->s.getTitle().contains(archivePath.getFileName().toString())).count(),
                                "The expected number of windows was not open");
    }

    @Test
    @DisplayName("Test: Open iso archive successfully (current window)")
    public void testFX_OpenIsoArchiveCurrentWindow_Success() {
        // Hard coded movement to open MenuItem
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 80));
        final Path archivePath = Paths.get("src", "test", "resources", "test.iso")
                                      .toAbsolutePath();
        // Via Sys menu
        simOpenArchive(this, archivePath, false, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(),"Expected archive " +
                "was not present");

        // Via main window
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertEquals(1,
                                Stage.getWindows().stream().map(Stage.class::cast).filter(s->s.getTitle() != null).filter(s->s.getTitle().contains(archivePath.getFileName().toString())).count(),
                                "The expected number of windows was not open");
    }

    @Test
    @DisplayName("Test: Open rar archive successfully (current window)")
    public void testFX_OpenRarArchiveCurrentWindow_Success() {
        // Hard coded movement to open MenuItem
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 80));
        final Path archivePath = Paths.get("src", "test", "resources", "test.iso")
                                      .toAbsolutePath();
        // Via Sys menu
        simOpenArchive(this, archivePath, false, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(),"Expected archive " +
                "was not present");

        // Via main window
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertEquals(1,
                                Stage.getWindows().stream().map(Stage.class::cast).filter(s->s.getTitle() != null).filter(s->s.getTitle().contains(archivePath.getFileName().toString())).count(),
                                "The expected number of windows was not open");
    }

    @Test
    @DisplayName("Test: Open tar archive successfully (current window)")
    public void testFX_OpenTarArchiveCurrentWindow_Success() {
        // Hard coded movement to open MenuItem
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 80));
        final Path archivePath = Paths.get("src", "test", "resources", "test.tar")
                                      .toAbsolutePath();
        // Via Sys menu
        simOpenArchive(this, archivePath, false, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(),"Expected archive " +
                "was not present");

        // Via main window
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertEquals(1,
                                Stage.getWindows().stream().map(Stage.class::cast).filter(s->s.getTitle() != null).filter(s->s.getTitle().contains(archivePath.getFileName().toString())).count(),
                                "The expected number of windows was not open");
    }

    @Test
    @DisplayName("Test: Open BZip archive successfully (current window)")
    public void testFX_OpenBzipArchiveCurrentWindow_Success() {
        // Hard coded movement to open MenuItem
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 80));
        final Path archivePath = Paths.get("src", "test", "resources", "test.tar.bz2")
                                      .toAbsolutePath();
        // Via Sys menu
        simOpenArchive(this, archivePath, false, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(),"Expected archive " +
                "was not present");

        // Via main window
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertEquals(1,
                                Stage.getWindows().stream().map(Stage.class::cast).filter(s->s.getTitle() != null).filter(s->s.getTitle().contains(archivePath.getFileName().toString())).count(),
                                "The expected number of windows was not open");
    }

    @Test
    @DisplayName("Test: Open GZip archive successfully (current window)")
    public void testFX_OpenGzipArchiveCurrentWindow_Success() {
        // Hard coded movement to open MenuItem
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 80));
        final Path archivePath = Paths.get("src", "test", "resources", "test.tar.gz")
                                      .toAbsolutePath();
        // Via Sys menu
        simOpenArchive(this, archivePath, false, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(),"Expected archive " +
                "was not present");

        // Via main window
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertEquals(1,
                                Stage.getWindows().stream().map(Stage.class::cast).filter(s->s.getTitle() != null).filter(s->s.getTitle().contains(archivePath.getFileName().toString())).count(),
                                "The expected number of windows was not open");
    }

    @Test
    @DisplayName("Test: Open xz archive successfully (current window)")
    public void testFX_OpenXZArchiveCurrentWindow_Success() {
        // Hard coded movement to open MenuItem
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 80));
        final Path archivePath = Paths.get("src", "test", "resources", "test.tar.xz")
                                      .toAbsolutePath();
        // Via Sys menu
        simOpenArchive(this, archivePath, false, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(),"Expected archive " +
                "was not present");

        // Via main window
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertEquals(1,
                                Stage.getWindows().stream().map(Stage.class::cast).filter(s->s.getTitle() != null).filter(s->s.getTitle().contains(archivePath.getFileName().toString())).count(),
                                "The expected number of windows was not open");
    }

    @Test
    @DisplayName("Test: Opening the 5 files will update the recent files menu appropriately with new entries")
    public void testFX_OpenRecentFilesFirst5Files_MatchExpectations() throws IOException {
        final Path RECENT_FILE = InternalContextCache.GLOBAL_CONFIGURATION_CACHE.<Path>getAdditionalConfig(CK_RECENT_FILE)
                                                                         .get();
        Files.deleteIfExists(RECENT_FILE);
        int count = 0;
        String[] extensions = {"zip","jar","tar","iso","cab"};
        for (String extension : extensions) {
            // Hard coded movement to open MenuItem
            clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 80));
            final Path archivePath = Paths.get("src", "test", "resources", String.format("test.%s", extension))
                                          .toAbsolutePath();
            // Via Sys menu
            simOpenArchive(this, archivePath, false, false);
            sleep(50, TimeUnit.MILLISECONDS);
            count++;

            Assertions.assertEquals(count, Files.lines(RECENT_FILE).count(),
                                    "The number of entries in the rf file was not as expected");
            Assertions.assertTrue(Files.lines(RECENT_FILE).anyMatch(f-> f.contains(Paths.get("src",
                                                                                             "test",
                                                                                             "resources", String.format("test.%s", extension))
                                                                                      .toAbsolutePath().toString())),
                                  "The expected file was not found in list");
        }
    }

    @Test
    @DisplayName("Test: Opening the 6th file will update the recent files menu by overwriting the oldest entry")
    public void testFX_OpenRecentFilesSixthFileOverwrite_MatchExpectations() throws IOException {
        Path RECENT_FILE =
                InternalContextCache.GLOBAL_CONFIGURATION_CACHE.<Path>getAdditionalConfig(CK_RECENT_FILE).get();
        Files.deleteIfExists(RECENT_FILE);
        int count = 0;
        String[] extensions = {"zip","jar","tar","iso","cab","tar.gz"};
        for (String extension : extensions) {
            // Hard coded movement to open MenuItem
            clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 80));
            final Path archivePath = Paths.get("src", "test", "resources", String.format("test.%s", extension))
                                          .toAbsolutePath();
            // Via Sys menu
            simOpenArchive(this, archivePath, false, false);
            sleep(50, TimeUnit.MILLISECONDS);
            count++;

            Assertions.assertEquals(Math.min(5,count), Files.lines(RECENT_FILE).count(),
                                    "The number of entries in the rf file was not as expected");
            Assertions.assertTrue(Files.lines(RECENT_FILE).anyMatch(f-> f.contains(Paths.get("src", "test", "resources", String.format("test.%s", extension))
                                                                                                     .toAbsolutePath().toString())),
                                  "The expected file was not found in list");
        }
        Assertions.assertTrue(Files.lines(RECENT_FILE).noneMatch(f-> f.contains(Paths.get("src", "test", "resources",
                                                                                                       "test.zip")
                                                                                                 .toAbsolutePath().toString())),
                              "The file was found in list unexpectedly");
    }

    @Test
    @DisplayName("Test: Open file in archive using OS defined software successfully")
    public void testFX_OpenFileInArchiveExternally_Success() {
        // Hard coded movement to open MenuItem
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 80));
        final Path archivePath = Paths.get("src", "test", "resources", "test.zip")
                                      .toAbsolutePath();
        // Via Sys menu
        simOpenArchive(this, archivePath, false, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(),"Expected archive " +
                "was not present");

        TableView<FileInfo> fileContentsView = lookup("#fileContentsView").queryAs(TableView.class);
        TableRow<FileInfo> row = FormUtil.selectTableViewEntry(this, fileContentsView, FileInfo::getFileName,
                                                               "first-file").get();
        doubleClickOn(row);
        DialogPane dialogPane = lookup(".dialog-pane").query();
        Assertions.assertTrue(dialogPane.getContentText().startsWith("Choosing yes will open a temporary copy of the selected file in an external application"),
                              "The text in confirmation dialog was not matched as expected");
    }

    ////////////////////////////////////////////////////////////////////////////////
    ////////// OTHER TEST CASES ////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    @Test
    @DisplayName("Test: Open zip archive and check ordering functionality of items in main screen")
    public void testFX_OpenZipArchiveCheckOrdering_Success() {
        // Hard coded movement to open MenuItem
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 80));
        final Path archivePath = Paths.get("src", "test", "resources", "order-test.zip")
                                      .toAbsolutePath();
        // Via Sys menu
        simOpenArchive(this, archivePath, false, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(),"Expected archive " +
                "was not present");

        // Select Name Header and check item order
        TableView<FileInfo> fileContentsView = lookup("#fileContentsView").queryAs(TableView.class);
        clickOn(new Point2D(350,225), MouseButton.PRIMARY).sleep(500, TimeUnit.MILLISECONDS);

        List<String> expectations = new LinkedList<>(List.of("FILE-1", "FILE-1.md5", "nested-dir"));
        List<FileInfo> files = new ArrayList<>(fileContentsView.getItems());
        Assertions.assertEquals(expectations.size(), files.size(), "The expected number of files was not retrieved");

        for (int i = 0; i < expectations.size(); i++) {
            Assertions.assertEquals(expectations.get(i), files.get(i).getFileName(),
                                    String.format("Expectation: %s does not match: %s", expectations.get(i), files.get(i).getFileName()));
        }

        clickOn(new Point2D(350,225), MouseButton.PRIMARY).sleep(500, TimeUnit.MILLISECONDS);
        Collections.reverse(expectations);
        files = new ArrayList<>(fileContentsView.getItems());
        Assertions.assertEquals(expectations.size(), files.size(), "The expected number of files was not retrieved");

        for (int i = 0; i < expectations.size(); i++) {
            Assertions.assertEquals(expectations.get(i), files.get(i).getFileName(),
                                    String.format("Expectation: %s does not match: %s", expectations.get(i), files.get(i).getFileName()));
        }

        simTraversalArchive(this, archivePath.toString(), "#fileContentsView", (r)->{}, "nested-dir",".DS_Store");
        sleep(250,TimeUnit.MILLISECONDS);

        // Select Size and check item order
        clickOn(new Point2D(525,225), MouseButton.PRIMARY).sleep(500, TimeUnit.MILLISECONDS);
        expectations = new LinkedList<>(List.of("nested-dir/FILE-2", "nested-dir/FILE-3.md5", "nested-dir/FILE-3", "nested-dir/.DS_Store"));
        files = new ArrayList<>(fileContentsView.getItems());
        Assertions.assertEquals(expectations.size(), files.size(), "The expected number of files was not retrieved");

        for (int i = 0; i < expectations.size(); i++) {
            Assertions.assertEquals(expectations.get(i), files.get(i).getFileName(),
                                    String.format("Expectation: %s does not match: %s", expectations.get(i), files.get(i).getFileName()));
        }

        clickOn(new Point2D(525,225), MouseButton.PRIMARY).sleep(500, TimeUnit.MILLISECONDS);
        Collections.reverse(expectations);
        files = new ArrayList<>(fileContentsView.getItems());
        Assertions.assertEquals(expectations.size(), files.size(), "The expected number of files was not retrieved");

        for (int i = 0; i < expectations.size(); i++) {
            Assertions.assertEquals(expectations.get(i), files.get(i).getFileName(),
                                    String.format("Expectation: %s does not match: %s", expectations.get(i), files.get(i).getFileName()));
        }

        // Select Modified timestamp and check item order
        clickOn(new Point2D(800,225), MouseButton.PRIMARY).sleep(500, TimeUnit.MILLISECONDS);
        expectations = new LinkedList<>(List.of("nested-dir/.DS_Store", "nested-dir/FILE-2", "nested-dir/FILE-3",
                                                "nested-dir/FILE-3.md5"));
        files = new ArrayList<>(fileContentsView.getItems());
        Assertions.assertEquals(expectations.size(), files.size(), "The expected number of files was not retrieved");

        for (int i = 0; i < expectations.size(); i++) {
            Assertions.assertEquals(expectations.get(i), files.get(i).getFileName(),
                                    String.format("Expectation: %s does not match: %s", expectations.get(i), files.get(i).getFileName()));
        }

        clickOn(new Point2D(800,225), MouseButton.PRIMARY).sleep(500, TimeUnit.MILLISECONDS);
        Collections.reverse(expectations);
        files = new ArrayList<>(fileContentsView.getItems());
        Assertions.assertEquals(expectations.size(), files.size(), "The expected number of files was not retrieved");

        for (int i = 0; i < expectations.size(); i++) {
            Assertions.assertEquals(expectations.get(i), files.get(i).getFileName(),
                                    String.format("Expectation: %s does not match: %s", expectations.get(i), files.get(i).getFileName()));
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    ////////// SAVE OPENED ARCHIVE /////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    @Test
    @DisplayName("Test: Save opened archive as another file with no extension will append extension automatically")
    public void testFX_SaveOpenArchiveAs_NoExtension_Success() throws IOException {
        // Hard coded movement to open MenuItem
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 80));
        final Path archivePath = Paths.get("src", "test", "resources", "test.zip")
                                      .toAbsolutePath();
        // Via Sys menu
        simOpenArchive(this, archivePath, false, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(),"Expected archive " +
                "was not present");

        // Save as...
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 140));
        final Path tempDirectory = Files.createTempDirectory("pz");
        chooseFile(PLATFORM, this,
                   tempDirectory.resolve(archivePath.getFileName().toString().replace(".zip", "")));
        Assertions.assertTrue(Files.exists(tempDirectory.resolve(archivePath.getFileName())),
                              "Target archive was not created with an extension");
    }
}
