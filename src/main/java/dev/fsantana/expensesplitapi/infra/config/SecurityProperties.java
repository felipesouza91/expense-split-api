package dev.fsantana.expensesplitapi.infra.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("api.security")
@Valid
public class SecurityProperties {

    private final TokenProperties token = new TokenProperties();

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