package com.breathchain.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.breathchain.common.Result;
import com.breathchain.dto.TaskCreateDTO;
import com.breathchain.entity.BreathingTask;
import com.breathchain.entity.TaskAssignment;
import com.breathchain.mapper.BreathingTaskMapper;
import com.breathchain.mapper.TaskAssignmentMapper;
import com.breathchain.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final BreathingTaskMapper taskMapper;
    private final TaskAssignmentMapper assignmentMapper;

    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @PostMapping
    public Result<BreathingTask> createTask(@Valid @RequestBody TaskCreateDTO dto) {
        BreathingTask task = new BreathingTask();
        org.springframework.beans.BeanUtils.copyProperties(dto, task);
        task.setDoctorId(SecurityUtils.currentUserId());
        task.setStatus("DRAFT");
        taskMapper.insert(task);
        return Result.success(task);
    }

    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @PutMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        BreathingTask task = taskMapper.selectById(id);
        if (task != null && task.getDoctorId().equals(SecurityUtils.currentUserId())) {
            task.setStatus("PUBLISHED");
            taskMapper.updateById(task);
        }
        return Result.success();
    }

    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @GetMapping("/mine")
    public Result<List<BreathingTask>> myTasks() {
        return Result.success(taskMapper.selectList(
            Wrappers.<BreathingTask>lambdaQuery()
                .eq(BreathingTask::getDoctorId, SecurityUtils.currentUserId())
                .orderByDesc(BreathingTask::getCreateTime)
        ));
    }

    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @PostMapping("/{taskId}/assign/{patientId}")
    public Result<TaskAssignment> assign(@PathVariable Long taskId, @PathVariable Long patientId) {
        TaskAssignment a = new TaskAssignment();
        a.setTaskId(taskId);
        a.setUserId(patientId);
        a.setDoctorId(SecurityUtils.currentUserId());
        a.setStatus("ACTIVE");
        assignmentMapper.insert(a);
        return Result.success(a);
    }

    @GetMapping("/assigned")
    public Result<List<BreathingTask>> assignedTasks() {
        Long userId = SecurityUtils.currentUserId();
        List<TaskAssignment> assigns = assignmentMapper.selectList(
            Wrappers.<TaskAssignment>lambdaQuery()
                .eq(TaskAssignment::getUserId, userId)
                .eq(TaskAssignment::getStatus, "ACTIVE")
        );
        if (assigns.isEmpty()) return Result.success(List.of());

        List<Long> taskIds = assigns.stream().map(TaskAssignment::getTaskId).toList();
        return Result.success(taskMapper.selectBatchIds(taskIds));
    }
}
