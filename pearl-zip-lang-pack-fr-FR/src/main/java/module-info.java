/*
 * Copyright © 2022 92AK
 */

import com.ntak.pearlzip.PearlZipFrProvider;

/**
 *  Language pack for Pearl Zip for French.
 */
module com.ntak.pearlzip.lang.frFR {
    requires com.ntak.pearlzip.archive;
    requires javafx.base;

    provides com.ntak.pearlzip.archive.pub.PearlZipResourceBundleProvider with PearlZipFrProvider;
}