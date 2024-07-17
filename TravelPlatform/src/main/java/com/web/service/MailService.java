package com.web.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;
    private static final String senderEmail = "rjs0205@gmail.com";
    private int number;
    
    public void createNumber() {
        number = (int)(Math.random() * (900000)) + 100000;
    }
    
    public MimeMessage createMail(String mail) {
        createNumber();
        MimeMessage message = javaMailSender.createMimeMessage();
        
        try {
            message.setFrom(senderEmail);
            message.setRecipients(MimeMessage.RecipientType.TO, mail);
            message.setSubject("여행 커뮤니티 저기 어때? 회원가입 이메일 인증 메일입니다.");
            String body = "";
            body += "<h4>" + "안녕하세요." + "</h4>";
            body += "<h4>" + "요청하신 이메일 인증번호를 안내 드립니다" + "</h4>";
            body += "<h4>" + "회원가입을 위해 아래의 숫자를 입력해주세요!" + "</h4></br>";
            body += "<h1>" + number + "</h1></br>";
            body += "<h4>" + "감사합니다." + "</h4>";
            message.setText(body, "UTF-8", "html");

        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return message;
    }
    
    public int sendMail(String mail) {
        MimeMessage message = createMail(mail);
        javaMailSender.send(message);
        
        return number;
    }
    
    public MimeMessage createPasswordMail(String mail) {
        createNumber();
        MimeMessage resetPasswordMessage = javaMailSender.createMimeMessage();
        
        try {
            
            resetPasswordMessage.setFrom(senderEmail);
            resetPasswordMessage.setRecipients(MimeMessage.RecipientType.TO, mail);
            resetPasswordMessage.setSubject("여행 커뮤니티 저기 어때? 임시 비밀번호입니다.");
            String resetbody = "";
            resetbody += "<h4>" + "안녕하세요." + "</h4>";
            resetbody += "<h4>" + "변경된 임시 비밀번호입니다." + "</h4>";
            resetbody += "<h4>" + "아래의 숫자로 로그인 하신 후 비밀번호 변경 하시면 됩니다." + "</h4></br>";
            resetbody += "<h1>" + number + "</h1></br>";
            resetbody += "<h4>" + "감사합니다." + "</h4>";
            resetPasswordMessage.setText(resetbody, "UTF-8", "html");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return resetPasswordMessage;
    }
    
    public int sendPasswordMail(String mail) {
        MimeMessage resetPasswordMessage = createPasswordMail(mail);
        javaMailSender.send(resetPasswordMessage);
        
        return number;
    }
    public int getVerificationNumber() {
        return number;
    }
}
