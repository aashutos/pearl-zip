/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.testfx;

import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.archive.util.CompressUtil;
import com.ntak.pearlzip.ui.UITestSuite;
import com.ntak.pearlzip.ui.util.AbstractPearlZipTestFX;
import com.ntak.testfx.FormUtil;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.ui.util.PearlZipFXUtil.*;

@Tag("fx-test")
public class AddToArchiveTestFX extends AbstractPearlZipTestFX {

    private static Path dir;

    /*
     * Test cases:
     * + Add folder to zip archive
     * + Add folder to tar archive
     * + Add folder to jar archive
     * + Add symbolic soft link file to zip archive
     * + Add symbolic hard link file and document file to tar archive
     * + Add image file to jar archive
     */

    @BeforeEach
    public void setUpOnce() throws IOException {
        dir = Paths.get(Files.createTempDirectory("pz").toString(), "root");
        Files.createDirectories(dir);

        // Creating files and directories...
        Files.createDirectories(Paths.get(dir.toAbsolutePath().toString(), "level1a"));
        Files.createFile(Paths.get(dir.toAbsolutePath().toString(), "level1a", "file1a1.txt"));
        Files.createFile(Paths.get(dir.toAbsolutePath().toString(), "level1a", "file1a2.txt"));
        Files.createFile(Paths.get(dir.toAbsolutePath().toString(), "level1a", "file1a3.txt"));
        Files.createFile(Paths.get(dir.toAbsolutePath().toString(), "level1a", "EXTRACT_ME.txt"));

        Files.createDirectories(Paths.get(dir.toAbsolutePath().toString(), "level1b", "level1b1"));
        Files.createFile(Paths.get(dir.toAbsolutePath().toString(), "level1b", "level1b1", "level2a.txt"));
        Files.createFile(Paths.get(dir.toAbsolutePath().toString(), "level1b", "file1b1.txt"));
        Files.createFile(Paths.get(dir.toAbsolutePath().toString(), "level1b", "file1b2.txt"));
        Files.createFile(Paths.get(dir.toAbsolutePath().toString(), "level1b", "DELETE_ME.txt"));

        Files.createDirectories(Paths.get(dir.toAbsolutePath().toString(), "level1c", "level1c1", "level2b"));
        Files.createDirectories(Paths.get(dir.toAbsolutePath().toString(), "level1c", "level1c2", "level2c"));
        Files.createFile(Paths.get(dir.toAbsolutePath().toString(), "level1c", "file1c1.txt"));
        Files.createFile(Paths.get(dir.toAbsolutePath().toString(), "level1c", "MOVE_DOWN.txt"));
        Files.createFile(Paths.get(dir.toAbsolutePath().toString(), "level1c", "COPY_DOWN.txt"));
        Files.createFile(Paths.get(dir.toAbsolutePath().toString(), "level1c", "level1c1", "level2b", "MOVE_UP.txt"));
        Files.createFile(Paths.get(dir.toAbsolutePath().toString(), "level1c", "level1c2", "level2c", "level2c1.txt"));
    }

    @AfterEach
    public void tearDownOnce() throws IOException {
        UITestSuite.clearDirectory(dir.getParent());
    }

    @Test
    @DisplayName("Test: Add folder to zip archive and verify contents")
    public void testFX_AddFolderToZipArchive_MatchExpectations() throws IOException {
        final String archiveFormat = "zip";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);
        simAddDirectoryToNewNonCompressorArchive(this, archive, dir);
    }

    @Test
    @DisplayName("Test: Add folder to tar archive and verify contents")
    public void testFX_AddFolderToTarArchive_MatchExpectations() throws IOException {
        final String archiveFormat = "tar";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);
        simAddDirectoryToNewNonCompressorArchive(this, archive, dir);
    }

    @Test
    @DisplayName("Test: Add folder to jar archive and verify contents")
    public void testFX_AddFolderToJarArchive_MatchExpectations() throws IOException {
        final String archiveFormat = "jar";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);
        simAddDirectoryToNewNonCompressorArchive(this, archive, dir);
    }

    @Test
    @DisplayName("Test: Add symbolic soft link file to zip archive and verify contents")
    public void testFX_AddSymSoftLinkFileToZipArchive_MatchExpectations() throws IOException {
        final String archiveFormat = "zip";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        Path file = Paths.get("src", "test", "resources", "test.lnk").toAbsolutePath();
        final long sourceHash = CompressUtil.crcHashFile(file.toFile());
        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);
        simNewArchive(this, archive);
        simAddFile(this, file);
        push(KeyCode.ENTER);
        TableView<FileInfo> fileContentsView = FormUtil.lookupNode(s->s.getTitle().contains(archiveName),
                                                                   "#fileContentsView");
        Assertions.assertEquals("test.zip", fileContentsView.getItems().get(0).getFileName(), "Shortcut was not " +
                "followed to original file");

        // Extract file and check consistency
        Path targetFile = Paths.get(dir.getParent().toAbsolutePath().toString(), file.getFileName().toString()).toAbsolutePath();
        FormUtil.selectTableViewEntry(this, fileContentsView,
                                      FileInfo::getFileName, file.getFileName().toString());
        simExtractFile(this, targetFile);
        final long targetHash = CompressUtil.crcHashFile(targetFile.toFile());

        Assertions.assertEquals(sourceHash, targetHash, "File hashes were not identical");
    }

    @Test
    @DisplayName("Test: Add symbolic hard link and document file to tar archive and verify contents")
    public void testFX_AddSymHardLinkFileToTarArchive_MatchExpectations() throws IOException {
        final String archiveFormat = "tar";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        Path fileHardLink = Paths.get("src", "test", "resources", "test-hard.lnk").toAbsolutePath();
        Path fileDoc = Paths.get("src", "test", "resources", "test.docx").toAbsolutePath();
        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);
        simNewArchive(this, archive);
        simAddFile(this, fileHardLink);
        push(KeyCode.ENTER);
        simAddFile(this, fileDoc);
        push(KeyCode.ENTER);

        TableView<FileInfo> fileContentsView = FormUtil.lookupNode(s->s.getTitle().contains(archiveName),
                                                                   "#fileContentsView");
        final List<String> files = fileContentsView.getItems()
                                                     .stream()
                                                     .map(FileInfo::getFileName)
                                                     .collect(Collectors.toList());
        Assertions.assertTrue(files.contains("test-hard.lnk"),
                              "Hard link was not found in archive");
        Assertions.assertTrue(files.contains("test.docx"), "Document was not found in archive");

        // Extract file and check consistency
        for (Path file : Arrays.asList(fileDoc, fileHardLink)) {
            final long sourceHash = CompressUtil.crcHashFile(file.toFile());
            Path targetFile = Paths.get(dir.getParent()
                                           .toAbsolutePath()
                                           .toString(),
                                        file.getFileName()
                                            .toString())
                                   .toAbsolutePath();
            FormUtil.selectTableViewEntry(this,
                                          fileContentsView,
                                          FileInfo::getFileName,
                                          file.getFileName()
                                              .toString());
            simExtractFile(this, targetFile);
            final long targetHash = CompressUtil.crcHashFile(targetFile.toFile());

            Assertions.assertEquals(sourceHash, targetHash, "File hashes were not identical");
        }
    }

    @Test
    @DisplayName("Test: Add image file to jar archive and verify contents")
    public void testFX_AddImageFileToZipArchive_MatchExpectations() throws IOException {
        final String archiveFormat = "jar";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        Path file = Paths.get("src", "test", "resources", "img.png").toAbsolutePath();
        final long sourceHash = CompressUtil.crcHashFile(file.toFile());
        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);
        simNewArchive(this, archive);
        simAddFile(this, file);

        TableView<FileInfo> fileContentsView = FormUtil.lookupNode(s->s.getTitle().contains(archiveName),
                                                                   "#fileContentsView");
        final List<String> files = fileContentsView.getItems()
                                                   .stream()
                                                   .map(FileInfo::getFileName)
                                                   .collect(Collectors.toList());
        Assertions.assertTrue(files.contains("img.png"),
                              "Image was not found in archive");

        // Extract file and check consistency
        Path targetFile = Paths.get(dir.getParent().toAbsolutePath().toString(), file.getFileName().toString()).toAbsolutePath();
        FormUtil.selectTableViewEntry(this, fileContentsView,
                                      FileInfo::getFileName, file.getFileName().toString());
        simExtractFile(this, targetFile);
        final long targetHash = CompressUtil.crcHashFile(targetFile.toFile());

        Assertions.assertEquals(sourceHash, targetHash, "File hashes were not identical");
    }
}
