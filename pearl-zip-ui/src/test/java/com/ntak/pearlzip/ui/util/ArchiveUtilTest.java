/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.util;

import com.ntak.pearlzip.archive.pub.*;
import com.ntak.pearlzip.archive.pub.profile.component.ReadServiceComponent;
import com.ntak.pearlzip.archive.pub.profile.component.WriteServiceComponent;
import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.model.ZipState;
import com.ntak.pearlzip.ui.pub.FrmMainController;
import com.ntak.pearlzip.ui.util.internal.ArchiveUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.CNS_RES_BUNDLE;
import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.TMP_DIR_PREFIX;
import static com.ntak.pearlzip.ui.UITestSuite.clearDirectory;
import static com.ntak.pearlzip.ui.constants.ZipConstants.CK_RECENT_FILE;
import static com.ntak.pearlzip.ui.constants.ZipConstants.CK_RECENT_FILES_MENU;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@Tag("Excluded")
public class ArchiveUtilTest {

    private static MessageDigest digest;
    private static FXArchiveInfo mockArchiveInfo;
    private static ArchiveReadService mockArchiveReadService;
    private static ArchiveWriteService mockArchiveWriteService;
    private static Path tempDirectory;
    private static Menu menuRecent;
    private static CountDownLatch latch = new CountDownLatch(1);
    private static FrmMainController mockMainController;
    private static TableView<FileInfo> tableView;
    private static Scene scene;
    private static Path RECENT_FILE;
    private static ArchiveServiceProfile archiveServiceProfile = new ArchiveServiceProfile("test-provider");

    /*
        Test cases:
        + Extract with null directory passed in
        + Extract to a non-existent directory
        + Extract successfully

        + Back up archive successfully (hash check SHA256)

        + Handle directory. List all files/folders in specified directory

        + Recent files - Create files up to 5 files New up to 5 files (NO_FILES_HISTORY=5). No overwrite
        + Recent files - Add sixth file (with NO_FILES_HISTORY=5). Overwrite oldest

        + Render menu items for recent files as expected
        + Render menu items - exclude non-existent entries. Therefore entries <= NO_FILES_HISTORY

        + Create new Archive successfully when conditions are right to do so

        + Restore back up archive
        + Remove back up archive
        + Check archive exists - success
        + Check archive exists - failure
    */

    @BeforeAll
    public static void setUpOnce() throws NoSuchAlgorithmException, IOException, InterruptedException {
        try {
            AtomicReference<Stage> stage = new AtomicReference<>();
            System.setProperty(CNS_RES_BUNDLE, "pearlzip-ui");
            Platform.startup(() -> {
                try {
                    tableView = new TableView<>();
                    scene = new Scene(tableView);
                    InternalContextCache.INTERNAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_RECENT_FILES_MENU, new Menu());

                    stage.set(new Stage());
                    stage.get()
                         .setScene(scene);
                } finally {
                    latch.countDown();
                }
            });
        } catch (Exception e) {
            latch.countDown();
        } finally {
            latch.await();
            menuRecent = new Menu();

            archiveServiceProfile.addComponent(new WriteServiceComponent(Set.of("zip"), Collections.emptyMap()));
            archiveServiceProfile.addComponent(new ReadServiceComponent(Set.of("zip"), Collections.emptyMap()));


            digest = MessageDigest.getInstance("SHA-256");
            tempDirectory = Files.createTempDirectory("pz");
            RECENT_FILE = Paths.get(tempDirectory.toAbsolutePath()
                                                   .toString(), "rf");
            InternalContextCache.GLOBAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_RECENT_FILE, RECENT_FILE);

            mockArchiveInfo = Mockito.mock(FXArchiveInfo.class);
            mockArchiveReadService = Mockito.mock(ArchiveReadService.class);
            mockMainController = Mockito.mock(FrmMainController.class);

            mockArchiveWriteService = Mockito.mock(ArchiveWriteService.class);

            when(mockArchiveInfo.getArchivePath()).thenReturn(Paths.get("src", "test", "resources", "test.zip")
                                                                   .toString());
            when(mockArchiveInfo.getFiles()).thenReturn(FXCollections.observableArrayList(List.of(
                    new FileInfo(0, 0, "folder", 0, 0, 0,
                                 LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "user", "group",
                                 0, "", true, false, Collections.emptyMap()),
                    new FileInfo(1, 0, "file", 0, 0, 0,
                                 LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "user", "group",
                                 0, "", false, false, Collections.emptyMap()),
                    new FileInfo(2, 1, "inner-file", 0, 0, 0,
                                 LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "user", "group",
                                 0, "", false, false, Collections.emptyMap()),
                    new FileInfo(3, 1, "another-inner-file", 0, 0, 0,
                                 LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "user", "group",
                                 0, "", false, false, Collections.emptyMap())
            )));
            when(mockArchiveInfo.getReadService()).thenReturn(mockArchiveReadService);
            when(mockArchiveInfo.getController()).thenReturn(Optional.of(mockMainController));
            when(mockMainController.getFileContentsView()).thenReturn(tableView);

            when(mockArchiveWriteService.getArchiveServiceProfile()).thenReturn(archiveServiceProfile);
            when(mockArchiveReadService.getArchiveServiceProfile()).thenReturn(archiveServiceProfile);
            ZipState.addArchiveProvider(mockArchiveWriteService);
            ZipState.addArchiveProvider(mockArchiveReadService);
        }
    }

    @AfterAll
    public static void tearDownOnce() throws IOException {
        clearDirectory(tempDirectory);
    }

    @AfterEach
    public void tearDown() throws IOException {
        Files.deleteIfExists(RECENT_FILE);
    }

    @Test
    @DisplayName("Test: Extract to directory with no directory will not execute")
    public void testExtractToDirectory_NullDirectory_Nothing() {
        com.ntak.pearlzip.ui.util.ArchiveUtil.extractToDirectory(1L, mockArchiveInfo, null);
        verify(mockArchiveInfo, never()).getFiles();
    }

    @Test
    @DisplayName("Test: Extract to directory with non-existent directory will not execute")
    public void testExtractToDirectory_NonExistentDirectory_Nothing() {
        com.ntak.pearlzip.ui.util.ArchiveUtil.extractToDirectory(2L, mockArchiveInfo, Paths.get("non-existent-path").toFile());
        verify(mockArchiveInfo, never()).getFiles();
    }

    @Test
    @DisplayName("Test: Extract to directory with valid directory will execute")
    public void testExtractToDirectory_ValidDirectory_Success() {
        when(mockArchiveInfo.getArchiveInfo()).thenReturn(new ArchiveInfo());
        com.ntak.pearlzip.ui.util.ArchiveUtil.extractToDirectory(2L, mockArchiveInfo, tempDirectory.toFile());
        verify(mockArchiveInfo, times(1)).getFiles();
        verify(mockArchiveReadService,
               times((int)mockArchiveInfo.getFiles().stream().filter(f->!f.isFolder()).count())).extractFile(anyLong(),
                                                                                                          any(Path.class),
                                                                                                             any(ArchiveInfo.class),
                                                                                                             any(FileInfo.class));
    }

    @Test
    @DisplayName("Test: Back up existing archive successfully")
    public void testBackUpArchiver_ExistingArchive_Success() throws IOException {
        Path archive = Path.of(mockArchiveInfo.getArchivePath());
        Path backup = com.ntak.pearlzip.ui.util.ArchiveUtil.createBackupArchive(mockArchiveInfo, tempDirectory);

        Assertions.assertTrue(Files.exists(archive), "Archive does not exist");
        Assertions.assertTrue(Files.exists(backup), "Back up does not exist");
        try (
                InputStream archiveStream = Files.newInputStream(archive);
                InputStream backupStream = Files.newInputStream(backup)
        ) {
            long archiveHash = ByteBuffer.wrap(digest.digest(archiveStream.readAllBytes())).getLong();
            long backupHash = ByteBuffer.wrap(digest.digest(backupStream.readAllBytes())).getLong();
            String archiveHashString = Long.toHexString(archiveHash).toUpperCase();
            String backupHashString = Long.toHexString(backupHash).toUpperCase();

            Assertions.assertEquals(archiveHash, backupHash,
                String.format(
                        """
                              The archive and back up SHA-256 hashes were not identical. Details:
                              + Archive SHA-256 = 0x%s
                              + Back up SHA=256 = 0x%s
                        """,
                        archiveHashString, backupHashString)
            );
        }
    }

    @Test
    @DisplayName("Test: Handle directory of a valid directory will list all files and directories under the specified directory")
    public void testHandleDirectory_MatchExpectations() throws IOException {
        String[] dirs = {"top-level", "top-level/subdir", "top-level/subdir2", "top-level/subdir/subdir3"};
        String[] files = {"a-file", "top-level/subdir/b-file", "top-level/subdir2/c-file",
                          "top-level/subdir/subdir3/a-file"};
        Path tempDirectory = Files.createTempDirectory("pz");

        Arrays.stream(dirs).forEach(d -> {
            try {
                Files.createDirectory(Paths.get(tempDirectory.toAbsolutePath().toString(), d));
            } catch(IOException e) {
            }
        });
        Arrays.stream(files).forEach(f -> {
            try {
                Files.createFile(Paths.get(tempDirectory.toAbsolutePath().toString(), f));
            } catch(IOException e) {
            }
        });
        List<FileInfo> detectedFiles = ArchiveUtil.handleDirectory("", tempDirectory, tempDirectory,0,0);

        Assertions.assertEquals(dirs.length + files.length, detectedFiles.size(), "The expected number of files " +
                "were not returned");
        List<String> filenames = detectedFiles.stream().map(FileInfo::getFileName).collect(Collectors.toList());
        Arrays.stream(dirs).forEach(d->Assertions.assertTrue(filenames.contains(d),
                                                             String.format("File %s not found", d)));
        Arrays.stream(files).forEach(f->Assertions.assertTrue(filenames.contains(f),
                                                             String.format("File %s not found", f)));

        // clean up temporary directory...
        clearDirectory(tempDirectory);
    }

    @Test
    @DisplayName("Test: Adding first 5 recent files keeps all entries")
    public void testAddRecentFile_NoOverwrite_Success() throws IOException {
        for (int i = 1; i <= 5; i++) {
            Path file = Path.of(tempDirectory.toAbsolutePath().toString(), String.format("recent-file-%d", i));
            Files.deleteIfExists(file);
            Files.createFile(file);
            ArchiveUtil.addToRecentFile(file.toFile());
            final List<String> contents = Files.readAllLines(RECENT_FILE);
            Assertions.assertEquals(contents.size(),
                                    i,
                                    String.format("After adding file %d. The expected number of files were not kept",
                                                  i));
            for (int j = 1; j <= i; j++) {
                int fileIndex = j;
                Assertions.assertEquals(1,
                                        contents.stream()
                                                .filter(l -> l.matches(String.format(".*recent-file-%d$", fileIndex)))
                                                .count(),
                                        String.format("File recent-file-%d was not found", j));
            }
        }
    }

    @Test
    @DisplayName("Test: Adding sixth-most recent file, replaces oldest entry")
    public void testAddRecentFile_WithOverwrite_Success() throws IOException {
        // Prepare first five entries
        testAddRecentFile_NoOverwrite_Success();

        // Prepare sixth entry
        Path file = Path.of(tempDirectory.toAbsolutePath().toString(), "recent-file-6");
        Files.createFile(file);
        ArchiveUtil.addToRecentFile(file.toFile());

        // Apply sixth entry
        final List<String> contents = Files.readAllLines(RECENT_FILE);

        // Confirm only 5 most recent entries exist
        Assertions.assertEquals(5, contents.size(), "After adding file 6. The expected number of files were not kept");
        for (int j = 2; j <= 6; j++) {
            int fileIndex = j;
            Assertions.assertEquals(1,
                                    contents.stream()
                                            .filter(l -> l.matches(String.format(".*recent-file-%d$", fileIndex)))
                                            .count(),
                                    String.format("File recent-file-%d was not found", j));
        }

        Assertions.assertNotEquals(1,
                                   contents.stream()
                                           .filter(l -> l.matches(".*recent-file-1$"))
                                           .count(),
                                   "File recent-file-1 was unexpectedly found");
    }

    @Test
    @DisplayName("Test: Render menu items for recent files as expected")
    public List<MenuItem> testRenderRecentMenu_Success() throws IOException, InterruptedException {
        String[] expectations = {".*recent-file-1$",".*recent-file-2$",
                                 ".*recent-file-3$",".*recent-file-4$",
                                 ".*recent-file-5$"};

        // Prepare first five entries
        testAddRecentFile_NoOverwrite_Success();
        CountDownLatch latch = new CountDownLatch(1);

        JFXUtil.runLater(() -> {
            ArchiveUtil.refreshRecentFileMenu(menuRecent);
            latch.countDown();
        });
        latch.await();

        final ObservableList<MenuItem> items = menuRecent.getItems();
        Assertions.assertEquals(expectations.length, items.size(), "The number of items were not as expected");

        for (String regex : expectations) {
            Assertions.assertTrue(items.stream().anyMatch(i-> i.getText().matches(regex)),
                                  String.format("No item in matched pattern %s", regex)
            );
        }

        return items;
    }

    @Test
    @DisplayName("Test: Render menu items for recent files that exist only")
    public void testRenderRecentMenu_NonExistentFile_MatchExpectations() throws IOException, InterruptedException {
        // Prepare menu with 5 entries
        List<MenuItem> items = testRenderRecentMenu_Success();
        String[] expectations = {".*recent-file-2$",
                                 ".*recent-file-3$",
                                 ".*recent-file-4$",
                                 ".*recent-file-5$"};

        // Delete one file
        Optional<MenuItem> item;
        if ((item = items.stream().filter(f->f.getText().matches(".*recent-file-1$")).findFirst()).isPresent()) {
            Files.deleteIfExists(Paths.get(item.get().getText().split(" ")[1]));

            CountDownLatch latch = new CountDownLatch(1);

            JFXUtil.runLater(() -> {
                ArchiveUtil.refreshRecentFileMenu(menuRecent);
                latch.countDown();
            });
            latch.await();

            items = menuRecent.getItems();
            Assertions.assertEquals(expectations.length, items.size(), "The number of items were not as expected");

            for (String regex : expectations) {
                Assertions.assertTrue(items.stream().anyMatch(i-> i.getText().matches(regex)),
                                      String.format("No item in matched pattern %s", regex)
                );
            }
        } else {
            fail("No data was found in recent files");
        }
    }

    @Test
    @DisplayName("Test: Create new archive successfully")
    public void testCreateArchive_Success() throws IOException {
        ArchiveInfo archiveInfo = new ArchiveInfo();
        archiveInfo.setArchiveFormat("zip");

        final Path archive = Paths.get(tempDirectory.toAbsolutePath()
                                                 .toString(),
                                    "temp-archive.zip");
        Files.deleteIfExists(archive);
        Files.createFile(archive);

        archiveInfo.setArchivePath(archive.toAbsolutePath().toString());

        com.ntak.pearlzip.ui.util.ArchiveUtil.newArchive(1L, archiveInfo, archive.toFile());
        verify(mockArchiveWriteService, times(1)).createArchive(anyLong(), any(ArchiveInfo.class));
    }

    @Test
    @DisplayName("Test: Restore a back up of an archive successfully")
    public void testRestoreBackUpArchive_Success() throws IOException {
        Path targetLocation = Files.createTempFile("test", ".zip");
        Path refArchive = Paths.get("src", "test", "resources", "test.zip");
        Path backupArchive = Files.createTempFile("testBackup", ".zip");
        Files.copy(refArchive, backupArchive, StandardCopyOption.REPLACE_EXISTING);

        Assertions.assertTrue(Files.exists(targetLocation), "Target archive does not exist");
        Assertions.assertTrue(Files.exists(backupArchive), "Back up does not exist");

        // Check initial hashes...
        String backupHashString;
        try (
                InputStream archiveStream = Files.newInputStream(targetLocation);
                InputStream backupStream = Files.newInputStream(backupArchive)
        ) {
            long archiveHash = ByteBuffer.wrap(digest.digest(archiveStream.readAllBytes())).getLong();
            long backupHash = ByteBuffer.wrap(digest.digest(backupStream.readAllBytes())).getLong();
            String archiveHashString = Long.toHexString(archiveHash).toUpperCase();
            backupHashString = Long.toHexString(backupHash).toUpperCase();
            Assertions.assertNotEquals(archiveHashString, backupHashString, "Hashes should be different for back up " +
                    "and restore archives");
        }

        // Restore back up...
        com.ntak.pearlzip.ui.util.ArchiveUtil.restoreBackupArchive(backupArchive, targetLocation);

        // Recalculate hash of file...
        try (InputStream archiveStream = Files.newInputStream(targetLocation)) {
            long archiveHashAfter = ByteBuffer.wrap(digest.digest(archiveStream.readAllBytes()))
                                              .getLong();
            String archiveHashStringAfter = Long.toHexString(archiveHashAfter)
                                                .toUpperCase();
            Assertions.assertEquals(archiveHashStringAfter, backupHashString,
                                    String.format(
                                            """
                                                  The archive and back up SHA-256 hashes were not identical. Details:
                                                  + Archive SHA-256 = 0x%s
                                                  + Back up SHA=256 = 0x%s
                                            """,
                                            archiveHashStringAfter, backupHashString)
            );
        }

        Assertions.assertTrue(Files.notExists(backupArchive), "Back up file should have been deleted.");
    }

    @Test
    @DisplayName("Test: Check an archive exists")
    public void testCheckArchiveExists_Success() throws AlertException {
        when(mockArchiveInfo.getArchivePath()).thenReturn(Paths.get("src", "test", "resources", "test.zip")
                                                               .toString());
        com.ntak.pearlzip.ui.util.ArchiveUtil.checkArchiveExists(mockArchiveInfo);
    }

    @Test
    @DisplayName("Test: Check a non-existent archive")
    public void testCheckArchiveExists_Fail() {
        when(mockArchiveInfo.getArchivePath()).thenReturn(Paths.get("src", "test", "resources", "test-non-existent.zip")
                                                               .toString());
        Assertions.assertThrows(AlertException.class, ()-> com.ntak.pearlzip.ui.util.ArchiveUtil.checkArchiveExists(mockArchiveInfo));
    }

    @Test
    @DisplayName("Test: Remove a temporary archive and parent timestamp folder")
    public void testRemoveTempArchiveAndParentTempFolder_Fail() throws IOException {
        Path tempDir = Files.createTempDirectory(TMP_DIR_PREFIX);
        Path tempArchive = Paths.get(tempDir.toAbsolutePath().toString(), "temp-archive.zip");
        Files.createFile(tempArchive);

        com.ntak.pearlzip.ui.util.ArchiveUtil.removeBackupArchive(tempArchive);

        Assertions.assertTrue(Files.notExists(tempArchive), "Temporary archive unexpectedly exists");
        Assertions.assertTrue(Files.notExists(tempDir), "Temporary directory unexpectedly exists");
    }
}
