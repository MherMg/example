package am.ejob.backend.common.repository.pushNotification;

import am.ejob.backend.common.model.pushNotification.UserNotificationToken;
import am.ejob.backend.common.model.user.User;
import org.springframework.data.repository.PagingAndSortingRepository;


public interface UserNotificationTokenRepository extends PagingAndSortingRepository<UserNotificationToken, String> {

    UserNotificationToken findByUser(User user);


}
