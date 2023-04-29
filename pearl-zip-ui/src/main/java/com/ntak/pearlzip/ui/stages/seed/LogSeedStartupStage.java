/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.stages.seed;

import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.util.AbstractSeedStartupStage;
import com.ntak.pearlzip.ui.util.internal.ModuleUtil;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.CNS_CUSTOM_RES_BUNDLE;
import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.CNS_RES_BUNDLE;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.CUSTOM_BUNDLE;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.LOG_BUNDLE;
import static com.ntak.pearlzip.archive.util.LoggingUtil.genLocale;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;

/**
 *  Sets up logging framework (log4j) and language packs.
 *
 *  @author Aashutos Kakshepati
 */
public class LogSeedStartupStage extends AbstractSeedStartupStage {
    @Override
    public void executeProcess() throws Exception {
        final Path STORE_ROOT = InternalContextCache.GLOBAL_CONFIGURATION_CACHE
                .<Path>getAdditionalConfig(CK_STORE_ROOT)
                .get();

        ////////////////////////////////////////////
        ///// Log4j Setup /////////////////////////
        //////////////////////////////////////////

        // Log4j configuration - handle fixed parameters when creating application image
        final Path log4jPath = STORE_ROOT.resolve("log4j2.xml");
        if (!Files.exists(log4jPath)) {
            Path logCfgFile = InternalContextCache.INTERNAL_CONFIGURATION_CACHE
                    .<FileSystem>getAdditionalConfig(CK_JRT_FILE_SYSTEM)
                    .get()
                    .getPath("modules", "com.ntak.pearlzip.archive", "log4j2.xml")
                    .toAbsolutePath();
            try(InputStream is = Files.newInputStream(logCfgFile)) {
                Files.copy(is, log4jPath);
            } catch(Exception e) {
            }
        }

        try {
            ConfigurationSource source = new ConfigurationSource(new FileInputStream(log4jPath.toString()));
            Configurator.initialize(null, source);
        } catch (Exception e) {
        }

        // Setting Locale
        Locale.setDefault(genLocale(System.getProperties()));
        Path RUNTIME_MODULE_PATH = InternalContextCache.INTERNAL_CONFIGURATION_CACHE.<Path>getAdditionalConfig(CK_RUNTIME_MODULE_PATH).get();
        LOG_BUNDLE = ModuleUtil.loadLangPackDynamic(RUNTIME_MODULE_PATH,
                                                    System.getProperty(CNS_RES_BUNDLE, "pearlzip"),
                                                    Locale.getDefault());
        CUSTOM_BUNDLE = ModuleUtil.loadLangPackDynamic(RUNTIME_MODULE_PATH,
                                                       System.getProperty(CNS_CUSTOM_RES_BUNDLE, "custom"),
                                                       Locale.getDefault());
    }
}
