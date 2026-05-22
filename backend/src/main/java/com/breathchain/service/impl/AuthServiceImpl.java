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
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.WRONG_PASSWORD);
        }
        // 医生：未审核 → 友好提示「待审核」而不是泛泛的「已停用」
        if ("DOCTOR".equals(user.getRole())) {
            DoctorProfile profile = doctorMapper.selectOne(
                Wrappers.<DoctorProfile>lambdaQuery().eq(DoctorProfile::getUserId, user.getId())
            );
            if (profile != null && profile.getCertified() != null && profile.getCertified() == 0) {
                throw new BusinessException(ResultCode.DOCTOR_PENDING_REVIEW);
            }
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
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
        boolean isDoctor = "DOCTOR".equals(role);

        // 校验绑定医生（患者注册时可选）
        if ("USER".equals(role) && dto.getDoctorId() != null) {
            SysUser bound = userMapper.selectById(dto.getDoctorId());
            if (bound == null || !"DOCTOR".equals(bound.getRole())) {
                throw new BusinessException(ResultCode.DOCTOR_NOT_FOUND);
            }
        }

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setRole(role);
        // 医生注册需审核：默认停用直到管理员通过认证
        user.setStatus(isDoctor ? 0 : 1);
        if ("USER".equals(role) && dto.getDoctorId() != null) {
            user.setDoctorId(dto.getDoctorId());
        }
        userMapper.insert(user);

        if (isDoctor) {
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
            // 不返回 token — 等待审核
            return LoginVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .role(user.getRole())
                .expireMillis(0)
                .build();
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
