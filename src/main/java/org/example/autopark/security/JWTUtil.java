package org.example.autopark.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Date;

@Component
public class JWTUtil {
    @Value("{jwt_secret}")
    private String secret;

    public String generateToken(String username){
        Date expirstionDate = Date.from(ZonedDateTime.now().plusMinutes(60).toInstant());

        return JWT.create()
                .withSubject("SimpleUser details")
                .withClaim("username", username)
                .withIssuedAt(new Date())
                .withIssuer("Saxophilyman")
                .withExpiresAt(expirstionDate)
                .sign(Algorithm.HMAC256(secret));
    }

    public String validateTokenAndRetrieve(String token) throws JWTVerificationException{
        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(secret))
                .withSubject("SimpleUser details")
                .withIssuer("Saxophilyman")
                .build();
        DecodedJWT decodedJWT = jwtVerifier.verify(token);
        return decodedJWT.getClaim("username").asString();
    }

}
