package am.ejob.backend.common.model.user;

import am.ejob.backend.common.model.Language;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    private String id;
    private String name;
    private String phoneNumber;
    private LocalDateTime cratedAt;
    private UserType userType;
    private String email;
    private Language language;

    public enum UserType {
        EMPLOYER,
        EMPLOYEE
    }
}
