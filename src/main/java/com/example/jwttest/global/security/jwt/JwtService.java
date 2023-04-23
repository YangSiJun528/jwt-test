package com.example.jwttest.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    /**
     * JWT 서명에 사용할 비밀키입니다.
     */
    @Value("${auth.jwt.secret-key}")
    private String secretKey;

    /**
     * JWT 토큰의 만료 시간입니다.
     */
    @Value("${auth.jwt.expiration}")
    private long jwtExpiration;

    /**
     * JWT refresh 토큰의 만료 시간입니다.
     */
    @Value("${auth.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    /**
     * JWT 토큰에서 사용자 이름을 추출합니다.
     * @param token 추출할 JWT 토큰
     * @return JWT 토큰에서 추출된 사용자 이름
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * JWT 토큰에서 사용자 이름을 추출합니다.
     * @param token 추출할 JWT 토큰
     * @return JWT 토큰에서 추출된 사용자 이름
     */
    public String extractUserRole(String token) {
        return extractClaim(token, clamis -> clamis.getOrDefault("Role", ));
    }

    /**
     * JWT 토큰에서 주어진 {@link Function}을 이용하여 클레임을 추출합니다.
     * @param token 추출할 JWT 토큰
     * @param claimsResolver 추출할 클레임 정보를 담은 {@link Function}
     * @param <T> 추출할 클레임 정보의 타입
     * @return 추출된 클레임 정보
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 주어진 사용자 정보를 이용하여 JWT 토큰을 생성합니다.
     * @param userDetails JWT 토큰에 담을 사용자 정보
     * @return 생성된 JWT 토큰
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * 주어진 사용자 정보와 추가 클레임 정보를 이용하여 JWT 토큰을 생성합니다.
     * @param extraClaims JWT 토큰에 추가할 클레임 정보
     * @param userDetails JWT 토큰에 담을 사용자 정보
     * @return 생성된 JWT 토큰
     */
    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * 주어진 사용자 정보를 이용하여 JWT refresh 토큰을 생성합니다.
     * @param userDetails JWT refresh 토큰에 담을 사용자 정보
     * @return 생성된 JWT refresh 토큰
     */
    public String generateRefreshToken(
            UserDetails userDetails
    ) {
        return buildToken(new HashMap<>(), userDetails, refreshExpiration);
    }

    /**
     * 주어진 extraClaims, userDetails, expiration 정보를 이용해 JWT 토큰을 생성합니다.
     * @param extraClaims JWT 토큰에 추가할 클레임 정보
     * @param userDetails JWT 토큰에 저장할 사용자 정보
     * @param expiration JWT 토큰의 만료 시간 (밀리초)
     * @return 생성된 JWT 토큰
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .claim("Role", userDetails.getAuthorities()) // TODO authorities가 여러 개일 가능성 있음
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 주어진 JWT 토큰이 주어진 사용자 정보와 일치하며 만료되지 않았는지 검증합니다.
     * @param token 검증할 JWT 토큰
     * @param userDetails 검증에 사용할 사용자 정보
     * @return JWT 토큰이 유효한지 여부
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
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
     * 주어진 JWT 토큰에서 모든 클레임 값을 추출합니다.
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
     * secretKey를 디코딩하여 hmacShaKeyFor()를 사용하여 서명용 키(key)를 생성합니다.
     * @return 생성된 서명용 키(key)
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
