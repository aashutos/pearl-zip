/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.archive.model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  Bean object containing metadata/profile information regarding an installed plugin.
 *
 *  @author Aashutos Kakshepati
 */
public class PluginInfo {
    private final String name;
    private final String minVersion;
    private final String maxVersion;
    private final Set<String> licenses = new HashSet<>();
    private final Set<String> dependencies = new LinkedHashSet<>();
    private final Set<String> themes = new LinkedHashSet<>();
    private final List<String> hashFormats = new LinkedList<>();
    private final Map<String,String> properties = new ConcurrentHashMap<>();

    public PluginInfo(String name, String minVersion, String maxVersion, List<String> licenses,
            List<String> dependencies, List<String> hashFormats, List<String> themes,
            Map<String,String> properties) {
        this.name = name;
        this.minVersion = minVersion;
        this.maxVersion = maxVersion;
        this.licenses.addAll(licenses);
        this.dependencies.addAll(dependencies);
        this.hashFormats.addAll(hashFormats);
        this.themes.addAll(themes);
        this.properties.putAll(properties);
    }

    public String getName() {
        return name;
    }

    public String getMinVersion() {
        return minVersion;
    }

    public String getMaxVersion() {
        return maxVersion;
    }

    public Set<String> getLicenses() {
        return Collections.unmodifiableSet(licenses);
    }

    public Set<String> getDependencies() {
        return Collections.unmodifiableSet(dependencies);
    }

    public List<String> getHashFormats() {
        return Collections.unmodifiableList(hashFormats);
    }

    public Set<String> getThemes() {
        return themes;
    }

    public Map<String,String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }
}
