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
import java.lang.reflect.Field;
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

    // CLAUSE: ($DIR) directory has been initialised
    public static void givenDirectoryHasBeenCreated(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }

        assertTrue(Files.exists(directory), String.format("%s does not exist", directory));
        assertTrue(Files.isDirectory(directory), String.format("%s is not a directory", directory));
    }

    // CLAUSE: file ($FILE) exists with attribute ($FILE_ATTRIBUTE)
    public static <T> T givenFileHasAttribute(Path path, String attribute, Class<T> klass) throws IOException {
        final Object attributeValue = Files.getAttribute(path, attribute);

        Assertions.assertNotNull(attributeValue, String.format("Attribute %s does not exist in file (%s) metadata", attribute, path));
        Assertions.assertTrue(klass.isInstance(attributeValue), String.format("Attribute class is not of expected instance type (%s)", klass.getCanonicalName()));

        return klass.cast(attributeValue);
    }

    // CLAUSE: properties file ($PROPERTY_FILE) read into system properties from classpath
    public static Properties givenClasspathFileReadIntoSystemProperties(String resource) throws IOException {
        Properties bootstrap = new Properties();
        bootstrap.load(CommonSpecifications.class.getResourceAsStream(resource));
        bootstrap.entrySet().stream().forEach(e->System.setProperty(e.getKey().toString(), e.getValue().toString()));

        return bootstrap;
    }

    // CLAUSE: ($CONFIG_NAME) property ($KEY) set to ($VALUE)
    public static void givenPropertySet(String key, String value, Properties properties) {
        properties.setProperty(key, value);
    }

    // CLAUSE: reflectively set static field ($FIELD_NAME) in class ($CLASS)
    public static <K> void givenSetPrivateStaticField(Class<?> klass, String filedName, K value) throws NoSuchFieldException, IllegalAccessException {
        Field field = klass.getDeclaredField(filedName);
        field.setAccessible(true);
        field.set(null,value);
    }

    // CLAUSE: refresh table ($TABLE) with data
    public static <T> void whenTableViewRefreshedWithData(FxRobot robot, String table, ObservableList<T> data) {
        TableView<T> tbl = robot.lookup(table)
                                .queryAs(TableView.class);
        tbl.setItems(data);
        tbl.refresh();
    }

    // CLAUSE: main dialog resized by ($X_OFFSET,$Y_OFFSET) pixels
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

    // CLAUSE: when column ($COLUMN_NAME) extracted from table ($TABLE)
    public static <T> TableColumn<T,?> whenColumnExtractedFromTable(FxRobot robot, String tableName, String colName) {
        TableView<T> fileContentsView = robot.lookup(tableName).queryAs(TableView.class);
        Optional<TableColumn<T,?>> optColumn = fileContentsView.getColumns().stream().filter(c -> c.getText().equals(colName)).findFirst();
        Assertions.assertTrue(optColumn.isPresent());
        return optColumn.get();
    }

    // CLAUSE: node ($NODE_NAME) clicked
    public static void whenNodeClickedByName(FxRobot robot, String identifier) {
        robot.clickOn(identifier);
    }

    // CLAUSE: node ($NODE_NAME) double-clicked
    public static void whenNodeDoubleClickedByName(FxRobot robot, String identifier) {
        robot.doubleClickOn(identifier);
    }

    // CLAUSE: AND click ($BUTTON_TYPE) on confirmation dialog
    public static void whenButtonClickedOnDialog(FxRobot robot, ButtonType buttonType) {
        DialogPane dialogPane = robot.lookup(".dialog-pane").query();
        robot.clickOn(dialogPane.lookupButton(buttonType));
        robot.sleep(SHORT_PAUSE, MILLISECONDS);
    }

    // CLAUSE: node ($NODE_NAME) clicked from Node ($PARENT_NODE)
    public static void whenSubNodeClickedByName(FxRobot robot, Supplier<Node> node, String identifier) {
        robot.clickOn(node.get().lookup(identifier));
    }

    // CLAUSE: expect ($NO_OF_FILES) files in the folder ($DIRECTORY)
    public static void thenExpectNoFilesInDirectory(Path directory, int count) throws IOException {
        Assertions.assertTrue(Files.isDirectory(directory), String.format("%s is not a directory", directory));
        Assertions.assertEquals(count, Files.list(directory).count(), String.format("%s has %d files. Expecting %s files in folder", directory, Files.list(directory).count(), count));
    }

    // CLAUSE: expect ($CONFIGURATION) property ($KEY) is set to ($VALUE)
    public static <T,R> void thenPropertyEqualsValue(T objectToTest, Function<T,R> extractor, R expectation) {
        R actual = extractor.apply(objectToTest);
        Assertions.assertEquals(expectation, actual, String.format("Property value %s does not match expected value %s", actual, expectation));
    }

    // CLAUSE: select entry ($ENTRY_VALUE) from table ($TABLE)
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

    // CLAUSE: Label ($LABEL) has value ($VALUE)
    public static void thenLabelOnActiveFormHasValue(FxRobot fxRobot, String labelId, String value) {
        Label label = fxRobot.lookup(labelId).queryAs(Label.class);
        Assertions.assertEquals(value, label.getText(), String.format("%s did not equal expected value: %s", labelId, value));
    }

    // CLAUSE: Label ($LABEL) matches pattern ($VALUE)
    public static void thenLabelOnActiveFormMatchesPattern(FxRobot fxRobot, String labelId, String pattern) {
        Label label = fxRobot.lookup(labelId).queryAs(Label.class);
        Assertions.assertTrue(label.getText().matches(pattern), String.format("%s did not match expected pattern: %s", labelId, pattern));
    }

    // CLAUSE: then close stage
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

    // CLAUSE: a dialog appears with message like "$MESSAGE_PATTERN"
    public static void thenExpectDialogWithMatchingMessage(FxRobot robot, String regExMessage) {
        DialogPane dialogPane = retryRetrievalForDuration(TestFXConstants.RETRIEVAL_TIMEOUT_MILLIS, () -> robot.lookup(".dialog-pane").queryAs(DialogPane.class));
        Matcher matcher = Pattern.compile(regExMessage).matcher(dialogPane.getContentText());

        Assertions.assertTrue(matcher.find(),
                              "The text in warning dialog was not matched as expected");
    }

    // CLAUSE: extract entries from table ($TABLE)
    public static <T> List<T> thenExtractEntriesFromTable(FxRobot robot, String tblName) {
        TableView<T> tableView = robot.lookup(tblName).queryTableView();
        return tableView.getItems();
    }

    // CLAUSE: window has expected dimensions of ($WIDTH,$HEIGHT)
    public static void thenWindowHasDimensions(Window window, double expWidth, double expHeight) {
        Assertions.assertEquals(expWidth, window.getWidth(),
                                String.format("Width did not have the expected value. Expected value: %f; Actual value: %f", expWidth, window.getWidth()));
        Assertions.assertEquals(expHeight, window.getHeight(),
                                String.format("Height did not have the expected value. Expected value: %f; Actual value: %f", expHeight, window.getHeight()));
    }

    // CLAUSE: ensure node ($NODE) is visible
    public static void thenExpectNodeVisibility(FxRobot robot, String nodeQuery, boolean expectedVisibility) {
        Assertions.assertEquals(expectedVisibility, robot.lookup(nodeQuery).queryAs(Node.class).isVisible(),
                                String.format("Node %s is not visible", nodeQuery));
    }

    // CLAUSE: ensure the line count for file ($FILE_NAME) = ($COUNT)
    public static void thenExpectNumberLinesInFile(Path file, int expectedLineCount) throws IOException {
        Assertions.assertEquals(expectedLineCount, Files.lines(file).count(), String.format("Expected count %d was not found in file %s", expectedLineCount, file));
    }

    // CLAUSE: ensure the file $FILE contains line matching pattern ($FILENAME_PATTERN)
    public static void thenExpectLinePatternInFile(Path file, String regExMessage) throws IOException {
        Assertions.assertTrue(Files.lines(file).anyMatch(l -> Pattern.compile(regExMessage).matcher(l).find()), String.format("Expected pattern '%s' did not match with a line in file %s", regExMessage, file));
    }

    // CLAUSE: ensure the file $FILE does not contains line matching pattern ($FILENAME_PATTERN)
    public static void thenNotExpectLinePatternInFile(Path file, String regExMessage) throws IOException {
        Assertions.assertTrue(Files.lines(file).noneMatch(l -> Pattern.compile(regExMessage).matcher(l).find()), String.format("Expected pattern '%s' unexpectedly matched with a line in file %s", regExMessage, file));
    }

    // CLAUSE: ensure (file|folder) ($FILE) exists
    public static void thenExpectFileExists(Path file) {
        Assertions.assertTrue(Files.exists(file), String.format("File %s does not exist", file));
    }

    // CLAUSE: ensure (file|folder) ($FILE) does not exist
    public static void thenNotExpectFileExists(Path file) {
        Assertions.assertFalse(Files.exists(file), String.format("File %s exists unexpectedly", file));
    }

    // CLAUSE: expect table ($TABLE) has a value matching expectation ($DESCRIPTION)
    public static <T> void thenTableViewHasValuesMatchingExpectation(FxRobot robot, String tableName, Predicate<T> assertionExpression) {
        TableView<T> rowGrid = robot.lookup(tableName).queryAs(TableView.class);
        List<T> rows = rowGrid.getItems();

        Assertions.assertTrue(rows.stream().anyMatch(assertionExpression::test), "None of the rows had the expected value.");
    }

    // CLAUSE: expect table ($TABLE) has no value matching expectation ($DESCRIPTION)
    public static <T> void thenTableViewHasValuesNotMatchingExpectation(FxRobot robot, String tableName, Predicate<T> assertionExpression) {
        TableView<T> rowGrid = robot.lookup(tableName).queryAs(TableView.class);
        List<T> rows = rowGrid.getItems();

        Assertions.assertFalse(rows.stream().anyMatch(assertionExpression::test), "One of the rows had the expected value.");
    }

    // CLAUSE: dialog exception message contains text like "$MESSAGE_PATTERN"
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
