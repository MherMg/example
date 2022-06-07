package am.ejob.backend.api.service;

import am.ejob.backend.api.rest.vo.UserVO;
import am.ejob.backend.api.rest.vo.chat.ChatVO;
import am.ejob.backend.api.rest.vo.chat.MessageVO;
import am.ejob.backend.api.rest.vo.chat.ReadMessageVO;
import am.ejob.backend.api.websocket.UserNotifier;
import am.ejob.backend.api.websocket.packet.*;
import am.ejob.backend.common.model.chat.Chat;
import am.ejob.backend.common.model.chat.Message;
import am.ejob.backend.common.model.chat.MessageState;
import am.ejob.backend.common.model.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class UserNotifyService {

    private static final Logger log = LoggerFactory.getLogger(UserNotifyService.class);

    private final UserNotifier userNotifier;


    public UserNotifyService(
            UserNotifier userNotifier
    ) {
        this.userNotifier = userNotifier;
    }


    @Async("webSocketThreadPoolExecutor")
    public void notifyNewMessage(Message message, User sender, User toUser) {
        userNotifier.notify(toUser.getId(), new NewMessageResponse(new MessageVO(message, new UserVO(sender))));
    }

    @Async("webSocketThreadPoolExecutor")
    public void notifyNewChat(Chat chat,byte[] icon) {
        userNotifier.notify(chat.getTo().getId(), new UpdateChatStateResponse(new ChatVO(chat.getId(), new UserVO(chat.getFrom()), MessageState.UNREAD,icon)));
    }

    @Async("webSocketThreadPoolExecutor")
    public void notifyMessageRead(User toUser, String chatId) {
        userNotifier.notify(toUser.getId(), new MessageStateResponse(new ReadMessageVO(chatId)));
    }

    @Async("webSocketThreadPoolExecutor")
    public void notifyNotification(int notificationCount, User toUser) {
        userNotifier.notify(toUser.getId(), new NewNotificationResponse(new NotificationCountResponseVo(notificationCount)));
    }
}
