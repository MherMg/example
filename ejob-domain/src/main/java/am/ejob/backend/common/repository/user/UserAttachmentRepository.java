package am.ejob.backend.common.repository.user;

import am.ejob.backend.common.model.user.User;
import am.ejob.backend.common.model.user.UserAttachment;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface UserAttachmentRepository extends PagingAndSortingRepository<UserAttachment, String> {
    UserAttachment findByUser (User user);

}
