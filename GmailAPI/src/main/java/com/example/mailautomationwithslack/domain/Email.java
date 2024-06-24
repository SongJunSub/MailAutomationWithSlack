package com.example.mailautomationwithslack.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Email {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String senderName;

    @Column(nullable = false)
    private String senderEmail;

    @Column(nullable = false)
    private String subject;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(nullable = false, unique = true)
    private String messageId;

    @Column(nullable = false)
    private boolean hasAttachments;

    @OneToMany(mappedBy = "email", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Attachment> attachments = new ArrayList<>();

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

    public Email(String senderName, String senderEmail, String subject, String body, String messageId, boolean hasAttachments) {
        this.senderName = senderName;
        this.senderEmail = senderEmail;
        this.subject = subject;
        this.body = body;
        this.messageId = messageId;
        this.hasAttachments = hasAttachments;
    }
}