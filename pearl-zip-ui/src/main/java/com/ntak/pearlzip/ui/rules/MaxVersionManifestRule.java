/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.rules;

import com.ntak.pearlzip.archive.model.PluginInfo;
import com.ntak.pearlzip.archive.pub.CheckManifestRule;
import com.ntak.pearlzip.ui.util.VersionComparator;

import java.nio.file.Path;
import java.util.Objects;

import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.CNS_NTAK_PEARL_ZIP_VERSION;
import static com.ntak.pearlzip.ui.constants.ZipConstants.LOG_VERSION_MAX_VERSION_BREACH;

/**
 *  Manifest check, which ensures current Pearl Zip Application version is found within (inclusive) the upper bounds of
 *  compatibility for the plugin.
 *
 *  @author Aashutos Kakshepati
 */
public class MaxVersionManifestRule implements CheckManifestRule {
    @Override
    public String getKey() {
        return "max-version";
    }

    @Override
    public void processManifest(PluginInfo info, Path targetDir) throws Exception {
        // Open max version assumption
        if (Objects.isNull(info.getMaxVersion())) {
            return;
        }

        int result = VersionComparator.getInstance()
                                  .compare(info.getMaxVersion(),
                                           System.getProperty(CNS_NTAK_PEARL_ZIP_VERSION, "0.0.0.0")
                                  );
        if (result < 0) {
            // LOG: PZAX archive requires an older version of PearlZip (Maximum version supported: %s)
            throw new Exception(resolveTextKey(LOG_VERSION_MAX_VERSION_BREACH, info.getMaxVersion()));
        }
    }
}
