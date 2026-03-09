package com.kmaleon.service;

import com.kmaleon.dto.LoginResponse;
import com.kmaleon.dto.UserProfileResponse;
import com.kmaleon.model.UserProfile;
import com.kmaleon.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AuthService {

    private final UserProfileRepository userProfileRepository;
    private final String supabaseUrl;
    private final String supabaseServiceKey;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthService(UserProfileRepository userProfileRepository,
                       @Value("${supabase.url}") String supabaseUrl,
                       @Value("${supabase.service-key}") String supabaseServiceKey) {
        this.userProfileRepository = userProfileRepository;
        this.supabaseUrl = supabaseUrl;
        this.supabaseServiceKey = supabaseServiceKey;
    }

    public LoginResponse login(String email, String password) {
        try {
            String body = objectMapper.writeValueAsString(Map.of("email", email, "password", password));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(supabaseUrl + "/auth/v1/token?grant_type=password"))
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .header("apikey", supabaseServiceKey)
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> json = objectMapper.readValue(response.body(), Map.class);
            String token = (String) json.get("access_token");

            UUID userId = extractUserId(token);
            UserProfile profile = userProfileRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario sin perfil asignado"));

            return new LoginResponse(token, profile.getRole(), profile.getName());

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al autenticar");
        }
    }

    public void logout(String bearerToken) {
        try {
            String token = bearerToken.startsWith("Bearer ") ? bearerToken.substring(7) : bearerToken;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(supabaseUrl + "/auth/v1/logout"))
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .header("apikey", supabaseServiceKey)
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            // logout best-effort, no lanzar error
        }
    }

    public UserProfileResponse me(UUID userId) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil no encontrado"));
        return UserProfileResponse.from(profile);
    }

    private UUID extractUserId(String token) {
        try {
            // El token ya fue validado por Supabase — solo extraemos el payload sin reverificar
            String[] parts = token.split("\\.");
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            @SuppressWarnings("unchecked")
            Map<String, Object> claims = objectMapper.readValue(payload, Map.class);
            return UUID.fromString((String) claims.get("sub"));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido");
        }
    }
}
