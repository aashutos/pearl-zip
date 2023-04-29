/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.stages.startup;

import com.ntak.pearlzip.archive.util.LoggingUtil;
import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.pub.ZipLauncher;
import com.ntak.pearlzip.ui.util.AbstractStartupStage;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.KeyStore;

import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.*;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.ROOT_LOGGER;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;

/**
 *  Sets up keystore and truststore from PearlZip if a custom stores have not been set beforehand and registers them for
 *  use by PearlZip and any encrypted connections.
 *
 *  @author Aashutos Kakshepati
 */
public class KeystoreStartupStage extends AbstractStartupStage {

    @Override
    public void executeProcess() throws Exception {
        ////////////////////////////////////////////
        ///// KeyStore Setup //////////////////////
        //////////////////////////////////////////

        // Root store folder
        Path storePath = InternalContextCache.GLOBAL_CONFIGURATION_CACHE
                                             .<Path>getAdditionalConfig(CK_STORE_ROOT)
                                             .get()
                                             .resolve(".store");
        if (Files.notExists(storePath)) {
            Files.createDirectories(storePath);
        }

        // Key Stores
        // Load key store and trust store
        try(InputStream kis = ZipLauncher.class.getClassLoader().getResourceAsStream("keystore");
            InputStream tis = ZipLauncher.class.getClassLoader().getResourceAsStream("truststore");
            InputStream cis = ZipLauncher.class.getClassLoader().getResourceAsStream("root.crt")) {

            // Postgres certificate setup...
            String crtPathString = Paths.get(System.getProperty("user.home"),".postgresql", "root.crt")
                                        .toString();
            final Path certTargetPath = Paths.get(crtPathString);
            if (Files.notExists(certTargetPath.getParent())) {
                Files.createDirectories(certTargetPath.getParent());
            }
            if (Files.exists(certTargetPath)) {
                Files.deleteIfExists(certTargetPath);
            }
            Files.copy(cis, certTargetPath, StandardCopyOption.REPLACE_EXISTING);

            // Copy KeyStore files only if it does not exist already
            String keystorePathString = Paths.get(storePath.toString(), "keystore.jks")
                                             .toString();
            final Path keystorePath = Paths.get(keystorePathString);
            if (!Files.exists(keystorePath)) {
                Files.copy(kis, keystorePath);
            }

            System.setProperty(CNS_JAVAX_NET_SSL_KEYSTORE, keystorePathString);
            System.setProperty(CNS_JAVAX_NET_SSL_KEYSTORETYPE, System.getProperty(CNS_JAVAX_NET_SSL_KEYSTORETYPE));
            System.setProperty(CNS_JAVAX_NET_SSL_KEYSTORE_PASSWORD, System.getProperty(CNS_NTAK_PEARL_ZIP_KEYSTORE_PASSWORD));

            // Copy truststore files only if it does not exist already
            String truststorePathString = Paths.get(storePath.toString(), "truststore.jks")
                                               .toString();
            final Path truststorePath = Paths.get(truststorePathString);
            if (!Files.exists(truststorePath)) {
                Files.copy(tis, truststorePath);
            }
            System.setProperty(CNS_JAVAX_NET_SSL_TRUSTSTORE, truststorePathString);
            System.setProperty(CNS_JAVAX_NET_SSL_TRUSTSTORETYPE, System.getProperty(CNS_JAVAX_NET_SSL_TRUSTSTORETYPE));
            System.setProperty(CNS_JAVAX_NET_SSL_TRUSTSTORE_PASSWORD, System.getProperty(CNS_NTAK_PEARL_ZIP_TRUSTSTORE_PASSWORD));

            try(InputStream ist = Files.newInputStream(truststorePath);
                InputStream isk = Files.newInputStream(keystorePath)) {

                // Initialise Trust Stores...
                SSLContext context = SSLContext.getInstance("SSL");
                TrustManagerFactory tsFactory = TrustManagerFactory
                        .getInstance(TrustManagerFactory.getDefaultAlgorithm());
                KeyStore ts = KeyStore.getInstance(System.getProperty(CNS_JAVAX_NET_SSL_TRUSTSTORETYPE));
                ts.load(ist, System.getProperty(CNS_NTAK_PEARL_ZIP_TRUSTSTORE_PASSWORD).toCharArray());
                tsFactory.init(ts);

                // Initialise Key Stores...
                KeyManagerFactory kmFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                KeyStore ks = KeyStore.getInstance(System.getProperty(CNS_JAVAX_NET_SSL_KEYSTORETYPE));
                ks.load(isk, System.getProperty(CNS_NTAK_PEARL_ZIP_KEYSTORE_PASSWORD).toCharArray());
                kmFactory.init(ks, System.getProperty(CNS_NTAK_PEARL_ZIP_KEYSTORE_PASSWORD).toCharArray());

                // Initialise context...
                context.init(kmFactory.getKeyManagers(), tsFactory.getTrustManagers(), new java.security.SecureRandom());
                InternalContextCache.INTERNAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_SSL_CONTEXT, context);
                HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
            } catch (Exception exc) {
                throw exc;
            }
        } catch(Exception e) {
            // LOG: Issue setting up key stores. Exception message: %s\nStack trace:\n%s
            ROOT_LOGGER.warn(resolveTextKey(LOG_ISSUE_SETTING_UP_KEYSTORE, e.getMessage(),
                                            LoggingUtil.getStackTraceFromException(e)));
        }
    }
}
