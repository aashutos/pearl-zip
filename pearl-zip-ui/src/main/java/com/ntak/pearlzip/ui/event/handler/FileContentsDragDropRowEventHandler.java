/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.event.handler;

import com.ntak.pearlzip.archive.pub.ArchiveWriteService;
import com.ntak.pearlzip.archive.pub.ErrorMessage;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.archive.pub.ProgressMessage;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.util.AlertException;
import com.ntak.pearlzip.ui.util.CheckEventHandler;
import com.ntak.pearlzip.ui.util.JFXUtil;
import com.ntak.pearlzip.ui.util.internal.ArchiveUtil;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.stage.Stage;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.KEY_FILE_PATH;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.PROGRESS;
import static com.ntak.pearlzip.archive.pub.ArchiveService.DEFAULT_BUS;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.JFXUtil.raiseAlert;
import static com.ntak.pearlzip.ui.util.internal.ArchiveUtil.handleDirectory;
import static javafx.scene.control.ProgressIndicator.INDETERMINATE_PROGRESS;

/**
 *  Event Handler for Drag-and-Drop add files/directories functionality.
 *  @author Aashutos Kakshepati
*/
public class FileContentsDragDropRowEventHandler implements CheckEventHandler<DragEvent> {

    private static final Logger LOGGER =
            LoggerContext.getContext().getLogger(FileContentsDragDropRowEventHandler.class);
    private TableView<FileInfo> fileContentsView;
    private FXArchiveInfo fxArchiveInfo;

    public FileContentsDragDropRowEventHandler(TableView<FileInfo> fileContentsView, FXArchiveInfo fxArchiveInfo) {
        this.fileContentsView = fileContentsView;
        this.fxArchiveInfo = fxArchiveInfo;
    }

    @Override
    public void handleEvent(DragEvent event) {
        int depth = fxArchiveInfo.getDepth()
                                 .get();
        String prefix = fxArchiveInfo.getPrefix();

        try {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                // TITLE: Confirmation: Add files
                // HEADER: File drop detected
                // BODY: Do you wish to add the dropped files to the archive?
                Optional<ButtonType> response = raiseAlert(Alert.AlertType.CONFIRMATION,
                                                           resolveTextKey(TITLE_CONFIRM_ADD_FILE),
                                                           resolveTextKey(HEADER_CONFIRM_ADD_FILE),
                                                           resolveTextKey(BODY_CONFIRM_ADD_FILE),
                                                           null,
                                                           fileContentsView.getScene().getWindow(),
                                                           ButtonType.YES, ButtonType.NO);

                if (response.isPresent() && response.get()
                                                    .getButtonData()
                                                    .equals(ButtonBar.ButtonData.YES)) {
                    long sessionId = System.currentTimeMillis();
                    ArchiveWriteService archiveWriteService = fxArchiveInfo.getWriteService();
                    List<FileInfo> files = new ArrayList<>();

                    JFXUtil.executeBackgroundProcess(sessionId, (Stage) fileContentsView.getScene().getWindow(),
                      ()->{
                          CountDownLatch latch = new CountDownLatch(1);
                          JFXUtil.runLater(() -> {
                              int index = fxArchiveInfo.getFiles()
                                                       .size();


                              try {
                                  for (File file : db.getFiles()) {
                                      try {
                                          if (file.isFile()) {
                                              // Retrieving metadata for file %s
                                              DEFAULT_BUS.post(new ProgressMessage(sessionId, PROGRESS,
                                                                                   resolveTextKey(LBL_RETRIEVE_FILE_META,
                                                                                                  file.getAbsolutePath()),
                                                                                   INDETERMINATE_PROGRESS, 1));
                                              Path destFile = Paths.get(prefix,
                                                                        file.toPath()
                                                                            .getFileName()
                                                                            .toString());
                                              FileInfo fileInfo = new FileInfo(index++, depth, destFile.toString(),
                                                                               -1, 0, 0, null, null, null,
                                                                               "", "", 0, "",
                                                                               file.isDirectory(), false,
                                                                           Collections.singletonMap(KEY_FILE_PATH,
                                                                                                    file.getAbsoluteFile()
                                                                                                        .toString()));
                                          files.add(fileInfo);
                                      } else { // folder
                                              // Retrieving metadata for files in folder %s
                                              DEFAULT_BUS.post(new ProgressMessage(sessionId, PROGRESS,
                                                                                                  resolveTextKey(LBL_RETRIEVE_FOLDER_META, file.getAbsolutePath()),
                                                                                              INDETERMINATE_PROGRESS, 1));
                                              List<FileInfo> genFiles = handleDirectory(prefix,
                                                                                        file.toPath().getParent(),
                                                                                        file.toPath(),
                                                                                        depth,
                                                                                        fxArchiveInfo.getFiles()
                                                                                                     .size());
                                              files.addAll(genFiles);

                                              // Empty directory case...
                                              if (genFiles.size() == 0) {
                                                  files.add(new FileInfo((index + 1), depth,
                                                                         depth > 0 ? String.format("%s/%s", prefix,
                                                                                                   file.getName()) :
                                                                                 file.getName(),
                                                                         -1, 0,
                                                                         0, null,
                                                                         null, null,
                                                                         "", "", 0, "",
                                                                         true, false,
                                                                         Collections.singletonMap(KEY_FILE_PATH,
                                                                                                  file.getAbsolutePath())));
                                              }
                                      }
                                  } catch(Exception e) {
                                      // LOG: Issue obtaining meta data for file/folder %s
                                      LOGGER.warn(resolveTextKey(LOG_ISSUE_RETRIEVE_META, file.getAbsolutePath()));
                                  }
                              }
                          } catch (Exception e) {
                              // LOG: Issue occurred adding files [%s] via Drag and Drop.
                              // TITLE: ERROR: Issue with adding files (via Drag and Drop)
                              // HEADER: Exception occurred when adding file(s)
                              // BODY: Could not add files (%s) to the archive. Please check stack trace below for more details:
                              LOGGER.warn(resolveTextKey(LOG_ISSUE_ADD_DRAG_DROP, event.getDragboard().getFiles()));
                              DEFAULT_BUS.post(new ErrorMessage(sessionId, resolveTextKey(TITLE_ISSUE_ADD_DRAG_DROP),
                                                                resolveTextKey(HEADER_ISSUE_ADD_DRAG_DROP),
                                                                resolveTextKey(BODY_ISSUE_ADD_DRAG_DROP,
                                                                               event.getDragboard().getFiles()), e,
                                                                null));
                          } finally {
                              latch.countDown();
                          }
                          });
                        latch.await();

                        archiveWriteService.addFile(sessionId, fxArchiveInfo.getArchiveInfo(),
                                                      files.toArray(new FileInfo[0]));
                      },
                      Throwable::printStackTrace,
                      (s)->JFXUtil.refreshFileView(fileContentsView, fxArchiveInfo, depth, prefix));
                }
            }
        } catch (Exception e) {
            // LOG: Issue occurred adding files [%s] via Drag and Drop.
            // TITLE: ERROR: Issue with adding files (via Drag and Drop)
            // HEADER: Exception occurred when adding file(s)
            // BODY: Could not add files (%s) to the archive. Please check stack trace below for more details:
            LOGGER.warn(resolveTextKey(LOG_ISSUE_ADD_DRAG_DROP, event.getDragboard().getFiles()));
            raiseAlert(Alert.AlertType.ERROR, resolveTextKey(TITLE_ISSUE_ADD_DRAG_DROP),
                       resolveTextKey(HEADER_ISSUE_ADD_DRAG_DROP),
                       resolveTextKey(BODY_ISSUE_ADD_DRAG_DROP, event.getDragboard().getFiles()), e,
                       fileContentsView.getScene().getWindow());
        }
    }

    @Override
    public void check(DragEvent event) throws AlertException {
        ArchiveUtil.checkArchiveExists(fxArchiveInfo);

        if (Objects.isNull(fxArchiveInfo.getWriteService())) {
            // LOG: Warning: Add functionality not supported for archive %s
            // TITLE: Warning: Add functionality not supported
            // HEADER: No Write provider for archive format
            // BODY: Cannot add file to archive as functionality is not supported for file: %s
            LOGGER.warn(resolveTextKey(LOG_ADD_FUNC_NOT_SUPPORTED, fxArchiveInfo.getArchivePath()));
            throw new AlertException(fxArchiveInfo,
                                     resolveTextKey(LOG_ADD_FUNC_NOT_SUPPORTED, fxArchiveInfo.getArchivePath()),
                                     Alert.AlertType.WARNING,
                       resolveTextKey(TITLE_ADD_FUNC_NOT_SUPPORTED),
                       resolveTextKey(HEADER_ADD_FUNC_NOT_SUPPORTED),
                       resolveTextKey(BODY_ADD_FUNC_NOT_SUPPORTED,
                                      Paths.get(fxArchiveInfo.getArchivePath())
                                           .getFileName()
                                           .toString()),
                       null,
                       fileContentsView.getScene().getWindow()
            );
        }
    }
}
