package com.example.mailautomationwithslack.config;

import com.example.mailautomationwithslack.domain.Attachment;
import com.example.mailautomationwithslack.domain.Email;
import com.example.mailautomationwithslack.dto.EmailDTO;
import com.example.mailautomationwithslack.listener.BatchJobCompletionListener;
import com.example.mailautomationwithslack.repository.AttachmentRepository;
import com.example.mailautomationwithslack.repository.EmailRepository;
import com.example.mailautomationwithslack.service.GmailApiService;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.api.services.gmail.model.MessagePartHeader;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class GmailBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final GmailApiService gmailApiService;
    private final EmailRepository emailRepository;
    private final AttachmentRepository attachmentRepository;
    private final Gmail gmail;
    private final BatchJobCompletionListener jobCompletionListener;

    private static final Logger logger = Logger.getLogger(GmailBatchConfig.class.getName());

    @Value("${gmail.attachment.directory}")
    private String attachmentDirectory;

    @Bean
    public Job gmailBatchJob(Step gmailBatchStep) {
        return new JobBuilder("emailBatchJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobCompletionListener)
                .start(gmailBatchStep)
                .build();
    }

    @Bean
    @JobScope
    public Step gmailBatchStep(Tasklet gmailBatchTasklet) {
        return new StepBuilder("emailBatchStep", jobRepository)
                .tasklet(gmailBatchTasklet, transactionManager)
                .build();
    }

    @Bean
    @StepScope
    public Tasklet gmailBatchTasklet() {
        return (contribution, chunkContext) -> {
            List<Message> messages = gmailApiService.listMessages();
            List<EmailDTO> emailDTOList = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (Message message : messages) {
                EmailDTO emailDTO = new EmailDTO();

                emailDTO.setCreatedUser("MAIL_AUTOMATION");
                emailDTO.setCreatedDate(LocalDateTime.now().format(formatter));
                emailDTO.setUpdatedUser("MAIL_AUTOMATION");
                emailDTO.setUpdatedDate(LocalDateTime.now().format(formatter));

                try {
                    if (!emailRepository.existsByMessageId(message.getId())) {
                        String sender = getHeader(message, "From");
                        String subject = getHeader(message, "Subject");
                        String body = getBody(message);
                        boolean hasAttachments = checkForAttachments(message);

                        if (sender != null && subject != null) {
                            Pattern pattern = Pattern.compile("\"([^\"]+)\"\\s*<([^>]+)>");
                            Matcher matcher = pattern.matcher(sender);
                            String senderName = sender;
                            String senderEmail = sender;

                            if (matcher.find()) {
                                senderName = matcher.group(1);
                                senderEmail = matcher.group(2);
                            }

                            Email email = new Email(senderName, senderEmail, subject, body, message.getId(), hasAttachments);

                            emailRepository.save(email);

                            if (hasAttachments) {
                                saveAttachments(message, email);
                            }

                            emailDTO.setSenderName(senderName);
                            emailDTO.setSenderEmail(senderEmail);
                            emailDTO.setSubject(subject);
                            emailDTO.setStatus("SUCCESS");
                            emailDTO.setResult("정상적으로 처리되었습니다.");
                            emailDTO.setHasAttachments(hasAttachments);
                            emailDTO.setMessageId(message.getId());
                        } else {
                            logger.severe("Email data is missing: sender=" + sender + ", subject=" + subject + ", body=" + body);

                            emailDTO.setStatus("FAILED");
                            emailDTO.setResult("Email data is missing: sender=" + sender + ", subject=" + subject + ", body=" + body);
                        }
                    }
                }
                catch (Exception e) {
                    logger.severe(e.getMessage());

                    emailDTO.setStatus("FAILED");
                    emailDTO.setResult("전송 중 문제가 발생하였습니다.\n" + e.getMessage());
                }

                emailDTOList.add(emailDTO);
            }

            chunkContext.getStepContext().getStepExecution().getJobExecution()
                    .getExecutionContext().put("emailDTOList", emailDTOList);

            return RepeatStatus.FINISHED;
        };
    }

    private String getHeader(Message message, String headerName) {
        if (message.getPayload() != null && message.getPayload().getHeaders() != null) {
            Optional<MessagePartHeader> mailHeader = message.getPayload().getHeaders().stream()
                    .filter(header -> header.getName().equals(headerName))
                    .findFirst();

            if (mailHeader.isPresent()) {
                return mailHeader.get().getValue();
            }
        }

        return null;
    }

    private String getBody(Message message) {
        if (message != null) {
            String mainText = message.getSnippet();

            if (mainText != null && !mainText.isEmpty()) {
                return mainText;
            }

            if (message.getPayload() != null) {
                StringBuilder stringBuilder = new StringBuilder();

                if (message.getPayload().getBody() != null && message.getPayload().getBody().getData() != null) {
                    String html = new String(message.getPayload().getBody().decodeData());
                    Document document = Jsoup.parse(html);
                    String text = document.text();

                    stringBuilder.append(text).append("\n");
                }

                if (message.getPayload().getParts() != null) {
                    for (MessagePart messagePart : message.getPayload().getParts()) {
                        processMessagePart(messagePart, stringBuilder);
                    }
                }

                return stringBuilder.toString().trim();
            }
        }

        return null;
    }

    private void processMessagePart(MessagePart messagePart, StringBuilder stringBuilder) {
        if (messagePart.getBody() != null && messagePart.getBody().getData() != null) {
            String html = new String(messagePart.getBody().decodeData());
            Document document = Jsoup.parse(html);
            String text = document.text();

            stringBuilder.append(text).append("\n");
        }

        if (messagePart.getParts() != null) {
            for (MessagePart part : messagePart.getParts()) {
                processMessagePart(part, stringBuilder);
            }
        }
    }

    private boolean checkForAttachments(Message message) {
        if (message.getPayload() != null) {
            if (message.getPayload().getParts() != null) {
                for (MessagePart messagePart : message.getPayload().getParts()) {
                    if (messagePart.getFilename() != null && !messagePart.getFilename().isEmpty()) {
                        return true;
                    }

                    if (messagePart.getParts() != null) {
                        for (MessagePart nestedPart : messagePart.getParts()) {
                            if (nestedPart.getFilename() != null && !nestedPart.getFilename().isEmpty()) {
                                return true;
                            }
                        }
                    }
                }
            } else if (message.getPayload().getFilename() != null && !message.getPayload().getFilename().isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private void saveAttachments(Message message, Email email) throws IOException {
        if (message.getPayload() != null && message.getPayload().getParts() != null) {
            for (MessagePart part : message.getPayload().getParts()) {
                if (part.getFilename() != null && !part.getFilename().isEmpty()) {
                    saveAttachment(part, email, message.getId());
                }

                if (part.getParts() != null) {
                    for (MessagePart nestedPart : part.getParts()) {
                        if (nestedPart.getFilename() != null && !nestedPart.getFilename().isEmpty()) {
                            saveAttachment(nestedPart, email, message.getId());
                        }
                    }
                }
            }
        }
    }

    private void saveAttachment(MessagePart messagePart, Email email, String messageId) throws IOException {
        final String fileName = messagePart.getFilename();
        String filePath = attachmentDirectory + File.separator + fileName;

        File directory = new File(attachmentDirectory);

        if (!directory.exists()) {
            directory.mkdir();
        }

        byte[] fileBytes = getAttachmentData(messagePart, messageId);

        if (fileBytes != null) {
            File file = new File(attachmentDirectory, fileName);
            String newFileName;
            int count = 1;

            while (file.exists()) {
                int dotIndex = fileName.lastIndexOf(".");

                if (dotIndex != -1) {
                    newFileName = fileName.substring(0, dotIndex) + "_" + count + fileName.substring(dotIndex);
                } else {
                    newFileName = fileName + "_" + count;
                }

                file = new File(attachmentDirectory, newFileName);
                filePath = attachmentDirectory + File.separator + newFileName;

                count++;
            }

            try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
                fileOutputStream.write(fileBytes);

                logger.info("Successfully saved attachment: " + fileName);
            } catch (Exception e) {
                logger.severe("Save File Error: " + fileName + " / Error Message: " + e.getMessage());
                throw e;
            }
        } else {
            logger.severe("No data found for attachment: " + fileName);
        }

        Attachment attachment = new Attachment(fileName, filePath, email);

        attachmentRepository.save(attachment);
    }

    private byte[] getAttachmentData(MessagePart messagePart, String messageId) throws IOException {
        if (messagePart.getBody() != null) {
            if (messagePart.getBody().getData() != null) {
                try {
                    // 표준 Base64
                    if (messagePart.getBody().getData().matches("^[A-Za-z0-9+/=]+$")) {
                        return Base64.getDecoder().decode(messagePart.getBody().getData());
                    }
                    // URL-Safe Base64
                    else if (messagePart.getBody().getData().matches("^[A-Za-z0-9_-]+$")) {
                        return Base64.getUrlDecoder().decode(messagePart.getBody().getData());
                    }
                } catch (IllegalArgumentException e) {
                    logger.severe("Attachment Data Error: " + e.getMessage());
                }
            } else if (messagePart.getBody().getAttachmentId() != null) {
                MessagePartBody attachmentPart = gmail.users().messages().attachments()
                        .get("me", messageId, messagePart.getBody().getAttachmentId()).execute();

                if (attachmentPart != null && attachmentPart.getData() != null) {
                    try {
                        // 표준 Base64
                        if (attachmentPart.getData().matches("^[A-Za-z0-9+/=]+$")) {
                            return Base64.getDecoder().decode(attachmentPart.getData());
                        }
                        // URL-Safe Base64
                        else if (attachmentPart.getData().matches("^[A-Za-z0-9_-]+={0,2}$")) {
                            return Base64.getUrlDecoder().decode(attachmentPart.getData());
                        }
                    } catch (IllegalArgumentException e) {
                        logger.severe("Attachment Data Error: " + e.getMessage());
                    }
                }
            }
        }

        return null;
    }

}