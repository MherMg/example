package am.ejob.backend.common.repository.pushNotification;

import am.ejob.backend.common.model.pushNotification.UserNotificationStatus;
import am.ejob.backend.common.model.user.User;
import org.springframework.data.repository.PagingAndSortingRepository;


public interface UserNotificationStatusRepository extends PagingAndSortingRepository<UserNotificationStatus, String> {

    UserNotificationStatus findByUser(User user);


}
