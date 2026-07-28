package br.com.fiap.saude.susmock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SusMockServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SusMockServiceApplication.class, args);
    }
}
