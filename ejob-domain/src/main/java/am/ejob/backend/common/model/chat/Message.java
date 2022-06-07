package am.ejob.backend.common.model.chat;

import am.ejob.backend.common.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "messages")
public class Message {

    @Id
    private String id;
    @DBRef
    private User sender;
    @DBRef
    private Chat chat;
    private String content;
    private LocalDateTime sendAt;
    @DBRef
    private ChatAttachment chatAttachment;

    private MessageState messageState = MessageState.UNREAD;
}
