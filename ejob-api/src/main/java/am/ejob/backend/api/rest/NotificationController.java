package am.ejob.backend.api.rest;

import am.ejob.backend.api.config.PushNotify;
import am.ejob.backend.api.rest.response.ResponseInfo;
import am.ejob.backend.api.rest.vo.NotificationVO;
import am.ejob.backend.api.service.FcmService;
import am.ejob.backend.api.service.NotificationService;
import am.ejob.backend.api.service.UserAttachmentService;
import am.ejob.backend.api.service.UserService;
import am.ejob.backend.common.model.pushNotification.UserNotification;
import am.ejob.backend.common.model.pushNotification.UserNotificationStatus;
import am.ejob.backend.common.model.pushNotification.UserNotificationToken;
import am.ejob.backend.common.model.user.User;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static am.ejob.backend.api.rest.response.ResponseMessage.*;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@CrossOrigin
@RequestMapping("/api/")
public class NotificationController {
    private final FcmService fcmService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final UserAttachmentService userAttachmentService;


    @Autowired
    public NotificationController(
            FcmService fcmClient,
            UserService userService,
            NotificationService notificationService,
            UserAttachmentService userAttachmentService
    ) {
        this.fcmService = fcmClient;
        this.userService = userService;
        this.notificationService = notificationService;
        this.userAttachmentService = userAttachmentService;
    }

    @Data
    public static class NotificationRequest {
        PushNotify conf;
        String clientToken;
    }

    public static class UserNotificationRequest {
        public String token;
    }

    public static class UserNotificationStatusRequest {
        public boolean isNotificationON;
    }

    @AllArgsConstructor
    public static class UserNotificationStatusResponse {
        public String userId;
        public boolean isNotificationON;
    }

    @AllArgsConstructor
    public static class UserNotificationResponse {
        public String token;
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "TOKEN_IS_EMPTY", response = ResponseInfo.class),
            @ApiResponse(code = 200, message = "", response = String.class)
    })
    @ApiOperation(value = "", authorizations = {@Authorization(value = "Bearer")})
    @PostMapping("public/send-notification")
    public HttpEntity<?> sendNotification(@RequestBody NotificationRequest request) throws ExecutionException, InterruptedException {
        if (isBlank(request.clientToken)) {
            return ResponseEntity.badRequest().body(ResponseInfo.createResponse(BAD_REQUEST, TOKEN_IS_EMPTY));
        }
        String s = fcmService.sendPersonal(request.conf, request.clientToken);
        return ResponseEntity.ok().body(s);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "TOKEN_IS_EMPTY,USER_NOTIFICATION_ALREADY_EXISTS", response = ResponseInfo.class),
            @ApiResponse(code = 200, message = "", response = UserNotificationResponse.class)
    })
    @ApiOperation(value = "", authorizations = {@Authorization(value = "Bearer")})
    @PostMapping("private/notification/token")
    public HttpEntity<?> createUserNotificationToken(@RequestBody UserNotificationRequest request) {
        if (isBlank(request.token)) {
            return ResponseEntity.badRequest().body(ResponseInfo.createResponse(BAD_REQUEST, TOKEN_IS_EMPTY));
        }
        User currentUser = userService.getCurrentUser();
        UserNotificationToken userNotificationToken = userService.findUserNotificationByUser(currentUser);
        if (userNotificationToken != null) {
            userService.updateUserNotification(userNotificationToken, request.token);
            return ResponseEntity.status(200).body(new UserNotificationResponse(userNotificationToken.getToken()));
        }
        UserNotificationToken newUserNotificationToken = userService.createUserNotification(currentUser, request.token);

        return ResponseEntity.ok().body(new UserNotificationResponse(newUserNotificationToken.getToken()));
    }

    @ApiResponses({
            @ApiResponse(code = 404, message = "USER_NOT_FOUND,USER_NOTIFICATION_NOT_FOUND", response = ResponseInfo.class),
            @ApiResponse(code = 200, message = "", response = UserNotificationResponse.class)
    })
    @ApiOperation(value = "", authorizations = {@Authorization(value = "Bearer")})
    @GetMapping("private/notification/token")
    public HttpEntity<?> getUserNotificationToken() {
        User currentUser = userService.getCurrentUser();

        UserNotificationToken userNotificationToken = userService.findUserNotificationByUser(currentUser);
        if (userNotificationToken == null) {
            return ResponseEntity.status(404).body(ResponseInfo.createResponse(NOT_FOUND, USER_NOTIFICATION_NOT_FOUND));
        }

        return ResponseEntity.ok().body(new UserNotificationResponse(userNotificationToken.getToken()));
    }


    @ApiOperation(value = "", authorizations = {@Authorization(value = "Bearer")})
    @GetMapping("private/notification/user")
    public HttpEntity<?> getUserNotifications() {
        User currentUser = userService.getCurrentUser();

        List<UserNotification> lastUserNotifications = notificationService.getLastNotifications(currentUser);


        List<NotificationVO> response = new ArrayList<>();
        lastUserNotifications.forEach(userNotification -> {
            byte[] icon = userAttachmentService.getUserAvatar(userNotification.getUser());

            response.add(new NotificationVO(userNotification, icon));

        });

        return ResponseEntity.ok().body(response);
    }

    @ApiOperation(value = "", authorizations = {@Authorization(value = "Bearer")})
    @GetMapping("private/notification/user/count")
    public HttpEntity<?> getUserNotificationsCount() {
        User currentUser = userService.getCurrentUser();

        return ResponseEntity.ok().body(notificationService.getNotificationCount(currentUser));
    }


    @ApiOperation(value = "", authorizations = {@Authorization(value = "Bearer")})
    @PostMapping("private/notification/{notificationId}/seen")
    public HttpEntity<?> updateUserNotificationSeen(@PathVariable("notificationId") String id) {
        User currentUser = userService.getCurrentUser();

        Optional<UserNotification> notification = notificationService.getNotificationById(id);
        if (notification.isEmpty()) {
            return ResponseEntity.status(404).body(ResponseInfo.createResponse(NOT_FOUND, USER_NOTIFICATION_NOT_FOUND));
        }
        if (!currentUser.getId().equals(notification.get().getUser().getId())) {
            return ResponseEntity.status(403).build();
        }
        notificationService.update(notification.get());
        return ResponseEntity.ok().build();
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "", response = UserNotificationStatusResponse.class)
    })
    @ApiOperation(value = "", authorizations = {@Authorization(value = "Bearer")})
    @PostMapping("private/notification/status")
    public HttpEntity<?> setUserNotificationStatus(@RequestBody UserNotificationStatusRequest request) {
        User currentUser = userService.getCurrentUser();
        UserNotificationStatus notificationStatus = notificationService.updateUserNotificationStatus(
                currentUser, request.isNotificationON);

        return ResponseEntity.ok().body(new UserNotificationStatusResponse(notificationStatus.getUser().getId(),
                notificationStatus.isNotificationON()));
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "NOTIFICATION_STATUS_IS_NULL", response = ResponseInfo.class),
            @ApiResponse(code = 200, message = "", response = UserNotificationStatusResponse.class)
    })
    @ApiOperation(value = "", authorizations = {@Authorization(value = "Bearer")})
    @GetMapping("private/notification/status")
    public HttpEntity<?> getUserNotificationStatusInfo() {
        User currentUser = userService.getCurrentUser();
        UserNotificationStatus notificationStatus = notificationService.findUserNotificationStatus(currentUser);
        if (notificationStatus == null) {
            return ResponseEntity.badRequest().body(ResponseInfo.createResponse(BAD_REQUEST, NOTIFICATION_STATUS_IS_NULL));
        }

        return ResponseEntity.ok().body(new UserNotificationStatusResponse(notificationStatus.getUser().getId(),
                notificationStatus.isNotificationON()));
    }
}
