/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.event.handler;

import com.ntak.pearlzip.ui.model.ZipState;
import com.ntak.pearlzip.ui.util.JFXUtil;
import com.ntak.pearlzip.ui.util.internal.ArchiveUtil;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.JFXUtil.executeBackgroundProcess;
import static com.ntak.pearlzip.ui.util.JFXUtil.raiseAlert;

/**
 *  Event Handler for Open Archive functionality.
 *  @author Aashutos Kakshepati
*/
public class BtnOpenEventHandler implements EventHandler<MouseEvent> {
    private final Stage stage;

    public BtnOpenEventHandler(Stage stage) {
        this.stage =  stage;
    }

    @Override
    public void handle(MouseEvent event) {

        FileChooser openFileView = new FileChooser();
        // Title: Open archive...
        openFileView.setTitle(resolveTextKey(TITLE_OPEN_ARCHIVE));
        final List<String> extensions = ZipState.supportedReadArchives()
                                             .stream()
                                             .map(e -> String.format("*.%s", e))
                                             .collect(Collectors.toList());
        openFileView.getExtensionFilters().add(new FileChooser.ExtensionFilter(String.format("%s %s",
                                                                                           "Supported archives",
                                                                                           extensions.toString()
                                                                                                     .replace("[","(")
                                                                                                     .replace("]",")")),
                                                                             extensions.toArray(new String[0]))
        );
        File rawFile = openFileView.showOpenDialog(new Stage());

        if (Objects.isNull(rawFile)) {
            return;
        }

        // TITLE: Confirmation: Open file in new window
        // HEADER: Do you wish to open file in a new window?
        // BODY: Select if you wish the archive to be opened in a new window or to replace the current window.

        Optional<ButtonType> response = raiseAlert(Alert.AlertType.CONFIRMATION,
                                                   resolveTextKey(TITLE_OPEN_NEW_WINDOW),
                                                   resolveTextKey(HEADER_OPEN_NEW_WINDOW),
                                                   resolveTextKey(BODY_OPEN_NEW_WINDOW), null,
                                                   stage,
                                                   // Open in New Window
                                                   new ButtonType(resolveTextKey(BTN_OPEN_NEW_WINDOW_YES), ButtonBar.ButtonData.YES),
                                                   // Open in Current Window
                                                   new ButtonType(resolveTextKey(BTN_OPEN_NEW_WINDOW_NO), ButtonBar.ButtonData.NO));
        long sessionId = System.currentTimeMillis();
        AtomicBoolean openSuccess = new AtomicBoolean(false);
        executeBackgroundProcess(sessionId, stage,
                                 () -> openSuccess.set(ArchiveUtil.openFile(rawFile)),
                                 (s) -> {
                                             // Default new window
                                             if (openSuccess.get() && response.isPresent() && response.get()
                                                                                 .getButtonData()
                                                                                 .equals(ButtonBar.ButtonData.NO)) {
                                                 JFXUtil.runLater(() -> this.stage.fireEvent(new WindowEvent(this.stage,
                                                                                                             WindowEvent.WINDOW_CLOSE_REQUEST)));
                                             } else {
                                                 this.stage.toFront();
                                                 Stage currentStage =
                                                         Stage.getWindows()
                                                              .stream()
                                                              .map(Stage.class::cast)
                                                              .filter(stg -> stg.getTitle() != null && stg.getTitle().contains(rawFile.getAbsolutePath()))
                                                              .findFirst()
                                                              .orElse(null);
                                                 if (Objects.nonNull(currentStage)) {
                                                     currentStage.toFront();
                                                 }
                                             }
                                         }
        );
    }
}
