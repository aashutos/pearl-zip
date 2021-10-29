/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.archive.constants;

import com.ntak.pearlzip.archive.pub.ArchiveInfo;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

/**
 *  Constant values utilised by the Archive service.
 *  @author Aashutos Kakshepati
 */
public class ArchiveConstants {
    public static final Properties CURRENT_SETTINGS = new Properties();
    public static final Properties WORKING_SETTINGS = new Properties();
    public static final Properties WORKING_APPLICATION_SETTINGS = new Properties();
    public static final List<Predicate<ArchiveInfo>> NEW_ARCHIVE_VALIDATORS = new CopyOnWriteArrayList<>();

    public static final ThreadGroup EVENTBUS_THREAD_GROUP = new ThreadGroup("EVENTBUS-THREAD-GROUP");
    public static final ExecutorService COM_BUS_EXECUTOR_SERVICE =
            Executors.newScheduledThreadPool(2*Runtime.getRuntime().availableProcessors(),
                                             (r)->new Thread(EVENTBUS_THREAD_GROUP,r));
}
