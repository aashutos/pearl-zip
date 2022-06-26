/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.archive.pub;

import javafx.scene.Node;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.CNS_COM_BUS_FACTORY;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.LOG_ARCHIVE_SERVICE_COM_BUS_INIT_ERROR;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.ROOT_LOGGER;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;

/**
 *  Interface defining common functionality associated with an archive extracting/compression implementation.
 *  @author Aashutos Kakshepati
 */
public interface ArchiveService {

    String OPTIONS = "pearlzip.pane.options";

    CommunicationBus DEFAULT_BUS = initializeBus();
    String CUSTOM_MENUS = "pearlzip.menu.custom-menus";

    /**
     *   This method decouples the implementation of the internal communication bus from PearlZip by utilising the
     *   CommunicationBus façade. The default implementation used and, which is bundled with the UI package is
     *   EventBus. The implementation can be configured by specifying the canonical name of the implementation factory
     *   with the key:
     *   <br/><br/>
     *   {@code configuration.ntak.com-bus-factory=com.ntak.pearlzip.ui.util.EventBusFactory}
     *
     *   @return CommunicationBus - The implementation of a CommunicationBus for internal communication within PearlZip
     */
    static CommunicationBus initializeBus() {
        try {
            CommunicationBusFactory factory = (CommunicationBusFactory) Class.forName(
                                                System.getProperty(CNS_COM_BUS_FACTORY,
                                                                  "com.ntak.pearlzip.ui.util.EventBusFactory")
            )
            .getDeclaredConstructor()
            .newInstance();

            return factory.initializeCommunicationBus();
        } catch (Exception e) {
            // LOG: Exception raised on initialisation of Communication Bus. A critical issue has occurred, Pearl Zip
            // will now close.\n
            // Exception Type: %s\n
            // Exception message: %s\n
            // Stack trace:\n%s
            ROOT_LOGGER.error(resolveTextKey(LOG_ARCHIVE_SERVICE_COM_BUS_INIT_ERROR,
                                             e.getClass().getCanonicalName(), e.getMessage(), e.getStackTrace()));
            throw new ExceptionInInitializerError(resolveTextKey(LOG_ARCHIVE_SERVICE_COM_BUS_INIT_ERROR,
                                                                 e.getClass().getCanonicalName(), e.getMessage(), e.getStackTrace()));
        }
    }

    /**
     *  Generated the minimal default ArchiveInfo for a given archive path. It sets the path and the archive format
     *  as well as the maximum compression level.
     *
     *  @param archivePath
     *  @return ArchiveInfo - Minimal configuration for an archive in PearlZip
     */
    static ArchiveInfo generateDefaultArchiveInfo(String archivePath) {
        ArchiveInfo archiveInfo = new ArchiveInfo();

        archiveInfo.setArchivePath(archivePath);
        archiveInfo.setArchiveFormat(archivePath.substring(archivePath.lastIndexOf(".") + 1));
        archiveInfo.setCompressionLevel(9);

        return archiveInfo;
    }

    /**
     *   Specifies whether an Archive Service is enabled for use by PearlZip. By default, every achive service
     *   implementation is enabled. The Archive Implementation can be disabled by adding a property to the bootstrap
     *   configuration:
     *   <br/><br/>
     *   {@code configuration.ntak.pearl-zip.provider.priority.enabled.[Canonical name of Archive
     *   Service
     *   implementation]=false}
     *
     *   @return boolean - returns true if the implementation is enabled and to be used by PearlZip
     */
    @Deprecated(forRemoval = true)
    default boolean isEnabled() {
        return Boolean.parseBoolean(System.getProperty(
                String.format("configuration.ntak.pearl-zip.provider.priority.enabled.%s",
                               getClass().getCanonicalName()
                ),
                "true")
        );
    }

    ArchiveServiceProfile getArchiveServiceProfile();

    default Optional<FXForm> getFXFormByIdentifier(String name, Object... parameters) { return Optional.empty();}

    class FXForm {
        private final String name;
        private final Node content;
        private final Map<String,Object> configuration;

        public FXForm(String name, Node content, Map<String,Object> configuration) {
            this.name = name;
            this.content = content;
            this.configuration = configuration;
        }

        public String getName() {
            return name;
        }

        public Node getContent() {
            return content;
        }

        public Map<String,Object> getConfiguration() {
            return Collections.unmodifiableMap(configuration);
        }
    }
}
