/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.testfx;

import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.util.AbstractPearlZipTestFX;
import com.ntak.pearlzip.ui.util.PearlZipSpecifications;
import javafx.scene.control.TableRow;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.ntak.pearlzip.ui.util.PearlZipFXUtil.simFileInfo;
import static com.ntak.pearlzip.ui.util.PearlZipFXUtil.simOpenArchive;

public class FileInfoTestFX extends AbstractPearlZipTestFX {

    /*
     *  Test cases:
     *  + File Information dialog displayed for a file contains the expected information
     *  + File Information dialog displayed for a folder contains the expected information
     */

    @AfterEach
    public void tearDown() throws IOException {

    }

    @Test
    @DisplayName("Test: File Information dialog displayed for a file contains the expected information")
    // GIVEN zip archive (test.zip) is open in PearlZip
    // WHEN select file (first-file)
    //     AND file Information is displayed for file (first-file)
    // THEN ensure details match expectations
    public void testFX_FileInfoForFile_MatchExpectations() {
        final Path archivePath = Paths.get("src", "test", "resources", "test.zip")
                                      .toAbsolutePath();

        // Given
        simOpenArchive(this, archivePath.toAbsolutePath(), true, false);

        // When
        TableRow<FileInfo> row = PearlZipSpecifications.whenEntrySelectedInCurrentWindow(this, "first-file");
        simFileInfo(this);

        // Then
        FileInfo info = row.getItem();
        PearlZipSpecifications.thenFileInfoScreenContentsMatchesFileMetaData(this, info);
    }

    @Test
    @DisplayName("Test: File Information dialog displayed for a folder contains the expected information")
    // GIVEN zip archive (test.zip) is open in PearlZip
    // WHEN select file (first-folder)
    //     AND file Information is displayed for file (first-folder)
    // THEN ensure details match expectations
    public void testFX_FileInfoForFolder_MatchExpectations() {
        final Path archivePath = Paths.get("src", "test", "resources", "test.zip")
                                      .toAbsolutePath();

        // Given
        simOpenArchive(this, archivePath.toAbsolutePath(), true, false);

        // When
        TableRow<FileInfo> row = PearlZipSpecifications.whenEntrySelectedInCurrentWindow(this, "first-folder");
        simFileInfo(this);

        // Then
        FileInfo info = row.getItem();
        PearlZipSpecifications.thenFileInfoScreenContentsMatchesFileMetaData(this, info);
    }
}
