/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.util;

import java.time.LocalDateTime;

public record NotificationEntry(int id, String topic, String message,
                                LocalDateTime creationTimestamp) {
    @Override
    public String toString() {
        return "NotificationEntry{" +
                "id=" + id +
                ", topic='" + topic + '\'' +
                ", message='" + message + '\'' +
                ", creationTimestamp=" + creationTimestamp +
                '}';
    }
}
