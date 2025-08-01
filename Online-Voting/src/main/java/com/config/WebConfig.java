package com.example.Online.Voting.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // allow CORS on all paths
                .allowedOrigins("http://localhost:5500")  // or the domain(s) you need
                .allowedMethods("*")  // GET, POST, PUT, etc.
                .allowedHeaders("*")
                .allowCredentials(true);

    }
}
