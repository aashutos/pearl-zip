/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.testfx;

import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.constants.ZipConstants;
import com.ntak.pearlzip.ui.util.AbstractPearlZipTestFX;
import com.ntak.pearlzip.ui.util.PearlZipFXUtil;
import com.ntak.testfx.FormUtil;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.ui.UITestSuite.clearDirectory;
import static com.ntak.pearlzip.ui.util.PearlZipFXUtil.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class ExtractFromArchiveTestFX extends AbstractPearlZipTestFX {

    /*
     *  Test cases:
     *  + Extract single file
     *  + Extract directory
     *  + Extract all files
     *  + Extract single file - non existent archive
     *  + Extract all files - non existent archive
     */

    @AfterEach
    public void tearDown() throws IOException {
        clearDirectory(Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath()
                                                        .toString(), "output"));
        Files.deleteIfExists(Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath().toString(), "1151.txt"));
        Files.deleteIfExists(Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath().toString(), "first-file"));
    }

    ////////////////////////////////////////////////////////////////////////////////
    ////////// EXTRACT FILE ////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    @Test
    @DisplayName("Test: Extract single file from zip archive")
    public void testFX_extractSingleFileZipArchive_Success() throws IOException {
        // Open archive
        final Path archivePath = Paths.get("src", "test", "resources", "test.zip")
                                      .toAbsolutePath();
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive " +
                "was not present");

        // Select file to extract...
        TableView<FileInfo> fileContentsView = lookup("#fileContentsView").queryAs(TableView.class);
        FormUtil.selectTableViewEntry(this, fileContentsView, FileInfo::getFileName,
                                                               "first-file").get();

        // Extract to destination
        Path targetFilePath = Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath().toString(), "first-file");
        Files.deleteIfExists(targetFilePath);
        PearlZipFXUtil.simExtractFile(this, targetFilePath);

        // Check extraction was successful...
        Assertions.assertTrue(Files.exists(targetFilePath), "File was not extracted");
    }

    @Test
    @DisplayName("Test: Extract single file from tar archive")
    public void testFX_extractSingleFileTarArchive_Success() throws IOException {
        // Open archive
        final Path archivePath = Paths.get("src", "test", "resources", "test.tar")
                                      .toAbsolutePath();
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive " +
                "was not present");

        // Select file to extract...
        TableView<FileInfo> fileContentsView = lookup("#fileContentsView").queryAs(TableView.class);
        FormUtil.selectTableViewEntry(this, fileContentsView, FileInfo::getFileName,
                                                               "1151.txt").get();

        // Extract to destination
        Path targetFilePath = Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath().toString(), "1151.txt");
        Files.deleteIfExists(targetFilePath);
        PearlZipFXUtil.simExtractFile(this, targetFilePath);

        // Check extraction was successful...
        Assertions.assertTrue(Files.exists(targetFilePath), "File was not extracted");
    }

    @Test
    @DisplayName("Test: Extract single file from jar archive")
    public void testFX_extractSingleFileJarArchive_Success() throws IOException {
        // Open archive
        final Path archivePath = Paths.get("src", "test", "resources", "test.jar")
                                      .toAbsolutePath();
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive " +
                "was not present");

        // Select file to extract...
        TableView<FileInfo> fileContentsView = lookup("#fileContentsView").queryAs(TableView.class);
        FormUtil.selectTableViewEntry(this, fileContentsView, FileInfo::getFileName,
                                                               "first-file").get();

        // Extract to destination
        Path targetFilePath = Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath().toString(), "1151.txt");
        Files.deleteIfExists(targetFilePath);
        PearlZipFXUtil.simExtractFile(this, targetFilePath);

        // Check extraction was successful...
        Assertions.assertTrue(Files.exists(targetFilePath), "File was not extracted");
    }

    @Test
    @DisplayName("Test: Extract single file from 7zip archive")
    public void testFX_extractSingleFile7zArchive_Success() throws IOException {
        // Open archive
        final Path archivePath = Paths.get("src", "test", "resources", "test.7z")
                                      .toAbsolutePath();
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive " +
                "was not present");

        // Select file to extract...
        TableView<FileInfo> fileContentsView = lookup("#fileContentsView").queryAs(TableView.class);
        FormUtil.selectTableViewEntry(this, fileContentsView, FileInfo::getFileName,
                                                               "1151.txt").get();

        // Extract to destination
        Path targetFilePath = Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath().toString(), "1151.txt");
        Files.deleteIfExists(targetFilePath);
        PearlZipFXUtil.simExtractFile(this, targetFilePath);

        // Check extraction was successful...
        Assertions.assertTrue(Files.exists(targetFilePath), "File was not extracted");
    }

    @Test
    @DisplayName("Test: Extract single file from cab archive")
    public void testFX_extractSingleFileCabArchive_Success() throws IOException {
        // Open archive
        final Path archivePath = Paths.get("src", "test", "resources", "test.cab")
                                      .toAbsolutePath();
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive " +
                "was not present");

        // Select file to extract...
        PearlZipFXUtil.simTraversalArchive(this, archivePath.toString(), "#fileContentsView", (r)->{}, "lala", "1151.txt");

        // Extract to destination
        Path targetFilePath = Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath().toString(), "1151.txt");
        Files.deleteIfExists(targetFilePath);
        PearlZipFXUtil.simExtractFile(this, targetFilePath);

        // Check extraction was successful...
        Assertions.assertTrue(Files.exists(targetFilePath), "File was not extracted");
    }

    @Test
    @DisplayName("Test: Extract single file from iso archive")
    public void testFX_extractSingleFileIsoArchive_Success() throws IOException {
        // Open archive
        final Path archivePath = Paths.get("src", "test", "resources", "test.iso")
                                      .toAbsolutePath();
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive " +
                "was not present");

        // Select file to extract...
        TableView<FileInfo> fileContentsView = lookup("#fileContentsView").queryAs(TableView.class);
        FormUtil.selectTableViewEntry(this, fileContentsView, FileInfo::getFileName,
                                                               "1151.txt").get();

        // Extract to destination
        Path targetFilePath = Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath().toString(), "1151.txt");
        Files.deleteIfExists(targetFilePath);
        PearlZipFXUtil.simExtractFile(this, targetFilePath);

        // Check extraction was successful...
        Assertions.assertTrue(Files.exists(targetFilePath), "File was not extracted");
    }

    @Test
    @DisplayName("Test: Extract single file from rar archive")
    public void testFX_extractSingleFileRarArchive_Success() throws IOException {
        // Open archive
        final Path archivePath = Paths.get("src", "test", "resources", "test.rar")
                                      .toAbsolutePath();
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive " +
                "was not present");

        // Select file to extract...
        TableView<FileInfo> fileContentsView = lookup("#fileContentsView").queryAs(TableView.class);
        FormUtil.selectTableViewEntry(this, fileContentsView, FileInfo::getFileName,
                                                               "1151.txt").get();

        // Extract to destination
        Path targetFilePath = Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath().toString(), "1151.txt");
        Files.deleteIfExists(targetFilePath);
        PearlZipFXUtil.simExtractFile(this, targetFilePath);

        // Check extraction was successful...
        Assertions.assertTrue(Files.exists(targetFilePath), "File was not extracted");
    }

    @Test
    @DisplayName("Test: Extract single file from a non existent archive. Yield expected alert.")
    public void testFX_extractSingleFileNonExistentArchive_Alert() throws IOException {
        final Path srcPath = Paths.get("src", "test", "resources", "test.zip")
                                  .toAbsolutePath();
        final Path archivePath = Paths.get(Files.createTempDirectory("pz").toAbsolutePath().toString(), "test.zip")
                                      .toAbsolutePath();
        Files.copy(srcPath, archivePath);

        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Select file to extract...
        TableView<FileInfo> fileContentsView = lookup("#fileContentsView").queryAs(TableView.class);
        FormUtil.selectTableViewEntry(this, fileContentsView, FileInfo::getFileName,
                                      "first-file").get();

        // Delete archive...
        Files.deleteIfExists(archivePath);

        // Try to extract...
        clickOn("#btnExtract", MouseButton.PRIMARY);
        sleep(50, MILLISECONDS);

        clickOn("#mnuExtractSelectedFile", MouseButton.PRIMARY);
        sleep(50, MILLISECONDS);

        // Assert alert expectations
        DialogPane dialogPane = lookup(".dialog-pane").queryAs(DialogPane.class);
        Assertions.assertTrue(dialogPane.getContentText().matches("Archive .* does not exist. PearlZip will now close the instance."), "The text in warning dialog was not matched as expected");
    }

    @Test
    @DisplayName("Test: Extract folder from jar archive")
    public void testFX_extractFolderJarArchive_Success() throws IOException {
        // Open archive
        final Path archivePath = Paths.get("src", "test", "resources", "test.jar")
                                      .toAbsolutePath();
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive " +
                "was not present");

        // Select file to extract...
        TableView<FileInfo> fileContentsView = lookup("#fileContentsView").queryAs(TableView.class);
        FormUtil.selectTableViewEntry(this, fileContentsView, FileInfo::getFileName,
                                      "first-folder").get();

        // Extract to destination
        Path targetFolderPath = Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath().toString());
        clearDirectory(targetFolderPath);
        Files.createDirectories(targetFolderPath);
        PearlZipFXUtil.simExtractFile(this, targetFolderPath);

        // Check extraction was successful...
        Assertions.assertTrue(Files.exists(Paths.get(targetFolderPath.toString(),"first-folder")), "Folder was not extracted");
        Assertions.assertTrue(Files.exists(Paths.get(targetFolderPath.toString(),"first-folder","first-nested-file")),
                              "File first-nested-file was not " +
                                      "extracted");
        Assertions.assertTrue(Files.exists(Paths.get(targetFolderPath.toString(),"first-folder",".DS_Store")), "File .DS_Store was not extracted");
    }

    @Test
    @DisplayName("Test: Extract nested folder from zip archive")
    public void testFX_extractNestedFolderZipJArchive_Success() throws IOException {
        // Open archive
        final Path archivePath = Paths.get("src", "test", "resources", "dd.zip")
                                      .toAbsolutePath();
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive " +
                "was not present");

        // Select file to extract...
        TableView<FileInfo> fileContentsView = lookup("#fileContentsView").queryAs(TableView.class);
        simTraversalArchive(this, archivePath.getFileName().toString(), "#fileContentsView",
                                                     (r)->{},
                                                     "a", "b", "c").get();

        // Extract to destination
        Path targetFolderPath = Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath().toString());

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        clearDirectory(targetFolderPath);
        Files.createDirectories(targetFolderPath);
        PearlZipFXUtil.simExtractFile(this, targetFolderPath);

        // Check extraction was successful...
        Assertions.assertTrue(Files.exists(Paths.get(targetFolderPath.toString(),"c")), "Folder a was not extracted");
        Assertions.assertTrue(Files.exists(Paths.get(targetFolderPath.toString(),"c","temp-file")),
                "temp-file was not extracted");

        PearlZipFXUtil.simUp(this);
        PearlZipFXUtil.simUp(this);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        // Select file to extract...
        simTraversalArchive(this, archivePath.getFileName().toString(), "#fileContentsView",
                                                     (r)->{},
                                                     "a").get();

        // Extract to destination
        targetFolderPath = Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath().toString());
        clearDirectory(targetFolderPath);
        Files.createDirectories(targetFolderPath);
        PearlZipFXUtil.simExtractFile(this, targetFolderPath);

        // Check extraction was successful...
        Assertions.assertTrue(Files.exists(Paths.get(targetFolderPath.toString(),"a")), "Folder a was not extracted");
        Assertions.assertTrue(Files.exists(Paths.get(targetFolderPath.toString(),"a","b")),
                              "Folder a/b was not extracted");
        Assertions.assertTrue(Files.exists(Paths.get(targetFolderPath.toString(),"a","b", "c")),
                              "Folder a/b/c was not extracted");
        Assertions.assertTrue(Files.exists(Paths.get(targetFolderPath.toString(),"a","b","c","temp-file")),
                              "temp-file was not extracted");

        clearDirectory(targetFolderPath);
    }

    ////////////////////////////////////////////////////////////////////////////////
    ////////// EXTRACT ALL /////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    @Test
    @DisplayName("Test: Extract whole zip archive")
    public void testFX_extractAllFilesZipArchive_MatchExpectations() throws IOException {
        // Open archive
        final Path archivePath = Paths.get("src", "test", "resources", "test.zip")
                                      .toAbsolutePath();
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Store contents
        Path targetFilePath = Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath().toString(), "output");
        clearDirectory(targetFilePath);
        Files.createDirectories(targetFilePath);
        TableView<FileInfo> fileContentsView = lookup("#fileContentsView").queryAs(TableView.class);
        List<Path> paths = fileContentsView.getItems()
                                             .stream()
                                             .map(f->Paths.get(targetFilePath.toString(), f.getFileName()))
                                             .collect(Collectors.toList());

        // Extract archive...
        PearlZipFXUtil.simExtractAll(this, targetFilePath);
        for (Path path : paths) {
            Assertions.assertTrue(Files.exists(path), String.format("Path %s does not exist", path));
        }
    }

    @Test
    @DisplayName("Test: Extract whole tar archive")
    public void testFX_extractAllFilesTarArchive_MatchExpectations() throws IOException {
        // Open archive
        final Path archivePath = Paths.get("src", "test", "resources", "test.tar")
                                      .toAbsolutePath();
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Store contents
        Path targetFilePath = Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath().toString(), "output");
        clearDirectory(targetFilePath);
        Files.createDirectories(targetFilePath);
        TableView<FileInfo> fileContentsView = lookup("#fileContentsView").queryAs(TableView.class);
        List<Path> paths = fileContentsView.getItems()
                                           .stream()
                                           .map(f->Paths.get(targetFilePath.toString(), f.getFileName()))
                                           .collect(Collectors.toList());

        // Extract archive...
        PearlZipFXUtil.simExtractAll(this, targetFilePath);
        for (Path path : paths) {
            Assertions.assertTrue(Files.exists(path), String.format("Path %s does not exist", path));
        }
    }

    @Test
    @DisplayName("Test: Extract whole jar archive")
    public void testFX_extractAllFilesJarArchive_MatchExpectations() throws IOException {
        // Open archive
        final Path archivePath = Paths.get("src", "test", "resources", "test.jar")
                                      .toAbsolutePath();
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Store contents
        Path targetFilePath = Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath().toString(), "output");
        clearDirectory(targetFilePath);
        Files.createDirectories(targetFilePath);
        TableView<FileInfo> fileContentsView = lookup("#fileContentsView").queryAs(TableView.class);
        List<Path> paths = fileContentsView.getItems()
                                           .stream()
                                           .map(f->Paths.get(targetFilePath.toString(), f.getFileName()))
                                           .collect(Collectors.toList());

        // Extract archive...
        PearlZipFXUtil.simExtractAll(this, targetFilePath);
        for (Path path : paths) {
            Assertions.assertTrue(Files.exists(path), String.format("Path %s does not exist", path));
        }
    }

    @Test
    @DisplayName("Test: Extract whole 7zip archive")
    public void testFX_extractAllFiles7zArchive_MatchExpectations() throws IOException {
        // Open archive
        final Path archivePath = Paths.get("src", "test", "resources", "test.7z")
                                      .toAbsolutePath();
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Store contents
        Path targetFilePath = Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath().toString(), "output");
        clearDirectory(targetFilePath);
        Files.createDirectories(targetFilePath);
        TableView<FileInfo> fileContentsView = lookup("#fileContentsView").queryAs(TableView.class);
        List<Path> paths = fileContentsView.getItems()
                                           .stream()
                                           .map(f->Paths.get(targetFilePath.toString(), f.getFileName()))
                                           .collect(Collectors.toList());

        // Extract archive...
        PearlZipFXUtil.simExtractAll(this, targetFilePath);
        for (Path path : paths) {
            Assertions.assertTrue(Files.exists(path), String.format("Path %s does not exist", path));
        }
    }

    @Test
    @DisplayName("Test: Extract whole cab archive")
    public void testFX_extractAllFilesCabArchive_MatchExpectations() throws IOException {
        // Open archive
        final Path archivePath = Paths.get("src", "test", "resources", "test.cab")
                                      .toAbsolutePath();
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Store contents
        Path targetFilePath = Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath().toString(), "output");
        clearDirectory(targetFilePath);
        Files.createDirectories(targetFilePath);
        TableView<FileInfo> fileContentsView = lookup("#fileContentsView").queryAs(TableView.class);
        List<Path> paths = fileContentsView.getItems()
                                           .stream()
                                           .map(f->Paths.get(targetFilePath.toString(), f.getFileName()))
                                           .collect(Collectors.toList());

        // Extract archive...
        PearlZipFXUtil.simExtractAll(this, targetFilePath);
        for (Path path : paths) {
            Assertions.assertTrue(Files.exists(path), String.format("Path %s does not exist", path));
        }
    }

    @Test
    @DisplayName("Test: Extract whole iso archive")
    public void testFX_extractAllFilesIsoArchive_MatchExpectations() throws IOException {
        // Open archive
        final Path archivePath = Paths.get("src", "test", "resources", "test.iso")
                                      .toAbsolutePath();
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Store contents
        Path targetFilePath = Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath().toString(), "output");
        clearDirectory(targetFilePath);
        Files.createDirectories(targetFilePath);
        TableView<FileInfo> fileContentsView = lookup("#fileContentsView").queryAs(TableView.class);
        List<Path> paths = fileContentsView.getItems()
                                           .stream()
                                           .map(f->Paths.get(targetFilePath.toString(), f.getFileName()))
                                           .collect(Collectors.toList());

        // Extract archive...
        PearlZipFXUtil.simExtractAll(this, targetFilePath);
        for (Path path : paths) {
            Assertions.assertTrue(Files.exists(path), String.format("Path %s does not exist", path));
        }
    }

    @Test
    @DisplayName("Test: Extract whole rar archive")
    public void testFX_extractAllFilesRarArchive_MatchExpectations() throws IOException {
        // Open archive
        final Path archivePath = Paths.get("src", "test", "resources", "test.rar")
                                      .toAbsolutePath();
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Store contents
        Path targetFilePath = Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath().toString(), "output");
        clearDirectory(targetFilePath);
        Files.createDirectories(targetFilePath);
        TableView<FileInfo> fileContentsView = lookup("#fileContentsView").queryAs(TableView.class);
        List<Path> paths = fileContentsView.getItems()
                                           .stream()
                                           .map(f->Paths.get(targetFilePath.toString(), f.getFileName()))
                                           .collect(Collectors.toList());

        // Extract archive...
        PearlZipFXUtil.simExtractAll(this, targetFilePath);
        for (Path path : paths) {
            Assertions.assertTrue(Files.exists(path), String.format("Path %s does not exist", path));
        }
    }

    @Test
    @DisplayName("Test: Extract all files from a non existent archive. Yield expected alert.")
    public void testFX_extractAllFilesNonExistentArchive_Alert() throws IOException {
        final Path srcPath = Paths.get("src", "test", "resources", "test.zip")
                                      .toAbsolutePath();
        final Path archivePath = Paths.get(Files.createTempDirectory("pz").toAbsolutePath().toString(), "test.zip")
                                      .toAbsolutePath();
        Files.copy(srcPath, archivePath);

        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        Files.deleteIfExists(archivePath);

        clickOn("#btnExtract", MouseButton.PRIMARY);
        sleep(50, MILLISECONDS);

        clickOn("#mnuExtractAll", MouseButton.PRIMARY);
        sleep(50, MILLISECONDS);

        DialogPane dialogPane = lookup(".dialog-pane").queryAs(DialogPane.class);
        Assertions.assertTrue(dialogPane.getContentText().matches("Archive .* does not exist. PearlZip will now close the instance."), "The text in warning dialog was not matched as expected");
    }
}
