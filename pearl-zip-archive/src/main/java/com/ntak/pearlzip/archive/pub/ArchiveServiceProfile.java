/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.archive.pub;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  Archive Service Profile descriptor for the plugin.
 *  Utilises Entity Component system pattern to provide extensible information.
 *
 *  @author Aashutos Kakshepati
 */
public class ArchiveServiceProfile {

    private final String identifier;
    private final ConcurrentHashMap<Class<? extends ArchiveServiceProfileComponent>, ArchiveServiceProfileComponent> COMPONENTS = new ConcurrentHashMap<>();

    public ArchiveServiceProfile(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    /**
     *   Adds a component to the Archive Service Profile entity.
     *
     *   @param component Component to be added
     *   @param <T> The type of component to be added (Inherits ArchiveServiceProfileComponent)
     *
     *   @return ArchiveServiceProfileComponent if component of type already exists, or else null if parameter passed in was ingested successfully.
     */
    public <T extends ArchiveServiceProfileComponent> T addComponent(T component) {
        if (COMPONENTS.containsKey(component.getClass())) {
            return (T)COMPONENTS.get(component.getClass());
        } else {
            COMPONENTS.put(component.getClass(), component);
            return null;
        }
    }

    public <T extends ArchiveServiceProfileComponent> Optional<T> getComponent(Class<T> klass) {
        try {
            return Optional.ofNullable((T)COMPONENTS.get(klass));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
