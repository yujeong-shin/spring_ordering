package com.encore.ordering.securities;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {
    @Value("${jwt.secretKey}")
    private String secretKey;
    @Value("${jwt.expiration}")
    private int expiration;

    public String createToken(String email, String role){
        //claims : 토큰 사용자에 대한 속성이나 데이터를 포함. 주로 페이로드를 의미
        //넘겨줄 정보들을 claims에 담으면 됨
        Claims claims = Jwts.claims().setSubject(email); //PAYLOAD에 {"sub": email 값}으로 들어감
        log.debug("expiration " + expiration);
        log.debug("secretKey " + secretKey);

        claims.put("role", role); //PAYLOAD에 {"role": "ADMIN"}으로 들어감

        Date now = new Date();
        JwtBuilder jwtBuilder = Jwts.builder();
        jwtBuilder.setClaims(claims);
        jwtBuilder.setIssuedAt(now);
        jwtBuilder.setExpiration(new Date(now.getTime() + expiration*60*1000L));
        jwtBuilder.signWith(SignatureAlgorithm.HS256, "mysecret");
        return jwtBuilder.compact();

//        String token = Jwts.builder()
//                .setClaims(claims)
//                .setIssuedAt(now)
//                .setExpiration(new Date(now.getTime() + 30*60*1000L))
//                .signWith(SignatureAlgorithm.HS256, "mySecretKey")
//                .compact();
//        return token;
    }
}
