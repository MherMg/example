package am.ejob.backend.api.service;

import am.ejob.backend.common.model.pushNotification.UserNotificationStatus;
import am.ejob.backend.common.model.pushNotification.UserNotificationToken;
import am.ejob.backend.common.model.Language;
import am.ejob.backend.common.model.user.LastLogin;
import am.ejob.backend.common.model.user.PersonalType;
import am.ejob.backend.common.model.user.User;
import am.ejob.backend.common.repository.pushNotification.UserNotificationStatusRepository;
import am.ejob.backend.common.repository.pushNotification.UserNotificationTokenRepository;
import am.ejob.backend.common.repository.user.LastLoginRepository;
import am.ejob.backend.common.repository.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class UserService {


    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final UserNotificationTokenRepository userNotificationTokenRepository;
    private final UserNotificationStatusRepository userNotificationStatusRepository;
    private final LastLoginRepository lastLoginRepository;


    @Autowired
    public UserService(
            UserRepository userRepository,
            UserNotificationTokenRepository userNotificationTokenRepository,
            UserNotificationStatusRepository userNotificationStatusRepository,
            LastLoginRepository lastLoginRepository
    ) {
        this.userRepository = userRepository;
        this.userNotificationTokenRepository = userNotificationTokenRepository;
        this.lastLoginRepository = lastLoginRepository;
        this.userNotificationStatusRepository = userNotificationStatusRepository;
    }

    public boolean userExists(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    public User createUser(
            String name,
            String phoneNumber,
            User.UserType type,
            PersonalType personalType,
            Language language
    ) {
        User newUser = new User();
        newUser.setName(name);
        newUser.setPhoneNumber(phoneNumber);
        newUser.setUserType(type);
        newUser.setCratedAt(LocalDateTime.now(ZoneOffset.UTC));
        newUser.setLanguage(language);
        User savedUser = userRepository.save(newUser);
        userNotificationStatusRepository.save(new UserNotificationStatus(savedUser, true));
        setUserOnline(savedUser, true);
        return savedUser;
    }


    public User getCurrentUser() {
        Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();
        if (currentUser == null) {
            throw new AccessDeniedException("Current user is not found");
        }
        String phoneNumber = currentUser.getName();
        User user = loadUserByPhoneNumber(phoneNumber);
        if (user == null) {
            log.info("current user not found");
        }
        return user;
    }

    public User loadUserByPhoneNumber(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber);
        if (user == null) {
            log.warn("User [email:{}] is not found", phoneNumber);
            return null;
        }
        return user;
    }


    public UserNotificationToken createUserNotification(User user, String token) {
        UserNotificationToken userNotificationToken = new UserNotificationToken();
        userNotificationToken.setUser(user);
        userNotificationToken.setToken(token);
        userNotificationToken.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        return userNotificationTokenRepository.save(userNotificationToken);
    }

    public UserNotificationToken findUserNotificationByUser(User user) {
        return userNotificationTokenRepository.findByUser(user);
    }

    public void updateUserNotification(UserNotificationToken userNotificationToken, String token) {
        userNotificationToken.setToken(token);
        userNotificationTokenRepository.save(userNotificationToken);
    }

    public void setUserOnline(User currentUser, boolean online) {
        LastLogin lastLogin = lastLoginRepository.findByUser(currentUser);
        if (lastLogin == null) {
            lastLogin = new LastLogin();
            lastLogin.setUser(currentUser);
        }
        lastLogin.setOnline(online);
        lastLogin.setOnlineAt(LocalDateTime.now());

        lastLoginRepository.save(lastLogin);
    }

    public LastLogin getUserLastLogin(User user) {
        return lastLoginRepository.findByUser(user);
    }
}
