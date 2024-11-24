package com.pwr.project.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")  // Zastosuj CORS do wszystkich endpointów zaczynających się od "/api/"
                .allowedOrigins("http://localhost:4200")  // Pozwól na żądania z front-endu Angular działającego na localhost:4200
                .allowedMethods("GET", "POST", "PUT", "DELETE")  // Dozwolone metody HTTP
                .allowedHeaders("Authorization", "Content-Type")  // Dozwolone nagłówki
                .allowCredentials(true)  // Zezwalaj na ciasteczka (cookies)
                .maxAge(3600);  // Czas przechowywania informacji o CORS w przeglądarce (w sekundach)
    }
}
