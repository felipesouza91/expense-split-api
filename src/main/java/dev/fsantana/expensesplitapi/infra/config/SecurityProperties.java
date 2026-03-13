package dev.fsantana.expensesplitapi.infra.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties("api.security")
@Valid
public class SecurityProperties {

    private final TokenProperties token = new TokenProperties();

    @NotNull
    private List<String> allowedOrigins ;

    @Getter
    @Setter
    public class TokenProperties {

        @NotBlank
        private String secret;
        @NotBlank
        private Integer tokenExpirationTime;
        @NotBlank
        private Integer refreshTokenExpirationTime;
    }
}