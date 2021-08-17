/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.model;

import com.ntak.pearlzip.archive.pub.ArchiveInfo;
import com.ntak.pearlzip.archive.pub.ArchiveReadService;
import com.ntak.pearlzip.archive.pub.ArchiveWriteService;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.pub.FrmMainController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ntak.pearlzip.archive.constants.LoggingConstants.LOG_ARCHIVE_INFO_ASSERT_PATH;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.LOG_ARCHIVE_INFO_ASSERT_READ_SERVICE;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;

/**
 *  Representation of the state of the Archive on an instance of the Pearl Zip UI as a specific point in time.
 *  @author Aashutos Kakshepati
*/
public class FXArchiveInfo {
    private final String parentPath;
    private final String archivePath;
    private final ArchiveReadService readService;
    private final ArchiveWriteService writeService;
    private final AtomicInteger depth = new AtomicInteger(0);
    private final FXMigrationInfo migrationInfo = new FXMigrationInfo();
    private FrmMainController controller;
    private final AtomicBoolean closeBypass = new AtomicBoolean(false);
    private final ArchiveInfo parentArchiveInfo;
    private final ArchiveInfo archiveInfo;
    private final FileInfo nestedFileInfoParent;

    private String prefix = "";
    private ObservableList<FileInfo> files;

    public FXArchiveInfo(String archivePath, ArchiveReadService readService, ArchiveWriteService writeService) {
        this(null, archivePath, readService, writeService, null);
    }

    public FXArchiveInfo(String parentPath, String archivePath, ArchiveReadService readService,
            ArchiveWriteService writeService, FileInfo nestedFileInfoParent) {
        this(Objects.nonNull(parentPath) ?
                     ZipState.getReadArchiveServiceForFile(parentPath).get().generateArchiveMetaData(parentPath)
                     : null,
             archivePath,
             readService,
             writeService,
             readService.generateArchiveMetaData(archivePath),
             nestedFileInfoParent);
    }

    public FXArchiveInfo(ArchiveInfo parentArchiveInfo, String archivePath, ArchiveReadService readService,
            ArchiveWriteService writeService, ArchiveInfo archiveInfo, FileInfo nestedFileInfoParent) {
        // LOG: Archive path should be valid
        assert Files.exists(Paths.get(archivePath)) : resolveTextKey(LOG_ARCHIVE_INFO_ASSERT_PATH);
        // LOG: Read service should not be null
        assert Objects.nonNull(readService) : resolveTextKey(LOG_ARCHIVE_INFO_ASSERT_READ_SERVICE);

        this.parentArchiveInfo = parentArchiveInfo;
        this.parentPath = Objects.nonNull(parentArchiveInfo) ? parentArchiveInfo.getArchivePath() : null;
        this.nestedFileInfoParent = Objects.nonNull(parentArchiveInfo) ? nestedFileInfoParent : null;
        this.archivePath = archivePath;
        this.readService = readService;
        this.writeService = writeService;
        this.archiveInfo = archiveInfo;

        setFiles(FXCollections.observableArrayList(
                new ArrayList<>(readService.listFiles(System.currentTimeMillis(), archivePath))));
    }

    public AtomicInteger getDepth() {
        return depth;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public synchronized ObservableList<FileInfo> getFiles() {
        return files;
    }

    public synchronized void setFiles(ObservableList<FileInfo> files) {
        this.files = files;
    }

    public String getArchivePath() {
        return archivePath;
    }

    public ArchiveReadService getReadService() {
        return readService;
    }

    public ArchiveWriteService getWriteService() {
        return writeService;
    }

    public String getParentPath() {
        return parentPath;
    }

    public FXMigrationInfo getMigrationInfo() {
        return migrationInfo;
    }

    public synchronized void refresh() {
        files.clear();
        files.addAll(readService.listFiles(System.currentTimeMillis(), archiveInfo));
        setPrefix("");
        depth.set(0);
    }

    public void setMainController(FrmMainController controller) {
        this.controller = controller;
    }

    public Optional<FrmMainController> getController() {
        return Optional.ofNullable(controller);
    }

    public AtomicBoolean getCloseBypass() {
        return closeBypass;
    }

    public ArchiveInfo getArchiveInfo() {
        return archiveInfo;
    }

    public ArchiveInfo getParentArchiveInfo() {
        return parentArchiveInfo;
    }

    public FileInfo getNestedFileInfoParent() {
        return nestedFileInfoParent;
    }
}
