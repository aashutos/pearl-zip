/*
 * Copyright © 2021 92AK
 */
package com.ntak.testfx;

public class TestFXConstants {

    public enum Platform {
        WIN,LINUX,MAC
    }

    public static final Platform PLATFORM = getPlatform();

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
