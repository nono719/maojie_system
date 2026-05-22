package com.breathchain.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.breathchain.common.BusinessException;
import com.breathchain.common.ResultCode;
import com.breathchain.dto.TrainingCompleteDTO;
import com.breathchain.entity.BreathingTask;
import com.breathchain.entity.RewardRecord;
import com.breathchain.entity.SysUser;
import com.breathchain.entity.TrainingRecord;
import com.breathchain.mapper.BreathingTaskMapper;
import com.breathchain.mapper.RewardRecordMapper;
import com.breathchain.mapper.SysUserMapper;
import com.breathchain.mapper.TrainingRecordMapper;
import com.breathchain.service.BlockchainService;
import com.breathchain.service.RewardCalculator;
import com.breathchain.service.TrainingService;
import com.breathchain.vo.TrainingResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingServiceImpl implements TrainingService {

    private final TrainingRecordMapper recordMapper;
    private final BreathingTaskMapper taskMapper;
    private final RewardRecordMapper rewardMapper;
    private final SysUserMapper userMapper;
    private final BlockchainService blockchainService;
    private final RewardCalculator rewardCalculator;

    @Override
    @Transactional
    public TrainingResultVO completeTraining(Long userId, Long taskId, TrainingCompleteDTO dto) {
        BreathingTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }

        // 1. 落库
        TrainingRecord record = new TrainingRecord();
        record.setUserId(userId);
        record.setTaskId(taskId);
        record.setDuration(dto.getDuration());
        record.setBreathCount(dto.getBreathCount());
        record.setCompletionRate(dto.getCompletionRate());
        record.setScore(dto.getScore());
        record.setHeartRate(dto.getHeartRate());
        record.setChainStatus("PENDING");
        recordMapper.insert(record);

        // 2. 计算哈希
        String dataHash = blockchainService.computeHash(record);
        record.setDataHash(dataHash);
        recordMapper.updateById(record);

        // 3. 上链
        TrainingResultVO.TrainingResultVOBuilder result = TrainingResultVO.builder()
            .recordId(record.getId())
            .dataHash(dataHash);

        try {
            String txHash = blockchainService.submitTrainingRecord(record, dataHash);
            record.setBlockTxId(txHash);
            record.setChainStatus("SUCCESS");
            recordMapper.updateById(record);
            result.blockTxId(txHash).chainStatus("SUCCESS");
        } catch (Exception ex) {
            log.error("上链失败 recordId={}", record.getId(), ex);
            record.setChainStatus("FAILED");
            record.setChainError(ex.getMessage());
            recordMapper.updateById(record);
            result.chainStatus("FAILED");
        }

        // 4. 发奖（按阶梯/连续打卡/评分加成）
        issueReward(userId, task, record, result);
        return result.build();
    }

    /**
     * 计算用户的连续打卡天数（含今天）
     */
    @Override
    public int streakDays(Long userId) {
        LocalDateTime past30Start = LocalDate.now().minusDays(30).atStartOfDay();
        List<TrainingRecord> recent = recordMapper.selectList(
            Wrappers.<TrainingRecord>lambdaQuery()
                .eq(TrainingRecord::getUserId, userId)
                .ge(TrainingRecord::getCreateTime, past30Start)
        );
        if (recent.isEmpty()) return 0;
        Set<LocalDate> days = new HashSet<>();
        for (TrainingRecord r : recent) {
            if (r.getCreateTime() != null) days.add(r.getCreateTime().toLocalDate());
        }
        int streak = 0;
        LocalDate d = LocalDate.now();
        while (days.contains(d)) { streak++; d = d.minusDays(1); }
        return streak;
    }

    private void issueReward(Long userId, BreathingTask task, TrainingRecord record,
                             TrainingResultVO.TrainingResultVOBuilder builder) {
        int streak = streakDays(userId);
        RewardCalculator.RewardDetail detail = rewardCalculator.calc(task, record, streak);
        builder.rewardBase(detail.getBase())
               .rewardReasons(detail.getReasons())
               .streakDays(streak);

        if (detail.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            builder.rewardAmount(BigDecimal.ZERO).rewardStatus("SKIPPED");
            return;
        }

        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getWalletAddress() == null || user.getWalletAddress().isBlank()) {
            log.warn("user {} has no wallet, skip reward", userId);
            builder.rewardAmount(detail.getAmount()).rewardStatus("SKIPPED");
            return;
        }

        RewardRecord reward = new RewardRecord();
        reward.setUserId(userId);
        reward.setTaskId(task.getId());
        reward.setTrainingRecordId(record.getId());
        reward.setAmount(detail.getAmount());
        reward.setStatus("PENDING");
        rewardMapper.insert(reward);

        try {
            String txHash = blockchainService.awardUser(
                user.getWalletAddress(), task.getId(), detail.getAmount()
            );
            reward.setTxHash(txHash);
            reward.setStatus("SUCCESS");
            rewardMapper.updateById(reward);
            builder.rewardAmount(detail.getAmount()).rewardTxHash(txHash).rewardStatus("SUCCESS");
        } catch (Exception ex) {
            log.error("发奖失败 userId={} taskId={}", userId, task.getId(), ex);
            reward.setStatus("FAILED");
            rewardMapper.updateById(reward);
            builder.rewardAmount(detail.getAmount()).rewardStatus("FAILED");
        }
    }

    @Override
    public List<TrainingRecord> myHistory(Long userId) {
        return recordMapper.selectList(
            Wrappers.<TrainingRecord>lambdaQuery()
                .eq(TrainingRecord::getUserId, userId)
                .orderByDesc(TrainingRecord::getCreateTime)
        );
    }

    @Override
    public List<TrainingRecord> patientHistory(Long doctorId, Long patientId) {
        SysUser patient = userMapper.selectById(patientId);
        if (patient == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (patient.getDoctorId() == null || !patient.getDoctorId().equals(doctorId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return myHistory(patientId);
    }
}
