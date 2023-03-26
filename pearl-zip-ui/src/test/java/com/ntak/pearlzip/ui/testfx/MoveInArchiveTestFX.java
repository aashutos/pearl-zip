/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.testfx;

import com.ntak.pearlzip.ui.UITestSuite;
import com.ntak.pearlzip.ui.util.AbstractPearlZipTestFX;
import com.ntak.pearlzip.ui.util.PearlZipSpecifications;
import com.ntak.testfx.specifications.CommonSpecifications;
import javafx.scene.control.MenuButton;
import javafx.scene.input.MouseButton;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.ui.UITestFXSuite.genSourceDataSet;
import static com.ntak.pearlzip.ui.constants.ResourceConstants.SSV;
import static com.ntak.pearlzip.ui.util.PearlZipFXUtil.*;
import static com.ntak.testfx.FormUtil.lookupNode;
import static com.ntak.testfx.TestFXConstants.SHORT_PAUSE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Tag("fx-test")
public class MoveInArchiveTestFX extends AbstractPearlZipTestFX {

    private static Path dir;
    private static Path emptyDir;

   /*
    *  Test cases:
    *  + Move up button and menu
    *  + Move down button and menu
    *  + Move disabled for compressor archives
    *  + Move cancel
    *  + Move no file selected
    *  + Move folder fail
    *  + Table context menu drop
    *  + Move into empty folder
    *  + Move from within a non-existent archive
    */

    @BeforeEach
    public void setUp() throws IOException {
        dir = genSourceDataSet();
        emptyDir = Paths.get(dir.toAbsolutePath().getParent().toString(), "empty-dir");
        Files.createDirectories(emptyDir);
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
        for (Path dir :
             Files.list(dir.getParent().getParent()).filter(p->p.getFileName().toString().startsWith("pz")).collect(
             Collectors.toList())) {
            UITestSuite.clearDirectory(dir);
        }
    }

    @Test
    @DisplayName("Test: Move file down by context menu and button in application within a zip archive successfully")
    // GIVEN a copy of zip archive (test.zip) is open in PearlZip
    // WHEN folder added to archive (root)
    //     AND Move file (root/level1c/MOVE_DOWN.txt) to (root/level1c/level1c1/MOVE_DOWN.txt) not using context menu
    //     AND Move file (root/level1c/level1c1/MOVE_DOWN.txt) to (root/level1c/level1c1/level2b/MOVE_DOWN.txt) using context menu
    // THEN ensure files (root/level1c/level1c1/level2b/MOVE_DOWN.txt) is included in the archive at depth (4) respectively
    //     AND ensure files (root/level1c/MOVE_DOWN.txt, root/level1c/level1c1/MOVE_DOWN.txt) is NOT included in the archive at depth (2,3) respectively
    public void testFX_moveFileDownByMenuAndButtonZip_MatchExpectations() throws IOException {
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
        simAddFolder(this, dir);
        PearlZipSpecifications.whenFileMovedWithinArchive(this, archivePath, Paths.get("root", "level1c", "MOVE_DOWN.txt"), Paths.get("root","level1c","level1c1","MOVE_DOWN.txt"), false);
        PearlZipSpecifications.whenFileMovedWithinArchive(this, archivePath, Paths.get("root","level1c","level1c1","MOVE_DOWN.txt"), Paths.get("root","level1c","level1c1", "level2b", "MOVE_DOWN.txt"), true);

        // Then
        PearlZipSpecifications.thenExpectFileExistsInArchive(archivePath, 4, "root/level1c/level1c1/level2b/MOVE_DOWN.txt");
    }

    @Test
    @DisplayName("Test: Move file down by context menu and button in application within a tar archive successfully")
    // GIVEN a copy of tar archive (test.tar) is open in PearlZip
    // WHEN folder added to archive (root)
    //     AND Move file (root/level1c/MOVE_DOWN.txt) to (root/level1c/level1c1/MOVE_DOWN.txt) using context menu
    //     AND Move file (root/level1c/level1c1/MOVE_DOWN.txt) to (root/level1c/level1c1/level2b/MOVE_DOWN.txt) using context menu
    // THEN ensure files (root/level1c/level1c1/level2b/MOVE_DOWN.txt) is included in the archive at depth (4) respectively
    //     AND ensure files (root/level1c/MOVE_DOWN.txt, root/level1c/level1c1/MOVE_DOWN.txt) is NOT included in the archive at depth (2,3) respectively
    public void testFX_moveFileDownByMenuAndButtonTar_MatchExpectations() throws IOException {
        // Given
        final Path srcPath = Paths.get("src", "test", "resources", "test.tar")
                                  .toAbsolutePath();
        final Path archivePath = Paths.get(Files.createTempDirectory("pz").toAbsolutePath().toString(), "test.tar")
                                      .toAbsolutePath();
        Files.copy(srcPath, archivePath);

        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // When
        simAddFolder(this, dir);
        PearlZipSpecifications.whenFileMovedWithinArchive(this, archivePath, Paths.get("root", "level1c", "MOVE_DOWN.txt"), Paths.get("root","level1c","level1c1","MOVE_DOWN.txt"), false);
        PearlZipSpecifications.whenFileMovedWithinArchive(this, archivePath, Paths.get("root","level1c","level1c1","MOVE_DOWN.txt"), Paths.get("root","level1c","level1c1", "level2b", "MOVE_DOWN.txt"), true);

        // Then
        PearlZipSpecifications.thenExpectFileExistsInArchive(archivePath, 4, "root/level1c/level1c1/level2b/MOVE_DOWN.txt");
    }

    @Test
    @DisplayName("Test: Move file down by context menu and button in application within a jar archive successfully")
    // GIVEN a copy of jar archive (test.jar) is open in PearlZip
    // WHEN folder added to archive (root)
    //     AND Move file (root/level1c/MOVE_DOWN.txt) to (root/level1c/level1c1/MOVE_DOWN.txt) using context menu
    //     AND Move file (root/level1c/level1c1/MOVE_DOWN.txt) to (root/level1c/level1c1/level2b/MOVE_DOWN.txt) using context menu
    // THEN ensure files (root/level1c/level1c1/level2b/MOVE_DOWN.txt) is included in the archive at depth (4) respectively
    //     AND ensure files (root/level1c/MOVE_DOWN.txt, root/level1c/level1c1/MOVE_DOWN.txt) is NOT included in the archive at depth (2,3) respectively
    public void testFX_moveFileDownByMenuAndButtonJar_MatchExpectations() throws IOException {
        // Given
        final Path srcPath = Paths.get("src", "test", "resources", "test.jar")
                                  .toAbsolutePath();
        final Path archivePath = Paths.get(Files.createTempDirectory("pz").toAbsolutePath().toString(), "test.jar")
                                      .toAbsolutePath();
        Files.copy(srcPath, archivePath);

        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // When
        simAddFolder(this, dir);
        PearlZipSpecifications.whenFileMovedWithinArchive(this, archivePath, Paths.get("root", "level1c", "MOVE_DOWN.txt"), Paths.get("root","level1c","level1c1","MOVE_DOWN.txt"), false);
        PearlZipSpecifications.whenFileMovedWithinArchive(this, archivePath, Paths.get("root","level1c","level1c1","MOVE_DOWN.txt"), Paths.get("root","level1c","level1c1", "level2b", "MOVE_DOWN.txt"), true);

        // Then
        PearlZipSpecifications.thenExpectFileExistsInArchive(archivePath, 4, "root/level1c/level1c1/level2b/MOVE_DOWN.txt");
    }

    @Test
    @DisplayName("Test: Move file up by main dialog buttons in application within a zip archive successfully")
    // GIVEN a copy of zip archive (test.zip) is open in PearlZip
    // WHEN folder added to archive (root)
    //     AND Move file (root/level1c/level1c1/level2b/MOVE_UP.txt) to (root/level1c/level1c1/MOVE_UP.txt) not using context menu
    // THEN ensure files (root/level1c/level1c1/MOVE_UP.txt) is included in the archive at depth (3) respectively
    //     AND ensure files (root/level1c/level1c1/level2b/MOVE_UP.txt) is NOT included in the archive at depth (4) respectively
    public void testFX_moveFileByTableContextMenuAndButtonZip_MatchExpectations() throws IOException {
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
        simAddFolder(this, dir);
        PearlZipSpecifications.whenFileMovedWithinArchive(this, archivePath, Paths.get("root", "level1c", "level1c1", "level2b", "MOVE_UP.txt"), Paths.get("root", "level1c", "level1c1", "MOVE_UP.txt"), false);

        // Then
        PearlZipSpecifications.thenExpectFileExistsInArchive(archivePath, 3, "root/level1c/level1c1/MOVE_UP.txt");
        PearlZipSpecifications.thenExpectFileNotExistsInArchive(archivePath, 3, "root/level1c/level1c1/level2b/MOVE_UP.txt");
    }

    @Test
    @DisplayName("Test: Move file up by context menu and button in application within a zip archive successfully")
    // GIVEN a copy of zip archive (test.zip) is open in PearlZip
    // WHEN folder added to archive (root)
    //     AND Move file (root/level1c/level1c1/level2b/MOVE_UP.txt) to (root/MOVE_UP.txt) using context menu
    // THEN ensure files (root/MOVE_UP.txt) is included in the archive at depth (1) respectively
    //     AND ensure files (root/level1c/level1c1/level2b/MOVE_UP.txt) is NOT included in the archive at depth (4) respectively
    public void testFX_moveFileUpByMenuAndButtonZip_MatchExpectations() throws IOException {
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
        simAddFolder(this, dir);
        PearlZipSpecifications.whenFileMovedWithinArchive(this, archivePath, Paths.get("root", "level1c", "level1c1", "level2b", "MOVE_UP.txt"), Paths.get("root", "level1c", "level1c1", "MOVE_UP.txt"), true);

        // Then
        PearlZipSpecifications.thenExpectFileExistsInArchive(archivePath, 3, "root/level1c/level1c1/MOVE_UP.txt");
        PearlZipSpecifications.thenExpectFileNotExistsInArchive(archivePath, 3, "root/level1c/level1c1/level2b/MOVE_UP.txt");
    }

    @Test
    @DisplayName("Test: Move file up by context menu and button in application within a tar archive successfully")
    // GIVEN a copy of tar archive (test.tar) is open in PearlZip
    // WHEN folder added to archive (root)
    //     AND Move file (root/level1c/level1c1/level2b/MOVE_UP.txt) to (root/MOVE_UP.txt) not using context menu
    // THEN ensure files (root/MOVE_UP.txt) is included in the archive at depth (1) respectively
    //     AND ensure files (root/level1c/level1c1/level2b/MOVE_UP.txt) is NOT included in the archive at depth (4) respectively
    public void testFX_moveFileUpByMenuAndButtonTar_MatchExpectations() throws IOException {
        // Given
        final Path srcPath = Paths.get("src", "test", "resources", "test.tar")
                                  .toAbsolutePath();
        final Path archivePath = Paths.get(Files.createTempDirectory("pz").toAbsolutePath().toString(), "test.tar")
                                      .toAbsolutePath();
        Files.copy(srcPath, archivePath);

        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // When
        simAddFolder(this, dir);
        PearlZipSpecifications.whenFileMovedWithinArchive(this, archivePath, Paths.get("root", "level1c", "level1c1", "level2b", "MOVE_UP.txt"), Paths.get("root", "level1c", "level1c1", "MOVE_UP.txt"), true);

        // Then
        PearlZipSpecifications.thenExpectFileExistsInArchive(archivePath, 3, "root/level1c/level1c1/MOVE_UP.txt");
        PearlZipSpecifications.thenExpectFileNotExistsInArchive(archivePath, 3, "root/level1c/level1c1/level2b/MOVE_UP.txt");
    }

    @Test
    @DisplayName("Test: Move file up by context menu and button in application within a jar archive successfully")
    // GIVEN a copy of jar archive (test.jar) is open in PearlZip
    // WHEN folder added to archive (root)
    //     AND Move file (root/level1c/level1c1/level2b/MOVE_UP.txt) to (root/MOVE_UP.txt) not using context menu
    // THEN ensure files (root/MOVE_UP.txt) is included in the archive at depth (1) respectively
    //     AND ensure files (root/level1c/level1c1/level2b/MOVE_UP.txt) is NOT included in the archive at depth (4) respectively
    public void testFX_moveFileUpByMenuAndButtonJar_MatchExpectations() throws IOException {
        // Given
        final Path srcPath = Paths.get("src", "test", "resources", "test.jar")
                                  .toAbsolutePath();
        final Path archivePath = Paths.get(Files.createTempDirectory("pz").toAbsolutePath().toString(), "test.jar")
                                      .toAbsolutePath();
        Files.copy(srcPath, archivePath);

        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // When
        simAddFolder(this, dir);
        PearlZipSpecifications.whenFileMovedWithinArchive(this, archivePath, Paths.get("root", "level1c", "level1c1", "level2b", "MOVE_UP.txt"), Paths.get("root", "level1c", "level1c1", "MOVE_UP.txt"), true);

        // Then
        PearlZipSpecifications.thenExpectFileExistsInArchive(archivePath, 3, "root/level1c/level1c1/MOVE_UP.txt");
        PearlZipSpecifications.thenExpectFileNotExistsInArchive(archivePath, 3, "root/level1c/level1c1/level2b/MOVE_UP.txt");
    }

    @Test
    @DisplayName("Test: Move file in application within a Gzip archive is blocked (Single file compressor)")
    // GIVEN a new tar.gz archive has been created in PearlZip
    // WHEN #btnMove clicked on
    // THEN ensure move button is disabled
    public void testFX_moveFileUpByMenuGzip_Blocked() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "gz", "test.tar");

        // When
        clickOn("#btnMove", MouseButton.PRIMARY);
        sleep(SHORT_PAUSE, MILLISECONDS);

        // Then
        MenuButton btnCopy = lookupNode(s->s.getTitle().contains(archive.toString()), "#btnMove");
        Assertions.assertTrue(btnCopy.isDisable(), "Move is not disabled for compressor archives");
    }

    @Test
    @DisplayName("Test: Move file in application within a Bzip archive is blocked (Single file compressor)")
    // GIVEN a new tar.bz2 archive has been created in PearlZip
    // WHEN #btnMove clicked on
    // THEN ensure move button is disabled
    public void testFX_moveFileUpByMenuBzip_Blocked() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "bz2", "test.tar");

        // When
        clickOn("#btnMove", MouseButton.PRIMARY);
        sleep(SHORT_PAUSE, MILLISECONDS);

        // Then
        MenuButton btnCopy = lookupNode(s->s.getTitle().contains(archive.toString()), "#btnMove");
        Assertions.assertTrue(btnCopy.isDisable(), "Move is not disabled for compressor archives");
    }

    @Test
    @DisplayName("Test: Move file in application within a xz archive is blocked (Single file compressor)")
    // GIVEN a new tar.xz archive has been created in PearlZip
    // WHEN #btnMove clicked on
    // THEN ensure move button is disabled
    public void testFX_moveFileUpByMenuXz_Blocked() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "xz", "test.tar");

        // When
        clickOn("#btnMove", MouseButton.PRIMARY);
        sleep(SHORT_PAUSE, MILLISECONDS);

        // Then
        MenuButton btnCopy = lookupNode(s->s.getTitle().contains(archive.toString()), "#btnMove");
        Assertions.assertTrue(btnCopy.isDisable(), "Move is not disabled for compressor archives");
    }

    @Test
    @DisplayName("Test: Move file and cancel")
    // GIVEN a new jar archive has been created in PearlZip
    // WHEN folder added to archive (root)
    //     AND select file (root/level1c/level1c1/level2b/MOVE_UP.txt)
    //     AND Click Move file
    //     AND Cancel Move process
    // THEN ensure files (root/level1c/level1c1/level2b/MOVE_UP.txt) is included in the archive at depth (4) respectively
    public void testFX_moveFileCancel() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "jar");

        // When
        simAddFolder(this, dir);

        Path file = Paths.get("root", "level1c", "level1c1", "level2b", "MOVE_UP.txt");
        final String tableName = "#fileContentsView";
        simTraversalArchive(this, archive.toString(), tableName, (r)->{}, SSV.split(file.toString()));
        clickOn("#btnMove");
        sleep(5, MILLISECONDS);
        clickOn("#mnuMoveSelected");
        sleep(5, MILLISECONDS);
        clickOn("#btnMove");
        sleep(5, MILLISECONDS);
        clickOn("#mnuCancelMove");

        // Then
        PearlZipSpecifications.thenExpectNumberOfFileMatchingPattern(archive, 1, ".*MOVE_UP.txt");
    }

    @Test
    @DisplayName("Test: Move with no file selected raises alert")
    // GIVEN a new jar archive has been created in PearlZip
    // WHEN folder added to archive (root)
    //     AND Click Move file
    // THEN a dialog appears with message like 'Move could not be initiated'
    public void testFX_moveNoFileSelected_Alert() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "jar");

        // When
        simAddFolder(this, dir);

        clickOn("#btnMove");
        sleep(5, MILLISECONDS);
        clickOn("#mnuMoveSelected");

        // Then
        CommonSpecifications.thenExpectDialogWithMatchingMessage(this, "^Move could not be initiated.*");
    }

    @Test
    @DisplayName("Test: Move on folder raises alert")
    // GIVEN a new jar archive has been created in PearlZip
    // WHEN folder added to archive (root)
    //     AND Move folder (root/level1c/level1c1/level2b) attempted
    // THEN a dialog appears with message like 'Move could not be initiated'
    public void testFX_moveFolderSelected_Alert() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "jar");

        // When
        simAddFolder(this, dir);

        Path file = Paths.get("root", "level1c", "level1c1", "level2b");
        simTraversalArchive(this, archive.toString(), "#fileContentsView", (r)->{}, SSV.split(file.toString())).get();
        clickOn("#btnMove");
        sleep(5, MILLISECONDS);
        clickOn("#mnuMoveSelected");

        // Then
        CommonSpecifications.thenExpectDialogWithMatchingMessage(this, "^Move could not be initiated.*");
    }

    @Test
    @DisplayName("Test: Move file into empty directory")
    // GIVEN a new jar archive has been created in PearlZip
    // WHEN folder added to archive (root)
    //     AND folder added to archive (empty-dir)
    //     AND Copy file (root/level1c/level1c1/level2b/MOVE_UP.txt) to (empty-dir/MOVE_UP.txt) not using context menu
    // THEN ensure files (empty-dir/MOVE_UP.txt) is included in the archive at depth (1) respectively
    public void testFX_moveFileIntoEmptyDirectory_Success() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "jar");

        // When
        simAddFolder(this, dir);
        simAddFolder(this, emptyDir);
        PearlZipSpecifications.whenFileCopiedWithinArchive(this, archive, Path.of("root","level1c","level1c1","level2b","MOVE_UP.txt"), Path.of("empty-dir","MOVE_UP.txt"), false);

        // Then
        PearlZipSpecifications.thenExpectFileExistsInArchive(archive, 1, "empty-dir/MOVE_UP.txt");
    }

    @Test
    @DisplayName("Test: Move file within a non-existent archive will yield the appropriate alert")
    // GIVEN a copy of zip archive (test.zip) is open in PearlZip
    // WHEN select file (first-file)
    //     AND delete file (copy of test.zip)
    //     AND Click Move file
    // THEN a dialog appears with message like 'Archive .* does not exist. PearlZip will now close the instance.'
    public void testFX_moveFileNonExistentArchive_Alert() throws IOException {
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
        // Select file to copy...
        sleep(100, MILLISECONDS);
        PearlZipSpecifications.whenEntrySelectedInCurrentWindow(this, "first-file");


        // Delete archive...
        Files.deleteIfExists(archivePath);

        // Try to extract...
        clickOn("#btnMove", MouseButton.PRIMARY);
        sleep(50, MILLISECONDS);

        clickOn("#mnuMoveSelected", MouseButton.PRIMARY);
        sleep(50, MILLISECONDS);

        // Then
        CommonSpecifications.thenExpectDialogWithMatchingMessage(this, "Archive .* does not exist. PearlZip will now close the instance.");
    }
}
