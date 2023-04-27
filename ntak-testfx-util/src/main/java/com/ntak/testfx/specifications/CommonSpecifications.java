/*
 * Copyright © 2023 92AK
 */
package com.ntak.testfx.specifications;

import com.ntak.testfx.TestFXConstants;
import com.ntak.testfx.internal.TestFXUtil;
import javafx.collections.ObservableList;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.junit.jupiter.api.Assertions;
import org.testfx.api.FxRobot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ntak.testfx.TestFXConstants.LONG_PAUSE;
import static com.ntak.testfx.TestFXConstants.SHORT_PAUSE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class CommonSpecifications {

    public static void givenDirectoryHasBeenCreated(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }

        assertTrue(Files.exists(directory), String.format("%s does not exist", directory));
        assertTrue(Files.isDirectory(directory), String.format("%s is not a directory", directory));
    }

    public static <T> T givenFileHasAttribute(Path path, String attribute, Class<T> klass) throws IOException {
        final Object attributeValue = Files.getAttribute(path, attribute);

        Assertions.assertNotNull(attributeValue, String.format("Attribute %s does not exist in file (%s) metadata", attribute, path));
        Assertions.assertTrue(klass.isInstance(attributeValue), String.format("Attribute class is not of expected instance type (%s)", klass.getCanonicalName()));

        return klass.cast(attributeValue);
    }

    public static Properties givenClasspathFileReadIntoSystemProperties(String resource) throws IOException {
        Properties bootstrap = new Properties();
        bootstrap.load(CommonSpecifications.class.getResourceAsStream(resource));
        bootstrap.entrySet().stream().forEach(e->System.setProperty(e.getKey().toString(), e.getValue().toString()));

        return bootstrap;
    }

    public static void givenPropertySet(String key, String value, Properties properties) {
        properties.setProperty(key, value);
    }

    public static <T> void whenTableViewRefreshedWithData(FxRobot robot, String table, ObservableList<T> data) {
        TableView<T> tbl = robot.lookup(table)
                                .queryAs(TableView.class);
        tbl.setItems(data);
        tbl.refresh();
    }

    public static void whenWindowResized(FxRobot robot, Window window, int xOffset, int yOffset) {
        double x = (window.getX() + window.getWidth());
        double y = (window.getY() + window.getHeight());

        robot.moveTo(0,0)
             .moveTo(x,y)
             .sleep(LONG_PAUSE, TimeUnit.MILLISECONDS)
             .press(MouseButton.PRIMARY)
             .moveBy(xOffset,yOffset)
             .sleep(LONG_PAUSE,TimeUnit.MILLISECONDS)
             .release(MouseButton.PRIMARY)
             .sleep(LONG_PAUSE,TimeUnit.MILLISECONDS);
    }

    public static <T> TableColumn<T,?> whenColumnExtractedFromTable(FxRobot robot, String tableName, String colName) {
        TableView<T> fileContentsView = robot.lookup(tableName).queryAs(TableView.class);
        Optional<TableColumn<T,?>> optColumn = fileContentsView.getColumns().stream().filter(c -> c.getText().equals(colName)).findFirst();
        Assertions.assertTrue(optColumn.isPresent());
        return optColumn.get();
    }

    public static void whenNodeClickedByName(FxRobot robot, String identifier) {
        robot.clickOn(identifier);
    }

    public static void whenNodeDoubleClickedByName(FxRobot robot, String identifier) {
        robot.doubleClickOn(identifier);
    }

    public static void whenButtonClickedOnDialog(FxRobot robot, ButtonType buttonType) {
        DialogPane dialogPane = robot.lookup(".dialog-pane").query();
        robot.clickOn(dialogPane.lookupButton(buttonType));
        robot.sleep(SHORT_PAUSE, MILLISECONDS);
    }

    public static void whenSubNodeClickedByName(FxRobot robot, Supplier<Node> node, String identifier) {
        robot.clickOn(node.get().lookup(identifier));
    }

    public static void thenExpectNoFilesInDirectory(Path directory, int count) throws IOException {
        Assertions.assertTrue(Files.isDirectory(directory), String.format("%s is not a directory", directory));
        Assertions.assertEquals(count, Files.list(directory).count(), String.format("%s has %d files. Expecting %s files in folder", directory, Files.list(directory).count(), count));
    }

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

    public static <T> List<T> thenExtractEntriesFromTable(FxRobot robot, String tblName) {
        TableView<T> tableView = robot.lookup(tblName).queryTableView();
        return tableView.getItems();
    }

    public static void thenWindowHasDimensions(Window window, double expWidth, double expHeight) {
        Assertions.assertEquals(expWidth, window.getWidth(),
                                String.format("Width did not have the expected value. Expected value: %f; Actual value: %f", expWidth, window.getWidth()));
        Assertions.assertEquals(expHeight, window.getHeight(),
                                String.format("Height did not have the expected value. Expected value: %f; Actual value: %f", expHeight, window.getHeight()));
    }

    public static void thenExpectNodeVisibility(FxRobot robot, String nodeQuery, boolean expectedVisibility) {
        Assertions.assertEquals(expectedVisibility, robot.lookup(nodeQuery).queryAs(Node.class).isVisible(),
                                String.format("Node %s is not visible", nodeQuery));
    }

    public static void thenExpectNumberLinesInFile(Path file, int expectedLineCount) throws IOException {
        Assertions.assertEquals(expectedLineCount, Files.lines(file).count(), String.format("Expected count %d was not found in file %s", expectedLineCount, file));
    }

    public static void thenExpectLinePatternInFile(Path file, String regExMessage) throws IOException {
        Assertions.assertTrue(Files.lines(file).anyMatch(l -> Pattern.compile(regExMessage).matcher(l).find()), String.format("Expected pattern '%s' did not match with a line in file %s", regExMessage, file));
    }

    public static void thenNotExpectLinePatternInFile(Path file, String regExMessage) throws IOException {
        Assertions.assertTrue(Files.lines(file).noneMatch(l -> Pattern.compile(regExMessage).matcher(l).find()), String.format("Expected pattern '%s' unexpectedly matched with a line in file %s", regExMessage, file));
    }

    public static void thenExpectFileExists(Path file) {
        Assertions.assertTrue(Files.exists(file), String.format("File %s does not exist", file));
    }

    public static void thenNotExpectFileExists(Path file) {
        Assertions.assertFalse(Files.exists(file), String.format("File %s exists unexpectedly", file));
    }

    public static <T> void thenTableViewHasValuesMatchingExpectation(FxRobot robot, String tableName, Predicate<T> assertionExpression) {
        TableView<T> rowGrid = robot.lookup(tableName).queryAs(TableView.class);
        List<T> rows = rowGrid.getItems();

        Assertions.assertTrue(rows.stream().anyMatch(assertionExpression::test), "None of the rows had the expected value.");
    }

    public static void thenExpectDialogWithMatchingExceptionMessage(FxRobot robot, String regEx) {
        TextArea textArea = retryRetrievalForDuration(TestFXConstants.RETRIEVAL_TIMEOUT_MILLIS, () -> (TextArea)robot.lookup(".dialog-pane").queryAs(DialogPane.class).lookup(".text-area"));
        Matcher matcher = Pattern.compile(regEx).matcher(textArea.getText());

        Assertions.assertTrue(matcher.find(),
                              String.format("Exception message was not as expected. Actual: %s; Expected pattern: %s", textArea.getText(), regEx));
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
