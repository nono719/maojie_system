# BreathChain Backend

Spring Boot 3.2 + Java 17 + MyBatis-Plus + Redis + Web3j.

## 启动

```bash
# 1. 启动依赖（在仓库根目录）
docker compose up -d mysql redis

# 2. 初始化数据库
mysql -h 127.0.0.1 -u breathchain -p < ../sql/init.sql

# 3. 编译运行
./mvnw spring-boot:run
# 或
./mvnw clean package && java -jar target/breathchain-backend-1.0.0.jar
```

## 关键端点

| Method | Path | 鉴权 | 说明 |
|--------|------|------|------|
| POST | `/api/auth/login` | 公开 | 用户名密码登录，返回 JWT |
| POST | `/api/auth/register` | 公开 | 注册（USER/DOCTOR） |
| GET  | `/api/auth/me` | 任意角色 | 当前登录态 |
| POST | `/api/tasks` | DOCTOR | 创建训练任务 |
| GET  | `/api/tasks/mine` | DOCTOR | 我创建的任务 |
| POST | `/api/tasks/{id}/publish` | DOCTOR | 发布任务 |
| POST | `/api/tasks/{taskId}/assign/{patientId}` | DOCTOR | 分配给患者 |
| GET  | `/api/tasks/assigned` | USER | 我的训练任务 |
| POST | `/api/training/{taskId}/complete` | USER | 完成一次训练（核心链路） |
| GET  | `/api/training/history` | 任意 | 我的训练历史 |
| GET  | `/api/training/history/{patientId}` | DOCTOR | 患者训练历史 |
| GET  | `/api/chain/verify/{recordId}` | DOCTOR/ADMIN | 校验记录是否被篡改 |
| GET  | `/api/chain/balance/{address}` | 任意 | 查询代币余额 |
| GET  | `/api/doctor/dashboard` | DOCTOR | 医生数据概览 |
| GET  | `/api/doctor/patients` | DOCTOR | 我的患者列表 |
| GET  | `/api/patient/home` | USER | 患者首页数据 |
| GET  | `/api/patient/rewards` | USER | 奖励发放记录 |

## 区块链配置

合约部署后，在 `application.yml` 配置地址 + 私钥（建议环境变量）：

```bash
export BREATH_TOKEN_ADDR=0x...
export TRAINING_RECORD_ADDR=0x...
export BREATHCHAIN_SERVICE_KEY=0x...    # 后端服务账户，须事先 setAwarder/setSubmitter
```

开发调试可设 `breathchain.blockchain.enabled=false` 跳过链调用。

## 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | ADMIN |
| doctor01 | admin123 | DOCTOR |
| patient01 | admin123 | USER |
