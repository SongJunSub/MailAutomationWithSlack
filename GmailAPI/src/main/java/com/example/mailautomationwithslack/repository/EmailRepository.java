package com.example.mailautomationwithslack.repository;

import com.example.mailautomationwithslack.domain.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailRepository extends JpaRepository<Email, Long> {

    boolean existsByMessageId(String messageId);

}