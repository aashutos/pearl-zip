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
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;

/**
 *  Manifest check, which ensures current Pearl Zip Application version is found above (or equal to) the lower bounds
 *  of compatibility for the plugin.
 *
 *  @author Aashutos Kakshepati
 */
public class MinVersionManifestRule implements CheckManifestRule {
    @Override
    public String getKey() {
        return "min-version";
    }

    @Override
    public void processManifest(PluginInfo info, Path targetDir) throws Exception {
        if (Objects.isNull(info.getMinVersion()) || info.getMinVersion().isEmpty()) {
            // LOG: PZAX archive requires an older version of PearlZip (Maximum version supported: %s)
            throw new Exception(resolveTextKey(LOG_VERSION_MAX_VERSION_BREACH, "0.0.0.3"));
        }

        int result = VersionComparator.getInstance()
                                      .compare(info.getMinVersion(),
                                               System.getProperty(CNS_NTAK_PEARL_ZIP_VERSION, "0.0.0.0")
                                      );
        if (result > 0) {
            // LOG: PZAX archive requires a newer version of PearlZip (Minimum version supported: %s)
            throw new Exception(resolveTextKey(LOG_VERSION_MIN_VERSION_BREACH, info.getMinVersion()));
        }
    }
}
