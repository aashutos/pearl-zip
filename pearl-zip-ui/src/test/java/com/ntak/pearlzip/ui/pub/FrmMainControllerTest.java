/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.pub;

import com.ntak.pearlzip.archive.pub.ArchiveReadService;
import com.ntak.pearlzip.archive.pub.ArchiveWriteService;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.model.ZipState;
import com.ntak.pearlzip.ui.util.JFXUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.InstanceField;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static org.mockito.Mockito.when;

@Tag("Excluded")
public class FrmMainControllerTest {
    private static FrmMainController controller;
    private static Stage stage;
    private static CountDownLatch latch = new CountDownLatch(1);

    private static TableView<FileInfo> fileContentsView;
    private static TableColumn<FileInfo, FileInfo> name;
    private static TableColumn<FileInfo, FileInfo> size;
    private static TableColumn<FileInfo, FileInfo> packedSize;
    private static TableColumn<FileInfo, FileInfo> modified;
    private static TableColumn<FileInfo, FileInfo> created;
    private static TableColumn<FileInfo, FileInfo> hash;
    private static TableColumn<FileInfo, FileInfo> comments;
    
    private static MenuButton btnNew;
    private static Button btnOpen;
    private static MenuButton btnAdd;
    private static MenuButton btnExtract;
    private static Button btnTest;
    private static MenuButton btnCopy;
    private static MenuButton btnMove;
    private static Button btnDelete;
    private static Button btnInfo;
    private static Button btnUp;
    private static ArchiveReadService mockReadService;
    private static ArchiveWriteService mockWriteService;
    private static FXArchiveInfo mockArchiveInfo;
    private static List<FileInfo> files;

    /*
        Test cases:
        + Non-compressor archive load
        + Compressor archive load
     */

    @BeforeAll
    public static void setUpOnce() throws InterruptedException, NoSuchFieldException {
        try {
            Platform.startup(()->latch.countDown());
        } catch (Exception e) {
            latch.countDown();
        } finally {
            latch.await();
            CountDownLatch secondaryLatch = new CountDownLatch(1);
            JFXUtil.runLater(() -> {
                stage = new Stage();
                secondaryLatch.countDown();
            });
            secondaryLatch.await();

            controller = new FrmMainController();

            // Initialise mocks
            mockReadService = Mockito.mock(ArchiveReadService.class);
            mockWriteService = Mockito.mock(ArchiveWriteService.class);
            mockArchiveInfo = Mockito.mock(FXArchiveInfo.class);

            // Initialise common stubbing
            when(mockReadService.supportedReadFormats()).thenReturn(List.of("zip","gz"));
            when(mockWriteService.supportedWriteFormats()).thenReturn(List.of("zip","gz"));
            when(mockReadService.getCompressorArchives()).thenCallRealMethod();
            when(mockWriteService.getCompressorArchives()).thenCallRealMethod();
            when(mockArchiveInfo.getReadService()).thenReturn(mockReadService);

            // Initialisation of field values
            fileContentsView = new TableView<>();
            name = new TableColumn<>();
            size = new TableColumn<>();
            packedSize = new TableColumn<>();
            modified = new TableColumn<>();
            created = new TableColumn<>();
            hash = new TableColumn<>();
            comments = new TableColumn<>();

            MenuItem mnuNewSingleFileCompressor = new MenuItem();
            mnuNewSingleFileCompressor.setId("mnuNewSingleFileCompressor");
            btnNew = new MenuButton(null,null, mnuNewSingleFileCompressor);
            btnOpen = new Button();
            btnAdd = new MenuButton();
            btnExtract = new MenuButton();
            btnTest = new Button();
            btnCopy = new MenuButton();
            btnMove = new MenuButton();
            btnDelete = new Button();
            btnInfo = new Button();
            btnUp = new Button();

            files = List.of(
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
            );

            // Reflectively set fields
            InstanceField fieldFileContentsView = new InstanceField(FrmMainController.class.getDeclaredField("fileContentsView"), controller);
            fieldFileContentsView.set(fileContentsView);
            InstanceField fieldName = new InstanceField(FrmMainController.class.getDeclaredField("name"), controller);
            fieldName.set(name);
            InstanceField fieldSize = new InstanceField(FrmMainController.class.getDeclaredField("size"), controller);
            fieldSize.set(size);
            InstanceField fieldPackedSize = new InstanceField(FrmMainController.class.getDeclaredField("packedSize"), controller);
            fieldPackedSize.set(packedSize);
            InstanceField fieldModified = new InstanceField(FrmMainController.class.getDeclaredField("modified"), controller);
            fieldModified.set(modified);
            InstanceField fieldCreated = new InstanceField(FrmMainController.class.getDeclaredField("created"), controller);
            fieldCreated.set(created);
            InstanceField fieldHash = new InstanceField(FrmMainController.class.getDeclaredField("hash"), controller);
            fieldHash.set(hash);
            InstanceField fieldComments = new InstanceField(FrmMainController.class.getDeclaredField("comments"), controller);
            fieldComments.set(comments);

            InstanceField fieldBtnNew = new InstanceField(FrmMainController.class.getDeclaredField("btnNew"), controller);
            fieldBtnNew.set(btnNew);
            InstanceField fieldBtnOpen = new InstanceField(FrmMainController.class.getDeclaredField("btnOpen"), controller);
            fieldBtnOpen.set(btnOpen);
            InstanceField fieldBtnAdd = new InstanceField(FrmMainController.class.getDeclaredField("btnAdd"), controller);
            fieldBtnAdd.set(btnAdd);
            InstanceField fieldBtnExtract = new InstanceField(FrmMainController.class.getDeclaredField("btnExtract"), controller);
            fieldBtnExtract.set(btnExtract);
            InstanceField fieldBtnTest = new InstanceField(FrmMainController.class.getDeclaredField("btnTest"), controller);
            fieldBtnTest.set(btnTest);
            InstanceField fieldBtnCopy = new InstanceField(FrmMainController.class.getDeclaredField("btnCopy"), controller);
            fieldBtnCopy.set(btnCopy);
            InstanceField fieldBtnMove = new InstanceField(FrmMainController.class.getDeclaredField("btnMove"), controller);
            fieldBtnMove.set(btnMove);
            InstanceField fieldBtnDelete = new InstanceField(FrmMainController.class.getDeclaredField("btnDelete"), controller);
            fieldBtnDelete.set(btnDelete);
            InstanceField fieldBtnInfo = new InstanceField(FrmMainController.class.getDeclaredField("btnInfo"), controller);
            fieldBtnInfo.set(btnInfo);
            InstanceField fieldBtnUp = new InstanceField(FrmMainController.class.getDeclaredField("btnUp"), controller);
            fieldBtnUp.set(btnUp);

            controller.initialize();
            ZipState.addArchiveProvider(mockReadService);
            ZipState.addArchiveProvider(mockWriteService);
        }
    }

    @Test
    @DisplayName("Test: Initialising a Main form with a non-compressor archives yields the expected configuration/layout")
    public void testInitData_NonCompressorArchive_MatchExpectations() {
        when(mockArchiveInfo.getArchivePath()).thenReturn(Paths.get("src", "test", "resources", "test.zip")
                                                               .toString());
        when(mockArchiveInfo.getFiles()).thenReturn(FXCollections.observableArrayList(files));

        controller.initData(stage, mockArchiveInfo);

        Assertions.assertEquals(mockArchiveInfo, stage.getUserData(), "User data for Stage not set to archive");
        Assertions.assertEquals(files.stream().filter(f->f.getLevel()==0).count(), fileContentsView.getItems().size()
                , "The expected number of files at level 0 was not found");
        for (FileInfo f : files.stream().filter(f->f.getLevel()==0).collect(Collectors.toSet())) {
            Assertions.assertTrue(fileContentsView.getItems().contains(f),
                                  String.format("File %s was not found in archive", f.getFileName()));
        }
    }

    @Test
    @DisplayName("Test: Initialising a Main form with a compressor archives yields the expected configuration/layout")
    public void testInitData_CompressorArchive_MatchExpectations() {
        when(mockArchiveInfo.getArchivePath()).thenReturn(Paths.get("src", "test", "resources", "test.tar.gz")
                                                               .toString());
        when(mockArchiveInfo.getFiles()).thenReturn(FXCollections.observableArrayList(files));

        controller.initData(stage, mockArchiveInfo);

        Assertions.assertEquals(mockArchiveInfo, stage.getUserData(), "User data for Stage not set to archive");
        Assertions.assertEquals(files.stream().filter(f->f.getLevel()==0).count(), fileContentsView.getItems().size()
                , "The expected number of files at level 0 was not found");

        for (FileInfo f : files.stream().filter(f->f.getLevel()==0).collect(Collectors.toSet())) {
            Assertions.assertTrue(fileContentsView.getItems().contains(f),
                                  String.format("File %s was not found in archive", f.getFileName()));
        }

        Assertions.assertTrue(btnAdd.isDisable(), "Add button has not been disabled");
        Assertions.assertTrue(btnDelete.isDisable(), "Delete button has not been disabled");
        Assertions.assertTrue(btnMove.isDisable(), "Move button has not been disabled");
        Assertions.assertTrue(btnCopy.isDisable(), "Copy button has not been disabled");
    }
}
