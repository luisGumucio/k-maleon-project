package com.kmaleon.controller;

import com.kmaleon.dto.AccountBalanceResponse;
import com.kmaleon.dto.AccountDepositRequest;
import com.kmaleon.dto.AccountDepositResponse;
import com.kmaleon.dto.AccountSummaryResponse;
import com.kmaleon.security.AuthenticatedUser;
import com.kmaleon.security.Roles;
import com.kmaleon.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("/deposit")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountDepositResponse deposit(@AuthenticationPrincipal AuthenticatedUser caller,
                                          @Valid @RequestBody AccountDepositRequest request) {
        return accountService.deposit(caller.getId(), request);
    }

    @GetMapping("/deposits")
    public List<AccountDepositResponse> getDeposits(@AuthenticationPrincipal AuthenticatedUser caller) {
        return accountService.getDeposits(caller.getId());
    }

    @GetMapping("/summary")
    public AccountSummaryResponse getSummary(@AuthenticationPrincipal AuthenticatedUser caller) {
        return accountService.getSummary(caller.getId());
    }
}
