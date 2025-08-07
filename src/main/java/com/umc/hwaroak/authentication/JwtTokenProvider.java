// JWT 발급, 검증, 파싱
package com.umc.hwaroak.authentication;

import com.umc.hwaroak.domain.common.Role;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.repository.MemberRepository;
import com.umc.hwaroak.response.ErrorCode;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    private final MemberRepository memberRepository;

    // .yml에 설정한 jwt.secret을 바탕으로 서명키 생성(위조 방지)
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long memberId) {
        Role role = memberRepository.findById(memberId)
                .map(Member::getRole)
                .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_NOT_FOUND));

        return createToken(memberId, role, accessTokenValidity, "ACCESS");
    }

    public String createRefreshToken(Long memberId) {
        Role role = memberRepository.findById(memberId)
                .map(Member::getRole)
                .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_NOT_FOUND));

        return createToken(memberId, role, refreshTokenValidity, "REFRESH");
    }

    private String createToken(Long memberId, Role role, long validity, String tokenType) {
        Date now = new Date();
        Key key = getSigningKey();

        return Jwts.builder()
                .setSubject(String.valueOf(memberId))
                .claim("authority", "ROLE_" + role.name())
                .claim("tokenType", tokenType)  // 🔥 추가
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + validity))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰에서 사용자 id 추출
    public String getMemberId(String token) {
        Key key = getSigningKey();
        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    // 토큰 파싱 및 유효성 검증
    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
            return e.getClaims(); // 만료되었어도 claims는 반환
        } catch (JwtException e) {
            throw new GeneralException(ErrorCode.EXPIRED_JWT_TOKEN);
        }
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT: {}", e.getMessage());
        } catch (SignatureException e) {
            log.warn("Invalid signature: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Illegal token: {}", e.getMessage());
        }
        return false;
    }

    // Spring Security 인증 객체 생성
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        if (!claims.get("authority").toString().startsWith("ROLE_")) {
            throw new GeneralException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("authority").toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        return new UsernamePasswordAuthenticationToken(getMemberId(token), null, authorities);
    }
}