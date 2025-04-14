package com.yardflowpro.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Value("${server.port}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("YardFlow Pro API")
                        .description("Intelligent Logistics Command Center API documentation")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("YardFlow Pro Support")
                                .email("support@yardflowpro.com")
                                .url("https://www.yardflowpro.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://www.yardflowpro.com/license")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Development Server")
                ));
    }
}