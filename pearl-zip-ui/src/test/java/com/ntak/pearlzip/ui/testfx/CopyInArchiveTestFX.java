/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.testfx;

import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.UITestSuite;
import com.ntak.pearlzip.ui.util.AbstractPearlZipTestFX;
import com.ntak.pearlzip.ui.util.PearlZipSpecifications;
import com.ntak.testfx.FormUtil;
import com.ntak.testfx.specifications.CommonSpecifications;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TableView;
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
public class CopyInArchiveTestFX extends AbstractPearlZipTestFX {

    private static Path dir;
    private static Path emptyDir;

   /*
    *  Test cases:
    *  + Copy up button and menu
    *  + Copy down button and menu
    *  + Copy disabled for compressor archives
    *  + Copy cancel
    *  + Copy no file selected
    *  + Copy folder fail
    *  + Table context menu paste
    *  + Copy into empty folder
    *  + Copy from within a non-existent archive
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
    @DisplayName("Test: Copy file down by context menu and button in application within a zip archive successfully")
    // GIVEN a new zip archive has been created in PearlZip
    // WHEN folder added to archive (root)
    //     AND Copy file (root/level1c/COPY_DOWN.txt) to (root/level1c/level1c1/COPY_DOWN.txt) not using context menu
    //     AND Copy file (root/level1c/level1c1/COPY_DOWN.txt) to (root/level1c/level1c1/level2b/COPY_DOWN.txt) using context menu
    // THEN ensure files (root/level1c/COPY_DOWN.txt, root/level1c/level1c1/COPY_DOWN.txt, root/level1c/level1c1/level2b/COPY_DOWN.txt) is included in the archive at depth (2,3,4) respectively
    public void testFX_copyFileDownByMenuAndButtonZip_MatchExpectations() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "zip");

        // When
        simAddFolder(this, dir);
        PearlZipSpecifications.whenFileCopiedWithinArchive(this, archive, Paths.get("root", "level1c", "COPY_DOWN.txt"), Paths.get("root", "level1c", "level1c1", "COPY_DOWN.txt"), false);
        PearlZipSpecifications.whenFileCopiedWithinArchive(this, archive, Paths.get("root", "level1c", "level1c1", "COPY_DOWN.txt"), Paths.get("root", "level1c", "level1c1", "level2b", "COPY_DOWN.txt"), true);

        // Then
        PearlZipSpecifications.thenExpectFileExistsInArchive(archive, 2, "root/level1c/COPY_DOWN.txt");
        PearlZipSpecifications.thenExpectFileExistsInArchive(archive, 3, "root/level1c/level1c1/COPY_DOWN.txt");
        PearlZipSpecifications.thenExpectFileExistsInArchive(archive, 4, "root/level1c/level1c1/level2b/COPY_DOWN.txt");
    }

    @Test
    @DisplayName("Test: Copy file up by table context menu and button in application within a zip archive successfully")
    // GIVEN a new zip archive has been created in PearlZip
    // WHEN folder added to archive (root)
    //     AND Copy file (root/level1c/level1c1/level2b/COPY_UP.txt) to (root/level1c/level1c1/COPY_UP.txt) not using context menu
    //     AND Copy file (root/level1c/level1c1/COPY_UP.txt) to (root/COPY_UP.txt) not using context menu
    // THEN ensure files (root/level1c/level1c1/level2b/COPY_UP.txt, root/level1c/level1c1/COPY_UP.txt, root/COPY_UP.txt) is included in the archive at depth (4,3,1) respectively
    public void testFX_copyFileUpByTableContextMenuAndButtonZip_MatchExpectations() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "zip");

        // When
        simAddFolder(this, dir);
        PearlZipSpecifications.whenFileCopiedWithinArchive(this, archive, Paths.get("root", "level1c", "level1c1", "level2b", "COPY_UP.txt"), Paths.get("root", "level1c", "level1c1", "COPY_UP.txt"), false);
        PearlZipSpecifications.whenFileCopiedWithinArchive(this, archive, Paths.get("root", "level1c", "level1c1", "COPY_UP.txt"), Paths.get("root",  "COPY_UP.txt"), true);

        // Then
        PearlZipSpecifications.thenExpectFileExistsInArchive(archive, 1, "root/COPY_UP.txt");
        PearlZipSpecifications.thenExpectFileExistsInArchive(archive, 3, "root/level1c/level1c1/COPY_UP.txt");
        PearlZipSpecifications.thenExpectFileExistsInArchive(archive, 4, "root/level1c/level1c1/level2b/COPY_UP.txt");
    }

    @Test
    @DisplayName("Test: Copy file down by context menu and button in application within a tar archive successfully")
    // GIVEN a new tar archive has been created in PearlZip
    // WHEN folder added to archive (root)
    //     AND Copy file (root/level1c/COPY_DOWN.txt) to (root/level1c/level1c1/COPY_DOWN.txt) not using context menu
    //     AND Copy file (root/level1c/level1c1/COPY_DOWN.txt) to (root/level1c/level1c1/level2b/COPY_DOWN.txt) using context menu
    // THEN ensure files (root/level1c/COPY_DOWN.txt, root/level1c/level1c1/COPY_DOWN.txt, root/level1c/level1c1/level2b/COPY_DOWN.txt) is included in the archive at depth (2,3,4) respectively
    public void testFX_copyFileDownByMenuAndButtonTar_MatchExpectations() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "tar");

        // When
        simAddFolder(this, dir);
        PearlZipSpecifications.whenFileCopiedWithinArchive(this, archive, Paths.get("root", "level1c", "COPY_DOWN.txt"), Paths.get("root", "level1c", "level1c1", "COPY_DOWN.txt"), false);
        PearlZipSpecifications.whenFileCopiedWithinArchive(this, archive, Paths.get("root", "level1c", "level1c1", "COPY_DOWN.txt"), Paths.get("root", "level1c", "level1c1", "level2b", "COPY_DOWN.txt"), true);

        // Then
        PearlZipSpecifications.thenExpectFileExistsInArchive(archive, 2, "root/level1c/COPY_DOWN.txt");
        PearlZipSpecifications.thenExpectFileExistsInArchive(archive, 3, "root/level1c/level1c1/COPY_DOWN.txt");
        PearlZipSpecifications.thenExpectFileExistsInArchive(archive, 4, "root/level1c/level1c1/level2b/COPY_DOWN.txt");
    }

    @Test
    @DisplayName("Test: Copy file down by context menu and button in application within a jar archive successfully")
    // GIVEN a new jar archive has been created in PearlZip
    // WHEN folder added to archive (root)
    //     AND Copy file (root/level1c/COPY_DOWN.txt) to (root/level1c/level1c1/COPY_DOWN.txt) not using context menu
    //     AND Copy file (root/level1c/level1c1/COPY_DOWN.txt) to (root/level1c/level1c1/level2b/COPY_DOWN.txt) using context menu
    // THEN ensure files (root/level1c/COPY_DOWN.txt, root/level1c/level1c1/COPY_DOWN.txt, root/level1c/level1c1/level2b/COPY_DOWN.txt) is included in the archive at depth (2,3,4) respectively
    public void testFX_copyFileDownByMenuAndButtonJar_MatchExpectations() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "jar");

        // When
        simAddFolder(this, dir);
        PearlZipSpecifications.whenFileCopiedWithinArchive(this, archive, Paths.get("root", "level1c", "COPY_DOWN.txt"), Paths.get("root", "level1c", "level1c1", "COPY_DOWN.txt"), false);
        PearlZipSpecifications.whenFileCopiedWithinArchive(this, archive, Paths.get("root", "level1c", "level1c1", "COPY_DOWN.txt"), Paths.get("root", "level1c", "level1c1", "level2b", "COPY_DOWN.txt"), true);

        // Then
        PearlZipSpecifications.thenExpectFileExistsInArchive(archive, 2, "root/level1c/COPY_DOWN.txt");
        PearlZipSpecifications.thenExpectFileExistsInArchive(archive, 3, "root/level1c/level1c1/COPY_DOWN.txt");
        PearlZipSpecifications.thenExpectFileExistsInArchive(archive, 4, "root/level1c/level1c1/level2b/COPY_DOWN.txt");
    }

    @Test
    @DisplayName("Test: Copy file up by context menu and button in application within a tar archive successfully")
    // GIVEN a new tar archive has been created in PearlZip
    // WHEN folder added to archive (root)
    //     AND Copy file (root/level1c/level1c1/level2b/COPY_UP.txt) to (root/level1c/COPY_UP.txt) not using context menu
    //     AND Copy file (root/level1c/level1c1/COPY_UP.txt) to (root/COPY_UP.txt) not using context menu
    // THEN ensure files (root/level1c/level1c1/level2b/COPY_UP.txt, root/level1c/COPY_UP.txt, root/COPY_UP.txt) is included in the archive at depth (4,2,1) respectively
    public void testFX_copyFileUpByMenuAndButtonTar_MatchExpectations() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "tar");

        // When
        simAddFolder(this, dir);
        PearlZipSpecifications.whenFileCopiedWithinArchive(this, archive, Paths.get("root", "level1c", "level1c1", "level2b", "COPY_UP.txt"), Paths.get("root", "level1c", "level1c1", "COPY_UP.txt"), false);
        PearlZipSpecifications.whenFileCopiedWithinArchive(this, archive, Paths.get("root", "level1c", "level1c1", "COPY_UP.txt"), Paths.get("root",  "COPY_UP.txt"), true);

        // Then
        PearlZipSpecifications.thenExpectFileExistsInArchive(archive, 1, "root/COPY_UP.txt");
        PearlZipSpecifications.thenExpectFileExistsInArchive(archive, 3, "root/level1c/level1c1/COPY_UP.txt");
        PearlZipSpecifications.thenExpectFileExistsInArchive(archive, 4, "root/level1c/level1c1/level2b/COPY_UP.txt");
    }

    @Test
    @DisplayName("Test: Copy file up by context menu and button in application within a jar archive successfully")
    // GIVEN a new jar archive has been created in PearlZip
    // WHEN folder added to archive (root)
    //     AND Copy file (root/level1c/level1c1/level2b/COPY_UP.txt) to (root/level1c/COPY_UP.txt) not using context menu
    //     AND Copy file (root/level1c/level1c1/COPY_UP.txt) to (root/COPY_UP.txt) not using context menu
    // THEN ensure files (root/level1c/level1c1/level2b/COPY_UP.txt, root/level1c/COPY_UP.txt, root/COPY_UP.txt) is included in the archive at depth (4,2,1) respectively
    public void testFX_copyFileUpByTableContextMenuAndButtonJar_MatchExpectations() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "jar");

        // When
        simAddFolder(this, dir);
        PearlZipSpecifications.whenFileCopiedWithinArchive(this, archive, Paths.get("root", "level1c", "level1c1", "level2b", "COPY_UP.txt"), Paths.get("root", "level1c", "level1c1", "COPY_UP.txt"), false);
        PearlZipSpecifications.whenFileCopiedWithinArchive(this, archive, Paths.get("root", "level1c", "level1c1", "COPY_UP.txt"), Paths.get("root",  "COPY_UP.txt"), true);

        // Then
        PearlZipSpecifications.thenExpectFileExistsInArchive(archive, 1, "root/COPY_UP.txt");
        PearlZipSpecifications.thenExpectFileExistsInArchive(archive, 3, "root/level1c/level1c1/COPY_UP.txt");
        PearlZipSpecifications.thenExpectFileExistsInArchive(archive, 4, "root/level1c/level1c1/level2b/COPY_UP.txt");
    }

    @Test
    @DisplayName("Test: Copy file in application within a Gzip archive is blocked (Single file compressor)")
    // GIVEN a new tar.gz archive has been created in PearlZip
    // WHEN Copy attempted
    // THEN Ensure copy button is disabled
    public void testFX_copyFileUpByMenuGzip_Blocked() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "gz", "test.tar");

        // When
        clickOn("#btnCopy", MouseButton.PRIMARY);
        sleep(SHORT_PAUSE, MILLISECONDS);

        // Then
        MenuButton btnCopy = lookupNode(s->s.getTitle().contains(archive.toString()), "#btnCopy");
        Assertions.assertTrue(btnCopy.isDisable(), "Copy is not disabled for compressor archives");
    }

    @Test
    @DisplayName("Test: Copy file in application within a Bzip archive is blocked (Single file compressor)")
    // GIVEN a new tar.bz2 archive has been created in PearlZip
    // WHEN Copy attempted
    // THEN Ensure copy button is disabled
    public void testFX_copyFileUpByMenuBzip_Blocked() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "bz2", "test.tar");

        // When
        clickOn("#btnCopy", MouseButton.PRIMARY);
        sleep(SHORT_PAUSE, MILLISECONDS);

        // Then
        MenuButton btnCopy = lookupNode(s->s.getTitle().contains(archive.toString()), "#btnCopy");
        Assertions.assertTrue(btnCopy.isDisable(), "Copy is not disabled for compressor archives");
    }

    @Test
    @DisplayName("Test: Copy file in application within a xz archive is blocked (Single file compressor)")
    // GIVEN a new tar.xz archive has been created in PearlZip
    // WHEN Copy attempted
    // THEN Ensure copy button is disabled
    public void testFX_copyFileUpByMenuXz_Blocked() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "xz", "test.tar");

        // When
        clickOn("#btnCopy", MouseButton.PRIMARY);
        sleep(SHORT_PAUSE, MILLISECONDS);

        // Then
        MenuButton btnCopy = lookupNode(s->s.getTitle().contains(archive.toString()), "#btnCopy");
        Assertions.assertTrue(btnCopy.isDisable(), "Copy is not disabled for compressor archives");
    }

    @Test
    @DisplayName("Test: Copy file and cancel")
    // GIVEN a new jar archive has been created in PearlZip
    // WHEN folder added to archive (root)
    //     AND select file (root/level1c/level1c1/level2b/COPY_UP.txt)
    //     AND Click Copy file
    //     AND Cancel Copy process
    // THEN ensure the number of files in archive = 1 with filename matching '.*COPY_UP.txt'
    public void testFX_copyFileCancel() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "jar");

        // When
        simAddFolder(this, dir);

        Path file = Paths.get("root", "level1c", "level1c1", "level2b", "COPY_UP.txt");
        final String tableName = "#fileContentsView";
        simTraversalArchive(this, archive.toString(), tableName, (r)->{}, SSV.split(file.toString()));
        clickOn("#btnCopy");
        sleep(5, MILLISECONDS);
        clickOn("#mnuCopySelected");
        sleep(5, MILLISECONDS);
        clickOn("#btnCopy");
        sleep(5, MILLISECONDS);
        clickOn("#mnuCancelCopy");

        // Then
        PearlZipSpecifications.thenExpectNumberOfFileMatchingPattern(archive, 1, ".*COPY_UP.txt");
    }

    @Test
    @DisplayName("Test: Copy with no file selected raises alert")
    // GIVEN a new jar archive has been created in PearlZip
    // WHEN folder added to archive (root)
    //     AND Click Copy file
    // THEN a dialog appears with message like 'Copy could not be initiated'
    public void testFX_copyNoFileSelected_Alert() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "jar");

        // When
        simAddFolder(this, dir);

        clickOn("#btnCopy");
        sleep(5, MILLISECONDS);
        clickOn("#mnuCopySelected");

        // Then
        CommonSpecifications.thenExpectDialogWithMatchingMessage(this, "^Copy could not be initiated.*");
    }

    @Test
    @DisplayName("Test: Copy on folder raises alert")
    // GIVEN a new jar archive has been created in PearlZip
    // WHEN folder added to archive (root)
    //     AND Copy folder (root/level1c/level1c1/level2b) attempted
    // THEN a dialog appears with message like 'Copy could not be initiated'
    public void testFX_copyFolderSelected_Alert() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "jar");

        // When
        simAddFolder(this, dir);

        Path file = Paths.get("root", "level1c", "level1c1", "level2b");
        simTraversalArchive(this, archive.toString(), "#fileContentsView", (r)->{}, SSV.split(file.toString())).get();
        clickOn("#btnCopy");
        sleep(5, MILLISECONDS);
        clickOn("#mnuCopySelected");

        // Then
        CommonSpecifications.thenExpectDialogWithMatchingMessage(this, "^Copy could not be initiated.*");
    }

    @Test
    @DisplayName("Test: Copy file into empty directory")
    // GIVEN a new jar archive has been created in PearlZip
    // WHEN folder added to archive (root)
    //     AND folder added to archive (empty-dir)
    //     AND Copy file (root/level1c/level1c1/level2b/COPY_UP.txt) to (empty-dir/COPY_UP.txt) not using context menu
    // THEN ensure files (empty-dir/COPY_UP.txt) is included in the archive at depth (1) respectively
    public void testFX_copyFileIntoEmptyDirectory_Success() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "jar");

        // When
        simAddFolder(this, dir);
        simAddFolder(this, emptyDir);
        PearlZipSpecifications.whenFileCopiedWithinArchive(this, archive, Path.of("root","level1c","level1c1","level2b","COPY_UP.txt"), Path.of("empty-dir","COPY_UP.txt"), false);

        // Then
        PearlZipSpecifications.thenExpectFileExistsInArchive(archive, 1, "empty-dir/COPY_UP.txt");
    }

    @Test
    @DisplayName("Test: Copy file within a non-existent archive will yield the appropriate alert")
    // GIVEN a copy of zip archive (test.zip) is open in PearlZip
    // WHEN select file (first-file)
    //     AND delete file (copy of test.zip)
    //     AND Click Copy file
    // THEN a dialog appears with message like 'Archive .* does not exist. PearlZip will now close the instance.'
    public void testFX_copyFileNonExistentArchive_Alert() throws IOException {
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
        TableView<FileInfo> fileContentsView = lookup("#fileContentsView").queryAs(TableView.class);
        sleep(100, MILLISECONDS);
        FormUtil.selectTableViewEntry(this, fileContentsView, FileInfo::getFileName,
                                      "first-file").get();

        // Delete archive...
        Files.deleteIfExists(archivePath);

        // Try to extract...
        clickOn("#btnCopy", MouseButton.PRIMARY);
        sleep(50, MILLISECONDS);

        clickOn("#mnuCopySelected", MouseButton.PRIMARY);
        sleep(50, MILLISECONDS);

        // Then
        CommonSpecifications.thenExpectDialogWithMatchingMessage(this, "Archive .* does not exist. PearlZip will now close the instance.");
    }
}
