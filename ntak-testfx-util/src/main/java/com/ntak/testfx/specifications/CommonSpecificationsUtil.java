/*
 * Copyright © 2023 92AK
 */
package com.ntak.testfx.specifications;

import com.ntak.testfx.TestFXConstants;
import com.ntak.testfx.internal.TestFXUtil;
import javafx.scene.AccessibleAttribute;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.junit.jupiter.api.Assertions;
import org.testfx.api.FxRobot;

import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class CommonSpecificationsUtil {
    // Common Test Util Method
    public static <T,R> void thenPropertyEqualsValue(T objectToTest, Function<T,R> extractor, R expectation) {
        R actual = extractor.apply(objectToTest);
        Assertions.assertEquals(expectation, actual, String.format("Property value %s does not match expected value %s", actual, expectation));
    }

    // Common Test Util Method
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

    // Common Test Util Method
    public static void thenLabelOnActiveFormHasValue(FxRobot fxRobot, String labelId, String value) {
        Label label = fxRobot.lookup(labelId).queryAs(Label.class);
        Assertions.assertEquals(value, label.getText(), String.format("%s did not equal expected value: %s", labelId, value));
    }

    // Common Test Util Method
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

}
