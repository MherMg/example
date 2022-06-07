package am.ejob.backend.api.rest.vo;

public class AuthorizationVO {

    public String accessToken;
    public String tokenType;

    public AuthorizationVO(String accessToken, String tokenType) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
    }
}
