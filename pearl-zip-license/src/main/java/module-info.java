import com.ntak.pearlzip.archive.pub.LicenseService;
import com.ntak.pearlzip.license.pub.PearlZipLicenseService;

/*
 * Copyright © 2022 92AK
 */
/**
 *  Provides license information of core dependencies of the Pearl Zip project.
 */
module com.ntak.pearlzip.license {
    exports com.ntak.pearlzip.license.pub;

    provides LicenseService with PearlZipLicenseService;

    requires com.ntak.pearlzip.archive;

    // Logging
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;

    // XML Parsing
    requires java.xml;
}