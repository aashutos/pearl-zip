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

public class HashHighlightFileInfoCellCallbackTest {

    HashHighlightFileInfoCellCallback callback = new HashHighlightFileInfoCellCallback();

    /*
         Test cases:
         + Set field call with mock values to ensure the relevant functionality occurs
     */

    @BeforeAll
    public static void setUpOnce() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {

        }
    }

    @AfterAll
    public static void tearDownOnce() {

    }

    @Test
    @DisplayName("Test: Set Hash field successfully")
    public void testSetField_ValidParameters_Success() {
        final TableCell<FileInfo,FileInfo> cell = new TableCell<>();
        FileInfo info = new FileInfo(0, 0, "filename", 232432432, 0, 0, LocalDateTime.now(), LocalDateTime.now(),
                                     LocalDateTime.now(), "user", "group", 0, "some comments", false,
                                     false,
                                     Collections.emptyMap());
        callback.setField(cell, info);
        Assertions.assertEquals("0xDDAA330", cell.getText(), "Fields were not set as expected");
    }
}
