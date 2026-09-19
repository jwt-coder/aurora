package com.aurora.service.impl;

import com.aurora.model.dto.UserDetailsDTO;
import com.aurora.service.RedisService;
import com.aurora.service.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static com.aurora.constant.AuthConstant.*;
import static com.aurora.constant.RedisConstant.LOGIN_USER;


@Slf4j
@Service
public class TokenServiceImpl implements TokenService {

    @Value("${jwt.secret}")
    private String secret;

    /**
     * 启动时根据 jwt.secret 初始化，避免每次请求重复解析
     */
    private SecretKey secretKey;

    @Autowired
    private RedisService redisService;

    @PostConstruct
    public void initSecretKey() {
        // 对 secret 原文做 SHA-256 派生 256 位密钥：任意口令（含含 -/_ 的口令）都能稳定复现，重启后 token 仍可验签
        if (StringUtils.hasText(secret) && secret.length() >= 32) {
            try {
                byte[] keyBytes = MessageDigest.getInstance("SHA-256")
                        .digest(secret.getBytes(StandardCharsets.UTF_8));
                secretKey = new SecretKeySpec(keyBytes, "AES");
                return;
            } catch (NoSuchAlgorithmException e) {
                // JVM 均内置 SHA-256，理论上不会到这里
                log.error("SHA-256 不可用，将生成随机密钥", e);
            }
        }
        // 密钥缺失或过短：为保证服务可用，使用随机密钥，但已签发的 token 在重启后全部失效
        log.error("jwt.secret 缺失或长度小于 32 字符，已生成随机密钥，请尽快在配置中设置足够强度的 jwt.secret");
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        secretKey = new SecretKeySpec(randomBytes, "AES");
    }

    @Override
    public String createToken(UserDetailsDTO userDetailsDTO) {
        refreshToken(userDetailsDTO);
        String userId = userDetailsDTO.getId().toString();
        return createToken(userId);
    }

    @Override
    public String createToken(String subject) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        SecretKey secretKey = generalKey();
        // 设置过期时间，与 Redis 会话 TTL 保持一致（7 天）
        return Jwts.builder().setId(getUuid()).setSubject(subject)
                .setIssuer("aurora")
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_TIME * 1000L))
                .signWith(signatureAlgorithm, secretKey).compact();
    }

    @Override
    public void refreshToken(UserDetailsDTO userDetailsDTO) {
        LocalDateTime currentTime = LocalDateTime.now();
        userDetailsDTO.setExpireTime(currentTime.plusSeconds(EXPIRE_TIME));
        String userId = userDetailsDTO.getId().toString();
        // 每个用户使用独立的 key，避免共享 Hash 的 TTL 被其他用户的登录/续期集体重置
        redisService.set(LOGIN_USER + ":" + userId, userDetailsDTO, EXPIRE_TIME);
    }

    @Override
    public void renewToken(UserDetailsDTO userDetailsDTO) {
        try {
            LocalDateTime expireTime = userDetailsDTO.getExpireTime();
            LocalDateTime currentTime = LocalDateTime.now();
            if (Duration.between(currentTime, expireTime).toMinutes() <= TWENTY_MINUTES) {
                refreshToken(userDetailsDTO);
            }
        } catch (Exception e) {
            // Redis超时时忽略token续期，不影响当前请求
        }
    }

    @Override
    public Claims parseToken(String token) {
        SecretKey secretKey = generalKey();
        // token 过期时抛出 ExpiredJwtException，由调用方捕获后返回 null，视为未登录
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    }

    @Override
    public UserDetailsDTO getUserDetailDTO(HttpServletRequest request) {
        String token = Optional.ofNullable(request.getHeader(TOKEN_HEADER)).orElse("").replaceFirst(TOKEN_PREFIX, "");
        if (StringUtils.hasText(token) && !token.equals("null")) {
            try {
                Claims claims = parseToken(token);
                String userId = claims.getSubject();
                return (UserDetailsDTO) redisService.get(LOGIN_USER + ":" + userId);
            } catch (Exception e) {
                // Redis超时、JWT解析失败或token已过期时返回null，不阻塞请求
                return null;
            }
        }
        return null;
    }

    @Override
    public void delLoginUser(Integer userId) {
        redisService.del(LOGIN_USER + ":" + userId);
    }

    public String getUuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public SecretKey generalKey() {
        return secretKey;
    }

}
