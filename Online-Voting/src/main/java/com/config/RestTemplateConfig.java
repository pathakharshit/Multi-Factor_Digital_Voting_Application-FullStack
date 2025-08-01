package com.example.Online.Voting.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * This configuration class creates a RestTemplate bean.
 * The RestTemplate is used for making HTTP calls to external services,
 * such as our Python OCR microservice.
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Creates a new RestTemplate bean that can be injected wherever needed.
     *
     * @return a new instance of RestTemplate.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
