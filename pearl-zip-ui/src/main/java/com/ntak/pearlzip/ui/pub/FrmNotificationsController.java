/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.pub;

import com.ntak.pearlzip.ui.util.JFXUtil;
import com.ntak.pearlzip.ui.util.NotificationEntry;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.ntak.pearlzip.ui.constants.ZipConstants.PRIMARY_EXECUTOR_SERVICE;

public class FrmNotificationsController {
    @FXML
    private TableView<NotificationEntry> tblNotifications;

    @FXML
    private TableColumn<NotificationEntry,String> colTopic;
    @FXML
    private TableColumn<NotificationEntry,String> colMessage;

    @FXML
    public void initialize() {
        colTopic.setCellValueFactory((s) -> new SimpleObjectProperty<>(s.getValue().topic()));
        colMessage.setCellValueFactory((s) -> new SimpleObjectProperty<>(s.getValue().message()));

        AtomicReference<List<NotificationEntry>> entries = new AtomicReference<>();
        PRIMARY_EXECUTOR_SERVICE.submit(()->
          {
            entries.set(JFXUtil.getNotifications("PearlZip Alert", "PearlZip Information Message"));
            JFXUtil.runLater(()->{
                tblNotifications.getItems().addAll(entries.get());
                tblNotifications.setItems(new SortedList<>(tblNotifications.getItems(),
                                                           Comparator.comparingInt(NotificationEntry::id).reversed())
                );
                tblNotifications.refresh();
            });
          }
        );
    }
}
