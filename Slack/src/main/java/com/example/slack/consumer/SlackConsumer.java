package com.example.slack.consumer;

import com.example.slack.dto.EmailDTO;
import com.example.slack.entity.EmailHistory;
import com.example.slack.repository.EmailHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SlackConsumer {

    private final ObjectMapper objectMapper;
    private final EmailHistoryRepository emailHistoryRepository;

    @KafkaListener(topics = "${spring.kafka.template.default-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void listener(String message) {
        try {
            EmailDTO emailDTO = objectMapper.readValue(message, EmailDTO.class);

            EmailHistory emailHistory = new EmailHistory(
                    emailDTO.getSenderName(),
                    emailDTO.getSenderEmail(),
                    emailDTO.getSubject(),
                    emailDTO.getStatus(),
                    emailDTO.getResult(),
                    emailDTO.isHasAttachments(),
                    emailDTO.getMessageId()
            );

            emailHistoryRepository.save(emailHistory);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}