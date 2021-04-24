/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.cell;

import com.ntak.pearlzip.archive.pub.FileInfo;
import javafx.application.Platform;
import javafx.scene.control.TableCell;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

public class ModifiedHighlightFileInfoCellCallbackTest {

    ModifiedHighlightFileInfoCellCallback callback = new ModifiedHighlightFileInfoCellCallback();

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
    @DisplayName("Test: Set Last Modified (Write) Time field successfully")
    public void testSetField_ValidParameters_Success() {
        final TableCell<FileInfo,FileInfo> cell = new TableCell<>();
        final LocalDateTime lastWriteTime = LocalDateTime.of(2021, 3
                , 4, 23, 0, 0);
        FileInfo info = new FileInfo(0, 0, "filename", 232432432, 0, 0,
                                     lastWriteTime,
                                     LocalDateTime.now(), LocalDateTime.now(), "user", "group", 0, "some comments", false,
                                     false,
                                     Collections.emptyMap());
        callback.setField(cell, info);
        Assertions.assertEquals(lastWriteTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), cell.getText(), "Fields were not set as expected");
    }
}
