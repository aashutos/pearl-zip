/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.event.handler;

import com.ntak.pearlzip.archive.pub.ArchiveInfo;
import com.ntak.pearlzip.archive.util.LoggingUtil;
import com.ntak.pearlzip.ui.model.ZipState;
import com.ntak.pearlzip.ui.util.ArchiveUtil;
import com.ntak.pearlzip.ui.util.JFXUtil;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.JFXUtil.raiseAlert;

/**
 *  Event Handler for Create Archive functionality.
 *  @author Aashutos Kakshepati
*/
public class BtnCreateEventHandler implements EventHandler<MouseEvent> {

    private static final Logger LOGGER = LoggerContext.getContext().getLogger(BtnCreateEventHandler.class);

    private final Stage stage;
    private final AtomicBoolean isRendered;
    private final ArchiveInfo archiveInfo;

    public BtnCreateEventHandler(Stage stage, AtomicBoolean isRendered, ArchiveInfo archiveInfo) {
        this.stage = stage;
        this.isRendered = isRendered;
        this.archiveInfo = archiveInfo;
    }

    @Override
    public void handle(MouseEvent event) {
        {
            try {
                Stage dlgStage = new Stage();

                FileChooser saveDialog = new FileChooser();
                // TITLE: Save archive to location...
                saveDialog.setTitle(resolveTextKey(TITLE_TARGET_ARCHIVE_LOCATION));
                saveDialog.setInitialFileName("UntitledArchive");
                File newArchive = saveDialog.showSaveDialog(dlgStage);
                if (newArchive != null) {
                    newArchive = genNewArchivePath(newArchive.getAbsolutePath(), "", archiveInfo.getArchiveFormat());

                    if (Files.exists(newArchive.toPath())) {
                        String timestamp = String.format("-%d",System.currentTimeMillis());
                        // TITLE: Warning: File selected not unique
                        // HEADER: The file name chosen already exists
                        // BODY: The filename chosen %s already exists. A unique filename will be used for the
                        //       archive: %s
                        raiseAlert(Alert.AlertType.WARNING,
                                   resolveTextKey(TITLE_FILE_NOT_UNIQUE),
                                   resolveTextKey(HEADER_FILE_NOT_UNIQUE),
                                   resolveTextKey(BODY_FILE_NOT_UNIQUE, newArchive.getAbsolutePath(),
                                                  timestamp,
                                                  archiveInfo.getArchiveFormat()),
                                   stage
                        );

                        newArchive = genNewArchivePath(newArchive.getAbsolutePath(), timestamp,
                                                       archiveInfo.getArchiveFormat());

                    }

                    File archive = newArchive;
                    long sessionId = System.currentTimeMillis();
                    JFXUtil.executeBackgroundProcess(sessionId, stage,
                                                     () -> ArchiveUtil.newArchive(sessionId, archiveInfo, archive),
                                                     (s)->{
                                                              stage.fireEvent(new WindowEvent(stage,
                                                                                             WindowEvent.WINDOW_CLOSE_REQUEST));
                                                              isRendered.getAndSet(false);
                                                          }
                    );
                }
            } catch(Exception e) {
                // LOG: Issue creating stage.\nException type: %s\nMessage:%s\nStack trace:\n%s
                LOGGER.warn(resolveTextKey(LOG_ISSUE_CREATING_STAGE, e.getClass().getCanonicalName(),
                                           e.getMessage(),
                                           LoggingUtil.getStackTraceFromException(e)));
                // TITLE: ERROR: Issue creating stage
                // HEADER: There was an issue creating the required dialog
                // BODY: Upon initiating function '%s', an issue occurred on attempting to create the dialog. This
                // function will not proceed any further.
                raiseAlert(Alert.AlertType.ERROR, resolveTextKey(TITLE_ISSUE_CREATING_STAGE),
                           resolveTextKey(HEADER_ISSUE_CREATING_STAGE),
                           resolveTextKey(BODY_ISSUE_CREATING_STAGE, this.getClass().getName()), e,
                           stage);
            } finally {
                stage.toFront();
            }
        }
    }

    private File genNewArchivePath(String path, String timestamp, String archiveFormat) {
        path = path.replaceFirst(String.format("(\\.%s|\\.tar\\.%s)", archiveFormat, archiveFormat),"");

        if (ZipState.getCompressorArchives().contains(archiveInfo.getArchiveFormat())
        ) {
            // tar.<ext> file format
            return new File(String.format("%s%s.tar.%s",
                                                path,
                                                timestamp,
                                                archiveFormat));
        } else {
            return new File(String.format("%s%s.%s",
                                                path,
                                                timestamp,
                                                archiveFormat));
        }
    }

}
