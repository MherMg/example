package am.ejob.backend.api.security;

import am.ejob.backend.common.model.user.User;
import org.springframework.security.core.authority.AuthorityUtils;

public class CurrentUser extends org.springframework.security.core.userdetails.User {

    private User user;

    public CurrentUser(User user) {
        super(user.getPhoneNumber(), user.getPhoneNumber(), AuthorityUtils.createAuthorityList("USER"));
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public String getId() {
        return user.getId();
    }
}
