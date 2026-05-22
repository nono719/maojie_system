package com.breathchain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterDTO {
    @NotBlank @Size(min = 3, max = 50, message = "用户名长度 3-50")
    private String username;

    @NotBlank @Size(min = 6, max = 50, message = "密码长度 6-50")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    private String phone;

    /** USER / DOCTOR */
    @Pattern(regexp = "^(USER|DOCTOR)$", message = "role 仅允许 USER/DOCTOR")
    private String role;

    /** 仅 DOCTOR 注册时必填 */
    private String licenseNo;
    private String hospital;
    private String department;
    private String title;
}
