/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.cell;

import com.ntak.pearlzip.archive.pub.ArchiveReadService;
import com.ntak.pearlzip.archive.pub.ArchiveWriteService;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.model.ZipState;
import com.ntak.pearlzip.ui.util.ArchiveUtil;
import com.ntak.pearlzip.ui.util.JFXUtil;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.KEY_FILE_PATH;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ResourceConstants.PATTERN_TEXTFIELD_TABLE_CELL_STYLE;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;

/**
 *  Implementation of cell renderer for the Comments field.
 *  @author Aashutos Kakshepati
*/
public class CommentsHighlightFileInfoCellCallback extends AbstractHighlightFileInfoCellCallback {

    private FXArchiveInfo fxArchiveInfo;

    public CommentsHighlightFileInfoCellCallback(FXArchiveInfo fxArchiveInfo) {
        this.fxArchiveInfo = fxArchiveInfo;
    }

    @Override
    public void setField(TableCell<FileInfo,FileInfo> cell, FileInfo info) {
        String comments = info.getComments();

        TextField textField = new TextField();
        textField.setText(comments);
        String color = cell.getTableRow().isSelected() ? "white" : "black";
        textField.setStyle(String.format(PATTERN_TEXTFIELD_TABLE_CELL_STYLE, color));

        textField.setOnKeyTyped(e -> info.setComments(e.getText()));
        textField.focusedProperty().addListener((observable,oldVal,newVal) -> {
            // Commit changes to comments
            if (oldVal && !newVal) {
                ArchiveWriteService writeService =
                        ZipState.getWriteArchiveServiceForFile(fxArchiveInfo.getArchivePath())
                                .orElse(null);
                ArchiveReadService readService =
                        ZipState.getReadArchiveServiceForFile(fxArchiveInfo.getArchivePath())
                                .orElse(null);

                long sessionId = System.currentTimeMillis();
                if (Objects.nonNull(writeService) && Objects.nonNull(readService)) {
                    Path tempFile =
                            Paths.get(InternalContextCache.GLOBAL_CONFIGURATION_CACHE
                                                          .<Path>getAdditionalConfig(CK_STORE_ROOT)
                                                          .get()
                                                          .toAbsolutePath()
                                                          .toString(),
                                              String.format("pz%s", sessionId),
                                              info.getFileName()
                    );
                    try {
                        Files.createDirectories(tempFile.getParent());
                        Files.createFile(tempFile);

                        if (readService.extractFile(sessionId, tempFile, fxArchiveInfo.getArchiveInfo(), info)) {
                            // Remove file...
                            sessionId = System.currentTimeMillis();
                            writeService.deleteFile(sessionId, fxArchiveInfo.getArchiveInfo(), info);

                            // Rewrite file into archive...
                            sessionId = System.currentTimeMillis();
                            info.setComments(textField.getText());
                            info.getAdditionalInfoMap().put(KEY_FILE_PATH, tempFile.toAbsolutePath().toString());
                            writeService.addFile(sessionId, fxArchiveInfo.getArchiveInfo(), info);
                        }
                    } catch(IOException e) {
                    } finally {
                        sessionId = System.currentTimeMillis();
                        ArchiveUtil.deleteDirectory(tempFile.getParent(), (f)->false);
                        final TableView<FileInfo> fileContentsView = fxArchiveInfo.getController()
                                                                                  .get()
                                                                                  .getFileContentsView();
                        fileContentsView.setItems(FXCollections.observableArrayList(readService.listFiles(sessionId,
                                                                                                          fxArchiveInfo.getArchiveInfo()
                                                  ))
                        );
                        JFXUtil.refreshFileView(fileContentsView,
                                                fxArchiveInfo,
                                                fxArchiveInfo.getDepth().get(),
                                                fxArchiveInfo.getPrefix());

                    }
                }  else {
                    // TITLE: Add comment functionality not supported for archive %s
                    // BODY: No Write provider for archive format
                    JFXUtil.raiseAlert(Alert.AlertType.ERROR,
                                       resolveTextKey(TITLE_ADD_COMMENT_NOT_SUPPORTED),
                                       null,
                                       resolveTextKey(BODY_ADD_COMMENT_NOT_SUPPORTED),
                                       null);
                }
            }
        });
        cell.setGraphic(textField);
    }
}
