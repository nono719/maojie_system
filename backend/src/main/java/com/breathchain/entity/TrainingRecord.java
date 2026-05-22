package com.breathchain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("training_record")
public class TrainingRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long taskId;
    private Integer duration;
    private Integer breathCount;
    private BigDecimal completionRate;
    private Integer score;
    private Integer heartRate;
    private String dataHash;
    private String blockTxId;
    /** PENDING / SUCCESS / FAILED */
    private String chainStatus;
    private String chainError;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
