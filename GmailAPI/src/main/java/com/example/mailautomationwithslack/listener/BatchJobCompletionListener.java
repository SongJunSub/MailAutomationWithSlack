package com.example.mailautomationwithslack.listener;

import com.example.mailautomationwithslack.dto.EmailDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BatchJobCompletionListener implements JobExecutionListener {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.template.default-topic}")
    private String topicName;

    @Override
    public void beforeJob(JobExecution jobExecution) {

    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        List<EmailDTO> emailDTOList = (List<EmailDTO>) jobExecution.getExecutionContext().get("emailDTOList");

        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            try {
                for (EmailDTO emailDTO : emailDTOList) {
                    kafkaTemplate.send(topicName, objectMapper.writeValueAsString(emailDTO));
                }
            }
            catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            kafkaTemplate.send(topicName, "after Job Exception Test");
        }
    }

}