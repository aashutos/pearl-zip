/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.archive.szjb.constants;

import com.ntak.pearlzip.archive.pub.ArchiveServiceProfile;
import com.ntak.pearlzip.archive.pub.profile.component.GeneralComponent;
import com.ntak.pearlzip.archive.pub.profile.component.ReadServiceComponent;

import java.util.Collections;
import java.util.Set;

public class SevenZipConstants {
    public static final ArchiveServiceProfile PROFILE = new ArchiveServiceProfile("pearl-zip-archive-szjb");

    static {
        PROFILE.addComponent(new GeneralComponent(Set.of("tgz"), Set.of("gz", "xz", "bz2", "tgz"), null));
        PROFILE.addComponent(new ReadServiceComponent(Set.of("zip", "gz", "bz2", "xz", "7z", "jar", "rar", "iso", "cab", "tgz"), Collections.emptyMap()));
    }
}
