package com.javaintegration.googleapi.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.javaintegration.googleapi.model.MessageClassification;
import com.javaintegration.googleapi.repository.MailRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Service
public class GmailService{

    private final OpenAIService openAIService;
    private final MailRepository mailRepository;
    private static final String APPLICATION_NAME = "MAIL MANIPULATION";
    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    /**
     * Directory to store authorization tokens for this application.
     */
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static final String USER = "me";
    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";


    public GmailService(OpenAIService openAIService,MailRepository mailRepository){
        this.openAIService = openAIService;
        this.mailRepository = mailRepository;
    }

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
        InputStream in = GmailService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        //returns an authorized Credential object.
        return credential;
    }

    public List<Message> getMails() throws IOException, GeneralSecurityException{
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        ListMessagesResponse messages = service.users().messages().list(USER).execute();
        List<Message> messageList = messages.getMessages();

        return messageList;
    }

    public String getMailById(String id) throws IOException, GeneralSecurityException {

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        Message message = null;
        try {
            message = service.users().messages().get(USER, id).setFormat("raw").execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String snippet = message.getSnippet();
        return new String(message.decodeRaw());
    }

    public MessageClassification getMailClassification(String id) throws IOException, GeneralSecurityException {

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        Message message = null;
        try {
            message = service.users().messages().get(USER, id).setFormat("raw").execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String snippet = message.getSnippet();

        String classification = openAIService.generatePrompt(snippet);

        JSONObject clfctn = new JSONObject(classification);
        JSONArray choices = clfctn.getJSONArray("choices");
        JSONObject value = (JSONObject) choices.get(0);
        JSONObject resp = value.getJSONObject("message");
        String openAiResponse = resp.getString("content");

        MessageClassification messageClassification = new MessageClassification();
        messageClassification.setMailSnippet(snippet);
        messageClassification.setClassification(openAiResponse);
        mailRepository.save(messageClassification);

        return messageClassification;
    }
}
