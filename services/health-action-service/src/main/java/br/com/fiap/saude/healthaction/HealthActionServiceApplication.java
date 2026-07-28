package br.com.fiap.saude.healthaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class HealthActionServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(HealthActionServiceApplication.class, args);
    }
}
