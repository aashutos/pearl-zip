/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.constants;

import com.ntak.pearlzip.ui.constants.internal.AdditionalContext;

import java.util.Objects;
import java.util.Optional;

/**
 *  Exposed read-only view of context objects for downstream plugins.
 *
 *  @author Aashutos Kakshepati
 */
public class AdditionalContextReader {
    private final AdditionalContext CONFIG;

    public AdditionalContextReader(final AdditionalContext config) {
        if (Objects.isNull(config)) {
            this.CONFIG = new AdditionalContext();
        } else {
            this.CONFIG = config;
        }
    }

    public <T> Optional<T> getAdditionalConfig(String key) {
        return CONFIG.getAdditionalConfig(key);
    }
}
