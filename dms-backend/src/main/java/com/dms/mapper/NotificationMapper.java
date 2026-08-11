package com.dms.mapper;

import com.dms.dto.response.NotificationResponse;
import com.dms.dto.response.NotificationSummaryResponse;
import com.dms.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userFullName", expression = "java(notification.getUser().getFirstName() + \" \" + notification.getUser().getLastName())")
    NotificationResponse toResponse(Notification notification);

    List<NotificationResponse> toResponseList(List<Notification> notifications);

    default NotificationSummaryResponse toNotificationSummaryResponse(
            long totalUnreadCount,
            Map<String, Long> unreadByPriority,
            Map<String, Long> unreadByType,
            List<Notification> recentNotifications) {
        return NotificationSummaryResponse.builder()
                .totalUnreadCount(totalUnreadCount)
                .unreadByPriority(unreadByPriority)
                .unreadByType(unreadByType)
                .recentNotifications(toResponseList(recentNotifications))
                .build();
    }
}