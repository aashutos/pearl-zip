/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.util;

public enum OptionTab {

    GENERAL(1),
    BOOTSTRAP_PROPERTIES(2),
    PROVIDERS(3),
    PLUGIN_LOADER(4),
    THEMES(5),
    LANGUAGES(6),
    STORE(7);

    private int index;

    OptionTab(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
