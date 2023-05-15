package com.example.jwttest.global.security.jwt;

import com.example.jwttest.domain.user.enums.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtManager {
    private final JwtEnvironment env;

    /**
     * 주어진 사용자 정보와 추가 클레임 정보를 이용하여 JWT 토큰을 생성합니다.
     *
     * @param extraClaims JWT 토큰에 추가할 클레임 정보
     * @param userInfo    JWT 토큰에 담을 사용자 정보
     * @return 생성된 JWT 토큰
     */
    public String generateToken(
            Map<String, Object> extraClaims,
            UserInfo userInfo
    ) {
        return buildToken(extraClaims, userInfo, env.accessExpiration());
    }

    /**
     * 주어진 사용자 정보를 이용하여 JWT refresh 토큰을 생성합니다.
     *
     * @param userInfo JWT refresh 토큰에 담을 사용자 정보
     * @return 생성된 JWT refresh 토큰
     */
    public String generateRefreshToken(
            UserInfo userInfo
    ) {
        return buildToken(new HashMap<>(), userInfo, env.refreshExpiration());
    }

    /**
     * 주어진 extraClaims, userDetails, expiration 정보를 이용해 JWT 토큰을 생성합니다.
     *
     * @param extraClaims JWT 토큰에 추가할 클레임 정보
     * @param userInfo    JWT 토큰에 저장할 사용자 정보
     * @param expiration  JWT 토큰의 만료 시간 (밀리초)
     * @return 생성된 JWT 토큰
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            UserInfo userInfo,
            long expiration
    ) {
        JwtBuilder jwtBuilder = Jwts.builder()
                .setIssuer(env.issuer())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .setSubject(userInfo.userId().toString())
                .claim("userRole", userInfo.userRole().name());

        // extraClaims가 null이 아닌 경우 claims에 extraClaims를 추가합니다.
        if (extraClaims != null) {
            jwtBuilder = jwtBuilder.addClaims(extraClaims);
        }

        // 키와 알고리즘을 사용하여 JWT를 서명하고 컴팩트한 문자열 토큰을 반환합니다.
        return jwtBuilder.signWith(getSignInKey(), SignatureAlgorithm.HS256).compact();
    }

//    public boolean validate(String token) {
//        try {
//            Jwts
//                    .parserBuilder()
//                    .requireIssuer(env.issuer())
//                    .setSigningKey(getSignInKey())
//                    .build()
//                    .parseClaimsJws(token);
//            return true;
//            // TODO Expried 예외처리하기
//        } catch (JwtException | IllegalArgumentException e) {
//            log.error(e.toString());
//            return false;
//        }
//    }

    public boolean validate(String token) throws JwtException {
        Jwts
                .parserBuilder()
                .requireIssuer(env.issuer())
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token);
        return true;
    }

    /**
     * 주어진 JWT 토큰에서 모든 클레임 값을 추출합니다.
     *
     * @param token 추출할 JWT 토큰
     * @return JWT 토큰에서 추출한 모든 클레임
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 주어진 JWT 토큰의 만료 여부를 검사합니다.
     *
     * @param token 검사할 JWT 토큰
     * @return JWT 토큰의 만료 여부
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * 주어진 JWT 토큰의 만료일를 추출합니다.
     *
     * @param token 검사할 JWT 토큰
     * @return JWT 토큰의 만료일
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * JWT 토큰에서 주어진 {@link Function}을 이용하여 클레임을 추출합니다.
     *
     * @param token          추출할 JWT 토큰
     * @param claimsResolver 추출할 클레임 정보를 담은 {@link Function}
     * @param <T>            추출할 클레임 정보의 타입
     * @return 추출된 클레임 정보
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 로그인에 사용되는 HMAC-SHA 키를 반환합니다.
     *
     * @return 로그인에 사용되는 HMAC-SHA 키
     */
    private Key getSignInKey() {
        // 환경 변수에서 비밀 키를 디코딩하여 바이트 배열을 초기화합니다.
        byte[] keyBytes = Decoders.BASE64.decode(env.secretKey());
        // 디코딩된 키 바이트를 기반으로 새로운 HMAC-SHA 키 객체를 반환합니다.
        return Keys.hmacShaKeyFor(keyBytes);
    }


    /**
     * JWT 토큰에서 사용자 정보를 추출합니다.
     *
     * @param token 추출할 JWT 토큰
     * @return JWT 토큰에 저장된 사용자 정보
     */
    public UserInfo extractUserInfo(String token) {
        String userRole = extractClaim(token, claims -> claims.get("userRole", String.class));
        String userId = extractClaim(token, Claims::getSubject);
        return new UserInfo(
                UUID.fromString(userId),
                Role.valueOf(userRole)
        );
    }
}
