/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.archive.acc.constants;

import com.ntak.pearlzip.archive.pub.ArchiveServiceProfile;
import com.ntak.pearlzip.archive.pub.profile.component.GeneralComponent;
import com.ntak.pearlzip.archive.pub.profile.component.ReadServiceComponent;
import com.ntak.pearlzip.archive.pub.profile.component.WriteServiceComponent;

import java.util.Collections;
import java.util.Set;

public class CommonsCompressConstants {
    public static final ArchiveServiceProfile PROFILE = new ArchiveServiceProfile("pearl-zip-archive-acc");

    static {
        PROFILE.addComponent(new GeneralComponent(Set.of("tgz"), Set.of("gz", "xz", "bz2", "lz", "lz4", "lzma", "z", "sz"), null));
        PROFILE.addComponent(new WriteServiceComponent(Set.of("zip", "jar", "gz", "xz", "bz2", "tar", "tgz"), Collections.emptyMap()));
        PROFILE.addComponent(new ReadServiceComponent(Set.of("tar"), Collections.emptyMap()));
    }
}
