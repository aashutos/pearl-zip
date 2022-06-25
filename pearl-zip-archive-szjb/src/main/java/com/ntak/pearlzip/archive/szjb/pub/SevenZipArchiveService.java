/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.archive.szjb.pub;

import com.ntak.pearlzip.archive.constants.ConfigurationConstants;
import com.ntak.pearlzip.archive.constants.LoggingConstants;
import com.ntak.pearlzip.archive.pub.*;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.PropertyInfo;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.util.Strings;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.CNS_NTAK_PEARL_ZIP_ICON_FOLDER;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.*;
import static com.ntak.pearlzip.archive.szjb.constants.SevenZipConstants.PROFILE;
import static com.ntak.pearlzip.archive.szjb.constants.SevenZipLoggingConstants.LOG_EXCEPTION_ON_EXTRACTION;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;

/**
 *  Implementation of the Archive Service for reading archives using the 7-Zip Java Binding library underneath.
 *  @author Aashutos Kakshepati
 */
public class SevenZipArchiveService implements ArchiveReadService {

    private static final Logger LOGGER = LoggerContext.getContext()
                                                      .getLogger(SevenZipArchiveService.class);
    private static final TransformEntry<ISimpleInArchiveItem> transformer = new SimpleSevenZipEntryTransform();

    @Override
    public List<FileInfo> listFiles(long sessionId, String archivePath) {
        ArchiveInfo archiveInfo = ArchiveService.generateDefaultArchiveInfo(archivePath);
        return listFiles(sessionId, archiveInfo);
    }

    @Override
    public List<FileInfo> listFiles(long sessionId, ArchiveInfo archiveInfo) {
        String archivePath = archiveInfo.getArchivePath();

        if (archivePath.matches(".*(.tgz)$")) {
            return List.of(new FileInfo(0, 0,
                                        Paths.get(String.format("%s.tar",
                                                  archivePath.substring(0, archivePath.lastIndexOf("."))))
                                                             .getFileName()
                                                             .toString(),
                                        -1,
                                        0, 0,
                                        null, null,
                                        null, null, null, 0,
                                        null, false, false,
                                        Collections.singletonMap("nested-archive","true")));
        }

        if (archivePath.matches(".*(.gz|.xz|.bz2)$")) {
            return List.of(new FileInfo(0, 0,
                                        Paths.get(archivePath.substring(0, archivePath.lastIndexOf("."))).getFileName().toString(), -1,
                                        0, 0,
                                        null, null,
                                        null, null, null, 0,
                                        null, false, false,
                                        Collections.singletonMap("nested-archive","true")));
        }

        try (final RandomAccessFile randomAccessFile = new RandomAccessFile(archivePath, "r");
             final IInArchive archive = SevenZip.openInArchive(null,
                                                               new RandomAccessFileInStream(randomAccessFile))) {
            // LOG: Archive format: %s
            LOGGER.info(resolveTextKey(LOG_ARCHIVE_SERVICE_FORMAT, archive.getArchiveFormat()));
            // LOG: No. of items: %s
            LOGGER.info(resolveTextKey(LOG_ARCHIVE_SERVICE_NUMBER_ITEMS, archive.getNumberOfItems()));

            List<FileInfo> files =
                    Stream.of(archive.getSimpleInterface()
                                     .getArchiveItems())
                          .map(transformer::transform)
                          .filter(Optional::isPresent)
                          .map(Optional::get)
                          .collect(Collectors.toList());

            // Handle file path only archives
            if (files.stream().noneMatch(FileInfo::isFolder)) {
                List<String> rootFileNames =
                files.stream()
                     .filter(f -> Paths.get(f.getFileName()).getParent() != null)
                     .map(f-> Paths.get(f.getFileName()).getParent().toString())
                     .filter(r-> !Strings.isEmpty(r))
                     .distinct()
                     .collect(Collectors.toList());

                for (String filename : rootFileNames) {
                    files.add(new FileInfo(files.size()+1,
                                           filename.length() - filename.replace(File.separator,"").length()
                            , filename, -1,
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

            // Handle directory creation
            HashSet<FileInfo> setFiles = new HashSet<>(files);
            for (FileInfo file : files) {
                final int level = file.getLevel();
                Path parent = Paths.get(file.getFileName());
                for (int j = 1; j <= level; j++) {
                    parent = parent.getParent();
                    final FileInfo fileInfo = new FileInfo(setFiles.size(),
                                                           level - j,
                                                           parent.toString(),
                                                           -1,
                                                           0,
                                                           0,
                                                           null,
                                                           null,
                                                           null,
                                                           null,
                                                           null,
                                                           0,
                                                           null,
                                                           true,
                                                           false,
                                                           Collections.singletonMap(
                                                                   ConfigurationConstants.KEY_ICON_REF,
                                                                   System.getProperty(
                                                                           CNS_NTAK_PEARL_ZIP_ICON_FOLDER, "")));
                    setFiles.add(fileInfo);
                }
            }

            return new ArrayList<>(setFiles);
        } catch(IOException e) {
            // LOG: %s on listing contents. Message: %s
            // TITLE: Issue listing entries from archive
            // HEADER: The archive %s could not be interrogated for contents
            // BODY: Exception %s was thrown on the attempt to list contents of the archive. Further details can be
            // found below.
            LOGGER.error(resolveTextKey(LoggingConstants.LOG_ARCHIVE_SERVICE_LISTING_EXCEPTION, e.getClass().getCanonicalName(),
                                        e.getMessage()));
            DEFAULT_BUS.post(new ErrorMessage(sessionId,
                                              resolveTextKey(TITLE_ARCHIVE_SERVICE_LISTING_EXCEPTION),
                                              resolveTextKey(HEADER_ARCHIVE_SERVICE_LISTING_EXCEPTION, archiveInfo.getArchivePath()),
                                              resolveTextKey(BODY_ARCHIVE_SERVICE_LISTING_EXCEPTION, e.getClass().getCanonicalName()),
                                              e,
                                              archiveInfo));
        } finally {
            ArchiveService.DEFAULT_BUS.post(new ProgressMessage(sessionId, COMPLETED, COMPLETED, 1, 1));
        }

        return Collections.emptyList();
    }

    @Override
    public boolean extractFile(long sessionId, Path targetLocation, String archivePath, FileInfo file) {
        ArchiveInfo archiveInfo = ArchiveService.generateDefaultArchiveInfo(archivePath);
        return extractFile(sessionId, targetLocation, archiveInfo, file);
    }

        @Override
    public boolean extractFile(long sessionId, Path targetLocation, ArchiveInfo archiveInfo, FileInfo file) {
        String archivePath = archiveInfo.getArchivePath();
        try (final RandomAccessFile randomAccessFile = new RandomAccessFile(archivePath, "r");
             final IInArchive archive = SevenZip.openInArchive(null,
                                                               new RandomAccessFileInStream(randomAccessFile))) {
            Optional<ISimpleInArchiveItem> optItem;
            List<ISimpleInArchiveItem> rawArchiveItems = List.of(archive.getSimpleInterface().getArchiveItems());

            // Nested archive file handling
            if (archivePath.matches(".*(.gz|.xz|.bz2|.tgz)$")) {
                optItem = Optional.of(rawArchiveItems.get(0));
            } else {
                optItem = rawArchiveItems.stream()
                                         .filter(f -> {
                                            try {
                                                return f.getPath()
                                                        .equals(file.getFileName());
                                            } catch(Exception e) {
                                                return false;
                                            }
                                         })
                                         .findFirst();
            }

            if (optItem.isPresent()) {
                try {
                    // LOG: Extracting zip entry %s...
                    DEFAULT_BUS.post(new ProgressMessage(sessionId, PROGRESS,
                                                         resolveTextKey(LBL_PROGRESS_LOADED_ENTRY,
                                                                        optItem.get().getPath()),
                                                         -1,
                                                         1)
                    );

                    if (!Files.exists(targetLocation)) {
                        Files.createFile(targetLocation);
                    }

                    ExtractOperationResult result = optItem.get()
                                                           .extractSlow(
                                                                   new RandomAccessFileOutStream(
                                                                           new RandomAccessFile(targetLocation.toAbsolutePath()
                                                                                                              .toString(), "rw")
                                                                   )
                                                           );
                    // Extraction result for file %s was %s
                    return result == ExtractOperationResult.OK;
                } catch (IOException e) {
                    // %s on extraction of file %s. Message: %s
                    LOGGER.error(resolveTextKey(LOG_EXCEPTION_ON_EXTRACTION, e.getClass().getCanonicalName(),
                                                file.getFileName(), e.getMessage()));
                }
            }
        } catch(IOException e) {
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
    public boolean testArchive(long sessionId, String archivePath) {
        try (final RandomAccessFile randomAccessFile = new RandomAccessFile(archivePath, "r");
             final IInArchive archive = SevenZip.openInArchive(null,
                                                               new RandomAccessFileInStream(randomAccessFile))) {

            LOGGER.info(resolveTextKey(LOG_ARCHIVE_SERVICE_FORMAT, archive.getArchiveFormat()));
            int properties = archive.getNumberOfProperties();
            for (int i = 0; i < properties; i++) {
                PropertyInfo pInfo = archive.getPropertyInfo(i);
                String value = archive.getStringArchiveProperty(pInfo.propID);
                // Read zip property (%s,%s)
                resolveTextKey(LOG_ARCHIVE_READ_ZIP_PROPERTY, pInfo.name, value);
                ArchiveService.DEFAULT_BUS.post(new ProgressMessage(sessionId, PROGRESS,
                                                                    resolveTextKey(LOG_ARCHIVE_READ_ZIP_PROPERTY, pInfo.name, value), 1,
                                                                    properties));
            }

            final ISimpleInArchive archiveSIf = archive.getSimpleInterface();
            int items = archiveSIf.getNumberOfItems();
            LOGGER.info(resolveTextKey(LOG_ARCHIVE_SERVICE_NUMBER_ITEMS, archiveSIf
                                                                                .getNumberOfItems()));
            for (int i = 0; i < items; i++) {
                ISimpleInArchiveItem item = archiveSIf
                                                   .getArchiveItem(i);
                // Zip entry read: (path=%s, raw size=%s, packed size=%s, last modified=%s, CRC32 hash=%s, is Folder=%s)
                LOGGER.info(
                    resolveTextKey(
                            LOG_ARCHIVE_SERVICE_ZIP_ENTRY,
                            item.getPath(),
                            item.getSize(),
                            item.getPackedSize(),
                            item.getLastWriteTime(),
                            item.getCRC(),
                            item.isFolder()
                    )
                );
                ArchiveService.DEFAULT_BUS.post(new ProgressMessage(sessionId, PROGRESS, resolveTextKey(
                        LOG_ARCHIVE_SERVICE_ZIP_ENTRY,
                        item.getPath(),
                        item.getSize(),
                        item.getPackedSize(),
                        item.getLastWriteTime(),
                        item.getCRC(),
                        item.isFolder()
                ), 1, items));
            }

            return true;
        } catch (Exception e) {

        } finally {
            ArchiveService.DEFAULT_BUS.post(new ProgressMessage(sessionId, COMPLETED,COMPLETED,1,1));
        }

        // Handle empty tar balls
        if (archivePath.endsWith(".tar")) {
            outer:
            try(InputStream is = Files.newInputStream(Paths.get(archivePath))) {
                for (byte b : is.readAllBytes()) {
                    if (b != 0) {
                        // fail test
                        break outer;
                    }
                }
                // Empty tar is in expected state
                return true;
            } catch(IOException e) {
            }
        }

        return false;
    }

    @Override
    public Set<String> getCompressorArchives() {
        return Set.of("gz", "xz", "bz2", "tgz");
    }

    @Override
    public ArchiveServiceProfile getArchiveServiceProfile() {
        return PROFILE;
    }
}
