/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.stages.startup;

import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.util.AbstractStartupStage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.ntak.pearlzip.archive.constants.ArchiveConstants.CURRENT_SETTINGS;
import static com.ntak.pearlzip.archive.constants.ArchiveConstants.WORKING_SETTINGS;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;

/**
 *  Sets up PearlZip Workspace including creating core directories and initiate settings.
 *
 *  @author Aashutos Kakshepati
 */
public class WorkspaceStartupStage extends AbstractStartupStage {

    public WorkspaceStartupStage() {
        super(WorkspaceStartupStage.class.getName());
    }

    @Override
    public void executeProcess() throws IOException {
        // 1. Set up secondary working directories within .pz
        ////////////////////////////////////////////
        ///// Create files and dir structure //////
        //////////////////////////////////////////

        // Create temporary store folder
        Path STORE_ROOT = InternalContextCache.GLOBAL_CONFIGURATION_CACHE
                .<Path>getAdditionalConfig(CK_STORE_ROOT)
                .get();
        Path STORE_TEMP = STORE_ROOT.resolve("temp");
        InternalContextCache.GLOBAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_STORE_TEMP, STORE_TEMP);

        if (!Files.exists(STORE_TEMP)) {
            Files.createDirectories(STORE_TEMP);
        }

        // Recent files
        Path RECENT_FILE = STORE_ROOT.resolve("rf");
        InternalContextCache.GLOBAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_RECENT_FILE, RECENT_FILE);
        if (!Files.exists(RECENT_FILE)) {
            Files.createFile(RECENT_FILE);
        }

        // Providers
        Path providerPath = STORE_ROOT.resolve("providers");
        Files.createDirectories(providerPath);
        InternalContextCache.GLOBAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_STORE_PROVIDERS, providerPath);

        // Themes
        Path themesPath = STORE_ROOT.resolve("themes");
        Files.createDirectories(themesPath);
        InternalContextCache.GLOBAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_STORE_THEMES, themesPath);

        // Initialise drag out constants...
        try {
            long maxSize = Long.parseLong(System.getProperty(CNS_NTAK_PEARL_ZIP_DEFAULT_MAX_SIZE_DRAG_OUT));
            InternalContextCache.INTERNAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_MAX_SIZE_DRAG_OUT, maxSize);
        } catch (Exception e) {

        }

        ////////////////////////////////////////////
        ///// Settings File Load ///////////////////
        ////////////////////////////////////////////
        Path SETTINGS_FILE = Paths.get(System.getProperty(CNS_SETTINGS_FILE, Paths.get(STORE_ROOT.toString(),
                                                                                       "settings.properties").toString()));
        InternalContextCache.GLOBAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_SETTINGS_FILE, SETTINGS_FILE);

        if (!Files.exists(SETTINGS_FILE)) {
            Files.createFile(SETTINGS_FILE);
        }

        try(InputStream settingsIStream = Files.newInputStream(SETTINGS_FILE)) {
            CURRENT_SETTINGS.load(settingsIStream);
            WORKING_SETTINGS.load(settingsIStream);
        }
    }
}
