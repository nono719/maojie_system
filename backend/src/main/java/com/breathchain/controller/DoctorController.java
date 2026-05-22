package com.breathchain.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.breathchain.common.Result;
import com.breathchain.entity.SysUser;
import com.breathchain.mapper.RewardRecordMapper;
import com.breathchain.mapper.SysUserMapper;
import com.breathchain.mapper.TrainingRecordMapper;
import com.breathchain.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/doctor")
@PreAuthorize("hasRole('DOCTOR')")
@RequiredArgsConstructor
public class DoctorController {

    private final SysUserMapper userMapper;
    private final TrainingRecordMapper recordMapper;
    private final RewardRecordMapper rewardMapper;

    @GetMapping("/patients")
    public Result<List<SysUser>> patients() {
        Long doctorId = SecurityUtils.currentUserId();
        return Result.success(userMapper.selectList(
            Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getDoctorId, doctorId)
                .eq(SysUser::getRole, "USER")
        ));
    }

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        Long doctorId = SecurityUtils.currentUserId();

        Long patientCount = userMapper.selectCount(
            Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getDoctorId, doctorId)
                .eq(SysUser::getRole, "USER")
        );

        // 今日训练次数 - 简化：所有训练记录数
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        Long todayTrainings = recordMapper.selectCount(
            Wrappers.<com.breathchain.entity.TrainingRecord>lambdaQuery()
                .ge(com.breathchain.entity.TrainingRecord::getCreateTime, todayStart)
        );

        Map<String, Object> data = new HashMap<>();
        data.put("patientCount", patientCount);
        data.put("todayTrainings", todayTrainings);
        data.put("activeTaskCount", 0); // TODO
        data.put("totalRewards", 0);    // TODO
        return Result.success(data);
    }
}
