package com.breathchain.service;

import com.breathchain.entity.BreathingTask;
import com.breathchain.entity.TrainingRecord;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 训练奖励计算器（论文 4.4.1 "奖励规则" 完善版）
 *
 * 规则（演示/答辩友好阈值，门槛低更容易观察发奖效果）：
 *   1) 完成率 < 5%         → 不发奖（防刷）
 *   2) 完成率 5-29%        → base × 0.2  "初步尝试"
 *   3) 完成率 30-59%       → base × 0.5  "已有进展"
 *   4) 完成率 60-79%       → base × 0.7  "良好"
 *   5) 完成率 80-89%       → base × 0.9  "优秀"
 *   6) 完成率 ≥ 90%        → base × 1.0  "完美"
 *   7) 评分 ≥ 90           → 在上一步基础上 × 1.2 "高质量训练加成"
 *   8) 连续打卡 ≥ 3 天      → × 1.3  "毅力之星"
 *   9) 连续打卡 ≥ 7 天      → × 1.5  "钢铁意志"（覆盖 8）
 *
 * 所有加成可叠加，但最高不超过 base × 2。
 */
@Component
public class RewardCalculator {

    public RewardDetail calc(BreathingTask task, TrainingRecord record, int streakDays) {
        BigDecimal base = task.getRewardAmount();
        BigDecimal completion = record.getCompletionRate();
        Integer score = record.getScore();

        RewardDetail.RewardDetailBuilder b = RewardDetail.builder()
            .base(base)
            .completionRate(completion)
            .score(score)
            .streakDays(streakDays);

        // 1) 完成率阶梯（演示阈值 5%）
        BigDecimal completionFactor;
        String completionTier;
        if (completion.compareTo(BigDecimal.valueOf(5)) < 0) {
            return b.amount(BigDecimal.ZERO).reasons(List.of("完成率不足 5%，本次不发放奖励")).build();
        } else if (completion.compareTo(BigDecimal.valueOf(30)) < 0) {
            completionFactor = new BigDecimal("0.2"); completionTier = "初步尝试 (5-29%)";
        } else if (completion.compareTo(BigDecimal.valueOf(60)) < 0) {
            completionFactor = new BigDecimal("0.5"); completionTier = "已有进展 (30-59%)";
        } else if (completion.compareTo(BigDecimal.valueOf(80)) < 0) {
            completionFactor = new BigDecimal("0.7"); completionTier = "良好 (60-79%)";
        } else if (completion.compareTo(BigDecimal.valueOf(90)) < 0) {
            completionFactor = new BigDecimal("0.9"); completionTier = "优秀 (80-89%)";
        } else {
            completionFactor = BigDecimal.ONE; completionTier = "完美 (≥90%)";
        }

        BigDecimal amount = base.multiply(completionFactor);

        java.util.List<String> reasons = new java.util.ArrayList<>();
        reasons.add("完成率 " + completion + "% (" + completionTier + ") × " + completionFactor);

        // 2) 评分加成
        if (score != null && score >= 90) {
            amount = amount.multiply(new BigDecimal("1.2"));
            reasons.add("评分 ≥ 90 高质量加成 × 1.2");
        }

        // 3) 连续打卡加成
        if (streakDays >= 7) {
            amount = amount.multiply(new BigDecimal("1.5"));
            reasons.add("连续打卡 " + streakDays + " 天「钢铁意志」× 1.5");
        } else if (streakDays >= 3) {
            amount = amount.multiply(new BigDecimal("1.3"));
            reasons.add("连续打卡 " + streakDays + " 天「毅力之星」× 1.3");
        }

        // 4) 上限保护：base × 2
        BigDecimal max = base.multiply(BigDecimal.valueOf(2));
        if (amount.compareTo(max) > 0) {
            amount = max;
            reasons.add("达到单次最高奖励上限 (base × 2)");
        }

        amount = amount.setScale(2, RoundingMode.HALF_UP);
        return b.amount(amount).reasons(reasons).build();
    }

    @Data @Builder
    public static class RewardDetail {
        private BigDecimal base;
        private BigDecimal completionRate;
        private Integer score;
        private int streakDays;
        private BigDecimal amount;
        private List<String> reasons;
    }
}
