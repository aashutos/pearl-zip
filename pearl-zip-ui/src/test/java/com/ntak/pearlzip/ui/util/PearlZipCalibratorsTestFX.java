/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.util;

import com.ntak.pearlzip.ui.UITestSuite;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.ntak.pearlzip.ui.util.PearlZipFXUtil.simNewArchive;
import static com.ntak.pearlzip.ui.util.PearlZipFXUtil.simWindowSelect;

@Tag("Calibration")
public class PearlZipCalibratorsTestFX extends AbstractPearlZipTestFX {

    private static Path tempDirRoot;
    private Path file;
    private Path folder;
    private Path nestedFile;
    private Path outputDir;



    @BeforeEach
    public void setUp() throws IOException {
        tempDirRoot = Files.createTempDirectory("pz");
        outputDir = Paths.get(tempDirRoot.toAbsolutePath().toString(), "output");
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
    public void calibrate_WindowMenu() throws IOException {
        List<Path> files = new LinkedList<>();

        for (int i = 0; i < 8; i++) {
            final Path tempArch = tempDirRoot.resolve(String.format("%s-%d.zip", "tempArch", i));
            simNewArchive(this, tempArch);
            files.add(tempArch);
        }

        for (Path archive : files) {
            simWindowSelect(this, archive);
            sleep(10000, TimeUnit.MILLISECONDS);

        }
    }
}
