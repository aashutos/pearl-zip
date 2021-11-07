/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.archive.pub;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  Java bean which stores normalised data about a particular entry in a zip archive.
 *  @author Aashutos Kakshepati
 */
public class FileInfo {
    private int index;
    private final int level;
    private final String fileName;
    private final long crcHash;
    private final long packedSize;
    private final long rawSize;
    private final LocalDateTime lastWriteTime;
    private final LocalDateTime lastAccessTime;
    private final LocalDateTime creationTime;
    private final String user;
    private final String group;
    private final int attributes;
    private String comments;
    private final boolean isFolder;
    private final boolean isEncrypted;
    private final Map<String,Object> additionalInfoMap;

    public FileInfo(int index, int level, String fileName, long crcHash, long packedSize, long rawSize, LocalDateTime lastWriteTime, LocalDateTime lastAccessTime, LocalDateTime creationTime, String user, String group, int attributes, String comments, boolean isFolder, boolean isEncrypted, Map<String,Object> additionalInfoMap) {
        assert index >= 0 : "Indices should be a positive integer (including 0)";
        assert level >= 0 : "Level should be a positive integer (including 0)";
        Objects.requireNonNull(fileName, "Filename should not be null");

        this.index = index;
        this.level = level;
        this.fileName = fileName;
        this.crcHash = crcHash;
        this.packedSize = packedSize;
        this.rawSize = rawSize;
        this.lastWriteTime = lastWriteTime;
        this.lastAccessTime = lastAccessTime;
        this.creationTime = creationTime;
        this.user = user;
        this.group = group;
        this.attributes = attributes;
        this.comments = comments;
        this.isFolder = isFolder;
        this.isEncrypted = isEncrypted;
        this.additionalInfoMap = new ConcurrentHashMap<>(additionalInfoMap);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getLevel() {
        return level;
    }

    public String getFileName() {
        return fileName;
    }

    public long getCrcHash() {
        return crcHash;
    }

    public long getPackedSize() {
        return packedSize;
    }

    public long getRawSize() {
        return rawSize;
    }

    public LocalDateTime getLastWriteTime() {
        return lastWriteTime;
    }

    public LocalDateTime getLastAccessTime() {
        return lastAccessTime;
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public String getUser() {
        return user;
    }

    public String getGroup() {
        return group;
    }

    public int getAttributes() {
        return attributes;
    }

    public String getComments() {
        return comments;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    public Map<String,Object> getAdditionalInfoMap() {
        return additionalInfoMap;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public FileInfo getSelf() { return this; }

    @Override
    public String toString() {
        return "FileInfo{" +
                "index=" + index +
                ", level=" + level +
                ", fileName='" + fileName + '\'' +
                ", crcHash=" + crcHash +
                ", packedSize=" + packedSize +
                ", rawSize=" + rawSize +
                ", lastWriteTime=" + lastWriteTime +
                ", lastAccessTime=" + lastAccessTime +
                ", creationTime=" + creationTime +
                ", user='" + user + '\'' +
                ", group='" + group + '\'' +
                ", attributes=" + attributes +
                ", comments='" + comments + '\'' +
                ", isFolder=" + isFolder +
                ", isEncrypted=" + isEncrypted +
                ", additionalInfoMap=" + additionalInfoMap +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FileInfo fileInfo = (FileInfo) o;
        return level == fileInfo.level && fileName.equals(fileInfo.fileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(level, fileName);
    }
}
