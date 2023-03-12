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
import javafx.scene.control.Button;
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
import static com.ntak.testfx.TestFXConstants.LONG_PAUSE;
import static com.ntak.testfx.TestFXConstants.SHORT_PAUSE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class DeleteInArchiveTestFX extends AbstractPearlZipTestFX {

    private static Path dir;
    private static Path setRoot;

    static {
        try {
            setRoot = Paths.get(Files.createTempDirectory("pz").toString(), "root");
        } catch(IOException e) {
        }
    }

    /*
     *  Test cases:
     *  + Delete file successfully in non-compressor archive
     *  + Delete failed in compressor archive
     *  + Delete folder successfully in non-compressor archive
     *  + Delete from a non-existent archive
     */

    @BeforeEach
    public void setUp() throws IOException {
        dir = genSourceDataSet();
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
        for (Path dir :
                Files.list(dir.getParent().getParent()).filter(p->p.getFileName().toString().startsWith("pz")).collect(
                        Collectors.toList())) {
            try {
                UITestSuite.clearDirectory(dir);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    @DisplayName("Test: Delete file successfully within Zip archive")
    // GIVEN a new zip archive has been created in PearlZip
    // WHEN folder added to archive (root)
    //     AND delete file (root/level1b/DELETE_ME.txt)
    // THEN ensure file (root/level1b/DELETE_ME.txt) does not exist
    public void testFX_DeleteFileZip_Success() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "zip");

        // When
        simAddFolder(this, dir);
        PearlZipSpecifications.whenDeleteFromArchive(this, archive, Path.of("root", "level1b", "DELETE_ME.txt"));

        // Then
        PearlZipSpecifications.thenExpectFileNotExistsInArchive(archive, 2, "root/level1b/DELETE_ME.txt");
    }

    @Test
    @DisplayName("Test: Delete file successfully within Tar archive")
    // GIVEN a new tar archive has been created in PearlZip
    // WHEN folder added to archive (root)
    //     AND delete file (root/level1b/DELETE_ME.txt)
    // THEN ensure file (root/level1b/DELETE_ME.txt) does not exist
    public void testFX_DeleteFileTar_Success() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "tar");

        // When
        simAddFolder(this, dir);
        PearlZipSpecifications.whenDeleteFromArchive(this, archive, Path.of("root", "level1b", "DELETE_ME.txt"));

        // Then
        PearlZipSpecifications.thenExpectFileNotExistsInArchive(archive, 2, "root/level1b/DELETE_ME.txt");
    }

    @Test
    @DisplayName("Test: Delete file successfully within Jar archive")
    // GIVEN a new jar archive has been created in PearlZip
    // WHEN folder added to archive (root)
    //     AND delete file (root/level1b/DELETE_ME.txt)
    // THEN ensure file (root/level1b/DELETE_ME.txt) does not exist
    public void testFX_DeleteFileJar_Success() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "jar");

        // When
        simAddFolder(this, dir);
        PearlZipSpecifications.whenDeleteFromArchive(this, archive, Path.of("root", "level1b", "DELETE_ME.txt"));

        // Then
        PearlZipSpecifications.thenExpectFileNotExistsInArchive(archive, 2, "root/level1b/DELETE_ME.txt");
    }

    @Test
    @DisplayName("Test: Delete file not possible within GZip archive")
    // GIVEN a new gz archive has been created in PearlZip
    // WHEN attempt to delete file (testgz.tar)
    // THEN ensure delete button is disabled
    public void testFX_DeleteFileGZip_Fail() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "tar.gz", "testgz");

        // When
        Path file = Paths.get("testgz.tar");
        simTraversalArchive(this, archive.toString(), "#fileContentsView", (r)->{}, SSV.split(file.toString())).get();
        simDelete(this);

        // Then
        Button btnDelete = lookupNode(s->s.getTitle().contains(archive.toString()), "#btnDelete");
        Assertions.assertTrue(btnDelete.isDisable(), "Delete is not disabled for compressor archives");
    }

    @Test
    @DisplayName("Test: Delete file not possible within BZip archive")
    // GIVEN a new bz2 archive has been created in PearlZip
    // WHEN attempt to delete file (testbz2.tar)
    // THEN ensure delete button is disabled
    public void testFX_DeleteFileBZip_Fail() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "tar.bz2", "testbz2");

        // When
        Path file = Paths.get("testbz2.tar");
        simTraversalArchive(this, archive.toString(), "#fileContentsView", (r)->{}, SSV.split(file.toString())).get();
        simDelete(this);

        // Then
        Button btnDelete = lookupNode(s->s.getTitle().contains(archive.toString()), "#btnDelete");
        Assertions.assertTrue(btnDelete.isDisable(), "Delete is not disabled for compressor archives");
    }

    @Test
    @DisplayName("Test: Delete file not possible within xz archive")
    // GIVEN a new xz archive has been created in PearlZip
    // WHEN attempt to delete file (testxz.tar)
    // THEN ensure delete button is disabled
    public void testFX_DeleteFileXZ_Fail() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "tar.xz", "testxz");

        // When
        Path file = Paths.get("testxz.tar");
        simTraversalArchive(this, archive.toString(), "#fileContentsView", (r)->{}, SSV.split(file.toString())).get();
        simDelete(this);

        // Then
        Button btnDelete = lookupNode(s->s.getTitle().contains(archive.toString()), "#btnDelete");
        Assertions.assertTrue(btnDelete.isDisable(), "Delete is not disabled for compressor archives");
    }

    @Test
    @DisplayName("Test: Delete folder successfully within Zip archive")
    // GIVEN a new zip archive has been created in PearlZip
    // WHEN folder added to archive (root)
    //     AND delete file (root/level1b/DELETE_ME_ALSO)
    // THEN ensure file (root/level1b/DELETE_ME_ALSO) does not exist
    //     AND ensure file (root/level1b/DELETE_ME_ALSO/AUTO_DELETED.txt) does not exist
    public void testFX_DeleteFolderZip_Success() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "zip");

        // When
        simAddFolder(this, dir);
        PearlZipSpecifications.whenDeleteFromArchive(this, archive, Path.of("root", "level1b", "DELETE_ME_ALSO"));

        // Then
        PearlZipSpecifications.thenExpectFileNotExistsInArchive(archive, 2, "root/level1b/DELETE_ME_ALSO");
        PearlZipSpecifications.thenExpectFileNotExistsInArchive(archive, 2, "root/level1b/DELETE_ME_ALSO/AUTO_DELETED.txt");
    }

    @Test
    @DisplayName("Test: Delete folder successfully within Tar archive")
    // GIVEN a new tar archive has been created in PearlZip
    // WHEN folder added to archive (root)
    //     AND delete file (root/level1b/DELETE_ME_ALSO)
    // THEN ensure file (root/level1b/DELETE_ME_ALSO) does not exist
    //     AND ensure file (root/level1b/DELETE_ME_ALSO/AUTO_DELETED.txt) does not exist
    public void testFX_DeleteFolderTar_Success() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "tar");

        // When
        simAddFolder(this, dir);
        PearlZipSpecifications.whenDeleteFromArchive(this, archive, Path.of("root", "level1b", "DELETE_ME_ALSO"));

        // Then
        PearlZipSpecifications.thenExpectFileNotExistsInArchive(archive, 2, "root/level1b/DELETE_ME_ALSO");
        PearlZipSpecifications.thenExpectFileNotExistsInArchive(archive, 2, "root/level1b/DELETE_ME_ALSO/AUTO_DELETED.txt");
    }

    @Test
    @DisplayName("Test: Delete folder successfully within Jar archive")
    // GIVEN a new jar archive has been created in PearlZip
    // WHEN folder added to archive (root)
    //     AND delete file (root/level1b/DELETE_ME_ALSO)
    // THEN ensure file (root/level1b/DELETE_ME_ALSO) does not exist
    //     AND ensure file (root/level1b/DELETE_ME_ALSO/AUTO_DELETED.txt) does not exist
    public void testFX_DeleteFolderJar_Success() {
        // Given
        Path archive = PearlZipSpecifications.givenCreateNewArchive(this, "jar");

        // When
        simAddFolder(this, dir);
        PearlZipSpecifications.whenDeleteFromArchive(this, archive, Path.of("root", "level1b", "DELETE_ME_ALSO"));

        // Then
        PearlZipSpecifications.thenExpectFileNotExistsInArchive(archive, 2, "root/level1b/DELETE_ME_ALSO");
        PearlZipSpecifications.thenExpectFileNotExistsInArchive(archive, 2, "root/level1b/DELETE_ME_ALSO/AUTO_DELETED.txt");
    }

    @Test
    @DisplayName("Test: Delete file within a non-existent archive will yield the appropriate alert")
    // GIVEN a copy of zip archive (test.zip) is open in PearlZip
    // WHEN select file (first-file)
    //     AND delete file (copy of test.zip)
    //     AND Click Delete file
    // THEN a dialog appears with message like 'Archive .* does not exist. PearlZip will now close the instance.'
    public void testFX_deleteFileNonExistentArchive_Alert() throws IOException {
        // Given
        final Path srcPath = Paths.get("src", "test", "resources", "test.zip")
                                  .toAbsolutePath();
        final Path archivePath = Paths.get(Files.createTempDirectory("pz").toAbsolutePath().toString(), "test.zip")
                                      .toAbsolutePath();
        Files.copy(srcPath, archivePath);

        simOpenArchive(this, archivePath, true, false);
        sleep(SHORT_PAUSE, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // When
        // Select file to copy...
        TableView<FileInfo> fileContentsView = lookup("#fileContentsView").queryAs(TableView.class);
        sleep(LONG_PAUSE, MILLISECONDS);
        FormUtil.selectTableViewEntry(this, fileContentsView, FileInfo::getFileName,
                                      "first-file").get();

        // Delete archive...
        Files.deleteIfExists(archivePath);

        // Try to extract...
        clickOn("#btnDelete", MouseButton.PRIMARY);
        sleep(SHORT_PAUSE, MILLISECONDS);

        // Then
        CommonSpecifications.thenExpectDialogWithMatchingMessage(this, "Archive .* does not exist. PearlZip will now close the instance.");

    }
}
