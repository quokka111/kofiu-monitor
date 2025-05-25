package com.example.kofiu;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailSender {
    public static void send(String subject, String body) {
        final String from = "minsuk0531@naver.com";
        final String to = "minsuk0531@naver.com";
        final String username = "minsuk0531@naver.com";
        final String password = "RH4CMKFGBLFB"; // ✅ 앱 비밀번호

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.naver.com"); // ✅ 수정됨
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
            new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);

            System.out.println("이메일 전송 완료");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
