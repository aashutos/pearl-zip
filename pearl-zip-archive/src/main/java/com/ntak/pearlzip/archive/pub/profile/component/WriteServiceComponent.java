/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.archive.pub.profile.component;

import java.util.Map;
import java.util.Set;

/**
 *  Entity component containing information about write functionalities (inherits from {@link ServiceFunctionalityComponent})
 *
 *  @author Aashutos Kakshepati
 */
public class WriteServiceComponent extends ServiceFunctionalityComponent {

    public WriteServiceComponent(Set<String> supportedFormats, Map<String,String> functionalitySpecs) {
        super(supportedFormats, functionalitySpecs);
    }
}
