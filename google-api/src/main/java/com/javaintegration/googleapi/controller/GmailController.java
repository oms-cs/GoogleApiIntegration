package com.javaintegration.googleapi.controller;


import com.google.api.services.gmail.model.Message;
import com.javaintegration.googleapi.model.MessageClassification;
import com.javaintegration.googleapi.service.GmailService;
import org.apache.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class GmailController {

    private final GmailService gmailService;

    public GmailController(GmailService gmailService){
        this.gmailService = gmailService;
    }

    @GetMapping("/messages")
    public ResponseEntity<List<Message>> getMails() throws GeneralSecurityException, IOException {
        return ResponseEntity.ok(gmailService.getMails());
    }

    @GetMapping("/messages/{id}")
    public ResponseEntity<String> getMailById(@PathVariable String id) throws GeneralSecurityException, IOException {
        return ResponseEntity.ok(gmailService.getMailById(id));
    }

    @GetMapping("/messages/open-ai/{id}")
    public ResponseEntity<MessageClassification> getMailClassification(@PathVariable String id) throws GeneralSecurityException, IOException {
        return ResponseEntity.status(HttpStatus.SC_CREATED).body(gmailService.getMailClassification(id));
    }
}
