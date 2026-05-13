package com.bankofgeorgia.gateway.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@Component
public class JwtValidator {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final TypeReference<Map<String, Object>> CLAIMS_TYPE = new TypeReference<>() {};

    private final JwtProperties properties;
    private final ObjectMapper objectMapper;

    public JwtValidator(JwtProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public Optional<JwtPrincipal> validate(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return Optional.empty();
            }

            String unsignedToken = parts[0] + "." + parts[1];
            byte[] actualSignature = Base64.getUrlDecoder().decode(parts[2]);
            byte[] expectedSignature = sign(unsignedToken);
            if (!MessageDigest.isEqual(actualSignature, expectedSignature)) {
                return Optional.empty();
            }

            Map<String, Object> claims = objectMapper.readValue(
                    Base64.getUrlDecoder().decode(parts[1]),
                    CLAIMS_TYPE
            );

            if (!properties.getIssuer().equals(claims.get("iss"))) {
                return Optional.empty();
            }

            Object expiresAt = claims.get("exp");
            if (!(expiresAt instanceof Number exp) || Instant.now().getEpochSecond() >= exp.longValue()) {
                return Optional.empty();
            }

            Object subject = claims.get("sub");
            Object role = claims.get("role");
            if (!(subject instanceof String sub) || !(role instanceof String roleName)) {
                return Optional.empty();
            }

            return Optional.of(new JwtPrincipal(sub, roleName));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private byte[] sign(String unsignedToken) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(properties.getSecret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
        return mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8));
    }
}
