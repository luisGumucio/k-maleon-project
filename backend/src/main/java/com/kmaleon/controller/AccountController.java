package com.kmaleon.controller;

import com.kmaleon.dto.AccountBalanceResponse;
import com.kmaleon.security.AuthenticatedUser;
import com.kmaleon.security.Roles;
import com.kmaleon.service.AccountService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
@PreAuthorize(Roles.ADMIN_OR_SUPER)
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/balance")
    public AccountBalanceResponse getBalance(@AuthenticationPrincipal AuthenticatedUser caller) {
        return accountService.getBalance(caller.getId());
    }

    @PostMapping("/initial-balance")
    public AccountBalanceResponse setInitialBalance(@AuthenticationPrincipal AuthenticatedUser caller,
                                                    @RequestBody Long amount) {
        return accountService.setInitialBalance(caller.getId(), amount);
    }
}
