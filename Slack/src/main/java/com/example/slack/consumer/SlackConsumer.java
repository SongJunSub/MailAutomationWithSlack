package com.example.slack.consumer;

import com.example.slack.dto.EmailDTO;
import com.example.slack.entity.EmailHistory;
import com.example.slack.repository.EmailHistoryRepository;
import com.example.slack.service.SlackNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SlackConsumer {

    private final EmailHistoryRepository emailHistoryRepository;
    private final SlackNotificationService slackNotificationService;

    @Value("${slack.channel}")
    private String slackChannelName;

    @KafkaListener(topics = "${spring.kafka.template.default-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void listener(@Payload EmailDTO emailDTO) {
        StringBuilder stringBuilder = new StringBuilder();

        try {
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

            stringBuilder.append("[").append(emailDTO.getStatus()).append("] 제목 : ").append(emailDTO.getSubject()).append("\n");
            stringBuilder.append("발송자명 : ").append(emailDTO.getSenderName()).append("\n");
            stringBuilder.append("발송자 메일 : ").append(emailDTO.getSenderEmail()).append("\n");
            stringBuilder.append("첨부파일 유무 : ").append(emailDTO.isHasAttachments());

            slackNotificationService.sendSlackMessage(slackChannelName, stringBuilder.toString());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}