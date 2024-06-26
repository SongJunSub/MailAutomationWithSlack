package com.example.slack.dto;

import lombok.Data;

@Data
public class SlackMessageDTO {

    private String channel;

    private String message;

    public SlackMessageDTO(String channel, String message) {
        this.channel = channel;
        this.message = message;
    }

}