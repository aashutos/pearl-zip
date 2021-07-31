/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.archive.pub;

import javafx.scene.Node;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 *  Interface defining functionality associated with the reading of archives.
 *  @author Aashutos Kakshepati
 */
public interface ArchiveReadService extends ArchiveService {
    /**
     *  Extracts metadata from the archive specified and wraps it in an ArchiveInfo object. The default
     *  implementation does not parse the file and just returns the default archive setup with minimum configuration.
     *  The default archive has no guarantee of its compatibility with the underlying archive as it may have custom
     *  features enabled e.g. encryption.
     *
     *  @param archivePath
     *  @return ArchiveInfo - POJO containing Archive Information parsed from the file metadata
     */
    default ArchiveInfo generateArchiveMetaData(String archivePath) {
        return ArchiveService.generateDefaultArchiveInfo(archivePath);
    }

    /**
     *   List contents of archive by archive path given. Hence, uses default archive info settings as per dictated by
     *   the archive service implementation.
     *
     *   @param sessionId Unique identifier representing the session the ProgressMessage will display messages for
     *   @param archivePath
     *   @return List&lt;FileInfo&gt; - List of normalised files and folders from the archive
     */
    List<FileInfo> listFiles(long sessionId, String archivePath);

    /**
     *   List contents of archive by the archive info object given. It can contain properties that can handle
     *   specific file specifications such as encryption methodology and credentials.
     *
     *   @param sessionId Unique identifier representing the session the ProgressMessage will display messages for
     *   @param archiveInfo
     *   @return List&lt;FileInfo&gt; - List of normalised files and folders from the archive
     */
    List<FileInfo> listFiles(long sessionId, ArchiveInfo archiveInfo);

    /**
     *   Extracts the specified file by the FileInfo object supplied from the archive.
     *
     *   @param sessionId Unique identifier representing the session the ProgressMessage will display messages for
     *   @param targetLocation Location to extract file to
     *   @param archivePath
     *   @param file The File metadata from the archive that is to be extracted
     *   @return boolean - returns true, if extraction was successful
     */
    boolean extractFile(long sessionId, Path targetLocation, String archivePath, FileInfo file);

    /**
     *   Extracts the specified file by the FileInfo object supplied from the archive. The archive info object can
     *   provide archive-specific configuration e.g. decryption passwords.
     *
     *   @param sessionId Unique identifier representing the session the ProgressMessage will display messages for
     *   @param targetLocation Location to extract file to
     *   @param archiveInfo
     *   @param file The File metadata from the archive that is to be extracted
     *   @return boolean - returns true, if extraction was successful
     */
    boolean extractFile(long sessionId, Path targetLocation, ArchiveInfo archiveInfo, FileInfo file);

    /**
     *   Checks the integrity of the archive. It is assumed that the archive headers are unencrypted. Hence, archive
     *   path would suffice.
     *
     *   @param sessionId Unique identifier representing the session the ProgressMessage will display messages for
     *   @param archivePath
     *   @return boolean - returns true if the archive is valid
     */
    boolean testArchive(long sessionId, String archivePath);

    /**
     *   Given the archive information passed in, the implementation can use this method to determine whether a
     *   dialog needs to be presented to the user prior to opening the archive in order to provide suitable options
     *   and archive-specific configuration e.g. decryption passwords.
     *
     *   @param archiveInfo
     *   @return Optional&lt;Node&gt; - The JavaFX Node representing the form to show prior to opening the archive or
     *   Empty if none is available.
     */
    default Optional<Node> getOpenArchiveOptionsPane(ArchiveInfo archiveInfo) { return Optional.empty(); }

    /**
     *   Lists out explicitly all archive formats that can be read by the implementation of the archive service.
     *
     *   @return List&lt;String&gt; - List of unique archive formats supported
     */
    List<String> supportedReadFormats();
}
