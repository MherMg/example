package am.ejob.backend.api.rest.vo.chat;

import am.ejob.backend.api.rest.vo.UserVO;
import am.ejob.backend.common.model.chat.Message;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;

public class MessageVO {

    public String id;
    public UserVO sender;
    public String content;
    public String chatId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime sendAt;
    public String attachmentId;

    public MessageVO(Message message, UserVO sender) {
        this.id = message.getId();
        this.sender = sender;
        this.content = message.getContent();
        this.chatId = message.getChat().getId();
        this.sendAt = message.getSendAt();
        this.attachmentId = message.getChatAttachment() != null ? message.getChatAttachment().getId() : null;
    }
}
