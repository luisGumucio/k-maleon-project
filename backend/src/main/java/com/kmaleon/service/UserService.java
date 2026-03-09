package com.kmaleon.service;

import com.kmaleon.dto.UserCreateRequest;
import com.kmaleon.dto.UserResponse;
import com.kmaleon.dto.UserUpdateRequest;
import com.kmaleon.model.UserProfile;
import com.kmaleon.repository.UserProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private static final Set<String> VALID_ROLES = Set.of(
            "super_admin", "admin", "inventory_admin", "almacenero", "encargado_sucursal");

    private static final Set<String> INVENTORY_ADMIN_ALLOWED_ROLES = Set.of(
            "almacenero", "encargado_sucursal");

    private final UserProfileRepository userProfileRepository;
    private final String supabaseUrl;
    private final String supabaseServiceKey;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserService(UserProfileRepository userProfileRepository,
                       @Value("${supabase.url}") String supabaseUrl,
                       @Value("${supabase.service-key}") String supabaseServiceKey) {
        this.userProfileRepository = userProfileRepository;
        this.supabaseUrl = supabaseUrl;
        this.supabaseServiceKey = supabaseServiceKey;
    }

    public List<UserResponse> findAll(String callerRole) {
        List<UserProfile> profiles;
        if ("super_admin".equals(callerRole)) {
            profiles = userProfileRepository.findAll();
        } else {
            profiles = userProfileRepository.findByRoleIn(INVENTORY_ADMIN_ALLOWED_ROLES);
        }
        return profiles.stream().map(UserResponse::from).toList();
    }

    @Transactional
    public UserResponse create(UserCreateRequest request, String callerRole) {
        if (!VALID_ROLES.contains(request.getRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol inválido: " + request.getRole());
        }
        if ("inventory_admin".equals(callerRole) && !INVENTORY_ADMIN_ALLOWED_ROLES.contains(request.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para crear este rol");
        }
        if ("encargado_sucursal".equals(request.getRole()) && request.getLocationId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Se requiere locationId para encargado_sucursal");
        }

        UUID userId = createSupabaseUser(request.getEmail(), request.getPassword());

        UserProfile profile = new UserProfile();
        profile.setId(userId);
        profile.setName(request.getName());
        profile.setRole(request.getRole());
        profile.setLocationId(request.getLocationId());

        return UserResponse.from(userProfileRepository.save(profile));
    }

    @Transactional
    public UserResponse update(UUID id, UserUpdateRequest request) {
        UserProfile profile = userProfileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (request.getName() != null) {
            profile.setName(request.getName());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            updateSupabasePassword(id, request.getPassword());
        }

        return UserResponse.from(userProfileRepository.save(profile));
    }

    @Transactional
    public void delete(UUID id) {
        userProfileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        deleteSupabaseUser(id);
        userProfileRepository.deleteById(id);
    }

    // --- Supabase Auth Admin API ---

    private UUID createSupabaseUser(String email, String password) {
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "email", email,
                    "password", password,
                    "email_confirm", true));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(supabaseUrl + "/auth/v1/admin/users"))
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .header("apikey", supabaseServiceKey)
                    .header("Authorization", "Bearer " + supabaseServiceKey)
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200 && response.statusCode() != 201) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error al crear usuario en Supabase: " + response.body());
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> json = objectMapper.readValue(response.body(), Map.class);
            return UUID.fromString((String) json.get("id"));

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al crear usuario en Supabase", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al crear usuario: " + e.getMessage());
        }
    }

    private void updateSupabasePassword(UUID userId, String newPassword) {
        try {
            String body = objectMapper.writeValueAsString(Map.of("password", newPassword));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(supabaseUrl + "/auth/v1/admin/users/" + userId))
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .header("apikey", supabaseServiceKey)
                    .header("Authorization", "Bearer " + supabaseServiceKey)
                    .PUT(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al actualizar contraseña");
        }
    }

    private void deleteSupabaseUser(UUID userId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(supabaseUrl + "/auth/v1/admin/users/" + userId))
                    .header("apikey", supabaseServiceKey)
                    .header("Authorization", "Bearer " + supabaseServiceKey)
                    .DELETE()
                    .build();

            httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al eliminar usuario");
        }
    }
}
