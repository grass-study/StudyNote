package com.ciny.studynote.global.component;

import com.ciny.studynote.dto.TokenInfo;
import com.ciny.studynote.model.User;
import com.ciny.studynote.repository.UserRepository;
import com.ciny.studynote.security.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {
    private final Key key;
    @Value("${spring.security.jwt.access_token_lifetime_in_ms}")
    private long ACCESS_TOKEN_LIFETIME_IN_MS;
    @Value("${spring.security.jwt.refresh_token_lifetime_in_ms}")
    private long REFRESH_TOKEN_LIFETIME_IN_MS;

    private final UserRepository userRepository;

    public JwtTokenProvider(
            @Value("${spring.security.jwt.secret}") String secretKey,
            UserRepository userRepository) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.userRepository = userRepository;
    }

    public TokenInfo generateToken(Authentication authentication) {
        // 권한 가져오기
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        // 액세스토큰 생성
        String accessToken = Jwts.builder()
                .setHeaderParam("type", "JWT")  // header: jwt 타입 명시
                .setSubject(authentication.getName())       // payload: 유저 아이디를 토큰 제목으로 사용
                .claim("auth", authorities)           // payload: 사용자 role
                .setExpiration(new Date(now + ACCESS_TOKEN_LIFETIME_IN_MS))        // payload: 유효기간
                .signWith(key, SignatureAlgorithm.HS256)    // signature
                .compact();

        // 리프레시토큰 생성
        String refreshToken = Jwts.builder()
                .setExpiration(new Date(now + REFRESH_TOKEN_LIFETIME_IN_MS))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return TokenInfo.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenLifetime(ACCESS_TOKEN_LIFETIME_IN_MS)
                .refreshTokenLifetime(REFRESH_TOKEN_LIFETIME_IN_MS)
                .build();
    }

    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);

        if (claims.get("auth") == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다");
        }

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("auth").toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        User user = userRepository.findByUsername(claims.getSubject()).orElse(null);

        if (user == null) {
            throw new NullPointerException("해당 유저 정보를 찾을 수 없습니다");
        }

        UserDetailsImpl principal = new UserDetailsImpl(user);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
        }
        return false;
    }

    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}
