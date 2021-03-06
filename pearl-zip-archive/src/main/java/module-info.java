/*
 *  Copyright (c) 2021 92AK
 */
/**
 *  Holds core business logic for processing archives, which when implemented can be used to interface with Pearl Zip
 *  as a provider.
 */
module com.ntak.pearlzip.archive {

    ///////////////////
    ///// Exports /////
    ///////////////////

    // Public package exported
    exports com.ntak.pearlzip.archive.util;
    exports com.ntak.pearlzip.archive.constants;
    exports com.ntak.pearlzip.archive.constants.internal to com.ntak.pearlzip.ui;
    exports com.ntak.pearlzip.archive.pub;
    exports com.ntak.pearlzip.archive.model;
    exports com.ntak.pearlzip.archive.pub.profile.component;

    ////////////////////
    ///// Requires /////
    ////////////////////

    // Logging dependencies
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires javafx.controls;
}
