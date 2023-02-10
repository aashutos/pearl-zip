/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.stages.seed;

import com.ntak.pearlzip.archive.model.PluginInfo;
import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.util.AbstractSeedStartupStage;
import javafx.util.Pair;

import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.internal.JFXUtil.initialiseBootstrapProperties;

/**
 *  Sets up caches, independent constants and initiate bootstrap configuration.
 *
 *  @author Aashutos Kakshepati
 */
public class PrimarySeedStartupStage extends AbstractSeedStartupStage {
    @Override
    public void executeProcess() throws Exception {
        ////////////////////////////////////////////
        ///// Primary Startup /////////////////////
        //////////////////////////////////////////

        // Setting JRT FileSystem
        InternalContextCache.INTERNAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_JRT_FILE_SYSTEM, FileSystems.getFileSystem(URI.create("jrt:/")));

        // Setting Plugins Metadata cache (Logging stage pre-requisite)
        InternalContextCache.INTERNAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_PLUGINS_METADATA, new ConcurrentHashMap<String,PluginInfo>());

        // Setting caches (language pack module pre-requisite)
        InternalContextCache.INTERNAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_LANG_PACKS, new HashSet<Pair<String,Locale>>());
        InternalContextCache.INTERNAL_CONFIGURATION_CACHE.<Map<String,ModuleLayer.Controller>>setAdditionalConfig(CK_MLC_CACHE, new ConcurrentHashMap<>());

        // Set up high level directory structures...
        final Path STORE_ROOT = Paths.get(System.getProperty(CNS_STORE_ROOT,
                                                             String.format("%s/.pz",
                                                                           System.getProperty("user.home"))));
        // Create root store
        if (!Files.exists(STORE_ROOT)) {
            Files.createDirectories(STORE_ROOT);
        }

        String defaultModulePath = STORE_ROOT.resolve("providers").toString();
        Path RUNTIME_MODULE_PATH =
                Paths.get(System.getProperty(CNS_NTAK_PEARL_ZIP_MODULE_PATH, defaultModulePath)).toAbsolutePath();
        InternalContextCache.INTERNAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_RUNTIME_MODULE_PATH, RUNTIME_MODULE_PATH);

        InternalContextCache.GLOBAL_CONFIGURATION_CACHE
                .setAdditionalConfig(CK_STORE_ROOT, STORE_ROOT
                );
        InternalContextCache.GLOBAL_CONFIGURATION_CACHE
                .setAdditionalConfig(CK_LOCAL_TEMP,
                                     Paths.get(Optional.ofNullable(System.getenv("TMPDIR"))
                                                       .orElse(STORE_ROOT.toString())
                                     )
                );

        Path APPLICATION_SETTINGS_FILE = STORE_ROOT.resolve("application.properties");
        InternalContextCache.GLOBAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_APPLICATION_SETTINGS_FILE,
                                                                            APPLICATION_SETTINGS_FILE);

        // Initialise Bootstrap Configuration...
        initialiseBootstrapProperties(STORE_ROOT, APPLICATION_SETTINGS_FILE);
    }
}
