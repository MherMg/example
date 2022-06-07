package am.ejob.backend.api.service;

import am.ejob.backend.api.config.FcmSettings;
import am.ejob.backend.api.config.PushNotify;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class FcmService {


    public FcmService(FcmSettings settings) {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();

        try (InputStream serviceAccount = classloader.getResourceAsStream("offert-eae99-firebase-adminsdk-to6ur-06d37aa304.json")) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(Objects.requireNonNull(serviceAccount)))
                    .build();

            FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            Logger.getLogger(FcmService.class.getName())
                    .log(Level.SEVERE, null, e);
        }
    }

    public String sendByTopic(PushNotify conf, String topic)
            throws InterruptedException, ExecutionException {

        Message message = Message.builder().setTopic(topic)
                .setWebpushConfig(WebpushConfig.builder()
                        .putHeader("ttl", conf.getTtlInSeconds())
                        .setNotification(createBuilder(conf).build())
                        .build())
                .build();
        return FirebaseMessaging.getInstance()
                .sendAsync(message)
                .get();
    }

    public String sendPersonal(PushNotify conf, String clientToken)
            throws ExecutionException, InterruptedException {
        Message message = Message.builder().setToken(clientToken)
                .setWebpushConfig(WebpushConfig.builder()
                        .putHeader("ttl", conf.getTtlInSeconds())
                        .setNotification(createBuilder(conf).build())
                        .build())
                .build();
        return FirebaseMessaging.getInstance()
                .sendAsync(message)
                .get();
    }

    public void subscribeUsers(String topic, List<String> clientTokens)
            throws FirebaseMessagingException {
        for (String token : clientTokens) {
            TopicManagementResponse response = FirebaseMessaging.getInstance()
                    .subscribeToTopic(Collections.singletonList(token), topic);
        }
    }

    private WebpushNotification.Builder createBuilder(PushNotify conf) {
        WebpushNotification.Builder builder = WebpushNotification.builder();
        builder.addAction(new WebpushNotification
                .Action(conf.getClick_action(), "Открыть"))
                .setImage(conf.getIcon())
                .setTitle(conf.getTitle())
                .setBody(conf.getBody());
        return builder;
    }
}