package com.bankofgeorgia.customer.security;

import com.bankofgeorgia.customer.model.Customer;
import com.bankofgeorgia.customer.model.Employee;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class JwtService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final JwtProperties properties;
    private final ObjectMapper objectMapper;

    public JwtService(JwtProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public String issueCustomerToken(Customer customer) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("role", "CUSTOMER");
        claims.put("name", customer.getName());
        claims.put("email", customer.getEmail());
        claims.put("username", customer.getUsername());
        return issueToken(customer.getId(), claims);
    }

    public String issueEmployeeToken(Employee employee) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("role", "EMPLOYEE");
        claims.put("name", employee.getName());
        claims.put("email", employee.getEmail());
        return issueToken(employee.getId(), claims);
    }

    private String issueToken(String subject, Map<String, Object> customClaims) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(properties.getExpirationMinutes() * 60);

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("iss", properties.getIssuer());
        payload.put("sub", subject);
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", expiresAt.getEpochSecond());
        payload.putAll(customClaims);

        String unsignedToken = encodeJson(header) + "." + encodeJson(payload);
        return unsignedToken + "." + sign(unsignedToken);
    }

    private String encodeJson(Map<String, Object> values) {
        try {
            return base64Url(objectMapper.writeValueAsBytes(values));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("unable to encode jwt", ex);
        }
    }

    private String sign(String unsignedToken) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(properties.getSecret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return base64Url(mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("unable to sign jwt", ex);
        }
    }

    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
