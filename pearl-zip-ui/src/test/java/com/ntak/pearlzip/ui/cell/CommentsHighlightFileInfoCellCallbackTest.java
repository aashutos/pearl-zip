/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.cell;

import com.ntak.pearlzip.archive.pub.ArchiveReadService;
import com.ntak.pearlzip.archive.pub.ArchiveWriteService;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.*;
import org.mockito.internal.util.reflection.InstanceField;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

import static org.mockito.Mockito.mock;

@Tag("Excluded")
public class CommentsHighlightFileInfoCellCallbackTest {

    public static CommentsHighlightFileInfoCellCallback callback;
    private static CountDownLatch latch = new CountDownLatch(1);

    static TableCell<FileInfo,FileInfo> cell;
    static TableRow<FileInfo> row;

    /*
         Test cases:
         + Set field call with mock values to ensure the relevant functionality occurs
     */

    @BeforeAll
    public static void setUpOnce() throws InterruptedException, IOException {
        try {
            Platform.startup(() -> {
                cell = new TableCell<>();
                row = new TableRow<>();
                latch.countDown();
            });
        } catch (Exception e) {
            Platform.runLater(() -> {
                cell = new TableCell<>();
                row = new TableRow<>();
                latch.countDown();
            });
        } finally {
            latch.await();

            Path tmpFile = Files.createTempFile("tmp", ".zip");
            ArchiveReadService mockReadService = mock(ArchiveReadService.class);
            ArchiveWriteService mockWriteService = mock(ArchiveWriteService.class);
            FXArchiveInfo fxArchiveInfo = new FXArchiveInfo(tmpFile.toAbsolutePath().toString(),
                                                            mockReadService,
                                                            mockWriteService);
            callback = new CommentsHighlightFileInfoCellCallback(fxArchiveInfo);
        }
    }

    @AfterAll
    public static void tearDownOnce() {

    }

    @Test
    @DisplayName("Test: Set Comments field successfully")
    public void testSetField_ValidParameters_Success() throws NoSuchFieldException {
        final Field fieldRow = TableCell.class.getDeclaredField("tableRow");
        fieldRow.setAccessible(true);
        InstanceField fieldROTableRow = new InstanceField(fieldRow,
                                                          cell);
        fieldROTableRow.set(new ReadOnlyObjectWrapper(row));

        FileInfo info = new FileInfo(0, 0, "filename", 0, 0, 0, LocalDateTime.now(), LocalDateTime.now(),
                                     LocalDateTime.now(), "user", "group", 0, "some comments", false, false, Collections.emptyMap());
        callback.setField(cell, info);
        Assertions.assertEquals("some comments", ((TextField)cell.getGraphic()).getText(), "Fields were not set as " +
                "expected");
    }
}
