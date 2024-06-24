package com.example.mailautomationwithslack.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BatchJobCompletionListener implements JobExecutionListener {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.template.default-topic}")
    private String topicName;

    @Override
    public void beforeJob(JobExecution jobExecution) {

    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            kafkaTemplate.send(topicName, "after Job Test");
        }
        else {
            kafkaTemplate.send(topicName, "after Job Exception Test");
        }
    }

}