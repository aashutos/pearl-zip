/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.pub;

import com.ntak.pearlzip.archive.constants.LoggingConstants;
import com.ntak.pearlzip.archive.pub.ArchiveReadService;
import com.ntak.pearlzip.archive.pub.ArchiveServiceProfile;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.archive.pub.profile.component.ReadServiceComponent;
import com.ntak.pearlzip.archive.pub.profile.component.WriteServiceComponent;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.model.FXMigrationInfo;
import com.ntak.pearlzip.ui.model.ZipState;
import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.InstanceField;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static com.ntak.pearlzip.archive.util.LoggingUtil.genLocale;
import static org.mockito.Mockito.when;

@Tag("Excluded")
public class ContextMenuControllerTest {

    private static ContextMenuController controller;
    static ContextMenu ctxMenu;
    private static MenuItem mnuPeek;
    private static MenuItem mnuExtract;
    private static MenuItem mnuCopy;
    private static MenuItem mnuMove;
    private static MenuItem mnuDelete;
    private static MenuItem mnuFileInfo;
    private static FileInfo fileInfo;
    private static TableRow<FileInfo> row;
    private static FXMigrationInfo migrationInfo = new FXMigrationInfo();

    private static FXArchiveInfo mockArchiveInfo;
    private static FrmMainController mockMainController;
    private static ArchiveReadService mockArchiveReadService;
    private static ArchiveServiceProfile archiveServiceProfile = new ArchiveServiceProfile("test-provider");

    private static CountDownLatch latch = new CountDownLatch(1);

    /*
        Test cases:
        + Context menu options set up on COPY mode
        + Context menu options set up on MOVE mode
        + Context menu options set up on DELETE mode
        + Context menu options set up on default mode
        + Context menu options set up on default mode - with compressor archive (only valid migration state)
        + Controller not assigned case (on default mode)
     */

    @BeforeAll
    public static void setUpOnce() throws NoSuchFieldException, InterruptedException {
        try {
            Platform.startup(() -> latch.countDown());
        } catch (Exception e) {
            latch.countDown();
        } finally {
            latch.await();

            Locale defaultLocale = genLocale(new Properties());
            LoggingConstants.LOG_BUNDLE = ResourceBundle.getBundle("pearlzip",
                                                                   defaultLocale);
            LoggingConstants.CUSTOM_BUNDLE = ResourceBundle.getBundle("custom",
                                                                      defaultLocale);
            controller = new ContextMenuController();
            ctxMenu = new ContextMenu();
            fileInfo =  new FileInfo(1, 0, "file", 0, 0, 0,
                                     LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "user", "group",
                                     0, "", false, false, Collections.emptyMap());
            row = new TableRow<>();
            row.setItem(fileInfo);

            archiveServiceProfile.addComponent(new WriteServiceComponent(Set.of("zip", "rar", "tar"), Collections.emptyMap()));
            archiveServiceProfile.addComponent(new ReadServiceComponent(Set.of("zip", "rar", "tar", "cab", "iso"), Collections.emptyMap()));

            // Initialise mocks...
            mockArchiveInfo = Mockito.mock(FXArchiveInfo.class);
            mockMainController = Mockito.mock(FrmMainController.class);
            mockArchiveReadService = Mockito.spy(ArchiveReadService.class);

            // initialise stubs...
            when(mockArchiveInfo.getController()).thenReturn(Optional.empty());
            when(mockArchiveInfo.getMigrationInfo()).thenReturn(migrationInfo);
            when(mockArchiveReadService.getArchiveServiceProfile()).thenReturn(archiveServiceProfile);
            when(mockArchiveReadService.getCompressorArchives()).thenCallRealMethod();
            ZipState.addArchiveProvider(mockArchiveReadService);

            // Initialisation of fields in controller...
            final Field fCtxMenu = ContextMenuController.class.getDeclaredField("ctxMenu");
            InstanceField ifCtxMenu = new InstanceField(fCtxMenu, controller);
            ifCtxMenu.set(ctxMenu);
        }
    }

    @BeforeEach
    public void setUp() throws NoSuchFieldException {
        // Set up of menu items
        mnuPeek = new MenuItem();
        mnuExtract = new MenuItem();
        mnuCopy = new MenuItem();
        mnuMove = new MenuItem();
        mnuDelete = new MenuItem();
        mnuFileInfo = new MenuItem();

        // Validate menu items
        Assertions.assertNull(mnuPeek.getOnAction(), "mnuPeek onAction was not initialised as expected");
        Assertions.assertNull(mnuExtract.getOnAction(), "mnuExtract onAction was not initialised as expected");
        Assertions.assertNull(mnuCopy.getOnAction(), "mnuCopy onAction was not initialised as expected");
        Assertions.assertNull(mnuMove.getOnAction(), "mnuMove onAction was not initialised as expected");
        Assertions.assertNull(mnuDelete.getOnAction(), "mnuDelete onAction was not initialised as expected");
        Assertions.assertNull(mnuFileInfo.getOnAction(), "mnuFileInfo onAction was not initialised as expected");

        // Initialise in Controller
        final Field mnuPeek = ContextMenuController.class.getDeclaredField("mnuPeek");
        InstanceField fieldMnuPeek = new InstanceField(mnuPeek, controller);
        fieldMnuPeek.set(ContextMenuControllerTest.mnuPeek);

        final Field mnuExtract = ContextMenuController.class.getDeclaredField("mnuExtract");
        InstanceField fieldMnuExtract = new InstanceField(mnuExtract, controller);

        fieldMnuExtract.set(ContextMenuControllerTest.mnuExtract);
        InstanceField fieldMnuCopy = new InstanceField(ContextMenuController.class.getDeclaredField("mnuCopy"),
                                                       controller);
        fieldMnuCopy.set(mnuCopy);
        InstanceField fieldMnuMove = new InstanceField(ContextMenuController.class.getDeclaredField("mnuMove"),
                                                       controller);
        fieldMnuMove.set(mnuMove);
        InstanceField fieldMnuDelete = new InstanceField(ContextMenuController.class.getDeclaredField("mnuDelete"),
                                                         controller);
        fieldMnuDelete.set(mnuDelete);
        InstanceField fieldMnuFileInfo = new InstanceField(ContextMenuController.class.getDeclaredField("mnuFileInfo"),
                                                           controller);
        fieldMnuFileInfo.set(mnuFileInfo);

        when(mockArchiveInfo.getController()).thenReturn(Optional.of(mockMainController));
        when(mockArchiveInfo.getArchivePath()).thenReturn("src/test/resources/test.zip");

        migrationInfo.clear();
    }

    @Test
    @DisplayName("Test: When Context Menu is opened in COPY mode, the expected options are set")
    public void testInitContextMenu_COPYMode_MatchExpectations() {
        // Initialise
        controller.initData(mockArchiveInfo, row);

        // Run after COPY Migration activated
        migrationInfo.initMigration(FXMigrationInfo.MigrationType.COPY, fileInfo);
        ctxMenu.onShowingProperty().get().handle(null);

        Assertions.assertTrue(mnuMove.isDisable(), "Move option was not disabled");
        Assertions.assertEquals("Move", mnuMove.getText(), "Move text was incorrect");
        Assertions.assertFalse(mnuCopy.isDisable(), "Copy option was disabled");
        Assertions.assertEquals("Paste", mnuCopy.getText(), "Copy text was incorrect");
        Assertions.assertTrue(mnuDelete.isDisable(), "Delete option was not disabled");
    }

    @Test
    @DisplayName("Test: When Context Menu is opened in MOVE mode, the expected options are set")
    public void testInitContextMenu_MOVEMode_MatchExpectations() {
        // Initialise
        controller.initData(mockArchiveInfo, row);

        // Run after MOVE Migration activated
        migrationInfo.initMigration(FXMigrationInfo.MigrationType.MOVE, fileInfo);
        ctxMenu.onShowingProperty().get().handle(null);

        Assertions.assertFalse(mnuMove.isDisable(), "Move option was disabled");
        Assertions.assertEquals("Drop", mnuMove.getText(), "Move text was incorrect");
        Assertions.assertTrue(mnuCopy.isDisable(), "Copy option was not disabled");
        Assertions.assertEquals("Copy", mnuCopy.getText(), "Copy text was incorrect");
        Assertions.assertTrue(mnuDelete.isDisable(), "Delete option was not disabled");
    }

    @Test
    @DisplayName("Test: When Context Menu is opened in DELETE mode, the expected options are set")
    public void testInitContextMenu_DELETEMode_MatchExpectations() {
        // Initialise
        controller.initData(mockArchiveInfo, row);

        // Run after DELETE Migration activated
        migrationInfo.initMigration(FXMigrationInfo.MigrationType.DELETE, fileInfo);
        ctxMenu.onShowingProperty().get().handle(null);

        Assertions.assertTrue(mnuMove.isDisable(), "Move option was not disabled");
        Assertions.assertEquals("Move", mnuMove.getText(), "Move text was incorrect");
        Assertions.assertTrue(mnuCopy.isDisable(), "Copy option was not disabled");
        Assertions.assertEquals("Copy", mnuCopy.getText(), "Copy text was incorrect");
        Assertions.assertFalse(mnuDelete.isDisable(), "Delete option was disabled");
    }

    @Test
    @DisplayName("Test: When Context Menu is opened in default mode, the expected options are set")
    public void testInitContextMenu_DefaultMode_MatchExpectations() {
        // Initialise
        controller.initData(mockArchiveInfo, row);
        ctxMenu.onShowingProperty().get().handle(null);

        Assertions.assertFalse(mnuMove.isDisable(), "Move option was not disabled");
        Assertions.assertEquals("Move", mnuMove.getText(), "Move text was incorrect");
        Assertions.assertFalse(mnuCopy.isDisable(), "Copy option was not disabled");
        Assertions.assertEquals("Copy", mnuCopy.getText(), "Copy text was incorrect");
        Assertions.assertFalse(mnuDelete.isDisable(), "Delete option was disabled");
    }

    @Test
    @DisplayName("Test: When Context Menu is opened in default mode and is a compressor archive, the expected options" +
            " are set")
    public void testInitContextMenu_DefaultModeCompressorArchive_MatchExpectations() {
        // Initialise
        when(mockArchiveInfo.getArchivePath()).thenReturn("src/test/resources/test.tar.gz");
        controller.initData(mockArchiveInfo, row);
        ctxMenu.onShowingProperty().get().handle(null);

        Assertions.assertTrue(mnuMove.isDisable(), "Move option was not disabled");
        Assertions.assertEquals("Move", mnuMove.getText(), "Move text was incorrect");
        Assertions.assertTrue(mnuCopy.isDisable(), "Copy option was not disabled");
        Assertions.assertEquals("Copy", mnuCopy.getText(), "Copy text was incorrect");
        Assertions.assertTrue(mnuDelete.isDisable(), "Delete option was disabled");
    }

    @Test
    @DisplayName("Test: When Context Menu is opened with no main controller assigned, the expected options are set")
    public void testInitContextMenu_NoMainController_MatchExpectations() {
        // Initialise
        when(mockArchiveInfo.getController()).thenReturn(Optional.empty());
        controller.initData(mockArchiveInfo, row);

        Assertions.assertTrue(mnuMove.isDisable(), "Move option was not disabled");
        Assertions.assertTrue(mnuCopy.isDisable(), "Copy option was not disabled");
        Assertions.assertTrue(mnuDelete.isDisable(), "Delete option was disabled");
    }
}
