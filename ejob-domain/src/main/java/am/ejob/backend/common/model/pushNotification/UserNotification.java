package am.ejob.backend.common.model.pushNotification;

import am.ejob.backend.common.model.Name;
import am.ejob.backend.common.model.user.User;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "user_notifications")
public class UserNotification {

    @Id
    private String id;

    @DBRef
    private User user;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime seenAt;

    private NotificationType notificationType;

    private NotificationState notificationState = NotificationState.UNSEEN;

    private String clickActionLink;

    private String icon;

    private List<Name> title;

    private List<Name> description;


    public enum NotificationState {
        SEEN, UNSEEN
    }
}
