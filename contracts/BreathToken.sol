// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

/**
 * @title BreathToken
 * @notice 呼吸训练干预系统代币合约 - ERC-20 兼容
 *         论文 2.2 智能合约安全机制：所有权 + 白名单 + Checks-Effects-Interactions
 */
contract BreathToken {
    string public constant name = "BreathChain Token";
    string public constant symbol = "BREATH";
    uint8 public constant decimals = 6;
    uint256 public totalSupply;

    address public owner;

    mapping(address => uint256) public balanceOf;
    mapping(address => mapping(address => uint256)) public allowance;

    // 奖励发放白名单（仅后端服务地址可调用 awardUser）
    mapping(address => bool) public awarders;

    // 防刷奖：用户 -> 任务ID -> 当日已领次数  (key = taskId * 1e18 + dayIndex)
    mapping(address => mapping(uint256 => uint256)) public dailyClaimed;

    event Transfer(address indexed from, address indexed to, uint256 value);
    event Approval(address indexed owner, address indexed spender, uint256 value);
    event Award(address indexed to, uint256 indexed taskId, uint256 value);
    event AwarderUpdated(address indexed account, bool enabled);
    event OwnershipTransferred(address indexed previousOwner, address indexed newOwner);

    modifier onlyOwner() {
        require(msg.sender == owner, "BreathToken: not owner");
        _;
    }

    modifier onlyAwarder() {
        require(awarders[msg.sender] || msg.sender == owner, "BreathToken: not awarder");
        _;
    }

    constructor() {
        owner = msg.sender;
        emit OwnershipTransferred(address(0), msg.sender);
    }

    // -------- 所有权管理 --------
    function transferOwnership(address newOwner) external onlyOwner {
        require(newOwner != address(0), "BreathToken: zero owner");
        emit OwnershipTransferred(owner, newOwner);
        owner = newOwner;
    }

    function setAwarder(address account, bool enabled) external onlyOwner {
        awarders[account] = enabled;
        emit AwarderUpdated(account, enabled);
    }

    // -------- ERC-20 核心 --------
    function transfer(address to, uint256 value) external returns (bool) {
        _transfer(msg.sender, to, value);
        return true;
    }

    function approve(address spender, uint256 value) external returns (bool) {
        allowance[msg.sender][spender] = value;
        emit Approval(msg.sender, spender, value);
        return true;
    }

    function transferFrom(address from, address to, uint256 value) external returns (bool) {
        uint256 currentAllowance = allowance[from][msg.sender];
        require(currentAllowance >= value, "BreathToken: allowance exceeded");
        allowance[from][msg.sender] = currentAllowance - value;
        _transfer(from, to, value);
        return true;
    }

    function _transfer(address from, address to, uint256 value) internal {
        require(to != address(0), "BreathToken: zero to");
        require(balanceOf[from] >= value, "BreathToken: balance low");
        // Checks-Effects-Interactions: 先更新状态，再触发事件
        balanceOf[from] -= value;
        balanceOf[to] += value;
        emit Transfer(from, to, value);
    }

    // -------- 铸币 / 发奖 --------
    function mint(address to, uint256 amount) external onlyOwner {
        require(to != address(0), "BreathToken: zero to");
        totalSupply += amount;
        balanceOf[to] += amount;
        emit Transfer(address(0), to, amount);
    }

    /**
     * @notice 给用户发放训练奖励，并记账"同人同任务同日仅一次"
     * @param to       患者钱包地址
     * @param taskId   训练任务ID
     * @param amount   奖励数量（含 decimals 的最小单位）
     */
    function awardUser(address to, uint256 taskId, uint256 amount) external onlyAwarder {
        require(to != address(0), "BreathToken: zero to");
        require(amount > 0, "BreathToken: zero amount");

        uint256 dayIndex = block.timestamp / 1 days;
        uint256 slot = taskId * 1e18 + dayIndex;
        require(dailyClaimed[to][slot] == 0, "BreathToken: already claimed today");

        // Effects
        dailyClaimed[to][slot] = amount;
        totalSupply += amount;
        balanceOf[to] += amount;

        // Interactions (events only)
        emit Transfer(address(0), to, amount);
        emit Award(to, taskId, amount);
    }
}
