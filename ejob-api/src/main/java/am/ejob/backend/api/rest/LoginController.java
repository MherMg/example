package am.ejob.backend.api.rest;

import am.ejob.backend.api.rest.response.ResponseInfo;
import am.ejob.backend.api.rest.vo.AuthorizationVO;
import am.ejob.backend.api.rest.vo.UserVO;
import am.ejob.backend.api.security.JwtTokenUtil;
import am.ejob.backend.api.service.NikitaMobileService;
import am.ejob.backend.api.service.SmsService;
import am.ejob.backend.api.service.UserService;
import am.ejob.backend.api.util.RandomStringGenerator;
import am.ejob.backend.common.model.sms.SmsVerify;
import am.ejob.backend.common.model.sms.VerifyLimit;
import am.ejob.backend.common.model.Language;
import am.ejob.backend.common.model.user.PersonalType;
import am.ejob.backend.common.model.user.User;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static am.ejob.backend.api.rest.response.ResponseMessage.*;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpStatus.*;


@RestController
@CrossOrigin
@RequestMapping("/api/public/auth/")
public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @Value("${sms.link.expire.timeout.sec}")
    private int smsLinkExpireTimeoutSec;

    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;
    private final SmsService smsService;
    private final NikitaMobileService nikitaMobileService;

    public LoginController(
            UserService userService,
            JwtTokenUtil jwtTokenUtil,
            SmsService smsService,
            NikitaMobileService nikitaMobileService
    ) {
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.smsService = smsService;
        this.nikitaMobileService = nikitaMobileService;
    }

    public static class GenerateCodeRequest {
        public String phoneNumber;
    }

    public static class RegisterRequest {
        public String name;
        public String phoneNumber;
        public String code;
        public User.UserType type;
        public PersonalType personalType;
        public Language language;
    }

    @AllArgsConstructor
    public static class RegisterResponse {
        public UserVO user;
        public AuthorizationVO authorization;
    }

    public static class VerifyCodeRequest {
        public String code;
        public String phoneNumber;
    }

    private AuthorizationVO generateLoginResponse(String phoneNumber) {
        return new AuthorizationVO(jwtTokenUtil.generateToken(phoneNumber), "Bearer");
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "PHONE_NUMBER_IS_NULL, BLOCKED_FOR_12_HOURS", response = ResponseInfo.class),
            @ApiResponse(code = 409, message = "BLOCKED_FOR_12_HOURS", response = ResponseInfo.class),
            @ApiResponse(code = 200, message = "VERIFICATION_CODE_SENT", response = ResponseInfo.class)
    })
    @PostMapping("/code")
    public HttpEntity<?> getSmsCode(@RequestBody GenerateCodeRequest request) {
        if (isBlank(request.phoneNumber)) {
            return ResponseEntity.badRequest().body(ResponseInfo.createResponse(BAD_REQUEST, PHONE_NUMBER_IS_NULL));
        }
        VerifyLimit verifyLimit = smsService.findVerifyLimitByPhoneNumber(request.phoneNumber);
        if (verifyLimit != null) {
            if (verifyLimit.getBlockedDate() != null && LocalDateTime.now().isBefore(verifyLimit.getBlockedDate())) {
                return ResponseEntity.status(409).body(ResponseInfo.createResponse(BAD_REQUEST, BLOCKED_FOR_12_HOURS));
            }
        }
        SmsVerify smsVerifySent = smsService.findSmsVerifyByPhoneNumberAndSent(request.phoneNumber);
        if (smsVerifySent != null && smsVerifySent.getState() == SmsVerify.State.SENT) {
            smsService.setVerifyStateProcessed(smsVerifySent);
            smsService.updateVerifyLimit(request.phoneNumber);

        }
        String code = RandomStringGenerator.randomAlphaNumeric(6);
        smsService.createSmsVerify(request.phoneNumber, code);
        nikitaMobileService.sendMessage(request.phoneNumber, code);

        return ResponseEntity.ok(ResponseInfo.createResponse(OK, VERIFICATION_CODE_SENT));
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "CODE_IS_EMPTY, PHONE_NUMBER_IS_NULL, CODE_IS_NULL, CODE_DOES_NOT_MATCH, USER_DOES_NOT_EXISTS", response = ResponseInfo.class),
            @ApiResponse(code = 409, message = "CODE_EXPIRED, BLOCKED_FOR_12_HOURS", response = ResponseInfo.class),
            @ApiResponse(code = 200, message = "NEW_VERIFICATION_CODE_SENT", response = ResponseInfo.class),
            @ApiResponse(code = 200, message = "", response = AuthorizationVO.class)
    })
    @PostMapping("/login")
    public HttpEntity<?> login(@RequestBody VerifyCodeRequest request) {

        if (isBlank(request.code)) {
            return ResponseEntity.badRequest().body(ResponseInfo.createResponse(BAD_REQUEST, CODE_IS_EMPTY));
        }

        if (isBlank(request.phoneNumber)) {
            return ResponseEntity.badRequest().body(ResponseInfo.createResponse(BAD_REQUEST, PHONE_NUMBER_IS_NULL));
        }

        SmsVerify smsVerify = smsService.findSmsVerifyByPhoneNumberAndSent(request.phoneNumber);
        if (smsVerify == null) {
            return ResponseEntity.badRequest().body(ResponseInfo.createResponse(BAD_REQUEST, CODE_IS_NULL));
        }

        if ((System.currentTimeMillis() - smsVerify.getCreatedAt().atZone(ZoneOffset.UTC).toInstant().toEpochMilli()) > (smsLinkExpireTimeoutSec * 1000)) {
            return ResponseEntity.status(409).body(ResponseInfo.createResponse(CONFLICT, CODE_EXPIRED));
        }

        if (!request.code.equals(smsVerify.getCode())) {
            if (smsVerify.getCount() == 3) {
                smsService.setVerifyStateProcessed(smsVerify);
                VerifyLimit verifyLimit = smsService.updateVerifyLimit(request.phoneNumber);
                if (verifyLimit.getCount() == 5) {
                    return ResponseEntity.status(409).body(ResponseInfo.createResponse(CONFLICT, BLOCKED_FOR_12_HOURS));
                } else {
                    request.code = RandomStringGenerator.randomAlphaNumeric(6);
                    smsService.createSmsVerify(request.phoneNumber, request.code);
                    nikitaMobileService.sendMessage(request.phoneNumber, request.code);
                    return ResponseEntity.ok().body(ResponseInfo.createResponse(OK, NEW_VERIFICATION_CODE_SENT));
                }
            } else {
                smsService.addCountToSmsVerify(smsVerify);
                return ResponseEntity.badRequest().body(ResponseInfo.createResponse(BAD_REQUEST, CODE_DOES_NOT_MATCH));
            }
        }

        boolean userExists = userService.userExists(request.phoneNumber);
        if (!userExists) {
            smsService.setVerifyStateProcessed(smsVerify);
            return ResponseEntity.badRequest().body(ResponseInfo.createResponse(BAD_REQUEST, USER_DOES_NOT_EXISTS));
        } else {
            smsService.setVerifyStateProcessed(smsVerify);
            smsService.updateLimitCount(request.phoneNumber);
            return ResponseEntity.status(200).body(generateLoginResponse(request.phoneNumber));
        }
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "NAME_IS_NULL, PHONE_NUMBER_IS_NULL, USER_TYPE_IS_NULL, CODE_IS_NULL,REGISTER_USER_ALREADY_EXISTS, CODE_DOES_NOT_MATCH", response = ResponseInfo.class),
            @ApiResponse(code = 409, message = "CODE_EXPIRED, BLOCKED_FOR_12_HOURS", response = ResponseInfo.class),
            @ApiResponse(code = 200, message = "NEW_VERIFICATION_CODE_SENT", response = ResponseInfo.class),
            @ApiResponse(code = 200, message = "", response = UserVO.class)
    })
    @PostMapping("/register")
    public HttpEntity<?> registration(@RequestBody RegisterRequest request) {

        if (isBlank(request.name)) {
            return ResponseEntity.badRequest().body(ResponseInfo.createResponse(BAD_REQUEST, NAME_IS_NULL));
        }

        if (isBlank(request.phoneNumber)) {
            return ResponseEntity.badRequest().body(ResponseInfo.createResponse(BAD_REQUEST, PHONE_NUMBER_IS_NULL));
        }
        boolean userExists = userService.userExists(request.phoneNumber);
        if (userExists) {
            return ResponseEntity.badRequest().body(ResponseInfo.createResponse(BAD_REQUEST, REGISTER_USER_ALREADY_EXISTS));
        }
        if (request.type == null) {
            return ResponseEntity.badRequest().body(ResponseInfo.createResponse(BAD_REQUEST, USER_TYPE_IS_NULL));
        }

        SmsVerify smsVerify = smsService.findSmsVerifyByPhoneNumberAndSent(request.phoneNumber);
        if (smsVerify == null) {
            return ResponseEntity.badRequest().body(ResponseInfo.createResponse(BAD_REQUEST, CODE_IS_NULL));
        }

        if ((System.currentTimeMillis() - smsVerify.getCreatedAt().atZone(ZoneOffset.UTC).toInstant().toEpochMilli()) > (smsLinkExpireTimeoutSec * 1000)) {
            return ResponseEntity.status(409).body(ResponseInfo.createResponse(CONFLICT, CODE_EXPIRED));
        }

        if (!request.code.equals(smsVerify.getCode())) {
            if (smsVerify.getCount() == 3) {
                smsService.setVerifyStateProcessed(smsVerify);
                VerifyLimit verifyLimit = smsService.updateVerifyLimit(request.phoneNumber);
                if (verifyLimit.getCount() == 5) {
                    return ResponseEntity.status(409).body(ResponseInfo.createResponse(CONFLICT, BLOCKED_FOR_12_HOURS));
                } else {
                    request.code = RandomStringGenerator.randomAlphaNumeric(6);
                    smsService.createSmsVerify(request.phoneNumber, request.code);
                    nikitaMobileService.sendMessage(request.phoneNumber, request.code);
                    return ResponseEntity.ok().body(ResponseInfo.createResponse(OK, NEW_VERIFICATION_CODE_SENT));
                }
            } else {
                smsService.addCountToSmsVerify(smsVerify);
                return ResponseEntity.badRequest().body(ResponseInfo.createResponse(BAD_REQUEST, CODE_DOES_NOT_MATCH));
            }
        }

        User user = userService.createUser(request.name, request.phoneNumber, request.type, request.personalType, request.language);
        smsService.setVerifyStateProcessed(smsVerify);

        smsService.updateLimitCount(request.phoneNumber);


        return ResponseEntity.ok((new RegisterResponse(new UserVO(user), generateLoginResponse(user.getPhoneNumber()))));
    }


}
