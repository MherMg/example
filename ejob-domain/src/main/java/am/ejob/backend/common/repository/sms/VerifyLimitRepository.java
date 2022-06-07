package am.ejob.backend.common.repository.sms;

import am.ejob.backend.common.model.sms.VerifyLimit;
import org.springframework.data.repository.PagingAndSortingRepository;


public interface VerifyLimitRepository extends PagingAndSortingRepository<VerifyLimit, String> {

    VerifyLimit findByPhoneNumber(String phoneNumber);

}
