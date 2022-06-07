package am.ejob.backend.api.service.chat;

import am.ejob.backend.api.service.S3FileService;
import am.ejob.backend.api.service.UserAttachmentService;
import am.ejob.backend.api.service.UserNotifyService;
import am.ejob.backend.common.model.chat.Chat;
import am.ejob.backend.common.model.chat.ChatAttachment;
import am.ejob.backend.common.model.user.User;
import am.ejob.backend.common.repository.chat.ChatAttachmentRepository;
import am.ejob.backend.common.repository.chat.ChatRepository;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
public class ChatService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatService.class);

    private final ChatRepository chatRepository;
    private final UserNotifyService userNotifyService;
    private final ChatAttachmentRepository chatAttachmentRepository;
    private final UserAttachmentService userAttachmentService;
    private final S3FileService s3FileService;

    @Autowired
    public ChatService(
            ChatRepository chatRepository,
            UserNotifyService userNotifyService,
            ChatAttachmentRepository chatAttachmentRepository,
            UserAttachmentService userAttachmentService,
            S3FileService s3FileService
    ) {
        this.chatRepository = chatRepository;
        this.userNotifyService = userNotifyService;
        this.chatAttachmentRepository = chatAttachmentRepository;
        this.userAttachmentService = userAttachmentService;
        this.s3FileService = s3FileService;
    }

    public Chat createChat(User fromUser, User toUser) {
        Chat existingChat = chatRepository.findByFromAndTo(fromUser, toUser);
        if (existingChat != null) {
            return existingChat;
        }
        LOGGER.debug("Creating chat ... [fromUserId:{};toUserId:{}]", fromUser.getId(), toUser.getId());
        Chat chat = new Chat();
        chat.setFrom(fromUser);
        chat.setTo(toUser);
        chatRepository.save(chat);
        LOGGER.debug("Created chat [chatId:{}]", chat.getId());
        userNotifyService.notifyNewChat(chat, userAttachmentService.getUserAvatar(fromUser));
        return chat;
    }

    public Optional<Chat> findChatById(String chatId) {
        return chatRepository.findById(chatId);
    }

    public Page<Chat> getUserChats(User currentUser, int from, int count) {
        Pageable pageable = PageRequest.of(from, count);
        switch (currentUser.getUserType()) {
            case EMPLOYER:
                return chatRepository.findAllByToOrderByUpdatedAtDesc(currentUser, pageable);

            case EMPLOYEE:
                return chatRepository.findAllByFromOrderByUpdatedAtDesc(currentUser, pageable);

        }
        return null;
    }

    public void deleteChat(Chat chat) {
        chatRepository.delete(chat);
    }

    public boolean isChatUser(String chatId, String userId) {
        Optional<Chat> chatOptional = findChatById(chatId);
        if (chatOptional.isPresent()) {
            if (chatOptional.get().getFrom().getId().equals(userId)) {
                return true;
            }
            return chatOptional.get().getTo().getId().equals(userId);
        }
        return false;
    }


    public ChatAttachment findUserAttachmentInChat(User currentUser, String chatId, String attachmentId) {
        return chatAttachmentRepository.findByChatIdAndUserIdAndId(chatId, currentUser.getId(), attachmentId);
    }

    public ChatAttachment findChatAttachment(String chatId, String attachmentId) {
        return chatAttachmentRepository.findByChatIdAndId(chatId, attachmentId);
    }

    public void deleteChatAttachment(ChatAttachment attachment) {
        s3FileService.delete(attachment.getName());
        chatAttachmentRepository.delete(attachment);
    }

    public ChatAttachment uploadChat(MultipartFile multipartFile, User currentUser, String chatId) {

        Optional<Chat> chatOptional = findChatById(chatId);
        Chat chat = chatOptional.get();
        ChatAttachment chatAttachment = new ChatAttachment();
        chatAttachment.setContentType(multipartFile.getContentType());
        chatAttachment.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        chatAttachment.setSize(multipartFile.getSize());
        chatAttachment.setUser(currentUser);
        chatAttachment.setChat(chat);
        chatAttachment.setOriginalName(multipartFile.getOriginalFilename());
        chatAttachment.setName(System.currentTimeMillis() + "_" + multipartFile.getOriginalFilename());
        chatAttachmentRepository.save(chatAttachment);
        try {
            File originalFile = new File(chatAttachment.getName());
            FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), originalFile);
            boolean s3Upload = s3FileService.upload(originalFile);
            if (!s3Upload) {
                deleteChatAttachment(chatAttachment);
                return null;
            }
        } catch (IOException e) {
            deleteChatAttachment(chatAttachment);
            return null;
        }
        return chatAttachment;
    }


    public InputStream getAttachmentFile(String name) {
        return userAttachmentService.getAttachmentFile(name);
    }

    public void updateChat(Chat chat) {
        chatRepository.save(chat);
    }
}
