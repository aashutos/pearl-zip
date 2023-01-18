/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.stages.startup;

import com.ntak.pearlzip.archive.pub.ArchiveService;
import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.util.AbstractStartupStage;
import com.ntak.pearlzip.ui.util.ErrorAlertConsumer;
import com.ntak.pearlzip.ui.util.ProgressMessageTraceLogger;

import static com.ntak.pearlzip.ui.constants.ZipConstants.CK_ERROR_ALERT_CONSUMER;
import static com.ntak.pearlzip.ui.constants.ZipConstants.CK_MESSAGE_TRACE_LOGGER;

/**
 *  Sets up communication bus(es) for PearlZip internal messaging purposes.
 *
 *  @author Aashutos Kakshepati
 */
public class EventBusStartupStage extends AbstractStartupStage {

    @Override
    public void executeProcess() throws Exception {
        // Loading additional EventBus consumers
        ProgressMessageTraceLogger MESSAGE_TRACE_LOGGER = ProgressMessageTraceLogger.getMessageTraceLogger();
        InternalContextCache.INTERNAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_MESSAGE_TRACE_LOGGER, MESSAGE_TRACE_LOGGER);
        ArchiveService.DEFAULT_BUS.register(MESSAGE_TRACE_LOGGER);

        ErrorAlertConsumer ERROR_ALERT_CONSUMER = ErrorAlertConsumer.getErrorAlertConsumer();
        InternalContextCache.INTERNAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_ERROR_ALERT_CONSUMER, ERROR_ALERT_CONSUMER);
        ArchiveService.DEFAULT_BUS.register(ERROR_ALERT_CONSUMER);
    }
}
