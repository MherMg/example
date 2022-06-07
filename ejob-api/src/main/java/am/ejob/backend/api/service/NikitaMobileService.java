package am.ejob.backend.api.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class NikitaMobileService {

    private static final Logger log = LoggerFactory.getLogger(NikitaMobileService.class);

    private final String nikitaMobileApiUrl;
    private final String nikitaMobileOriginator;
    private final String nikitaMobileUsername;
    private final String nikitaMobilePassword;
    private final RestTemplate restTemplate;

    public NikitaMobileService(@Value("${nikita.mobile.api.url}") String nikitaMobileApiUrl,
                               @Value("${nikita.mobile.originator}") String nikitaMobileOriginator,
                               @Value("${nikita.mobile.username}") String nikitaMobileUsername,
                               @Value("${nikita.mobile.password}") String nikitaMobilePassword) {
        this.nikitaMobileApiUrl = nikitaMobileApiUrl;
        this.nikitaMobileOriginator = nikitaMobileOriginator;
        this.nikitaMobileUsername = nikitaMobileUsername;
        this.nikitaMobilePassword = nikitaMobilePassword;
        this.restTemplate = new RestTemplate();
    }

    @Data
    public static class Messages {
        public String recipient;
        public String priority;
        @JsonProperty("message-id")
        public String messageId;
        public Sms sms;
    }

    @Data
    public static class Sms {
        public String originator;
        public Content content;
    }

    @Data
    public static class Content {
        public String text;
    }

    @Data
    public static class Request {
        public List<Messages> messages;
    }

    public void sendMessage(String phoneNumber, String text) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBasicAuth(nikitaMobileUsername, nikitaMobilePassword, StandardCharsets.UTF_8);

            Messages messages = new Messages();
            messages.setRecipient(phoneNumber);
            messages.setPriority("2");
            messages.setMessageId(LocalDateTime.now().toString());

            Sms sms = new Sms();
            sms.setOriginator(nikitaMobileOriginator);

            Content content = new Content();
            content.setText(text);
            sms.setContent(content);

            messages.setSms(sms);

            Request request = new Request();
            request.setMessages(Collections.singletonList(messages));

            HttpEntity<Request> entity = new HttpEntity<>(request, headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(nikitaMobileApiUrl, HttpMethod.POST, entity, String.class);

            if (responseEntity.getBody() != null && responseEntity.getBody().equals("OK")) {
                log.debug("<Nikita Mobile> sms send to [phoneNumber:{};responseCode:{};sms:{}]...",
                        phoneNumber, responseEntity.getStatusCode(), text);
            } else {
                log.debug("SMS has not been sent to [phone number:{}]...", phoneNumber);
            }

        } catch (Exception e) {
            log.error("Nikita mobile error", e);
        }
    }
}
