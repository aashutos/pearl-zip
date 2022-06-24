/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.util;

import com.ntak.pearlzip.archive.pub.ArchiveReadService;
import com.ntak.pearlzip.archive.pub.ArchiveWriteService;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@Tag("Excluded")
public class JFXUtilTest {
    private static Button button;

    private static ArchiveReadService mockReadService;
    private static ArchiveWriteService mockWriteService;
    private static TableView<FileInfo> fileInfoTableView;
    private static FXArchiveInfo archiveInfo;
    private static Path archive;
    private static CountDownLatch latch = new CountDownLatch(1);

    /*
        Test cases:
        + Set image and text for a button
        + Refresh file view - traverse down tree
        + Refresh file view - traverse up tree
        + Refresh file view - refresh same directory level
     */

    @BeforeAll
    public static void setUpOnce() throws IOException, InterruptedException {
        try {
            Platform.startup(() -> latch.countDown());
        } catch (Exception e) {
            latch.countDown();
        } finally {
            latch.await();
            archive = Files.createTempFile("pz", "");

            button = new Button();
            button.setGraphic(new ImageView());
            fileInfoTableView = new TableView<>();

            mockReadService = Mockito.mock(ArchiveReadService.class);
            mockWriteService = Mockito.mock(ArchiveWriteService.class);

            archiveInfo = new FXArchiveInfo(archive.toAbsolutePath().toString(), mockReadService, mockWriteService);

            when(mockReadService.listFiles(anyLong(), eq(archiveInfo.getArchiveInfo()))).thenReturn(List.of(
                    new FileInfo(0, 0, "folder", 0, 0, 0,
                                 LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "user", "group",
                                 0, "", true, false, Collections.emptyMap()),
                    new FileInfo(1, 0, "file", 0, 0, 0,
                                 LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "user", "group",
                                 0, "", false, false, Collections.emptyMap()),
                    new FileInfo(2, 1, "folder/inner-file", 0, 0, 0,
                                 LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "user", "group",
                                 0, "", false, false, Collections.emptyMap()),
                    new FileInfo(3, 1, "folder/another-inner-file", 0, 0, 0,
                                 LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "user", "group",
                                 0, "", false, false, Collections.emptyMap()),
                    new FileInfo(4, 1, "folder2/hello-file", 0, 0, 0,
                                 LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "user", "group",
                                 0, "", false, false, Collections.emptyMap()),
                    new FileInfo(5, 1, "folder/hello-folder", 0, 0, 0,
                                 LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "user", "group",
                                 0, "", true, false, Collections.emptyMap()),
                    new FileInfo(6, 2, "folder/hello-folder/what-file", 0, 0, 0,
                                 LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "user", "group",
                                 0, "", false, false, Collections.emptyMap()),
                    new FileInfo(7, 2, "folder/hello-folder/which-file", 0, 0, 0,
                                 LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "user", "group",
                                 0, "", false, false, Collections.emptyMap()),
                    new FileInfo(8, 0, "folder2", 0, 0, 0,
                                 LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "user", "group",
                                 0, "", true, false, Collections.emptyMap())
            ));
        }
    }

    @Test
    @DisplayName("Test: Set image and text for a button successfully")
    public void testChangeButtonPicText_WithValidImageAndText_Success() {
        com.ntak.pearlzip.ui.util.internal.JFXUtil.changeButtonPicText(button, "add.png", "Test Button");
        Assertions.assertNotNull(button.getGraphic(), "Graphic was not assigned");
        Assertions.assertNotNull(((ImageView)button.getGraphic()).getImage(), "Image was not assigned");
        Assertions.assertTrue(((ImageView)button.getGraphic()).getImage().getUrl().matches(".*add\\.png"), "Image " +
                "assigned was not as expected");
        Assertions.assertEquals("Test Button", button.getText(), "Text does not match");
    }

    @Test
    @DisplayName("Test: Refresh FileView after traversal down file tree")
    public void testRefreshFileView_TraverseDownTree_MatchExpectations() {
        testTraversalFileView(List.of("file", "folder", "folder2"), 0, "",
                              List.of("folder/another-inner-file", "folder/hello-folder", "folder/inner-file"), 1,
                              "folder"
        );
    }

    @Test
    @DisplayName("Test: Refresh FileView after traversal up file tree")
    public void testRefreshFileView_TraverseUpTree_MatchExpectations() {
        testTraversalFileView(List.of("folder/hello-folder/what-file", "folder/hello-folder/which-file"), 2, "folder" +
                                      "/hello-folder",
                              List.of("folder/another-inner-file", "folder/hello-folder", "folder/inner-file"), 1,
                              "folder"
        );
    }

    @Test
    @DisplayName("Test: Refresh FileView with no traversal")
    public void testRefreshFileView_SameLevel_MatchExpectations() {
        testTraversalFileView(List.of("file", "folder", "folder2"), 0, "",
                              List.of("file", "folder", "folder2"), 0, ""
        );
    }

    public static void testTraversalFileView(List<String> initExpectations, int initDepth, String initPrefix,
            List<String> finalExpectations, int finalDepth, String finalPrefix) {
        // Initial state
        List<String> expectations = new ArrayList<>(initExpectations);
        expectations.sort(String::compareTo);
        JFXUtil.refreshFileView(fileInfoTableView, archiveInfo, initDepth, initPrefix);
        List<String> filenames = fileInfoTableView.getItems()
                                                  .stream()
                                                  .map(FileInfo::getFileName)
                                                  .sorted(String::compareTo)
                                                  .collect(Collectors.toList());

        Assertions.assertEquals(expectations.size(), filenames.size(), "The number of files was not as expected");
        Assertions.assertEquals(initDepth, archiveInfo.getDepth().get(), "The initial tree depth was not correct");
        Assertions.assertEquals(initPrefix, archiveInfo.getPrefix(), "Prefix was not initialised as expected");
        for (int i = 0; i < expectations.size(); i++) {
            Assertions.assertEquals(expectations.get(i), filenames.get(i),
                                    String.format("Filename %s did not match %s",
                                                  filenames.get(i),
                                                  expectations.get(i))
            );
        }

        // final state
        expectations = new ArrayList<>(finalExpectations);
        expectations.sort(String::compareTo);
        JFXUtil.refreshFileView(fileInfoTableView, archiveInfo, finalDepth, finalPrefix);
        filenames = fileInfoTableView.getItems()
                                     .stream()
                                     .map(FileInfo::getFileName)
                                     .sorted(String::compareTo)
                                     .collect(Collectors.toList());

        Assertions.assertEquals(expectations.size(), filenames.size(), "The number of files was not as expected");
        Assertions.assertEquals(finalDepth, archiveInfo.getDepth().get(), "The initial tree depth was not correct");
        Assertions.assertEquals(finalPrefix, archiveInfo.getPrefix(), "Prefix was not initialised as expected");
        for (int i = 0; i < expectations.size(); i++) {
            Assertions.assertEquals(expectations.get(i), filenames.get(i),
                                    String.format("Filename %s did not match %s",
                                                  filenames.get(i),
                                                  expectations.get(i))
            );
        }
    }
}
