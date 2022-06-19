/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.archive.constants;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.util.ResourceBundle;

/**
 *  Logging keys for Resource Bundles.
 *  @author Aashutos Kakshepati
 */
public class LoggingConstants {

    public static final Logger ROOT_LOGGER = LoggerContext.getContext().getRootLogger();

    public static final String LOG_ARCHIVE_SERVICE_FORMAT = "logging.ntak.pearl-zip.archive-service.format";
    public static final String LOG_ARCHIVE_SERVICE_NUMBER_ITEMS = "logging.ntak.pearl-zip.archive-service.no-items";
    public static final String LOG_ARCHIVE_READ_ZIP_PROPERTY = "logging.ntak.pearl-zip.archive-service.read-zip-property";
    public static final String LOG_ARCHIVE_SERVICE_ZIP_ENTRY = "logging.ntak.pearl-zip.archive-service.read-zip-entry";

    public static final String LOG_ARCHIVE_SERVICE_CREATE_EXCEPTION = "logging.ntak.pearl-zip.archive-service.create.exception";
    public static final String TITLE_ARCHIVE_SERVICE_CREATE_EXCEPTION = "title.ntak.pearl-zip.archive-service.create.exception";
    public static final String HEADER_ARCHIVE_SERVICE_CREATE_EXCEPTION = "header.ntak.pearl-zip.archive-service.create.exception";
    public static final String BODY_ARCHIVE_SERVICE_CREATE_EXCEPTION = "body.ntak.pearl-zip.archive-service.create.exception";

    public static final String LOG_ARCHIVE_SERVICE_CREATE_ISSUE = "logging.ntak.pearl-zip.archive-service.create.exception";
    public static final String TITLE_ARCHIVE_SERVICE_CREATE_ISSUE = "title.ntak.pearl-zip.archive-service.create.exception";
    public static final String HEADER_ARCHIVE_SERVICE_CREATE_ISSUE = "header.ntak.pearl-zip.archive-service.create.exception";
    public static final String BODY_ARCHIVE_SERVICE_CREATE_ISSUE = "body.ntak.pearl-zip.archive-service.create.exception";

    public static final String LOG_ARCHIVE_SERVICE_ADD_EXCEPTION = "logging.ntak.pearl-zip.archive-service.add.exception";
    public static final String LOG_ARCHIVE_SERVICE_COM_BUS_INIT_ERROR = "logging.ntak.pearl-zip.archive-service.com-bus-init-error";

    public static final String LOG_TRANSFORM_EXCEPTION = "logging.ntak.pearl-zip.transform.exception";
    public static final String LOG_SKIP_SYMLINK = "logging.ntak.pearl-zip.skip-symlink";

    public static final String LOG_ARCHIVE_INFO_ASSERT_PATH = "logging.ntak.pearl-zip.archive-info.assert.path";
    public static final String LOG_ARCHIVE_INFO_ASSERT_READ_SERVICE = "logging.ntak.pearl-zip.archive-info.assert.read-service";

    public static final String LOG_ARCHIVE_SERVICE_EXTRACT_EXCEPTION = "logging.ntak.pearl-zip.archive-service.extract.exception";
    public static final String TITLE_ARCHIVE_SERVICE_EXTRACT_EXCEPTION = "title.ntak.pearl-zip.archive-service.extract.exception";
    public static final String HEADER_ARCHIVE_SERVICE_EXTRACT_EXCEPTION = "header.ntak.pearl-zip.archive-service.extract.exception";
    public static final String BODY_ARCHIVE_SERVICE_EXTRACT_EXCEPTION = "body.ntak.pearl-zip.archive-service.extract.exception";

    public static final String LOG_ARCHIVE_SERVICE_LISTING_EXCEPTION = "logging.ntak.pearl-zip.archive-service.listing.exception";
    public static final String TITLE_ARCHIVE_SERVICE_LISTING_EXCEPTION = "title.ntak.pearl-zip.archive-service.listing.exception";
    public static final String HEADER_ARCHIVE_SERVICE_LISTING_EXCEPTION = "header.ntak.pearl-zip.archive-service.listing.exception";
    public static final String BODY_ARCHIVE_SERVICE_LISTING_EXCEPTION = "body.ntak.pearl-zip.archive-service.listing.exception";

    public static final String LBL_PROGRESS_CLEAR_UP = "label.ntak.pearl-zip.progress.clear-up";
    public static final String LBL_PROGRESS_DELETED_ENTRIES = "label.ntak.pearl-zip.progress.deleted-entries";
    public static final String LBL_PROGRESS_DELETING_ENTRIES = "label.ntak.pearl-zip.progress.deleting-entries";
    public static final String LBL_PROGRESS_LOADED_ENTRY = "label.ntak.pearl-zip.progress.loaded-entry";
    public static final String LBL_PROGRESS_EXTRACT_ENTRY = "label.ntak.pearl-zip.progress.extract-entry";
    public static final String LBL_PROGRESS_COMPLETION = "label.ntak.pearl-zip.progress.completion";
    public static final String LBL_PROGRESS_LOADING = "label.ntak.pearl-zip.progress.loading";

    // QUEUE KEYS
    public static final String PROGRESS = "PROGRESS";
    public static final String COMPLETED = "COMPLETED";
    public static final String ERROR = "ERROR";
    public static ResourceBundle CUSTOM_BUNDLE;
    public static ResourceBundle LOG_BUNDLE;
}
