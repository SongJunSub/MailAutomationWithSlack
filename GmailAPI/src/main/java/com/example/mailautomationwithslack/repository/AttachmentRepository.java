package com.example.mailautomationwithslack.repository;

import com.example.mailautomationwithslack.domain.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {


}