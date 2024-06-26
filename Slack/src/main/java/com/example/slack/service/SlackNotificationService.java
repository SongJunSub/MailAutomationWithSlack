package com.example.slack.service;

import com.example.slack.dto.SlackMessageDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class SlackNotificationService {

    private final ObjectMapper objectMapper;
    private final OkHttpClient okHttpClient = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Value("${slack.oauth.token}")
    private String slackToken;

    @Value("${slack.channel}")
    private String slackChannelName;

    public void sendSlackMessage(String message) throws JsonProcessingException {
        String slackURL = "https://slack.com/api/chat.postMessage";

        SlackMessageDTO slackMessageDTO = new SlackMessageDTO(slackChannelName, message);

        RequestBody requestBody = RequestBody.create(objectMapper.writeValueAsString(slackMessageDTO), JSON);

        Request request = new Request.Builder()
                .url(slackURL)
                .header("Authorization", "Bearer " + slackToken)
                .post(requestBody)
                .build();

        try (Response response = okHttpClient.newCall(request).execute()){
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}