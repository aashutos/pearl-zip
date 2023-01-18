/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.stages.startup;

import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.util.AbstractStartupStage;
import com.ntak.pearlzip.ui.util.StoreRepoDetails;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.ntak.pearlzip.ui.constants.ZipConstants.*;

/**
 *  Sets up caches and constants after logging initialisation.
 *
 *  @author Aashutos Kakshepati
 */
public class InMemoryCacheStartupStage extends AbstractStartupStage {

    @Override
    public void executeProcess() {
        // 1 Creating maps and general objects with no dependencies
        InternalContextCache.INTERNAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_APP_LATCH, new CountDownLatch((1)));
        InternalContextCache.INTERNAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_LCK_CLEAR_CACHE, new ReentrantReadWriteLock(true));
        InternalContextCache.INTERNAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_POST_PZAX_COMPLETION_CALLBACK, (Runnable)() -> System.exit(0));
        InternalContextCache.INTERNAL_CONFIGURATION_CACHE.<Map<String,StoreRepoDetails>>setAdditionalConfig(CK_STORE_REPO, new ConcurrentHashMap<>());
    }
}
