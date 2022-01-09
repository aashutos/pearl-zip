/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.archive.pub;

import com.ntak.pearlzip.archive.model.LicenseInfo;

import java.util.Map;

/**
 *  Generic service, which provides functionality to derive and supply dependency license information.
 *  @author Aashutos Kakshepati
 */
public interface LicenseService {
    Map<String,LicenseInfo> retrieveDeclaredLicenses();
}
