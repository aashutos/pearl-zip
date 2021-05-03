/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.pub;

import com.ntak.pearlzip.archive.pub.ProgressMessage;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.InstanceField;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import static com.ntak.pearlzip.archive.constants.LoggingConstants.PROGRESS;
import static java.lang.Thread.MIN_PRIORITY;

@Tag("Excluded")
public class FrmProgressControllerTest {
   private static FrmProgressController controller;
   private static CountDownLatch latch = new CountDownLatch(1);
   private static Stage stage;

   private static Label lblProgress;
   private static ProgressBar barProgress;
   private static Consumer<Stage> mockConsumer;

   /*
        Test cases:
        + Update progress interim
        + Update progress complete
    */

   @BeforeAll
   public static void setUpOnce() throws InterruptedException, NoSuchFieldException {
       try {
           Platform.startup(() -> latch.countDown());
       } catch (Exception e) {
           latch.countDown();
       } finally {
           latch.await();
           CountDownLatch secondLatch = new CountDownLatch(1);
           Platform.runLater(()->{
               stage = new Stage();
               secondLatch.countDown();
           });
           secondLatch.await();

           // Initialise mocks...
           mockConsumer = Mockito.mock(Consumer.class);

           Thread.currentThread().setPriority(MIN_PRIORITY);
       }
   }

   @BeforeEach
   public void setUp() throws NoSuchFieldException {
       controller = new FrmProgressController();

       // Initialise fields...
       lblProgress = new Label();
       barProgress = new ProgressBar();

       // Assign fields...
       InstanceField fieldLblProgress = new InstanceField(FrmProgressController.class.getDeclaredField(
               "lblProgress"), controller);
       fieldLblProgress.set(lblProgress);
       InstanceField fieldBarProgress = new InstanceField(FrmProgressController.class.getDeclaredField(
               "barProgress"),controller);
       fieldBarProgress.set(barProgress);

       controller.initData(stage,new CountDownLatch(1), mockConsumer, 1L);
       barProgress.setProgress(0);
   }

   @Test
   @DisplayName("Test: Consume Progress Increment Update will reflect in the progress bar fields appropriately")
   public void testConsumeProgressUpdate_ProgressIncrement_MatchExpectations() throws InterruptedException {
       final int total = 100;
       for (int i = 1; i < total; i++) {
           controller.consumeUpdate(new ProgressMessage(1L,
                                                        PROGRESS,
                                                        String.format("Test progress message: %d", i),
                                                        1,
                                                        total));
           Thread.sleep(50);
           Assertions.assertEquals(String.format("Test progress message: %d", i), lblProgress.getText(),
                                   String.format("Progress message %d not processed successfully", i));
           Assertions.assertEquals(String.format("%.2f", (double)i/ total),
                                   String.format("%.2f", barProgress.getProgress()),
                                   String.format("Progress for %d was not a successful match", i));
       }
   }

    @Test
    @DisplayName("Test: Consume Process Completed Update will reflect in the progress bar fields appropriately")
    public void testConsumeProgressUpdate_ProcessCompleted_MatchExpectations() throws InterruptedException {
        controller.consumeUpdate(new ProgressMessage(1L,
                                                     PROGRESS,
                                                     "Test progress message",
                                                     0,
                                                     ProgressIndicator.INDETERMINATE_PROGRESS));
        Thread.sleep(100);
        Assertions.assertEquals("Test progress message", lblProgress.getText(),
                                "Progress increment message was not processed successfully");

        controller.consumeUpdate(new ProgressMessage(1L,
                                                     PROGRESS,
                                                     "Test completed message",
                                                     1,
                                                     -1));
        Thread.sleep(100);
        Assertions.assertEquals("Test completed message", lblProgress.getText(),
                                "Completed message was not processed successfully");
        Assertions.assertEquals("-1.00",
                                String.format("%.2f", barProgress.getProgress()),
                                "Progress was not ");
    }
}
