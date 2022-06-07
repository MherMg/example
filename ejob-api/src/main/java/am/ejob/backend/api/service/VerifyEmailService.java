package am.ejob.backend.api.service;

import am.ejob.backend.common.model.mail.EmailVerify;
import am.ejob.backend.common.model.mail.State;
import am.ejob.backend.common.repository.mail.EmailVerifyRepository;
import org.springframework.stereotype.Service;

@Service
public class VerifyEmailService {

    private final EmailVerifyRepository emailVerifyRepository;

    public VerifyEmailService(EmailVerifyRepository emailVerifyRepository) {
        this.emailVerifyRepository = emailVerifyRepository;
    }

    public void saveOrUpdateEmailVerifyRequest(EmailVerify emailVerify) {
        emailVerifyRepository.save(emailVerify);
    }

    public EmailVerify getEmailVerifyRequestByEmail(String email) {
        return emailVerifyRepository.findByEmailIgnoreCaseAndState(email, State.SENT);
    }

}
