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

public class CreatedHighlightFileInfoCellCallbackTest {

    CreatedHighlightFileInfoCellCallback callback = new CreatedHighlightFileInfoCellCallback();

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
    @DisplayName("Test: Set Created Time field successfully")
    public void testSetField_ValidParameters_Success() {
        final TableCell<FileInfo,FileInfo> cell = new TableCell<>();
        final LocalDateTime creationTime = LocalDateTime.of(2021, 1, 1, 2, 30, 0);
        FileInfo info = new FileInfo(0, 0, "filename", 0, 0, 0, LocalDateTime.now(), LocalDateTime.now(),
                                     creationTime, "user", "group", 0, "some comments", false,
                                     false,
                                     Collections.emptyMap());
        callback.setField(cell, info);
        Assertions.assertEquals(creationTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), cell.getText(), "Fields were not set as expected");
    }
}
