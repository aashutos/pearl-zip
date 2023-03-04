/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.testfx;

import com.ntak.pearlzip.ui.UITestSuite;
import com.ntak.pearlzip.ui.util.AbstractPearlZipTestFX;
import com.ntak.pearlzip.ui.util.PearlZipSpecifications;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static com.ntak.pearlzip.ui.util.PearlZipFXUtil.*;

public class WindowTestFX extends AbstractPearlZipTestFX {

    private static Path tempDirRoot;
    private Path file;
    private Path folder;
    private Path nestedFile;

    /*
     *  Test cases:
     *  + Nest tarball into the compressor archive and verify contents is as expected
     *  + Open compressor archive and expand nested tarball and test window menu state is as expected before/after reintegration
     */

    @BeforeEach
    public void setUp() throws IOException {
        tempDirRoot = Files.createTempDirectory("pz");
        file = Paths.get(tempDirRoot.toString(), "temp-file");
        folder = Paths.get(tempDirRoot.toString(), "temp-folder");
        nestedFile = Paths.get(tempDirRoot.toString(), "temp-folder", "sub-temp-file");

        Files.createFile(file);
        Files.createDirectories(folder);
        Files.createFile(nestedFile);
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
        UITestSuite.clearDirectory(tempDirRoot);
    }

    @Test
    @DisplayName("Test: Open compressor archive and expand nested tarball and test window menu state is as expected before/after reintegration")
    // GIVEN a copy of tar.gz archive (empty.tgz as temp.tar.gz) is open in PearlZip
    // THEN ensure window menu count = 1
    //     AND window menu contains entry for archive(s) (temp.tar.gz)
    // WHEN nested file (temp.tar) opened from PearlZip
    // THEN ensure window menu count = 2
    //     AND window menu contains entry for archive(s) (temp.tar.gz,temp.tar)
    //     AND ensure archive (temp.tar) is focused
    // WHEN bring window (temp.tar.gz) to the front
    // THEN ensure archive (temp.tar.gz) is focused
    // WHEN bring window (temp.tar) to the front
    //     AND close nested archive and save = true
    // THEN ensure window menu count = 1
    //     AND window menu contains entry for archive(s) (empty.tgz)
    public void testFX_OpenCompressorArchive_WindowMenuState_MatchExpectations() throws IOException {
        // 1. Prepare compressor archive
        Path srcArchive = Paths.get("src", "test", "resources", "empty.tgz");
        Path archive = Paths.get(tempDirRoot.toAbsolutePath()
                                            .toString(), "temp.tar.gz");
        Files.copy(srcArchive, archive, StandardCopyOption.REPLACE_EXISTING);

        // Given
        simOpenArchive(this, archive, true, false);

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(1);
        PearlZipSpecifications.thenMainInstanceExistsWithName(archive.toString());

        // When
        PearlZipSpecifications.whenOpenNestedEntry(this, archive.toString(), "temp.tar");

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(2);
        PearlZipSpecifications.thenMainInstanceExistsWithName(archive.toString());
        PearlZipSpecifications.thenMainInstanceExistsWithName("temp.tar");

        String nestedArchive = lookupArchiveInfo("temp.tar").get().getArchivePath();
        PearlZipSpecifications.thenExpectedArchiveWindowIsSelected(nestedArchive);

        // When
        Assertions.assertTrue(simWindowSelect(this, archive), "Successfully changed window to parent archive");

        // Then
        PearlZipSpecifications.thenExpectedArchiveWindowIsSelected(archive.toString());

        // When
        Assertions.assertTrue(simWindowSelect(this, Paths.get(nestedArchive)), "Successfully changed window to parent archive");
        PearlZipSpecifications.whenCloseNestedArchive(this, true);

        // Then
        PearlZipSpecifications.thenExpectNumberOfMainInstances(1);
        PearlZipSpecifications.thenMainInstanceExistsWithName(archive.toString());
    }
}
