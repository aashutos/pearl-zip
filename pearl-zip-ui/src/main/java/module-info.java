/*
 * Copyright © 2021 92AK
 */
import com.ntak.pearlzip.archive.pub.ArchiveReadService;
import com.ntak.pearlzip.archive.pub.ArchiveWriteService;

/**
 *  General UI/front-end JavaFX code for the Pearl Zip application.
 */
module com.ntak.pearlzip.ui {
    exports com.ntak.pearlzip.ui.cell;
    exports com.ntak.pearlzip.ui.pub;
    exports com.ntak.pearlzip.ui.util;
    opens com.ntak.pearlzip.ui.util;

    // UI dependencies
    requires java.desktop;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires nsmenufx;

    // PearlZip dependencies
    requires com.ntak.pearlzip.archive;
    requires com.ntak.pearlzip.lang.enGB;
    requires com.ntak.pearlzip.archive.szjb;
    requires com.ntak.pearlzip.archive.acc;
    requires com.ntak.pearlzip.license;

    requires sevenzipjbinding;

    opens com.ntak.pearlzip.ui.pub;
    exports com.ntak.pearlzip.ui.constants;
    opens com.ntak.pearlzip.ui.model;
    exports com.ntak.pearlzip.ui.mac;
    opens com.ntak.pearlzip.ui.mac;
    exports com.ntak.pearlzip.ui.model;

    // Logging dependencies
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;

    requires eventbus;

    // SPI Definition
    uses ArchiveWriteService;
    uses ArchiveReadService;
}