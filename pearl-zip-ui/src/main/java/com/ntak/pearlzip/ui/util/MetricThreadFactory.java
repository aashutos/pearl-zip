/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.util;

import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.util.Optional;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.KEY_DEFAULT;
import static com.ntak.pearlzip.archive.util.LoggingUtil.getStackTraceFromException;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.CK_THREAD_GROUP;
import static com.ntak.pearlzip.ui.constants.ZipConstants.LOG_THREAD_EXECUTION_ISSUE;

/**
 *  Factory method used to generate a LoggingMetricThread.
 *  @author Aashutos Kakshepati
*/
public class MetricThreadFactory implements ThreadFactory {

    private final MetricProfile profile;
    private final ThreadGroup threadGroup;

    protected MetricThreadFactory(MetricProfile profile, String threadGroupName) {
        this.profile = profile;
        this.threadGroup = new ThreadGroup(Optional.ofNullable(threadGroupName).orElse(KEY_DEFAULT));
        InternalContextCache.INTERNAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_THREAD_GROUP, this.threadGroup);
    }

    public static ThreadFactory create(MetricProfile profile) {
        return new MetricThreadFactory(profile, KEY_DEFAULT);
    }

    @Override
    public Thread newThread(Runnable runnable) {
        return new LoggingMetricThread(threadGroup, runnable, profile.beforeMetric(), profile.afterMetric());
    }

    protected static class LoggingMetricThread extends Thread {

        private static final Logger LOGGER = LoggerContext.getContext().getLogger(LoggingMetricThread.class);

        private final Runnable runnable;
        private final Consumer<Thread> beforeMetric;
        private final Consumer<Thread> afterMetric;

        public LoggingMetricThread(ThreadGroup threadGroup, Runnable runnable) {
            this(threadGroup, runnable, (t)->{}, (t)->{});
        }

        public LoggingMetricThread(ThreadGroup threadGroup, Runnable runnable, Consumer<Thread> beforeMetric,
                Consumer<Thread> afterMetric) {
            super(threadGroup, runnable);
            this.runnable = runnable;
            this.beforeMetric = beforeMetric;
            this.afterMetric = afterMetric;
        }

        @Override
        public void run() {
            try {
                beforeMetric.accept(this);
                runnable.run();
            } catch(Exception e) {
                errorHandler(e);
            } finally {
                afterMetric.accept(this);
            }
        }

        static void errorHandler(Exception e) {
            final Thread.UncaughtExceptionHandler exceptionHandler = Thread.currentThread()
                                                                           .getUncaughtExceptionHandler();
            if (!(exceptionHandler instanceof ThreadGroup)) {
                exceptionHandler.uncaughtException(Thread.currentThread(), e);
            } else {
                // Exception occurred on Thread: %s with exception message: %s\nStack trace: %s
                LOGGER.error(resolveTextKey(LOG_THREAD_EXECUTION_ISSUE, Thread.currentThread()
                                                       .getName(),
                                 e.getLocalizedMessage(),
                                 getStackTraceFromException(e))
                );
            }
        }
    }

}
