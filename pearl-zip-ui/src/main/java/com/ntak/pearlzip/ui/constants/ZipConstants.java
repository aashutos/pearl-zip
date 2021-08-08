/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.constants;

import com.ntak.pearlzip.ui.pub.MacZipLauncher;
import com.ntak.pearlzip.ui.util.ErrorAlertConsumer;
import com.ntak.pearlzip.ui.util.ProgressMessageTraceLogger;
import de.jangassen.MenuToolkit;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *  General logging, configuration keys and other shared resources for the JavaFX UI.
 *  @author Aashutos Kakshepati
*/
public class ZipConstants {
    public static final String CNS_NTAK_PEARL_ZIP_NO_FILES_HISTORY = "configuration.ntak.pearl-zip.no-files-history";
    public static final String CNS_NTAK_PEARL_ZIP_RESIZEABLE = "configuration.ntak.pearl-zip.resizeable";
    public static final String CNS_NTAK_PEARL_ZIP_VERSION = "configuration.ntak.pearl-zip.version";
    public static final String CNS_NTAK_PEARL_ZIP_APP_NAME = "configuration.ntak.pearl-zip.app-name";
    public static final String CNS_NTAK_PEARL_ZIP_COPYRIGHT = "configuration.ntak.pearl-zip.copyright";
    public static final String CNS_NTAK_PEARL_ZIP_WEBLINK = "configuration.ntak.pearl-zip.weblink";
    public static final String CNS_NTAK_PEARL_ZIP_LICENSE_LOCATION = "configuration.ntak.pearl-zip.license-location";
    public static final String CNS_NTAK_PEARL_ZIP_LICENSE_OVERRIDE_LOCATION = "configuration.ntak.pearl-zip.license-override-location";
    public static final String CNS_NTAK_PEARL_ZIP_MODULE_PATH = "configuration.ntak.pearl-zip.module-path";
    public static final String CNS_PROVIDER_PRIORITY_ROOT_KEY = "configuration.ntak.pearl-zip.provider.priority.%s";
    public static final String CNS_STORE_ROOT = "configuration.ntak.pearl-zip.store.temp";
    public static final String CNS_SETTINGS_FILE = "configuration.ntak.pearl-zip.settings-file";
    public static final String CNS_WINDOW_HEIGHT = "configuration.ntak.pearl-zip.window-height";
    public static final String CNS_WINDOW_WIDTH = "configuration.ntak.pearl-zip.window-width";
    public static final String CNS_THREAD_POOL_SIZE = "configuration.ntak.pearl-zip.thread-pool-size";
    public static final String CNS_METRIC_FACTORY = "configuration.ntak.pearl-zip.metric-factory";
    public static final String CNS_CONCURRENCY_LOCK_POLL_TIMEOUT = "configuration.ntak.pearl-zip.concurrency.lock-poll-timeout";

    public static final String LOG_ARCHIVE_CAN_EXTRACT = "logging.ntak.pearl-zip.tar-can-extract";
    public static final String LOG_CLICKED_ROW = "logging.ntak.pearl-zip.clicked-row";
    public static final String LOG_ISSUE_EXTRACTING_FILE_FOR_COPY = "logging.ntak.pearl-zip.issue-extracting-file-for-copy";
    public static final String LOG_ISSUE_ADDING_FILE_FOR_COPY = "logging.ntak.pearl-zip.issue-adding-file-for-copy";
    public static final String LOG_PASTE_FILE_DETAILS = "logging.ntak.pearl-zip.paste-file-details";
    public static final String LOG_THREAD_EXECUTION_ISSUE = "logging.ntak.pearl-zip.thread-execution-issue";
    public static final String LOG_ISSUE_RUNNING_BACKGROUND_PROCESS = "logging.ntak.pearl-zip.issue-running-background-process";
    public static final String LOG_PROGRESS_MSG = "logging.ntak.pearl-zip.progress-msg";
    public static final String LOG_TEMP_DIRS_TO_DELETE = "logging.ntak.pearl-zip.temp-dirs-to-delete";

    public static final String LOG_ISSUE_RETRIEVE_META = "logging.ntak.pearl-zip.dragboard.issue-retrieve-meta";

    public static final String LOG_LICENSE_FILE_INFO = "logging.ntak.pearl-zip.license.license-file-info";

    public static final String LOG_OS_TRIGGER_DETECTED = "logging.ntak.pearl-zip.os-trigger-detected";

    public static final String LOG_ISSUE_INTEGRATING_CHANGES = "logging.ntak.pearl-zip.issue-integrating-changes";

    public static final String LOG_CREATE_ARCHIVE = "logging.ntak.pearl-zip.log-create-archive";

    public static final String LOG_LOCKING_IN_PROPERTY = "logging.ntak.pearl-zip.locking-in-property";

    public static final String LOG_SKIP_ADD_SELF = "logging.ntak.pearl-zip.skip-add-self";
    public static final String TITLE_SKIP_ADD_SELF = "title.ntak.pearl-zip.skip-add-self";
    public static final String HEADER_SKIP_ADD_SELF = "header.ntak.pearl-zip.skip-add-self";
    public static final String BODY_SKIP_ADD_SELF = "body.ntak.pearl-zip.skip-add-self";

    public static final String LOG_LOADING_MODULE = "logging.ntak.pearl-zip.loading-module";
    public static final String LOG_READ_SERVICES_IDENTIFIED = "logging.ntak.pearl-zip.read-services-identified";
    public static final String LOG_WRITE_SERVICES_IDENTIFIED = "logging.ntak.pearl-zip.write-services-identified";

    public static final String LOG_ARCHIVE_LOCKED = "logging.ntak.pearl-zip.archive-locked";
    public static final String TITLE_ARCHIVE_LOCKED = "title.ntak.pearl-zip.archive-locked";
    public static final String HEADER_ARCHIVE_LOCKED = "header.ntak.pearl-zip.archive-locked";
    public static final String BODY_ARCHIVE_LOCKED = "body.ntak.pearl-zip.archive-locked";

    public static final String LOG_ARCHIVE_DOES_NOT_EXIST = "logging.ntak.pearl-zip.archive-does-not-exist";
    public static final String TITLE_ARCHIVE_DOES_NOT_EXIST = "title.ntak.pearl-zip.archive-does-not-exist";
    public static final String HEADER_ARCHIVE_DOES_NOT_EXIST = "header.ntak.pearl-zip.archive-does-not-exist";
    public static final String BODY_ARCHIVE_DOES_NOT_EXIST = "body.ntak.pearl-zip.archive-does-not-exist";

    public static final String LOG_GENERAL_EVENT_HANDLER_EXCEPTION = "logging.ntak.pearl-zip.general-event-handler-exception";

    public static final String LOG_ARCHIVE_TEST_FAILED = "logging.ntak.pearl-zip.archive-test-failed";

    // Paste exception
    public static final String LOG_PASTE_EXCEPTION = "logging.ntak.pearl-zip.paste-exception";
    public static final String TITLE_PASTE_EXCEPTION = "title.ntak.pearl-zip.paste-exception";
    public static final String HEADER_PASTE_EXCEPTION = "header.ntak.pearl-zip.paste-exception";
    public static final String BODY_PASTE_EXCEPTION = "body.ntak.pearl-zip.paste-exception";

    // Issue creating stage
    public static final String LOG_ISSUE_CREATING_STAGE = "logging.ntak.pearl-zip.issue-creating-stage";
    public static final String TITLE_ISSUE_CREATING_STAGE = "title.ntak.pearl-zip.issue-creating-stage";
    public static final String HEADER_ISSUE_CREATING_STAGE = "header.ntak.pearl-zip.issue-creating-stage";
    public static final String BODY_ISSUE_CREATING_STAGE = "body.ntak.pearl-zip.issue-creating-stage";

    // Add function not supported
    public static final String LOG_ADD_FUNC_NOT_SUPPORTED = "logging.ntak.pearl-zip.add-func.not-supported";
    public static final String TITLE_ADD_FUNC_NOT_SUPPORTED = "title.ntak.pearl-zip.add-func.not-supported";
    public static final String HEADER_ADD_FUNC_NOT_SUPPORTED = "header.ntak.pearl-zip.add-func.not-supported";
    public static final String BODY_ADD_FUNC_NOT_SUPPORTED = "body.ntak.pearl-zip.add-func.not-supported";

    // Delete function not supported
    public static final String LOG_DEL_FUNC_NOT_SUPPORTED = "logging.ntak.pearl-zip.del-func.not-supported";
    public static final String TITLE_DEL_FUNC_NOT_SUPPORTED = "title.ntak.pearl-zip.del-func.not-supported";
    public static final String HEADER_DEL_FUNC_NOT_SUPPORTED = "header.ntak.pearl-zip.del-func.not-supported";
    public static final String BODY_DEL_FUNC_NOT_SUPPORTED = "body.ntak.pearl-zip.del-func.not-supported";

    // Extract function not supported
    public static final String LOG_EXT_FUNC_NOT_SUPPORTED = "logging.ntak.pearl-zip.ext-func.not-supported";
    public static final String TITLE_EXT_FUNC_NOT_SUPPORTED = "title.ntak.pearl-zip.ext-func.not-supported";
    public static final String HEADER_EXT_FUNC_NOT_SUPPORTED = "header.ntak.pearl-zip.ext-func.not-supported";
    public static final String BODY_EXT_FUNC_NOT_SUPPORTED = "body.ntak.pearl-zip.ext-func.not-supported";

    // Issue deleting file
    public static final String TITLE_ISSUE_DELETE_FILE = "title.ntak.pearl-zip.issue-delete-file";
    public static final String HEADER_ISSUE_DELETE_FILE = "header.ntak.pearl-zip.issue-delete-file";
    public static final String BODY_ISSUE_DELETE_FILE = "body.ntak.pearl-zip.issue-delete-file";

    // No file selected
    public static final String LOG_NO_FILE_SELECTED = "logging.ntak.pearl-zip.no-file-selected";
    public static final String TITLE_NO_FILE_SELECTED = "title.ntak.pearl-zip.no-file-selected";
    public static final String HEADER_NO_FILE_SELECTED = "header.ntak.pearl-zip.no-file-selected";
    public static final String BODY_NO_FILE_SELECTED = "body.ntak.pearl-zip.no-file-selected";

    // No file selected
    public static final String LOG_NO_FILE_FOLDER_SELECTED = "logging.ntak.pearl-zip.no-file-folder-selected";
    public static final String TITLE_NO_FILE_FOLDER_SELECTED = "title.ntak.pearl-zip.no-file-folder-selected";
    public static final String HEADER_NO_FILE_FOLDER_SELECTED = "header.ntak.pearl-zip.no-file-folder-selected";
    public static final String BODY_NO_FILE_FOLDER_SELECTED = "body.ntak.pearl-zip.no-file-folder-selected";

    // Error opening tarball
    public static final String LOG_ERR_OPEN_NESTED_TARBALL = "logging.ntak.pearl-zip.err-open-nested-tarball";
    public static final String TITLE_ERR_OPEN_NESTED_TARBALL = "title.ntak.pearl-zip.err-open-nested-tarball";
    public static final String HEADER_ERR_OPEN_NESTED_TARBALL = "header.ntak.pearl-zip.err-open-nested-tarball";
    public static final String BODY_ERR_OPEN_NESTED_TARBALL = "body.ntak.pearl-zip.err-open-nested-tarball";

    // confirm save archive
    public static final String TITLE_CONFIRM_SAVE_ARCHIVE = "title.ntak.pearl-zip.confirm-save-archive";
    public static final String HEADER_CONFIRM_SAVE_ARCHIVE = "header.ntak.pearl-zip.confirm-save-archive";
    public static final String BODY_CONFIRM_SAVE_ARCHIVE = "body.ntak.pearl-zip.confirm-save-archive";

    // confirm save nested archive
    public static final String TITLE_CONFIRM_SAVE_NESTED_ARCHIVE = "title.ntak.pearl-zip.confirm-save-nested-archive";
    public static final String HEADER_CONFIRM_SAVE_NESTED_ARCHIVE = "header.ntak.pearl-zip.confirm-save-nested-archive";
    public static final String BODY_CONFIRM_SAVE_NESTED_ARCHIVE = "body.ntak.pearl-zip.confirm-save-nested-archive";

    // confirm save archive
    public static final String TITLE_CONFIRM_ADD_FILE = "title.ntak.pearl-zip.confirm-add-file";
    public static final String HEADER_CONFIRM_ADD_FILE = "header.ntak.pearl-zip.confirm-add-file";
    public static final String BODY_CONFIRM_ADD_FILE = "body.ntak.pearl-zip.confirm-add-file";

    // Test archive success
    public static final String TITLE_TEST_ARCHIVE_SUCCESS = "title.ntak.pearl-zip.test-archive-success";
    public static final String HEADER_TEST_ARCHIVE_SUCCESS = "header.ntak.pearl-zip.test-archive-success";
    public static final String BODY_TEST_ARCHIVE_SUCCESS = "body.ntak.pearl-zip.test-archive-success";

    // Open file externally
    public static final String TITLE_OPEN_EXT_FILE = "title.ntak.pearl-zip.open-ext-file";
    public static final String HEADER_OPEN_EXT_FILE = "header.ntak.pearl-zip.open-ext-file";
    public static final String BODY_OPEN_EXT_FILE = "body.ntak.pearl-zip.open-ext-file";

    // Open file externally
    public static final String TITLE_ERR_OPEN_FILE = "title.ntak.pearl-zip.err-open-file";
    public static final String HEADER_ERR_OPEN_FILE = "header.ntak.pearl-zip.err-open-file";
    public static final String BODY_ERR_OPEN_FILE = "body.ntak.pearl-zip.err-open-file";

    // Test archive failure
    public static final String TITLE_TEST_ARCHIVE_FAILURE = "title.ntak.pearl-zip.test-archive-failure";
    public static final String HEADER_TEST_ARCHIVE_FAILURE = "header.ntak.pearl-zip.test-archive-failure";
    public static final String BODY_TEST_ARCHIVE_FAILURE = "body.ntak.pearl-zip.test-archive-failure";

    // Cannot init copy
    public static final String LOG_CANNOT_INIT_COPY = "logging.ntak.pearl-zip.cannot-init-copy";
    public static final String TITLE_CANNOT_INIT_COPY = "title.ntak.pearl-zip.cannot-init-copy";
    public static final String HEADER_CANNOT_INIT_COPY = "header.ntak.pearl-zip.cannot-init-copy";
    public static final String BODY_CANNOT_INIT_COPY = "body.ntak.pearl-zip.cannot-init-copy";

    // Cannot init copy
    public static final String LOG_CANNOT_INIT_MOVE = "logging.ntak.pearl-zip.cannot-init-move";
    public static final String TITLE_CANNOT_INIT_MOVE = "title.ntak.pearl-zip.cannot-init-move";
    public static final String HEADER_CANNOT_INIT_MOVE = "header.ntak.pearl-zip.cannot-init-move";
    public static final String BODY_CANNOT_INIT_MOVE = "body.ntak.pearl-zip.cannot-init-move";

    // File does not exist (Open recent)
    public static final String TITLE_FILE_NOT_EXIST = "title.ntak.pearl-zip.file-not-exist";
    public static final String HEADER_FILE_NOT_EXIST = "header.ntak.pearl-zip.file-not-exist";
    public static final String BODY_FILE_NOT_EXIST = "body.ntak.pearl-zip.file-not-exist";

    // File selected does not exist (New single file compressor archive)
    public static final String LOG_FILE_SELECTED_DOES_NOT_EXIST = "logging.ntak.pearl-zip.file-selected-does-not-exist";
    public static final String TITLE_FILE_SELECTED_DOES_NOT_EXIST = "title.ntak.pearl-zip.file-selected-does-not-exist";
    public static final String HEADER_FILE_SELECTED_DOES_NOT_EXIST = "header.ntak.pearl-zip.file-selected-does-not-exist";
    public static final String BODY_FILE_SELECTED_DOES_NOT_EXIST = "body.ntak.pearl-zip.file-selected-does-not-exist";

    // Cannot move same dir
    public static final String TITLE_CANNOT_DROP_SAME_DIR = "title.ntak.pearl-zip.cannot-drop-same-dir";

    // Cannot copy same dir
    public static final String TITLE_CANNOT_PASTE_SAME_DIR = "title.ntak.pearl-zip.cannot-paste-same-dir";

    // License details
    public static final String TITLE_LICENSE_DETAILS = "title.ntak.pearl-zip.license-details";

    // Copy and Move same dir issue common
    public static final String HEADER_ISSUE_SAME_DIR = "header.ntak.pearl-zip.issue-same-dir";
    public static final String BODY_ISSUE_SAME_DIR = "body.ntak.pearl-zip.issue-same-dir";

    // Open in new window
    public static final String TITLE_OPEN_NEW_WINDOW = "title.ntak.pearl-zip.open-new-window";
    public static final String HEADER_OPEN_NEW_WINDOW = "header.ntak.pearl-zip.open-new-window";
    public static final String BODY_OPEN_NEW_WINDOW = "body.ntak.pearl-zip.open-new-window";
    public static final String BTN_OPEN_NEW_WINDOW_YES = "btn.ntak.pearl-zip.open-new-window.yes";
    public static final String BTN_OPEN_NEW_WINDOW_NO = "btn.ntak.pearl-zip.open-new-window.no";

    // Not a unique file
    public static final String TITLE_FILE_NOT_UNIQUE = "title.ntak.pearl-zip.file-not-unique";
    public static final String HEADER_FILE_NOT_UNIQUE = "header.ntak.pearl-zip.file-not-unique";
    public static final String BODY_FILE_NOT_UNIQUE = "body.ntak.pearl-zip.file-not-unique";

    // Clear cache confirmation
    public static final String TITLE_CLEAR_CACHE = "title.ntak.pearl-zip.clear-cache";
    public static final String HEADER_CLEAR_CACHE = "header.ntak.pearl-zip.clear-cache";
    public static final String BODY_CLEAR_CACHE = "body.ntak.pearl-zip.clear-cache";

    public static final String TITLE_NO_COMPRESSOR_WRITE_SERVICES = "title.ntak.pearl-zip.no-compressor-write-services";
    public static final String BODY_NO_COMPRESSOR_WRITE_SERVICES = "body.ntak.pearl-zip.no-compressor-write-services";

    public static final String TITLE_CLEAR_CACHE_BLOCKED = "title.ntak.pearl-zip.clear-cache-blocked";
    public static final String BODY_CLEAR_CACHE_BLOCKED = "body.ntak.pearl-zip.clear-cache-blocked";

    public static final String LOG_INVALID_ARCHIVE_SETUP = "logging.ntak.pearl-zip.invalid-archive-setup";

    public static final String LOG_ISSUE_SAVE_ARCHIVE = "logging.ntak.pearl-zip.issue-save-archive";

    public static final String TITLE_PATTERN = "title.ntak.pearl-zip.title-pattern";
    public static final String TITLE_FILE_PATTERN = "title.ntak.pearl-zip.title-file-pattern";
    public static final String TITLE_NEW_ARCHIVE_PATTERN = "title.ntak.pearl-zip.new-archive-pattern";
    public static final String TITLE_SELECT_FILE_TO_COMPRESS = "title.ntak.pearl-zip.select-file-to-compress";
    public static final String TITLE_SAVE_ARCHIVE_PATTERN = "title.ntak.pearl-zip.save-archive-pattern";
    public static final String TITLE_ADD_TO_ARCHIVE_PATTERN = "title.ntak.pearl-zip.add-to-archive-pattern";
    public static final String TITLE_EXTRACT_ARCHIVE_PATTERN = "title.ntak.pearl-zip.extract-archive-pattern";
    public static final String TITLE_OPTIONS_PATTERN = "title.ntak.pearl-zip.options-pattern";
    public static final String TITLE_OPEN_ARCHIVE = "title.ntak.pearl-zip.open-archive";
    public static final String TITLE_TARGET_ARCHIVE_LOCATION = "title.ntak.pearl-zip.target-archive-location";
    public static final String TITLE_SOURCE_DIR_LOCATION = "title.ntak.pearl-zip.source-dir-location";
    public static final String TITLE_TARGET_DIR_LOCATION = "title.ntak.pearl-zip.target-dir-location";

    public static final String LBL_ARCHIVE_EXT_TYPE_PATTERN = "label.ntak.pearl-zip.archive-ext-type-pattern";
    public static final String LBL_BUTTON_MOVE = "label.ntak.pearl-zip.button.move";
    public static final String LBL_BUTTON_DROP = "label.ntak.pearl-zip.button.drop";
    public static final String LBL_BUTTON_COPY = "label.ntak.pearl-zip.button.copy";
    public static final String LBL_BUTTON_PASTE = "label.ntak.pearl-zip.button.paste";
    public static final String LBL_RETRIEVE_FILE_META = "label.ntak.pearl-zip.retrieve-file-meta";
    public static final String LBL_RETRIEVE_FOLDER_META = "label.ntak.pearl-zip.retrieve-folder-meta";

    public static final String LBL_CLEAR_UP_TEMP_STORAGE = "label.ntak.pearl-zip.clear-up-temp-storage";
    public static final String LBL_CLEAR_UP_OS_TEMP = "label.ntak.pearl-zip.clear-up-os-temp";
    public static final String LBL_SKIP_OS_TEMP_CLEAN = "label.ntak.pearl-zip.skip-os-temp-clean";
    public static final String LBL_CLEAR_UP_RECENTS = "label.ntak.pearl-zip.clear-up-recents";
    public static final FileSystem JRT_FILE_SYSTEM = FileSystems.getFileSystem(URI.create("jrt:/"));

    public static MacZipLauncher APP;
    public static Path LOCAL_TEMP;
    public static Path STORE_ROOT;
    public static Path STORE_TEMP;
    public static Path RECENT_FILE;
    public static Path SETTINGS_FILE;
    public static ExecutorService PRIMARY_EXECUTOR_SERVICE;
    public static ThreadGroup THREAD_GROUP;
    public static ProgressMessageTraceLogger MESSAGE_TRACE_LOGGER;
    public static ErrorAlertConsumer ERROR_ALERT_CONSUMER;
    public static MenuToolkit MENU_TOOLKIT;
    public static Path RUNTIME_MODULE_PATH;

    public static final ReadWriteLock LCK_CLEAR_CACHE = new ReentrantReadWriteLock(true);
}
