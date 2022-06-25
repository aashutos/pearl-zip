/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.archive.pub.profile.component;

import java.util.Collections;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

/**
 *  Entity component containing basic information about the plugin.
 *
 *  @author Aashutos Kakshepati
 */
public class GeneralComponent {
    private final Set<String> aliasFormats;
    private final Set<String> compressorArchives;
    private final ResourceBundle resourceBundle;

    public GeneralComponent(Set<String> aliasFormats, Set<String> compressorArchives, ResourceBundle resourceBundle) {
        this.aliasFormats = Collections.unmodifiableSet(aliasFormats);
        this.compressorArchives = Collections.unmodifiableSet(compressorArchives);
        this.resourceBundle = resourceBundle;
    }

    public boolean isEnabled() {
        return Boolean.parseBoolean(System.getProperty(
                String.format("configuration.ntak.pearl-zip.provider.priority.enabled.%s",
                              getClass().getCanonicalName()
                ),
                "true")
        );
    }

    public Set<String> getAliasFormats() {
        return aliasFormats;
    }

    public Set<String> getCompressorArchives() {
        return compressorArchives;
    }

    public Optional<ResourceBundle> getResourceBundle() { return Optional.ofNullable(resourceBundle); }
}
