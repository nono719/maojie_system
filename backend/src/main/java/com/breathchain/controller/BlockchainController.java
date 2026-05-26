package com.breathchain.controller;

import com.breathchain.common.BusinessException;
import com.breathchain.common.Result;
import com.breathchain.common.ResultCode;
import com.breathchain.entity.TrainingRecord;
import com.breathchain.mapper.TrainingRecordMapper;
import com.breathchain.service.BlockchainService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/chain")
@RequiredArgsConstructor
public class BlockchainController {

    private static final BigDecimal TOKEN_DECIMALS = BigDecimal.valueOf(1_000_000L);

    private final BlockchainService blockchainService;
    private final TrainingRecordMapper recordMapper;

    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @GetMapping("/verify/{recordId}")
    public Result<Map<String, Object>> verifyRecord(@PathVariable Long recordId) {
        TrainingRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(ResultCode.TRAINING_RECORD_NOT_FOUND);
        }
        if (record.getDataHash() == null) {
            return Result.success(Map.of(
                "verified", false, "reason", "记录尚未上链"
            ));
        }
        String localHash = record.getDataHash();
        String onChainHash = blockchainService.queryRecordHash(record.getId());
        Boolean verified = onChainHash == null ? null : onChainHash.equalsIgnoreCase(localHash);

        Map<String, Object> data = new HashMap<>();
        data.put("recordId", record.getId());
        data.put("localDataHash", localHash);
        data.put("dataHash", localHash);
        data.put("onChainDataHash", onChainHash);
        data.put("blockTxId", record.getBlockTxId());
        data.put("verified", verified);
        if (onChainHash == null) {
            data.put("reason", "无法读取链上真实哈希，请检查区块链节点或合约配置");
        }
        return Result.success(data);
    }

    @GetMapping("/balance/{address}")
    public Result<Map<String, Object>> balance(@PathVariable String address) {
        BigInteger raw = blockchainService.queryBalance(address);
        BigDecimal display = new BigDecimal(raw).divide(TOKEN_DECIMALS);
        return Result.success(Map.of(
            "address", address,
            "raw", raw.toString(),
            "balance", display.toPlainString()
        ));
    }
}
