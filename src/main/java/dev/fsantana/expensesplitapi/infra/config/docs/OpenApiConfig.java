package dev.fsantana.expensesplitapi.infra.config.docs;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "security_auth",
        type = SecuritySchemeType.HTTP ,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("Expense Split Api")
                        .description("API for clients")
                        .version("0.0.1")
                        .contact(new Contact()
                                .name("Felipe Santana")
                                .url( "https://fsantana.dev" )));
    }
}
