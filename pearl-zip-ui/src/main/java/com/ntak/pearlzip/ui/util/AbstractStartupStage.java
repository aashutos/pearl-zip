/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.util;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import static com.ntak.pearlzip.archive.constants.LoggingConstants.*;
import static com.ntak.pearlzip.archive.util.LoggingUtil.getStackTraceFromException;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;

/**
 *  A post logging initiated stage. Logging is provided on start and end of stage execution as well as to present error stack traces.
 *
 *  @author Aashutos Kakshepati
 */
public abstract class AbstractStartupStage extends AbstractSeedStartupStage {
    static final Logger LOGGER =  LoggerContext.getContext().getLogger(AbstractStartupStage.class);
    String NAME;

    public AbstractStartupStage() {
        this.NAME = this.getClass().getName();
    }

    public AbstractStartupStage(String name) {
        this.NAME = name;
    }

    @Override
    public void execute() {
        try {
            logStartStage();
            executeProcess();
            logEndStage();
        } catch (Exception e) {
            logException(e);
        }
    }

    public abstract void executeProcess() throws Exception;

    void logStartStage() {
        // LOG: Starting stage %s...
        LOGGER.info(resolveTextKey(LOG_START_STAGE, NAME));
    }

    void logEndStage() {
        // LOG: Stage %s completed.
        LOGGER.info(resolveTextKey(LOG_END_STAGE, NAME));
    }

    @Override
    void logException(Throwable throwable) {
        // LOG: Issue on executing stage %s.\nStack trace:\n %s
        LOGGER.error(resolveTextKey(LOG_ISSUE_STAGE, NAME, getStackTraceFromException(throwable)));
    }
}
