package dev.fsantana.expensesplitapi.infra.config;

import dev.fsantana.expensesplitapi.domain.exceptions.AppEntityNotFound;
import dev.fsantana.expensesplitapi.domain.models.User;
import dev.fsantana.expensesplitapi.domain.repositories.UserRepository;
import dev.fsantana.expensesplitapi.security.models.Auth;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userDataProvider;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User user =  userDataProvider.findByEmail(username)
                    .orElseThrow(() -> new AppEntityNotFound("User not found"));
            return new Auth(user, null, null);
        };
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }
}
