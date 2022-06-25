/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.model;

import com.ntak.pearlzip.archive.model.LicenseInfo;
import com.ntak.pearlzip.archive.pub.*;
import com.ntak.pearlzip.archive.pub.profile.component.ReadServiceComponent;
import com.ntak.pearlzip.archive.pub.profile.component.WriteServiceComponent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.ntak.pearlzip.ui.constants.ZipConstants.CNS_PROVIDER_PRIORITY_ROOT_KEY;
import static org.mockito.Mockito.when;

public class ZipStateTest {
    private LicenseInfo licenseInfo = new LicenseInfo("module", "1.0.0", "license", "license-text.txt", "http://license.org");
    private static ArchiveWriteService mockWriteService;
    private static ArchiveServiceProfile archiveServiceProfile = new ArchiveServiceProfile("test-provider");
    private static final ArchiveWriteService priorityWriteService = new ArchiveWriteService() {
        @Override
        public ArchiveServiceProfile getArchiveServiceProfile() {
            return archiveServiceProfile;
        }

        @Override
        public void createArchive(long sessionId, String archivePath, FileInfo... files) {

        }

        @Override
        public void createArchive(long sessionId, ArchiveInfo archiveInfo, FileInfo... files) {

        }

        @Override
        public boolean addFile(long sessionId, String archivePath, FileInfo... files) {
            return false;
        }

        @Override
        public boolean addFile(long sessionId, ArchiveInfo archiveInfo, FileInfo... files) {
            return false;
        }

        @Override
        public boolean deleteFile(long sessionId, String archivePath, FileInfo file) {
            return false;
        }

        @Override
        public boolean deleteFile(long sessionId, ArchiveInfo archiveInfo, FileInfo file) {
            return false;
        }

        @Override
        public List<String> supportedWriteFormats() {
            return List.of("zip");
        }
    };
    private static ArchiveReadService mockReadService;

    /*
        Test cases:
        + Add license declarations
        + Add write archive service
        + Add read archive service
        + Compressor archives listing
        + Priority archive service override
     */

    @BeforeAll
    public static void setUpOnce() {
        mockWriteService = Mockito.mock(ArchiveWriteService.class);
        archiveServiceProfile.addComponent(new WriteServiceComponent(Set.of("zip", "rar", "tar"), Collections.emptyMap()));
        archiveServiceProfile.addComponent(new ReadServiceComponent(Set.of("zip","rar","tar","cab","iso"), Collections.emptyMap()));

        when(mockWriteService.getArchiveServiceProfile()).thenReturn(archiveServiceProfile);
        when(mockWriteService.supportedWriteFormats()).thenReturn(new ArrayList<>(List.of("zip", "rar", "tar")));
        when(mockWriteService.getCompressorArchives()).thenCallRealMethod();

        mockReadService = Mockito.mock(ArchiveReadService.class);
        when(mockReadService.getArchiveServiceProfile()).thenReturn(archiveServiceProfile);
        when(mockReadService.getCompressorArchives()).thenCallRealMethod();
    }

    @Test
    @DisplayName("Test: Add license successfully")
    public void testAddLicense_Success() {
        Assertions.assertEquals(0, ZipState.getLicenseDeclarations().size(), "A license was not added");
        ZipState.addLicenseDeclaration("module:license",
                                       licenseInfo
        );
        Assertions.assertEquals(1, ZipState.getLicenseDeclarations().size(), "A license was not added");
        Assertions.assertTrue(ZipState.getLicenseDeclaration("module:license").isPresent(), "Expected license was not retrieved");
                                Assertions.assertEquals(licenseInfo, ZipState.getLicenseDeclaration("module:license").get(),
                                "The license contents do not match");
    }

    @Test
    @DisplayName("Test: Add archive write service successfully")
    public void testAddArchiveWriteService_Success() {
        ZipState.addArchiveProvider(mockWriteService);

        Assertions.assertTrue(ZipState.getWriteProviders().size() > 0, "Write Provider was not added");

        Assertions.assertTrue(ZipState.getWriteArchiveServiceForFile("test.zip").isPresent());
        Assertions.assertTrue(ZipState.getWriteArchiveServiceForFile("another-test.rar").isPresent());
        Assertions.assertTrue(ZipState.getWriteArchiveServiceForFile("yet-another-test.tar").isPresent());
        Assertions.assertFalse(ZipState.getWriteArchiveServiceForFile("negative-test.tgz").isPresent());
    }

    @Test
    @DisplayName("Test: Add archive read service successfully")
    public void testAddArchiveReadService_Success() {
        ZipState.addArchiveProvider(mockReadService);

        Assertions.assertTrue(ZipState.getReadProviders().size() > 0, "Write Provider was not added");

        Assertions.assertTrue(ZipState.getReadArchiveServiceForFile("test.zip").isPresent());
        Assertions.assertTrue(ZipState.getReadArchiveServiceForFile("a-test.cab").isPresent());
        Assertions.assertTrue(ZipState.getReadArchiveServiceForFile("yes-a-test.iso").isPresent());
        Assertions.assertTrue(ZipState.getReadArchiveServiceForFile("another-test.rar").isPresent());
        Assertions.assertTrue(ZipState.getReadArchiveServiceForFile("yet-another-test.tar").isPresent());
        Assertions.assertFalse(ZipState.getReadArchiveServiceForFile("negative-test.tgz").isPresent());
    }

    @Test
    @DisplayName("Test: List default compressor archive file types successfully")
    public void testListCompressorArchive_Success() {
        Set<String> archiveFormats = ZipState.getCompressorArchives();

        Assertions.assertTrue(archiveFormats.contains("gz"),"gz format not declared");
        Assertions.assertTrue(archiveFormats.contains("xz"),"gz format not declared");
        Assertions.assertTrue(archiveFormats.contains("bz2"),"gz format not declared");
        Assertions.assertTrue(archiveFormats.contains("lz"),"gz format not declared");
        Assertions.assertTrue(archiveFormats.contains("lz4"),"gz format not declared");
        Assertions.assertTrue(archiveFormats.contains("lzma"),"gz format not declared");
        Assertions.assertTrue(archiveFormats.contains("z"),"gz format not declared");
        Assertions.assertTrue(archiveFormats.contains("sz"),"gz format not declared");
    }

    @Test
    @DisplayName("Test: Add priority Write Service. Ensure it is chosen in the expected scenarios")
    public void testAddPriorityWriteArchiveService_Success() {
        String priorityServiceKey = String.format(CNS_PROVIDER_PRIORITY_ROOT_KEY,
                                                  priorityWriteService.getClass().getCanonicalName());
        String mockServiceKey = String.format(CNS_PROVIDER_PRIORITY_ROOT_KEY,
                                              mockWriteService.getClass().getCanonicalName());

        System.setProperty(priorityServiceKey,"9999");
        System.setProperty(mockServiceKey,"0");

        ZipState.addArchiveProvider(mockWriteService);
        ZipState.addArchiveProvider(priorityWriteService);

        Assertions.assertTrue(ZipState.getWriteArchiveServiceForFile("test.zip").isPresent());
        Assertions.assertEquals(priorityWriteService, ZipState.getWriteArchiveServiceForFile("test.zip").get(),
                                "Priority Service was not used");
        Assertions.assertTrue(ZipState.getWriteArchiveServiceForFile("another-test.rar").isPresent());
        Assertions.assertEquals(mockWriteService, ZipState.getWriteArchiveServiceForFile("another-test.rar").get(),
                                "Mock Service was not used");
        Assertions.assertTrue(ZipState.getWriteArchiveServiceForFile("yet-another-test.tar").isPresent());
        Assertions.assertEquals(mockWriteService,
                                ZipState.getWriteArchiveServiceForFile("yet-another-test.tar").get(),
                                "Mock Service was not used");

        System.clearProperty(priorityServiceKey);
        System.clearProperty(mockServiceKey);
    }
}
