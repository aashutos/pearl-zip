/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.pub;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.InstanceField;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;

import static com.ntak.pearlzip.ui.constants.ResourceConstants.DTF_YYYY;

public class FrmAboutControllerTest {
    private static FrmAboutController controller;
    private static Stage aboutStage;

    private static VBox vbAbout;
    private static Label lblAppName;
    private static Label lblVersion;
    private static Label lblCopyright;
    private static Label lblWeblink;
    private static Button btnLicenseInfo;

    private static CountDownLatch latch = new CountDownLatch(1);
    /*
        Test cases:
        + Initialise About Form. Ensure always on top and expected values have been set.
     */

    @BeforeAll
    public static void setUpOnce() throws InterruptedException, NoSuchFieldException {
        try {
            Platform.startup(() -> {
                aboutStage = new Stage();
                latch.countDown();
            });

        } catch (Exception e) {
            latch.countDown();
        } finally {
            latch.await();

            controller = new FrmAboutController();

            // Initialise objects
            vbAbout = new VBox();
            lblAppName = new Label();
            lblVersion = new Label();
            lblCopyright = new Label();
            lblWeblink = new Label();
            btnLicenseInfo = new Button();

            // Reflectively setting fields
            InstanceField fieldVbAbout = new InstanceField(FrmAboutController.class.getDeclaredField("vbAbout"),
                                                           controller);
            fieldVbAbout.set(vbAbout);
            InstanceField fieldLblAppName = new InstanceField(FrmAboutController.class.getDeclaredField("lblAppName"),
                                                           controller);
            fieldLblAppName.set(lblAppName);
            InstanceField fieldLblVersion = new InstanceField(FrmAboutController.class.getDeclaredField("lblVersion"),
                                                           controller);
            fieldLblVersion.set(lblVersion);
            InstanceField fieldLblCopyright = new InstanceField(FrmAboutController.class.getDeclaredField("lblCopyright"),
                                                           controller);
            fieldLblCopyright.set(lblCopyright);
            InstanceField fieldLblWeblink = new InstanceField(FrmAboutController.class.getDeclaredField("lblWeblink"),
                                                           controller);
            fieldLblWeblink.set(lblWeblink);
            InstanceField fieldBtnLicenseInfo = new InstanceField(FrmAboutController.class.getDeclaredField(
                    "btnLicenseInfo"),
                                                           controller);
            fieldBtnLicenseInfo.set(btnLicenseInfo);
        }
    }

    @Test
    @DisplayName("Test: Initialise About Form. Ensure always on top and expected values have been set")
    public void testInitialise_MatchExpectations() {
        controller.initialize();
        Assertions.assertEquals("PearlZip", lblAppName.getText(), "App Name not as expected");
        Assertions.assertEquals(String.format("© %s 92AK\nProgram written by Aashutos Kakshepati",
                                              LocalDate.now().format(
                                                              DTF_YYYY)), lblCopyright.getText(), "Copyright not as expected");
        Assertions.assertEquals("0.0.0.0", lblVersion.getText(), "Version not as expected");
        Assertions.assertEquals("https://pearlzip.92ak.co.uk", lblWeblink.getText(), "Weblink not as expected");

        aboutStage = Mockito.mock(Stage.class);
        controller.initData(aboutStage);
        Assertions.assertTrue(aboutStage.isAlwaysOnTop(), "Always On Top was not set to true");
    }
}
