/*
 * Copyright © 2023 92AK
 */
package com.ntak.testfx.internal;

import javafx.application.Platform;

public class TestFXUtil {
    public static void runLater(final Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }
}
