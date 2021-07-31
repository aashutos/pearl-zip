/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.archive.pub;

import javafx.scene.Node;
import javafx.util.Pair;

import java.util.List;
import java.util.Optional;

/**
 *  Interface defining functionality associated with the writing of archives.
 *  @author Aashutos Kakshepati
 */
public interface ArchiveWriteService extends ArchiveService {

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

    /**
     *   Generates an Options screen that is to be displayed as a tab on the New Archive Form. The Pair object
     *   returned contains a String key pertaining to the title of the tab and a JavaFX Node containing the root
     *   object to be found within the tab.
     *
     *   @return Optional&lt;Pair&lt;String,Node&gt;&gt; - The object representing a New Form configuration tab for
     *   the archive service implementation
     */
    default Optional<Pair<String,Node>> getCreateArchiveOptionsPane() { return Optional.empty(); }

    /**
     *   Lists out explicitly all archive formats that can be written to by the implementation of the archive service.
     *
     *   @return List&lt;String&gt; - List of archive extensions that can be created and modified by the archive
     *   service implementation
     */
    List<String> supportedWriteFormats();
}
