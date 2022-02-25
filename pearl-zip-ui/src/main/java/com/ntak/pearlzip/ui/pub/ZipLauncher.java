/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.pub;

import com.ntak.pearlzip.archive.model.PluginInfo;
import com.ntak.pearlzip.archive.pub.LicenseService;
import com.ntak.pearlzip.archive.util.LoggingUtil;
import com.ntak.pearlzip.ui.constants.ZipConstants;
import com.ntak.pearlzip.ui.mac.MacPearlZipApplication;
import com.ntak.pearlzip.ui.model.ZipState;
import com.ntak.pearlzip.ui.rules.*;
import com.ntak.pearlzip.ui.util.*;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

import java.awt.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.ntak.pearlzip.archive.constants.ArchiveConstants.CURRENT_SETTINGS;
import static com.ntak.pearlzip.archive.constants.ArchiveConstants.WORKING_SETTINGS;
import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.*;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.*;
import static com.ntak.pearlzip.archive.util.LoggingUtil.genLocale;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.JFXUtil.initialiseBootstrapProperties;

/**
 *  Loads the main UI screen for the Zip Application.
 *  @author Aashutos Kakshepati
*/
public class ZipLauncher {

    public static final CopyOnWriteArrayList<String> OS_FILES = new CopyOnWriteArrayList<>();

    // Reference: https://github.com/eschmar/javafx-custom-file-ext-boilerplate
    static {
        if (Desktop.getDesktop().isSupported(Desktop.Action.APP_OPEN_FILE)) {
            Desktop.getDesktop().setOpenFileHandler((e)-> e.getFiles().stream().map(File::getAbsolutePath).forEach(l -> {
                try {
                    OS_FILES.add(l);
                } catch(Exception exc) {
                }
            }));
        }
    }

    public static void main(String[] args) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException, InterruptedException, IOException {
        // Dynamically load launcher...
        Properties props = new Properties();
        props.load(MacPearlZipApplication.class.getClassLoader()
                                               .getResourceAsStream("application.properties"));
        Class<?> klass = Class.forName(props.getProperty(CNS_LAUNCHER_CANONICAL_NAME));
        Method mainMethod = klass.getMethod("main", String[].class);
        mainMethod.invoke(null, (Object) args);

        // Wait for latch unless countdown was not triggered due to race. A break check is initiated in this case.
       while (!APP_LATCH.await(300, TimeUnit.MILLISECONDS)) {
           if (JFXUtil.getMainStageInstances().size() == 0)  {
               break;
           }
       }

       Runtime.getRuntime().exit(0);
    }

    public static void initialize() throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        // Load bootstrap properties
        Properties props = initialiseBootstrapProperties();

        ////////////////////////////////////////////
        ///// Settings File Load ///////////////////
        ////////////////////////////////////////////

        SETTINGS_FILE = Paths.get(System.getProperty(CNS_SETTINGS_FILE, Paths.get(STORE_ROOT.toString(),
                                                     "settings.properties").toString()));
        if (!Files.exists(SETTINGS_FILE)) {
            Files.createFile(SETTINGS_FILE);
        }
        try(InputStream settingsIStream = Files.newInputStream(SETTINGS_FILE)) {
            CURRENT_SETTINGS.load(settingsIStream);
            WORKING_SETTINGS.load(settingsIStream);
        }

        ////////////////////////////////////////////
        ///// Log4j Setup /////////////////////////
        //////////////////////////////////////////

        // Create root store
        Files.createDirectories(ZipConstants.STORE_ROOT);

        // Log4j configuration - handle fixed parameters when creating application image
        String log4jCfg = Paths.get(STORE_ROOT.toString(), "log4j2.xml")
                               .toString();
        final Path log4jPath = Paths.get(log4jCfg);
        if (!Files.exists(log4jPath)) {
            Path logCfgFile = JRT_FILE_SYSTEM.getPath("modules", "com.ntak.pearlzip.archive", "log4j2.xml");
            try(InputStream is = Files.newInputStream(logCfgFile)) {
                Files.copy(is, log4jPath);
            } catch(Exception e) {

            }
        }
        ConfigurationSource source = new ConfigurationSource(new FileInputStream(log4jCfg));
        Configurator.initialize(null, source);

        // Setting Locale
        Locale.setDefault(genLocale(props));
        LOG_BUNDLE = ModuleUtil.loadLangPackDynamic(RUNTIME_MODULE_PATH,
                                                        System.getProperty(CNS_RES_BUNDLE, "pearlzip"),
                                              Locale.getDefault());
        CUSTOM_BUNDLE = ModuleUtil.loadLangPackDynamic(RUNTIME_MODULE_PATH,
                                                       System.getProperty(CNS_CUSTOM_RES_BUNDLE,"custom"),
                                        Locale.getDefault());

        // Load License Declarations
        try {
            LicenseService licenseService = (LicenseService) Class.forName(System.getProperty(
                                                                          CNS_NTAK_PEARL_ZIP_LICENSE_SERVICE_CANONICAL_NAME,
                                                                          "com.ntak.pearlzip.license.pub.PearlZipLicenseService"))
                                                                  .getDeclaredConstructor()
                                                                  .newInstance();
            licenseService.retrieveDeclaredLicenses()
                          .forEach(ZipState::addLicenseDeclaration);
        } catch (Exception e) {

        }

        ////////////////////////////////////////////
        ///// KeyStore Setup //////////////////////
        //////////////////////////////////////////

        // Root store folder
        Path storePath = Paths.get(STORE_ROOT.toString(), ".store");
        if (Files.notExists(storePath)) {
            Files.createDirectories(storePath);
        }

        // Key Stores
        // Load key store and trust store
        try(InputStream kis = new BufferedInputStream(ZipLauncher.class.getClassLoader().getResourceAsStream("keystore.jks"));
            InputStream tis = ZipLauncher.class.getClassLoader().getResourceAsStream("truststore.jks")) {

            // Copy KeyStore files
            String keystorePathString = Paths.get(storePath.toString(), "keystore.jks")
                                             .toString();
            final Path keystorePath = Paths.get(keystorePathString);
            if (!Files.exists(keystorePath)) {
                Files.copy(kis, keystorePath);
            }
            System.setProperty(CNS_JAVAX_NET_SSL_KEYSTORE, keystorePathString);
            System.setProperty(CNS_JAVAX_NET_SSL_KEYSTORE_PASSWORD, System.getProperty(CNS_NTAK_PEARL_ZIP_KEYSTORE_PASSWORD));

            // Copy truststore files
            String truststorePathString = Paths.get(storePath.toString(), "truststore.jks")
                                               .toString();
            final Path truststorePath = Paths.get(truststorePathString);
            if (!Files.exists(truststorePath)) {
                Files.copy(tis, truststorePath);
            }
            System.setProperty(CNS_JAVAX_NET_SSL_TRUSTSTORE, truststorePathString);
            System.setProperty(CNS_JAVAX_NET_SSL_TRUSTSTORE_PASSWORD, System.getProperty(CNS_NTAK_PEARL_ZIP_TRUSTSTORE_PASSWORD));
        } catch(Exception e) {
            // LOG: Issue setting up key stores. Exception message: %s\nStack trace:\n%s
            ROOT_LOGGER.warn(resolveTextKey(LOG_ISSUE_SETTING_UP_KEYSTORE, e.getMessage(),
                                            LoggingUtil.getStackTraceFromException(e)));
        }

        ////////////////////////////////////////////
        ///// Plugin Manifest Load ////////////////
        //////////////////////////////////////////

        // Loading rules...
        MANIFEST_RULES.add(new MinVersionManifestRule());
        MANIFEST_RULES.add(new MaxVersionManifestRule());
        MANIFEST_RULES.add(new LicenseManifestRule());
        MANIFEST_RULES.add(new CheckLibManifestRule());
        MANIFEST_RULES.add(new RemovePatternManifestRule());
        MANIFEST_RULES.add(new ThemeManifestRule());

        // Loading plugin manifests...
        LOCAL_MANIFEST_DIR = Paths.get(STORE_ROOT.toAbsolutePath().toString(), "manifests");
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

        if (Files.isDirectory(RUNTIME_MODULE_PATH)) {
            // LOG: Loading modules from path: %s
            ROOT_LOGGER.info(resolveTextKey(LOG_LOADING_MODULE, RUNTIME_MODULE_PATH.toAbsolutePath().toString()));
            ModuleUtil.loadModulesDynamic(RUNTIME_MODULE_PATH);
        } else {
            ModuleUtil.loadModulesStatic();
        }

        // Initialising Thread Pool
        String klassName;
        MetricProfile profile = MetricProfile.getDefaultProfile();
        if (Objects.nonNull(klassName = System.getProperty(CNS_METRIC_FACTORY))) {
            try {
                MetricProfileFactory factory = (MetricProfileFactory) Class.forName(klassName)
                                                                           .getDeclaredConstructor()
                                                                           .newInstance();
                profile = factory.getProfile();
            } catch(Exception e) {

            }
            PRIMARY_EXECUTOR_SERVICE =
                    Executors.newScheduledThreadPool(Math.max(Integer.parseInt(System.getProperty(
                            CNS_THREAD_POOL_SIZE,
                            "4")), 1),
                                                     MetricThreadFactory.create(profile));
        } else {
            PRIMARY_EXECUTOR_SERVICE =
                    Executors.newScheduledThreadPool(Math.max(Integer.parseInt(System.getProperty(
                            CNS_THREAD_POOL_SIZE,
                            "4")), 1),
                                                     MetricThreadFactory.create(MetricProfile.getDefaultProfile()));
        }
    }
}
