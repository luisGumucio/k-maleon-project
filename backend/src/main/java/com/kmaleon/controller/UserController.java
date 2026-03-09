package com.kmaleon.controller;

import com.kmaleon.dto.UserCreateRequest;
import com.kmaleon.dto.UserResponse;
import com.kmaleon.dto.UserUpdateRequest;
import com.kmaleon.security.AuthenticatedUser;
import com.kmaleon.security.Roles;
import com.kmaleon.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@PreAuthorize(Roles.USERS_MANAGERS)
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> findAll(@AuthenticationPrincipal AuthenticatedUser caller) {
        return userService.findAll(caller.getRole());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody UserCreateRequest request,
                               @AuthenticationPrincipal AuthenticatedUser caller) {
        return userService.create(request, caller.getRole());
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable UUID id,
                               @RequestBody UserUpdateRequest request) {
        return userService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        userService.delete(id);
    }
}
