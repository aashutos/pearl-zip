/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.license.pub;

import com.ntak.pearlzip.archive.model.LicenseInfo;
import com.ntak.pearlzip.archive.pub.LicenseService;
import com.ntak.pearlzip.license.constants.LicenseConstants;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import java.io.InputStream;
import java.util.*;

import static com.ntak.pearlzip.archive.util.LoggingUtil.getStackTraceFromException;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.license.constants.LicenseConstants.*;

/**
 *  Default implementation of the License Service that draws up information taken from Maven repositories.
 *  @author Aashutos Kakshepati
 */
public class PearlZipLicenseService implements LicenseService {

    private static final Logger LOGGER = LoggerContext.getContext().getLogger(PearlZipLicenseService.class);

    @Override
    public Map<String,LicenseInfo> retrieveDeclaredLicenses() {
        Map<String,LicenseInfo> licMap = new HashMap<>();

        try(InputStream licenseFile =
                    PearlZipLicenseService.class
                            .getClassLoader()
                            .getResourceAsStream(System.getProperty(LicenseConstants.CNS_LICENSE_LOCATION,"LICENSE.xml"));
            InputStream licenseOverrideFile =
                    PearlZipLicenseService.class
                            .getClassLoader()
                            .getResourceAsStream(System.getProperty(CNS_LICENSE_OVERRIDE_LOCATION, "LICENSE-OVERRIDE.xml"))) {

            // Parse Maven generated license file
            DocumentBuilder documentBuilder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
            Document document = documentBuilder.parse(licenseFile);
            document.getDocumentElement().normalize();

            List<LicenseInfo> licenseInfoList = extractLicenseInfo(document);
            licenseInfoList.forEach(l->licMap.put(String.format("%s:%s", l.canonicalName(), l.licenseType()),l));

            // Parse Manual override license file
            Document overrideDocument = documentBuilder.parse(licenseOverrideFile);
            List<LicenseInfo> licenseOverrideInfoList = extractLicenseInfo(overrideDocument);
            licenseOverrideInfoList.forEach(l->licMap.put(String.format("%s:%s", l.canonicalName(), l.licenseType()),l));
        } catch(Exception e) {
            // LOG: Issue parsing license files. Exception type: %s\nMessage: %s\nStack trace:%s
            LOGGER.warn(resolveTextKey(LOG_ISSUE_PARSE_LICENSE_FILE, e.getClass().getCanonicalName(), e.getMessage(),
                                       getStackTraceFromException(e)));
        }

        return licMap;
    }

    private List<LicenseInfo>  extractLicenseInfo(Document document) {
        List<LicenseInfo> licenseInfoList = new ArrayList<>();
        
        NodeList dependencies = document.getElementsByTagName(TAG_DEPENDENCY);
        for (int i = 0; i < dependencies.getLength(); i++) {
            Node dependency = dependencies.item(i);
            if (dependency.getNodeType() == Node.ELEMENT_NODE) {
                try {
                    Element eElement = (Element) dependency;
                    
                    String uniqueId = String.format("%s.%s", eElement.getElementsByTagName(TAG_GROUP_ID)
                                                                     .item(0)
                                                                     .getTextContent(),
                                                            eElement.getElementsByTagName(TAG_ARTIFACT_ID)
                                                                    .item(0)
                                                                    .getTextContent()
                    );
                    String version = eElement.getElementsByTagName(TAG_VERSION)
                                             .item(0)
                                             .getTextContent();
                    NodeList licenses = eElement.getElementsByTagName(TAG_LICENSE);

                    String licenseName = null;
                    String licensePath = null;
                    String url = null;
                    if (Objects.nonNull(licenses)) {
                        for (int j = 0; j < licenses.getLength(); j++) {
                            Node license = licenses.item(j);
                            if (Objects.nonNull(license) && license.getNodeType() == Node.ELEMENT_NODE) {
                                Element lElement = (Element) license;
                                licenseName = lElement.getElementsByTagName(TAG_NAME)
                                                      .item(0)
                                                      .getTextContent();
                                final Node urlNode = lElement.getElementsByTagName(TAG_URL)
                                                      .item(0);
                                if (Objects.nonNull(urlNode)) {
                                    url = urlNode.getTextContent();
                                }
                                final Node file = lElement.getElementsByTagName(TAG_FILE)
                                                          .item(0);

                                if (Objects.nonNull(file)) {
                                    licensePath = file
                                            .getTextContent();

                                }
                            }
                            LicenseInfo info = new LicenseInfo(uniqueId, version, licenseName, licensePath, url);
                            licenseInfoList.add(info);
                            // LOG: \nAdded license declaration. Details:\nUnique Id: %s\nVersion: %s\nLicense Name:
                            // %s\nLicense File: %s\n
                            LOGGER.info(resolveTextKey(LOG_ADDED_LICENSE_DECLARATION, uniqueId, version, licenseName, licensePath));
                        }
                    } else {
                        LicenseInfo info = new LicenseInfo(uniqueId, version, licenseName, licensePath, url);
                        licenseInfoList.add(info);
                        // LOG: \nAdded license declaration. Details:\nUnique Id: %s\nVersion: %s\nLicense Name:
                        // %s\nLicense File: %s\n
                        LOGGER.info(resolveTextKey(LOG_ADDED_LICENSE_DECLARATION, uniqueId, version, licenseName, licensePath));
                    }
                } catch (Exception e) {
                    // LOG: Skipping generation of dependency: %s. Exception type: %s; message: %s
                    LOGGER.warn(resolveTextKey(LOG_SKIPPING_DEPENDENCY_LICENSE_RETRIEVAL,
                                               dependency.getNodeName(),
                                               e.getClass().getCanonicalName(),
                                               e.getMessage()));
                }
                
            }
        }
        return licenseInfoList;
    }
}
