/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.constants;

public enum ExtensionType {
    ALL("All"),
    THEME("Theme"),
    PLUGIN("Plugin");

    private String value;

    ExtensionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
