/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.cell;

import com.ntak.pearlzip.archive.pub.FileInfo;
import javafx.application.Platform;
import javafx.scene.control.TableCell;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

@Tag("Excluded")
public class PackedSizeHighlightFileInfoCellCallbackTest {

    PackedSizeHighlightFileInfoCellCallback callback = new PackedSizeHighlightFileInfoCellCallback();
    private static CountDownLatch latch = new CountDownLatch(1);

    /*
         Test cases:
         + Set field call with mock values to ensure the relevant functionality occurs
     */

    @BeforeAll
    public static void setUpOnce() throws InterruptedException {
        try {
            Platform.startup(() -> latch.countDown());
        } catch (Exception e) {
            latch.countDown();
        } finally {
            latch.await();
        }
    }

    @AfterAll
    public static void tearDownOnce() {

    }

    @Test
    @DisplayName("Test: Set Packed Size field successfully")
    public void testSetField_ValidParameters_Success() {
        final TableCell<FileInfo,FileInfo> cell = new TableCell<>();
        FileInfo info = new FileInfo(0, 0, "filename", 0, 1024, 0,
                                     LocalDateTime.now(),
                                     LocalDateTime.now(), LocalDateTime.now(), "user", "group", 0, "some comments", false,
                                     false,
                                     Collections.emptyMap());
        callback.setField(cell, info);
        Assertions.assertEquals("1024", cell.getText(), "Fields were not set as expected");
    }
}
