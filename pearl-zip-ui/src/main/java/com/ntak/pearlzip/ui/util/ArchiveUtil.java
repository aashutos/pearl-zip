/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.util;

import com.ntak.pearlzip.archive.pub.ArchiveReadService;
import com.ntak.pearlzip.archive.pub.ArchiveWriteService;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.model.ZipState;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.*;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.JFXUtil.raiseAlert;

public class ArchiveUtil {
    private static final Logger LOGGER = LoggerContext.getContext().getLogger(ArchiveUtil.class);

    public static void extractToDirectory(long sessionId, FXArchiveInfo fxArchiveInfo, File dir) {
        ArchiveReadService archiveReadService = fxArchiveInfo.getReadService();

        if (Objects.nonNull(dir) && dir.exists()) {
            Map<Integer,List<FileInfo>> mapFiles =
                    fxArchiveInfo.getFiles().stream().collect(Collectors.groupingBy(FileInfo::getLevel));

            for (int level : mapFiles.keySet().stream().sorted().collect(Collectors.toList())) {
                List<FileInfo> files = mapFiles.getOrDefault(level, Collections.emptyList());
                files.stream().filter(FileInfo::isFolder).forEach(f-> {
                    try {
                        Files.createDirectory(Paths.get(dir.getAbsolutePath(), f.getFileName()));
                    } catch(IOException e) {
                    }
                });

                files.stream().filter(f -> !f.isFolder()).forEach(f -> archiveReadService.extractFile(sessionId,
                                                Paths.get(dir.getAbsolutePath(),
                                                         Paths.get(f.getFileName()).toString()),
                                                fxArchiveInfo.getArchiveInfo(),
                                                f)
                );
            }
        }
    }

    public static Path createBackupArchive(FXArchiveInfo fxArchiveInfo, Path tempDir) throws IOException {
        Path backupArchive =  Paths.get(tempDir.toString(),
                                     Paths.get(fxArchiveInfo.getArchivePath()).getFileName().toString());
        Files.copy(Path.of(fxArchiveInfo.getArchivePath()), backupArchive, StandardCopyOption.REPLACE_EXISTING);
        return backupArchive;
    }

    public static boolean restoreBackupArchive(Path backupArchive, Path targetLocation) {
        try {
            if (Objects.nonNull(backupArchive) && Objects.nonNull(targetLocation) && Files.exists(backupArchive)) {
                Files.copy(backupArchive, targetLocation, StandardCopyOption.REPLACE_EXISTING);
                if (!backupArchive.equals(targetLocation)) {
                    Files.deleteIfExists(backupArchive);
                }
                return true;
            }

            return false;
        } catch(Exception e) {
            return false;
        }
    }

    public static void removeBackupArchive(Path tempArchive) throws IOException {
        Files.deleteIfExists(tempArchive);
        if (tempArchive.getParent().getFileName().toString().matches(REGEX_TIMESTAMP_DIR) && Files.list(tempArchive.getParent())
                                                                                                  .findAny()
                                                                                                  .isEmpty()) {
            Files.deleteIfExists(tempArchive.getParent());
        }
    }

    public static void deleteDirectory(Path d, Predicate<Path> exclusionPattern) {
        try {
            // Delete all files in directory
            Files.walk(d)
                 .filter(p -> !Files.isDirectory(p) && !exclusionPattern.test(p))
                 .forEach(p -> {
                     try {
                         Files.deleteIfExists(p);
                     } catch(IOException ioException) {
                     }
                 });

            // Delete nested directories
            Files.walk(d)
                 .filter(Files::isDirectory)
                 .sorted(Comparator.comparingInt((Path f) -> f.toAbsolutePath()
                                                              .toString()
                                                              .length()).reversed())
                 .forEach(p -> {
                     try {
                         Files.deleteIfExists(p);
                     } catch(IOException ioException) {
                     }
                 });

            // Delete top-level directory itself
            Files.deleteIfExists(d);
        } catch(IOException ioe) {
        }
    }

    public static void addDirectory(long sessionId, FXArchiveInfo fxArchiveInfo, File dirToAdd) throws IOException {
        int depth = fxArchiveInfo.getDepth()
                                 .get();
        int index = fxArchiveInfo.getFiles()
                                 .size();
        String prefix = fxArchiveInfo.getPrefix();
        Path dirPath = dirToAdd.toPath();
        ArchiveWriteService archiveWriteService = fxArchiveInfo.getWriteService();

        Path tempDir = Files.createTempDirectory(TMP_DIR_PREFIX);
        Path tempArchive = createBackupArchive(fxArchiveInfo, tempDir);

        List<FileInfo> files = com.ntak.pearlzip.ui.util.internal.ArchiveUtil.handleDirectory(prefix, dirPath.getParent(), dirPath, depth + 1, index);
        files.add(new FileInfo((index + 1), depth,
                               depth > 0 ? String.format("%s/%s", prefix,
                                                         dirPath.getFileName()
                                                                .toString()) : dirPath.getFileName()
                                                                                      .toString(),
                               -1, 0,
                               0, null,
                               null, null,
                               "", "", 0, "",
                               true, false,
                               Collections.singletonMap(KEY_FILE_PATH, dirPath.toString())));

        if (files.removeIf(f -> f.getAdditionalInfoMap()
                                 .getOrDefault(KEY_FILE_PATH, "")
                                 .equals(fxArchiveInfo.getArchivePath()))) {
            // LOG: Skipping the addition of this archive within itself...
            LOGGER.warn(resolveTextKey(LOG_SKIP_ADD_SELF));
        }

        boolean success = archiveWriteService.addFile(sessionId, fxArchiveInfo.getArchiveInfo(),
                                                      files.toArray(new FileInfo[0]));
        if (!success) {
            restoreBackupArchive(tempArchive,
                                 Paths.get(fxArchiveInfo.getArchivePath()));
            JFXUtil.runLater(fxArchiveInfo::refresh);

            // LOG: Issue adding directory %s
            // TITLE: ERROR: Failed to add directory to archive
            // HEADER: Directory %s could not be added to archive %s
            // BODY: Archive has been reverted to the last stable state.
            LOGGER.error(resolveTextKey(LOG_ISSUE_ADDING_DIR, dirToAdd.getAbsolutePath()));
            raiseAlert(Alert.AlertType.ERROR,
                       resolveTextKey(TITLE_ISSUE_ADDING_DIR),
                       resolveTextKey(HEADER_ISSUE_ADDING_DIR),
                       resolveTextKey(BODY_ISSUE_ADDING_DIR),
                       null
            );
        }

        removeBackupArchive(tempArchive);
    }

    public static void addFile(long sessionId, FXArchiveInfo fxArchiveInfo, File rawFile, String fileName) throws IOException {
        int depth = fxArchiveInfo.getDepth().get();
        int index = fxArchiveInfo.getFiles().size();
        String prefix = fxArchiveInfo.getPrefix();

        Path tempDir = Files.createTempDirectory(TMP_DIR_PREFIX);
        Path tempArchive = createBackupArchive(fxArchiveInfo, tempDir);

        ArchiveWriteService service = ZipState.getWriteArchiveServiceForFile(
               fxArchiveInfo.getArchivePath()).get();
        boolean success;
        if (rawFile.isFile()) {
            FileInfo fileToAdd = new FileInfo(fxArchiveInfo.getFiles()
                                                           .size(),
                                              fxArchiveInfo.getDepth()
                                                           .get(),
                                              fileName,
                                              -1,
                                              0,
                                              rawFile.getTotalSpace(),
                                              LocalDateTime.ofInstant(Instant.ofEpochMilli(
                                                                              rawFile.lastModified()),
                                                                      ZoneId.systemDefault()),
                                              null,
                                              null,
                                              null,
                                              null,
                                              0,
                                              "",
                                              !rawFile.isFile(),
                                              false,
                                              Collections.singletonMap(
                                                      KEY_FILE_PATH,
                                                      rawFile.getAbsoluteFile()
                                                             .getPath()));
            success = service.addFile(sessionId,
                                      fxArchiveInfo.getArchiveInfo(),
                                      fileToAdd);
        } else { // Mac App is a directory
            List<FileInfo> files = com.ntak.pearlzip.ui.util.internal.ArchiveUtil.handleDirectory(prefix,
                                                                                                  rawFile.toPath().getParent(), rawFile.toPath(), depth +1,
                                                                                                  index);
            success = service.addFile(sessionId,
                                      fxArchiveInfo.getArchiveInfo(),
                                      files.toArray(new FileInfo[0]));
        }

        if (!success) {
            restoreBackupArchive(tempArchive,
                                 Paths.get(fxArchiveInfo.getArchivePath()));
            JFXUtil.runLater(fxArchiveInfo::refresh);

            // LOG: Issue adding file %s
            // TITLE: ERROR: Failed to add file to archive
            // HEADER: File %s could not be added to archive %s
            // BODY: Archive has been reverted to the last stable state.
            LOGGER.error(resolveTextKey(LOG_ISSUE_ADDING_FILE,
                                                                                       rawFile.getAbsolutePath()));
            raiseAlert(Alert.AlertType.ERROR,
                       resolveTextKey(TITLE_ISSUE_ADDING_FILE),
                       resolveTextKey(HEADER_ISSUE_ADDING_FILE),
                       resolveTextKey(BODY_ISSUE_ADDING_FILE),
                       null
            );
        }
    }

    public static Stage launchMainStage(FXArchiveInfo fxArchiveInfo) {
        return com.ntak.pearlzip.ui.util.internal.ArchiveUtil.launchMainStage(new Stage(), fxArchiveInfo);
    }
}
