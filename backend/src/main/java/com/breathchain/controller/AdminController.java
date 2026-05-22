package com.breathchain.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.breathchain.common.BusinessException;
import com.breathchain.common.Result;
import com.breathchain.common.ResultCode;
import com.breathchain.config.Web3jConfig;
import com.breathchain.entity.BreathingTask;
import com.breathchain.entity.DoctorProfile;
import com.breathchain.entity.RewardRecord;
import com.breathchain.entity.SysUser;
import com.breathchain.entity.TrainingRecord;
import com.breathchain.mapper.BreathingTaskMapper;
import com.breathchain.mapper.DoctorProfileMapper;
import com.breathchain.mapper.RewardRecordMapper;
import com.breathchain.mapper.SysUserMapper;
import com.breathchain.mapper.TrainingRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final SysUserMapper userMapper;
    private final DoctorProfileMapper doctorMapper;
    private final BreathingTaskMapper taskMapper;
    private final TrainingRecordMapper recordMapper;
    private final RewardRecordMapper rewardMapper;
    private final Web3j web3j;
    private final Credentials serviceCredentials;
    private final Web3jConfig web3Config;

    // ---------- 数据概览 ----------
    @GetMapping("/overview")
    public Map<String, Object> overview() {
        Map<String, Object> data = new HashMap<>();
        data.put("userCount", userMapper.selectCount(null));
        data.put("doctorCount", userMapper.selectCount(
            Wrappers.<SysUser>lambdaQuery().eq(SysUser::getRole, "DOCTOR")));
        data.put("patientCount", userMapper.selectCount(
            Wrappers.<SysUser>lambdaQuery().eq(SysUser::getRole, "USER")));
        data.put("taskCount", taskMapper.selectCount(null));
        data.put("recordCount", recordMapper.selectCount(null));

        BigDecimal totalRewards = rewardMapper.selectList(
            Wrappers.<RewardRecord>lambdaQuery().eq(RewardRecord::getStatus, "SUCCESS")
        ).stream()
            .map(RewardRecord::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        data.put("totalRewards", totalRewards);

        long chainBlock = 0;
        try {
            chainBlock = web3j.ethBlockNumber().send().getBlockNumber().longValue();
        } catch (Exception ex) {
            log.warn("query chainBlock failed: {}", ex.getMessage());
        }
        data.put("chainBlock", chainBlock);
        return data;
    }

    // ---------- 用户管理 ----------
    @GetMapping("/users")
    public List<SysUser> listUsers() {
        return userMapper.selectList(
            Wrappers.<SysUser>lambdaQuery().orderByAsc(SysUser::getId)
        );
    }

    @PutMapping("/users/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        SysUser user = userMapper.selectById(id);
        if (user == null) throw new BusinessException(ResultCode.USER_NOT_FOUND);
        if ("ADMIN".equals(user.getRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "不能停用管理员账户");
        }
        user.setStatus(status);
        userMapper.updateById(user);
        return Result.success();
    }

    // ---------- 医生审核 ----------
    @GetMapping("/doctors")
    public List<Map<String, Object>> listDoctors() {
        List<DoctorProfile> profiles = doctorMapper.selectList(null);
        return profiles.stream().map(p -> {
            SysUser u = userMapper.selectById(p.getUserId());
            Map<String, Object> row = new HashMap<>();
            row.put("id", p.getId());
            row.put("userId", p.getUserId());
            row.put("username", u != null ? u.getUsername() : null);
            row.put("realName", u != null ? u.getRealName() : null);
            row.put("licenseNo", p.getLicenseNo());
            row.put("hospital", p.getHospital());
            row.put("department", p.getDepartment());
            row.put("title", p.getTitle());
            row.put("certified", p.getCertified());
            return row;
        }).toList();
    }

    @PutMapping("/doctors/{id}/certify")
    public Result<Void> certify(@PathVariable Long id) {
        DoctorProfile p = doctorMapper.selectById(id);
        if (p == null) throw new BusinessException(ResultCode.NOT_FOUND);
        p.setCertified(1);
        doctorMapper.updateById(p);
        return Result.success();
    }

    // ---------- 训练记录审计 ----------
    @GetMapping("/training-records")
    public List<TrainingRecord> listRecords() {
        return recordMapper.selectList(
            Wrappers.<TrainingRecord>lambdaQuery().orderByDesc(TrainingRecord::getCreateTime)
        );
    }

    // ---------- 区块链状态 ----------
    @GetMapping("/chain-info")
    public Map<String, Object> chainInfo() {
        Map<String, Object> data = new HashMap<>();
        data.put("chainId", web3Config.getChainId());
        data.put("breathTokenAddress", web3Config.getBreathTokenAddress());
        data.put("trainingRecordAddress", web3Config.getTrainingRecordAddress());
        data.put("serviceAccount", serviceCredentials.getAddress());
        try {
            data.put("blockNumber", web3j.ethBlockNumber().send().getBlockNumber().longValue());
        } catch (Exception ex) {
            data.put("blockNumber", -1L);
            data.put("error", ex.getMessage());
        }
        return data;
    }
}
