package com.javaintegration.googleapi.service;

import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OpenAIService {



        private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

        private final String prompt = "Classify this content into 3 categories Bugs, Sales, Updates ";

        private final String apiKey = "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";
        private final RestTemplate restTemplate = new RestTemplate();

        public String generatePrompt(String content)
        {
            String userPrompt = prompt + content;

            System.out.println("userPrompt  == "+userPrompt);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            JSONObject[] jsonArr = new JSONObject[1];

            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content",userPrompt);

            jsonArr[0] = message;

            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("model", "gpt-3.5-turbo");
            jsonRequest.put("messages",jsonArr);

            // We are including only some of the parameters to the json request
            //String requestJson = "{\"model\" : \"gpt-3.5-turbo\",\"messages\": [{\"role\": \"user\", \"content\": "+ userPrompt +"}]}";

            HttpEntity<String> request = new HttpEntity<>(jsonRequest.toString(), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(OPENAI_URL, request, String.class);



            return response.getBody();
        }
}
