/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.rules;

import com.ntak.pearlzip.archive.model.PluginInfo;
import com.ntak.pearlzip.archive.pub.CheckManifestRule;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.LOG_REQUIRED_LICENSE_FILE_NOT_EXIST;

/**
 *  Manifest check, which ensures required licenses are bundles within pzax archive
 *
 *  @author Aashutos Kakshepati
 */
public class LicenseManifestRule implements CheckManifestRule {
    @Override
    public String getKey() {
        return "license";
    }

    @Override
    public void processManifest(PluginInfo info, Path targetDir) throws Exception {
        for (String license : info.getLicenses()) {
            Path licenseFile = Paths.get(targetDir.toAbsolutePath()
                                                  .toString(),
                                         license);
            if (!Files.exists(licenseFile)) {
                // LOG: Required license file (%s) does not exist.
                throw new Exception(resolveTextKey(LOG_REQUIRED_LICENSE_FILE_NOT_EXIST, licenseFile));
            }
        }
    }
}
