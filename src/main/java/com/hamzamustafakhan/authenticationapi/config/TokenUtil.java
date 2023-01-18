package com.hamzamustafakhan.authenticationapi.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class TokenUtil implements Serializable {

    public static final long VALIDITY = 21600000;

    @Value("${jwt.secret}")
    private String jwtSecret;

    public Claims getClaims(String token) throws UnsupportedEncodingException {
        Claims claims = Jwts.parser().setSigningKey(jwtSecret.getBytes("UTF-8")).parseClaimsJws(token).getBody();
        return claims;
    }

    public String createToken(UserDetails userDetails, int id, String roleName) throws UnsupportedEncodingException {
        Map<String, Object> claims = new HashMap<>();
        HashMap userClaims = new HashMap();
        userClaims.put("email", userDetails.getUsername());
        userClaims.put("id", id);
        userClaims.put("issuer", "Hamza Mustafa Khan");
        userClaims.put("role", roleName);

        String token = Jwts.builder()
                .setClaims(userClaims)
                .setExpiration(new Date(System.currentTimeMillis() + VALIDITY))
                .signWith(SignatureAlgorithm.HS256, jwtSecret.getBytes("UTF-8"))
                .compact();

        return token;
    }

    public String refreshToken(Claims claims) throws UnsupportedEncodingException {
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + VALIDITY))
                .signWith(SignatureAlgorithm.HS256, jwtSecret.getBytes("UTF-8"))
                .compact();
    }

    public String getEmailFromToken(String token) throws UnsupportedEncodingException {
        Claims claims = getClaims(token);
        String email = claims.get("email", String.class);
        return email;
    }

    public Date getExpirationFromToken(String token) throws UnsupportedEncodingException {
        Claims claims = getClaims(token);
        Date date = claims.get("exp", Date.class);
        return date;
    }

    public boolean checkExpiration(String token) throws UnsupportedEncodingException {
        Date expirationDate = getExpirationFromToken(token);
        return expirationDate.before(new Date());
    }

    public boolean checkValidity(UserDetails userDetails, String token) throws UnsupportedEncodingException{
        String email = getEmailFromToken(token);
        return (!checkExpiration(token) && email.equals(userDetails.getUsername()));
    }
}
