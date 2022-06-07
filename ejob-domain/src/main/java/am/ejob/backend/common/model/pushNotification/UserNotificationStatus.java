package am.ejob.backend.common.model.pushNotification;

import am.ejob.backend.common.model.user.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@Data
@Document(collection = "user_notification_status")
public class UserNotificationStatus {

    @Id
    private String id;

    @DBRef
    private User user;

    private boolean isNotificationON;

    public UserNotificationStatus(User user, boolean isNotificationON) {
        this.user = user;
        this.isNotificationON = isNotificationON;
    }

}
