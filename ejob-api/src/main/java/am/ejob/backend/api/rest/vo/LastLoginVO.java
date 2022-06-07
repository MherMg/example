package am.ejob.backend.api.rest.vo;

import am.ejob.backend.common.model.user.LastLogin;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class LastLoginVO {
    public boolean online;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    public LocalDateTime onlineAt;

    public LastLoginVO(LastLogin lastLogin) {
        this.online = lastLogin != null && lastLogin.isOnline();
        this.onlineAt = lastLogin != null ? lastLogin.getOnlineAt() : null;
    }
}
