/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.testfx;

import com.jfoenix.controls.JFXSnackbar;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.constants.ZipConstants;
import com.ntak.pearlzip.ui.util.AbstractPearlZipTestFX;
import javafx.scene.control.TableRow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;

import static com.ntak.pearlzip.ui.util.PearlZipFXUtil.simOpenArchive;
import static com.ntak.pearlzip.ui.util.PearlZipFXUtil.simTraversalArchive;

public class MainTestFX extends AbstractPearlZipTestFX {

    /*
     *  Test cases:
     *  + Resize main window
     *  + Check Toast notification appears on navigation to sub-folder in archive
     */

    @Test
    @DisplayName("Test: Resize main window")
    public void testFX_ResizeMainWindow_Success() {
        System.setProperty(ZipConstants.CNS_NTAK_PEARL_ZIP_RESIZEABLE, "true");

        VBox wrapper = this.lookup("#wrapper").queryAs(VBox.class);
        final double origWidth = wrapper.getScene()
                                    .getWindow()
                                    .getWidth();
        final double origHeight = wrapper.getScene()
                                         .getWindow()
                                         .getHeight();

        double x = (wrapper.getScene().getWindow().getX() + origWidth);
        double y = (wrapper.getScene().getWindow().getY() + origHeight);

        this.moveTo(0,0)
            .moveTo(x,y)
            .sleep(300,TimeUnit.MILLISECONDS)
            .press(MouseButton.PRIMARY)
            .moveBy(100,100)
            .sleep(300,TimeUnit.MILLISECONDS)
            .release(MouseButton.PRIMARY)
            .sleep(200,TimeUnit.MILLISECONDS);

        Assertions.assertEquals(origWidth + 100, wrapper.getScene().getWindow().getWidth(),
                                "Width did not change to expected value");
        Assertions.assertEquals(origHeight + 100, wrapper.getScene().getWindow().getHeight(),
                                "Height did not change to expected value");
    }

    @Test
    @DisplayName("Test: Check Toast notification appears on navigation to sub-folder in archive")
    public void testFX_DisplayNotification_DirectoryChange_MatchExpectations() throws IOException {
        final Path srcArchivePath = Paths.get("src", "test", "resources", "test.zip")
                                  .toAbsolutePath();
        final Path archivePath = Files.createTempDirectory("pz")
                             .resolve(srcArchivePath.getFileName());
        Files.copy(srcArchivePath,
                   archivePath,
                   StandardCopyOption.REPLACE_EXISTING
        );

        // Set snack bar timeout to arbitrary long value
        System.setProperty(ZipConstants.CNS_NTAK_PEARL_ZIP_TOAST_DURATION, "100000");

        // Open test.zip
        simOpenArchive(this, archivePath, true, false);

        // Initialisation checks...
        Assertions.assertFalse(this.lookup("#toast").queryAs(JFXSnackbar.class).isVisible(),
                              "Snackbar notification is not visible");

        // Navigate to first-folder
        TableRow<FileInfo> row = simTraversalArchive(this, archivePath.getFileName().toString(), "#fileContentsView",
                                                     (r)->{},
                                                     "first-folder").get();

        this.clickOn(row)
            .sleep(500, TimeUnit.MILLISECONDS);

        // Check Snack bar displays itself
        Assertions.assertTrue(this.lookup("#toast").queryAs(JFXSnackbar.class).isVisible(),
                              "Snackbar notification is not visible");
    }
}
