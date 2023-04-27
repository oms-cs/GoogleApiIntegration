package com.javaintegration.googleapi.repository;

import com.javaintegration.googleapi.model.MessageClassification;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MailRepository extends MongoRepository<MessageClassification, String> {
}
