package com.breathchain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("doctor_profile")
public class DoctorProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String licenseNo;
    private String hospital;
    private String department;
    private String title;
    private Integer certified;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
