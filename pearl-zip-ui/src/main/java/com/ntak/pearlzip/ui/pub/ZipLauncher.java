/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.pub;

import com.ntak.pearlzip.archive.constants.ConfigurationConstants;
import com.ntak.pearlzip.archive.constants.LoggingConstants;
import com.ntak.pearlzip.license.pub.LicenseService;
import com.ntak.pearlzip.license.pub.PearlZipLicenseService;
import com.ntak.pearlzip.ui.constants.ZipConstants;
import com.ntak.pearlzip.ui.mac.MacPearlZipApplication;
import com.ntak.pearlzip.ui.model.ZipState;
import com.ntak.pearlzip.ui.util.MetricProfile;
import com.ntak.pearlzip.ui.util.MetricProfileFactory;
import com.ntak.pearlzip.ui.util.MetricThreadFactory;
import com.ntak.pearlzip.ui.util.ModuleUtil;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

import java.awt.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;

import static com.ntak.pearlzip.archive.constants.ArchiveConstants.CURRENT_SETTINGS;
import static com.ntak.pearlzip.archive.constants.ArchiveConstants.WORKING_SETTINGS;
import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.CNS_RES_BUNDLE;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.LOG_BUNDLE;
import static com.ntak.pearlzip.archive.util.LoggingUtil.genLocale;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.ArchiveUtil.initialiseApplicationSettings;

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

       APP_LATCH.await();
       Runtime.getRuntime().exit(0);
    }

    public static void initialize() throws IOException {
        // Load bootstrap properties
        Properties props = new Properties();
        props.load(MacPearlZipApplication.class.getClassLoader()
                                               .getResourceAsStream("application.properties"));
        ZipConstants.STORE_ROOT = Paths.get(System.getProperty(CNS_STORE_ROOT, String.format("%s/.pz",
                                                                                             System.getProperty(
                                                                                                     "user.home"))));
        ZipConstants.LOCAL_TEMP =
                Paths.get(Optional.ofNullable(System.getenv("TMPDIR"))
                                  .orElse(STORE_ROOT.toString()));
        APPLICATION_SETTINGS_FILE = Paths.get(STORE_ROOT.toString(), "application.properties");
        initialiseApplicationSettings();

        String defaultModulePath = Path.of(STORE_ROOT.toAbsolutePath().toString(), "providers").toString();
        ZipConstants.RUNTIME_MODULE_PATH =
                Paths.get(System.getProperty(CNS_NTAK_PEARL_ZIP_MODULE_PATH, defaultModulePath)).toAbsolutePath();

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

        // Overwrite with external properties file
        // Reserved properties are kept as per internal key definition
        Map<String,String> reservedKeyMap = new HashMap<>();
        Path tmpRK = Paths.get(STORE_ROOT.toString(), "rk");
        try (FileOutputStream fileOutputStream = new FileOutputStream(tmpRK.toString());
             FileChannel channel = fileOutputStream.getChannel();
             FileLock lock = channel.lock()) {
                // Standard resource case
                Path reservedKeys = Paths.get(MacPearlZipApplication.class.getClassLoader()
                                                                          .getResource("reserved-keys")
                                                                          .getPath());
                LoggingConstants.ROOT_LOGGER.info(reservedKeys);

                // standard/jar resource case
                if (Objects.nonNull(reservedKeys)) {

                    try (InputStream is = MacPearlZipApplication.class.getClassLoader()
                                                                      .getResourceAsStream("reserved-keys")) {
                        Files.copy(is, tmpRK, StandardCopyOption.REPLACE_EXISTING);
                    }
                } else if (!Files.exists(reservedKeys)) {
                    reservedKeys = JRT_FILE_SYSTEM.getPath("modules", "com.ntak.pearlzip.ui", "reserved-keys");
                    Files.copy(reservedKeys, tmpRK, StandardCopyOption.REPLACE_EXISTING);
                }

            Files.lines(tmpRK)
                 .filter(k -> Objects.nonNull(k) && Objects.nonNull(props.getProperty(k)))
                 // LOG: Locking in key: %s with value: %s
                 .peek(k -> LoggingConstants.ROOT_LOGGER.info(resolveTextKey(LOG_LOCKING_IN_PROPERTY,
                                                                             k,
                                                                             props.getProperty(k))))
                 .forEach(k -> reservedKeyMap.put(k, props.getProperty(k)));
        }

        if (Files.exists(APPLICATION_SETTINGS_FILE)) {
            props.load(Files.newBufferedReader(APPLICATION_SETTINGS_FILE));
        }

        props.putAll(System.getProperties());
        props.putAll(reservedKeyMap);
        System.setProperties(props);
        LoggingConstants.ROOT_LOGGER.info(props);

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
        ResourceBundle.getBundle(System.getProperty(ConfigurationConstants.CNS_CUSTOM_RES_BUNDLE, "custom"),
                                 Locale.getDefault());
        LOG_BUNDLE = ResourceBundle.getBundle(System.getProperty(CNS_RES_BUNDLE, "pearlzip"),
                                              Locale.getDefault());

        // Load License Declarations
        LicenseService licenseService = new PearlZipLicenseService();
        licenseService.retrieveDeclaredLicenses()
                      .forEach(ZipState::addLicenseDeclaration);

        ////////////////////////////////////////////
        ///// Runtime Module Load /////////////////
        //////////////////////////////////////////

        if (Files.isDirectory(RUNTIME_MODULE_PATH)) {
            // LOG: Loading modules from path: %s
            LoggingConstants.ROOT_LOGGER.info(resolveTextKey(LOG_LOADING_MODULE, RUNTIME_MODULE_PATH.toAbsolutePath().toString()));
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
