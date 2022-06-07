package am.ejob.backend.common.repository.user;

import am.ejob.backend.common.model.user.LastLogin;
import am.ejob.backend.common.model.user.User;
import org.springframework.data.repository.PagingAndSortingRepository;


public interface LastLoginRepository extends PagingAndSortingRepository<LastLogin, String> {


    LastLogin findByUser(User user);
}
