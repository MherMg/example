package am.ejob.backend.common.repository.pushNotification;

import am.ejob.backend.common.model.pushNotification.NotificationType;
import am.ejob.backend.common.model.pushNotification.UserNotification;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;


public interface UserNotificationRepository extends PagingAndSortingRepository<UserNotification, String> {

    List<UserNotification> findAllByUserIdAndNotificationState(String userId, UserNotification.NotificationState notificationState);

    List<UserNotification> findTop5ByUserIdAndNotificationStateAndNotificationTypeInOrderByCreatedAtDesc(String userId, UserNotification.NotificationState notificationState, List<NotificationType> types);
}
