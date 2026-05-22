package com.breathchain.security;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class AuthPrincipal implements Serializable {
    private Long userId;
    private String username;
    private String role;
}
