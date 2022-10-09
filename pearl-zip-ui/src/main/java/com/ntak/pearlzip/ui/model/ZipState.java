/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.model;

import com.ntak.pearlzip.archive.model.LicenseInfo;
import com.ntak.pearlzip.archive.pub.ArchiveReadService;
import com.ntak.pearlzip.archive.pub.ArchiveService;
import com.ntak.pearlzip.archive.pub.ArchiveWriteService;
import com.ntak.pearlzip.archive.pub.profile.component.GeneralComponent;
import com.ntak.pearlzip.archive.pub.profile.component.ReadServiceComponent;
import com.ntak.pearlzip.archive.pub.profile.component.WriteServiceComponent;
import javafx.scene.control.ContextMenu;
import org.apache.logging.log4j.util.Strings;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.ui.constants.ZipConstants.*;

/**
 *  Core constant resources, which is used to set up the Pearl Zip environment.
 *  @author Aashutos Kakshepati
*/
public class ZipState {

    public static final int WIDTH = Integer.parseInt(System.getProperty(CNS_WINDOW_WIDTH, "816"));
    public static final int HEIGHT = Integer.parseInt(System.getProperty(CNS_WINDOW_HEIGHT, "480"));
    public static final long LOCK_POLL_TIMEOUT = Integer.parseInt(System.getProperty(CNS_CONCURRENCY_LOCK_POLL_TIMEOUT,
                                                                               "100"));
    public static final AtomicBoolean ROW_TRIGGER = new AtomicBoolean(false);
    public static final CopyOnWriteArrayList<ContextMenu> CONTEXT_MENU_INSTANCES = new CopyOnWriteArrayList<>();

    private static final List<ArchiveWriteService> WRITE_PROVIDERS = new LinkedList<>();
    private static final List<ArchiveReadService> READ_PROVIDERS = new LinkedList<>();
    private static final Map<String,ArchiveWriteService> ARCHIVE_WRITE_MAP = new ConcurrentHashMap<>();
    private static final Map<String,ArchiveReadService> ARCHIVE_READ_MAP = new ConcurrentHashMap<>();
    private static final Set<String> COMPRESSOR_ARCHIVE_FORMATS = new HashSet<>();
    private static final Map<String,LicenseInfo> LICENSE_DECLARATION_MAP = new HashMap<>();

    public static Optional<ArchiveReadService> getReadArchiveServiceForFile(String filename) {
        try {
            String extension = filename.substring(filename.lastIndexOf(".")+1).toLowerCase();
            return  Optional.ofNullable(ARCHIVE_READ_MAP.get(extension));
        } catch(Exception e) {
            return Optional.empty();
        }
    }

    public static Optional<ArchiveWriteService> getWriteArchiveServiceForFile(String filename) {
        try {
            String extension = filename.substring(filename.lastIndexOf(".")+1).toLowerCase();
            return  Optional.ofNullable(ARCHIVE_WRITE_MAP.get(extension));
        } catch(Exception e) {
            return Optional.empty();
        }
    }

    public static void addArchiveProvider(ArchiveService service) {
        COMPRESSOR_ARCHIVE_FORMATS.addAll(service.getArchiveServiceProfile()
                                                 .getComponent(GeneralComponent.class)
                                                 .orElse(new GeneralComponent(Set.of("tgz"), Set.of("gz", "xz", "bz2", "lz", "lz4", "lzma", "z", "sz"), null))
                                                 .getCompressorArchives()
                                                 .stream()
                                                 .filter(Strings::isNotBlank)
                                                 .collect(Collectors.toSet())
        );
        if (service instanceof  ArchiveWriteService writeService) {
            addArchiveToMap(ARCHIVE_WRITE_MAP, writeService.getArchiveServiceProfile()
                                                           .getComponent(WriteServiceComponent.class)
                                                           .orElse(new WriteServiceComponent(Collections.emptySet(), Collections.emptyMap()))
                                                           .getSupportedFormats()
                                                           .stream()
                                                           .filter(Strings::isNotBlank)
                                                           .collect(Collectors.toList()),
                            (ArchiveWriteService) service);

            WRITE_PROVIDERS.removeIf(s -> s.getClass()
                                           .getCanonicalName()
                                           .equals(service.getClass()
                                                          .getCanonicalName()));
            WRITE_PROVIDERS.add((ArchiveWriteService) service);
        }
        if (service instanceof ArchiveReadService readService) {
            addArchiveToMap(ARCHIVE_READ_MAP, readService.getArchiveServiceProfile()
                                                         .getComponent(ReadServiceComponent.class)
                                                         .orElse(new ReadServiceComponent(Collections.emptySet(), Collections.emptyMap()))
                                                         .getSupportedFormats()
                                                         .stream()
                                                         .filter(Strings::isNotBlank)
                                                         .collect(Collectors.toList()),
                            (ArchiveReadService) service);

            READ_PROVIDERS.removeIf(s -> s.getClass()
                                          .getCanonicalName()
                                          .equals(service.getClass()
                                                         .getCanonicalName()));
            READ_PROVIDERS.add((ArchiveReadService) service);
        }
    }

    private static <T extends ArchiveService> void addArchiveToMap(Map<String,T> cache,
            List<String> supportedFormats,
            T service) {
        if (service != null) {
            for (String format: supportedFormats) {
                ArchiveService currentService;
                if ((currentService = cache.putIfAbsent(format, service)) != null) {
                    String currentProvKey = String.format(CNS_PROVIDER_PRIORITY_ROOT_KEY,
                                                          currentService.getClass().getCanonicalName());
                    String newProvKey = String.format(CNS_PROVIDER_PRIORITY_ROOT_KEY,
                                                      service.getClass().getCanonicalName());
                    String currPriority = System.getProperty(currentProvKey, "1");
                    String newPriority = System.getProperty(newProvKey, "0");

                    try {
                        if (Integer.valueOf(newPriority)
                                   .compareTo(Integer.valueOf(currPriority)) > 0) {
                            cache.put(format, service);
                        }
                    } catch (NumberFormatException|NullPointerException e) {

                    }
                }
            }
        }
    }

    public static Set<String> supportedReadArchives() {
        return ARCHIVE_READ_MAP.keySet();
    }

    public static Set<String> supportedWriteArchives() {
        return ARCHIVE_WRITE_MAP.keySet();
    }

    public static Set<String> getCompressorArchives() {
        return Collections.unmodifiableSet(COMPRESSOR_ARCHIVE_FORMATS);
    }

    public static List<ArchiveWriteService> getWriteProviders() {
        return Collections.unmodifiableList(WRITE_PROVIDERS);
    }

    public static List<ArchiveReadService> getReadProviders() {
        return Collections.unmodifiableList(READ_PROVIDERS);
    }

    public static void addLicenseDeclaration(String uniqueId, LicenseInfo licenseDeclaration) {
        if (Objects.nonNull(uniqueId) & Objects.nonNull(licenseDeclaration)) {
            LICENSE_DECLARATION_MAP.put(uniqueId, licenseDeclaration);
        }
    }

    public static List<LicenseInfo> getLicenseDeclarations() {
        return new ArrayList<>(LICENSE_DECLARATION_MAP.values());
    }

    public static Optional<LicenseInfo> getLicenseDeclaration(String uniqueId) {
        return Optional.ofNullable(LICENSE_DECLARATION_MAP.get(uniqueId));
    }

    public static HashSet<String> getSupportedCompressorWriteFormats() {
        HashSet<String> supportedWriteFormats = getCompressorArchives()
                .stream()
                .filter(f -> supportedWriteArchives().contains(f))
                .collect(Collectors.toCollection(HashSet::new));

        for (ArchiveService service : getWriteProviders()) {
            supportedWriteFormats.removeAll(service.getArchiveServiceProfile()
                                                   .getComponent(GeneralComponent.class)
                                                   .orElse(new GeneralComponent(Collections.emptySet(), Collections.emptySet(), null))
                                                   .getAliasFormats());
        }

        return supportedWriteFormats;
    }

    public static HashSet<String> getRawSupportedCompressorWriteFormats() {
        return getCompressorArchives().stream()
                                      .filter(f -> supportedWriteArchives().contains(f))
                                      .collect(Collectors.toCollection(HashSet::new));
    }
}
