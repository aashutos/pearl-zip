/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip;

import com.ntak.pearlzip.archive.pub.PearlZipResourceBundleProvider;
import javafx.util.Pair;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class PearlZipFrProvider implements PearlZipResourceBundleProvider {
    public ResourceBundle getBundle(String baseName, Locale locale) {
        return ResourceBundle.getBundle(baseName,locale);
    }

    @Override
    public List<Pair<String,Locale>> providedLanguages() {
        return List.of(new Pair<>("French (France)", new Locale("fr","FR")));
    }
}
