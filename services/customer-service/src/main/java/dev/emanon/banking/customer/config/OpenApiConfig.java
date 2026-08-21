package dev.emanon.banking.customer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customerServiceOpenApi() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Customer Service API")
                                .description(
                                        "REST API for managing banking customers"
                                )
                                .version("v1")
                                .contact(
                                        new Contact()
                                                .name("Banking Platform")
                                )
                                .license(
                                        new License()
                                                .name("MIT")
                                )
                );
    }
}