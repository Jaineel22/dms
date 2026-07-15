package com.dms.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MarkNotificationsReadRequest {

    /** If null or empty, all of the current user's unread notifications are marked as read. */
    private List<Long> notificationIds;
}