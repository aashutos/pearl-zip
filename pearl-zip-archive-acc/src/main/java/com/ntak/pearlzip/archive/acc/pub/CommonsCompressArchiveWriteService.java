/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.archive.acc.pub;

import com.ntak.pearlzip.archive.pub.*;
import com.ntak.pearlzip.archive.util.LoggingUtil;
import org.apache.commons.compress.archivers.*;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.UnicodeCommentExtraField;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.changes.ChangeSet;
import org.apache.commons.compress.changes.ChangeSetPerformer;
import org.apache.commons.compress.changes.ChangeSetResults;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.util.Arrays;
import java.util.Objects;

import static com.ntak.pearlzip.archive.acc.constants.CommonsCompressConstants.PROFILE;
import static com.ntak.pearlzip.archive.acc.constants.CommonsCompressLoggingConstants.*;
import static com.ntak.pearlzip.archive.acc.util.CommonsCompressUtil.getArchiveFormat;
import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.KEY_FILE_PATH;
import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.TMP_DIR_PREFIX;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.*;
import static com.ntak.pearlzip.archive.util.CompressUtil.crcHashFile;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static java.util.zip.Deflater.BEST_COMPRESSION;
import static org.apache.commons.compress.compressors.CompressorStreamFactory.BZIP2;

/**
 *   Implementation of an Archive Write Service, which utilises the Apache Commons Compress library underneath for
 *   various formats.
 *
 *   @author Aashutos Kakshepati
 */
public class CommonsCompressArchiveWriteService implements ArchiveWriteService {

    private static final Logger LOGGER = LoggerContext.getContext().getLogger(CommonsCompressArchiveWriteService.class);

    @Override
    public void createArchive(long sessionId, String archivePath, FileInfo... files) {
        // Set default compression options
        try {
            ArchiveInfo archiveInfo = ArchiveService.generateDefaultArchiveInfo(archivePath);
            createArchive(sessionId, archiveInfo, files);
        } catch (IndexOutOfBoundsException e) {
            ArchiveService.DEFAULT_BUS.post(new ProgressMessage(sessionId, COMPLETED,COMPLETED,1,1));
        }
    }

    @Override
    public void createArchive(long sessionId, ArchiveInfo archiveInfo, FileInfo... files) {
        try {
            String archivePath = archiveInfo.getArchivePath();
            String format = getArchiveFormat(archivePath);
            boolean isCompressor = switch(format.toLowerCase()) {
                // Compressor streams - create tar file and then compress with the below
                case CompressorStreamFactory.GZIP, BZIP2, CompressorStreamFactory.XZ -> true;

                // Archive Output Stream
                case ArchiveStreamFactory.JAR, ArchiveStreamFactory.ZIP, ArchiveStreamFactory.SEVEN_Z,
                        ArchiveStreamFactory.TAR -> false;
                default -> false;
            };

            if (!format.isEmpty()) {
                if (isCompressor) {
                    // Compress first file
                    if (Objects.nonNull(files) && files.length == 1) {
                        executeFileCompressor(sessionId, archivePath, files);
                    } else { // Empty Compressor archive created
                        executeArchiveCompressor(sessionId, archivePath, files);
                    }
                } else {
                    executeArchiver(sessionId, archivePath, files);
                }
            }
        } catch (Exception e) {
            // TITLE: Issue creating archive
            // HEADER: The archive %s could not be created
            // BODY: Exception %s was thrown on the attempt to create the archive. Further details can be found
            // below.
            DEFAULT_BUS.post(new ErrorMessage(sessionId,
                                              resolveTextKey(TITLE_ARCHIVE_SERVICE_CREATE_EXCEPTION),
                                              resolveTextKey(HEADER_ARCHIVE_SERVICE_CREATE_EXCEPTION, archiveInfo.getArchivePath()),
                                              resolveTextKey(BODY_ARCHIVE_SERVICE_CREATE_EXCEPTION, e.getClass().getCanonicalName()),
                                              e,
                                              archiveInfo));
        } finally {
            ArchiveService.DEFAULT_BUS.post(new ProgressMessage(sessionId, COMPLETED,COMPLETED,1,1));
        }
    }

    private void executeArchiveCompressor(long sessionId, String archivePath, FileInfo... files) throws Exception {
        String format = getArchiveFormat(archivePath);
        try(OutputStream fo = Files.newOutputStream(Paths.get(archivePath));
            CompressorOutputStream cos = CompressorStreamFactory.findAvailableCompressorOutputStreamProviders()
                                                                .get(format)
                                                                .createCompressorOutputStream(format, fo);
            TarArchiveOutputStream aoStream = new TarArchiveOutputStream(cos)) {
            prepareStream(aoStream);
            addEntriesToArchiveStream(sessionId, aoStream, files);
            cos.flush();
        } catch(CompressorException | IOException e) {
            // LOG: Issue adding entries to archive or creating archive %s. Message: %s
            LOGGER.error(resolveTextKey(LOG_ARCHIVE_SERVICE_CREATE_EXCEPTION, archivePath, e.getMessage()));
            throw e;
        }
    }

    private static void executeFileCompressor(long sessionId, String archivePath, FileInfo... files) throws Exception {
        String format = getArchiveFormat(archivePath);
        Path path = null;
        try (OutputStream fo = Files.newOutputStream(Paths.get(archivePath));
             CompressorOutputStream cos = CompressorStreamFactory.findAvailableCompressorOutputStreamProviders()
                                                                 .get(format)
                                                                 .createCompressorOutputStream(format, fo)
        ) {
            path = Path.of((String) files[0].getAdditionalInfoMap()
                                            .get(KEY_FILE_PATH));
            cos.write(Files.readAllBytes(path));
            cos.flush();
        } catch(CompressorException | IOException | NullPointerException | IllegalArgumentException e) {
            // LOG: Issue adding entries to archive or creating archive %s. Message: %s
            LOGGER.error(resolveTextKey(LOG_ARCHIVE_SERVICE_CREATE_EXCEPTION, archivePath, e.getMessage()));
            throw e;
        } finally {
            DEFAULT_BUS.post(new ProgressMessage(sessionId, PROGRESS,
                                                 resolveTextKey(LBL_PROGRESS_LOADED_ENTRY,
                                                                path),
                                                 1,
                                                 1));
        }
    }

    private void executeArchiver(long sessionId, String archivePath, FileInfo[] files) throws Exception {
        final String extension = getArchiveFormat(archivePath);
        try(final OutputStream oStream = Files.newOutputStream(Path.of(archivePath));
            final ArchiveOutputStream aoStream =
                    ArchiveStreamFactory.findAvailableArchiveOutputStreamProviders()
                                        .get(extension)
                                        .createArchiveOutputStream(extension, oStream, null)
        ) {
            prepareStream(aoStream);
            addEntriesToArchiveStream(sessionId, aoStream, files);
        } catch(IOException | ArchiveException e) {
            // LOG: Issue adding entries to archive or creating archive %s. Message: %s
            LOGGER.error(resolveTextKey(LOG_ARCHIVE_SERVICE_CREATE_EXCEPTION, archivePath, e.getMessage()));
            throw e;
        }
    }

    private void addEntriesToArchiveStream(long sessionId, ArchiveOutputStream aoStream, FileInfo... files) throws IOException {
        int total = (int) Arrays.stream(files)
                                .filter(s -> !s.isFolder())
                                .count();
        for (FileInfo f : files) {
            if (!f.isFolder()) {
                try {
                    File file = new File((String) f.getAdditionalInfoMap()
                                                   .get(KEY_FILE_PATH));
                    if (file.exists() && !Files.isSymbolicLink(file.toPath())) {
                        ArchiveEntry entry =
                                aoStream.createArchiveEntry(file, f.getFileName());

                        prepareArchiveEntry(entry, f);

                        if (aoStream.canWriteEntryData(entry)) {
                            aoStream.putArchiveEntry(entry);
                            try(InputStream fileStream = Files.newInputStream(file.toPath())) {
                                IOUtils.copy(fileStream, aoStream);
                            }

                            // Loaded entry %s
                            DEFAULT_BUS.post(new ProgressMessage(sessionId, PROGRESS,
                                                                 resolveTextKey(LBL_PROGRESS_LOADED_ENTRY,
                                                                                f.getFileName()),
                                                                 1,
                                                                 total)
                            );
                        }

                        aoStream.closeArchiveEntry();
                        aoStream.flush();
                    } else {
                        // TODO: Need to handle symlink: Zip, Jar and Tar etc.
                        //       Zips can keep the symlink, tar copies original file. Need to determine if relative or absolute
                        //       paths and also whether a hard or soft link...
                        // LOG: File %s was detected as a symbolic link. The file will not be added to the archive.
                        LOGGER.warn(resolveTextKey(LOG_SKIP_SYMLINK, file.getAbsolutePath()));
                    }
                } catch(IOException e) {
                    // LOG: Issue adding entry %s(%s) to archive. Message: %s
                    LOGGER.error(resolveTextKey(LOG_ARCHIVE_SERVICE_ADD_EXCEPTION,
                                                f.getFileName(),
                                                f.getAdditionalInfoMap()
                                                 .get(KEY_FILE_PATH),
                                                e.getMessage()));
                }
            }
            else {
                File file = new File((String) f.getAdditionalInfoMap()
                                               .get(KEY_FILE_PATH));
                if (file.exists()) {
                    ArchiveEntry entry =
                            aoStream.createArchiveEntry(file, f.getFileName());
                    prepareArchiveEntry(entry, f);
                    aoStream.putArchiveEntry(entry);
                    aoStream.closeArchiveEntry();
                    aoStream.flush();
                }
            }
        }

        DEFAULT_BUS.post(new ProgressMessage(sessionId, PROGRESS,
                                             resolveTextKey(LOG_ACC_EB_FINISHING_PROCESS),
                                             total,
                                             total));
        aoStream.finish();
    }

    private void prepareStreamEntry(ArchiveEntry entry) {
        if (entry instanceof ZipArchiveEntry zae) {
            try {
                final byte[] bytComment = Arrays.stream(zae.getExtraFields())
                                                .filter(UnicodeCommentExtraField.class::isInstance)
                                                .findFirst()
                                                .get()
                                                .getLocalFileDataData();

                if (bytComment.length > 5) {
                    String comment = StandardCharsets.UTF_8.decode(ByteBuffer.wrap((Arrays.copyOfRange(bytComment, 5,
                                                                                                       bytComment.length))))
                                                           .toString();
                    zae.setComment(comment);
                }
            } catch (Exception e) {}
        }
    }

    private void prepareStream(ArchiveOutputStream aoStream) {
        if (aoStream instanceof TarArchiveOutputStream tarOStream) {
            tarOStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
            tarOStream.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
            tarOStream.setAddPaxHeadersForNonAsciiNames(true);
        }

        if (aoStream instanceof ZipArchiveOutputStream zipOStream) {
            zipOStream.setUseZip64(Zip64Mode.AsNeeded);
            zipOStream.setCreateUnicodeExtraFields(ZipArchiveOutputStream.UnicodeExtraFieldPolicy.ALWAYS);
            zipOStream.setMethod(ZipArchiveOutputStream.DEFLATED);
            zipOStream.setLevel(BEST_COMPRESSION);
        }
    }

    private void prepareArchiveEntry(ArchiveEntry entry, FileInfo file) {
        try {
            Path filePath = Paths.get(file.getAdditionalInfoMap().getOrDefault(KEY_FILE_PATH,"").toString());
            BasicFileAttributeView basicView =
                    Files.getFileAttributeView(filePath, BasicFileAttributeView.class);

            if (entry instanceof JarArchiveEntry jarEntry) {
                jarEntry.setMethod(JarArchiveOutputStream.DEFLATED);
                jarEntry.setComment(file.getComments());
                jarEntry.setCrc(crcHashFile(filePath.toFile()));
                if (Objects.nonNull(basicView) && Objects.nonNull(basicView.readAttributes()) && !Files.isSymbolicLink(filePath)) {
                    jarEntry.setCreationTime(basicView.readAttributes()
                                                      .creationTime());
                    jarEntry.setLastModifiedTime(basicView.readAttributes()
                                                          .lastModifiedTime());
                    jarEntry.setLastAccessTime(basicView.readAttributes()
                                                        .lastModifiedTime());
                    jarEntry.setInternalAttributes(file.getAttributes());
                }
                return;
            }

            if (entry instanceof ZipArchiveEntry zipEntry) {
                zipEntry.setMethod(ZipArchiveOutputStream.DEFLATED);
                zipEntry.setCrc(crcHashFile(filePath.toFile()));
                zipEntry.setComment(file.getComments());
                if (Objects.nonNull(basicView) && Objects.nonNull(basicView.readAttributes()) && !Files.isSymbolicLink(filePath)) {
                    zipEntry.setCreationTime(basicView.readAttributes()
                                                      .creationTime());
                    zipEntry.setLastModifiedTime(basicView.readAttributes()
                                                          .lastModifiedTime());
                    zipEntry.setLastAccessTime(basicView.readAttributes()
                                                        .lastModifiedTime());
                    zipEntry.setInternalAttributes(file.getAttributes());
                }
                return;
            }

            if (entry instanceof TarArchiveEntry tarEntry) {
                if (Objects.nonNull(file.getUser())) {
                    tarEntry.setUserName(file.getUser());
                }

                if (Objects.nonNull(file.getUser())) {
                    tarEntry.setGroupName(file.getGroup());
                }

                if (Objects.nonNull(basicView) && Objects.nonNull(basicView.readAttributes()) && !Files.isSymbolicLink(filePath)) {
                    tarEntry.setModTime(basicView.readAttributes().lastModifiedTime().toMillis());
                }
            }
        } catch (Exception e) {
            // Issue occurred on preparing archive entry: %s.\nException type: %s.\nStack trace:\n%s
            LOGGER.error(resolveTextKey(LOG_ACC_PREPARE_ENTRY_ISSUE,
                                        file.getFileName(),
                                        e.getClass().getCanonicalName(),
                                        LoggingUtil.getStackTraceFromException(e)));
        }
    }

    @Override
    public boolean addFile(long sessionId, String archivePath, FileInfo... files) {
        ArchiveInfo archiveInfo = ArchiveService.generateDefaultArchiveInfo(archivePath);

        return addFile(sessionId, archiveInfo, files);
    }

    @Override
    public boolean addFile(long sessionId, ArchiveInfo archiveInfo, FileInfo... files) {
        try {
            String archivePath = archiveInfo.getArchivePath();
            Path tempDir = Files.createTempDirectory(TMP_DIR_PREFIX);
            Path tmpArchive = Paths.get(tempDir.toString(), Paths.get(archivePath).getFileName().toString());
            Files.createFile(tmpArchive);

            try(
                    final InputStream iStream = Files.newInputStream(Path.of(archivePath));
                    final ArchiveInputStream aiStream =
                            ArchiveStreamFactory.findAvailableArchiveInputStreamProviders()
                                                .get(getArchiveFormat(archivePath))
                                                .createArchiveInputStream(getArchiveFormat(archivePath), iStream, null);
                    final OutputStream oStream = Files.newOutputStream(Path.of(tmpArchive.toString()));
                    final ArchiveOutputStream aoStream =
                            ArchiveStreamFactory.findAvailableArchiveOutputStreamProviders()
                                                .get(getArchiveFormat(tmpArchive.toString()))
                                                .createArchiveOutputStream(getArchiveFormat(tmpArchive.toString()), oStream, null)
            ) {
                prepareStream(aoStream);
                ArchiveEntry existingEntry;
                while ((existingEntry = aiStream.getNextEntry()) != null) {
                    prepareStreamEntry(existingEntry);
                    aoStream.putArchiveEntry(existingEntry);
                    IOUtils.copy(aiStream, aoStream);
                    aoStream.closeArchiveEntry();
                }
                addEntriesToArchiveStream(sessionId, aoStream, files);
            } catch(Exception e) {
                // LOG: Issue occurred on adding file to archive %s.\nException type: %s.\nStack trace:\n%s
                // TITLE: Issue adding to archive
                // HEADER: An entry could not be added to archive %s
                // BODY: Exception %s was thrown on the attempt to add an entry to archive. Further details can be
                // found below.
                LOGGER.error(resolveTextKey(LOG_ACC_ADD_FILE_ISSUE,
                                            archivePath,
                                            e.getClass().getCanonicalName(),
                                            LoggingUtil.getStackTraceFromException(e)));

                LOGGER.error(resolveTextKey(LOG_ACC_ADD_FILE_ISSUE, archiveInfo.getArchivePath(),
                                            e.getMessage()));
                DEFAULT_BUS.post(new ErrorMessage(sessionId,
                                                  resolveTextKey(TITLE_ACC_ADD_FILE_ISSUE),
                                                  resolveTextKey(HEADER_ACC_ADD_FILE_ISSUE, archiveInfo.getArchivePath()),
                                                  resolveTextKey(BODY_ACC_ADD_FILE_ISSUE, e.getClass().getCanonicalName()),
                                                  e,
                                                  archiveInfo));
            }
            Files.delete(Path.of(archivePath));
            Files.move(tmpArchive, Path.of(archivePath));
            Files.deleteIfExists(tmpArchive);

            return true;
        } catch (IOException e) {
            // IO Issue occurred on trying to initiate the archive process.\nStack trace:\n%s
            LOGGER.error(resolveTextKey(LOG_ACC_INIT_I0_ISSUE,
                                        archiveInfo.getArchivePath(),
                                        e.getClass().getCanonicalName(),
                                        LoggingUtil.getStackTraceFromException(e)));
        }

        return false;
    }

    @Override
    public boolean deleteFile(long sessionId, String archivePath, FileInfo file) {
        ArchiveInfo archiveInfo = ArchiveService.generateDefaultArchiveInfo(archivePath);
        return deleteFile(sessionId, archiveInfo, file);
    }

    @Override
    public boolean deleteFile(long sessionId, ArchiveInfo archiveInfo, FileInfo file) {
        String archivePath = archiveInfo.getArchivePath();
        final ChangeSet changeSet = new ChangeSet();
        if (file.isFolder()) {
            changeSet.deleteDir(file.getFileName());
        } else {
            changeSet.delete(file.getFileName());
        }

        try {
            Path tempDir = Files.createTempDirectory(TMP_DIR_PREFIX);
            Path tmpArchive = Paths.get(tempDir.toString(),
                                        Paths.get(archivePath)
                                             .getFileName()
                                             .toString());
            Files.createFile(tmpArchive);

            try(
                    final InputStream iStream = Files.newInputStream(Path.of(archivePath));
                    final ArchiveInputStream aiStream =
                            ArchiveStreamFactory.findAvailableArchiveInputStreamProviders()
                                                .get(getArchiveFormat(archivePath))
                                                .createArchiveInputStream(getArchiveFormat(archivePath), iStream, null);
                    final OutputStream oStream = Files.newOutputStream(tmpArchive);
                    final ArchiveOutputStream aoStream =
                            ArchiveStreamFactory.findAvailableArchiveOutputStreamProviders()
                                                .get(getArchiveFormat(archivePath))
                                                .createArchiveOutputStream(getArchiveFormat(archivePath), oStream, null)
            ) {
                // Deleting entries...
                prepareStream(aoStream);
                DEFAULT_BUS.post(new ProgressMessage(sessionId, PROGRESS,
                                                     resolveTextKey(LBL_PROGRESS_DELETING_ENTRIES),
                                                     0,
                                                     1));

                ChangeSetPerformer csPerformer = new ChangeSetPerformer(changeSet);
                ChangeSetResults results = csPerformer.perform(aiStream, aoStream);

                // Deleted entries
                DEFAULT_BUS.post(new ProgressMessage(sessionId, PROGRESS,
                                                     resolveTextKey(LBL_PROGRESS_DELETED_ENTRIES),
                                                     1,
                                                     1));

                if (results.getDeleted().size() > 0) {
                    // Clearing up...
                    DEFAULT_BUS.post(new ProgressMessage(sessionId, PROGRESS,
                                                         resolveTextKey(LBL_PROGRESS_CLEAR_UP),
                                                         1,
                                                         1));
                    Files.delete(Path.of(archivePath));
                    Files.move(tmpArchive, Path.of(archivePath));
                    Files.deleteIfExists(tmpArchive);
                    return true;
                }
            } catch(Exception e) {
                // LOG: Issue occurred on deleting file from archive %s.\nException type: %s.\nStack trace:\n%s
                // TITLE: Issue deleting archive
                // HEADER: An entry could not be removed from archive %s
                // BODY: Exception %s was thrown on the attempt to remove an entry from the archive. Further details can be
                // found below.
                LOGGER.error(resolveTextKey(LOG_ACC_DELETE_FILE_ISSUE,
                                            archivePath,
                                            e.getClass().getCanonicalName(),
                                            LoggingUtil.getStackTraceFromException(e)));
                DEFAULT_BUS.post(new ErrorMessage(sessionId,
                                                  resolveTextKey(TITLE_ACC_DELETE_FILE_ISSUE),
                                                  resolveTextKey(HEADER_ACC_DELETE_FILE_ISSUE, archiveInfo.getArchivePath()),
                                                  resolveTextKey(BODY_ACC_DELETE_FILE_ISSUE, e.getClass().getCanonicalName()),
                                                  e,
                                                  archiveInfo));
            }
        } catch (IOException e) {
            // IO Issue occurred on trying to initiate the archive process.\nStack trace:\n%s
            LOGGER.error(resolveTextKey(LOG_ACC_INIT_I0_ISSUE,
                                        archivePath,
                                        e.getClass().getCanonicalName(),
                                        LoggingUtil.getStackTraceFromException(e)));
        }

        return false;
    }

    @Override
    public ArchiveServiceProfile getArchiveServiceProfile() {
        return PROFILE;
    }
}
