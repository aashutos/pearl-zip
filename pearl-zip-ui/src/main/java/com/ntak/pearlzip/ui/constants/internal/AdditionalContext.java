/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.constants.internal;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  In-memory cache of configuration context objects used internally by PearlZip for set up and internal usage.
 */
public class AdditionalContext {
    private final Map<String,Object> ADDITIONAL_CONFIG = new ConcurrentHashMap<>();

    public <T> Optional<T> getAdditionalConfig(String key) {
        if (Objects.nonNull(key)) {
            try {
                T value = (T) ADDITIONAL_CONFIG.get(key);
                return Optional.ofNullable(value);
            } catch (Exception e) {
            }
        }
        return Optional.empty();
    }

    public <T> void setAdditionalConfig(String key, T value) {
        if (Objects.nonNull(key) && Objects.nonNull(value)) {
            ADDITIONAL_CONFIG.put(key, value);
        }
    }
}
