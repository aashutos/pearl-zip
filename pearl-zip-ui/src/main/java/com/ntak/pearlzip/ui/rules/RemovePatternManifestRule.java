/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.rules;

import com.ntak.pearlzip.archive.model.PluginInfo;
import com.ntak.pearlzip.archive.pub.CheckManifestRule;
import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.util.internal.ModuleUtil;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ResourceConstants.CSV;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;

/**
 *  Parses through metadata to identify specified file patterns to remove from the PearlZip providers folder. This
 *  functionality if set, will override automatic removal policy in PearlZip.
 *
 *  @author Aashutos Kakshepati
 */
public class RemovePatternManifestRule implements CheckManifestRule {

    private static final Logger LOGGER =  LoggerContext.getContext().getLogger(RemovePatternManifestRule.class);

    @Override
    public String getKey() {
        return KEY_MANIFEST_DELETED;
    }

    @Override
    public void processManifest(PluginInfo info, Path targetDir) {
        Path RUNTIME_MODULE_PATH = InternalContextCache.INTERNAL_CONFIGURATION_CACHE.<Path>getAdditionalConfig(CK_RUNTIME_MODULE_PATH).get();
        Arrays.stream(CSV.split(info.getProperties().getOrDefault(getKey(),"")))
              .filter(s -> !s.isEmpty())
              .forEach(l -> {
            try {
                ModuleUtil.purgeLibrary(RUNTIME_MODULE_PATH,
                                        Paths.get(RUNTIME_MODULE_PATH.toAbsolutePath().toString(), l),
                                        info.getName());
            } catch(IOException e) {
                // LOG: Failed to execute Remove Pattern: %s as part of clean up before installation of plugin: %s
                LOGGER.warn(resolveTextKey(LOG_FAILED_EXECUTE_REMOVE_PATTERN, l, info.getName()));
            }
        });
    }
}
