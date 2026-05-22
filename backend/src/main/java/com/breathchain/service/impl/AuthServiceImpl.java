package com.breathchain.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.breathchain.common.BusinessException;
import com.breathchain.common.ResultCode;
import com.breathchain.dto.LoginDTO;
import com.breathchain.dto.RegisterDTO;
import com.breathchain.entity.DoctorProfile;
import com.breathchain.entity.SysUser;
import com.breathchain.mapper.DoctorProfileMapper;
import com.breathchain.mapper.SysUserMapper;
import com.breathchain.security.JwtUtil;
import com.breathchain.service.AuthService;
import com.breathchain.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final DoctorProfileMapper doctorMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginVO login(LoginDTO dto) {
        SysUser user = userMapper.selectOne(
            Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, dto.getUsername())
        );
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.WRONG_PASSWORD);
        }
        return buildLoginVO(user);
    }

    @Override
    @Transactional
    public LoginVO register(RegisterDTO dto) {
        Long count = userMapper.selectCount(
            Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, dto.getUsername())
        );
        if (count != null && count > 0) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }
        String role = dto.getRole() == null ? "USER" : dto.getRole();

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setRole(role);
        user.setStatus(1);
        userMapper.insert(user);

        if ("DOCTOR".equals(role)) {
            if (dto.getLicenseNo() == null || dto.getLicenseNo().isBlank()) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "医生注册必须提供执业证书编号");
            }
            DoctorProfile profile = new DoctorProfile();
            profile.setUserId(user.getId());
            profile.setLicenseNo(dto.getLicenseNo());
            profile.setHospital(dto.getHospital());
            profile.setDepartment(dto.getDepartment());
            profile.setTitle(dto.getTitle());
            profile.setCertified(0);
            doctorMapper.insert(profile);
        }
        return buildLoginVO(user);
    }

    private LoginVO buildLoginVO(SysUser user) {
        String token = jwtUtil.generate(user.getId(), user.getUsername(), user.getRole());
        return LoginVO.builder()
            .token(token)
            .userId(user.getId())
            .username(user.getUsername())
            .realName(user.getRealName())
            .role(user.getRole())
            .walletAddress(user.getWalletAddress())
            .expireMillis(jwtUtil.getExpireMillis())
            .build();
    }
}
