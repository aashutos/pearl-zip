/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.archive.constants;

/**
 *  Unique identifiers for configuration files and for parameters.
 *  @author Aashutos Kakshepati
 */
public class ConfigurationConstants {
    public static final String CNS_NTAK_PEARL_ZIP_ICON_FILE = "configuration.ntak.pearl-zip.icon.file";
    public static final String CNS_NTAK_PEARL_ZIP_ICON_FOLDER = "configuration.ntak.pearl-zip.icon.folder";
    public static final String CNS_NTAK_PEARL_ZIP_LICENSE_SERVICE_CANONICAL_NAME = "configuration.ntak.pearl-zip.license-service-canonical-name";
    public static final String CNS_LOCALE_LANG = "configuration.ntak.pearl-zip.locale.lang";
    public static final String CNS_LOCALE_COUNTRY = "configuration.ntak.pearl-zip.locale.country";
    public static final String CNS_LOCALE_VARIANT = "configuration.ntak.pearl-zip.locale.variant";
    public static final String CNS_RES_BUNDLE = "configuration.ntak.pearl-zip.resource-bundle";
    public static final String CNS_CUSTOM_RES_BUNDLE = "configuration.ntak.pearl-zip.custom-resource-bundle";
    public static final String CNS_TMP_DIR_PREFIX = "configuration.ntak.tmp-dir-prefix";
    public static final String CNS_COM_BUS_FACTORY = "configuration.ntak.com-bus-factory";

    public static final String CNS_NTAK_PEARL_ZIP_JDBC_URL = "configuration.ntak.pearl-zip.jdbc.url";
    public static final String CNS_NTAK_PEARL_ZIP_JDBC_USER = "configuration.ntak.pearl-zip.jdbc.user";
    public static final String CNS_NTAK_PEARL_ZIP_JDBC_PASSWORD = "configuration.ntak.pearl-zip.jdbc.password";
    public static final String CNS_NTAK_PEARL_ZIP_KEYSTORE_PASSWORD = "configuration.ntak.pearl-zip.keystore.password";
    public static final String CNS_NTAK_PEARL_ZIP_TRUSTSTORE_PASSWORD = "configuration.ntak.pearl-zip.truststore.password";

    public static final String CNS_JAVAX_NET_SSL_KEYSTORE = "javax.net.ssl.keyStore";
    public static final String CNS_JAVAX_NET_SSL_TRUSTSTORE = "javax.net.ssl.trustStore";
    public static final String CNS_JAVAX_NET_SSL_KEYSTORETYPE = "javax.net.ssl.keyStoreType";
    public static final String CNS_JAVAX_NET_SSL_TRUSTSTORETYPE = "javax.net.ssl.trustStoreType";
    public static final String CNS_JAVAX_NET_SSL_KEYSTORE_PASSWORD = "javax.net.ssl.keyStorePassword";
    public static final String CNS_JAVAX_NET_SSL_TRUSTSTORE_PASSWORD = "javax.net.ssl.trustStorePassword";

    public static final String KEY_SESSION_ID = "session-id";
    public static final String KEY_FILE_PATH = "file-path";
    public static final String KEY_FILE_REPLACE = "file-replace";
    public static final String KEY_ICON_REF = "icon-ref";
    public static final String KEY_DEFAULT = "default";

    public static final String TMP_DIR_PREFIX = System.getProperty(CNS_TMP_DIR_PREFIX,"pz");
    public static final String REGEX_TIMESTAMP_DIR = "pz\\d+";
}
