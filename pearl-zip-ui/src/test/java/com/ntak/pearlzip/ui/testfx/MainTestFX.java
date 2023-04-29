/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.testfx;

import com.ntak.pearlzip.ui.constants.ZipConstants;
import com.ntak.pearlzip.ui.util.AbstractPearlZipTestFX;
import com.ntak.pearlzip.ui.util.PearlZipSpecifications;
import com.ntak.testfx.specifications.CommonSpecifications;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static com.ntak.pearlzip.ui.util.PearlZipFXUtil.simOpenArchive;

public class MainTestFX extends AbstractPearlZipTestFX {

    /*
     *  Test cases:
     *  + Resize main window
     *  + Check Toast notification appears on navigation to sub-folder in archive
     */

    @Test
    @DisplayName("Test: Resize main window")
    // GIVEN resize functionality is enabled
    // WHEN main dialog resized by (100,100) pixels
    // THEN main dialog has been resized to the expected dimensions
    public void testFX_ResizeMainWindow_Success() {
        String resizeableStr = System.getProperty(ZipConstants.CNS_NTAK_PEARL_ZIP_RESIZEABLE);

        try {
            // Given
            System.setProperty(ZipConstants.CNS_NTAK_PEARL_ZIP_RESIZEABLE, "true");
            VBox wrapper = this.lookup("#wrapper")
                               .queryAs(VBox.class);

            // When
            final Window window = wrapper.getScene().getWindow();
            final double origWidth = window.getWidth();
            final double origHeight = window.getHeight();
            int xOffset = 100;
            int yOffset = 100;
            CommonSpecifications.whenWindowResized(this,
                                                   window,
                                                   xOffset,
                                                   yOffset);

            // Then
            CommonSpecifications.thenWindowHasDimensions(window, origWidth + xOffset, origHeight + yOffset);
        } finally {
            System.setProperty(ZipConstants.CNS_NTAK_PEARL_ZIP_RESIZEABLE, resizeableStr);
        }
    }

    @Test
    @DisplayName("Test: Check Toast notification appears on navigation to sub-folder in archive")
    // GIVEN a copy of zip archive (test.zip) is open in PearlZip
    //     AND property configuration.ntak.pearl-zip.toast-duration set to 100000
    //     AND snackbar is not visible
    // WHEN select file (first-folder)
    // THEN ensure snackbar toast message is visible
    public void testFX_DisplayNotification_DirectoryChange_MatchExpectations() throws IOException {
        String origProperty = System.getProperty(ZipConstants.CNS_NTAK_PEARL_ZIP_TOAST_DURATION);
        try {
            // Given
            // Set snack bar timeout to arbitrary long value
            System.setProperty(ZipConstants.CNS_NTAK_PEARL_ZIP_TOAST_DURATION, "100000");

            final Path srcArchivePath = Paths.get("src", "test", "resources", "test.zip")
                                             .toAbsolutePath();
            final Path archivePath = Files.createTempDirectory("pz")
                                          .resolve(srcArchivePath.getFileName());
            Files.copy(srcArchivePath,
                       archivePath,
                       StandardCopyOption.REPLACE_EXISTING
            );

            // Open test.zip
            simOpenArchive(this, archivePath, true, false);
            CommonSpecifications.thenExpectNodeVisibility(this, "#toast", false);

            // When
            PearlZipSpecifications.whenEntrySelectedInCurrentWindow(this, "first-folder");
            this.doubleClickOn(MouseButton.PRIMARY);

            // Then
            CommonSpecifications.thenExpectNodeVisibility(this, "#toast", true);
        } finally {
            if (origProperty != null) {
                System.setProperty(ZipConstants.CNS_NTAK_PEARL_ZIP_TOAST_DURATION, origProperty);
            } else {
                System.clearProperty(ZipConstants.CNS_NTAK_PEARL_ZIP_TOAST_DURATION);
            }
        }
    }
}
