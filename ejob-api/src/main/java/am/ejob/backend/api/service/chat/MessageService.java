package am.ejob.backend.api.service.chat;

import am.ejob.backend.api.service.NotificationService;
import am.ejob.backend.api.service.UserNotifyService;
import am.ejob.backend.common.model.chat.Chat;
import am.ejob.backend.common.model.chat.ChatAttachment;
import am.ejob.backend.common.model.chat.Message;
import am.ejob.backend.common.model.chat.MessageState;
import am.ejob.backend.common.model.pushNotification.NotificationType;
import am.ejob.backend.common.model.user.User;
import am.ejob.backend.common.repository.chat.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class MessageService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageService.class);

    private final MessageRepository messageRepository;
    private final UserNotifyService userNotifyService;
    private final ChatService chatService;
    private final NotificationService notificationService;

    public MessageService(
            MessageRepository messageRepository,
            UserNotifyService userNotifyService,
            ChatService chatService,
            NotificationService notificationService
    ) {
        this.messageRepository = messageRepository;
        this.userNotifyService = userNotifyService;
        this.chatService = chatService;
        this.notificationService = notificationService;
    }

    public Message createMessage(User sender, Chat chat, String content, String attachmentId) {
        LOGGER.debug("Creating message ... [snederId:{};chatId:{};content:{}]", sender.getId(), chat.getId(), content);
        Message message = new Message();
        message.setSender(sender);
        message.setChat(chat);
        message.setContent(content);
        message.setSendAt(LocalDateTime.now(ZoneOffset.UTC));
        if (attachmentId != null) {
            ChatAttachment chatAttachment = chatService.findChatAttachment(chat.getId(), attachmentId);
            if (chatAttachment != null) {
                message.setChatAttachment(chatAttachment);
            }
        }
        LOGGER.debug("Created message ... [snederId:{};chatId:{};content:{}]", sender.getId(), chat.getId(), content);
        Message saved = messageRepository.save(message);

        User toUser;
        if (chat.getFrom().getId().equals(sender.getId())) {
            toUser = chat.getTo();
        } else {
            toUser = chat.getFrom();
        }
        chat.setUpdatedAt(LocalDateTime.now());
        chatService.updateChat(chat);
        userNotifyService.notifyNewMessage(saved, sender, toUser);
        notificationService.createForMessage(toUser, NotificationType.MESSAGE, chat.getId(), sender.getName());
        return saved;
    }

    public Page<Message> findAllMessagesByChat(Chat chat, int from, int count) {
        Pageable pageable = PageRequest.of(from, count);
        return messageRepository.findAllByChatOrderBySendAtDesc(chat, pageable);
    }

    public boolean checkUserUnreadMessage(Chat chat, User currentUser) {
        List<Message> messageList = messageRepository.findAllByChatIdAndSenderIdAndMessageState(chat.getId(), currentUser.getId(), MessageState.UNREAD);
        return !messageList.isEmpty();
    }

    public boolean unreadMessage(Chat chat, User user) {

        if (user.getId().equals(chat.getTo().getId())) {
            user = chat.getFrom();
        } else {
            user = chat.getTo();
        }
        List<Message> messageList = messageRepository.findAllByChatIdAndSenderIdAndMessageState(chat.getId(), user.getId(), MessageState.UNREAD);
        return !messageList.isEmpty();
    }

    public void updateMessagesRead(Chat chat, User currentUser) {
        User to;
        if (currentUser.getId().equals(chat.getTo().getId())) {
            to = chat.getFrom();
        } else {
            to = chat.getTo();
        }
        List<Message> messages = messageRepository.findAllBySenderIdAndMessageState(to.getId(), MessageState.UNREAD);
        LOGGER.debug("update messages,size[{}]", messages.size());
        if (!messages.isEmpty()) {
            messages.forEach(message -> {
                message.setMessageState(MessageState.READ);
                messageRepository.save(message);
            });

            userNotifyService.notifyMessageRead(to, chat.getId());
        }
    }


}
