/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.util;

import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.archive.util.CompressUtil;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.testfx.FormUtil;
import com.ntak.testfx.TestFXConstants;
import com.ntak.testfx.specifications.CommonSpecifications;
import javafx.geometry.Point2D;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import org.junit.jupiter.api.Assertions;
import org.testfx.api.FxRobot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static com.ntak.pearlzip.ui.util.PearlZipFXUtil.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.jupiter.api.Assertions.fail;

public class PearlZipSpecifications {

    /////////////////
    ///// GIVEN /////
    /////////////////

    public static Path givenCreateNewArchive(FxRobot robot, String extension) {
        return givenCreateNewArchive(robot, extension, null);
    }

    public static Path givenCreateNewArchive(FxRobot robot, String extension, String name) {
        String fileName;
        if (!Objects.isNull(name)) {
            fileName = String.format("%s.%s", name, extension);
        } else {
            fileName = String.format("test%s.%s", extension, extension);
        }

        try {
            final var archiveName = Files.createTempDirectory("pz")
                                         .resolve(fileName);
            simNewArchive(robot, archiveName);
            Assertions.assertTrue(lookupArchiveInfo(fileName).isPresent(), "Archive is not open in PearlZip");

            return archiveName;
        } catch(IOException e) {
            fail(String.format("Could not create archive: %s", fileName));
            return null;
        }
    }

    public static String givenDefaultArchiveDetails() {
        String archiveName = JFXUtil.getMainStageInstances().stream().findFirst().map(s -> (FXArchiveInfo)s.getUserData()).map(FXArchiveInfo::getArchivePath).orElse(null);

        if (Objects.isNull(archiveName)) {
            fail("No open archives!");
        }

        return archiveName;
    }

    ////////////////
    ///// WHEN /////
    ////////////////

    public static void whenOpenNestedEntry(FxRobot robot, String archiveName, String... nestedArchivePath) {
        TableRow row = CommonSpecifications.retryRetrievalForDuration(TestFXConstants.RETRIEVAL_TIMEOUT_MILLIS, () -> PearlZipFXUtil.simTraversalArchive(robot, archiveName, "#fileContentsView", (r) -> {}, nestedArchivePath).get());
        robot.sleep(250, MILLISECONDS);
        robot.doubleClickOn(row);
    }

    public static void whenCloseNestedArchive(FxRobot robot, boolean saveArchive) {
        robot.clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 160));
        robot.sleep(50, MILLISECONDS);
        DialogPane dialogPane = CommonSpecifications.retryRetrievalForDuration(TestFXConstants.RETRIEVAL_TIMEOUT_MILLIS, () -> robot.lookup(".dialog-pane").query());
        Assertions.assertTrue(dialogPane.getContentText()
                                        .startsWith(
                                                "Please specify if you wish to persist the changes of the nested archive"));
        robot.clickOn(dialogPane.lookupButton(saveArchive? ButtonType.YES:ButtonType.NO));
        robot.sleep(250, MILLISECONDS);
    }

    ////////////////
    ///// THEN /////
    ////////////////

    public static void thenExpectFileExistsInCurrentWindow(Path archiveName, String fileInArchive) {
        String archivePath = CommonSpecifications.retryRetrievalForDuration(TestFXConstants.RETRIEVAL_TIMEOUT_MILLIS, () -> lookupArchiveInfo(archiveName.toString()).get().getArchivePath());
        TableView<FileInfo> fileContentsView = FormUtil.lookupNode(s -> s.getTitle()
                                                                         .contains(archivePath),
                                                                   "#fileContentsView");
        Assertions.assertNotNull(fileContentsView, "Expected archive was not found and so no metadata could be retrieved");
        Assertions.assertEquals(fileInArchive,
                                fileContentsView.getItems()
                                                .stream()
                                                .filter(f -> f.getFileName().equals(fileInArchive))
                                                .findFirst()
                                                .get()
                                                .getFileName(),
                                String.format("File %s was not found in archive", fileInArchive));
    }

    public static void thenExpectFileNotExistsInCurrentWindow(Path archiveName, String fileInArchive) {
        TableView<FileInfo> fileContentsView = FormUtil.lookupNode(s -> s.getTitle()
                                                                         .contains(archiveName.toString()),
                                                                   "#fileContentsView");
        Assertions.assertNotNull(fileContentsView, "Expected archive was not found and so no metadata could be retrieved");
        Assertions.assertFalse(fileContentsView.getItems()
                                                .stream()
                                                .filter(f -> f.getFileName().equals(fileInArchive))
                                                .findFirst()
                                                .isPresent(),
                                String.format("File %s was found in archive unexpectedly", fileInArchive));
    }

    public static void thenExpectNumberOfFilesInCurrentWindow(Path archiveName, int expectedCount) {
        TableView<FileInfo> fileContentsView = CommonSpecifications.retryRetrievalForDuration(TestFXConstants.RETRIEVAL_TIMEOUT_MILLIS, () -> FormUtil.lookupNode(s -> s.getTitle()
                                                                                                                                                                        .contains(archiveName.toString()),
                                                                                                                                                                  "#fileContentsView"));
        Assertions.assertNotNull(fileContentsView, "Expected archive was not found and so no metadata could be retrieved");
        Assertions.assertEquals(expectedCount,
                                fileContentsView.getItems()
                                                .size(),
                                String.format("Expected number of files (%d) was not found in archive %s", expectedCount, archiveName));
    }

    public static void thenExpectFileNotExistsInArchive(Path archiveName, int depth, String fileName) {
        FXArchiveInfo archiveInfo = PearlZipFXUtil.lookupArchiveInfo(archiveName.toString()).orElse(null);
        Assertions.assertNotNull(archiveInfo, "Expected archive was not found and so no metadata could be retrieved");
        Assertions.assertFalse(archiveInfo.getFiles().stream().filter(e -> e.getLevel() == depth && Objects.equals(e.getFileName(), fileName)).findFirst().isPresent(),
                                String.format("Expected file %s (at depth %d) was unexpectedly found in archive %s", fileName, depth, archiveName));
    }

    public static void thenExpectFileExistsInArchive(Path archiveName, int depth, String fileName) {
        FXArchiveInfo archiveInfo = PearlZipFXUtil.lookupArchiveInfo(archiveName.toString()).orElse(null);
        Assertions.assertNotNull(archiveInfo, "Expected archive was not found and so no metadata could be retrieved");
        Assertions.assertEquals(fileName,
                                archiveInfo.getFiles().stream().filter(e -> e.getLevel() == depth && Objects.equals(e.getFileName(), fileName)).findFirst().map(f -> f.getFileName()).orElse(""),
                                String.format("Expected file %s (at depth %d) was not found in archive %s", fileName, depth, archiveName));
    }

    public static void thenexpectNumberOfFilesInArchive(Path archiveName, int expectedCount) {
        FXArchiveInfo archiveInfo = PearlZipFXUtil.lookupArchiveInfo(archiveName.toString()).orElse(null);
        Assertions.assertNotNull(archiveInfo, "Expected archive was not found and so no metadata could be retrieved");
        Assertions.assertEquals(expectedCount,
                                archiveInfo.getFiles().size(),
                                String.format("Expected number of files (%d) was not found in archive %s", expectedCount, archiveName));
    }

    public static void thenExpectCRCHashFileEntryMatches(FxRobot robot, long expectedHash, Path archiveName, Path fileToExtract) throws IOException {
        Path tempDir = Files.createTempDirectory("pz");

        TableView<FileInfo> fileContentsView = FormUtil.lookupNode(s -> s.getTitle()
                                                                         .contains(archiveName.toString()),
                                                                   "#fileContentsView");
        FormUtil.selectTableViewEntry(robot,
                                      fileContentsView,
                                      FileInfo::getFileName,
                                      fileToExtract.toString());
        simExtractFile(robot, tempDir.resolve(fileToExtract.getFileName()));
        final long targetHash = CompressUtil.crcHashFile(tempDir.resolve(fileToExtract.getFileName()).toFile());

        Assertions.assertEquals(expectedHash, targetHash, "File hashes were not identical");
    }

    public static void thenCheckIntegrityOfExpectedFiles(FxRobot robot, Path[] referenceFiles, Path archiveName, Path[] entries) throws IOException {
        if (referenceFiles.length != entries.length) {
            fail("Parameters not set up correctly on expectations");
        }

        for (int i = 0; i < referenceFiles.length; i++) {
            final long sourceHash = CompressUtil.crcHashFile(referenceFiles[i].toFile());
            thenExpectFileExistsInCurrentWindow(archiveName, entries[i].toString());
            thenExpectCRCHashFileEntryMatches(robot, sourceHash, archiveName, entries[i]);
        }
    }

    public static void thenExpectNumberOfMainInstances(int expectedInstances) {
        Assertions.assertEquals(expectedInstances,
                                JFXUtil.getMainStageInstances()
                                       .size(),
                                String.format("New main windows instance unexpectedly created. Only expected %d instances", expectedInstances));
    }

    public static void thenMainInstanceExistsWithName(String archiveName) {
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent());
    }
}
