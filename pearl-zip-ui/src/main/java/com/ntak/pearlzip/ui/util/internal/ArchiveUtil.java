/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.util.internal;

import com.ntak.pearlzip.archive.pub.ArchiveInfo;
import com.ntak.pearlzip.archive.pub.ArchiveReadService;
import com.ntak.pearlzip.archive.pub.ArchiveService;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Pair;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.archive.constants.ArchiveConstants.WORKING_APPLICATION_SETTINGS;
import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.KEY_FILE_PATH;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ResourceConstants.NO_FILES_HISTORY;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.JFXUtil.raiseAlert;

/**
 *  Useful utility methods used by the UI to trigger and manage archiving processes.
 *  @author Aashutos Kakshepati
*/
public class ArchiveUtil {

    private static final Logger LOGGER = LoggerContext.getContext().getLogger(ArchiveUtil.class);

    public static List<FileInfo> handleDirectory(String prefix, Path root, Path directory, int depth, int index) throws IOException {
        List<FileInfo> files = new ArrayList<>();
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
            for (Path path : dirStream) {
                if (Files.isDirectory(path)) {
                    final List<FileInfo> subDirFiles = handleDirectory(prefix, root, path, (depth + 1), index);
                    files.addAll(subDirFiles);
                    index += subDirFiles.size();
                    if (files.stream()
                             .noneMatch(f -> f.getFileName()
                                              .equals(root.relativize(path)
                                                          .toString()))) {
                        files.add(new FileInfo(index++, depth,
                                               Paths.get(prefix,
                                                         root.relativize(path)
                                                             .toString())
                                                    .toString(), -1, 0
                                , 0, null,
                                               null, null, "", "", 0, "", true, false,
                                               Collections.singletonMap(KEY_FILE_PATH, path.toString())));
                    }
                    continue;
                }
                files.add(new FileInfo(index++, depth,
                                       Paths.get(prefix,
                                                 root.relativize(path)
                                                     .toString())
                                            .toString(),
                                       -1, 0,
                                       0, null,
                                       null, null,
                                       "", "", 0, "",
                                       false, false,
                                       Collections.singletonMap(KEY_FILE_PATH, path.toString())));
            }
        }
        return files;
    }

    public static void addToRecentFile(File file) {
        Path RECENT_FILE =
                InternalContextCache.GLOBAL_CONFIGURATION_CACHE.<Path>getAdditionalConfig(CK_RECENT_FILE).get();
        final int size = Math.min(NO_FILES_HISTORY, 15);
        String[] files = new String[size];
        try {
            List<String> currentHistory = Files.lines(RECENT_FILE)
                                               .sequential()
                                               .filter(f->!f.equals(file.getAbsolutePath()))
                                               .filter(f->Files.exists(Paths.get(f)))
                                               .limit(size-1)
                                               .collect(Collectors.toList());
            for (int i = 0; i < currentHistory.size(); i++) {
                files[i+1] = currentHistory.get(i);
            }
        } catch(IOException e) {
        }
        files[0] = file.getAbsolutePath();
        try (PrintWriter writer = new PrintWriter(Files.newOutputStream(RECENT_FILE), true)
        ) {
            for (String line : files) {
                if (Objects.nonNull(line)) {
                    writer.println(line);
                }
            }

            synchronized(InternalContextCache.INTERNAL_CONFIGURATION_CACHE
                                             .getAdditionalConfig(CK_RECENT_FILES_MENU)
                                             .get()
            ) {
                com.ntak.pearlzip.ui.util.JFXUtil.runLater(()->refreshRecentFileMenu(InternalContextCache.INTERNAL_CONFIGURATION_CACHE
                                                                   .<Menu>getAdditionalConfig(CK_RECENT_FILES_MENU)
                                                                   .get()));
            }
        } catch(IOException e) {
        }
    }

    public static void refreshRecentFileMenu(Menu mnuOpenRecent) {
        Path RECENT_FILE =
                InternalContextCache.GLOBAL_CONFIGURATION_CACHE.<Path>getAdditionalConfig(CK_RECENT_FILE).get();
        Stage stage = (Stage)Stage.getWindows().stream().filter(Window::isFocused).findFirst().orElse(new Stage());
        mnuOpenRecent.getItems().clear();
        try (Scanner scanner = new Scanner(Files.newInputStream(RECENT_FILE))) {
            int i = 1;
            while (scanner.hasNext()) {
                String filePath = scanner.nextLine();
                if (Files.exists(Path.of(filePath))) {
                    MenuItem mnuFilePath = new MenuItem();
                    mnuFilePath.setText(String.format("%d. %s", i++, filePath));
                    mnuFilePath.setOnAction((e) -> {
                        final Path path = Paths.get(filePath);
                        if (Files.exists(path)) {
                            com.ntak.pearlzip.ui.util.ArchiveUtil.openFile(path.toFile());
                        } else {
                            // TITLE: Warning: File does not exist
                            // HEADER: The selected file does not exist
                            // BODY: The chosen file %s does not exist. It will be removed from the list.
                            raiseAlert(Alert.AlertType.WARNING,
                                       resolveTextKey(TITLE_FILE_NOT_EXIST),
                                       resolveTextKey(HEADER_FILE_NOT_EXIST),
                                       resolveTextKey(BODY_FILE_NOT_EXIST, path.toAbsolutePath().toString()),
                                       stage
                            );
                            mnuOpenRecent.hide();
                        }
                    });
                    mnuOpenRecent.getItems()
                                 .add(mnuFilePath);
                }
            }
        } catch(IOException e) {
        }
    }

    public static void checkPreOpenDialog(ArchiveReadService readService, ArchiveInfo archiveInfo) throws InterruptedException, IOException {
        Optional<ArchiveService.FXForm> optFXForm;
        if ((optFXForm = readService.getFXFormByIdentifier(ArchiveReadService.OPEN_ARCHIVE_OPTIONS,
                                                           archiveInfo)).isPresent()) {
            Node root = optFXForm.get().getContent();

            CountDownLatch latch = new CountDownLatch(1);
            com.ntak.pearlzip.ui.util.JFXUtil.runLater(() -> {
                Stage preOpenStage = new Stage();
                com.ntak.pearlzip.ui.util.internal.JFXUtil.loadPreOpenDialog(preOpenStage, root);
                latch.countDown();
            });
            latch.await();
            Pair<AtomicBoolean,String> result = (Pair<AtomicBoolean, String>) root.getUserData();

            if (Objects.nonNull(result) && Objects.nonNull(result.getKey()) && !result.getKey().get()) {
                // LOG: Issue occurred when opening archive %s. Issue reason: %s
                // TITLE: Invalid Archive Setup
                // HEADER: Archive could not be open
                // BODY: There were issues opening the archive. Reason given by plugin for issue: %s. Check logs
                // for further details.
                LOGGER.error(resolveTextKey(LOG_INVALID_ARCHIVE_SETUP, archiveInfo.getArchivePath(),
                                            result.getValue()));

                throw new IOException(resolveTextKey(LOG_INVALID_ARCHIVE_SETUP, archiveInfo.getArchivePath(),
                                                     result.getValue()));
            }
        }
    }

    public static void initialiseApplicationSettings() {
        synchronized(WORKING_APPLICATION_SETTINGS) {
            Path APPLICATION_SETTINGS_FILE =
                    InternalContextCache.GLOBAL_CONFIGURATION_CACHE
                                        .<Path>getAdditionalConfig(CK_APPLICATION_SETTINGS_FILE)
                                        .get();
            try(InputStream settingsIStream = Files.newInputStream(APPLICATION_SETTINGS_FILE)) {
                WORKING_APPLICATION_SETTINGS.clear();
                WORKING_APPLICATION_SETTINGS.load(settingsIStream);
            } catch(IOException e) {
            }
        }
    }

    public static void loadPzaxPackage(String f) {
        // TITLE: Confirmation: Load Providers Module
        // BODY: Do you wish to load PZAX Module: %s?
        ButtonType response =
                raiseAlert(Alert.AlertType.CONFIRMATION,
                           resolveTextKey(TITLE_CONFIRM_LOAD_PROVIDER_MODULE),
                           null,
                           resolveTextKey(BODY_CONFIRM_LOAD_PROVIDER_MODULE, f),
                           null,
                           null,
                           ButtonType.YES,
                           ButtonType.NO
                ).get();

        if (response.equals(ButtonType.YES)) {
            ModuleUtil.loadModuleFromExtensionPackage(Paths.get(f));
        } else {
            if (Stage.getWindows().size() == 0) {
                com.ntak.pearlzip.ui.util.JFXUtil.runLater(InternalContextCache.INTERNAL_CONFIGURATION_CACHE.<Runnable>getAdditionalConfig(CK_POST_PZAX_COMPLETION_CALLBACK).get());
            }
        }
    }

}
