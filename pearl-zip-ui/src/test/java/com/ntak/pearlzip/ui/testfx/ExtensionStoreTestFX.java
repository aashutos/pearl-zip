/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.testfx;

import com.ntak.pearlzip.ui.util.AbstractPearlZipTestFX;
import com.ntak.pearlzip.ui.util.ExtensionStoreEntry;
import com.ntak.testfx.FormUtil;
import javafx.geometry.Point2D;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableView;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.ntak.pearlzip.archive.constants.ArchiveConstants.WORKING_APPLICATION_SETTINGS;
import static com.ntak.pearlzip.ui.constants.ZipConstants.CNS_NTAK_PEARL_ZIP_RAW_VERSION;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Tag("UAT-ENV-DEP")
public class ExtensionStoreTestFX extends AbstractPearlZipTestFX {

    /*
     *  Test cases:
     *  + Search for all extension store entries successfully
     *  + Search filtered entries by Installed State successfully
     */

    @Test
    @DisplayName("Test: Search for all extension store entries successfully")
    public void testFX_SearchAllExtensionStore_Success() {
        try {
            System.setProperty(CNS_NTAK_PEARL_ZIP_RAW_VERSION, "0.0.5.0");
            WORKING_APPLICATION_SETTINGS.setProperty(CNS_NTAK_PEARL_ZIP_RAW_VERSION, "0.0.5.0");

            // Open up extension store...
            this.clickOn(Point2D.ZERO.add(160, 10))
                .clickOn(Point2D.ZERO.add(160, 60))
                .sleep(300, MILLISECONDS)

            // Click on search
                .clickOn("#btnSearch")
                .sleep(1000, MILLISECONDS);

            // Check entries...
            TableView<ExtensionStoreEntry> tblEntries = this.lookup("#tblStore").queryTableView();
            Assertions.assertNotEquals(0, tblEntries.getItems().size(), "Check entries retrieved from upstream DB");
        }
        finally {
            System.setProperty(CNS_NTAK_PEARL_ZIP_RAW_VERSION, "0.0.0.0");
            WORKING_APPLICATION_SETTINGS.setProperty(CNS_NTAK_PEARL_ZIP_RAW_VERSION, "0.0.0.0");
        }
    }

    @Test
    @DisplayName("Test: Search filtered entries by Installed State successfully")
    public void testFX_SearchFilteredExtensionStore_Success() {
        try {
            System.setProperty(CNS_NTAK_PEARL_ZIP_RAW_VERSION, "0.0.5.0");
            WORKING_APPLICATION_SETTINGS.setProperty(CNS_NTAK_PEARL_ZIP_RAW_VERSION, "0.0.5.0");

            // Open up extension store...
            this.clickOn(Point2D.ZERO.add(160, 10))
                .clickOn(Point2D.ZERO.add(160, 60))
                .sleep(300, MILLISECONDS);

            // filter by install state
            ChoiceBox<String> cmbInstallState = FormUtil.lookupNode((s) -> s.getTitle()
                                                                            .contains("Extension Store"), "#choiceInstallState");
            FormUtil.selectChoiceBoxEntry(this, cmbInstallState, "Installable");

            // Click on search
            this.clickOn("#btnSearch")
                .sleep(1000, MILLISECONDS);

            // Check entries...
            TableView<ExtensionStoreEntry> tblEntries = this.lookup("#tblStore")
                                                            .queryTableView();
            Assertions.assertNotEquals(0,
                                       tblEntries.getItems()
                                                 .size(),
                                       "Check entries retrieved from upstream DB");
            Assertions.assertEquals(0,
                                       tblEntries.getItems()
                                                 .stream()
                                                 .filter(r -> !r.installState().equals("Installable"))
                                                 .count(),
                                       "Check entries retrieved from upstream DB");
        } finally {
            System.setProperty(CNS_NTAK_PEARL_ZIP_RAW_VERSION, "0.0.0.0");
            WORKING_APPLICATION_SETTINGS.setProperty(CNS_NTAK_PEARL_ZIP_RAW_VERSION, "0.0.0.0");
        }
    }

}
