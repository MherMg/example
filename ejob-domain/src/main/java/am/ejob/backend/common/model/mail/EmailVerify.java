package am.ejob.backend.common.model.mail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document("email_verify")
public class EmailVerify {

    @Id
    private String id;

    private String email;

    private State state;

    private String confirmationCode;

    private LocalDateTime createdAt;

    private int requestCount = 0;
}
