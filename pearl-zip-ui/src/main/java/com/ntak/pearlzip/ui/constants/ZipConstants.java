/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.constants;

import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.stages.seed.LogSeedStartupStage;
import com.ntak.pearlzip.ui.stages.seed.PrimarySeedStartupStage;
import com.ntak.pearlzip.ui.stages.startup.*;
import com.ntak.pearlzip.ui.util.AbstractSeedStartupStage;
import com.ntak.pearlzip.ui.util.AbstractStartupStage;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 *  General logging, configuration keys and other shared resources for the JavaFX UI.
 *  @author Aashutos Kakshepati
*/
public class ZipConstants {

    ///// PEARLZIP CONFIGURATION KEYS /////
    public static final String CNS_NTAK_PEARL_ZIP_NO_FILES_HISTORY = "configuration.ntak.pearl-zip.no-files-history";
    public static final String CNS_NTAK_PEARL_ZIP_RESIZEABLE = "configuration.ntak.pearl-zip.resizeable";
    public static final String CNS_NTAK_PEARL_ZIP_VERSION = "configuration.ntak.pearl-zip.version";
    public static final String CNS_NTAK_PEARL_ZIP_APP_NAME = "configuration.ntak.pearl-zip.app-name";
    public static final String CNS_NTAK_PEARL_ZIP_COPYRIGHT = "configuration.ntak.pearl-zip.copyright";
    public static final String CNS_NTAK_PEARL_ZIP_WEBLINK = "configuration.ntak.pearl-zip.weblink";
    public static final String CNS_NTAK_PEARL_ZIP_COMMIT_HASH = "configuration.ntak.pearl-zip.commit-hash";
    public static final String CNS_NTAK_PEARL_ZIP_RAW_VERSION = "configuration.ntak.pearl-zip.raw-version";
    public static final String CNS_NTAK_PEARL_ZIP_LICENSE_LOCATION = "configuration.ntak.pearl-zip.license-location";
    public static final String CNS_NTAK_PEARL_ZIP_LICENSE_OVERRIDE_LOCATION = "configuration.ntak.pearl-zip.license-override-location";
    public static final String CNS_NTAK_PEARL_ZIP_MODULE_PATH = "configuration.ntak.pearl-zip.module-path";
    public static final String CNS_NTAK_PEARL_ZIP_DEFAULT_MIN_WIDTH = "configuration.ntak.pearl-zip.default-min-width";
    public static final String CNS_NTAK_PEARL_ZIP_DEFAULT_MIN_HEIGHT = "configuration.ntak.pearl-zip.default-min-height";
    public static final String CNS_NTAK_PEARL_ZIP_DEFAULT_MAX_FILES_DRAG_OUT = "configuration.ntak.pearl-zip.default-max-files-drag-out";
    public static final String CNS_NTAK_PEARL_ZIP_DEFAULT_MAX_SIZE_DRAG_OUT = "configuration.ntak.pearl-zip.default-max-size-drag-out";
    public static final String CNS_NTAK_PEARL_ZIP_TOAST_DURATION = "configuration.ntak.pearl-zip.toast-duration";
    public static final String CNS_NTAK_PEARL_ZIP_SAFE_MODE = "configuration.ntak.pearl-zip.safe-mode";
    public static final String CNS_PROVIDER_PRIORITY_ROOT_KEY = "configuration.ntak.pearl-zip.provider.priority.%s";
    public static final String CNS_STORE_ROOT = "configuration.ntak.pearl-zip.store.temp";
    public static final String CNS_SETTINGS_FILE = "configuration.ntak.pearl-zip.settings-file";
    public static final String CNS_WINDOW_HEIGHT = "configuration.ntak.pearl-zip.window-height";
    public static final String CNS_WINDOW_WIDTH = "configuration.ntak.pearl-zip.window-width";
    public static final String CNS_THREAD_POOL_SIZE = "configuration.ntak.pearl-zip.thread-pool-size";
    public static final String CNS_METRIC_FACTORY = "configuration.ntak.pearl-zip.metric-factory";
    public static final String CNS_CONCURRENCY_LOCK_POLL_TIMEOUT = "configuration.ntak.pearl-zip.concurrency.lock-poll-timeout";
    public static final String CNS_SYSMENU_WINDOW_TEXT = "sysmenu.window.text";
    public static final String CNS_DEFAULT_FORMAT = "configuration.ntak.pearl-zip.default-format";
    public static final String CNS_LAUNCHER_CANONICAL_NAME = "configuration.ntak.pearl-zip.launcher-canonical-name";
    public static final String CNS_SHOW_NOTIFICATION = "configuration.ntak.pearl-zip.show-notification";
    public static final String CNS_SHOW_TARGET_FOLDER_EXTRACT_SELECTED = "configuration.ntak.pearl-zip.show-target-folder-extract-selected";
    public static final String CNS_SHOW_TARGET_FOLDER_EXTRACT_ALL = "configuration.ntak.pearl-zip.show-target-folder-extract-all";
    public static final String CNS_THEME_NAME = "configuration.ntak.pearl-zip.theme-name";
    public static final String CNS_NTAK_PEARL_ZIP_DB_CACHE_THRESHOLD_HOURS = "configuration.ntak.pearl-zip.db-cache.threshold.hours";
    public static final String CNS_DB_CONNECTION_TIMEOUT = "configuration.ntak.pearl-zip.db.connection.timeout";

    ///// PEARLZIP SRTING PROPERTY KEYS /////
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

    public static final String LOG_TOAST_CURRENT_DIRECTORY = "logging.ntak.pearl-zip.toast.current-directory";

    public static final String TITLE_ISSUE_INTEGRATING_CHANGES = "title.ntak.pearl-zip.issue-integrating-changes";
    public static final String HEADER_ISSUE_INTEGRATING_CHANGES = "header.ntak.pearl-zip.issue-integrating-changes";
    public static final String BODY_ISSUE_INTEGRATING_CHANGES = "body.ntak.pearl-zip.issue-integrating-changes";
    public static final String LOG_ISSUE_INTEGRATING_CHANGES = "logging.ntak.pearl-zip.issue-integrating-changes";

    public static final String LOG_ISSUE_ADDING_FILE = "logging.ntak.pearl-zip.issue-adding-file";
    public static final String TITLE_ISSUE_ADDING_FILE = "title.ntak.pearl-zip.issue-adding-file";
    public static final String HEADER_ISSUE_ADDING_FILE = "header.ntak.pearl-zip.issue-adding-file";
    public static final String BODY_ISSUE_ADDING_FILE = "body.ntak.pearl-zip.issue-adding-file";

    public static final String LOG_ISSUE_ADDING_DIR = "logging.ntak.pearl-zip.issue-adding-dir";
    public static final String TITLE_ISSUE_ADDING_DIR = "title.ntak.pearl-zip.issue-adding-dir";
    public static final String HEADER_ISSUE_ADDING_DIR = "header.ntak.pearl-zip.issue-adding-dir";
    public static final String BODY_ISSUE_ADDING_DIR = "body.ntak.pearl-zip.issue-adding-dir";

    public static final String LOG_CREATE_ARCHIVE = "logging.ntak.pearl-zip.log-create-archive";

    public static final String LOG_LOCKING_IN_PROPERTY = "logging.ntak.pearl-zip.locking-in-property";

    public static final String LOG_SKIP_ADD_SELF = "logging.ntak.pearl-zip.skip-add-self";
    public static final String TITLE_SKIP_ADD_SELF = "title.ntak.pearl-zip.skip-add-self";
    public static final String HEADER_SKIP_ADD_SELF = "header.ntak.pearl-zip.skip-add-self";
    public static final String BODY_SKIP_ADD_SELF = "body.ntak.pearl-zip.skip-add-self";

    public static final String LOG_LOADING_MODULE = "logging.ntak.pearl-zip.loading-module";
    public static final String LOG_READ_SERVICES_IDENTIFIED = "logging.ntak.pearl-zip.read-services-identified";
    public static final String LOG_WRITE_SERVICES_IDENTIFIED = "logging.ntak.pearl-zip.write-services-identified";
    public static final String LOG_MISSING_KEYS_LANG_PACK = "logging.ntak.pearl-zip.missing-keys-lang-pack";

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

    public static final String LOG_ISSUE_SETTING_UP_KEYSTORE = "logging.ntak.pearl-zip.issue-setting-up-keystore";
    public static final String LOG_NOTIFICATIONS_SQL_ISSUE = "logging.ntak.pearl-zip.notifications-sql-issue";
    public static final String LOG_NOTIFICATIONS_ISSUE = "logging.ntak.pearl-zip.notifications-issue";

    public static final String LOG_EXTENSION_STORE_COUNT_ISSUE = "logging.ntak.pearl-zip.extension-store-count-issue";
    public static final String LOG_EXTENSION_STORE_ISSUE = "logging.ntak.pearl-zip.extension-store-issue";

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

    // File selected does not exist (New single file compressor archive)
    public static final String LOG_ISSUE_CONNECTING_DB = "logging.ntak.pearl-zip.issue-connecting-db";
    public static final String TITLE_ISSUE_CONNECTING_DB = "title.ntak.pearl-zip.issue-connecting-db";
    public static final String BODY_ISSUE_CONNECTING_DB = "body.ntak.pearl-zip.issue-connecting-db";

    // Issue persisting Repository metadata
    public static final String LOG_ISSUE_PERSISTING_REPO = "logging.ntak.pearl-zip.issue-persisting-repo";

    // Issue parsing Repository metadata
    public static final String LOG_ISSUE_PARSING_REPO = "logging.ntak.pearl-zip.issue-parsing-repo";

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

    // License denied
    public static final String TITLE_LICENSE_DENIED = "title.ntak.pearl-zip.license-denied";
    public static final String BODY_LICENSE_DENIED = "body.ntak.pearl-zip.license-denied";

    // Installed library
    public static final String TITLE_LIB_INSTALLED = "title.ntak.pearl-zip.lib-installed";
    public static final String BODY_LIB_INSTALLED = "body.ntak.pearl-zip.lib-installed";

    // Clear cache confirmation
    public static final String TITLE_CLEAR_CACHE = "title.ntak.pearl-zip.clear-cache";
    public static final String HEADER_CLEAR_CACHE = "header.ntak.pearl-zip.clear-cache";
    public static final String BODY_CLEAR_CACHE = "body.ntak.pearl-zip.clear-cache";

    // Refresh keystore confirmation
    public static final String TITLE_REFRESH_KEYSTORE = "title.ntak.pearl-zip.refresh-keystore";
    public static final String HEADER_REFRESH_KEYSTORE = "header.ntak.pearl-zip.refresh-keystore";
    public static final String BODY_REFRESH_KEYSTORE = "body.ntak.pearl-zip.refresh-keystore";

    // Issue loading library
    public static final String LOG_ISSUE_LOAD_LIB = "logging.ntak.pearl-zip.issue-load-lib";
    public static final String TITLE_ISSUE_LOAD_LIB = "title.ntak.pearl-zip.issue-load-lib";
    public static final String HEADER_ISSUE_LOAD_LIB = "header.ntak.pearl-zip.issue-load-lib";
    public static final String BODY_ISSUE_LOAD_LIB = "body.ntak.pearl-zip.issue-load-lib";

    // Cannot drag out due to threshold breach
    public static final String TITLE_CANNOT_DRAG_OUT_FILE = "title.ntak.pearl-zip.cannot-drag-out-file";
    public static final String HEADER_CANNOT_DRAG_OUT_FILE = "header.ntak.pearl-zip.cannot-drag-out-file";
    public static final String BODY_CANNOT_DRAG_OUT_FILE = "body.ntak.pearl-zip.cannot-drag-out-file";

    // Cannot drag out folder
    public static final String TITLE_CANNOT_DRAG_OUT_FOLDER = "title.ntak.pearl-zip.cannot-drag-out-folder";
    public static final String HEADER_CANNOT_DRAG_OUT_FOLDER = "header.ntak.pearl-zip.cannot-drag-out-folder";
    public static final String BODY_CANNOT_DRAG_OUT_FOLDER = "body.ntak.pearl-zip.cannot-drag-out-folder";

    public static final String TITLE_NO_COMPRESSOR_WRITE_SERVICES = "title.ntak.pearl-zip.no-compressor-write-services";
    public static final String BODY_NO_COMPRESSOR_WRITE_SERVICES = "body.ntak.pearl-zip.no-compressor-write-services";

    public static final String TITLE_CLEAR_CACHE_BLOCKED = "title.ntak.pearl-zip.clear-cache-blocked";
    public static final String BODY_CLEAR_CACHE_BLOCKED = "body.ntak.pearl-zip.clear-cache-blocked";

    public static final String TITLE_CONFIRM_LOAD_PROVIDER_MODULE = "title.ntak.pearl-zip.confirm-load-provider-module";
    public static final String BODY_CONFIRM_LOAD_PROVIDER_MODULE = "body.ntak.pearl-zip.confirm-load-provider-module";

    public static final String TITLE_ADD_COMMENT_NOT_SUPPORTED = "title.ntak.pearl-zip.add-comments.not-supported";
    public static final String BODY_ADD_COMMENT_NOT_SUPPORTED = "body.ntak.pearl-zip.add-comments.not-supported";

    public static final String LOG_ISSUE_ADD_DRAG_DROP = "logging.ntak.pearl-zip.issue-add-drag-drop";
    public static final String TITLE_ISSUE_ADD_DRAG_DROP = "title.ntak.pearl-zip.issue-add-drag-drop";
    public static final String HEADER_ISSUE_ADD_DRAG_DROP = "header.ntak.pearl-zip.issue-add-drag-drop";
    public static final String BODY_ISSUE_ADD_DRAG_DROP = "body.ntak.pearl-zip.issue-add-drag-drop";

    public static final String LOGGING_ARCHIVE_CANNOT_CLOSE = "logging.ntak.pearl-zip.archive-cannot-close";
    public static final String TITLE_ARCHIVE_CANNOT_CLOSE = "title.ntak.pearl-zip.archive-cannot-close";
    public static final String BODY_ARCHIVE_CANNOT_CLOSE = "body.ntak.pearl-zip.archive-cannot-close";

    public static final String TITLE_SAFE_MODE_ENABLED = "title.ntak.pearl-zip.safe-mode-enabled";
    public static final String BODY_SAFE_MODE_ENABLED = "body.ntak.pearl-zip.safe-mode-enabled";

    // Confirm pzax extension installation
    public static final String TITLE_CONFIRM_INSTALL_EXTENSION = "title.ntak.pearl-zip.confirm-install-extension";
    public static final String HEADER_CONFIRM_INSTALL_EXTENSION = "header.ntak.pearl-zip.confirm-install-extension";
    public static final String BODY_CONFIRM_INSTALL_EXTENSION = "body.ntak.pearl-zip.confirm-install-extension";

    public static final String TITLE_CHANGE_LANG_PACK = "title.ntak.pearl-zip.change-lang-pack";
    public static final String HEADER_CHANGE_LANG_PACK = "header.ntak.pearl-zip.change-lang-pack";
    public static final String BODY_CHANGE_LANG_PACK = "body.ntak.pearl-zip.change-lang-pack";

    public static final String LOG_INVALID_ARCHIVE_SETUP = "logging.ntak.pearl-zip.invalid-archive-setup";

    public static final String LOG_ISSUE_SAVE_ARCHIVE = "logging.ntak.pearl-zip.issue-save-archive";

    public static final String LOG_REQUIRED_LICENSE_FILE_NOT_EXIST = "logging.ntak.pearl-zip.required-license-not-exist";
    public static final String LOG_HASH_INTEGRITY_FAILURE = "logging.ntak.pearl-zip.hash-integrity-failure";
    public static final String LOG_LIB_FILE_MANIFEST_ENTRY_CORRUPT = "logging.ntak.pearl-zip.lib-file-manifest-entry-corrupt";

    public static final String LOG_VERSION_MIN_VERSION_BREACH = "logging.ntak.pearl-zip.min-version-breach";
    public static final String LOG_VERSION_MAX_VERSION_BREACH = "logging.ntak.pearl-zip.max-version-breach";
    public static final String LOG_THEME_NOT_EXIST = "logging.ntak.pearl-zip.theme-not-exist";

    public static final String LOG_QUERY_RESULT_INVALID_EXTRACTION = "logging.ntak.pearl-zip.query-result-invalid-extraction";
    public static final String LOG_ISSUE_RETRIEVE_QUERY_DATA = "logging.ntak.pearl-zip.issue-retrieve-query-data";

    public static final String LOG_ISSUE_DOWNLOADING_PZAX_ARCHIVE = "logging.ntak.pearl-zip.issue-downloading-pzax-archive";

    public static final String TITLE_SAFE_MODE_PATTERN = "title.ntak.pearl-zip.safe-mode-pattern";
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
    public static final String TITLE_SELECT_INSTALL_EXTENSION = "title.ntak.pearl-zip.select-install-extension";
    public static final String TITLE_SELECT_PLUGINS_PURGE = "title.ntak.pearl-zip.select-plugins-purge";
    public static final String TITLE_ADD_STORE = "title.ntak.pearl-zip.add-store";
    public static final String TITLE_EXTENSION_STORE = "title.ntak.pearl-zip.extension-store";
    public static final String TITLE_EXTENSION_DETAILS = "title.ntak.pearl-zip.extension-details";

    public static final String TITLE_NEW_VERSION_AVAILABLE = "title.ntak.pearl-zip.new-version-available";
    public static final String BODY_NEW_VERSION_AVAILABLE = "body.ntak.pearl-zip.new-version-available";

    public static final String TITLE_LATEST_VERSION = "title.ntak.pearl-zip.latest-version";
    public static final String BODY_LATEST_VERSION = "body.ntak.pearl-zip.latest-version";

    public static final String TITLE_CONFIRM_PURGE_ALL = "title.ntak.pearl-zip.confirm-purge-all";
    public static final String BODY_CONFIRM_PURGE_ALL = "body.ntak.pearl-zip.confirm-purge-all";

    public static final String TITLE_PURGE_COMPLETE = "title.ntak.pearl-zip.purge-complete";
    public static final String BODY_PURGE_COMPLETE = "body.ntak.pearl-zip.purge-complete";

    public static final String TITLE_CONFIRM_PURGE_SELECTED = "title.ntak.pearl-zip.confirm-purge-selected";
    public static final String BODY_CONFIRM_PURGE_SELECTED = "body.ntak.pearl-zip.confirm-purge-selected";

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
    public static final String LBL_CLEAR_UP_DB_CACHE = "label.ntak.pearl-zip.clear-up-db-cache";

    public static final String LOG_CREATE_DIRECTORY = "logging.ntak.pearl-zip.create-directory";
    public static final String LOG_DIR_EXTRACT_COMPLETE = "logging.ntak.pearl-zip.dir-extract-complete";

    public static final String LOG_INVALID_QUERY_PARAMETER_SET = "logging.ntak.pearl-zip.invalid-query-parameter-set";

    public static final String LOG_USING_CACHE_FOR_QUERY = "logging.ntak.pearl-zip.using-cached-query";

    public static final String LOG_FAILED_EXECUTE_REMOVE_PATTERN = "logging.ntak.pearl-zip.failed-execute-remove-pattern";
    public static final String LOG_ISSUE_PARSING_MANIFEST = "logging.ntak.pearl-zip.issue-parsing-manifest";

    public static final String LOG_QUERY_EXECUTED = "logging.ntak.pearl-zip.log-query-executed";

    ///// PUBLIC CACHE /////
    public static final AdditionalContextReader GLOBAL_INTERNAL_CACHE = new AdditionalContextReader(InternalContextCache.GLOBAL_CONFIGURATION_CACHE);


    ///// CACHE KEYS /////
    public static final String CK_APP = "APP";
    public static final String CK_HOST_SERVICES = "HOST_SERVICES";
    public static final String CK_PARAMETERS = "PARAMETERS";
    public static final String CK_WINDOW_MENU = "WINDOW_MENU";
    public static final String CK_RECENT_FILES_MENU = "RECENT_FILES_MENU";
    public static final String CK_LOCAL_TEMP = "LOCAL_TEMP";
    public static final String CK_STORE_ROOT = "STORE_ROOT";
    public static final String CK_STORE_TEMP = "STORE_TEMP";
    public static final String CK_RECENT_FILE = "RECENT_FILE";
    public static final String CK_SETTINGS_FILE = "SETTINGS_FILE";
    public static final String CK_APPLICATION_SETTINGS_FILE = "APPLICATION_SETTINGS_FILE";
    public static final String CK_LOCAL_MANIFEST_DIR = "LOCAL_MANIFEST_DIR";
    public static final String CK_PRIMARY_EXECUTOR_SERVICE = "PRIMARY_EXECUTOR_SERVICE";
    public static final String CK_THREAD_GROUP = "THREAD_GROUP";
    public static final String CK_MESSAGE_TRACE_LOGGER = "MESSAGE_TRACE_LOGGER";
    public static final String CK_ERROR_ALERT_CONSUMER = "ERROR_ALERT_CONSUMER";
    public static final String CK_RUNTIME_MODULE_PATH = "RUNTIME_MODULE_PATH";
    public static final String CK_RK_KEYS = "RK_KEYS";
    public static final String CK_POST_PZAX_COMPLETION_CALLBACK = "POST_PZAX_COMPLETION_CALLBACK";
    public static final String CK_LANG_PACKS = "LANG_PACKS";
    public static final String CK_JRT_FILE_SYSTEM = "JRT_FILE_SYSTEM";
    public static final String CK_APP_LATCH = "APP_LATCH";
    public static final String CK_LCK_CLEAR_CACHE = "LCK_CLEAR_CACHE";
    public static final String CK_MANIFEST_RULES = "MANIFEST_RULES";
    public static final String CK_PLUGINS_METADATA = "PLUGINS_METADATA";
    public static final String CK_MAX_SIZE_DRAG_OUT = "MAX_SIZE_DRAG_OUT";
    public static final String CK_QUERY_RESULT_CACHE = "QUERY_CACHE";
    public static final String CK_QUERY_DEFINITION_CACHE = "QUERY_DEFINITION_CACHE";
    public static final String CK_MLC_CACHE = "MLC_CACHE";
    public static final String CK_REPO_ROOT = "REPO_ROOT";
    public static final String CK_STORE_REPO = "STORE_REPO";
    public static final String CK_SSL_CONTEXT = "SSL_CONTEXT";
    public static final String CK_STORE_PROVIDERS = "STORE_PROVIDERS";
    public static final String CK_STORE_THEMES = "STORE_THEMES";


    ///// GENERAL PEARLZIP CONSTANTS /////
    public static final String WINDOW_FOCUS_SYMBOL = " • ";
    public static final String CNS_PROP_HEADER = "PearlZip Application Settings File Generated @ %s";

    public static final String MANIFEST_FILE_NAME = "MF";
    public static final String KEY_MANIFEST_DELETED= "remove-pattern";
    public static final Set<String> CORE_THEMES = Set.of("modena", "modena-dark");

    public static final List<AbstractSeedStartupStage> SEED_STARTUP_STAGE = Arrays.asList(new PrimarySeedStartupStage(),
                                                                                          new LogSeedStartupStage());
    public static final List<AbstractStartupStage> STARTUP_STAGE = Arrays.asList(new WorkspaceStartupStage(),
                                                                                 new InMemoryCacheStartupStage(),
                                                                                 new ModuleLoadStartupStage(),
                                                                                 new KeystoreStartupStage(),
                                                                                 new QueryStartupStage(),
                                                                                 new StoreStartupStage(),
                                                                                 new EventBusStartupStage(),
                                                                                 new ThreadPoolStartupStage(),
                                                                                 new LicenseStartupStage());
}
