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

### 4.6 钱包地址配置

患者要想收到链上 `BREATH` 奖励，必须先给对应用户配置 `wallet_address`。

推荐做法：

1. 用管理员账号登录前端
2. 进入用户管理页，找到目标患者
3. 在编辑弹窗中填写区块链钱包地址（`0x...`）
4. 保存后，后续训练完成时即可把奖励发到该地址

如果需要生成测试钱包，可使用后端工具：

```bash
cd backend
mvn exec:java -Dexec.mainClass=com.breathchain.tool.WalletGenerator -Dexec.args="3"
```

该命令会输出测试用地址和私钥。将生成的 `Address` 写入患者的 `wallet_address`
字段即可；私钥仅用于测试环境，生产或正式演示时应由钱包持有人自行保管。

也可以直接通过 SQL 配置：

```sql
UPDATE sys_user
SET wallet_address = '0x...'
WHERE username = 'patient01';
```

如果患者未配置钱包地址，训练记录仍会正常落库并上链，但奖励代币不会实际发放到链上地址。

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

## 7. 查询链上真实哈希

项目中的 `TrainingRecord.verifyRecord(recordId, dataHash)` 只能返回 `true / false`，
用于判断链下数据与链上存证是否一致；如果要**拿到链上真实存储的哈希值**，必须调用
`TrainingRecord.getRecord(recordId)`，读取返回值中的 `dataHash` 字段。

本项目后端已经封装了这个查询过程。可直接调用：

```bash
GET /api/chain/verify/{recordId}
```

返回示例：

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "recordId": 24,
    "localDataHash": "0x295af548a3916e1cc6c498ab185335bc2c8dba48e93a80e1f260cf893dc37bde",
    "onChainDataHash": "0x295af548a3916e1cc6c498ab185335bc2c8dba48e93a80e1f260cf893dc37bde",
    "verified": true,
    "blockTxId": "0x00a6ebc3233ca562abf99cedd990172d16be50607ee65ac9de539b797807e2bb"
  }
}
```

字段说明：

- `localDataHash`: 前端 / 数据库中的哈希
- `onChainDataHash`: 通过链上 `getRecord(recordId)` 查出的真实哈希
- `verified`: 两者是否一致
- `blockTxId`: 该条训练记录的上链交易哈希

如果要在命令行中直接验证，可先登录拿到 token，再查询：

```bash
TOKEN=$(curl -s -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}' \
  http://localhost:18080/api/auth/login | \
  python3 -c 'import sys, json; print(json.load(sys.stdin)["data"]["token"])')

curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:18080/api/chain/verify/24
```

在前端页面中，管理员端「训练记录审计」和医生端「患者详情 -> 最近训练记录 -> 验证」
都会显示这两个值，从而明确体现该哈希是**从链上读取出来的真实值**，而不是前端本地生成。

## 8. License

仅用于本人毕业设计学术研究目的。
