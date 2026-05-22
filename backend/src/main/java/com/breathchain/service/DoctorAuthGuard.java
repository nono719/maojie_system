package com.breathchain.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.breathchain.common.BusinessException;
import com.breathchain.common.ResultCode;
import com.breathchain.entity.DoctorProfile;
import com.breathchain.mapper.DoctorProfileMapper;
import com.breathchain.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 医生端写操作前置校验：要求当前登录医生已通过认证。
 * 未通过 → 抛 DOCTOR_PENDING_REVIEW，前端拿到提示。
 */
@Component
@RequiredArgsConstructor
public class DoctorAuthGuard {

    private final DoctorProfileMapper doctorMapper;

    public void requireCertified() {
        Long uid = SecurityUtils.currentUserId();
        DoctorProfile profile = doctorMapper.selectOne(
            Wrappers.<DoctorProfile>lambdaQuery().eq(DoctorProfile::getUserId, uid)
        );
        if (profile == null || profile.getCertified() == null || profile.getCertified() != 1) {
            throw new BusinessException(ResultCode.DOCTOR_PENDING_REVIEW);
        }
    }

    public boolean isCertified() {
        Long uid = SecurityUtils.currentUserId();
        DoctorProfile profile = doctorMapper.selectOne(
            Wrappers.<DoctorProfile>lambdaQuery().eq(DoctorProfile::getUserId, uid)
        );
        return profile != null && profile.getCertified() != null && profile.getCertified() == 1;
    }
}
