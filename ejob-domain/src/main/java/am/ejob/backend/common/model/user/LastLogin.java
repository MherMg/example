package am.ejob.backend.common.model.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "user_last_login")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LastLogin {

    @Id
    private String id;
    private LocalDateTime onlineAt;
    @DBRef
    User user;
    private boolean online;

}
