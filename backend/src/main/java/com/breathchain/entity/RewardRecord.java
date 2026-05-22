package com.breathchain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("reward_record")
public class RewardRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long taskId;
    private Long trainingRecordId;
    private BigDecimal amount;
    private String txHash;
    /** PENDING / SUCCESS / FAILED */
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
