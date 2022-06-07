package am.ejob.backend.api.service;

import am.ejob.backend.api.config.PushNotify;
import am.ejob.backend.common.model.Language;
import am.ejob.backend.common.model.Name;
import am.ejob.backend.common.model.pushNotification.NotificationType;
import am.ejob.backend.common.model.pushNotification.UserNotification;
import am.ejob.backend.common.model.pushNotification.UserNotificationStatus;
import am.ejob.backend.common.model.pushNotification.UserNotificationToken;
import am.ejob.backend.common.model.user.User;
import am.ejob.backend.common.repository.pushNotification.UserNotificationRepository;
import am.ejob.backend.common.repository.pushNotification.UserNotificationStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
public class NotificationService {

    private final UserNotificationRepository userNotificationRepository;
    private final UserService userService;
    private final FcmService fcmService;
    private final UserNotifyService userNotifyService;
    private final UserNotificationStatusRepository userNotificationStatusRepository;

    @Autowired
    public NotificationService(
            UserNotificationRepository userNotificationRepository,
            UserService userService,
            FcmService fcmService,
            UserNotifyService userNotifyService,
            UserNotificationStatusRepository userNotificationStatusRepository) {
        this.userNotificationRepository = userNotificationRepository;
        this.userService = userService;
        this.fcmService = fcmService;
        this.userNotifyService = userNotifyService;
        this.userNotificationStatusRepository = userNotificationStatusRepository;
    }

    @Async
    public void createForOffer(
            User user,
            NotificationType notificationType,
            String idForAction,
            String responsePrice
    ) {
        String actionLink;
        actionLink = "http://localhost:8080/offers?offerId=" + idForAction;

        String armTitle = "Նոր հայտարարություն";
        String enTitle = "New offer";
        String ruTitle = "Новое предложение";

        String armDescription = "Գինը - " + responsePrice + " դրամ";
        String enDescription = "Price - " + responsePrice + " AMD";
        String ruDescription = "Цена - " + responsePrice + " драм";

        List<Name> titles = new ArrayList<>();
        titles.add(new Name(enTitle, Language.EN));
        titles.add(new Name(ruTitle, Language.RU));
        titles.add(new Name(armTitle, Language.ARM));

        List<Name> descriptions = new ArrayList<>();
        descriptions.add(new Name(enDescription, Language.EN));
        descriptions.add(new Name(ruDescription, Language.RU));
        descriptions.add(new Name(armDescription, Language.ARM));

        UserNotification userNotification = new UserNotification();
        userNotification.setUser(user);
        userNotification.setNotificationType(notificationType);
        userNotification.setClickActionLink(actionLink);
        userNotification.setTitle(titles);
        userNotification.setDescription(descriptions);
        userNotificationRepository.save(userNotification);

        switch (user.getLanguage()) {
            case ARM:
                pushNotification(user, actionLink, armTitle, armDescription);
                break;
            case EN:
                pushNotification(user, actionLink, enTitle, enDescription);
                break;
            case RU:
                pushNotification(user, actionLink, ruTitle, ruDescription);
                break;
        }
    }

    @Async
    public void createForOfferToSpecialist(
            User employee,
            NotificationType notificationType,
            String idForAction,
            String responsePrice,
            User author
    ) {
        String actionLink;
        actionLink = "https://localhost:8080/offers?offerId=" + idForAction;

        String armTitle = author.getName() + "ը առաջարկում է արձագանքել իր հայտարարությանը";
        String enTitle = author.getName() + " makes you an offer";
        String ruTitle = "Новое предложение от " + author.getName();

        String armDescription = "Գինը - " + responsePrice + " դրամ";
        String enDescription = "Price - " + responsePrice + " AMD";
        String ruDescription = "Цена - " + responsePrice + " драм";

        List<Name> titles = new ArrayList<>();
        titles.add(new Name(enTitle, Language.EN));
        titles.add(new Name(ruTitle, Language.RU));
        titles.add(new Name(armTitle, Language.ARM));

        List<Name> descriptions = new ArrayList<>();
        descriptions.add(new Name(enDescription, Language.EN));
        descriptions.add(new Name(ruDescription, Language.RU));
        descriptions.add(new Name(armDescription, Language.ARM));

        UserNotification userNotification = new UserNotification();
        userNotification.setUser(employee);
        userNotification.setNotificationType(notificationType);
        userNotification.setClickActionLink(actionLink);
        userNotification.setTitle(titles);
        userNotification.setDescription(descriptions);
        userNotificationRepository.save(userNotification);

        switch (employee.getLanguage()) {
            case ARM:
                pushNotification(employee, actionLink, armTitle, armDescription);

                break;
            case EN:
                pushNotification(employee, actionLink, enTitle, enDescription);
                break;
            case RU:
                pushNotification(employee, actionLink, ruTitle, ruDescription);
                break;
        }
    }

    @Async
    public void createForBalance(
            User user,
            NotificationType notificationType,
            String amount,
            boolean bonus
    ) {
        String actionLink;
        actionLink = "https://localhost:8080/balance";

        String armTitle = "Հաշվի վերալիցքավորում";
        String enTitle = "Account recharge";
        String ruTitle = "Пополнение счета";

        String armDescription = "Ձեր հաշիվը վերալիցքավորվել է " + amount + " դրամով";
        String enDescription = "Your account has been recharged with " + amount + " AMD";
        String ruDescription = "Ваш счет пополнен на " + amount + " драмов";

        if (bonus) {
            armDescription = "Դուք ստացել եք բոնուս " + amount + " դրամ";
            enDescription = "You have received a bonus of " + amount + " AMD";
            ruDescription = "Вы получили бонус " + amount + " драм";
        }

        List<Name> titles = new ArrayList<>();
        titles.add(new Name(enTitle, Language.EN));
        titles.add(new Name(ruTitle, Language.RU));
        titles.add(new Name(armTitle, Language.ARM));

        List<Name> descriptions = new ArrayList<>();
        descriptions.add(new Name(enDescription, Language.EN));
        descriptions.add(new Name(ruDescription, Language.RU));
        descriptions.add(new Name(armDescription, Language.ARM));

        UserNotification userNotification = new UserNotification();
        userNotification.setUser(user);
        userNotification.setNotificationType(notificationType);
        userNotification.setClickActionLink(actionLink);
        userNotification.setTitle(titles);
        userNotification.setDescription(descriptions);
        userNotificationRepository.save(userNotification);

        switch (user.getLanguage()) {
            case ARM:
                pushNotification(user, actionLink, armTitle, armDescription);
                break;
            case EN:
                pushNotification(user, actionLink, enTitle, enDescription);
                break;
            case RU:
                pushNotification(user, actionLink, ruTitle, ruDescription);
                break;
        }
    }

    @Async
    public void createForMessage(
            User user,
            NotificationType notificationType,
            String idForAction,
            String messageSender
    ) {
        String actionLink;
        actionLink = "https://localhost:8080/chat?chatId=" + idForAction;

        String armTitle = "Նոր հաղորդագրություն";
        String enTitle = "New message";
        String ruTitle = "Новое сообщение";

        String armDescription = messageSender + "-ը ուղարկել է ձեզ նոր հաղորդագրություն";
        String enDescription = messageSender + " sent you a new message";
        String ruDescription = messageSender + " отправил вам новое сообщение";

        List<Name> titles = new ArrayList<>();
        titles.add(new Name(enTitle, Language.EN));
        titles.add(new Name(ruTitle, Language.RU));
        titles.add(new Name(armTitle, Language.ARM));

        List<Name> descriptions = new ArrayList<>();
        descriptions.add(new Name(enDescription, Language.EN));
        descriptions.add(new Name(ruDescription, Language.RU));
        descriptions.add(new Name(armDescription, Language.ARM));

        UserNotification userNotification = new UserNotification();
        userNotification.setUser(user);
        userNotification.setNotificationType(notificationType);
        userNotification.setClickActionLink(actionLink);
        userNotification.setTitle(titles);
        userNotification.setDescription(descriptions);
        userNotificationRepository.save(userNotification);
        switch (user.getLanguage()) {
            case ARM:
                pushNotification(user, actionLink, armTitle, armDescription);
                break;
            case EN:
                pushNotification(user, actionLink, enTitle, enDescription);
                break;
            case RU:
                pushNotification(user, actionLink, ruTitle, ruDescription);
                break;
        }

    }

    public Optional<UserNotification> getNotificationById(String notificationId) {

        return userNotificationRepository.findById(notificationId);
    }


    public List<UserNotification> getLastNotifications(User user) {

        List<UserNotification> notificationsUnseen = userNotificationRepository.findAllByUserIdAndNotificationState(user.getId(), UserNotification.NotificationState.UNSEEN);

        List<NotificationType> types = new ArrayList<>();
        types.add(NotificationType.OFFER);
        types.add(NotificationType.BALANCE);
        List<UserNotification> notificationsSeen = userNotificationRepository
                .findTop5ByUserIdAndNotificationStateAndNotificationTypeInOrderByCreatedAtDesc
                        (user.getId(), UserNotification.NotificationState.SEEN, types);

        notificationsUnseen.addAll(notificationsSeen);
        notificationsUnseen.sort(Comparator.comparing(UserNotification::getCreatedAt));
        return notificationsUnseen;
    }

    public void update(UserNotification userNotification) {
        userNotification.setSeenAt(LocalDateTime.now());
        userNotification.setNotificationState(UserNotification.NotificationState.SEEN);
        userNotificationRepository.save(userNotification);
    }

    @Async
    public void pushNotification(User user, String clickAction, String title, String description) {
        UserNotificationToken userNotificationToken = userService.findUserNotificationByUser(user);

        PushNotify pushNotify = new PushNotify();
        pushNotify.setTitle(title);
        pushNotify.setBody(description);
        pushNotify.setTtlInSeconds("10");
        pushNotify.setClick_action(clickAction);
        try {
            if (userNotificationToken != null) {
                fcmService.sendPersonal(pushNotify, userNotificationToken.getToken());
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        userNotifyService.notifyNotification(getNotificationCount(user), user);
    }

    public int getNotificationCount(User user) {
        return userNotificationRepository.findAllByUserIdAndNotificationState(user.getId(), UserNotification.NotificationState.UNSEEN).size();
    }

    public UserNotificationStatus createUserNotificationStatus(User user, boolean isNotificationON) {
        UserNotificationStatus userNotificationStatus = new UserNotificationStatus();
        userNotificationStatus.setUser(user);
        userNotificationStatus.setNotificationON(isNotificationON);
        return userNotificationStatusRepository.save(userNotificationStatus);
    }

    public UserNotificationStatus findUserNotificationStatus(User user) {
        return userNotificationStatusRepository.findByUser(user);
    }

    public UserNotificationStatus updateUserNotificationStatus(User user, boolean isNotificationON) {
        UserNotificationStatus status = findUserNotificationStatus(user);
        if (status == null) {
            return createUserNotificationStatus(user, isNotificationON);
        }
        status.setNotificationON(isNotificationON);
        return userNotificationStatusRepository.save(status);
    }
}
