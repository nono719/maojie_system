package com.breathchain.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.breathchain.common.Result;
import com.breathchain.entity.BreathingTask;
import com.breathchain.entity.RewardRecord;
import com.breathchain.entity.SysUser;
import com.breathchain.entity.TrainingRecord;
import com.breathchain.mapper.BreathingTaskMapper;
import com.breathchain.mapper.RewardRecordMapper;
import com.breathchain.mapper.SysUserMapper;
import com.breathchain.mapper.TrainingRecordMapper;
import com.breathchain.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/doctor")
@PreAuthorize("hasRole('DOCTOR')")
@RequiredArgsConstructor
public class DoctorController {

    private final SysUserMapper userMapper;
    private final TrainingRecordMapper recordMapper;
    private final RewardRecordMapper rewardMapper;
    private final BreathingTaskMapper taskMapper;

    @GetMapping("/patients")
    public Result<List<SysUser>> patients() {
        Long doctorId = SecurityUtils.currentUserId();
        return Result.success(userMapper.selectList(
            Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getDoctorId, doctorId)
                .eq(SysUser::getRole, "USER")
        ));
    }

    @GetMapping("/patients/{id}/detail")
    public Map<String, Object> patientDetail(@PathVariable Long id) {
        Long doctorId = SecurityUtils.currentUserId();
        SysUser patient = userMapper.selectById(id);
        if (patient == null || !"USER".equals(patient.getRole())) {
            throw new com.breathchain.common.BusinessException(
                com.breathchain.common.ResultCode.USER_NOT_FOUND);
        }
        if (patient.getDoctorId() == null || !patient.getDoctorId().equals(doctorId)) {
            throw new com.breathchain.common.BusinessException(
                com.breathchain.common.ResultCode.FORBIDDEN);
        }

        // 训练统计
        List<TrainingRecord> records = recordMapper.selectList(
            Wrappers.<TrainingRecord>lambdaQuery()
                .eq(TrainingRecord::getUserId, id)
                .orderByDesc(TrainingRecord::getCreateTime)
        );
        long total = records.size();
        BigDecimal avgCompletion = BigDecimal.ZERO;
        if (!records.isEmpty()) {
            BigDecimal sum = BigDecimal.ZERO;
            int n = 0;
            for (TrainingRecord r : records) {
                if (r.getCompletionRate() != null) { sum = sum.add(r.getCompletionRate()); n++; }
            }
            if (n > 0) avgCompletion = sum.divide(BigDecimal.valueOf(n), 1, java.math.RoundingMode.HALF_UP);
        }
        LocalDateTime lastActive = records.isEmpty() ? null : records.get(0).getCreateTime();

        // 奖励
        BigDecimal totalReward = rewardMapper.selectList(
            Wrappers.<RewardRecord>lambdaQuery()
                .eq(RewardRecord::getUserId, id)
                .eq(RewardRecord::getStatus, "SUCCESS")
        ).stream().map(RewardRecord::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> data = new HashMap<>();
        data.put("user", patient);
        data.put("totalTrainings", total);
        data.put("avgCompletion", avgCompletion);
        data.put("lastActive", lastActive);
        data.put("totalReward", totalReward);
        data.put("recentRecords", records.stream().limit(10).toList());
        return data;
    }

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        Long doctorId = SecurityUtils.currentUserId();

        List<Long> myPatientIds = userMapper.selectList(
            Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getDoctorId, doctorId)
                .eq(SysUser::getRole, "USER")
        ).stream().map(SysUser::getId).toList();

        long todayTrainings = 0;
        BigDecimal totalRewards = BigDecimal.ZERO;
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        if (!myPatientIds.isEmpty()) {
            todayTrainings = recordMapper.selectCount(
                Wrappers.<TrainingRecord>lambdaQuery()
                    .in(TrainingRecord::getUserId, myPatientIds)
                    .ge(TrainingRecord::getCreateTime, todayStart)
            );
            totalRewards = rewardMapper.selectList(
                Wrappers.<RewardRecord>lambdaQuery()
                    .in(RewardRecord::getUserId, myPatientIds)
                    .eq(RewardRecord::getStatus, "SUCCESS")
            ).stream()
                .map(RewardRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        Long activeTaskCount = taskMapper.selectCount(
            Wrappers.<BreathingTask>lambdaQuery()
                .eq(BreathingTask::getDoctorId, doctorId)
                .eq(BreathingTask::getStatus, "PUBLISHED")
        );

        Map<String, Object> data = new HashMap<>();
        data.put("patientCount", (long) myPatientIds.size());
        data.put("todayTrainings", todayTrainings);
        data.put("activeTaskCount", activeTaskCount);
        data.put("totalRewards", totalRewards);
        return Result.success(data);
    }

    @GetMapping("/trend")
    public Map<String, Object> trend(@RequestParam(defaultValue = "7") int days) {
        Long doctorId = SecurityUtils.currentUserId();
        List<Long> myPatientIds = userMapper.selectList(
            Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getDoctorId, doctorId)
                .eq(SysUser::getRole, "USER")
        ).stream().map(SysUser::getId).toList();

        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(days - 1);

        List<TrainingRecord> recent = myPatientIds.isEmpty() ? List.of() : recordMapper.selectList(
            Wrappers.<TrainingRecord>lambdaQuery()
                .in(TrainingRecord::getUserId, myPatientIds)
                .ge(TrainingRecord::getCreateTime, start.atStartOfDay())
        );

        List<String> labels = new ArrayList<>();
        List<Integer> records = new ArrayList<>();
        List<Integer> completion = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate d = start.plusDays(i);
            labels.add(d.toString().substring(5));
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
