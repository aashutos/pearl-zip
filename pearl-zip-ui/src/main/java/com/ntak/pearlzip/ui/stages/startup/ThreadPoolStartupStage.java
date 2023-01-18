/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.stages.startup;

import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.util.AbstractStartupStage;
import com.ntak.pearlzip.ui.util.MetricProfile;
import com.ntak.pearlzip.ui.util.MetricProfileFactory;
import com.ntak.pearlzip.ui.util.MetricThreadFactory;

import java.util.Objects;
import java.util.concurrent.Executors;

import static com.ntak.pearlzip.ui.constants.ZipConstants.*;

/**
 *  Sets up thread pool for use in PearlZip.
 *
 *  @author Aashutos Kakshepati
 */
public class ThreadPoolStartupStage extends AbstractStartupStage {
    @Override
    public void executeProcess() throws Exception {
        // Initialising Thread Pool
        String klassName;
        MetricProfile profile = MetricProfile.getDefaultProfile();
        if (Objects.nonNull(klassName = System.getProperty(CNS_METRIC_FACTORY))) {
            try {
                MetricProfileFactory factory = (MetricProfileFactory) Class.forName(klassName)
                                                                           .getDeclaredConstructor()
                                                                           .newInstance();
                profile = factory.getProfile();
            } catch(Exception e) {

            }
            InternalContextCache.INTERNAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_PRIMARY_EXECUTOR_SERVICE,
                                                                                  Executors.newScheduledThreadPool(Math.max(Integer.parseInt(System.getProperty(CNS_THREAD_POOL_SIZE, "4")), 1),
                                                                                                                   MetricThreadFactory.create(profile))
            );
        } else {
            InternalContextCache.INTERNAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_PRIMARY_EXECUTOR_SERVICE,
                                                                                  Executors.newScheduledThreadPool(Math.max(Integer.parseInt(System.getProperty(CNS_THREAD_POOL_SIZE,"4")), 1),
                                                                                                                   MetricThreadFactory.create(MetricProfile.getDefaultProfile()))
            );
        }
    }
}
