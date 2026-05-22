package com.breathchain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TaskCreateDTO {

    @NotBlank @Size(max = 100)
    private String taskName;

    @Size(max = 500)
    private String description;

    @NotNull @Min(1)  private Integer inhaleSeconds;
    @NotNull @Min(0)  private Integer holdSeconds;
    @NotNull @Min(1)  private Integer exhaleSeconds;
    @NotNull @Min(0)  private Integer keepSeconds;
    @NotNull @Min(30) private Integer duration;
    @NotNull @Min(1)  private Integer dailyTimes;

    @NotNull @DecimalMin("0.0")
    private BigDecimal rewardAmount;
}
