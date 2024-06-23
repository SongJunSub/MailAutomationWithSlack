package com.example.mailautomationwithslack.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    public Attachment(String fileName, String filePath, Email email) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.email = email;

        email.getAttachments().add(this);
    }

}