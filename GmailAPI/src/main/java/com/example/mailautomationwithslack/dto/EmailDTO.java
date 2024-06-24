package com.example.mailautomationwithslack.dto;

import lombok.Data;

@Data
public class EmailDTO {

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

}