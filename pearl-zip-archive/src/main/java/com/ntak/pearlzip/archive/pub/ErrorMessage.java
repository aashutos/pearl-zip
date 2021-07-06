/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.archive.pub;

import static com.ntak.pearlzip.archive.constants.LoggingConstants.ERROR;

public class ErrorMessage extends ProgressMessage {

    private final String title;
    private final String header;
    private final Exception exception;
    private final ArchiveInfo archiveInfo;

    public ErrorMessage(long sessionId, String title, String header, String body, Exception exception, ArchiveInfo archiveInfo) {
        super(sessionId, ERROR, body, -1, 0);

        this.title = title;
        this.header = header;
        this.exception = exception;
        this.archiveInfo = archiveInfo;
    }

    public String getTitle() {
        return title;
    }

    public String getHeader() {
        return header;
    }

    public Exception getException() {
        return exception;
    }

    public ArchiveInfo getArchiveInfo() {
        return archiveInfo;
    }
}
