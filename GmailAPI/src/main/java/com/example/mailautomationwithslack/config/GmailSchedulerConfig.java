package com.example.mailautomationwithslack.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class GmailSchedulerConfig {

    private final JobLauncher jobLauncher;
    private final Job gmailJob;

    // 1분 마다 실행
    @Scheduled(cron = "0 */1 * * * *")
    public void performGmailJob() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("requestTime", System.currentTimeMillis())
                .toJobParameters();

        try {
            jobLauncher.run(gmailJob, jobParameters);
        } catch (JobExecutionAlreadyRunningException e) {
            System.out.println("Error Message: " + e.getMessage());
        }
    }

}