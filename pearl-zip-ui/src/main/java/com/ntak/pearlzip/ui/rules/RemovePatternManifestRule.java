/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.rules;

import com.ntak.pearlzip.archive.model.PluginInfo;
import com.ntak.pearlzip.archive.pub.CheckManifestRule;
import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.util.internal.ModuleUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static com.ntak.pearlzip.ui.constants.ResourceConstants.CSV;
import static com.ntak.pearlzip.ui.constants.ZipConstants.CK_RUNTIME_MODULE_PATH;
import static com.ntak.pearlzip.ui.constants.ZipConstants.KEY_MANIFEST_DELETED;

/**
 *  Parses through metadata to identify specified file patterns to remove from the PearlZip providers folder. This
 *  functionality if set, will override automatic removal policy in PearlZip.
 *
 *  @author Aashutos Kakshepati
 */
public class RemovePatternManifestRule implements CheckManifestRule {

    @Override
    public String getKey() {
        return KEY_MANIFEST_DELETED;
    }

    @Override
    public void processManifest(PluginInfo info, Path targetDir) {
        Path RUNTIME_MODULE_PATH = InternalContextCache.INTERNAL_CONFIGURATION_CACHE.<Path>getAdditionalConfig(CK_RUNTIME_MODULE_PATH).get();
        Arrays.stream(CSV.split(info.getProperties().getOrDefault(getKey(),"")))
              .forEach(l -> {
            try {
                ModuleUtil.purgeLibrary(RUNTIME_MODULE_PATH,
                                        Paths.get(RUNTIME_MODULE_PATH.toAbsolutePath().toString(), l),
                                        info.getName());
            } catch(IOException e) {
            }
        });
    }
}
