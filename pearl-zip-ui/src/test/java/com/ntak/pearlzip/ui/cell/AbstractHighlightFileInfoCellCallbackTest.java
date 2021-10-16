/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.cell;

import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.model.FXMigrationInfo;
import com.ntak.pearlzip.ui.util.JFXUtil;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

import static com.ntak.pearlzip.ui.constants.ResourceConstants.DEFAULT_HIGHLIGHT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@Tag("Excluded")
public class AbstractHighlightFileInfoCellCallbackTest {

    private static AbstractHighlightFileInfoCellCallback callback;
    private static TableColumn<FileInfo,FileInfo> param = new TableColumn<>();
    private FileInfo info = new FileInfo(0, 0, "filename", 0, 0, 0, LocalDateTime.now(), LocalDateTime.now(),
                                 LocalDateTime.now(), "user", "group", 0, "some comments", false, false, Collections.emptyMap());
    private static Stage stage;
    private static TableView tableView;
    private static Scene scene;
    private static FXArchiveInfo mockArchiveInfo;
    private static FXMigrationInfo mockMigrationInfo;

     /*
         Test cases:
         + Call method execution null values in parameter object
         + Call method with values - ensure setField called, no migration
         + Call method with values - ensure setField called, migration highlight call
     */

    @BeforeAll
    public static void setUpOnce() throws InterruptedException {
        try {
            callback = Mockito.spy(AbstractHighlightFileInfoCellCallback.class);
            mockArchiveInfo = Mockito.mock(FXArchiveInfo.class);
            mockMigrationInfo = Mockito.mock(FXMigrationInfo.class);
            System.setProperty("glass.platform", "Monocle");
            System.setProperty("testfx.headless", "true");
            System.setProperty("monocle.platform", "Headless");
            System.setProperty("prism.order", "sw");

            final CountDownLatch firstLatch = new CountDownLatch(1);
            Platform.startup(()->{
                stage = new Stage();
                tableView = new TableView();
                scene = new Scene(tableView);
                stage.setScene(scene);
                firstLatch.countDown();
            });
            firstLatch.await();
        } catch (Exception e) {
            final CountDownLatch secondLatch = new CountDownLatch(1);
            JFXUtil.runLater(() -> {
                stage = new Stage();
                tableView = new TableView();
                scene = new Scene(tableView);
                stage.setScene(scene);
                secondLatch.countDown();
            });
            secondLatch.await();
        }
    }

    @AfterAll
    public static void tearDownOnce() {
    }

    @AfterEach
    public void tearDown() {
        Mockito.reset(callback, mockArchiveInfo);
    }

    @Test
    @DisplayName("Test: Call method execution null values in parameter object")
    public void testCall_EmptyParameter_MatchExpectations() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        TableCell<FileInfo,FileInfo> cell = callback.call(param);
        cell.updateTableView(tableView);
        Assertions.assertNotNull(cell, "The generated table cell was null");
        cell.getClass().getMethod("updateItem", FileInfo.class, boolean.class).invoke(cell,null,false);
        Mockito.verify(callback, never()).setField(any(TableCell.class), any(FileInfo.class));
        Assertions.assertNull(cell.getBackground(), "A background was unexpectedly set");
    }

    @Test
    @DisplayName("Test: Call method with values - ensure setField called, no migration")
    public void testCall_NoneEmptyParameterNoMigration_MatchExpectations() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        when(mockArchiveInfo.getMigrationInfo()).thenReturn(mockMigrationInfo);
        when(mockMigrationInfo.getType()).thenReturn(FXMigrationInfo.MigrationType.NONE);
        stage.setUserData(mockArchiveInfo);
        TableCell<FileInfo,FileInfo> cell = callback.call(param);
        cell.updateTableView(tableView);
        Assertions.assertNotNull(cell, "The generated table cell was null");
        cell.getClass().getMethod("updateItem", FileInfo.class, boolean.class).invoke(cell,info,false);
        Mockito.verify(callback, Mockito.times(1)).setField(any(TableCell.class), any(FileInfo.class));
        Assertions.assertNull(cell.getBackground(), "A background was unexpectedly set");
    }

    @Test
    @DisplayName("Test: Call method with values - ensure setField called, no migration")
    public void testCall_NoneEmptyParameterMigration_MatchExpectations() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        when(mockArchiveInfo.getMigrationInfo()).thenReturn(mockMigrationInfo);
        when(mockMigrationInfo.getFile()).thenReturn(info);
        when(mockMigrationInfo.getType()).thenReturn(FXMigrationInfo.MigrationType.COPY);
        stage.setUserData(mockArchiveInfo);
        TableCell<FileInfo,FileInfo> cell = callback.call(param);
        cell.updateTableView(tableView);
        Assertions.assertNotNull(cell, "The generated table cell was null");
        cell.getClass().getMethod("updateItem", FileInfo.class, boolean.class).invoke(cell,info,false);
        Mockito.verify(callback, Mockito.times(1)).setField(any(TableCell.class), any(FileInfo.class));
        Assertions.assertEquals(DEFAULT_HIGHLIGHT, cell.getBackground().getFills().get(0), "Background fill does not match");
    }
}
