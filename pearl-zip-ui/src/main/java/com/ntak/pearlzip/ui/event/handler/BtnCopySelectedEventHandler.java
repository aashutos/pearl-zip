/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.event.handler;

import com.ntak.pearlzip.archive.pub.*;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.model.FXMigrationInfo;
import com.ntak.pearlzip.ui.model.ZipState;
import com.ntak.pearlzip.ui.util.JFXUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.KEY_FILE_PATH;
import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.TMP_DIR_PREFIX;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.COMPLETED;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.ArchiveUtil.createBackupArchive;
import static com.ntak.pearlzip.ui.util.JFXUtil.changeButtonPicText;
import static com.ntak.pearlzip.ui.util.JFXUtil.raiseAlert;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 *  Event Handler for Copy Migration functionality.
 *  @author Aashutos Kakshepati
*/
public class BtnCopySelectedEventHandler implements EventHandler<ActionEvent> {

    private static final Logger LOGGER = LoggerContext.getContext()
                                                      .getLogger(BtnCopySelectedEventHandler.class);

    private final TableView<FileInfo> fileContentsView;
    private final FXArchiveInfo fxArchiveInfo;
    private final MenuItem mnuCopySelected;
    private final MenuButton copyButton;
    private final MenuButton moveButton;
    private final Button delButton;

    public BtnCopySelectedEventHandler(TableView<FileInfo> fileContentsView, MenuButton copyButton,
            MenuButton moveButton, Button delButton, FXArchiveInfo fxArchiveInfo) {
        this.fileContentsView = fileContentsView;
        this.mnuCopySelected = copyButton.getItems()
                                         .stream()
                                         .filter(m -> m.getId()
                                                       .equals("mnuCopySelected"))
                                         .findFirst()
                                         .get();
        this.copyButton = copyButton;
        this.moveButton = moveButton;
        this.delButton = delButton;
        this.fxArchiveInfo = fxArchiveInfo;
    }

    @Override
    public void handle(ActionEvent event) {
        FileInfo selectedItem;
        // If item not selected (and not in paste mode)
        // or if a compressor zip then exit method
        // or selected is folder and not in paste mode
        // or Migration Info of type MOVE
        final FXMigrationInfo migrationInfo = fxArchiveInfo.getMigrationInfo();
        if (((selectedItem = fileContentsView.getSelectionModel()
                                             .getSelectedItem()) == null && !FXMigrationInfo.MigrationType.COPY.equals(
                migrationInfo.getType()))
                || ZipState.getCompressorArchives()
                           .contains(fxArchiveInfo.getArchivePath()
                                                  .substring(fxArchiveInfo.getArchivePath()
                                                                          .lastIndexOf(".") + 1))
                || (!FXMigrationInfo.MigrationType.COPY.equals(migrationInfo.getType()) && selectedItem.isFolder())
                || FXMigrationInfo.MigrationType.MOVE.equals(migrationInfo.getType())
                || Objects.isNull(fxArchiveInfo.getWriteService())
        ) {
            // TITLE: Warning: Cannot initiate copy
            // HEADER: Copy is not possible
            // BODY: Copy could not be initiated for one of the following reasons:
            // \n\t\u2022 No item has been selected to copy.
            // \n\t\u2022 The archive is a compressor archive and so copy is unsupported.
            // \n\t\u2022 Migration is in MOVE mode.
            // \n\t\u2022 The selected item is a folder.
            raiseAlert(Alert.AlertType.WARNING,
                       resolveTextKey(TITLE_CANNOT_INIT_COPY),
                       resolveTextKey(HEADER_CANNOT_INIT_COPY),
                       resolveTextKey(BODY_CANNOT_INIT_COPY),
                       fileContentsView.getScene()
                                       .getWindow());
            return;
        }

        // If Migration type is COPY
        // PASTE MODE START
        if (migrationInfo.getType()
                         .equals(FXMigrationInfo.MigrationType.COPY)) {
            ArchiveReadService archiveReadService = fxArchiveInfo.getReadService();
            ArchiveWriteService archiveWriteService = fxArchiveInfo.getWriteService();
            int depth = fxArchiveInfo.getDepth()
                                     .get();
            int prevCount = fxArchiveInfo.getFiles()
                                         .size();
            String prefix = fxArchiveInfo.getPrefix();
            long sessionId = System.currentTimeMillis();
            try {
                // If current folder = source folder then warning alert and exit (clear selection)
                if (Optional.ofNullable(Paths.get(migrationInfo
                                                          .getFile()
                                                          .getFileName())
                                             .getParent())
                            .orElse(Paths.get(""))
                            .toString()
                            .equals(fxArchiveInfo.getPrefix())) {
                    // TITLE: Warning: Cannot paste in this location
                    // HEADER: Cancelling migration
                    // BODY: Migration has been cancelled as the destination location is the same as the source
                    // location.
                    // This is an unsupported process.
                    raiseAlert(Alert.AlertType.WARNING,
                               resolveTextKey(TITLE_CANNOT_PASTE_SAME_DIR),
                               resolveTextKey(HEADER_ISSUE_SAME_DIR),
                               resolveTextKey(BODY_ISSUE_SAME_DIR),
                               fileContentsView.getScene()
                                               .getWindow()
                    );

                    // finally: clear Migration Info, Enable move and delete button, update Copy button look
                    migrationInfo.clear();
                    moveButton.setDisable(false);
                    delButton.setDisable(false);
                    changeButtonPicText(copyButton, "copy.png", resolveTextKey(LBL_BUTTON_COPY));
                    mnuCopySelected.setText("Copy Selected");

                    JFXUtil.refreshFileView(fileContentsView, fxArchiveInfo, depth, prefix);
                } else { // else execute copy routine
                    // Extract to temp location
                    // Add from temp location with new prefix
                    Path fileName = Paths.get(migrationInfo.getFile()
                                                           .getFileName())
                                         .getFileName();
                    Path tempDir = Files.createTempDirectory(TMP_DIR_PREFIX);
                    Path tempFile = Paths.get(tempDir.toString(), fileName.toString());
                    Path tempArchive = createBackupArchive(fxArchiveInfo, tempDir);
                    Files.copy(Paths.get(fxArchiveInfo.getArchivePath()), tempArchive, REPLACE_EXISTING);

                    boolean success = archiveReadService.extractFile(sessionId, tempFile,
                                                                     fxArchiveInfo.getArchivePath(),
                                                                     migrationInfo.getFile());

                    if (!success) {
                        // LOG: Issue extracting file to %s for copy from archive %s
                        LOGGER.error(resolveTextKey(LOG_ISSUE_EXTRACTING_FILE_FOR_COPY, tempFile.toString(),
                                                    fxArchiveInfo.getArchivePath()));
                        throw new IOException(resolveTextKey(LOG_ISSUE_EXTRACTING_FILE_FOR_COPY,
                                                             tempFile.toString(),
                                                             fxArchiveInfo.getArchivePath()));
                    }

                    JFXUtil.executeBackgroundProcess(sessionId, (Stage) fileContentsView.getScene()
                                                                             .getWindow(),
                                                     () -> {
                                                              FileInfo newFile =
                                                                      new FileInfo(fxArchiveInfo.getFiles()
                                                                                                .size(),
                                                                                   fxArchiveInfo.getDepth()
                                                                                                .get(),
                                                                                   Paths.get(fxArchiveInfo.getPrefix(),
                                                                                             fileName.toString())
                                                                                        .toString(),
                                                                                   -1,
                                                                                   0,
                                                                                   0,
                                                                                   null,
                                                                                   null,
                                                                                   null,
                                                                                   "",
                                                                                   "",
                                                                                   0,
                                                                                   String.format(
                                                                                           "File %s copied into archive by %s",
                                                                                           fileName,
                                                                                           APP),
                                                                                   Files.isDirectory(tempFile),
                                                                                   false,
                                                                                   Collections.singletonMap(
                                                                                           KEY_FILE_PATH,
                                                                                           tempFile.toAbsolutePath()
                                                                                                   .toString())
                                                                      );

                                                              LOGGER.info(resolveTextKey(
                                                                      LOG_PASTE_FILE_DETAILS,
                                                                      newFile.getFileName(),
                                                                      fxArchiveInfo.getDepth(),
                                                                      fxArchiveInfo.getPrefix()));

                                                              archiveWriteService.addFile(sessionId,
                                                                                          fxArchiveInfo.getArchivePath(),
                                                                                          newFile);
                                                              fxArchiveInfo.refresh();
                                                              boolean successCopy = (prevCount + 1 == fxArchiveInfo.getFiles()
                                                                                                                   .size());

                                                              if (!successCopy) {
                                                                  Files.copy(tempArchive,
                                                                             Paths.get(fxArchiveInfo.getArchivePath()),
                                                                             REPLACE_EXISTING);
                                                                  Files.deleteIfExists(tempArchive);

                                                                  // LOG: Issue adding file %s to archive %s
                                                                  LOGGER.error(resolveTextKey(
                                                                          LOG_ISSUE_ADDING_FILE_FOR_COPY,
                                                                          fileName.toString(),
                                                                          fxArchiveInfo.getArchivePath()));
                                                                  throw new IOException(resolveTextKey(
                                                                          LOG_ISSUE_ADDING_FILE_FOR_COPY,
                                                                          fileName.toString(),
                                                                          fxArchiveInfo.getArchivePath()));

                                                              }
                                                          },
                                                     (s) -> {
                                                              // finally: clear Migration Info, Enable move and delete button, update Copy button look
                                                              migrationInfo.clear();
                                                              moveButton.setDisable(false);
                                                              delButton.setDisable(false);
                                                              changeButtonPicText(copyButton,
                                                                                  "copy.png",
                                                                                  resolveTextKey(LBL_BUTTON_COPY));
                                                              mnuCopySelected.setText("Copy Selected");

                                                              JFXUtil.refreshFileView(fileContentsView,
                                                                                      fxArchiveInfo,
                                                                                      depth,
                                                                                      prefix);
                                                          }
                    );
                }
            } catch(Exception e) {
                // LOG: Issue occurred on pasting migration item (root item: %s). Migration has been cancelled.
                LOGGER.error(resolveTextKey(LOG_PASTE_EXCEPTION, migrationInfo.getFile()));
                ArchiveService.DEFAULT_BUS.post(new ProgressMessage(sessionId, COMPLETED, COMPLETED, 1, 1));

                // TITLE: ERROR: Issue occurred on migration
                // HEADER: There was while performing migration
                // BODY: Upon initiating function '%s', an issue occurred on attempting the migration. Migration has been cancelled.
                raiseAlert(Alert.AlertType.ERROR, resolveTextKey(TITLE_PASTE_EXCEPTION),
                           resolveTextKey(HEADER_PASTE_EXCEPTION),
                           resolveTextKey(BODY_PASTE_EXCEPTION, this.getClass().getName()), e,
                           fileContentsView.getScene().getWindow());
            }
            return;
        }
        // PASTE MODE END

        // If Migration Info type is null
        // COPY MODE START
        if (!selectedItem.isFolder()) {
            synchronized(migrationInfo) {
                if (migrationInfo
                        .getType()
                        .equals(FXMigrationInfo.MigrationType.NONE)) {
                    // Change picture to paste
                    changeButtonPicText(copyButton, "paste.png", resolveTextKey(LBL_BUTTON_PASTE));
                    // Disable move button
                    moveButton.setDisable(true);
                    // Disable delete button
                    delButton.setDisable(true);
                    mnuCopySelected.setText("Paste Selected");

                    // Set MigrationInfo
                    migrationInfo.initMigration(FXMigrationInfo.MigrationType.COPY, selectedItem);
                    fileContentsView.refresh();
                }
                // COPY MODE END
            }
        }
    }
}
