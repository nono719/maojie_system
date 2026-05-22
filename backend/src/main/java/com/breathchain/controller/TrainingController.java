package com.breathchain.controller;

import com.breathchain.common.Result;
import com.breathchain.dto.TrainingCompleteDTO;
import com.breathchain.entity.TrainingRecord;
import com.breathchain.security.SecurityUtils;
import com.breathchain.service.TrainingService;
import com.breathchain.vo.TrainingResultVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/training")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{taskId}/complete")
    public Result<TrainingResultVO> complete(@PathVariable Long taskId,
                                             @Valid @RequestBody TrainingCompleteDTO dto) {
        return Result.success(
            trainingService.completeTraining(SecurityUtils.currentUserId(), taskId, dto)
        );
    }

    @GetMapping("/history")
    public Result<List<TrainingRecord>> myHistory() {
        return Result.success(trainingService.myHistory(SecurityUtils.currentUserId()));
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/history/{patientId}")
    public Result<List<TrainingRecord>> patientHistory(@PathVariable Long patientId) {
        return Result.success(
            trainingService.patientHistory(SecurityUtils.currentUserId(), patientId)
        );
    }
}
