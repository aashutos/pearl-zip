/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.stages.startup;

import com.ntak.pearlzip.archive.model.PluginInfo;
import com.ntak.pearlzip.archive.pub.CheckManifestRule;
import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.rules.*;
import com.ntak.pearlzip.ui.util.AbstractStartupStage;
import com.ntak.pearlzip.ui.util.internal.ModuleUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.archive.constants.LoggingConstants.ROOT_LOGGER;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;

/**
 *  Loads providers, which handle different kinds of archive formats and sets up the mechanism for parsing pzax manifests.
 *  Plugin metadata/descriptors are ingested into PearlZip.
 *
 *  @author Aashutos Kakshepati
 */
public class ModuleLoadStartupStage extends AbstractStartupStage {
    @Override
    public void executeProcess() throws Exception {
        ////////////////////////////////////////////
        ///// Plugin Manifest Load ////////////////
        //////////////////////////////////////////

        // Loading rules...
        List<CheckManifestRule> MANIFEST_RULES = new CopyOnWriteArrayList<>();
        MANIFEST_RULES.add(new MinVersionManifestRule());
        MANIFEST_RULES.add(new MaxVersionManifestRule());
        MANIFEST_RULES.add(new LicenseManifestRule());
        MANIFEST_RULES.add(new CheckLibManifestRule());
        MANIFEST_RULES.add(new RemovePatternManifestRule());
        MANIFEST_RULES.add(new ThemeManifestRule());
        InternalContextCache.INTERNAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_MANIFEST_RULES, MANIFEST_RULES);


        // Loading plugin manifests...
        Path LOCAL_MANIFEST_DIR = InternalContextCache.GLOBAL_CONFIGURATION_CACHE
                                                      .<Path>getAdditionalConfig(CK_STORE_ROOT)
                                                      .get()
                                                      .resolve("manifests");
        InternalContextCache.GLOBAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_LOCAL_MANIFEST_DIR, LOCAL_MANIFEST_DIR);
        if (!Files.exists(LOCAL_MANIFEST_DIR)) {
            Files.createDirectories(LOCAL_MANIFEST_DIR);
        }

        Files.list(LOCAL_MANIFEST_DIR)
             .filter(m -> m.getFileName()
                           .toString()
                           .toUpperCase()
                           .endsWith(".MF"))
             .forEach(m -> {
                 try {
                     Map<String,PluginInfo> PLUGINS_METADATA = InternalContextCache.INTERNAL_CONFIGURATION_CACHE.<Map<String, PluginInfo>>getAdditionalConfig(CK_PLUGINS_METADATA).get();
                     Optional<PluginInfo> optInfo = ModuleUtil.parseManifest(m);
                     if (optInfo.isPresent()) {
                         PluginInfo info = optInfo.get();
                         synchronized(PLUGINS_METADATA) {
                             PLUGINS_METADATA.put(info.getName(), info);
                         }
                     }
                 } catch(Exception e) {
                 }
             });

        ////////////////////////////////////////////
        ///// Runtime Module Load /////////////////
        //////////////////////////////////////////

        Path RUNTIME_MODULE_PATH = InternalContextCache.INTERNAL_CONFIGURATION_CACHE.<Path>getAdditionalConfig(CK_RUNTIME_MODULE_PATH).get();

        if (Files.isDirectory(RUNTIME_MODULE_PATH)) {
            // Load modules by iterating through folders under RUNTIME_MODULE_PATH...
            for (Path modulePath : Files.list(RUNTIME_MODULE_PATH).filter(Files::isDirectory).collect(Collectors.toSet())) {
                // LOG: Loading modules from path: %s
                ROOT_LOGGER.info(resolveTextKey(LOG_LOADING_MODULE,
                                                modulePath.toAbsolutePath().toString()));
                ModuleUtil.loadModulesDynamic(modulePath.toAbsolutePath());
            }
        }

        ModuleUtil.loadModulesStatic();
    }
}
