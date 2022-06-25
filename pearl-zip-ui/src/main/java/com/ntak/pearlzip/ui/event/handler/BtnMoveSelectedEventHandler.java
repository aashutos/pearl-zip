/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.event.handler;

import com.ntak.pearlzip.archive.pub.ArchiveReadService;
import com.ntak.pearlzip.archive.pub.ArchiveWriteService;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.model.FXMigrationInfo;
import com.ntak.pearlzip.ui.model.ZipState;
import com.ntak.pearlzip.ui.util.AlertException;
import com.ntak.pearlzip.ui.util.ArchiveUtil;
import com.ntak.pearlzip.ui.util.CheckEventHandler;
import com.ntak.pearlzip.ui.util.JFXUtil;
import javafx.event.ActionEvent;
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
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.JFXUtil.raiseAlert;
import static com.ntak.pearlzip.ui.util.internal.JFXUtil.changeButtonPicText;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 *  Event Handler for Move Migration functionality.
 *  @author Aashutos Kakshepati
*/
public class BtnMoveSelectedEventHandler implements CheckEventHandler<ActionEvent> {

    private static final Logger LOGGER = LoggerContext.getContext().getLogger(BtnMoveSelectedEventHandler.class);

    private final TableView<FileInfo> fileContentsView;
    private final FXArchiveInfo fxArchiveInfo;
    private final MenuItem mnuMoveSelected;
    private final MenuButton copyButton;
    private final MenuButton moveButton;
    private final Button delButton;

    public BtnMoveSelectedEventHandler(TableView<FileInfo> fileContentsView, MenuButton copyButton,
            MenuButton moveButton, Button delButton, FXArchiveInfo fxArchiveInfo) {
        this.fileContentsView = fileContentsView;
        this.mnuMoveSelected = moveButton.getItems()
                                         .stream()
                                         .filter(m -> m.getId().equals("mnuMoveSelected"))
                                         .findFirst()
                                         .get();
        this.copyButton = copyButton;
        this.moveButton = moveButton;
        this.delButton = delButton;
        this.fxArchiveInfo = fxArchiveInfo;
    }

    @Override
    public void handleEvent(ActionEvent event) {
        final FileInfo selectedItem = fileContentsView.getSelectionModel().getSelectedItem();
        final FXMigrationInfo migrationInfo = fxArchiveInfo.getMigrationInfo();

        // If Migration type is MOVE
        // DROP MODE START
        if (migrationInfo.getType().equals(FXMigrationInfo.MigrationType.MOVE)) {
            ArchiveReadService archiveReadService = fxArchiveInfo.getReadService();
            ArchiveWriteService archiveWriteService = fxArchiveInfo.getWriteService();
            int depth = fxArchiveInfo.getDepth().get();
            String prefix = fxArchiveInfo.getPrefix();
            try {
                // If current folder = source folder then warning alert and exit (clear selection)
                if (Optional.ofNullable(Paths.get(migrationInfo
                                                               .getFile().getFileName()).getParent()).orElse(Paths.get("")).toString().equals(fxArchiveInfo.getPrefix())) {
                    // TITLE: Warning: Cannot drop in this location
                    // HEADER: Cancelling migration
                    // BODY: Migration has been cancelled as the destination location is the same as the source
                    // location.
                    // This is an unsupported process.
                    raiseAlert(Alert.AlertType.WARNING,
                               resolveTextKey(TITLE_CANNOT_DROP_SAME_DIR),
                               resolveTextKey(HEADER_ISSUE_SAME_DIR),
                               resolveTextKey(BODY_ISSUE_SAME_DIR),
                               fileContentsView.getScene().getWindow()
                    );

                    // finally: clear Migration Info, Enable move and delete button, update Copy button look
                    migrationInfo.clear();
                    copyButton.setDisable(false);
                    delButton.setDisable(false);
                    changeButtonPicText(moveButton, "move.png", resolveTextKey(LBL_BUTTON_MOVE));
                    mnuMoveSelected.setText("Move Selected");

                    JFXUtil.refreshFileView(fileContentsView, fxArchiveInfo, depth, prefix);
                } else {
                    // else execute copy routine
                    // Extract to temp location
                    // Add from temp location with new prefix
                    long sessionId = System.currentTimeMillis();
                    JFXUtil.executeBackgroundProcess(sessionId, (Stage) fileContentsView.getScene().getWindow(),
                                                          ()->{
                                                              Path fileName = Paths.get(migrationInfo.getFile().getFileName()).getFileName();
                                                              Path tempDir = Files.createTempDirectory(TMP_DIR_PREFIX);
                                                              Path tempFile = Paths.get(tempDir.toString(), fileName.toString());
                                                              Path tempArchive = com.ntak.pearlzip.ui.util.ArchiveUtil.createBackupArchive(fxArchiveInfo, tempDir);
                                                              Files.copy(Paths.get(fxArchiveInfo.getArchivePath()), tempArchive, REPLACE_EXISTING);

                                                              boolean success =
                                                                      archiveReadService.extractFile(sessionId,
                                                                                                     tempFile, fxArchiveInfo.getArchiveInfo(),
                                                                                                               migrationInfo.getFile());

                                                              if (!success) {
                                                                  // LOG: Issue extracting file to %s for copy from archive %s
                                                                  LOGGER.error(resolveTextKey(LOG_ISSUE_EXTRACTING_FILE_FOR_COPY, tempFile.toString(),
                                                                                              fxArchiveInfo.getArchivePath()));
                                                                  throw new IOException(resolveTextKey(LOG_ISSUE_EXTRACTING_FILE_FOR_COPY, tempFile.toString(), fxArchiveInfo.getArchivePath()));
                                                              }

                                                              FileInfo newFile =
                                                                      new FileInfo(fxArchiveInfo.getFiles().size(),
                                                                                   fxArchiveInfo.getDepth().get(),
                                                                                   Paths.get(fxArchiveInfo.getPrefix(), fileName.toString()).toString(),
                                                                                   -1,
                                                                                   0,
                                                                                   0,
                                                                                   null,
                                                                                   null,
                                                                                   null,
                                                                                   "",
                                                                                   "",
                                                                                   0,
                                                                                   String.format("File %s copied into archive by PearlZip",
                                                                                                 fileName),
                                                                                   Files.isDirectory(tempFile),
                                                                                   false,
                                                                                   Collections.singletonMap(KEY_FILE_PATH, tempFile.toAbsolutePath().toString())
                                                                      );

                                                              LOGGER.info(resolveTextKey(LOG_PASTE_FILE_DETAILS, newFile.getFileName(),
                                                                                         fxArchiveInfo.getDepth(), fxArchiveInfo.getPrefix()));

                                                              success = archiveWriteService.addFile(sessionId,
                                                                                                    fxArchiveInfo.getArchiveInfo(), newFile);
                                                              success &= archiveWriteService.deleteFile(sessionId,
                                                                                                        fxArchiveInfo.getArchiveInfo(), fxArchiveInfo.getMigrationInfo()
                                                                                                                                                     .getFile());

                                                              if (!success) {
                                                                  com.ntak.pearlzip.ui.util.ArchiveUtil.restoreBackupArchive(tempArchive,
                                                                                                                             Paths.get(fxArchiveInfo.getArchivePath()));

                                                                  // LOG: Issue adding file %s to archive %s
                                                                  LOGGER.error(resolveTextKey(LOG_ISSUE_ADDING_FILE_FOR_COPY, fileName.toString(),
                                                                                              fxArchiveInfo.getArchivePath()));
                                                                  throw new IOException(resolveTextKey(LOG_ISSUE_ADDING_FILE_FOR_COPY, fileName.toString(),
                                                                                                       fxArchiveInfo.getArchivePath()));
                                                              }

                                                              Files.deleteIfExists(tempFile);
                                                              com.ntak.pearlzip.ui.util.ArchiveUtil.removeBackupArchive(tempArchive);
                                                          },
                                                          (e)->{
                                                              // LOG: Issue occurred on pasting migration item (root item: %s). Migration has been cancelled.
                                                              LOGGER.error(resolveTextKey(LOG_PASTE_EXCEPTION, migrationInfo
                                                                    .getFile()));

                                                              // TITLE: ERROR: Issue occurred on migration
                                                              // HEADER: There was while performing migration
                                                              // BODY: Upon initiating function '%s', an issue occurred on attempting the migration. Migration has been cancelled.
                                                              raiseAlert(Alert.AlertType.ERROR,
                                                                         resolveTextKey(TITLE_PASTE_EXCEPTION),
                                                                         resolveTextKey(HEADER_PASTE_EXCEPTION),
                                                                         resolveTextKey(BODY_PASTE_EXCEPTION, this.getClass().getName()),
                                                                         null
                                                              );
                                                          },
                                                          (s)->{
                                                              // finally: clear Migration Info, Enable move and delete button, update Copy button look
                                                              migrationInfo.clear();
                                                              copyButton.setDisable(false);
                                                              delButton.setDisable(false);
                                                              changeButtonPicText(moveButton, "move.png", resolveTextKey(LBL_BUTTON_MOVE));
                                                              mnuMoveSelected.setText("Move Selected");

                                                              JFXUtil.refreshFileView(fileContentsView, fxArchiveInfo, depth, prefix);
                                                          }
                    );
                }
            } catch (Exception e) {
                // LOG: Issue occurred on pasting migration item (root item: %s). Migration has been cancelled.
                LOGGER.error(resolveTextKey(LOG_PASTE_EXCEPTION, migrationInfo
                                                                              .getFile()));

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
        // DROP MODE END

        // If Migration Info type is null
        // COPY MODE START
        if (!selectedItem.isFolder()) {
            synchronized(migrationInfo) {
                if (migrationInfo.getType()
                                 .equals(FXMigrationInfo.MigrationType.NONE)) {
                    // Change picture to drop
                    changeButtonPicText(moveButton, "drop.png", resolveTextKey(LBL_BUTTON_DROP));
                    // Disable move button
                    copyButton.setDisable(true);
                    // Disable delete button
                    delButton.setDisable(true);
                    mnuMoveSelected.setText("Drop Selected");

                    // Set MigrationInfo
                    migrationInfo.initMigration(FXMigrationInfo.MigrationType.MOVE, selectedItem);
                    fileContentsView.refresh();
                }
            }
            // COPY MODE END
        }
    }

    @Override
    public void check(ActionEvent event) throws AlertException {
        ArchiveUtil.checkArchiveExists(fxArchiveInfo);

        FileInfo selectedItem;
        // If item not selected (and not in paste mode)
        // or if a compressor zip then exit method
        // or selected is folder and not in paste mode
        // or Migration Info of type COPY
        final FXMigrationInfo migrationInfo = fxArchiveInfo.getMigrationInfo();
        if (((selectedItem = fileContentsView.getSelectionModel().getSelectedItem()) == null && !FXMigrationInfo.MigrationType.MOVE.equals(
                migrationInfo.getType()))
                || ZipState.getCompressorArchives().contains(fxArchiveInfo.getArchivePath().substring(fxArchiveInfo.getArchivePath().lastIndexOf(".")+1))
                || (!FXMigrationInfo.MigrationType.MOVE.equals(migrationInfo.getType()) && selectedItem.isFolder())
                || FXMigrationInfo.MigrationType.COPY.equals(migrationInfo.getType())
                || Objects.isNull(fxArchiveInfo.getWriteService())
        ) {
            // LOG: Move could not be initiated for file %s
            // TITLE: Warning: Cannot initiate move
            // HEADER: Move is not possible
            // BODY: Move could not be initiated for one of the following reasons:
            // \n\t\u2022 No item has been selected to move.
            // \n\t\u2022 The archive is a compressor archive and so copy is unsupported.
            // \n\t\u2022 Migration is in COPY mode.
            // \n\t\u2022 The selected item is a folder.

            JFXUtil.refreshFileView(fileContentsView, fxArchiveInfo, fxArchiveInfo.getDepth().get(), fxArchiveInfo.getPrefix());
            String fileName = "-";
            if (Objects.nonNull(selectedItem)) {
                fileName = selectedItem.getFileName();
            }
            throw new AlertException(fxArchiveInfo,
                                     resolveTextKey(LOG_CANNOT_INIT_MOVE, fileName),
                                     Alert.AlertType.WARNING,
                                     resolveTextKey(TITLE_CANNOT_INIT_MOVE),
                                     resolveTextKey(HEADER_CANNOT_INIT_MOVE),
                                     resolveTextKey(BODY_CANNOT_INIT_MOVE),
                                     null,
                                     fileContentsView.getScene().getWindow());
        }
    }
}
