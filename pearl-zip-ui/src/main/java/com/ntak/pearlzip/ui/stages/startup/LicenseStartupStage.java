/*
 * Copyright © 2023 92AK
 */
package com.ntak.pearlzip.ui.stages.startup;

import com.ntak.pearlzip.archive.pub.LicenseService;
import com.ntak.pearlzip.ui.model.ZipState;
import com.ntak.pearlzip.ui.util.AbstractStartupStage;

import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.CNS_NTAK_PEARL_ZIP_LICENSE_SERVICE_CANONICAL_NAME;

/**
 *  Declares third-party open source license information that have been used by this application.
 *
 *  @author Aashutos Kakshepati
 */
public class LicenseStartupStage extends AbstractStartupStage {
    @Override
    public void executeProcess() throws Exception {
        // Load License Declarations
        try {
            LicenseService licenseService = (LicenseService) Class.forName(System.getProperty(
                                                                          CNS_NTAK_PEARL_ZIP_LICENSE_SERVICE_CANONICAL_NAME,
                                                                          "com.ntak.pearlzip.license.pub.PearlZipLicenseService"))
                                                                  .getDeclaredConstructor()
                                                                  .newInstance();
            licenseService.retrieveDeclaredLicenses()
                          .forEach(ZipState::addLicenseDeclaration);
        } catch (Exception e) {

        }
    }
}
