/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.util;

import com.ntak.pearlzip.archive.pub.ErrorMessage;
import com.ntak.pearlzip.archive.pub.ProgressMessage;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Objects;

import static com.ntak.pearlzip.ui.util.JFXUtil.raiseAlert;

public class ErrorAlertConsumer {

    private static final ErrorAlertConsumer ERROR_ALERT_CONSUMER = new ErrorAlertConsumer();

    private ErrorAlertConsumer(){}

    @Subscribe(threadMode=ThreadMode.BACKGROUND)
    public void logProgressMessage(ProgressMessage message) {
        if (message instanceof ErrorMessage errorMessage) {
            Stage rootStage =
                    JFXUtil.getMainStageInstances()
                           .stream()
                           .filter(s -> Objects.nonNull(s) && Objects.nonNull(s.getTitle()) && Objects.nonNull(errorMessage.getArchiveInfo()) && Objects.nonNull(errorMessage.getArchiveInfo().getArchivePath())  && s.getTitle().contains(errorMessage.getArchiveInfo().getArchivePath()))
                           .findFirst()
                           .orElse(null);

            Platform.runLater(() -> {
                raiseAlert(
                        Alert.AlertType.ERROR,
                        errorMessage.getTitle(),
                        errorMessage.getHeader(),
                        errorMessage.message(),
                        errorMessage.getException(),
                        rootStage
                );

                if (Objects.nonNull(rootStage)) {
                    try {
                        rootStage.fireEvent(new WindowEvent(rootStage, WindowEvent.WINDOW_CLOSE_REQUEST));
                    } catch (Exception e) {}
                }
            });
        }
    }

    public static ErrorAlertConsumer getErrorAlertConsumer() {return ERROR_ALERT_CONSUMER;}
}
