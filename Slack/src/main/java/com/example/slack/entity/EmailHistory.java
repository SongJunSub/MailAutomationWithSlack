package com.example.slack.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Getter
@NoArgsConstructor
public class EmailHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String senderName;

    private String senderEmail;

    private String subject;

    private String status;

    private String result;

    private boolean hasAttachments;

    private String messageId;

    private String createdUser;

    private String createdDate;

    private String updatedUser;

    private String updatedDate;

    @PrePersist
    protected void onCreate() {
        this.createdUser = "MAIL_AUTOMATION";
        this.createdDate = getCurrentDate();
        this.updatedUser = "MAIL_AUTOMATION";
        this.updatedDate = getCurrentDate();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedUser = "MAIL_AUTOMATION";
        this.updatedDate = getCurrentDate();
    }

    private String getCurrentDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public EmailHistory(String senderName, String senderEmail, String subject, String status, String result, boolean hasAttachments, String messageId) {
        this.senderName = senderName;
        this.senderEmail = senderEmail;
        this.subject = subject;
        this.status = status;
        this.result = result;
        this.hasAttachments = hasAttachments;
        this.messageId = messageId;
    }

}