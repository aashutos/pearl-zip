/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.pub;

import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.util.JFXUtil;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.junit.jupiter.api.*;
import org.mockito.internal.util.reflection.InstanceField;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.KEY_FILE_PATH;
import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.KEY_ICON_REF;

@Tag("Excluded")
public class FrmFileInfoControllerTest {

    private static CountDownLatch latch = new CountDownLatch(1);
    private static FrmFileInfoController controller;
    private static AtomicBoolean atoBool = new AtomicBoolean();
    private static Stage stage;
    private static FileInfo fileInfo;

    // General accordion page
    private static Label lblIndexValue;
    private static Label lblLevelValue;
    private static Label lblFilenameValue;
    private static Label lblHashValue;
    private static Label lblRawSizeValue;
    private static Label lblPackedSizeValue;
    private static Label lblFolderValue;
    private static Label lblEncryptValue;
    private static Label lblCommentsValue;

    // Timestamps accordion page
    private static Label lblLastWriteTimeValue;
    private static Label lblLastAccessTimeValue;
    private static Label lblCreateTimeValue;

    // Ownerships accordion page
    private static Label lblUserValue;
    private static Label lblGroupValue;

    // Other accordion page
    private static TableView<Pair<String,String>> tblOtherInfo;
    private static TableColumn<Pair<String,String>, String> key;
    private static TableColumn<Pair<String,String>, String> value;

    private static Button btnClose;
    private static Map<String,Object> parameters;

    /*
        Test cases:
        + Expected fields are presented from FileInfo
     */

    @BeforeAll
    public static void setUpOnce() throws InterruptedException, NoSuchFieldException {
        try {
            Platform.startup(() -> latch.countDown());
        } catch (Exception e) {
            latch.countDown();
        } finally {
            latch.await();
            controller = new FrmFileInfoController();
            CountDownLatch secondLatch = new CountDownLatch(1);
            JFXUtil.runLater(() -> {
                stage = new Stage();
                secondLatch.countDown();
            });
            secondLatch.await();

            // Set up FileInfo
            parameters = new HashMap<>();
            parameters.put(KEY_FILE_PATH, "/tmp/archive.zip");
            parameters.put(KEY_ICON_REF, "archive.png");
            fileInfo =  new FileInfo(1, 0, "archive.zip", 1257468032, 1024, 10240,
                                     LocalDateTime.of(1954,5,1,3,23,11),
                                     LocalDateTime.of(1995,2,3,4,5,46),
                                     LocalDateTime.of(1900,1,1,4,0,0),
                                     "user",
                                     "group",
                                     7, "A test file", false, false, parameters);

            // Initialise fields
            key = new TableColumn<>();
            value = new TableColumn<>();

            lblIndexValue = new Label();
            lblLevelValue = new Label();
            lblFilenameValue = new Label();
            lblHashValue = new Label();
            lblRawSizeValue = new Label();
            lblPackedSizeValue = new Label();
            lblFolderValue = new Label();
            lblEncryptValue = new Label();
            lblCommentsValue = new Label();

            lblLastWriteTimeValue = new Label();
            lblLastAccessTimeValue = new Label();
            lblCreateTimeValue = new Label();

            lblUserValue = new Label();
            lblGroupValue = new Label();

            tblOtherInfo = new TableView<>();

            btnClose = new Button();

            // Reflectively set fields
            InstanceField fieldKey = new InstanceField(FrmFileInfoController.class.getDeclaredField("key"),
                                                           controller);
            fieldKey.set(key);
            InstanceField fieldValue = new InstanceField(FrmFileInfoController.class.getDeclaredField("value"),
                                                      controller);
            fieldValue.set(value);

            InstanceField fieldLblIndexValue = new InstanceField(FrmFileInfoController.class.getDeclaredField(
                    "lblIndexValue"),
                                                         controller);
            fieldLblIndexValue.set(lblIndexValue);
            InstanceField fieldLblLevelValue = new InstanceField(FrmFileInfoController.class.getDeclaredField(
                    "lblLevelValue"),
                                                         controller);
            fieldLblLevelValue.set(lblLevelValue);
            InstanceField fieldLblFilenameValue = new InstanceField(FrmFileInfoController.class.getDeclaredField(
                    "lblFilenameValue"),
                                                         controller);
            fieldLblFilenameValue.set(lblFilenameValue);
            InstanceField fieldLblHashValue = new InstanceField(FrmFileInfoController.class.getDeclaredField(
                    "lblHashValue"),
                                                         controller);
            fieldLblHashValue.set(lblHashValue);
            InstanceField fieldLblRawSizeValue = new InstanceField(FrmFileInfoController.class.getDeclaredField(
                    "lblRawSizeValue"),
                                                         controller);
            fieldLblRawSizeValue.set(lblRawSizeValue);
            InstanceField fieldLblPackedSizeValue = new InstanceField(FrmFileInfoController.class.getDeclaredField(
                    "lblPackedSizeValue"),
                                                         controller);
            fieldLblPackedSizeValue.set(lblPackedSizeValue);
            InstanceField fieldLblFolderValue = new InstanceField(FrmFileInfoController.class.getDeclaredField("lblFolderValue"),
                                                         controller);
            fieldLblFolderValue.set(lblFolderValue);
            InstanceField fieldLblEncryptValue = new InstanceField(FrmFileInfoController.class.getDeclaredField(
                    "lblEncryptValue"),
                                                         controller);
            fieldLblEncryptValue.set(lblEncryptValue);
            InstanceField fieldLblCommentsValue = new InstanceField(FrmFileInfoController.class.getDeclaredField(
                    "lblCommentsValue"),
                                                         controller);
            fieldLblCommentsValue.set(lblCommentsValue);


            InstanceField fieldLblLastWriteTimeValue =
                    new InstanceField(FrmFileInfoController.class.getDeclaredField("lblLastWriteTimeValue"),
                                                         controller);
            fieldLblLastWriteTimeValue.set(lblLastWriteTimeValue);
            InstanceField fieldLblLastAccessTimeValue =
                    new InstanceField(FrmFileInfoController.class.getDeclaredField("lblLastAccessTimeValue"),
                                                         controller);
            fieldLblLastAccessTimeValue.set(lblLastAccessTimeValue);
            InstanceField fieldLblCreateTimeValue = new InstanceField(FrmFileInfoController.class.getDeclaredField(
                    "lblCreateTimeValue"), controller);
            fieldLblCreateTimeValue.set(lblCreateTimeValue);

            InstanceField fieldLblUserValue = new InstanceField(FrmFileInfoController.class.getDeclaredField("lblUserValue"),
                                                         controller);
            fieldLblUserValue.set(lblUserValue);
            InstanceField fieldLblGroupValue = new InstanceField(FrmFileInfoController.class.getDeclaredField("lblGroupValue"),
                                                         controller);
            fieldLblGroupValue.set(lblGroupValue);
            InstanceField fieldTblOtherInfo = new InstanceField(FrmFileInfoController.class.getDeclaredField(
                    "tblOtherInfo"), controller);
            fieldTblOtherInfo.set(tblOtherInfo);

            InstanceField fieldBtnClose = new InstanceField(FrmFileInfoController.class.getDeclaredField(
                    "btnClose"), controller);
            fieldBtnClose.set(btnClose);

            // Initialise
            controller.initialize();
        }
    }

    @Test
    @DisplayName("Test: Loading FileInfo form will correctly load all expected fields into the dialog")
    public void testInitData_ValidFileInfoParameter_MatchExpectations() throws InterruptedException {
        CountDownLatch testLatch = new CountDownLatch(1);
        JFXUtil.runLater(() -> {
            controller.initData(stage, fileInfo, atoBool);
            testLatch.countDown();
        });
        testLatch.await();

        Assertions.assertEquals("1", lblIndexValue.getText(), "Label lblIndexValue not set to the expected value");
        Assertions.assertEquals("0", lblLevelValue.getText(), "Label lblLevelValue not set to the expected value");
        Assertions.assertEquals("archive.zip", lblFilenameValue.getText(), "Label lblFilenameValue not set to the " +
                "expected value");
        Assertions.assertEquals(String.format("0x%s",Long.toHexString(1257468032).toUpperCase()), lblHashValue.getText(), "Label" +
                " " +
            "lblHashValue not " +
                "set to the expected value");
        Assertions.assertEquals("10240", lblRawSizeValue.getText(), "Label lblRawSizeValue not set to the expected " +
                "value");
        Assertions.assertEquals("1024", lblPackedSizeValue.getText(), "Label lblPackedSizeValue not set to the " +
                "expected value");
        Assertions.assertEquals("file", lblFolderValue.getText(), "Label lblFolderValue not set to the expected " +
                "value");
        Assertions.assertEquals("plaintext", lblEncryptValue.getText(), "Label lblEncryptValue not set to the expected " +
                "value");
        Assertions.assertEquals("A test file", lblCommentsValue.getText(), "Label lblCommentsValue not set to the expected value");

        Assertions.assertEquals(LocalDateTime.of(1954,5,1,3,23,11).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), lblLastWriteTimeValue.getText(),
                                "Label lblLastWriteTimeValue not set to the expected value");
        Assertions.assertEquals(LocalDateTime.of(1995,2,3,4,5,46).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), lblLastAccessTimeValue.getText(),
                                "Label lblLastAccessTimeValue not set to the expected value");
        Assertions.assertEquals(LocalDateTime.of(1900,1,1,4,0,0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), lblCreateTimeValue.getText(),
                                "Label lblCreateTimeValue not set to the expected value");

        Assertions.assertEquals("user", lblUserValue.getText(), "Label lblUserValue not set to the expected value");
        Assertions.assertEquals("group", lblGroupValue.getText(), "Label lblGroupValue not set to the expected value");

        for (Pair<String,String> param : tblOtherInfo.getItems()) {
            Assertions.assertTrue(parameters.containsKey(param.getKey()), String.format("Property %s not found",
                                                                                        param.getKey()));
            Assertions.assertEquals(parameters.get(param.getKey()), param.getValue(),
                                    String.format("Property %s with unexpected value %s", param.getKey(), param.getValue()));
        }
    }
}
