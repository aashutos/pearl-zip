/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.testfx;

import com.ntak.pearlzip.ui.util.AbstractPearlZipTestFX;
import com.ntak.pearlzip.ui.util.ExtensionStoreEntry;
import com.ntak.pearlzip.ui.util.PearlZipSpecifications;
import com.ntak.testfx.FormUtil;
import com.ntak.testfx.specifications.CommonSpecifications;
import javafx.scene.control.ChoiceBox;
import org.junit.jupiter.api.*;

import java.util.List;

import static com.ntak.pearlzip.archive.constants.ArchiveConstants.WORKING_APPLICATION_SETTINGS;
import static com.ntak.pearlzip.ui.constants.ZipConstants.CNS_NTAK_PEARL_ZIP_RAW_VERSION;
import static com.ntak.testfx.TestFXConstants.LONG_PAUSE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Tag("UATExtensionStore")
public class ExtensionStoreTestFX extends AbstractPearlZipTestFX {

    /*
     *  Test cases:
     *  + Search for all extension store entries successfully
     *  + Search filtered entries by Installed State successfully
     */

    @BeforeEach
    public void setUp() {
        System.setProperty(CNS_NTAK_PEARL_ZIP_RAW_VERSION, "0.0.5.0");
        WORKING_APPLICATION_SETTINGS.setProperty(CNS_NTAK_PEARL_ZIP_RAW_VERSION, "0.0.5.0");
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
        System.setProperty(CNS_NTAK_PEARL_ZIP_RAW_VERSION, "0.0.0.0");
        WORKING_APPLICATION_SETTINGS.setProperty(CNS_NTAK_PEARL_ZIP_RAW_VERSION, "0.0.0.0");
    }

    @Test
    @DisplayName("Test: Search for all extension store entries successfully")
    /*
     *  GIVEN extension store is open
     *  WHEN search button is pressed
     *  THEN ensure table (#tblStore) is not empty
     */
    public void testFX_SearchAllExtensionStore_Success() {
        // Given
        PearlZipSpecifications.givenExtensionStoreOpened(this);

        // When
        this.clickOn("#btnSearch")
            .sleep(LONG_PAUSE, MILLISECONDS);

        // Then
        List<ExtensionStoreEntry> items = CommonSpecifications.thenExtractEntriesFromTable(this, "#tblStore");
        Assertions.assertNotEquals(0, items.size(), "Check entries retrieved from upstream DB");
    }

    @Test
    @DisplayName("Test: Search filtered entries by Installed State successfully")
    /*
     *  GIVEN extension store is open
     *  WHEN combo box (#choiceInstallState) option: Installable is selected
     *      AND search button is pressed
     *  THEN ensure table (#tblStore) is not empty
     *      AND ensure table (#tblStore) has entries with install state = Installable
     */
    public void testFX_SearchFilteredExtensionStore_Success() {
        // Given
        PearlZipSpecifications.givenExtensionStoreOpened(this);

        // When
        ChoiceBox<String> cmbInstallState = FormUtil.lookupNode((s) -> s.getTitle()
                                                                        .contains("Extension Store"), "#choiceInstallState");
        FormUtil.selectChoiceBoxEntry(this, cmbInstallState, "Installable");
        this.clickOn("#btnSearch")
            .sleep(LONG_PAUSE, MILLISECONDS);

        // Then
        List<ExtensionStoreEntry> items = CommonSpecifications.thenExtractEntriesFromTable(this, "#tblStore");
        Assertions.assertNotEquals(0, items.size(), "Check entries retrieved from upstream DB");
        Assertions.assertTrue(items.stream().allMatch(e-> "Installable".equals(e.installState())), "Not all entries in #tblStore was in the expected state");
    }
}
