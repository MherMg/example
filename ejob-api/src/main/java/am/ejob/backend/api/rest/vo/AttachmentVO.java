package am.ejob.backend.api.rest.vo;

import am.ejob.backend.common.model.chat.ChatAttachment;
import am.ejob.backend.common.model.user.UserAttachment;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;

public class AttachmentVO {

    public String id;

    public String contentType;

    public long size;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime createdAt;

    public String originalName;


    public AttachmentVO(UserAttachment userAttachment) {
        this.id = userAttachment.getId();
        this.contentType = userAttachment.getContentType();
        this.size = userAttachment.getSize();
        this.createdAt = userAttachment.getCreatedAt();
        this.originalName = userAttachment.getOriginalName();
    }

    public AttachmentVO(ChatAttachment chatAttachment) {
        this.id = chatAttachment.getId();
        this.contentType = chatAttachment.getContentType();
        this.size = chatAttachment.getSize();
        this.createdAt = chatAttachment.getCreatedAt();
        this.originalName = chatAttachment.getOriginalName();
    }
}
