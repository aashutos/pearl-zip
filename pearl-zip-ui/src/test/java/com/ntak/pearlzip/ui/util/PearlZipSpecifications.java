/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.util;

import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.archive.util.CompressUtil;
import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.testfx.FormUtil;
import com.ntak.testfx.TestFXConstants;
import com.ntak.testfx.specifications.CommonSpecifications;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.util.Pair;
import org.junit.jupiter.api.Assertions;
import org.testfx.api.FxRobot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import static com.ntak.pearlzip.ui.constants.ResourceConstants.SSV;
import static com.ntak.pearlzip.ui.constants.ZipConstants.CK_WINDOW_MENU;
import static com.ntak.pearlzip.ui.util.PearlZipFXUtil.*;
import static com.ntak.testfx.NativeFileChooserUtil.chooseFile;
import static com.ntak.testfx.TestFXConstants.*;
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

    public static void givenExtensionStoreOpened(FxRobot robot) {
        robot.clickOn(Point2D.ZERO.add(160, 10))
             .clickOn(Point2D.ZERO.add(160, 60))
             .sleep(LONG_PAUSE, MILLISECONDS);
    }

    ////////////////
    ///// WHEN /////
    ////////////////

    public static void whenOpenNestedEntry(FxRobot robot, String archiveName, String... nestedArchivePath) {
        TableRow row = CommonSpecifications.retryRetrievalForDuration(TestFXConstants.RETRIEVAL_TIMEOUT_MILLIS, () -> PearlZipFXUtil.simTraversalArchive(robot, archiveName, "#fileContentsView", (r) -> {}, nestedArchivePath).get());
        robot.sleep(MEDIUM_PAUSE, MILLISECONDS);
        robot.doubleClickOn(row);
        robot.sleep(LONG_PAUSE, MILLISECONDS);
    }

    public static void whenCloseNestedArchive(FxRobot robot, boolean saveArchive) {
        whenCloseArchive(robot);
        DialogPane dialogPane = CommonSpecifications.retryRetrievalForDuration(TestFXConstants.RETRIEVAL_TIMEOUT_MILLIS, () -> robot.lookup(".dialog-pane").query());
        Assertions.assertTrue(dialogPane.getContentText()
                                        .startsWith(
                                                "Please specify if you wish to persist the changes of the nested archive"));
        robot.clickOn(dialogPane.lookupButton(saveArchive? ButtonType.YES:ButtonType.NO));
        robot.sleep(MEDIUM_PAUSE, MILLISECONDS);
    }

    public static void whenCloseArchive(FxRobot robot) {
        robot.clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 160));
        robot.sleep(SHORT_PAUSE, MILLISECONDS);
    }

    public static void whenDeleteFromArchive(FxRobot robot, Path archive, Path file) {
        PearlZipFXUtil.simTraversalArchive(robot, archive.toString(),"#fileContentsView", (r)->{}, SSV.split(file.toString()));
        PearlZipFXUtil.simDelete(robot);
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
                                archiveInfo.getFiles().stream().filter(e -> e.getLevel() == depth && Objects.equals(e.getFileName(), fileName)).findFirst().map(FileInfo::getFileName).orElse(""),
                                String.format("Expected file %s (at depth %d) was not found in archive %s", fileName, depth, archiveName));
    }

    public static void thenExpectNumberOfFilesInArchive(Path archiveName, int expectedCount) {
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

    public static void thenExpectedArchiveWindowIsSelected(String archiveName) {
        String value = CommonSpecifications.retryRetrievalForDuration(RETRIEVAL_TIMEOUT_MILLIS, () -> InternalContextCache.INTERNAL_CONFIGURATION_CACHE
                .<Menu>getAdditionalConfig(CK_WINDOW_MENU)
                .get().getItems()
                .stream()
                .filter(s -> s.getText()
                                .contains(String.format("%s%s", archiveName, " • "))
                ).findFirst().map(MenuItem::getText).orElse(""));
        Assertions.assertFalse(value.isEmpty(), String.format("Expected archive path %s not found as active.", archiveName));
    }

    public static void thenExpectFileHierarchyInTargetDirectory(Path tempDir, Path... files) {
        for (Path file : files) {
            Assertions.assertTrue(Files.exists(tempDir.resolve(file)), String.format("File: %s does not exist.", file.toAbsolutePath()));
        }
    }

    public static Path givenNewSingleFileCompressorArchive(FxRobot robot, String format, Path nestedFile, Path targetDir, boolean useMainMenu) {
        // New single file compressor archive
        if (useMainMenu) {
            robot.clickOn(Point2D.ZERO.add(110, 10))
                 .clickOn(Point2D.ZERO.add(110, 60));
        } else {
            robot.clickOn("#btnNew").clickOn("#mnuNewSingleFileCompressor");
        }

        // Set archive format
        ComboBox<String> cmbArchiveFormat = CommonSpecifications.retryRetrievalForDuration(RETRIEVAL_TIMEOUT_MILLIS, () -> FormUtil.lookupNode(s -> s.isShowing() && s.getTitle().equals("Create new archive..."), "#comboArchiveFormat"));
        FormUtil.selectComboBoxEntry(robot, cmbArchiveFormat, format);

        // Select file to archive
        robot.clickOn("#btnSelectFile");
        simOpenArchive(robot, nestedFile, false, false);
        robot.clickOn("#btnCreate").sleep(SHORT_PAUSE, MILLISECONDS);
        Path pathArchive = targetDir.toAbsolutePath().resolve(String.format("arbitrary-file.txt.%s", format));
        chooseFile(PLATFORM, robot, pathArchive.getParent().toAbsolutePath());

        // Check archive exists
        Assertions.assertTrue(Files.exists(pathArchive), String.format("Path: %s does not exist", pathArchive));

        return pathArchive;
    }

    public static void whenFileCopiedWithinArchive(FxRobot robot, Path archive, Path from, Path to, boolean useContextMenu) {
        FXArchiveInfo info = lookupArchiveInfo(archive.toString()).get();

        while (info.getDepth().get() > 0) {
            simUp(robot);
        }

        simCopyFile(robot, useContextMenu, archive.toString(), "#fileContentsView", from, SSV.split(from.getParent().relativize(to.getParent()).toString()));
    }

    public static void thenExpectNumberOfFileMatchingPattern(Path archive, int count, String regEx) {
        FXArchiveInfo info = lookupArchiveInfo(archive.toString()).get();
        Assertions.assertEquals(count, info.getFiles().stream().filter(f -> f.getFileName().matches(regEx)).count());
    }

    public static void whenFileExtracted(FxRobot robot, Path targetLocation) throws IOException {
        if (Files.isRegularFile(targetLocation)) {
            Files.deleteIfExists(targetLocation);
        }
        simExtractFile(robot, targetLocation);
    }

    public static TableRow<FileInfo> whenEntrySelectedInCurrentWindow(FxRobot robot, String entryName) {
        TableView<FileInfo> fileContentsView = robot.lookup("#fileContentsView").queryAs(TableView.class);
        return FormUtil.selectTableViewEntry(robot, fileContentsView, FileInfo::getFileName,
                                      entryName).get();
    }

    public static void thenFileInfoScreenContentsMatchesFileMetaData(FxRobot robot, FileInfo fileInfo) {
        Label lblIndexValue = robot.lookup("#lblIndexValue").queryAs(Label.class);
        Label lblLevelValue = robot.lookup("#lblLevelValue").queryAs(Label.class);
        Label lblFilenameValue = robot.lookup("#lblFilenameValue").queryAs(Label.class);
        Label lblHashValue = robot.lookup("#lblHashValue").queryAs(Label.class);
        Label lblRawSizeValue = robot.lookup("#lblRawSizeValue").queryAs(Label.class);
        Label lblPackedSizeValue = robot.lookup("#lblPackedSizeValue").queryAs(Label.class);
        Label lblFolderValue = robot.lookup("#lblFolderValue").queryAs(Label.class);
        Label lblEncryptValue = robot.lookup("#lblEncryptValue").queryAs(Label.class);
        Label lblCommentsValue = robot.lookup("#lblCommentsValue").queryAs(Label.class);
        Label lblLastWriteTimeValue = robot.lookup("#lblLastWriteTimeValue").queryAs(Label.class);
        Label lblLastAccessTimeValue = robot.lookup("#lblLastAccessTimeValue").queryAs(Label.class);
        Label lblCreateTimeValue = robot.lookup("#lblCreateTimeValue").queryAs(Label.class);
        Label lblUserValue = robot.lookup("#lblUserValue").queryAs(Label.class);
        Label lblGroupValue = robot.lookup("#lblGroupValue").queryAs(Label.class);

        robot.clickOn("#tpGeneral");
        robot.sleep(5, MILLISECONDS);
        Assertions.assertEquals(String.valueOf(fileInfo.getIndex()), lblIndexValue.getText(), "Index does not match");
        Assertions.assertEquals(String.valueOf(fileInfo.getLevel()), lblLevelValue.getText(), "Level does not match");
        Assertions.assertEquals(String.valueOf(fileInfo.getFileName()), lblFilenameValue.getText(),
                                "Filename does not match");
        Assertions.assertEquals(String.format("0x%s",Long.toHexString(fileInfo.getCrcHash()).toUpperCase()), lblHashValue.getText(),
                                "Hash does not match");
        Assertions.assertEquals(String.valueOf(fileInfo.getRawSize()), lblRawSizeValue.getText(),
                                "Raw size does not match");
        Assertions.assertEquals(String.valueOf(fileInfo.getPackedSize()), lblPackedSizeValue.getText(),
                                "Packed size does not match");
        Assertions.assertEquals(fileInfo.isFolder()?"folder":"file", lblFolderValue.getText(),
                                "Is Folder does not match");
        Assertions.assertEquals(fileInfo.isEncrypted()?"encrypted":"plaintext", lblEncryptValue.getText(),
                                "Is Encrypted does not match");
        Assertions.assertEquals(fileInfo.getComments(), lblCommentsValue.getText(),
                                "Comments does not match");

        robot.clickOn("#tpTimestamps");
        robot.sleep(5, MILLISECONDS);
        Assertions.assertEquals(Objects.isNull(fileInfo.getLastWriteTime())?"-":fileInfo.getLastWriteTime()
                                                                                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), lblLastWriteTimeValue.getText(),
                                "Last Write Timestamp does not match");
        Assertions.assertEquals(Objects.isNull(fileInfo.getLastAccessTime())?"-":fileInfo.getLastAccessTime()
                                                                                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), lblLastAccessTimeValue.getText(),
                                "Last Access Timestamp does not match");
        Assertions.assertEquals(Objects.isNull(fileInfo.getCreationTime())?"-":fileInfo.getCreationTime()
                                                                                         .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), lblCreateTimeValue.getText(),
                                "Creation Timestamp does not match");

        robot.clickOn("#tpOwnership");
        robot.sleep(5, MILLISECONDS);
        Assertions.assertEquals(fileInfo.getUser(), lblUserValue.getText(), "User does not match");
        Assertions.assertEquals(fileInfo.getGroup(), lblGroupValue.getText(), "Group does not match");

        robot.clickOn("#tpOther");
        robot.sleep(5, MILLISECONDS);
        TableView<Pair<String,String>> props = robot.lookup("#tblOtherInfo").queryAs(TableView.class);
        List<Pair<String,String>> propList = props.getItems();
        propList.stream().forEach(p->Assertions.assertEquals(fileInfo.getAdditionalInfoMap().get(p.getKey()),
                                                             p.getValue(),
                                                             String.format("Property (%s,%s) did not match",
                                                                           p.getKey(), p.getValue())));
    }
}
