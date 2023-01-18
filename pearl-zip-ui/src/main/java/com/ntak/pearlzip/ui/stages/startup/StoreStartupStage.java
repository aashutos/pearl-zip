/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.stages.startup;

import com.ntak.pearlzip.ui.constants.ResourceConstants;
import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.util.AbstractStartupStage;
import com.ntak.pearlzip.ui.util.StoreRepoDetails;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.*;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;

/**
 *  Sets up repository metadata for PearlZip Extension Stores for use within the application to consume
 *  and install plugins and themes.
 *
 *  @author Aashutos Kakshepati
 */
public class StoreStartupStage extends AbstractStartupStage {
    @Override
    public void executeProcess() throws Exception {
        ////////////////////////////////////////////
        ///// Store Repository Load ///////////////
        //////////////////////////////////////////

        Path repoPath = InternalContextCache.GLOBAL_CONFIGURATION_CACHE
                                            .<Path>getAdditionalConfig(CK_STORE_ROOT)
                                            .get()
                                            .resolve("repository");
        InternalContextCache.GLOBAL_CONFIGURATION_CACHE
                .setAdditionalConfig(CK_REPO_ROOT, repoPath);

        if (!Files.exists(repoPath)) {
            Files.createDirectories(repoPath);
        }

        // Load default repository and overwrite file
        Path defaultRepoFile = repoPath.resolve("default");

        StoreRepoDetails storeRepoDetails = new StoreRepoDetails(ResourceConstants.DEFAULT, System.getProperty(CNS_NTAK_PEARL_ZIP_JDBC_URL), System.getProperty(CNS_NTAK_PEARL_ZIP_JDBC_USER), System.getProperty(CNS_NTAK_PEARL_ZIP_JDBC_PASSWORD));
        com.ntak.pearlzip.ui.util.internal.JFXUtil.persistStoreRepoDetails(storeRepoDetails, defaultRepoFile);

        // Load persisted repository files
        Files.list(repoPath).filter(p -> {
                 try {
                     return !Files.isHidden(p);
                 } catch(IOException e) {
                     return false;
                 }
             })
             .forEach(com.ntak.pearlzip.ui.util.internal.JFXUtil::loadStoreRepoDetails);

        // Read in repository files into in-memory map
        InternalContextCache.INTERNAL_CONFIGURATION_CACHE.<Map<String,StoreRepoDetails>>getAdditionalConfig(CK_STORE_REPO).get().put(ResourceConstants.DEFAULT, storeRepoDetails);
    }
}
