/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.archive.model;

import java.util.Objects;

/**
 *  Java record holding license information of dependencies for presentation into the UI.
 *  @author Aashutos Kakshepati
 */
public record LicenseInfo(String canonicalName, String version, String licenseType, String licenseFile, String url) {
    public LicenseInfo {
        assert Objects.nonNull(canonicalName) : "Canonical name must be specified";
        if (Objects.isNull(licenseType)) {
            licenseType = "unknown";
        }
    }

    @Override
    public String toString() {
        return "LicenseInfo{" +
                "canonicalName='" + canonicalName + '\'' +
                ", version='" + version + '\'' +
                ", licenseType='" + licenseType + '\'' +
                ", licenseFile='" + licenseFile + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
