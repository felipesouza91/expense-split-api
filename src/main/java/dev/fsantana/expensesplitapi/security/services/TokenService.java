package dev.fsantana.expensesplitapi.security.services;

import dev.fsantana.expensesplitapi.domain.models.User;
import io.jsonwebtoken.Claims;

import java.util.Map;
import java.util.function.Function;

public interface TokenService {

     String extractUserId(String token);

     <T> T extractClaim(String token, Function<Claims, T> claimsResolver) ;

     String generateToken(User userDetails);

     String generateRefreshToken();

     String generateToken(Map<String, Object> extraClaims, User userDetails) ;

     long getExpirationTime() ;

     long getRefreshTokenExpirationTime() ;

     boolean isTokenValid(String token, User userDetails) ;
}