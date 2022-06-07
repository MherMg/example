package am.ejob.backend.common.model.sms;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document("verify_limit")
public class VerifyLimit {

    @Id
    private String id;
    private String phoneNumber;
    private int count;
    private LocalDateTime blockedDate;
}
