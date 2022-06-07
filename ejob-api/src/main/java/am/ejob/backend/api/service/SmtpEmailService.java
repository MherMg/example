package am.ejob.backend.api.service;

import am.ejob.backend.common.model.user.User;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class SmtpEmailService {

    @Value("${spring.mail.username}")
    private String mailUsername;
    @Value("${spring.mail.password}")
    private String mailPassword;
    @Value("${spring.mail.host}")
    private String mailSmtpHost;
    @Value("${spring.mail.port}")
    private String mailSmtpPort;

    public void sendRequestReview(String email, String link) {
        send("New Action ", link, email);
    }

    public void sendMail(String email, String confirmationCode) {
        send("New Action ", "Verify Code =   " + confirmationCode, email);
    }

    public void sendVerifyMail(String email, User currentUser) {
        send("New Action  ", "Dear " + currentUser.getName() + "  Your email  " + email + "  is successfully added", email);
    }

    public void sendSupportMail(String phoneNumber, String name, String email, String description) {

        if (email != null && name == null) {
            send("New Action ", "PhoneNumber ->  " + phoneNumber + "  Email ->  " + email + "  Desc ->  " + description, "samvelbaloyan1995@gmail.com");
        }

        if (name != null && email == null) {
            send("New Action ", "PhoneNumber ->  " + phoneNumber + "  Name ->  " + name + "  Desc ->  " + description, "samvelbaloyan1995@gmail.com");
        }

        if (name == null && email == null) {
            send("New Action ", "PhoneNumber ->  " + phoneNumber + "  Desc ->  " + description, "samvelbaloyan1995@gmail.com");
        }

        send("New Action ", "PhoneNumber ->  " + phoneNumber + "  Email ->  " + email + "  Name ->  " + name + "  Desc ->  " + description, "samvelbaloyan1995@gmail.com");
    }

    public void send(String subject, String text, String email) {

        try {
            Session session = Session.getInstance(getMailProperties(mailUsername, mailPassword));

            MimeMessage message = new MimeMessage(session);
            message.setHeader("Content-Type", "text/plain; charset-UTF-8");
            message.setSubject(subject, "UTF-8");
            message.setText(text, "UTF-8");
            message.setFrom(new InternetAddress(mailUsername));

            InternetAddress toAddress = new InternetAddress(email);
            message.setRecipient(Message.RecipientType.TO, toAddress);
            message.setSubject(subject);
            message.setText(text);

            Transport transport = session.getTransport("smtp");
            transport.connect(mailSmtpHost, mailUsername, mailPassword);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private Properties getMailProperties(String email, String password) {
        Properties props = new Properties();
        props.put("mail.smtp.host", mailSmtpHost);
        props.put("mail.smtp.user", email);
        props.put("mail.smtp.password", password);
        props.put("mail.smtp.port", mailSmtpPort);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.debug", "true");
        return props;
    }

}
