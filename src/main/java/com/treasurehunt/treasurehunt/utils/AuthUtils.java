package com.treasurehunt.treasurehunt.utils;

import com.treasurehunt.treasurehunt.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

public class AuthUtils {

    // Signature for token generation
    private static final byte[] secret = Base64.getDecoder().decode("AMPtJss+/lcnifPliBUSAUEjBPH2ZQDaIX/jNhHkcbQ=");

    public static boolean verifyUser(String userId) {
        // Step1: The server verifies the credentials are correct and created an encrypted and signed token with a
        // private key
        return true;
    }

    public static String generateToken(User user) {

        Instant now = Instant.now();
        String jwt = Jwts.builder()
                         .setSubject(user.getUserId())
                         .setAudience("video demo")
                         //.claim("1d20", new Random().nextInt(20) + 1)
                         .claim("email", user.getEmail())
                         .setIssuedAt(Date.from(now))
                         .setExpiration(Date.from(now.plus(60, ChronoUnit.MINUTES)))
                         .signWith(Keys.hmacShaKeyFor(secret))
                         .compact();
        return jwt;
    }

    public static String getUserIdFromToken(String jwt) {
        // On subsequent requests, the token is decoded with the same private key and if valid the request is processed.
        // Hash-based Message Authentication Code (HMAC)
        try {
            Jws<Claims> result = Jwts.parser()
                                     .requireAudience("video demo")
                                     .setAllowedClockSkewSeconds(62)
                                     .setSigningKey(Keys.hmacShaKeyFor(secret))
                                     .parseClaimsJws(jwt);
            // The signature is still validated, and the JWT instance will still not be returned if the jwt string is
            // invalid, as expected.
            // You just get to ‘inspect’ the JWT data for key discovery before the parser validates it.

            return result.getBody().getSubject();
        } catch (Exception e) {
            throw new JwtTokenMissingException("Invalid token");
        }
    }
}
