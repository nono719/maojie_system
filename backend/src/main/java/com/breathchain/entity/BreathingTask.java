package com.breathchain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("breathing_task")
public class BreathingTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String taskName;
    private String description;
    private Integer inhaleSeconds;
    private Integer holdSeconds;
    private Integer exhaleSeconds;
    private Integer keepSeconds;
    /** 单次训练总时长(秒) */
    private Integer duration;
    private Integer dailyTimes;
    private BigDecimal rewardAmount;
    private Long doctorId;
    /** DRAFT / PUBLISHED / ARCHIVED */
    private String status;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
