package com.breathchain.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TrainingCompleteDTO {

    @NotNull @Min(0)
    private Integer duration;        // 实际时长（秒）

    @Min(0)
    private Integer breathCount;     // 呼吸次数

    @NotNull @DecimalMin("0.00") @DecimalMax("100.00")
    private BigDecimal completionRate;  // 完成率 %

    @NotNull @Min(0) @Max(100)
    private Integer score;            // 评分

    private Integer heartRate;        // 平均心率（可选）
}
