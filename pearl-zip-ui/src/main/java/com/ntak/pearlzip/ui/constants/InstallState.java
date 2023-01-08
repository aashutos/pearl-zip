/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.constants;

public enum InstallState {
    ALL("All"),
    INSTALLABLE("Installable"),
    INSTALLED("Installed"),
    UPDATABLE("Updatable"),
    INCOMPATIBLE("Incompatible");

    private final String value;

    InstallState(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
