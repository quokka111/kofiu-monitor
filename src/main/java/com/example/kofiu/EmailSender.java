package com.example.kofiu;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailSender {
    public static void send(String subject, String body) {
        // ✅ 환경변수로부터 가져오기 (GitHub Secrets)
        final String from = System.getenv("NAVER_USER");
        final String to = System.getenv("NAVER_USER"); // 수신자도 동일
        final String username = System.getenv("NAVER_USER");
        final String password = System.getenv("NAVER_PASS");

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
