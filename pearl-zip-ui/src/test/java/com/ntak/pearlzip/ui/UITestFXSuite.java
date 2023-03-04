/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.*;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/***
 *   @author Aashutos Kakshepati
 */
@RunWith(JUnitPlatform.class)
@SelectPackages("com.ntak.pearlzip.ui.testfx")
@SuiteDisplayName("PearlZip UI module Integration TestFX tests")
@IncludeClassNamePatterns("^.*TestFX$")
@IncludeTags("fx-test")
@ExcludeTags("Calibration")
public class UITestFXSuite {

    public static Path genSourceDataSet() throws IOException {
        Path setRoot = Paths.get(Files.createTempDirectory("pz").toString(), "root");
        Files.createDirectories(setRoot);

        // Creating files and directories...
        Files.createDirectories(Paths.get(setRoot.toAbsolutePath().toString(), "level1a"));
        Files.createFile(Paths.get(setRoot.toAbsolutePath().toString(), "level1a", "file1a1.txt"));
        Files.createFile(Paths.get(setRoot.toAbsolutePath().toString(), "level1a", "file1a2.txt"));
        Files.createFile(Paths.get(setRoot.toAbsolutePath().toString(), "level1a", "file1a3.txt"));
        Files.createFile(Paths.get(setRoot.toAbsolutePath().toString(), "level1a", "EXTRACT_ME.txt"));

        Files.createDirectories(Paths.get(setRoot.toAbsolutePath().toString(), "level1b", "level1b1"));
        Files.createDirectories(Paths.get(setRoot.toAbsolutePath().toString(), "level1b", "DELETE_ME_ALSO"));
        Files.createFile(Paths.get(setRoot.toAbsolutePath().toString(), "level1b", "level1b1", "level2a.txt"));
        Files.createFile(Paths.get(setRoot.toAbsolutePath().toString(), "level1b", "file1b1.txt"));
        Files.createFile(Paths.get(setRoot.toAbsolutePath().toString(), "level1b", "file1b2.txt"));
        Files.createFile(Paths.get(setRoot.toAbsolutePath().toString(), "level1b", "DELETE_ME.txt"));
        Files.createFile(Paths.get(setRoot.toAbsolutePath().toString(), "level1b", "DELETE_ME_ALSO", "AUTO_DELETED.txt"));

        Files.createDirectories(Paths.get(setRoot.toAbsolutePath().toString(), "level1c", "level1c1", "level2b"));
        Files.createDirectories(Paths.get(setRoot.toAbsolutePath().toString(), "level1c", "level1c2", "level2c"));
        Files.createFile(Paths.get(setRoot.toAbsolutePath().toString(), "level1c", "file1c1.txt"));
        Files.createFile(Paths.get(setRoot.toAbsolutePath().toString(), "level1c", "MOVE_DOWN.txt"));
        Files.createFile(Paths.get(setRoot.toAbsolutePath().toString(), "level1c", "COPY_DOWN.txt"));
        Files.createFile(Paths.get(setRoot.toAbsolutePath().toString(), "level1c", "level1c1", "level2b", "COPY_UP" +
                ".txt"));
        Files.createFile(Paths.get(setRoot.toAbsolutePath().toString(), "level1c", "level1c1", "level2b", "MOVE_UP.txt"));
        Files.createFile(Paths.get(setRoot.toAbsolutePath().toString(), "level1c", "level1c2", "level2c", "level2c1.txt"));
        return setRoot;
    }
}
