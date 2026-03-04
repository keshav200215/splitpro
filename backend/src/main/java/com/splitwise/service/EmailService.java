package com.splitwise.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendDebtReminder(String to, String message) {

        SimpleMailMessage mail = new SimpleMailMessage();

        mail.setTo(to);
        mail.setSubject("SplitPro Weekly Debt Reminder");
        mail.setText(message);

        mailSender.send(mail);
    }
}