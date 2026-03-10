package dev.fsantana.expensesplitapi.security.services.impl;

import dev.fsantana.expensesplitapi.domain.exceptions.AppRuleException;
import dev.fsantana.expensesplitapi.domain.models.User;
import dev.fsantana.expensesplitapi.domain.repositories.UserRepository;
import dev.fsantana.expensesplitapi.security.models.Auth;
import dev.fsantana.expensesplitapi.security.services.SessionService;
import dev.fsantana.expensesplitapi.security.services.TokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;


    @Override
    @Transactional
    public Auth registerUser(User user) {
        Optional<User> byEmail = userRepository.findByEmail(user.getEmail());
        if (byEmail.isPresent()) {
            throw new AppRuleException("Email/password invalid");
        }
        String passwordEncoded = passwordEncoder.encode(user.getPasswordHash());
        user.setPasswordHash(passwordEncoded);
        userRepository.save(user);
        String token = tokenService.generateToken(user);
        String refreshToken = tokenService.generateRefreshToken();

        return new Auth(user, token, refreshToken);
    }

    @Override
    public Auth login(String email, String password) {
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        password
                )
        );
        User user = ((Auth) authenticate.getPrincipal()).getUser();
        String token = tokenService.generateToken(user);
        String refreshToken = tokenService.generateRefreshToken();

        return new Auth(user, token, refreshToken);
    }

}
