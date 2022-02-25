/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.rules;

import com.ntak.pearlzip.archive.model.PluginInfo;
import com.ntak.pearlzip.archive.pub.CheckManifestRule;
import com.ntak.pearlzip.ui.constants.ZipConstants;
import com.ntak.pearlzip.ui.util.ModuleUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static com.ntak.pearlzip.ui.constants.ResourceConstants.CSV;
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
        Arrays.stream(CSV.split(info.getProperties().getOrDefault(getKey(),"")))
              .forEach(l -> {
            try {
                ModuleUtil.purgeLibrary(ZipConstants.RUNTIME_MODULE_PATH,
                                        Paths.get(ZipConstants.RUNTIME_MODULE_PATH.toAbsolutePath().toString(), l),
                                        info.getName());
            } catch(IOException e) {
            }
        });
    }
}
