/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.pub;

import com.ntak.pearlzip.archive.pub.ArchiveService;
import com.ntak.pearlzip.archive.pub.ProgressMessage;
import com.ntak.pearlzip.ui.util.JFXUtil;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import static com.ntak.pearlzip.archive.constants.LoggingConstants.*;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;

/**
 *  Controller for the Progress Bar dialog.
 *  @author Aashutos Kakshepati
*/
public class FrmProgressController {

    private Stage stage;
    @FXML
    private Label lblProgress;
    @FXML
    private ProgressBar barProgress;
    private CountDownLatch initiationLatch;
    private boolean isStarted = false;
    private Consumer<Stage> callback;
    private long sessionId;

    @FXML
    public void initialize() {
        ArchiveService.DEFAULT_BUS.register(this);
    }

    public void initData(Stage stage, CountDownLatch latch, Consumer<Stage> callback, long sessionId) {
        this.stage = stage;
        this.initiationLatch = latch;
        this.sessionId = sessionId;
        stage.setOnShown((e)->latch.countDown());
        this.callback = callback;
        stage.setOnCloseRequest((e)-> ArchiveService.DEFAULT_BUS.unregister(this));
    }

    @Subscribe(threadMode=ThreadMode.BACKGROUND)
    public void consumeUpdate(ProgressMessage message) {
        if (message.type().equals(PROGRESS) && message.sessionId() == sessionId) {
            isStarted = true;
            JFXUtil.runLater(() -> {
                lblProgress.setText(message.message());
                if ((message.completed() / message.total()) >= 0) {
                    barProgress.setProgress(barProgress.getProgress() + (message.completed() / message.total()));
                }
                if (message.total() == ProgressIndicator.INDETERMINATE_PROGRESS) {
                    barProgress.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
                }
            });
        }

        if (isStarted && message.type().equals(COMPLETED) && message.sessionId() == sessionId) {
            JFXUtil.runLater(() -> {
                try {
                    lblProgress.setText(resolveTextKey(LBL_PROGRESS_COMPLETION));
                    barProgress.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
                    callback.accept(stage);
                } catch(Exception e) {
                } finally {
                    PauseTransition delay = new PauseTransition(Duration.millis(100));
                    delay.setOnFinished((e) -> stage.fireEvent(new WindowEvent(stage,
                                                                               WindowEvent.WINDOW_CLOSE_REQUEST)));
                    delay.play();
                }
            });
        }
    }

}
