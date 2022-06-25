/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.archive.pub.profile.component;

import com.ntak.pearlzip.archive.pub.ArchiveService;
import com.ntak.pearlzip.archive.pub.ArchiveServiceProfileComponent;

import java.util.Collections;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

/**
 *  Entity component containing basic information about the plugin.
 *
 *  @author Aashutos Kakshepati
 */
public class GeneralComponent implements ArchiveServiceProfileComponent {
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

    /**
     *   Declares a set of file extensions, which are alias of core formats. This list of formats will not be used in
     *   the creation of archives. It is anticipated that this field will contain shortened convenience extensions in
     *   which long explicit extensions would be preferable (e.g. tar.gz would be preferred to tgz). The shortened
     *   format can still be read and modified subject to the underlying {@link ArchiveService} implementation.
     *
     *   @return Set&lt;String&gt; - Set of alias file extensions
     */
    public Set<String> getAliasFormats() {
        return aliasFormats;
    }

    public Set<String> getCompressorArchives() {
        return compressorArchives;
    }

    public Optional<ResourceBundle> getResourceBundle() { return Optional.ofNullable(resourceBundle); }
}
