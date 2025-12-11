package com.lunchconnect;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.core.context.SecurityContextHolder;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class LunchconnectBackendApplication {
    @PostConstruct
    public void enableThreadLocalContext() {
        // Asegura que el contexto de seguridad permanezca en el hilo de la solicitud.
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_THREADLOCAL);
    }

    public static void main(String[] args) {
        SpringApplication.run(LunchconnectBackendApplication.class, args);
    }
}