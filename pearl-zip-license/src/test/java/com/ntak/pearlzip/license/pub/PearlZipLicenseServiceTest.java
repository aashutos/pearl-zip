/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.license.pub;

import com.ntak.pearlzip.license.model.LicenseInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.ntak.pearlzip.license.constants.LicenseConstants.CNS_LICENSE_LOCATION;
import static com.ntak.pearlzip.license.constants.LicenseConstants.CNS_LICENSE_OVERRIDE_LOCATION;

public class PearlZipLicenseServiceTest {

    private PearlZipLicenseService licenseService = new PearlZipLicenseService();

    /**
     *  Test cases:
     *  + Successfully parse license file and overrides (as per used in application)
     *  + Override check
     *  + Exception catch
     */

    @BeforeEach
    public void setUpBeforeEach() {
        System.setProperty(CNS_LICENSE_LOCATION, "LICENSE.xml");
        System.setProperty(CNS_LICENSE_OVERRIDE_LOCATION, "LICENSE-OVERRIDE.xml");
    }

    @Test
    @DisplayName("Test: Successfully retrieve license descriptors for dependencies")
    public void testRetrieveLicense_ValidSetup_Success() {
        String[] expectedKeys = {
                "org.apache.logging.log4j.log4j-api:Apache License, Version 2.0",
                "org.apache.logging.log4j.log4j-core:Apache License, Version 2.0",
                "org.apache.commons.commons-compress:Apache License, Version 2.0",
                "org.greenrobot.eventbus:The Apache Software License, Version 2.0",
                "org.opentest4j.opentest4j:The Apache License, Version 2.0",
                "org.apiguardian.apiguardian-api:The Apache License, Version 2.0",
                "net.sf.sevenzipjbinding.sevenzipjbinding-all-platforms:LGPL, version 2.1",
                "net.sf.sevenzipjbinding.sevenzipjbinding-all-platforms:unRAR restriction",
                "com.ntak.sevenzipjbinding:LGPL, version 2.1",
                "com.ntak.sevenzipjbinding:unRAR restriction",
                "org.junit.jupiter.junit-jupiter-engine:Eclipse Public License v2.0",
                "org.junit.jupiter.junit-jupiter-api:Eclipse Public License v2.0",
                "org.junit.platform.junit-platform-commons:Eclipse Public License v2.0",
                "org.junit.platform.junit-platform-engine:Eclipse Public License v2.0",
                "com.ntak.pearl-zip-archive-acc:BSD 3-Clause",
                "com.ntak.pearl-zip-archive-szjb:BSD 3-Clause",
                "com.ntak.pearl-zip-archive:BSD 3-Clause",
                "com.ntak.pearl-zip-ui:BSD 3-Clause",
                "com.ntak.pearl-zip-assembly-osx:BSD 3-Clause",
                "com.ntak.pearl-zip-license:BSD 3-Clause",
                "com.ntak.pearl-zip-ui#graphics:Creative Commons Attribution 4.0 International",
                "com.ntak.pearl-zip-ui#logo-resources:Creative Commons Attribution Non-Commercial No-Derivatives 4.0 International"
        };

        Map<String,LicenseInfo> licenses = licenseService.retrieveDeclaredLicenses();
        Assertions.assertNotNull(licenses, "Licenses was unexpectedly null");
        Assertions.assertEquals(40, licenses.size(), "The expected licenses were not retrieved");

        for (String key : expectedKeys) {
            Assertions.assertTrue(licenses.containsKey(key), String.format("key '%s' was not found", key));
        }
    }

    @Test
    @DisplayName("Test: Successfully retrieve dependency license descriptors with override applied")
    public void testRetrieveLicense_ValidSetupOverride_MatchExpectations() {
        System.setProperty(CNS_LICENSE_OVERRIDE_LOCATION, "LICENSE-TEST-OVERRIDE.xml");

        Map<String,LicenseInfo> licenses = licenseService.retrieveDeclaredLicenses();
        Assertions.assertNotNull(licenses, "Licenses was unexpectedly null");


        LicenseInfo overriddenLicense = licenses.get("com.ntak.pearl-zip-archive:BSD 3-Clause");
        Assertions.assertNotNull(overriddenLicense, "License details was not overridden");
        Assertions.assertEquals("http://url-override",overriddenLicense.url(), "URL was not the expected value");
        Assertions.assertEquals("overridden-file.html",overriddenLicense.licenseFile(), "Filename was not the expected value");
    }

    @Test
    @DisplayName("Test: Empty map returned when exception thrown on parsing licenses")
    public void testRetrieveLicense_ExceptionThrown_Empty() {
        System.setProperty(CNS_LICENSE_LOCATION, "NON-EXISTENT-FILE.xml");

        Map<String,LicenseInfo> licenses = licenseService.retrieveDeclaredLicenses();
        Assertions.assertNotNull(licenses, "Licenses was unexpectedly null");
        Assertions.assertEquals(0, licenses.size(), "The expected licenses were not retrieved");
    }

    @Test
    @DisplayName("Test: Partial map returned when exception thrown on parsing override licenses")
    public void testRetrieveLicense_ExceptionThrownOverride_Partial() {
        System.setProperty(CNS_LICENSE_LOCATION, "LICENSE.xml");
        System.setProperty(CNS_LICENSE_OVERRIDE_LOCATION, "NON-EXISTENT-FILE.xml");

        Map<String,LicenseInfo> licenses = licenseService.retrieveDeclaredLicenses();
        Assertions.assertNotNull(licenses, "Licenses was unexpectedly null");
        Assertions.assertEquals(27, licenses.size(), "The expected licenses were not retrieved");
    }
}
