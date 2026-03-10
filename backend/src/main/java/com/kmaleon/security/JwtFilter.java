package com.kmaleon.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmaleon.model.UserProfile;
import com.kmaleon.repository.UserProfileRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    private final UserProfileRepository userProfileRepository;
    private final String jwksUrl;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, PublicKey> keyCache = new ConcurrentHashMap<>();

    public JwtFilter(UserProfileRepository userProfileRepository,
                     @Value("${supabase.url}") String supabaseUrl) {
        this.userProfileRepository = userProfileRepository;
        this.jwksUrl = supabaseUrl + "/auth/v1/.well-known/jwks.json";
        log.info("[JwtFilter] JWKS URL configured: {}", jwksUrl);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String header = request.getHeader("Authorization");

        log.debug("[JwtFilter] {} {} — Authorization header present: {}", request.getMethod(), path, header != null);

        if (header == null || !header.startsWith("Bearer ")) {
            log.debug("[JwtFilter] No Bearer token — passing through unauthenticated");
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        log.debug("[JwtFilter] Token received (first 20 chars): {}...", token.substring(0, Math.min(20, token.length())));

        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                log.warn("[JwtFilter] Malformed JWT — expected 3 parts, got {}", parts.length);
                filterChain.doFilter(request, response);
                return;
            }

            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));
            @SuppressWarnings("unchecked")
            Map<String, Object> jwtHeader = objectMapper.readValue(headerJson, Map.class);
            String kid = (String) jwtHeader.get("kid");
            String alg = (String) jwtHeader.get("alg");
            log.info("[JwtFilter] JWT header — kid: {}, alg: {}", kid, alg);

            PublicKey publicKey = keyCache.get(kid);
            if (publicKey == null) {
                log.info("[JwtFilter] kid '{}' not in cache — fetching JWKS from {}", kid, jwksUrl);
                refreshKeyCache();
                publicKey = keyCache.get(kid);
                log.info("[JwtFilter] After refresh — cache keys: {}", keyCache.keySet());
            }

            if (publicKey == null) {
                log.warn("[JwtFilter] No public key found for kid '{}' after JWKS refresh", kid);
                filterChain.doFilter(request, response);
                return;
            }

            log.debug("[JwtFilter] Verifying token with public key for kid '{}'", kid);
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            UUID userId = UUID.fromString(claims.getSubject());
            log.info("[JwtFilter] Token valid — userId: {}", userId);

            UserProfile profile = userProfileRepository.findById(userId).orElse(null);
            if (profile == null) {
                log.warn("[JwtFilter] No user_profile found for userId: {}", userId);
                filterChain.doFilter(request, response);
                return;
            }

            log.info("[JwtFilter] Authenticated — userId: {}, role: {}", userId, profile.getRole());
            String role = "ROLE_" + profile.getRole().toUpperCase();
            var auth = new UsernamePasswordAuthenticationToken(
                    new AuthenticatedUser(userId, profile.getRole(), profile.getName(), profile.getLocationId()),
                    null,
                    List.of(new SimpleGrantedAuthority(role))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception e) {
            log.warn("[JwtFilter] JWT validation failed — {}: {}", e.getClass().getSimpleName(), e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private void refreshKeyCache() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(jwksUrl))
                    .GET()
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            log.info("[JwtFilter] JWKS response status: {}", resp.statusCode());
            log.debug("[JwtFilter] JWKS response body: {}", resp.body());

            @SuppressWarnings("unchecked")
            Map<String, Object> jwks = objectMapper.readValue(resp.body(), Map.class);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> keys = (List<Map<String, Object>>) jwks.get("keys");

            log.info("[JwtFilter] JWKS contains {} key(s)", keys == null ? 0 : keys.size());
            if (keys != null) {
                for (Map<String, Object> key : keys) {
                    String keyId = (String) key.get("kid");
                    String kty = (String) key.get("kty");
                    String keyAlg = (String) key.get("alg");
                    log.info("[JwtFilter] Found JWKS key — kid: {}, kty: {}, alg: {}", keyId, kty, keyAlg);
                    if ("EC".equals(kty) && keyId != null) {
                        try {
                            PublicKey publicKey = buildEcPublicKey(key);
                            keyCache.put(keyId, publicKey);
                            log.info("[JwtFilter] Cached EC public key for kid: {}", keyId);
                        } catch (Exception e) {
                            log.error("[JwtFilter] Failed to build EC public key for kid {}: {}", keyId, e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("[JwtFilter] Error fetching JWKS: {} — {}", e.getClass().getSimpleName(), e.getMessage());
        }
    }

    private PublicKey buildEcPublicKey(Map<String, Object> jwk) throws Exception {
        byte[] xBytes = Base64.getUrlDecoder().decode((String) jwk.get("x"));
        byte[] yBytes = Base64.getUrlDecoder().decode((String) jwk.get("y"));

        java.security.AlgorithmParameters params = java.security.AlgorithmParameters.getInstance("EC");
        params.init(new java.security.spec.ECGenParameterSpec("secp256r1"));
        ECParameterSpec ecParams = params.getParameterSpec(ECParameterSpec.class);

        ECPoint point = new ECPoint(new BigInteger(1, xBytes), new BigInteger(1, yBytes));
        ECPublicKeySpec keySpec = new ECPublicKeySpec(point, ecParams);
        return KeyFactory.getInstance("EC").generatePublic(keySpec);
    }
}
