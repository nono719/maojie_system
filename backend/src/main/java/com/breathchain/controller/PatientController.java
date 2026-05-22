package com.breathchain.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.breathchain.common.Result;
import com.breathchain.entity.RewardRecord;
import com.breathchain.entity.SysUser;
import com.breathchain.entity.TrainingRecord;
import com.breathchain.mapper.RewardRecordMapper;
import com.breathchain.mapper.SysUserMapper;
import com.breathchain.mapper.TrainingRecordMapper;
import com.breathchain.security.SecurityUtils;
import com.breathchain.service.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/patient")
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class PatientController {

    private final SysUserMapper userMapper;
    private final TrainingRecordMapper recordMapper;
    private final RewardRecordMapper rewardMapper;
    private final TrainingService trainingService;

    @GetMapping("/profile")
    public Result<SysUser> profile() {
        return Result.success(userMapper.selectById(SecurityUtils.currentUserId()));
    }

    @GetMapping("/home")
    public Map<String, Object> home() {
        Long userId = SecurityUtils.currentUserId();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        Long todayCount = recordMapper.selectCount(
            Wrappers.<TrainingRecord>lambdaQuery()
                .eq(TrainingRecord::getUserId, userId)
                .ge(TrainingRecord::getCreateTime, todayStart)
        );

        Long totalCount = recordMapper.selectCount(
            Wrappers.<TrainingRecord>lambdaQuery()
                .eq(TrainingRecord::getUserId, userId)
        );

        BigDecimal totalReward = rewardMapper.selectList(
            Wrappers.<RewardRecord>lambdaQuery()
                .eq(RewardRecord::getUserId, userId)
                .eq(RewardRecord::getStatus, "SUCCESS")
        ).stream()
            .map(RewardRecord::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> data = new HashMap<>();
        data.put("todayTrainingCount", todayCount);
        data.put("totalTrainingCount", totalCount);
        data.put("tokenBalance", totalReward);
        data.put("streakDays", trainingService.streakDays(userId));
        return data;
    }

    @GetMapping("/trend")
    public Map<String, Object> trend(@RequestParam(defaultValue = "7") int days) {
        Long userId = SecurityUtils.currentUserId();
        return buildTrend(userId, days);
    }

    @GetMapping("/rewards")
    public Result<List<RewardRecord>> rewards() {
        return Result.success(rewardMapper.selectList(
            Wrappers.<RewardRecord>lambdaQuery()
                .eq(RewardRecord::getUserId, SecurityUtils.currentUserId())
                .orderByDesc(RewardRecord::getCreateTime)
        ));
    }

    // ------- helpers -------
    private Map<String, Object> buildTrend(Long userId, int days) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(days - 1);

        List<TrainingRecord> recent = recordMapper.selectList(
            Wrappers.<TrainingRecord>lambdaQuery()
                .eq(TrainingRecord::getUserId, userId)
                .ge(TrainingRecord::getCreateTime, start.atStartOfDay())
        );

        List<String> labels = new ArrayList<>();
        List<Integer> records = new ArrayList<>();
        List<Integer> completion = new ArrayList<>();

        for (int i = 0; i < days; i++) {
            LocalDate d = start.plusDays(i);
            labels.add(d.toString().substring(5)); // MM-dd
            int cnt = 0; int sumCompletion = 0;
            for (TrainingRecord r : recent) {
                if (r.getCreateTime() != null && r.getCreateTime().toLocalDate().equals(d)) {
                    cnt++;
                    if (r.getCompletionRate() != null) sumCompletion += r.getCompletionRate().intValue();
                }
            }
            records.add(cnt);
            completion.add(cnt > 0 ? sumCompletion / cnt : 0);
        }

        Map<String, Object> out = new HashMap<>();
        out.put("labels", labels);
        out.put("records", records);
        out.put("completion", completion);
        return out;
    }
}
