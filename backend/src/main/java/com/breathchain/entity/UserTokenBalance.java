package com.breathchain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user_token_balance")
public class UserTokenBalance {

    @TableId(type = IdType.INPUT)
    private Long userId;

    private String walletAddress;
    private BigDecimal balance;
    private LocalDateTime lastSyncTime;
    private LocalDateTime updateTime;
}
