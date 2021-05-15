/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/***
 *   @author akakshepati
 */
@RunWith(JUnitPlatform.class)
@SelectPackages("com.ntak.pearlzip")
@IncludeClassNamePatterns("^.*Test$")
@SuiteDisplayName("PearlZip UI module tests")
public class UITestSuiteAll {

    public static void clearDirectory(Path directory) throws IOException {
        Files.walk(directory).filter((f)->!Files.isDirectory(f)).forEach(f-> {
            try {
                Files.deleteIfExists(f);
            } catch(IOException e) {
            }
        });
        Files.walk(directory).filter(Files::isDirectory).sorted((a,b)->b.toString().length()-a.toString().length()).forEach(f-> {
            try {
                Files.deleteIfExists(f);
            } catch(IOException e) {
            }
        });
        Files.deleteIfExists(directory);
    }
}
