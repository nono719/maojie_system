package com.breathchain.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.breathchain.common.BusinessException;
import com.breathchain.common.Result;
import com.breathchain.common.ResultCode;
import com.breathchain.entity.PatientAssignmentRequest;
import com.breathchain.entity.SysUser;
import com.breathchain.mapper.PatientAssignmentRequestMapper;
import com.breathchain.mapper.SysUserMapper;
import com.breathchain.security.SecurityUtils;
import com.breathchain.service.DoctorAuthGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class AssignmentRequestController {

    private final PatientAssignmentRequestMapper requestMapper;
    private final SysUserMapper userMapper;
    private final DoctorAuthGuard doctorGuard;

    // ============== 医生端 ==============

    /** 医生发起：申请绑定某个患者（按 username 或 user_id） */
    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/doctor/assignment-requests")
    public Result<PatientAssignmentRequest> createRequest(@RequestBody Map<String, Object> body) {
        doctorGuard.requireCertified();
        Long doctorId = SecurityUtils.currentUserId();

        SysUser patient = null;
        Object patientIdRaw = body.get("patientId");
        Object usernameRaw = body.get("username");
        if (patientIdRaw != null) {
            patient = userMapper.selectById(Long.valueOf(patientIdRaw.toString()));
        } else if (usernameRaw != null) {
            patient = userMapper.selectOne(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, usernameRaw.toString())
            );
        }
        if (patient == null || !"USER".equals(patient.getRole())) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND.getCode(), "找不到目标患者");
        }
        if (patient.getDoctorId() != null && patient.getDoctorId().equals(doctorId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "该患者已经是您的患者");
        }
        // 防重复申请
        Long dup = requestMapper.selectCount(
            Wrappers.<PatientAssignmentRequest>lambdaQuery()
                .eq(PatientAssignmentRequest::getDoctorId, doctorId)
                .eq(PatientAssignmentRequest::getPatientId, patient.getId())
                .eq(PatientAssignmentRequest::getStatus, "PENDING")
        );
        if (dup != null && dup > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "已有针对该患者的待处理申请");
        }

        PatientAssignmentRequest req = new PatientAssignmentRequest();
        req.setDoctorId(doctorId);
        req.setPatientId(patient.getId());
        req.setReason(body.getOrDefault("reason", "").toString());
        req.setStatus("PENDING");
        requestMapper.insert(req);
        return Result.success(req);
    }

    /** 医生：查看我提交的申请历史 */
    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/doctor/assignment-requests")
    public List<Map<String, Object>> myRequests() {
        Long doctorId = SecurityUtils.currentUserId();
        return enrich(requestMapper.selectList(
            Wrappers.<PatientAssignmentRequest>lambdaQuery()
                .eq(PatientAssignmentRequest::getDoctorId, doctorId)
                .orderByDesc(PatientAssignmentRequest::getCreateTime)
        ));
    }

    // ============== 管理员端 ==============

    /** 管理员：列出全部申请（可按 status 过滤） */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/assignment-requests")
    public List<Map<String, Object>> listAll(@RequestParam(required = false) String status) {
        var q = Wrappers.<PatientAssignmentRequest>lambdaQuery();
        if (status != null && !status.isBlank()) q.eq(PatientAssignmentRequest::getStatus, status);
        q.orderByDesc(PatientAssignmentRequest::getCreateTime);
        return enrich(requestMapper.selectList(q));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/assignment-requests/{id}/approve")
    @Transactional
    public Result<Void> approve(@PathVariable Long id) {
        PatientAssignmentRequest req = requestMapper.selectById(id);
        if (req == null) throw new BusinessException(ResultCode.NOT_FOUND);
        if (!"PENDING".equals(req.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "申请已处理");
        }
        // 1. 更新患者绑定的医生
        SysUser patient = userMapper.selectById(req.getPatientId());
        if (patient == null) throw new BusinessException(ResultCode.USER_NOT_FOUND);
        patient.setDoctorId(req.getDoctorId());
        userMapper.updateById(patient);
        // 2. 更新申请状态
        req.setStatus("APPROVED");
        req.setProcessedBy(SecurityUtils.currentUserId());
        req.setProcessedTime(LocalDateTime.now());
        requestMapper.updateById(req);
        return Result.success();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/assignment-requests/{id}/reject")
    public Result<Void> reject(@PathVariable Long id) {
        PatientAssignmentRequest req = requestMapper.selectById(id);
        if (req == null) throw new BusinessException(ResultCode.NOT_FOUND);
        if (!"PENDING".equals(req.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "申请已处理");
        }
        req.setStatus("REJECTED");
        req.setProcessedBy(SecurityUtils.currentUserId());
        req.setProcessedTime(LocalDateTime.now());
        requestMapper.updateById(req);
        return Result.success();
    }

    // 内部：附加 doctor / patient 信息
    private List<Map<String, Object>> enrich(List<PatientAssignmentRequest> list) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (PatientAssignmentRequest r : list) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", r.getId());
            row.put("doctorId", r.getDoctorId());
            row.put("patientId", r.getPatientId());
            row.put("reason", r.getReason());
            row.put("status", r.getStatus());
            row.put("createTime", r.getCreateTime());
            row.put("processedTime", r.getProcessedTime());
            SysUser doctor = userMapper.selectById(r.getDoctorId());
            SysUser patient = userMapper.selectById(r.getPatientId());
            row.put("doctorName", doctor != null ? (doctor.getRealName() != null ? doctor.getRealName() : doctor.getUsername()) : null);
            row.put("doctorUsername", doctor != null ? doctor.getUsername() : null);
            row.put("patientName", patient != null ? (patient.getRealName() != null ? patient.getRealName() : patient.getUsername()) : null);
            row.put("patientUsername", patient != null ? patient.getUsername() : null);
            row.put("patientCurrentDoctorId", patient != null ? patient.getDoctorId() : null);
            out.add(row);
        }
        return out;
    }
}
