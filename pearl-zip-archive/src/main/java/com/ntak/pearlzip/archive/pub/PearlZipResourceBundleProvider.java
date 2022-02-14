/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.archive.pub;

import javafx.util.Pair;

import java.util.List;
import java.util.Locale;
import java.util.spi.ResourceBundleProvider;

/**
 *  Overlay interface on top of standard java ResourceBundleProvider to enable contracts for use with the PearlZip UI.
 *
 *  @author Aashutos Kakshepati
 */
public interface PearlZipResourceBundleProvider extends ResourceBundleProvider {

    List<Pair<String,Locale>> providedLanguages();

}
