package com.breathchain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("task_assignment")
public class TaskAssignment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;
    private Long userId;
    private Long doctorId;
    private LocalDate startDate;
    private LocalDate endDate;
    /** ACTIVE / PAUSED / FINISHED */
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
