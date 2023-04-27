package com.javaintegration.googleapi.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(value = "mail-classificationS")
public class MessageClassification {

    @Id
    private String id;
    private String mailSnippet;
    private String classification;

    public MessageClassification() {
    }
    public MessageClassification(String id, String mailSnippet, String classification) {
        this.id = id;
        this.mailSnippet = mailSnippet;
        this.classification = classification;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMailSnippet() {
        return mailSnippet;
    }

    public void setMailSnippet(String mailSnippet) {
        this.mailSnippet = mailSnippet;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }
}
