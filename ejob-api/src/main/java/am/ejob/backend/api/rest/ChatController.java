package am.ejob.backend.api.rest;

import am.ejob.backend.api.rest.response.ResponseInfo;
import am.ejob.backend.api.rest.vo.AttachmentStreamVO;
import am.ejob.backend.api.rest.vo.AttachmentVO;
import am.ejob.backend.api.rest.vo.LastLoginVO;
import am.ejob.backend.api.rest.vo.UserVO;
import am.ejob.backend.api.rest.vo.chat.ChatVO;
import am.ejob.backend.api.rest.vo.chat.MessageVO;
import am.ejob.backend.api.service.UserAttachmentService;
import am.ejob.backend.api.service.UserService;
import am.ejob.backend.api.service.chat.ChatService;
import am.ejob.backend.api.service.chat.MessageService;
import am.ejob.backend.api.util.FileUtils;
import am.ejob.backend.common.model.chat.Chat;
import am.ejob.backend.common.model.chat.ChatAttachment;
import am.ejob.backend.common.model.chat.Message;
import am.ejob.backend.common.model.chat.MessageState;
import am.ejob.backend.common.model.user.User;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import lombok.AllArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static am.ejob.backend.api.rest.response.ResponseMessage.*;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpStatus.*;

@Controller
@CrossOrigin
@RequestMapping("/api/private")
public class ChatController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatController.class);

    private final UserService userService;
    private final ChatService chatService;
    private final MessageService messageService;
    private final UserAttachmentService userAttachmentService;

    public ChatController(
            UserService userService,
            ChatService chatService,
            MessageService messageService,
            UserAttachmentService userAttachmentService) {
        this.userService = userService;
        this.chatService = chatService;
        this.messageService = messageService;
        this.userAttachmentService = userAttachmentService;
    }


    public static class MessageRequest {
        public String chatId;
        public String content;
        public String attachmentId;
    }

    @AllArgsConstructor
    public static class MessageResponse {
        public List<MessageVO> messageVO;
        public MessageState messageState;
        public int pageNumber;
        public int totalPages;
        public long totalElements;

    }

    @AllArgsConstructor
    public static class ChatResponse {
        public List<ChatVO> chatList;
        public int pageNumber;
        public int totalPages;
        public long totalElements;

    }

    @ApiOperation(value = "Send Message", authorizations = {@Authorization(value = "Bearer")})
    @PostMapping("/messages")
    public HttpEntity<?> sendMessage(@RequestBody MessageRequest request) {


        Optional<Chat> chatOptional = chatService.findChatById(request.chatId);
        if (chatOptional.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Message message = messageService.createMessage(userService.getCurrentUser(), chatOptional.get(), request.content, request.attachmentId);

        return ResponseEntity.ok().body(new MessageVO(message, new UserVO(message.getSender())));

    }


    @ApiOperation(value = "Send Message", authorizations = {@Authorization(value = "Bearer")})
    @PostMapping("/messages/read/{chatId}")
    public HttpEntity<?> chatRead(@PathVariable("chatId") String chatId) {

        Optional<Chat> chatOptional = chatService.findChatById(chatId);
        if (chatOptional.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        messageService.updateMessagesRead(chatOptional.get(), userService.getCurrentUser());
        return ResponseEntity.ok().build();

    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "CHAT_ID_IS_NULL", response = ResponseInfo.class),
            @ApiResponse(code = 404, message = "CHAT_IS_NOT_FOUND", response = ResponseInfo.class),
            @ApiResponse(code = 200, message = "", response = MessageVO.class)
    })
    @ApiOperation(value = "Get Message List", authorizations = {@Authorization(value = "Bearer")})
    @GetMapping("/messages/{chatId}")
    public HttpEntity<?> getAllMessages(
            @PathVariable("chatId") String chatId,
            @RequestParam(value = "from") int from,
            @RequestParam(value = "count") int count) {

        User currentUser = userService.getCurrentUser();
        if (from < 0) {
            return ResponseEntity.badRequest().body(ResponseInfo.createResponse(BAD_REQUEST, PAGE_INDEX_IS_INVALID));
        }
        if (count < 1) {
            return ResponseEntity.badRequest().body(ResponseInfo.createResponse(BAD_REQUEST, PAGE_SIZE_IS_INVALID));
        }
        if (isBlank(chatId)) {
            return ResponseEntity.badRequest().body(ResponseInfo.createResponse(BAD_REQUEST, CHAT_ID_IS_NULL));
        }

        Optional<Chat> chatOptional = chatService.findChatById(chatId);

        if (chatOptional.isEmpty()) {
            return ResponseEntity.status(404).body(ResponseInfo.createResponse(NOT_FOUND, CHAT_IS_NOT_FOUND));
        }

        Page<Message> messages = messageService.findAllMessagesByChat(chatOptional.get(), from, count);
        MessageState messageState = MessageState.READ;
        if (messageService.checkUserUnreadMessage(chatOptional.get(), currentUser)) {
            messageState = MessageState.UNREAD;
        }
        return ResponseEntity.ok(
                new MessageResponse(
                        messages.stream().map(message ->
                                new MessageVO(message,
                                        new UserVO(message.getSender()))).collect(Collectors.toList()),
                        messageState,
                        messages.getPageable().getPageNumber(),
                        messages.getTotalPages(),
                        messages.getTotalElements())
        );
    }

    @ApiOperation(value = "Get Chat List", authorizations = {@Authorization(value = "Bearer")})
    @GetMapping("/chats")
    public HttpEntity<?> getChats(
            @RequestParam(value = "from") int from,
            @RequestParam(value = "count") int count
    ) {
        User currentUser = userService.getCurrentUser();

        Page<Chat> chats = chatService.getUserChats(currentUser, from, count);
        List<ChatVO> chatsResponse = new ArrayList<>();

        switch (currentUser.getUserType()) {
            case EMPLOYEE:
                chats.forEach(chat -> {
                    if (messageService.unreadMessage(chat, currentUser)) {
                        chatsResponse.add(new ChatVO(
                                        chat.getId(),
                                        new UserVO(
                                                chat.getTo()
                                        ),
                                        MessageState.UNREAD,
                                        userAttachmentService.getUserAvatar(chat.getTo()),
                                        new LastLoginVO(userService.getUserLastLogin(chat.getTo()))
                                )
                        );
                    } else {
                        chatsResponse.add(
                                new ChatVO(
                                        chat.getId(),
                                        new UserVO(
                                                chat.getTo()
                                        ),
                                        MessageState.READ,
                                        userAttachmentService.getUserAvatar(chat.getTo()),
                                        new LastLoginVO(userService.getUserLastLogin(chat.getTo()))
                                )
                        );
                    }
                });
                return ResponseEntity.ok(
                        new ChatResponse(
                                chatsResponse,
                                chats.getPageable().getPageNumber(),
                                chats.getTotalPages(),
                                chats.getTotalElements())
                );
            case EMPLOYER:
                chats.forEach(chat -> {
                    if (messageService.unreadMessage(chat, currentUser)) {
                        chatsResponse.add(
                                new ChatVO(
                                        chat.getId(),
                                        new UserVO(
                                                chat.getFrom()
                                        ),
                                        MessageState.UNREAD,
                                        userAttachmentService.getUserAvatar(chat.getFrom()),
                                        new LastLoginVO(userService.getUserLastLogin(chat.getFrom()))
                                )
                        );
                    } else {
                        chatsResponse.add(
                                new ChatVO(
                                        chat.getId(),
                                        new UserVO(
                                                chat.getFrom()
                                        ),
                                        MessageState.READ,
                                        userAttachmentService.getUserAvatar(chat.getFrom()),
                                        new LastLoginVO(userService.getUserLastLogin(chat.getFrom()))
                                )
                        );
                    }
                });
                return ResponseEntity.ok(
                        new ChatResponse(
                                chatsResponse,
                                chats.getPageable().getPageNumber(),
                                chats.getTotalPages(),
                                chats.getTotalElements())
                );
        }
        return ResponseEntity.notFound().build();
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "CHAT_ID_IS_NULL", response = ResponseInfo.class),
            @ApiResponse(code = 404, message = "CHAT_IS_NOT_FOUND", response = ResponseInfo.class),
            @ApiResponse(code = 200, message = "")
    })
    @ApiOperation(value = "Delete Message", authorizations = {@Authorization(value = "Bearer")})
    @DeleteMapping("/chat/{chatId}")
    public HttpEntity<?> deleteMessage(@PathVariable("chatId") String chatId) {

        if (isBlank(chatId)) {
            return ResponseEntity.badRequest().body(ResponseInfo.createResponse(BAD_REQUEST, CHAT_ID_IS_NULL));
        }

        Optional<Chat> chatOptional = chatService.findChatById(chatId);

        if (chatOptional.isEmpty()) {
            return ResponseEntity.status(404).body(ResponseInfo.createResponse(NOT_FOUND, CHAT_IS_NOT_FOUND));
        }

        chatService.deleteChat(chatOptional.get());
        return ResponseEntity.ok().build();
    }


    @ApiResponses({
            @ApiResponse(code = 400, message = "FILE_IS_NULL,FILE_IS_NOT_IMAGE,INVALID_FILE_SIZE,INVALID_IMAGE_EXTENSION", response = ResponseInfo.class),
            @ApiResponse(code = 500, message = "INTERNAL_SERVER_ERROR_UPLOAD_IMAGE", response = ResponseInfo.class),
            @ApiResponse(code = 201, message = "", response = AttachmentVO.class)
    })
    @ApiOperation(value = "", authorizations = {@Authorization(value = "Bearer")})
    @PostMapping("/chat/{chatId}/file")
    public HttpEntity<?> uploadFile(
            @RequestPart MultipartFile multipartFile,
            @PathVariable(value = "chatId") String chatId

    ) {
        User currentUser = userService.getCurrentUser();
        if (!chatService.isChatUser(chatId, currentUser.getId())) {
            return ResponseEntity.status(403).build();
        }

        if (multipartFile == null) {
            return ResponseEntity.badRequest().body(ResponseInfo.createResponse(BAD_REQUEST, FILE_IS_NULL));
        }
        if (FileUtils.invalidSize(new MultipartFile[]{multipartFile}, 50)) {
            return ResponseEntity.badRequest().body(ResponseInfo.createResponse(BAD_REQUEST, INVALID_FILE_SIZE));
        }


        ChatAttachment chatAttachment = chatService.uploadChat(multipartFile, currentUser, chatId);
        if (chatAttachment == null) {
            return ResponseEntity.status(500).body(ResponseInfo.createResponse(INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_UPLOAD_IMAGE));
        }
        return ResponseEntity.ok(new AttachmentVO(chatAttachment));

    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "ATTACHMENT_ID_IS_NULL,FILE_IS_NOT_IMAGE", response = ResponseInfo.class),
            @ApiResponse(code = 404, message = "ATTACHMENT_NOT_FOUND", response = ResponseInfo.class),
            @ApiResponse(code = 500, message = "INTERNAL_SERVER_ERROR_GET_FILE", response = ResponseInfo.class),
            @ApiResponse(code = 200, message = "byte []")
    })
    @ApiOperation(value = "", authorizations = {@Authorization(value = "Bearer")})
    @GetMapping("/chat/{chatId}/file/{attachmentId}")
    public HttpEntity<?> getFile(
            @PathVariable(value = "attachmentId") String attachmentId,
            @PathVariable(value = "chatId") String chatId

    ) throws IOException {
        if (isBlank(attachmentId)) {
            ResponseEntity.badRequest().body(ResponseInfo.createResponse(BAD_REQUEST, ATTACHMENT_ID_IS_NULL));
        }
        User currentUser = userService.getCurrentUser();
        if (!chatService.isChatUser(chatId, currentUser.getId())) {
            return ResponseEntity.status(403).build();
        }


        ChatAttachment chatAttachment = chatService.findChatAttachment(chatId, attachmentId);
        if (chatAttachment == null) {
            return ResponseEntity.status(404).body(ResponseInfo.createResponse(NOT_FOUND, ATTACHMENT_NOT_FOUND));
        }
        InputStream attachmentFile = chatService.getAttachmentFile(chatAttachment.getName());
        if (attachmentFile == null) {
            return ResponseEntity.status(500).body(ResponseInfo.createResponse(INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_GET_FILE));
        }
        return ResponseEntity.ok(writeStreamResponse(
                attachmentFile,
                chatAttachment.getContentType()
        ));
    }


    @ApiOperation(value = "", authorizations = {@Authorization(value = "Bearer")})
    @DeleteMapping("/chat/{chatId}/file/{attachmentId}")
    public HttpEntity<?> deleteFile(
            @PathVariable(value = "attachmentId") String attachmentId,
            @PathVariable(value = "chatId") String chatId
    ) {
        User currentUser = userService.getCurrentUser();
        if (!chatService.isChatUser(chatId, currentUser.getId())) {
            return ResponseEntity.status(403).build();
        }
        ChatAttachment chatAttachment = chatService.findUserAttachmentInChat(currentUser, chatId, attachmentId);
        if (chatAttachment == null) {
            return ResponseEntity.status(404).body(ResponseInfo.createResponse(NOT_FOUND, ATTACHMENT_NOT_FOUND));
        }
        chatService.deleteChatAttachment(chatAttachment);
        return ResponseEntity.ok().body(ResponseInfo.createResponse(OK, ATTACHMENT_IS_DELETED));
    }


    public static AttachmentStreamVO writeStreamResponse(InputStream inputStream, String contentType) throws IOException {
        AttachmentStreamVO attachmentStreamVO = new AttachmentStreamVO();
        attachmentStreamVO.setContentType(contentType);
        attachmentStreamVO.setStream(IOUtils.toByteArray(inputStream));
        return attachmentStreamVO;
    }

}
