package com.breathchain.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.breathchain.common.Result;
import com.breathchain.entity.RewardRecord;
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
@RequestMapping("/patient")
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class PatientController {

    private final SysUserMapper userMapper;
    private final TrainingRecordMapper recordMapper;
    private final RewardRecordMapper rewardMapper;

    @GetMapping("/profile")
    public Result<SysUser> profile() {
        return Result.success(userMapper.selectById(SecurityUtils.currentUserId()));
    }

    @GetMapping("/home")
    public Result<Map<String, Object>> home() {
        Long userId = SecurityUtils.currentUserId();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        Long todayCount = recordMapper.selectCount(
            Wrappers.<com.breathchain.entity.TrainingRecord>lambdaQuery()
                .eq(com.breathchain.entity.TrainingRecord::getUserId, userId)
                .ge(com.breathchain.entity.TrainingRecord::getCreateTime, todayStart)
        );

        Long totalCount = recordMapper.selectCount(
            Wrappers.<com.breathchain.entity.TrainingRecord>lambdaQuery()
                .eq(com.breathchain.entity.TrainingRecord::getUserId, userId)
        );

        Map<String, Object> data = new HashMap<>();
        data.put("todayTrainingCount", todayCount);
        data.put("totalTrainingCount", totalCount);
        // 其余字段可逐步补充：totalDuration、tokenBalance、streakDays
        return Result.success(data);
    }

    @GetMapping("/rewards")
    public Result<List<RewardRecord>> rewards() {
        return Result.success(rewardMapper.selectList(
            Wrappers.<RewardRecord>lambdaQuery()
                .eq(RewardRecord::getUserId, SecurityUtils.currentUserId())
                .orderByDesc(RewardRecord::getCreateTime)
        ));
    }
}
