package com.example.mailautomationwithslack.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Getter
@NoArgsConstructor
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    private String filePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", referencedColumnName = "messageId")
    private Email email;

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

    public Attachment(String fileName, String filePath, Email email) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.email = email;

        email.getAttachments().add(this);
    }

}