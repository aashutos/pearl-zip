/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.cell;

import com.ntak.pearlzip.archive.pub.FileInfo;
import javafx.application.Platform;
import javafx.scene.control.TableCell;
import javafx.scene.image.ImageView;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.Collections;

import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.KEY_ICON_REF;

public class NameHighlightFileInfoCellCallbackTest {

    NameHighlightFileInfoCellCallback callback = new NameHighlightFileInfoCellCallback();

    /*
         Test cases:
         + Set field call with mock values (file icon) to ensure the relevant functionality occurs
         + Set field call with mock values (folder icon) to ensure the relevant functionality occurs
         + Set field call with mock values (custom icon) to ensure the relevant functionality occurs
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
    @DisplayName("Test: Set Name field with file icon successfully")
    public void testSetField_ValidFile_MatchExpectations() {
        final TableCell<FileInfo,FileInfo> cell = new TableCell<>();
        FileInfo info = new FileInfo(0, 0, "filename", 0, 0, 0,
                                     LocalDateTime.now(),
                                     LocalDateTime.now(), LocalDateTime.now(), "user", "group", 0, "some comments", false,
                                     false,
                                     Collections.emptyMap());
        callback.setField(cell, info);
        Assertions.assertEquals("filename", cell.getText(), "Fields were not set as expected");
        Assertions.assertTrue(((ImageView)(cell.getGraphic())).getImage().getUrl().matches(".*file.png"),
                              "Fields were not set as expected");

        info = new FileInfo(0, 0, "dir", 0, 0, 0,
                                     LocalDateTime.now(),
                                     LocalDateTime.now(), LocalDateTime.now(), "user", "group", 0, "some comments",
                            true,
                                     false,
                                     Collections.emptyMap());
        callback.setField(cell, info);
        Assertions.assertEquals("dir", cell.getText(), "Fields were not set as expected");
        Assertions.assertTrue(((ImageView)(cell.getGraphic())).getImage().getUrl().matches(".*folder.png"),
                              "Fields were not set as expected");

        info = new FileInfo(0, 0, "dir", 0, 0, 0,
                            LocalDateTime.now(),
                            LocalDateTime.now(), LocalDateTime.now(), "user", "group", 0, "some comments",
                            false,
                            false,
                            Collections.singletonMap(KEY_ICON_REF, "file://add.png"));
        callback.setField(cell, info);
        Assertions.assertEquals("dir", cell.getText(), "Fields were not set as expected");
        Assertions.assertTrue(((ImageView)(cell.getGraphic())).getImage().getUrl().matches(".*add.png"),
                              "Fields were not set as expected");
    }

    @Test
    @DisplayName("Test: Set Name field with folder icon successfully")
    public void testSetField_ValidFolder_MatchExpectations() {
        final TableCell<FileInfo,FileInfo> cell = new TableCell<>();
        FileInfo info = new FileInfo(0, 0, "dir", 0, 0, 0,
                            LocalDateTime.now(),
                            LocalDateTime.now(), LocalDateTime.now(), "user", "group", 0, "some comments",
                            true,
                            false,
                            Collections.emptyMap());
        callback.setField(cell, info);
        Assertions.assertEquals("dir", cell.getText(), "Fields were not set as expected");
        Assertions.assertTrue(((ImageView) (cell.getGraphic())).getImage()
                                                               .getUrl()
                                                               .matches(".*folder.png"),
                              "Fields were not set as expected");
    }

    @Test
    @DisplayName("Test: Set Name field with custom icon successfully")
    public void testSetField_ValidCustomIcon_MatchExpectations() {
        final TableCell<FileInfo,FileInfo> cell = new TableCell<>();
        FileInfo info = new FileInfo(0, 0, "dir", 0, 0, 0,
                            LocalDateTime.now(),
                            LocalDateTime.now(), LocalDateTime.now(), "user", "group", 0, "some comments",
                            false,
                            false,
                            Collections.singletonMap(KEY_ICON_REF, "file://add.png"));
        callback.setField(cell, info);
        Assertions.assertEquals("dir", cell.getText(), "Fields were not set as expected");
        Assertions.assertTrue(((ImageView)(cell.getGraphic())).getImage().getUrl().matches(".*add.png"),
                              "Fields were not set as expected");
    }
}
