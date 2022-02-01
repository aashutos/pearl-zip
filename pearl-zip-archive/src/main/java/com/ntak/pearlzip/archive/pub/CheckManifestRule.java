/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.archive.pub;

import com.ntak.pearlzip.archive.model.PluginInfo;

import java.nio.file.Path;

/**
 *  A rule as part of a chain that validates a PZAX plugin manifest.
 *
 *  @author Aashutos Kakshepati
 */
public interface CheckManifestRule {
    String getKey();
    void processManifest(PluginInfo info, Path targetDir) throws Exception;
}
