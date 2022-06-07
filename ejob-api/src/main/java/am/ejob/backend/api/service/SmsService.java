package am.ejob.backend.api.service;

import am.ejob.backend.common.model.sms.SmsVerify;
import am.ejob.backend.common.model.sms.VerifyLimit;
import am.ejob.backend.common.repository.sms.SmsVerifyRepository;
import am.ejob.backend.common.repository.sms.VerifyLimitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static am.ejob.backend.common.model.sms.SmsVerify.State.PROCESSED;
import static am.ejob.backend.common.model.sms.SmsVerify.State.SENT;

@Service
public class SmsService {
    private final SmsVerifyRepository smsVerifyRepository;
    private final VerifyLimitRepository verifyLimitRepository;

    @Autowired
    public SmsService(
            SmsVerifyRepository smsVerifyRepository,
            VerifyLimitRepository verifyLimitRepository
    ) {
        this.smsVerifyRepository = smsVerifyRepository;
        this.verifyLimitRepository = verifyLimitRepository;
    }

    public SmsVerify findSmsVerifyByPhoneNumberAndSent(String phoneNumber) {
        return smsVerifyRepository.findByPhoneNumberAndState(phoneNumber, SENT);
    }

    public void createSmsVerify(String phoneNumber, String code) {
        SmsVerify smsVerify = new SmsVerify();
        smsVerify.setPhoneNumber(phoneNumber);
        smsVerify.setCode(code);
        smsVerify.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        smsVerify.setState(SENT);
        smsVerifyRepository.save(smsVerify);
    }

    public void setVerifyStateProcessed(SmsVerify smsVerify) {
        smsVerify.setState(PROCESSED);
        smsVerifyRepository.save(smsVerify);
    }

    public VerifyLimit findVerifyLimitByPhoneNumber(String phoneNumber) {
        return verifyLimitRepository.findByPhoneNumber(phoneNumber);
    }

    public void addCountToSmsVerify(SmsVerify smsVerify) {
        int count = smsVerify.getCount();
        count++;
        smsVerify.setCount(count);
        smsVerifyRepository.save(smsVerify);
    }

    public VerifyLimit createVerifyLimit(String phoneNumber) {
        VerifyLimit verifyLimit = new VerifyLimit();
        verifyLimit.setPhoneNumber(phoneNumber);
        verifyLimit.setCount(1);
        return verifyLimitRepository.save(verifyLimit);
    }

    public VerifyLimit updateVerifyLimit(String phoneNumber) {
        VerifyLimit limit = findVerifyLimitByPhoneNumber(phoneNumber);
        if (limit == null) {
            return createVerifyLimit(phoneNumber);
        }
        if (limit.getCount() == 5) {
            limit.setCount(0);
            limit.setBlockedDate(null);
            verifyLimitRepository.save(limit);
        }
        if (limit.getCount() < 5) {
            limit.setCount(limit.getCount() + 1);
            verifyLimitRepository.save(limit);
            if (limit.getCount() == 5) {
                limit.setBlockedDate(LocalDateTime.now().plusHours(12));
                verifyLimitRepository.save(limit);
            }
        }
        return limit;
    }

    public void updateLimitCount(String phoneNumber) {
        VerifyLimit verifyLimit = findVerifyLimitByPhoneNumber(phoneNumber);
        if (verifyLimit != null) {
            verifyLimit.setCount(0);
            verifyLimit.setBlockedDate(null);
            verifyLimitRepository.save(verifyLimit);
        }
    }
}
