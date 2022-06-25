/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.archive.pub.profile.component;

import com.ntak.pearlzip.archive.pub.ArchiveServiceProfileComponent;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 *  Root Entity Component providing information about service functionality.
 */
public class ServiceFunctionalityComponent implements ArchiveServiceProfileComponent {
    private final Set<String> supportedFormats;
    private Map<String,String> functionalitySpecs;

    public ServiceFunctionalityComponent(Set<String> supportedFormats, Map<String,String> functionalitySpecs) {
        this.supportedFormats = Collections.unmodifiableSet(supportedFormats);
        this.functionalitySpecs = Collections.unmodifiableMap(functionalitySpecs);
    }

    /**
     *   Lists out explicitly all archive formats that can be used by the implementation of the archive service.
     *
     *   @return List&lt;String&gt; - List of archive extensions that can be created and modified by the archive
     *   service implementation
     */
    public Set<String> getSupportedFormats() {
        return supportedFormats;
    }

    public Set<String> getFunctionalityKeys() {
        return Collections.unmodifiableSet(functionalitySpecs.keySet());
    }

    public String getFunctionalitySpec(String key) {
        if (Objects.nonNull(key)) {
            return functionalitySpecs.get(key);
        }

        return null;
    }
}
