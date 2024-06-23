package com.example.mailautomationwithslack.service;

import com.example.mailautomationwithslack.listener.StartupListener;
import com.example.mailautomationwithslack.repository.EmailRepository;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GmailApiService {

    private final Gmail gmail;
    private final EmailRepository emailRepository;

    public List<Message> listMessages() throws IOException {
        long startupTime = StartupListener.getStartUpTime();
        // 서버가 기동된 시간부터 읽지 않은 메시지만 Insert 되도록 한다.
        String setting = "is:unread after:" + startupTime;
        ListMessagesResponse response = gmail.users().messages().list("me").setQ(setting).execute();
        List<Message> messages = new ArrayList<>();

        if (response.getMessages() != null) {
            for (Message message : response.getMessages()) {
                if (!emailRepository.existsByMessageId(message.getId())) {
                    Message fullMessage = getMessage(message.getId());

                    messages.add(fullMessage);
                }
            }
        }

        return messages;
    }

    public Message getMessage(String messageId) throws IOException {
        return gmail.users().messages().get("me", messageId).execute();
    }

}