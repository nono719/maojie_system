package com.breathchain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("patient_assignment_request")
public class PatientAssignmentRequest {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long doctorId;
    private Long patientId;
    private String reason;
    /** PENDING / APPROVED / REJECTED */
    private String status;
    private Long processedBy;
    private LocalDateTime processedTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
