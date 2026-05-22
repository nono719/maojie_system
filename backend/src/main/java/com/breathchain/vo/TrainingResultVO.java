package com.breathchain.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

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
}
