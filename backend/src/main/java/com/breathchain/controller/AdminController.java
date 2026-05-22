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

    // ---------- 最近 N 天趋势 ----------
    @GetMapping("/trend")
    public Map<String, Object> trend(@RequestParam(defaultValue = "7") int days) {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate start = today.minusDays(days - 1);

        List<TrainingRecord> recent = recordMapper.selectList(
            Wrappers.<TrainingRecord>lambdaQuery()
                .ge(TrainingRecord::getCreateTime, start.atStartOfDay())
        );
        List<RewardRecord> recentRewards = rewardMapper.selectList(
            Wrappers.<RewardRecord>lambdaQuery()
                .ge(RewardRecord::getCreateTime, start.atStartOfDay())
                .eq(RewardRecord::getStatus, "SUCCESS")
        );

        java.util.List<String> labels = new java.util.ArrayList<>();
        java.util.List<Integer> records = new java.util.ArrayList<>();
        java.util.List<BigDecimal> rewards = new java.util.ArrayList<>();
        for (int i = 0; i < days; i++) {
            java.time.LocalDate d = start.plusDays(i);
            labels.add(d.toString().substring(5));

            int cnt = 0;
            for (TrainingRecord r : recent) {
                if (r.getCreateTime() != null && r.getCreateTime().toLocalDate().equals(d)) cnt++;
            }
            records.add(cnt);

            BigDecimal sum = BigDecimal.ZERO;
            for (RewardRecord r : recentRewards) {
                if (r.getCreateTime() != null && r.getCreateTime().toLocalDate().equals(d)) {
                    sum = sum.add(r.getAmount());
                }
            }
            rewards.add(sum);
        }
        Map<String, Object> out = new HashMap<>();
        out.put("labels", labels);
        out.put("records", records);
        out.put("rewards", rewards);
        return out;
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
        // 同步启用对应账户
        SysUser u = userMapper.selectById(p.getUserId());
        if (u != null) {
            u.setStatus(1);
            userMapper.updateById(u);
        }
        return Result.success();
    }

    @PutMapping("/doctors/{id}/reject")
    public Result<Void> rejectDoctor(@PathVariable Long id) {
        DoctorProfile p = doctorMapper.selectById(id);
        if (p == null) throw new BusinessException(ResultCode.NOT_FOUND);
        p.setCertified(-1);
        doctorMapper.updateById(p);
        SysUser u = userMapper.selectById(p.getUserId());
        if (u != null) {
            u.setStatus(0);
            userMapper.updateById(u);
        }
        return Result.success();
    }

    // ---------- 修改用户信息 ----------
    @PutMapping("/users/{id}")
    public Result<SysUser> updateUser(@PathVariable Long id, @RequestBody SysUser body) {
        SysUser exist = userMapper.selectById(id);
        if (exist == null) throw new BusinessException(ResultCode.USER_NOT_FOUND);
        // 限定可改字段（防越权改密码 / id / role 串改）
        if (body.getRealName() != null) exist.setRealName(body.getRealName());
        if (body.getPhone() != null)    exist.setPhone(body.getPhone());
        if (body.getEmail() != null)    exist.setEmail(body.getEmail());
        if (body.getWalletAddress() != null) exist.setWalletAddress(body.getWalletAddress());
        // role 只能 ADMIN ↔ 自身角色之间的限定调整，且不能把自己降级（这里简化：role 可改但不能改自己）
        if (body.getRole() != null && !exist.getRole().equals("ADMIN")) {
            if (!"ADMIN".equals(body.getRole())) {  // 不允许升级为 ADMIN
                exist.setRole(body.getRole());
            }
        }
        if (body.getDoctorId() != null && "USER".equals(exist.getRole())) {
            exist.setDoctorId(body.getDoctorId());
        }
        if (body.getStatus() != null && !"ADMIN".equals(exist.getRole())) {
            exist.setStatus(body.getStatus());
        }
        userMapper.updateById(exist);
        return Result.success(userMapper.selectById(id));
    }

    @GetMapping("/users/{id}")
    public Result<SysUser> userDetail(@PathVariable Long id) {
        SysUser u = userMapper.selectById(id);
        if (u == null) throw new BusinessException(ResultCode.USER_NOT_FOUND);
        return Result.success(u);
    }

    // ---------- 训练记录审计 ----------
    @GetMapping("/training-records")
    public List<Map<String, Object>> listRecords() {
        List<TrainingRecord> records = recordMapper.selectList(
            Wrappers.<TrainingRecord>lambdaQuery().orderByDesc(TrainingRecord::getCreateTime)
        );
        return records.stream().map(r -> {
            Map<String, Object> row = new HashMap<>();
            row.put("id", r.getId());
            row.put("userId", r.getUserId());
            row.put("taskId", r.getTaskId());
            row.put("duration", r.getDuration());
            row.put("breathCount", r.getBreathCount());
            row.put("completionRate", r.getCompletionRate());
            row.put("score", r.getScore());
            row.put("heartRate", r.getHeartRate());
            row.put("dataHash", r.getDataHash());
            row.put("blockTxId", r.getBlockTxId());
            row.put("chainStatus", r.getChainStatus());
            row.put("createTime", r.getCreateTime());
            SysUser u = userMapper.selectById(r.getUserId());
            row.put("username", u != null ? u.getUsername() : null);
            row.put("userRealName", u != null ? u.getRealName() : null);
            BreathingTask t = taskMapper.selectById(r.getTaskId());
            row.put("taskName", t != null ? t.getTaskName() : null);
            return row;
        }).toList();
    }

    @GetMapping("/training-records/{id}/detail")
    public Map<String, Object> recordDetail(@PathVariable Long id) {
        TrainingRecord r = recordMapper.selectById(id);
        if (r == null) throw new BusinessException(ResultCode.TRAINING_RECORD_NOT_FOUND);
        SysUser u = userMapper.selectById(r.getUserId());
        BreathingTask t = taskMapper.selectById(r.getTaskId());

        // 顺带查链上一致性
        Boolean verified = null;
        try {
            if (r.getDataHash() != null) {
                org.web3j.abi.datatypes.Function fn = new org.web3j.abi.datatypes.Function(
                    "verifyRecord",
                    java.util.List.of(
                        new org.web3j.abi.datatypes.generated.Uint256(java.math.BigInteger.valueOf(r.getId())),
                        new org.web3j.abi.datatypes.generated.Bytes32(org.web3j.utils.Numeric.hexStringToByteArray(r.getDataHash()))
                    ),
                    java.util.List.of(new org.web3j.abi.TypeReference<org.web3j.abi.datatypes.Bool>() {})
                );
                String encoded = org.web3j.abi.FunctionEncoder.encode(fn);
                org.web3j.protocol.core.methods.response.EthCall response = web3j.ethCall(
                    org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
                        serviceCredentials.getAddress(),
                        web3Config.getTrainingRecordAddress(),
                        encoded),
                    org.web3j.protocol.core.DefaultBlockParameterName.LATEST
                ).send();
                if (!response.hasError()) {
                    java.util.List<org.web3j.abi.datatypes.Type> decoded =
                        org.web3j.abi.FunctionReturnDecoder.decode(response.getValue(), fn.getOutputParameters());
                    if (!decoded.isEmpty()) verified = (Boolean) decoded.get(0).getValue();
                }
            }
        } catch (Exception ex) { /* 链不通时 verified 保持 null */ }

        Map<String, Object> data = new HashMap<>();
        data.put("record", r);
        data.put("user", u);
        data.put("task", t);
        data.put("chainVerified", verified);
        return data;
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
