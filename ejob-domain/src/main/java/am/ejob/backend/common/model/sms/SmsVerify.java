package am.ejob.backend.common.model.sms;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document("sms_verify")
public class SmsVerify {

    @Id
    private String id;
    private String phoneNumber;
    private String code;
    private LocalDateTime createdAt;
    private State state;
    private int count = 1;

    public enum State {
        SENT,
        PROCESSED
    }
}
