package com.kmaleon.security;

import com.kmaleon.model.UserProfile;
import com.kmaleon.repository.UserProfileRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.ECParameterSpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final UserProfileRepository userProfileRepository;
    private final String jwksUrl;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, PublicKey> keyCache = new ConcurrentHashMap<>();

    public JwtFilter(UserProfileRepository userProfileRepository,
                     @Value("${supabase.url}") String supabaseUrl) {
        this.userProfileRepository = userProfileRepository;
        this.jwksUrl = supabaseUrl + "/auth/v1/.well-known/jwks.json";
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            // Extraer kid del header del JWT sin verificar
            String[] parts = token.split("\\.");
            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));
            @SuppressWarnings("unchecked")
            Map<String, Object> jwtHeader = objectMapper.readValue(headerJson, Map.class);
            String kid = (String) jwtHeader.get("kid");

            PublicKey publicKey = keyCache.computeIfAbsent(kid, this::fetchPublicKey);
            if (publicKey == null) {
                filterChain.doFilter(request, response);
                return;
            }

            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            UUID userId = UUID.fromString(claims.getSubject());
            UserProfile profile = userProfileRepository.findById(userId).orElse(null);
            if (profile == null) {
                filterChain.doFilter(request, response);
                return;
            }

            String role = "ROLE_" + profile.getRole().toUpperCase();
            var auth = new UsernamePasswordAuthenticationToken(
                    new AuthenticatedUser(userId, profile.getRole(), profile.getName()),
                    null,
                    List.of(new SimpleGrantedAuthority(role))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private PublicKey fetchPublicKey(String kid) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(jwksUrl))
                    .GET()
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            @SuppressWarnings("unchecked")
            Map<String, Object> jwks = objectMapper.readValue(resp.body(), Map.class);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> keys = (List<Map<String, Object>>) jwks.get("keys");

            for (Map<String, Object> key : keys) {
                if (kid.equals(key.get("kid")) && "EC".equals(key.get("kty"))) {
                    return buildEcPublicKey(key);
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching JWKS: " + e.getMessage());
        }
        return null;
    }

    private PublicKey buildEcPublicKey(Map<String, Object> jwk) throws Exception {
        byte[] xBytes = Base64.getUrlDecoder().decode((String) jwk.get("x"));
        byte[] yBytes = Base64.getUrlDecoder().decode((String) jwk.get("y"));

        ECParameterSpec params = getP256Params();

        ECPoint point = new ECPoint(new BigInteger(1, xBytes), new BigInteger(1, yBytes));
        ECPublicKeySpec keySpec = new ECPublicKeySpec(point, params);
        return KeyFactory.getInstance("EC").generatePublic(keySpec);
    }

    private ECParameterSpec getP256Params() throws Exception {
        // Obtener los params de P-256 desde una key dummy para no depender de BouncyCastle
        java.security.AlgorithmParameters params = java.security.AlgorithmParameters.getInstance("EC");
        params.init(new java.security.spec.ECGenParameterSpec("secp256r1"));
        return params.getParameterSpec(ECParameterSpec.class);
    }
}
