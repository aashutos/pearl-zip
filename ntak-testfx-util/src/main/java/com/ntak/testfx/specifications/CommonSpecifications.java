/*
 * Copyright © 2023 92AK
 */
package com.ntak.testfx.specifications;

import com.ntak.testfx.TestFXConstants;
import com.ntak.testfx.internal.TestFXUtil;
import javafx.scene.AccessibleAttribute;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.junit.jupiter.api.Assertions;
import org.testfx.api.FxRobot;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.jupiter.api.Assertions.fail;

public class CommonSpecifications {

    public static <T,R> void thenPropertyEqualsValue(T objectToTest, Function<T,R> extractor, R expectation) {
        R actual = extractor.apply(objectToTest);
        Assertions.assertEquals(expectation, actual, String.format("Property value %s does not match expected value %s", actual, expectation));
    }

    public static <T> TableRow<T> thenSelectEntryFromTableView(FxRobot fxRobot, TableView<T> tableView, int i) throws InterruptedException {
        tableView.getSelectionModel()
                   .select(i);
        final T item = tableView.getSelectionModel()
                                               .getSelectedItem();
        System.out.printf("Selected: %s%n", item);
        TableRow<T> row =
                ((TableCell<T,String>) tableView.queryAccessibleAttribute(AccessibleAttribute.CELL_AT_ROW_COLUMN,
                                                                          i, 0)).getTableRow();
        fxRobot.sleep(TestFXConstants.SHORT_PAUSE, MILLISECONDS);
        final int finalI = i;
        CountDownLatch latch = new CountDownLatch(1);
        TestFXUtil.runLater(() -> {
            try {
                tableView.scrollTo(finalI);
            } finally {
                latch.countDown();
            }
        });
        latch.await();

        return row;
    }

    public static void thenLabelOnActiveFormHasValue(FxRobot fxRobot, String labelId, String value) {
        Label label = fxRobot.lookup(labelId).queryAs(Label.class);
        Assertions.assertEquals(value, label.getText(), String.format("%s did not equal expected value: %s", labelId, value));
    }

    public static void thenLabelOnActiveFormMatchesPattern(FxRobot fxRobot, String labelId, String pattern) {
        Label label = fxRobot.lookup(labelId).queryAs(Label.class);
        Assertions.assertTrue(label.getText().matches(pattern), String.format("%s did not match expected pattern: %s", labelId, pattern));
    }

    public static void thenCloseStage(FxRobot fxRobot, Stage stage) throws InterruptedException {
        CountDownLatch scrollLatch = new CountDownLatch(1);
        TestFXUtil.runLater(() -> {
            try {
                stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
            } finally {
                scrollLatch.countDown();
            }
        });
        scrollLatch.await();
        fxRobot.sleep(TestFXConstants.MEDIUM_PAUSE, MILLISECONDS);
    }

    public static void thenExpectDialogWithMatchingMessage(FxRobot robot, String regExMessage) {
        DialogPane dialogPane = retryRetrievalForDuration(TestFXConstants.RETRIEVAL_TIMEOUT_MILLIS, () -> robot.lookup(".dialog-pane").queryAs(DialogPane.class));
        Matcher matcher = Pattern.compile(regExMessage).matcher(dialogPane.getContentText());

        Assertions.assertTrue(matcher.find(),
                              "The text in warning dialog was not matched as expected");
    }

    public static <T> T retryRetrievalForDuration(long timeoutMillis, Supplier<T> supplier) {
        long startTime = System.currentTimeMillis();
        int attempt = 1;
        while ((System.currentTimeMillis() - startTime) < timeoutMillis) {
            try {
                Thread.sleep(TestFXConstants.POLLING_TIMEOUT);
                System.out.printf("Attempt %d to execute retryable process%n", attempt);
                T value = supplier.get();

                Objects.requireNonNull(value, "Dialog was not retrieved");
                return value;
            } catch (Exception e) {
                attempt++;
            }
        }

        fail("Could not retrieve object in a timely manner.");

        return null;
    }
}
