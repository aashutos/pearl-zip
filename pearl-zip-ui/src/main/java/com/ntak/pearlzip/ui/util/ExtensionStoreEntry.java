/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.util;

import com.ntak.pearlzip.archive.model.PluginInfo;
import com.ntak.pearlzip.ui.constants.InstallState;
import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;

import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

import static com.ntak.pearlzip.ui.constants.ResourceConstants.COLONSV;
import static com.ntak.pearlzip.ui.constants.ZipConstants.CK_PLUGINS_METADATA;

public record ExtensionStoreEntry(int id, String packageName, String packageURL, String packageHash, String description, String minVersion, String maxVersion, String typeName, String providerName, String providerDescription) {
    public String installState() {
        String packageName = COLONSV.split(packageName())[0];
        String version = COLONSV.split(packageName())[1];

        PluginInfo info = InternalContextCache.INTERNAL_CONFIGURATION_CACHE.<ConcurrentMap<String,PluginInfo>>getAdditionalConfig(CK_PLUGINS_METADATA).get().get(packageName);

        if (Objects.nonNull(info) && packageName.equals(info.getName())) {
            if (VersionComparator.getInstance().compare(version, info.getMinVersion()) > 0) {
                return InstallState.UPDATABLE.getValue();
            }
            return InstallState.INCOMPATIBLE.getValue();
        }

        return InstallState.INSTALLABLE.getValue();
    }

    @Override
    public String toString() {
        return "ExtensionStoreEntry{" +
                "id=" + id +
                ", lowerBound='" + packageName + '\'' +
                ", packageURL='" + packageURL + '\'' +
                ", packageHash='" + packageHash + '\'' +
                ", description='" + description + '\'' +
                ", minVersion='" + minVersion + '\'' +
                ", maxVersion='" + maxVersion + '\'' +
                ", typeName='" + typeName + '\'' +
                ", providerName='" + providerName + '\'' +
                ", providerDescription='" + providerDescription + '\'' +
                '}';
    }
}
