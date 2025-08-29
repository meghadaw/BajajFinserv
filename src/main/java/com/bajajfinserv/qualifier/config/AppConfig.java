package com.bajajfinserv.qualifier.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // Set timeouts to handle network issues
        factory.setConnectTimeout(30000); // 30 seconds
        factory.setReadTimeout(60000);    // 60 seconds

        RestTemplate restTemplate = new RestTemplate(factory);

        return restTemplate;
    }
}
