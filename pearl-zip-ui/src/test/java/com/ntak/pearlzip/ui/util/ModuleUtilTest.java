/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.util;

import com.ntak.pearlzip.archive.constants.LoggingConstants;
import com.ntak.pearlzip.archive.model.PluginInfo;
import com.ntak.pearlzip.archive.pub.CheckManifestRule;
import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.rules.*;
import javafx.application.Platform;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.CNS_RES_BUNDLE;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.LOG_BUNDLE;
import static com.ntak.pearlzip.archive.util.LoggingUtil.genLocale;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.ArchiveUtil.deleteDirectory;

public class ModuleUtilTest {

    private static Path tempDir;
    private static CountDownLatch latch = new CountDownLatch(1);

    /*
     *  Test cases:
     *  + Successful manifest verification. Ensure file meta is appropriate.
     *  + Failure manifest verification. License file does not exits.
     *  + Failure manifest verification. Lib file does not exist.
     *  + Failure manifest verification. Lib file hash does not match.
     */

    @BeforeEach
    public void setUp() throws IOException, NoSuchAlgorithmException, InterruptedException {
        try {
            System.setProperty(CNS_RES_BUNDLE, "pearlzip-ui");
            Platform.startup(() -> latch.countDown());
        } catch(Exception e) {
            latch.countDown();
        } finally {
            latch.await();
            Locale defaultLocale = genLocale(new Properties());
            LoggingConstants.LOG_BUNDLE = ResourceBundle.getBundle("pearlzip",
                                                                   defaultLocale);
            LoggingConstants.CUSTOM_BUNDLE = ResourceBundle.getBundle("custom",
                                                                      defaultLocale);
            LOG_BUNDLE.keySet()
                      .forEach(k -> System.out.printf("Property: (k=%s,v=%s)%n",
                                                      k,
                                                      LOG_BUNDLE.getString(k)));
        }
        // Loading rules...
        List<CheckManifestRule> MANIFEST_RULES = new CopyOnWriteArrayList<>();
        MANIFEST_RULES.add(new MinVersionManifestRule());
        MANIFEST_RULES.add(new MaxVersionManifestRule());
        MANIFEST_RULES.add(new LicenseManifestRule());
        MANIFEST_RULES.add(new CheckLibManifestRule());
        MANIFEST_RULES.add(new RemovePatternManifestRule());
        MANIFEST_RULES.add(new ThemeManifestRule());
        InternalContextCache.INTERNAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_MANIFEST_RULES, MANIFEST_RULES);
        InternalContextCache.INTERNAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_PLUGINS_METADATA, new ConcurrentHashMap<String, PluginInfo>());


        System.setProperty(CNS_NTAK_PEARL_ZIP_VERSION, "0.0.4.0");

        // Populating test data
        tempDir = java.nio.file.Files.createTempDirectory("pz");
        prepareData(tempDir);
    }

    @AfterEach
    public void tearDown() {
        deleteDirectory(tempDir, (b) -> false);
    }

    private static void prepareData(Path tempDir) throws IOException, NoSuchAlgorithmException {
        Path MF = Paths.get(tempDir.toAbsolutePath()
                                   .toString(), "MF");
        Path lib = Paths.get(tempDir.toAbsolutePath()
                                    .toString(), "lib.jar");
        Path dep = Paths.get(tempDir.toAbsolutePath()
                                    .toString(), "dep.jar");
        Path libHash = Paths.get(tempDir.toAbsolutePath()
                                        .toString(), "lib.sha256");
        Path license = Paths.get(tempDir.toAbsolutePath()
                                        .toString(), "license.txt");

        Files.createFile(MF);
        Files.createFile(lib);
        Files.createFile(dep);
        Files.createFile(libHash);
        Files.createFile(license);

        Files.writeString(lib, "temp-jar-file", StandardOpenOption.WRITE);

        MessageDigest digest = MessageDigest.getInstance("sha256");
        String calculatedHash = HexFormat.of()
                                         .formatHex(digest.digest(Files.readAllBytes(lib)));
        Files.writeString(libHash, calculatedHash, StandardOpenOption.WRITE);

        Files.writeString(license, "license-file-nothing-to-agree-to", StandardOpenOption.WRITE);

        Files.writeString(MF, "min-version:0.0.4.0\n", StandardOpenOption.APPEND);
        Files.writeString(MF, "license:license.txt\n", StandardOpenOption.APPEND);
        Files.writeString(MF, "lib-file:sha256:lib.jar\n", StandardOpenOption.APPEND);
        Files.writeString(MF, "lib-file:N/A:dep.jar\n", StandardOpenOption.APPEND);
    }

    @Test
    public void testParseManifest_Success() {
        PluginInfo info = ModuleUtil.parseManifest(Paths.get(tempDir.toAbsolutePath().toString(), "MF")).get();

        Assertions.assertTrue(info.getDependencies()
                                     .toString()
                                     .contains(Paths.get("lib.jar")
                                                    .toString()),
                              "lib.jar not found");
        Assertions.assertTrue(info.getDependencies()
                                     .toString()
                                     .contains(Paths.get("dep.jar")
                                                    .toString()),
                              "dep.jar not found");
        Assertions.assertTrue(info.getLicenses()
                                     .toString()
                                     .contains(Paths.get("license.txt")
                                                    .toString()),
                              "license.txt not found");
    }

    @Test
    public void testCheckManifest_LicenseFileDoesNotExist_Fail() throws IOException {
        final Path license = Paths.get(tempDir.toAbsolutePath()
                                              .toString(), "license.txt");
        Files.deleteIfExists(license);
        PluginInfo info = ModuleUtil.parseManifest(Paths.get(tempDir.toAbsolutePath().toString(), "MF")).get();
        Exception e = Assertions.assertThrows(Exception.class, () -> ModuleUtil.checkManifest(info, tempDir));
        Assertions.assertEquals(String.format("Required license file (%s) does not exist.", license), e.getMessage());
    }

    @Test
    public void testCheckManifest_LibFileDoesNotExist_Fail() throws IOException {
        final Path lib = Paths.get(tempDir.toAbsolutePath()
                                          .toString(), "lib.jar");
        Files.deleteIfExists(lib);
        PluginInfo info = ModuleUtil.parseManifest(Paths.get(tempDir.toAbsolutePath().toString(), "MF")).get();
        Exception e = Assertions.assertThrows(Exception.class, () -> ModuleUtil.checkManifest(info, tempDir));
        Assertions.assertEquals(String.format("%s", lib), e.getMessage());
    }

    @Test
    public void testCheckManifest_LibFileHashMismatch_Fail() throws IOException {
        Path lib = Paths.get(tempDir.toAbsolutePath()
                                    .toString(), "lib.jar");
        final Path libHash = Paths.get(tempDir.toAbsolutePath()
                                              .toString(), "lib.sha256");
        Files.deleteIfExists(libHash);
        Files.createFile(libHash);
        Files.writeString(libHash, "000000", StandardOpenOption.WRITE);
        PluginInfo info = ModuleUtil.parseManifest(Paths.get(tempDir.toAbsolutePath().toString(), "MF")).get();
        Exception e = Assertions.assertThrows(Exception.class, () -> ModuleUtil.checkManifest(info, tempDir));
        Assertions.assertEquals(String.format("Calculated hash " +
                                                      "(f00051cb448e299509d965c899bb8a0cec7651bd458dc3ac70ae54de05cb2432) does not match the expected reference (000000) value. Integrity check failed for library: %s.",
                                              lib), e.getMessage());
    }
}
