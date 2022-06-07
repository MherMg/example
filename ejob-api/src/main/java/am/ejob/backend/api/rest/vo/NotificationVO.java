package am.ejob.backend.api.rest.vo;

import am.ejob.backend.common.model.pushNotification.NotificationType;
import am.ejob.backend.common.model.pushNotification.UserNotification;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data

public class NotificationVO {

    public String id;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    public LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    public LocalDateTime seenAt;

    public NotificationType notificationType;

    public UserNotification.NotificationState notificationState;

    public String clickActionLink;

    public byte[] icon;


    public NotificationVO(UserNotification userNotification, byte[] icon) {
        this.id = userNotification.getId();
        this.createdAt = userNotification.getCreatedAt();
        this.seenAt = userNotification.getSeenAt();
        this.notificationType = userNotification.getNotificationType();
        this.notificationState = userNotification.getNotificationState();
        this.clickActionLink = userNotification.getClickActionLink();
        this.icon = icon;
    }
}
