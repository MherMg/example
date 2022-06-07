package am.ejob.backend.api.config;

import lombok.Data;

@Data
public class PushNotify {

    private String title;
    private String body;
    private String icon;
    private String click_action;
    private String ttlInSeconds;

    public PushNotify() {
    }

    public PushNotify(String title, String body, String icon,
                      String click_action, String ttlInSeconds) {
        this.title = title;
        this.body = body;
        this.icon = icon;
        this.click_action = click_action;
        this.ttlInSeconds = ttlInSeconds;
    }
}
