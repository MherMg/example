package am.ejob.backend.common.model.chat;

import am.ejob.backend.common.model.user.User;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "chat_attachment")
public class ChatAttachment {
    @Id
    private String id;

    @DBRef
    private Chat chat;

    @DBRef
    private User user;

    private String contentType;

    private long size;

    private LocalDateTime createdAt;

    private String name;

    private String originalName;

}
