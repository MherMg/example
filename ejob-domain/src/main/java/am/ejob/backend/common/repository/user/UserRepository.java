package am.ejob.backend.common.repository.user;

import am.ejob.backend.common.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface UserRepository extends PagingAndSortingRepository<User, String> {

    User findByPhoneNumber(String phoneNumber);

    Optional<User> findByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    User findByIdAndUserType(String id, User.UserType type);

    Page<User> findAllByUserType(User.UserType userType, Pageable pageable);
}
