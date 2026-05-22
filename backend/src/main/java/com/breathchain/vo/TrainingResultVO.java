package com.breathchain.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class TrainingResultVO {
    private Long recordId;
    private String dataHash;
    private String blockTxId;
    private String chainStatus;

    private BigDecimal rewardAmount;
    private String rewardTxHash;
    private String rewardStatus;

    // 奖励计算明细（论文 4.4.1 完善奖励规则）
    private BigDecimal rewardBase;
    private List<String> rewardReasons;
    private Integer streakDays;
}
