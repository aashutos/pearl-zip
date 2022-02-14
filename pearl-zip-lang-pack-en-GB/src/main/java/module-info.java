/*
 * Copyright © 2022 92AK
 */

import com.ntak.pearlzip.PearlZipEnProvider;

/**
 *  Language pack for Pearl Zip for British English.
 */
module com.ntak.pearlzip.lang.enGB {
    requires com.ntak.pearlzip.archive;
    requires javafx.base;

    provides com.ntak.pearlzip.archive.pub.PearlZipResourceBundleProvider with PearlZipEnProvider;
}