# BreathChain - 基于区块链的呼吸训练干预系统

> 成都信息工程大学 · 人工智能学院 · 区块链工程
> 毛杰 (2022131065) 本科毕业设计
> 指导教师：金虎 教授；辅助指导：黄源源 副教授

📖 **完整操作手册**：[docs/USER_GUIDE.md](docs/USER_GUIDE.md) — 三角色流程、钱包配置、奖励机制、链上验证等

## 1. 项目简介

针对慢阻肺、哮喘、心脏康复等需要长期呼吸训练的患者，构建一套医生 + 患者协同的居家康复 Web 系统。

**核心创新点**：
- 训练数据 **链下 MySQL** 存原始信息，**链上 FISCO BCOS** 仅存 Keccak-256 哈希 — 防篡改且节省 Gas
- **BreathToken** ERC-20 智能合约自动发放代币奖励，激励患者坚持训练
- **TrainingRecord** 合约提供 `addRecord` / `verifyRecord` 用于医生抽查数据是否被篡改

## 2. 技术栈（论文 5.1.2）

| 层 | 技术 |
|----|------|
| 前端 | Vue 3.2 + Vite + Pinia + Vue Router + Ant Design Vue + ECharts |
| 后端 | Spring Boot 3.2 + Spring Security + JWT + MyBatis-Plus |
| 数据库 | MySQL 8.0（读写分离）+ Redis 6.0（热点缓存） |
| 区块链 | FISCO BCOS 3.0 联盟链 + Solidity ^0.8.0 + Web3j |

## 3. 目录结构

```
maojie_system/
├── contracts/        # Solidity 智能合约 (BreathToken, TrainingRecord)
├── sql/              # 数据库初始化脚本
├── backend/          # Spring Boot 后端
├── frontend/         # Vue 3 前端
├── docker-compose.yml
└── README.md
```

## 4. 快速开始

### 4.1 启动依赖

```bash
# MySQL + Redis
docker compose up -d

# FISCO BCOS 节点（参考 https://fisco-bcos-doc.readthedocs.io）
bash build_chain.sh -l 127.0.0.1:4 -p 30300,20200
```

### 4.2 部署合约

```bash
solc --abi --bin --optimize -o build/ contracts/*.sol
# 用 console / web3j 部署，记录两个地址
# 把后端服务账户加入白名单
```

### 4.3 启动后端

```bash
cd backend
export BREATH_TOKEN_ADDR=0x...
export TRAINING_RECORD_ADDR=0x...
export BREATHCHAIN_SERVICE_KEY=0x...
./mvnw spring-boot:run
```

### 4.4 启动前端

```bash
cd frontend
npm install
npm run dev
# 浏览器打开 http://localhost:5173
```

### 4.5 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | ADMIN |
| doctor01 | admin123 | DOCTOR |
| patient01 | admin123 | USER |

## 5. 核心业务流程

```
患者 → 选择训练任务 → 开始呼吸训练（动画引导）
       ↓
   训练完成 → 后端写入 MySQL（PENDING）
       ↓
   计算 Keccak-256 哈希 → TrainingRecord.addRecord 上链
       ↓
   完成率 ≥ 60% → BreathToken.awardUser 发放代币
       ↓
   返回 txHash + 奖励金额给前端
```

## 6. 论文章节对应

| 论文章节 | 代码位置 |
|----------|---------|
| 5.2.2 数据库设计 | [sql/init.sql](sql/init.sql) |
| 5.2.3 区块链模块 / 附录A | [contracts/](contracts/) |
| 6.2 医生端 | [frontend/src/views/doctor/](frontend/src/views/doctor/) |
| 6.3 患者端 | [frontend/src/views/patient/](frontend/src/views/patient/) |
| 6.4 区块链交互 | [backend/.../service/impl/BlockchainServiceImpl.java](backend/src/main/java/com/breathchain/service/impl/BlockchainServiceImpl.java) |
| 7.2 / 7.3 测试 | 待补充 |

## 7. License

仅用于本人毕业设计学术研究目的。
