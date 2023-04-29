/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.util;

import com.ntak.pearlzip.archive.util.LoggingUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 *  The root abstract class that performs safe execution of a stage and logs error to the last resort log file
 *  (as it is expected to be executed prior to logging being initiated).
 *
 *  This is a helper class to clearly define and easily modify the start up process for PearlZip going forward.
 *
 *  @author Aashutos Kakshepati
 */
public abstract class AbstractSeedStartupStage {
    private final String NAME;

    public AbstractSeedStartupStage() {
        this.NAME = this.getClass().getName();
    }

    public AbstractSeedStartupStage(String name) {
        this.NAME = name;
    }

    public void execute() {
        try {
            executeProcess();
        } catch (Exception e) {
            logException(e);
        }
    }

    public abstract void executeProcess() throws Exception;

    void logException(Throwable throwable) {
        Path lastResortLogFile = Paths.get(String.format("%s/.pzdump-%s.log",
                                                         System.getProperty("user.home"),
                                                         LocalDateTime.now().toInstant(ZoneOffset.UTC)));
        try {
            Files.writeString(lastResortLogFile, LoggingUtil.getStackTraceFromException(throwable), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        } catch(IOException e) {
        }
    }
}
