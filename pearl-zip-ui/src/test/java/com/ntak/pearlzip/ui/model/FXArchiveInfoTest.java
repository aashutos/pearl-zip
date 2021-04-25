/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.model;

import com.ntak.pearlzip.archive.pub.ArchiveReadService;
import com.ntak.pearlzip.archive.pub.ArchiveWriteService;
import com.ntak.pearlzip.archive.pub.FileInfo;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class FXArchiveInfoTest {

    private static ArchiveWriteService writeService;
    private static ArchiveReadService readService;
    private static FXArchiveInfo fxArchiveInfo;
    private static Path archive;
    /*
     * Test cases:
     * + Test refresh resets state of archive
     */

    @BeforeAll
    public static void setUpOnce() throws IOException {
        archive = Files.createTempFile("pz", "");
        writeService = Mockito.mock(ArchiveWriteService.class);
        readService = Mockito.mock(ArchiveReadService.class);

        when(readService.listFiles(anyLong(), eq(archive.toAbsolutePath().toString()))).thenReturn(List.of(
                new FileInfo(0, 0, "folder", 0, 0, 0,
                             LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "user", "group",
                             0, "", true, false, Collections.emptyMap()),
                new FileInfo(1, 0, "file", 0, 0, 0,
                             LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "user", "group",
                             0, "", false, false, Collections.emptyMap()),
                new FileInfo(2, 1, "inner-file", 0, 0, 0,
                             LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "user", "group",
                             0, "", false, false, Collections.emptyMap()),
                new FileInfo(3, 1, "another-inner-file", 0, 0, 0,
                             LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "user", "group",
                             0, "", false, false, Collections.emptyMap())
        ));
    }

    @AfterAll
    public static void tearDownOnce() throws IOException {
        Files.deleteIfExists(archive);
    }

    @Test
    @DisplayName("Test: Refresh resets the state of the FXArchiveInfo instance")
    public void testRefresh_MatchExpectations() {
        // Initial state
        fxArchiveInfo = new FXArchiveInfo(archive.toAbsolutePath().toString(), readService, writeService);
        Assertions.assertNotNull(fxArchiveInfo.getMigrationInfo());
        Assertions.assertEquals(FXMigrationInfo.MigrationType.NONE, fxArchiveInfo.getMigrationInfo().getType(),
                                "MigrationType was not initialised as expected");
        Assertions.assertEquals(0, fxArchiveInfo.getDepth().get(), "Initial depth was not as expected");
        Assertions.assertEquals("", fxArchiveInfo.getPrefix(), "Prefix was not initialised correctly");
        Assertions.assertEquals(writeService, fxArchiveInfo.getWriteService(), "Write Service was not initialised");
        Assertions.assertEquals(readService, fxArchiveInfo.getReadService(), "Read Service was not initialised");
        Assertions.assertTrue(fxArchiveInfo.getController().isEmpty(), "Controller was unexpectedly initialised");
        Assertions.assertNotNull(fxArchiveInfo.getFiles(), "No files were retrieved from mock archive");
        Assertions.assertEquals(4, fxArchiveInfo.getFiles().size(), "The expected number of files was retrieved from " +
                "mock archive");

        // Navigate to a subdirectory in archive
        fxArchiveInfo.setPrefix("folder");
        Assertions.assertEquals("folder", fxArchiveInfo.getPrefix(), "Prefix was not set as expected");
        Assertions.assertEquals(1,fxArchiveInfo.getDepth().incrementAndGet(), "Depth did not increment");

        // Refresh called with mocked update to the archive
        // Mock update read service output
        when(readService.listFiles(anyLong(), eq(archive.toAbsolutePath().toString()))).thenReturn(List.of(
                new FileInfo(0, 0, "folder", 0, 0, 0,
                             LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "user", "group",
                             0, "", true, false, Collections.emptyMap()),
                new FileInfo(1, 0, "file", 0, 0, 0,
                             LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "user", "group",
                             0, "", false, false, Collections.emptyMap()),
                new FileInfo(2, 1, "inner-file", 0, 0, 0,
                             LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "user", "group",
                             0, "", false, false, Collections.emptyMap()),
                new FileInfo(3, 1, "another-inner-file", 0, 0, 0,
                             LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "user", "group",
                             0, "", false, false, Collections.emptyMap()),
                new FileInfo(4, 1, "hello-file", 0, 0, 0,
                             LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "user", "group",
                             0, "", false, false, Collections.emptyMap())
        ));
        fxArchiveInfo.refresh();

        // Check refresh worked successfully
        Assertions.assertEquals(0, fxArchiveInfo.getDepth().get(), "Initial depth was not as expected");
        Assertions.assertEquals("", fxArchiveInfo.getPrefix(), "Prefix was not initialised correctly");
        Assertions.assertNotNull(fxArchiveInfo.getFiles(), "No files were retrieved from mock archive");
        Assertions.assertEquals(5, fxArchiveInfo.getFiles().size(), "The expected number of files was retrieved from " +
                "mock archive");

    }
}
