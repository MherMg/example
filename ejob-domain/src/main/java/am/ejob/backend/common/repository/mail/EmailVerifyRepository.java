package am.ejob.backend.common.repository.mail;

import am.ejob.backend.common.model.mail.EmailVerify;
import am.ejob.backend.common.model.mail.State;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface EmailVerifyRepository extends PagingAndSortingRepository<EmailVerify, String> {

    EmailVerify findByEmailIgnoreCaseAndState(String email, State state);

}
