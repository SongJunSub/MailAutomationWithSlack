package com.example.slack.dto;

import lombok.Data;

@Data
public class SlackMessageDTO {

    private String channel;

    private String text;

    public SlackMessageDTO(String channel, String text) {
        this.channel = channel;
        this.text = text;
    }

}