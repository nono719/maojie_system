package com.breathchain.controller;

import com.breathchain.common.Result;
import com.breathchain.dto.LoginDTO;
import com.breathchain.dto.RegisterDTO;
import com.breathchain.security.SecurityUtils;
import com.breathchain.service.AuthService;
import com.breathchain.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(authService.login(dto));
    }

    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterDTO dto) {
        return Result.success(authService.register(dto));
    }

    @GetMapping("/me")
    public Result<Object> me() {
        return Result.success(SecurityUtils.current());
    }
}
