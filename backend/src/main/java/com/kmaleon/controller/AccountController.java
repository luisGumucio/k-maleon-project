package com.kmaleon.controller;

import com.kmaleon.dto.AccountBalanceResponse;
import com.kmaleon.service.AccountService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/balance")
    public AccountBalanceResponse getBalance() {
        return accountService.getBalance();
    }

    @PostMapping("/initial-balance")
    public AccountBalanceResponse setInitialBalance(@RequestBody Long amount) {
        return accountService.setInitialBalance(amount);
    }
}
