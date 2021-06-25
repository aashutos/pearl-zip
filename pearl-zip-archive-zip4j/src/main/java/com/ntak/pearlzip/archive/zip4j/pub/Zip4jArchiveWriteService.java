/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.archive.zip4j.pub;

import com.ntak.pearlzip.archive.pub.ArchiveInfo;
import com.ntak.pearlzip.archive.pub.ArchiveWriteService;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.archive.pub.ProgressMessage;
import com.ntak.pearlzip.archive.util.LoggingUtil;
import com.ntak.pearlzip.archive.zip4j.util.Zip4jUtil;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.KEY_FILE_PATH;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.*;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.archive.zip4j.constants.Zip4jConstants.*;

/**
 *  Implementation of the Archive Service for writing zip archives using the Zip4j library underneath.
 *  @author Aashutos Kakshepati
 */
public class Zip4jArchiveWriteService implements ArchiveWriteService {

    private static final Logger LOGGER = LoggerContext.getContext()
                                                      .getLogger(Zip4jArchiveWriteService.class);

    @Override
    public void createArchive(long sessionId, ArchiveInfo archiveInfo, FileInfo... files) {
        try {
            ZipParameters parameters = new ZipParameters();

            boolean isSplitArchiveRequest =
                    archiveInfo.<Boolean>getProperty(KEY_SPLIT_ARCHIVE_ENABLE).orElse(false);
            long splitSize = archiveInfo.<Long>getProperty(KEY_SPLIT_ARCHIVE_SIZE).orElse(MIN_SPLIT_ARCHIVE_SIZE);

            Zip4jUtil.initializeZipParameters(parameters, archiveInfo);
            ZipFile archive = new ZipFile(archiveInfo.getArchivePath(),
                                          archiveInfo.<char[]>getProperty(KEY_ENCRYPTION_PW).orElse(null));

            // Split archive only works with a non-empty archive.
            // TODO: Functionality to create a split archive and also to open and modify it via the use of the merge
            //  archive functionality (temporarily interim).
            //  As soon as ZipFile is interrogated the ZipModel is retrieved to get information about whether it is
            //  encrypted a split archive or a valid file etc. which will help with providing details about the archive
            if (isSplitArchiveRequest) {
                final Path tempFile = Files.createTempFile(".tmp", "");
                LocalDateTime creationTime = LocalDateTime.now();
                if (files.length == 0) {
                    FileInfo fileInfo = new FileInfo(0,0,".pz-archive",0L,0L,0L,
                                                     creationTime, creationTime, creationTime,
                                                     null, null, 0,
                                                     "", false, parameters.isEncryptFiles(),
                                                     Collections.singletonMap(KEY_FILE_PATH,
                                                                              tempFile.toAbsolutePath().toString()));
                    files = new FileInfo[]{fileInfo};
                }
                archive.createSplitZipFile(Arrays.stream(files)
                                                 .map(f -> (String)f.getAdditionalInfoMap().get(KEY_FILE_PATH))
                                                 .filter(Objects::nonNull)
                                                 .map(File::new)
                                                 .collect(Collectors.toList()),
                                           parameters,
                                           isSplitArchiveRequest,
                                           splitSize);
                Files.deleteIfExists(tempFile);
            } else {
                // Create stub file to ensure archive is created
                final Path tempFile = Files.createTempFile(".tmp", "");
                archive.addFile(tempFile.toFile());
                archive.removeFile(tempFile.toFile()
                                           .getName());
                Files.deleteIfExists(tempFile);

                // Adding subsequent archive files, if any...
                addFile(sessionId, archiveInfo, files);
            }
        } catch(IOException e) {
            // LOG: Issue creating zip archive.\nException thrown: %s\nException message: %s\nStack trace:\n%s
            LOGGER.error(resolveTextKey(LOG_ARCHIVE_Z4J_ISSUE_CREATING_ARCHIVE,
                                       e.getClass().getCanonicalName(),
                                       e.getMessage(),
                                       LoggingUtil.getStackTraceFromException(e)
            ));
            DEFAULT_BUS.post(new ProgressMessage(sessionId, ERROR, resolveTextKey(LOG_ARCHIVE_Z4J_ISSUE_CREATING_ARCHIVE,
                                                                                  e.getClass().getCanonicalName(),
                                                                                  e.getMessage(),
                                                                                  LoggingUtil.getStackTraceFromException(e)),0,1));
        } finally {
            DEFAULT_BUS.post(new ProgressMessage(sessionId, COMPLETED, COMPLETED,1,1));
        }
    }

    @Override
    public void createArchive(long sessionId, String archivePath, FileInfo... files) {
        ArchiveInfo archiveInfo = generateDefaultArchiveInfo(archivePath);

        createArchive(sessionId, archiveInfo, files);
    }

    @Override
    public boolean addFile(long sessionId, String archivePath, FileInfo... files) {
        ArchiveInfo archiveInfo = generateDefaultArchiveInfo(archivePath);
        return addFile(sessionId, archiveInfo, files);
    }

    @Override
    public boolean addFile(long sessionId, ArchiveInfo archiveInfo, FileInfo... files) {
        try {
            ZipFile archive = new ZipFile(archiveInfo.getArchivePath(),
                                          archiveInfo.<char[]>getProperty(KEY_ENCRYPTION_PW).orElse(null));

            // Add files...
            addFilesInPlace(sessionId, archive, archiveInfo,
                            Arrays.stream(files).filter(f->!f.isFolder()).collect(Collectors.toList()));

            return true;
        } catch(ZipException e) {
            // LOG: Issue adding to zip archive.\nException thrown: %s\nException message: %s\nStack trace:\n%s
            LOGGER.error(resolveTextKey(LOG_ARCHIVE_Z4J_ISSUE_ADDING_FILE, archiveInfo.getArchivePath(),
                                        e.getMessage()));
            DEFAULT_BUS.post(new ProgressMessage(sessionId, ERROR, resolveTextKey(LOG_ARCHIVE_Z4J_ISSUE_ADDING_FILE, archiveInfo.getArchivePath(),
                                                                                  e.getMessage()),0,1));
        } finally {
            DEFAULT_BUS.post(new ProgressMessage(sessionId, COMPLETED, COMPLETED,1,1));
        }
        return false;
    }

    private void addFilesInPlace(long sessionId, ZipFile archive, ArchiveInfo archiveInfo, List<FileInfo> files) throws ZipException {
        for (FileInfo file : files) {
            System.out.println(file.getAdditionalInfoMap());

            // LOG: Adding file %s...
            DEFAULT_BUS.post(new ProgressMessage(sessionId, PROGRESS,
                                                 resolveTextKey(LOG_ARCHIVE_Z4J_ADDING_FILE, file.getFileName())
                    , 1, files.size()));

            String fileName = file.getFileName();
            ZipParameters fileParam = new ZipParameters();
            Zip4jUtil.initializeZipParameters(fileParam, archiveInfo);

            if (file.isFolder()) {
                fileParam.setFileNameInZip(String.format(PATTERN_FOLDER, fileName));
                archive.addFolder(Paths.get(file.getAdditionalInfoMap().get(KEY_FILE_PATH).toString())
                                       .toAbsolutePath()
                                       .toFile(), fileParam);
            } else {
                fileParam.setFileNameInZip(fileName);
                archive.addFile(Paths.get(file.getAdditionalInfoMap().get(KEY_FILE_PATH).toString())
                                     .toAbsolutePath()
                                     .toFile(), fileParam);
            }
        }
    }

    @Override
    public boolean deleteFile(long sessionId, String archivePath, FileInfo file) {
        ArchiveInfo archiveInfo = generateDefaultArchiveInfo(archivePath);
        return deleteFile(sessionId, archiveInfo, file);
    }

    @Override
    public boolean deleteFile(long sessionId, ArchiveInfo archiveInfo, FileInfo file) {
        try {
            ZipFile archive = new ZipFile(archiveInfo.getArchivePath(),
                                          archiveInfo.<char[]>getProperty(KEY_ENCRYPTION_PW).orElse(null));

            // Overwrite files action is default
            // LOG: Deleting file %s...
            DEFAULT_BUS.post(new ProgressMessage(sessionId, PROGRESS,
                                                 resolveTextKey(LOG_ARCHIVE_Z4J_DELETING_FILE, file.getFileName()), 1, 1));

            if (file.isFolder()) {
                archive.removeFile(String.format(PATTERN_FOLDER, file.getFileName()));
            } else {
                archive.removeFile(file.getFileName());
            }

            return true;
        } catch(ZipException e) {
            // LOG: Issue deleting from zip archive.\nException thrown: %s\nException message: %s\nStack trace:\n%s
            LOGGER.error(resolveTextKey(LOG_ARCHIVE_Z4J_ISSUE_DELETING_FILE,
                                       e.getClass().getCanonicalName(),
                                       e.getMessage(),
                                       LoggingUtil.getStackTraceFromException(e)
            ));
            DEFAULT_BUS.post(new ProgressMessage(sessionId, ERROR, resolveTextKey(LOG_ARCHIVE_Z4J_ISSUE_DELETING_FILE,
                                                                                  e.getClass().getCanonicalName(),
                                                                                  e.getMessage(),
                                                                                  LoggingUtil.getStackTraceFromException(e)
            ),0,1));
        } finally {
            DEFAULT_BUS.post(new ProgressMessage(sessionId, COMPLETED, COMPLETED,1,1));
        }
        return false;
    }

    private static ArchiveInfo generateDefaultArchiveInfo(String archivePath) {
        ArchiveInfo archiveInfo = new ArchiveInfo();
        archiveInfo.setArchivePath(archivePath);
        archiveInfo.setArchiveFormat("zip");
        archiveInfo.setCompressionLevel(9);
        archiveInfo.addProperty(KEY_ENCRYPTION_ENABLE, false);
        archiveInfo.addProperty(KEY_COMPRESSION_METHOD, "DEFLATE");

        return archiveInfo;
    }

    @Override
    public Optional<ResourceBundle> getResourceBundle() {
        return Optional.of(RES_BUNDLE);
    }

    @Override
    public List<String> supportedWriteFormats() {
        return List.of("zip");
    }
}