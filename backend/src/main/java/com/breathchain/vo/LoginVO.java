package com.breathchain.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginVO {
    private String token;
    private Long userId;
    private String username;
    private String realName;
    private String role;
    private String walletAddress;
    private long expireMillis;
    /** 医生角色专用：true=已认证 / false=待审核 / null=非医生 */
    private Boolean certified;
}
