/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.util;

import com.ntak.pearlzip.archive.pub.ArchiveService;
import com.ntak.pearlzip.archive.pub.ProgressMessage;
import com.ntak.pearlzip.ui.constants.ZipConstants;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.model.FXMigrationInfo;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.REGEX_TIMESTAMP_DIR;
import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.TMP_DIR_PREFIX;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.PROGRESS;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.JFXUtil.getMainStageInstances;
import static javafx.scene.control.ProgressIndicator.INDETERMINATE_PROGRESS;

/**
 *  Implementation of CaughtRunnable used to provide Clear Cache functionality for PearlZip.
 *  @author Aashutos Kakshepati
 */
public class ClearCacheRunnable implements CaughtRunnable {

    private static final Logger LOGGER = LoggerContext.getContext()
                                                      .getLogger(ClearCacheRunnable.class);
    private final long sessionId;
    private final boolean isOnlyTempDirs;

    public ClearCacheRunnable(long sessionId, boolean isOnlyTempDirs) {
        this.sessionId = sessionId;
        this.isOnlyTempDirs = isOnlyTempDirs;
    }

    @Override
    public void execute() throws Exception {
        // Clearing up temporary storage location...
        ArchiveService.DEFAULT_BUS.post(new ProgressMessage(sessionId, PROGRESS,
                                                            resolveTextKey(LBL_CLEAR_UP_TEMP_STORAGE),
                                                            INDETERMINATE_PROGRESS, 1));
        List<String> openFiles =
                getMainStageInstances().stream()
                                       .map(s -> ((FXArchiveInfo) s.getUserData()).getArchivePath())
                                       .collect(
                                               Collectors.toList());
        Files.newDirectoryStream(ZipConstants.STORE_TEMP,
                                 (f) -> !openFiles.contains(f.toAbsolutePath()
                                                             .toString()))
             .forEach(f -> {
                 try {
                     Files.deleteIfExists(f);
                 } catch(IOException ioException) {
                 }
             });

        // Cleaning up OS temporary data..
        long activeMigrationsCount =
                getMainStageInstances().stream()
                                       .map(s -> ((FXArchiveInfo) s.getUserData()).getMigrationInfo()
                                                                                  .getType())
                                       .filter(t -> !t.equals(FXMigrationInfo.MigrationType.NONE))
                                       .count();
        if (activeMigrationsCount == 0) {
            ArchiveService.DEFAULT_BUS.post(new ProgressMessage(sessionId,
                                                                PROGRESS,
                                                                resolveTextKey(
                                                                        LBL_CLEAR_UP_OS_TEMP),
                                                                INDETERMINATE_PROGRESS,
                                                                1));
            LinkedList<Path> tempDirectories = new LinkedList<>();
            try(DirectoryStream<Path> dirs =
                        Files.newDirectoryStream(ZipConstants.LOCAL_TEMP,
                                                 (f) -> f.getFileName()
                                                         .toString()
                                                         .startsWith(TMP_DIR_PREFIX) || f.getFileName()
                                                                                         .toString()
                                                                                         .matches(
                                                                                                 REGEX_TIMESTAMP_DIR))) {
                dirs.forEach(tempDirectories::add);
            }

            // Remove nested pz directory in .pz/temp directory
            Files.newDirectoryStream(ZipConstants.STORE_TEMP,
                                     f -> f.getFileName()
                                           .toString()
                                           .startsWith(TMP_DIR_PREFIX) || f.getFileName()
                                                                           .toString()
                                                                           .matches(
                                                                                   REGEX_TIMESTAMP_DIR))
                 .forEach(tempDirectories::add);

            // LOG: Temporary directories to be deleted: %s
            LOGGER.debug(resolveTextKey(LOG_TEMP_DIRS_TO_DELETE,
                                        tempDirectories));
            tempDirectories.stream()
                           .forEach(p -> ArchiveUtil.deleteDirectory(p,
                                                                     (f)->openFiles.contains(f.toAbsolutePath().toString())));
        } else {
            ArchiveService.DEFAULT_BUS.post(new ProgressMessage(sessionId,
                                                                PROGRESS,
                                                                resolveTextKey(
                                                                        LBL_SKIP_OS_TEMP_CLEAN),
                                                                INDETERMINATE_PROGRESS,
                                                                1));
        }

        // Additional locations to clear up
        if (!isOnlyTempDirs) {
            // Clearing up recently open files...
            ArchiveService.DEFAULT_BUS.post(new ProgressMessage(sessionId, PROGRESS,
                                                                resolveTextKey(
                                                                        LBL_CLEAR_UP_RECENTS),
                                                                INDETERMINATE_PROGRESS, 1));
            Files.deleteIfExists(ZipConstants.RECENT_FILE);
        }
    }
}
