/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.archive.acc.pub;

import com.ntak.pearlzip.archive.constants.ConfigurationConstants;
import com.ntak.pearlzip.archive.constants.LoggingConstants;
import com.ntak.pearlzip.archive.pub.*;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.util.Strings;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.archive.acc.constants.CommonsCompressConstants.PROFILE;
import static com.ntak.pearlzip.archive.acc.util.CommonsCompressUtil.getArchiveFormat;
import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.CNS_NTAK_PEARL_ZIP_ICON_FOLDER;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.*;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;

/**
 *   Implementation of an Archive Read Service, which utilises the Apache Commons Compress library underneath for
 *   the tar format. PAX headers are supported intrinsically for long file names.
 *
 *   @author Aashutos Kakshepati
 */
public class CommonsCompressArchiveReadService implements ArchiveReadService {

    private static final Logger LOGGER = LoggerContext.getContext().getLogger(CommonsCompressArchiveReadService.class);

    @Override
    public List<FileInfo> listFiles(long sessionId, ArchiveInfo archiveInfo) {
        String archivePath = archiveInfo.getArchivePath();
        List<FileInfo> files = new LinkedList<>();
        final String extension = getArchiveFormat(archivePath);
        try(final InputStream iStream = Files.newInputStream(Path.of(archivePath));
            final ArchiveInputStream aiStream =
                    ArchiveStreamFactory.findAvailableArchiveInputStreamProviders()
                                        .get(extension)
                                        .createArchiveInputStream(extension, iStream, null)
        ) {
            if (aiStream instanceof TarArchiveInputStream tais) {
                TarArchiveEntry entry;
                int index = 0;
                while ((entry = tais.getNextTarEntry()) != null) {
                    String name = entry.getName();
                    if (entry.isDirectory()) {
                        name = name.substring(0,name.length()-1);
                    }
                    FileInfo fileInfo = new FileInfo(index++,
                                                     (int)name.chars().filter(c -> c == '/').count(),
                                                     name,
                                                     0,
                                                     entry.getSize(),
                                                     entry.getRealSize(),
                                                     entry.getLastModifiedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                                                     entry.getLastModifiedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), null, entry.getUserName(),
                                                     entry.getGroupName(), 0, "", entry.isDirectory(), false,
                                                     Collections.emptyMap());
                    files.add(fileInfo);
                }

                // Handle file path only archives
                List<String> rootFileNames =
                        files.stream()
                             .filter(f -> Paths.get(f.getFileName()).getParent() != null)
                             .map(f-> Paths.get(f.getFileName()).getParent().toString())
                             .filter(r-> !Strings.isEmpty(r) && files.stream().map(FileInfo::getFileName).noneMatch(f->f.equals(r)))
                             .distinct()
                             .collect(Collectors.toList());

                if (rootFileNames.size() > 0) {
                    for (String filename : rootFileNames) {
                        files.add(new FileInfo(files.size()+1,
                                               filename.length() - filename.replace(File.separator, "").length()
                                , filename, 0,
                                               0,0
                                ,null,null,
                                               null,null,null,0,
                                               null,
                                               true,false,
                                               Collections.singletonMap(
                                                       ConfigurationConstants.KEY_ICON_REF, System.getProperty(
                                                               CNS_NTAK_PEARL_ZIP_ICON_FOLDER, ""))));
                    }
                }
            }
        } catch(IOException | ArchiveException e) {
            // LOG: %s on listing contents. Message: %s
            // TITLE: Issue listing entries from archive
            // HEADER: The archive %s could not be interrogated for contents
            // BODY: Exception %s was thrown on the attempt to list contents of the archive. Further details can be
            // found below.
            LOGGER.error(resolveTextKey(LOG_ARCHIVE_SERVICE_LISTING_EXCEPTION, e.getClass().getCanonicalName(), e.getMessage()));
            DEFAULT_BUS.post(new ErrorMessage(sessionId,
                                              resolveTextKey(TITLE_ARCHIVE_SERVICE_LISTING_EXCEPTION),
                                              resolveTextKey(HEADER_ARCHIVE_SERVICE_LISTING_EXCEPTION, archiveInfo.getArchivePath()),
                                              resolveTextKey(BODY_ARCHIVE_SERVICE_LISTING_EXCEPTION, e.getClass().getCanonicalName()),
                                              e,
                                              archiveInfo));
        }

        return files;
    }

    @Override
    public List<FileInfo> listFiles(long sessionId, String archivePath) {
        ArchiveInfo archiveInfo = ArchiveService.generateDefaultArchiveInfo(archivePath);
        return listFiles(sessionId, archiveInfo);
    }

    @Override
    public boolean extractFile(long sessionId, Path targetLocation, ArchiveInfo archiveInfo, FileInfo file) {
        final String archivePath = archiveInfo.getArchivePath();
        final String extension = getArchiveFormat(archivePath);
        try(final InputStream iStream = Files.newInputStream(Path.of(archivePath));
            final ArchiveInputStream aiStream =
                    ArchiveStreamFactory.findAvailableArchiveInputStreamProviders()
                                        .get(extension)
                                        .createArchiveInputStream(extension, iStream, null)
        ) {
            if (aiStream instanceof TarArchiveInputStream tais) {
                TarArchiveEntry entry;
                while ((entry = tais.getNextTarEntry()) != null) {
                    if (entry.getName().equals(file.getFileName())) {
                        IOUtils.copy(aiStream, Files.newOutputStream(targetLocation));
                        return true;
                    }
                }
            }
        } catch(IOException | ArchiveException e) {
            // LOG: %s on extracting file(s). Message: %s
            // TITLE: Issue extracting archive
            // HEADER: The archive %s could not be extracted
            // BODY: Exception %s was thrown on the attempt to extract from the archive. Further details can be found
            // below.
            LOGGER.error(resolveTextKey(LoggingConstants.LOG_ARCHIVE_SERVICE_EXTRACT_EXCEPTION,
                                        e.getClass().getCanonicalName(), e.getMessage()));
            DEFAULT_BUS.post(new ErrorMessage(sessionId,
                                              resolveTextKey(TITLE_ARCHIVE_SERVICE_EXTRACT_EXCEPTION),
                                              resolveTextKey(HEADER_ARCHIVE_SERVICE_EXTRACT_EXCEPTION, archiveInfo.getArchivePath()),
                                              resolveTextKey(BODY_ARCHIVE_SERVICE_EXTRACT_EXCEPTION, e.getClass().getCanonicalName()),
                                              e,
                                              archiveInfo));
        }
        return false;
    }

    @Override
    public boolean extractFile(long sessionId, Path targetLocation, String archivePath, FileInfo file) {
        ArchiveInfo archiveInfo = ArchiveService.generateDefaultArchiveInfo(archivePath);
        return extractFile(sessionId, targetLocation, archiveInfo, file);
    }

    @Override
    public boolean testArchive(long sessionId, String archivePath) {
        final String extension = getArchiveFormat(archivePath);
        try(final InputStream iStream = Files.newInputStream(Path.of(archivePath));
            final ArchiveInputStream aiStream =
                    ArchiveStreamFactory.findAvailableArchiveInputStreamProviders()
                                        .get(extension)
                                        .createArchiveInputStream(extension, iStream, null)
        ) {
            if (aiStream instanceof TarArchiveInputStream tais) {
                TarArchiveEntry entry;
                while ((entry = tais.getNextTarEntry()) != null) {
                    if (!entry.isCheckSumOK()) {
                        return false;
                    }
                }
            }
        } catch(IOException | ArchiveException e) {
            return false;
        } finally {
            ArchiveService.DEFAULT_BUS.post(new ProgressMessage(sessionId, COMPLETED, COMPLETED, 1, 1));
        }

        return true;
    }

    @Override
    public ArchiveServiceProfile getArchiveServiceProfile() {
        return PROFILE;
    }
}
