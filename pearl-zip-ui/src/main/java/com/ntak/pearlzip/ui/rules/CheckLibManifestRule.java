/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.rules;

import com.ntak.pearlzip.archive.model.PluginInfo;
import com.ntak.pearlzip.archive.pub.CheckManifestRule;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.LOG_HASH_INTEGRITY_FAILURE;
import static com.ntak.pearlzip.ui.constants.ZipConstants.LOG_LIB_FILE_MANIFEST_ENTRY_CORRUPT;

/**
 *  Manifest check, which ensures that the integrity of jars according to accompanying hash file.
 *
 *  @author Aashutos Kakshepati
 */
public class CheckLibManifestRule implements CheckManifestRule {
    @Override
    public String getKey() {
        return "lib-file";
    }

    @Override
    public void processManifest(PluginInfo info, Path targetDir) throws Exception {
        Set<String> dependencies = info.getDependencies();
        List<String> hashFormats = info.getHashFormats();
        Iterator<String> it = dependencies.iterator();

        if (dependencies.size() != hashFormats.size()) {
            // LOG: Manifest structure is invalid or corrupt. Please check lib-file entries.
            throw new Exception(resolveTextKey(LOG_LIB_FILE_MANIFEST_ENTRY_CORRUPT));
        }

        for (int i = 0; i < dependencies.size(); i++) {
            final String lib = it.next();
            Path libFile = Paths.get(targetDir.toAbsolutePath()
                                              .toString(), lib
                                     );
            String hashFormat = hashFormats.get(i);

            // Check hash, if required
            if (!hashFormat.equals("N/A")) {
                Path digestFile = Paths.get(targetDir.toAbsolutePath()
                                                     .toString(),
                                            lib.replace(".jar",
                                                              String.format(".%s",
                                                                            hashFormat)));
                MessageDigest digest = MessageDigest.getInstance(hashFormat);
                String calculatedHash = HexFormat.of()
                                                 .formatHex(digest.digest(Files.readAllBytes(libFile)));
                String referenceHash = Files.readString(digestFile);
                if (!calculatedHash.equals(referenceHash.trim())) {
                    // LOG: Calculated hash (%s) does not match the expected
                    // reference (%s)
                    // value. Integrity check failed for library: %s.
                    throw new Exception(resolveTextKey(LOG_HASH_INTEGRITY_FAILURE,
                                                       calculatedHash, referenceHash,
                                                       libFile));
                }
            }
        }
    }
}
