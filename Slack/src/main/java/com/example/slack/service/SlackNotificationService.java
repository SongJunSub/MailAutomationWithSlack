package com.example.slack.service;

import com.example.slack.dto.SlackMessageDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import okhttp3.RequestBody;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SlackNotificationService {

    public void sendSlackMessage(String channel, String message) {
        String slackURL = "https://slack.com/api/chat.postMessage";

        SlackMessageDTO slackMessageDTO = new SlackMessageDTO(channel, message);
    }

}