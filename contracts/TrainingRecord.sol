// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

/**
 * @title TrainingRecord
 * @notice 训练记录存证合约 — 论文 5.2.3 + 附录 A
 *         链下 MySQL 存原始数据，链上仅存 Keccak-256 哈希
 */
contract TrainingRecord {
    struct Record {
        uint256 userId;
        uint256 taskId;
        uint256 duration;        // 实际训练时长（秒）
        uint256 completionRate;  // 完成率 * 100 (保留两位小数转整数)
        uint256 score;           // 训练评分
        uint256 timestamp;       // 上链时间戳
        bytes32 dataHash;        // 链下数据的 Keccak-256
        address submitter;       // 提交者（后端服务地址）
        bool exists;
    }

    address public owner;
    mapping(address => bool) public submitters;

    // 训练记录ID -> 记录
    mapping(uint256 => Record) public records;

    event RecordAdded(
        uint256 indexed recordId,
        uint256 indexed userId,
        uint256 indexed taskId,
        bytes32 dataHash,
        uint256 timestamp
    );
    event SubmitterUpdated(address indexed account, bool enabled);
    event OwnershipTransferred(address indexed previousOwner, address indexed newOwner);

    modifier onlyOwner() {
        require(msg.sender == owner, "TrainingRecord: not owner");
        _;
    }

    modifier onlySubmitter() {
        require(submitters[msg.sender] || msg.sender == owner, "TrainingRecord: not submitter");
        _;
    }

    constructor() {
        owner = msg.sender;
        emit OwnershipTransferred(address(0), msg.sender);
    }

    function transferOwnership(address newOwner) external onlyOwner {
        require(newOwner != address(0), "TrainingRecord: zero owner");
        emit OwnershipTransferred(owner, newOwner);
        owner = newOwner;
    }

    function setSubmitter(address account, bool enabled) external onlyOwner {
        submitters[account] = enabled;
        emit SubmitterUpdated(account, enabled);
    }

    /**
     * @notice 添加一条训练记录上链
     * @dev    recordId 由后端业务系统保证唯一（对应 MySQL training_record.id）
     */
    function addRecord(
        uint256 recordId,
        uint256 userId,
        uint256 taskId,
        uint256 duration,
        uint256 completionRate,
        uint256 score,
        bytes32 dataHash
    ) external onlySubmitter {
        require(!records[recordId].exists, "TrainingRecord: exists");
        require(dataHash != bytes32(0), "TrainingRecord: empty hash");

        records[recordId] = Record({
            userId: userId,
            taskId: taskId,
            duration: duration,
            completionRate: completionRate,
            score: score,
            timestamp: block.timestamp,
            dataHash: dataHash,
            submitter: msg.sender,
            exists: true
        });

        emit RecordAdded(recordId, userId, taskId, dataHash, block.timestamp);
    }

    /**
     * @notice 验证某条链下记录是否未被篡改
     * @return true = 哈希一致；false = 记录不存在或哈希不匹配
     */
    function verifyRecord(uint256 recordId, bytes32 dataHash) external view returns (bool) {
        Record storage r = records[recordId];
        return r.exists && r.dataHash == dataHash;
    }

    function getRecord(uint256 recordId)
        external
        view
        returns (
            uint256 userId,
            uint256 taskId,
            uint256 duration,
            uint256 completionRate,
            uint256 score,
            uint256 timestamp,
            bytes32 dataHash
        )
    {
        Record storage r = records[recordId];
        require(r.exists, "TrainingRecord: not found");
        return (r.userId, r.taskId, r.duration, r.completionRate, r.score, r.timestamp, r.dataHash);
    }
}
