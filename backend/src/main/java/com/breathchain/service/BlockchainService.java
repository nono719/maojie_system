package com.breathchain.service;

import com.breathchain.entity.TrainingRecord;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 区块链交互服务 — 训练记录上链 + 代币奖励发放。
 * 实现细节见 BlockchainServiceImpl，对接 FISCO BCOS / 以太坊兼容节点。
 */
public interface BlockchainService {

    /**
     * 根据训练数据计算 Keccak-256 哈希
     */
    String computeHash(TrainingRecord record);

    /**
     * 把训练记录哈希上链
     * @return 交易哈希 (0x...)
     */
    String submitTrainingRecord(TrainingRecord record, String dataHash);

    /**
     * 向指定钱包地址发放代币奖励
     * @return 交易哈希
     */
    String awardUser(String toAddress, Long taskId, BigDecimal amount);

    /**
     * 验证某条链下记录是否与链上一致
     */
    boolean verifyRecord(Long recordId, String expectedHash);

    /**
     * 读取链上存储的训练记录哈希；记录不存在或链不可用时返回 null
     */
    String queryRecordHash(Long recordId);

    /**
     * 查询某地址的代币余额（最小单位）
     */
    BigInteger queryBalance(String address);
}
