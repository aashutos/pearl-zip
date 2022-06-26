/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.archive.pub;

/**
 *  Interface defining functionality associated with the writing of archives.
 *  @author Aashutos Kakshepati
 */
public interface ArchiveWriteService extends ArchiveService {

    String CREATE_OPTIONS = "pearlzip.pane.create-options";

    /**
     *  Creates an archive at the path specified with optionally adding files to the archive.
     *
     *  @param sessionId Unique identifier representing the session the ProgressMessage will display messages for
     *  @param archivePath
     *  @param files
     */
    void createArchive(long sessionId, String archivePath, FileInfo... files);

    /**
     *  Creates an archive at the path specified with optionally adding files to the archive. The archive info object can
     *  provide archive-specific configuration e.g. decryption passwords.
     *
     *  @param sessionId Unique identifier representing the session the ProgressMessage will display messages for
     *  @param archiveInfo
     *  @param files
     */
    void createArchive(long sessionId, ArchiveInfo archiveInfo, FileInfo... files);

    /**
     *  Adds files(s) to the location within the archive as specified by the FileInfo object.
     *
     *  @param sessionId Unique identifier representing the session the ProgressMessage will display messages for
     *  @param archivePath
     *  @param files FileInfo archive metadata representing what is to be added to the archive
     *  @return boolean - returns true if file was added successfully
     */
    boolean addFile(long sessionId, String archivePath, FileInfo... files);

    /**
     *  Adds file(s) to the location within the archive as specified by the FileInfo object. The
     *      *   archive info object can provide archive-specific configuration e.g. decryption passwords.
     *
     *  @param sessionId Unique identifier representing the session the ProgressMessage will display messages for
     *  @param archiveInfo
     *  @param files FileInfo archive metadata representing what is to be added to the archive
     *  @return boolean - returns true if file was added successfully
     */
    boolean addFile(long sessionId, ArchiveInfo archiveInfo, FileInfo... files);

    /**
     *   Deletes the specified file represented by a FileInfo archive metadata from the specified archive.
     *
     *   @param sessionId Unique identifier representing the session the ProgressMessage will display messages for
     *   @param archivePath
     *   @param file FileInfo archive metadata representing what is to be deleted from the archive
     *   @return boolean - returns true if file is successfully deleted
     */
    boolean deleteFile(long sessionId, String archivePath, FileInfo file);

    /**
     *   Deletes the specified file represented by a FileInfo archive metadata from the specified archive. The
     *   archive info object can provide archive-specific configuration e.g. decryption passwords.
     *
     *   @param sessionId Unique identifier representing the session the ProgressMessage will display messages for
     *   @param archiveInfo
     *   @param file FileInfo archive metadata representing what is to be deleted from the archive
     *   @return boolean - returns true if file is successfully deleted
     */
    boolean deleteFile(long sessionId, ArchiveInfo archiveInfo, FileInfo file);
}
