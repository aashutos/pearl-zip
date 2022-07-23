/*
 * Copyright © 2022 92AK
 */

import com.ntak.pearlzip.archive.pub.ArchiveReadService;
import com.ntak.pearlzip.archive.pub.ArchiveWriteService;
import com.ntak.pearlzip.archive.pub.LicenseService;

/**
 *  General UI/front-end JavaFX code for the Pearl Zip application.
 */
module com.ntak.pearlzip.ui {

    // UI dependencies
    requires java.desktop;
    requires java.sql;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires nsmenufx;
    requires com.jfoenix;

    // PearlZip dependencies
    requires com.ntak.pearlzip.archive;

    // Logging dependencies
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;

    requires eventbus;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.xml;
    requires com.fasterxml.jackson.core;
    requires jakarta.xml.bind;

    // SPI Definition
    uses ArchiveWriteService;
    uses ArchiveReadService;
    uses LicenseService;
    uses com.ntak.pearlzip.archive.pub.PearlZipResourceBundleProvider;

    // Module Interfaces
    exports com.ntak.pearlzip.ui.cell;
    exports com.ntak.pearlzip.ui.constants;
    exports com.ntak.pearlzip.ui.mac;
    exports com.ntak.pearlzip.ui.model;
    exports com.ntak.pearlzip.ui.pub;
    exports com.ntak.pearlzip.ui.util;
    exports com.ntak.pearlzip.ui.util.internal to com.fasterxml.jackson.databind;

    opens com.ntak.pearlzip.ui.pub;
    opens com.ntak.pearlzip.ui.util;
    opens com.ntak.pearlzip.ui.model;
    opens com.ntak.pearlzip.ui.mac;
}