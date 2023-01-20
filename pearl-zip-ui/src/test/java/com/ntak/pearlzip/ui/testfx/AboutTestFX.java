/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.testfx;

import com.ntak.pearlzip.archive.model.LicenseInfo;
import com.ntak.pearlzip.archive.pub.LicenseService;
import com.ntak.pearlzip.license.pub.PearlZipLicenseService;
import com.ntak.pearlzip.ui.model.ZipState;
import com.ntak.pearlzip.ui.util.AbstractPearlZipTestFX;
import com.ntak.pearlzip.ui.util.JFXUtil;
import javafx.geometry.Point2D;
import javafx.scene.AccessibleAttribute;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class AboutTestFX extends AbstractPearlZipTestFX {
    /*
     *  Test cases:
     *  + Open about form, check labels and close
     *  + Open about form, open license screen and iterate through each license and ensure each license displayed
     *    correctly
     */

    @BeforeEach
    public void setUp() {
        // Load License Declarations
        LicenseService licenseService = new PearlZipLicenseService();
        licenseService.retrieveDeclaredLicenses()
                      .forEach(ZipState::addLicenseDeclaration);
    }

    @Test
    @DisplayName("Test: Open about form, check labels and close")
    public void testFX_OpenAbout_Success() {
        // Show about form...
        clickOn(Point2D.ZERO.add(80, 10)).clickOn(Point2D.ZERO.add(80, 30));
        sleep(250, MILLISECONDS);

        // Check labels...
        Label lblAppName = lookup("#lblAppName").queryAs(Label.class);
        Label lblVersion = lookup("#lblVersion").queryAs(Label.class);
        Label lblCopyright = lookup("#lblCopyright").queryAs(Label.class);
        Label lblWeblink = lookup("#lblWeblink").queryAs(Label.class);
        Label lblGeneral = lookup("#lblGeneral").queryAs(Label.class);

        Assertions.assertEquals("PearlZip", lblAppName.getText(), "Application Name did not match");
        Assertions.assertTrue(lblVersion.getText().matches(".*\\d\\.\\d\\.\\d\\.\\d.*"),
                              "Application version did not match");
        Assertions.assertEquals(String.format("\u00A9 2021-%s 92AK\nProgram written by Aashutos Kakshepati",
                                              LocalDate.now().getYear()),
                                lblCopyright.getText(),
                                "Copyright did not match");
        Assertions.assertEquals("https://pearlzip.92ak.co.uk", lblWeblink.getText(), "Weblink did not match");
        Assertions.assertEquals("BSD 3-Clause Open-source Licensed Software. Click dialog to close.", lblGeneral.getText(), "General text did not match");

    }

    @Test
    @DisplayName("Test: Open about form, check dependency licenses and close")
    public void testFX_OpenDependencyLicenses_Success() throws InterruptedException {
        // Show about form...
        clickOn(Point2D.ZERO.add(80, 10)).clickOn(Point2D.ZERO.add(80, 30));
        sleep(250, MILLISECONDS);

        // Open dependencies listings
        clickOn("#btnLicenseInfo");
        sleep(250, MILLISECONDS);

        // Iterate through each dependency and verify license contents...
        TableView<LicenseInfo> tblLicenses = lookup("#tblLicenseInfo").queryAs(TableView.class);
        int totalDependencies = tblLicenses.getItems().size();
        for (int i = 0; i < totalDependencies; i++) {
            tblLicenses.getSelectionModel()
                       .select(i);
            final LicenseInfo license = tblLicenses.getSelectionModel()
                                                   .getSelectedItem();
            System.out.printf("Selected: %s%n", license.canonicalName());
            TableRow<LicenseInfo> row =
                    ((TableCell<LicenseInfo,String>) tblLicenses.queryAccessibleAttribute(AccessibleAttribute.CELL_AT_ROW_COLUMN,
                                                                                          i, 0)).getTableRow();
            sleep(50, MILLISECONDS);
            final int finalI = i;
            CountDownLatch latch = new CountDownLatch(1);
            JFXUtil.runLater(() -> {
                try {
                    tblLicenses.scrollTo(finalI);
                } finally {
                    latch.countDown();
                }
            });
            latch.await();
            doubleClickOn(row);
            sleep(1, SECONDS);

            // Verify contents...
            final WebView page = lookup("#webLicense").queryAs(WebView.class);
            final Stage stage = (Stage) page.getScene()
                                            .getWindow();
            Assertions.assertEquals(String.format("License Details : %s", license.licenseFile()), stage.getTitle(),
                                    "License file was not as expected");

            // Close stage
            CountDownLatch scrollLatch = new CountDownLatch(1);
            JFXUtil.runLater(() -> {
                try {
                    stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
                } finally {
                    scrollLatch.countDown();
                }
            });
            scrollLatch.await();
            sleep(200, MILLISECONDS);
        }
    }
}
