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
import static com.ntak.pearlzip.ui.constants.ZipConstants.LOG_THEME_NOT_EXIST;

public class ThemeManifestRule implements CheckManifestRule {
    @Override
    public String getKey() {
        return "themes";
    }

    @Override
    public void processManifest(PluginInfo info, Path targetDir) throws Exception {
        for (String theme : info.getThemes()) {
            if (!Files.exists(Paths.get(targetDir.toAbsolutePath()
                                                 .toString(), theme))) {
                // LOG: Theme %s does not exist.
                throw new Exception(resolveTextKey(LOG_THEME_NOT_EXIST, theme));
            }
        }
    }
}
