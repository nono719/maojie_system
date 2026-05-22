package com.breathchain.security;

import com.breathchain.common.BusinessException;
import com.breathchain.common.ResultCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static AuthPrincipal current() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthPrincipal p)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return p;
    }

    public static Long currentUserId() {
        return current().getUserId();
    }

    public static String currentRole() {
        return current().getRole();
    }
}
