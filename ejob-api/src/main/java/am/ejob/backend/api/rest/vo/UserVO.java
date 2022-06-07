package am.ejob.backend.api.rest.vo;

import am.ejob.backend.common.model.Language;
import am.ejob.backend.common.model.user.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {

    public String id;
    public String name;
    public String phoneNumber;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime cratedAt;
    public User.UserType userType;
    public String email;
    public Language language;

    public UserVO(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.phoneNumber = user.getPhoneNumber();
        this.cratedAt = user.getCratedAt();
        this.userType = user.getUserType();
        this.email = user.getEmail();
        this.language = user.getLanguage();
    }
}
