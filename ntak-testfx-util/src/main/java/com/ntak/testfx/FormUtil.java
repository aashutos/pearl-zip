/*
 * Copyright © 2023 92AK
 */
package com.ntak.testfx;

import com.ntak.testfx.specifications.CommonSpecifications;
import javafx.collections.ObservableList;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.testfx.api.FxRobot;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.ntak.testfx.TestFXConstants.RETRIEVAL_TIMEOUT_MILLIS;
import static com.ntak.testfx.TestFXConstants.SHORT_PAUSE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class FormUtil {
    public static <T,R> Optional<TableRow<T>> selectTableViewEntry(FxRobot robot, TableView<T> table,
            Function<T,R> extractor, R option) {
        robot.clickOn(String.format("#%s", table.getId()), new MouseButton[]{MouseButton.PRIMARY});
        robot.sleep(SHORT_PAUSE, MILLISECONDS);

        for (int i = 0; i < table.getItems().size(); i++) {
            final String rowId = String.format("%s-%d", table.getId(), i);
            int index = i;
            TableRow<T> row = CommonSpecifications.retryRetrievalForDuration(RETRIEVAL_TIMEOUT_MILLIS, () -> ((TableCell<T,T>)table.queryAccessibleAttribute(AccessibleAttribute.CELL_AT_ROW_COLUMN,
                                                                                                  index, 0)).getTableRow());
            synchronized(row) {
                row.setId(rowId);
                System.out.printf("Clicking on: %s%n", rowId);
                robot.clickOn(row, new MouseButton[0]);
            }
            System.out.printf("Comparing: %s to %s%n", option, row.getItem());
            if (extractor.apply(row.getItem()).equals(option)) {
                return Optional.of(row);
            }
            robot.sleep(SHORT_PAUSE, MILLISECONDS);
        }

        return Optional.empty();
    }

    public static <T> void selectComboBoxEntry(FxRobot robot, ComboBox<T> combo, T option) {
        final String id = String.format("#%s", combo.getId());

        final ObservableList<T> items = combo.getItems();
        int index = 0;
        for (T item : items) {
            if (item.equals(option)) {
                break;
            }
            index++;
        }
        robot.clickOn(id, new MouseButton[]{MouseButton.PRIMARY})
             .sleep(50, MILLISECONDS);
        for (int i = 0; i < index; i++) {
            robot.push(new KeyCode[]{KeyCode.DOWN})
                 .sleep(50, MILLISECONDS);
        }
        robot.push(new KeyCode[]{KeyCode.ENTER});
    }

    public static <T> void selectChoiceBoxEntry(FxRobot robot, ChoiceBox<T> combo, T option) {
        final String id = String.format("#%s", combo.getId());

        final ObservableList<T> items = combo.getItems();
        int index = 0;
        for (T item : items) {
            if (item.equals(option)) {
                break;
            }
            index++;
        }
        robot.clickOn(id, new MouseButton[]{MouseButton.PRIMARY})
             .sleep(50, MILLISECONDS);
        for (int i = 0; i < index; i++) {
            robot.push(new KeyCode[]{KeyCode.DOWN})
                 .sleep(50, MILLISECONDS);
        }
        robot.push(new KeyCode[]{KeyCode.ENTER});
    }


    public static void resetComboBox(FxRobot robot, ComboBox comboDefaultCompressionLevel) {
        robot.clickOn(comboDefaultCompressionLevel);
        for (int i = 0; i < comboDefaultCompressionLevel.getItems()
                                                        .size(); i++) {
            robot.push(KeyCode.UP).sleep(50, MILLISECONDS);
        }
        robot.push(KeyCode.ENTER);
    }

    public static <T extends Node> T lookupNode(Predicate<Stage> stageExtractor, String id) {
        return (T)Stage.getWindows()
                    .stream()
                    .map(Stage.class::cast)
                    .filter(stageExtractor)
                    .findFirst()
                    .get()
                    .getScene()
                    .lookup(id);
    }

    public static Optional<Stage> lookupStage(String regExPattern) {
        return Window.getWindows().stream().map(Stage.class::cast).filter(s -> s.getTitle().matches(regExPattern)).findFirst();
    }
}
