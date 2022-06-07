package am.ejob.backend.common.repository.sms;

import am.ejob.backend.common.model.sms.SmsVerify;
import org.springframework.data.repository.PagingAndSortingRepository;


public interface SmsVerifyRepository extends PagingAndSortingRepository<SmsVerify, String> {

    SmsVerify findByPhoneNumberAndState(String phoneNumber, SmsVerify.State state);

}
