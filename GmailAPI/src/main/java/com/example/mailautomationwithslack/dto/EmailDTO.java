package com.example.mailautomationwithslack.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class EmailDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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

}