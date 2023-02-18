/*
 * Copyright © 2023 92AK
 */
package com.ntak.testfx;

import java.util.regex.Pattern;

public class TestFXConstants {

    public static final long SHORT_PAUSE = 50;
    public static final long MEDIUM_PAUSE = 300;
    public static final long LONG_PAUSE = 1000;
    public static final long RETRIEVAL_TIMEOUT_MILLIS = 600000;
    public static final long POLLING_TIMEOUT = 2000;

    public enum Platform {
        WIN,LINUX,MAC
    }

    public static final Platform PLATFORM = getPlatform();
    public static final Pattern SSV = Pattern.compile(Pattern.quote("/"));

    private static Platform getPlatform() {
        String os = System.getProperty("os.name");

        if (os.startsWith("Windows")) {
            return Platform.WIN;
        }

        if (os.startsWith("Mac")) {
            return Platform.MAC;
        }

        return Platform.LINUX;
    }
}
