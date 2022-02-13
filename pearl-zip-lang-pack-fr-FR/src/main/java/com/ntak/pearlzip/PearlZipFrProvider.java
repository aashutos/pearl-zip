/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.spi.ResourceBundleProvider;

public class PearlZipFrProvider implements ResourceBundleProvider {
    public ResourceBundle getBundle(String baseName, Locale locale) {
        return ResourceBundle.getBundle(baseName,locale);
    }
}
