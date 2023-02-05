/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.testfx;

import com.ntak.pearlzip.archive.model.LicenseInfo;
import com.ntak.pearlzip.archive.pub.LicenseService;
import com.ntak.pearlzip.license.pub.PearlZipLicenseService;
import com.ntak.pearlzip.ui.model.ZipState;
import com.ntak.pearlzip.ui.util.AbstractPearlZipTestFX;
import com.ntak.testfx.TestFXConstants;
import javafx.geometry.Point2D;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;

import java.time.LocalDate;

import static com.ntak.testfx.specifications.CommonSpecificationsUtil.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

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
    // GIVEN the about form is open
    // THEN Label <label> has value <value>
    public void testFX_OpenAbout_Success() {
        // Show about form...
        givenAboutFormIsOpen(this);

        // Check labels...
        thenLabelOnActiveFormHasValue(this, "#lblAppName", "PearlZip");
        thenLabelOnActiveFormMatchesPattern(this, "#lblVersion", ".*\\d\\.\\d\\.\\d\\.\\d.*");
        thenLabelOnActiveFormHasValue(this, "#lblCopyright", String.format("\u00A9 2021-%s 92AK\nProgram written by Aashutos Kakshepati",
                                                                                                    LocalDate.now().getYear()));
        thenLabelOnActiveFormHasValue(this, "#lblWeblink", "https://pearlzip.92ak.co.uk");
        thenLabelOnActiveFormHasValue(this, "#lblGeneral", "BSD 3-Clause Open-source Licensed Software. Click dialog to close.");
    }

    @Test
    @DisplayName("Test: Open about form, check dependency licenses and close")
    // GIVEN the about form is open
    //       AND license info dialog is open
    // THEN EACH dependency license is displayed with the expected license file
    public void testFX_OpenDependencyLicenses_Success() throws InterruptedException {
        // Show about form...
        givenAboutFormIsOpen(this);

        // Open dependencies listings
        givenLicenseInfoDialogIsOpen(this);

        // Iterate through each dependency and verify license contents...
        thenEachDependencyLicenseIsDisplayedWithExpectedLicenseFile(this);
    }

    ////////////////////////////////
    // Behavioural Specifications //
    ////////////////////////////////
    public static void givenLicenseInfoDialogIsOpen(FxRobot fxRobot) {
        fxRobot.clickOn("#btnLicenseInfo");
        fxRobot.sleep(250, MILLISECONDS);
    }

    public static void givenAboutFormIsOpen(FxRobot fxRobot) {
        fxRobot.clickOn(Point2D.ZERO.add(80, 10)).clickOn(Point2D.ZERO.add(80, 30));
        fxRobot.sleep(250, MILLISECONDS);
    }

    public static void thenEachDependencyLicenseIsDisplayedWithExpectedLicenseFile(FxRobot fxRobot) throws InterruptedException {
        TableView<LicenseInfo> tblLicenses = fxRobot.lookup("#tblLicenseInfo").queryAs(TableView.class);
        int totalDependencies = tblLicenses.getItems().size();
        for (int i = 0; i < totalDependencies; i++) {
            TableRow<LicenseInfo> row = thenSelectEntryFromTableView(fxRobot, tblLicenses, i);

            fxRobot.doubleClickOn(row);
            fxRobot.sleep(TestFXConstants.LONG_PAUSE);

            // Verify contents...
            final WebView page = fxRobot.lookup("#webLicense").queryAs(WebView.class);
            thenPropertyEqualsValue(page,
                                                             (w) -> ((Stage)page.getScene().getWindow()).getTitle(),
                                                             String.format("License Details : %s", row.getItem().licenseFile())
            );
            // Close stage
            thenCloseStage(fxRobot, (Stage) page.getScene().getWindow());
        }
    }
}
