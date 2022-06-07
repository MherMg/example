package am.ejob.backend.common.model.pushNotification;

import am.ejob.backend.common.model.user.User;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "user_notification_token")
public class UserNotificationToken {

    @Id
    private String id;

    @DBRef
    private User user;

    private String token;

    private LocalDateTime createdAt;


}
