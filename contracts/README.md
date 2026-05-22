# Smart Contracts

呼吸训练干预系统智能合约 - Solidity ^0.8.0

| 合约 | 作用 |
|------|------|
| `BreathToken.sol` | ERC-20 兼容代币 `BREATH`，含白名单 `awardUser` 与「同人同任务同日仅一次」防刷规则 |
| `TrainingRecord.sol` | 训练记录哈希存证，提供 `addRecord` / `verifyRecord` |

## 编译

使用 [solc](https://docs.soliditylang.org/en/latest/installing-solidity.html) 0.8.x：

```bash
solc --abi --bin --optimize \
  -o build/ \
  contracts/BreathToken.sol contracts/TrainingRecord.sol
```

## 部署到 FISCO BCOS

1. 启动 FISCO BCOS 3.0 本地链（参考官方 [build_chain.sh](https://fisco-bcos-doc.readthedocs.io/zh-cn/latest/docs/quick_start/air_installation.html)）
2. 用 console 部署：

```bash
[group0]: /apps>  deploy BreathToken
[group0]: /apps>  deploy TrainingRecord
```

3. 取得合约地址，写入后端 `application.yml`：

```yaml
blockchain:
  breath-token-address: 0x...
  training-record-address: 0x...
```

4. 把后端服务账户加入白名单：

```bash
[group0]: /apps>  call BreathToken <addr> setAwarder <backend-address> true
[group0]: /apps>  call TrainingRecord <addr> setSubmitter <backend-address> true
```

## 用 Web3j 生成 Java 包装类

```bash
solc --abi --bin --optimize \
  -o build/ \
  contracts/BreathToken.sol contracts/TrainingRecord.sol

web3j generate solidity \
  -b build/BreathToken.bin \
  -a build/BreathToken.abi \
  -p com.breathchain.blockchain.contract \
  -o backend/src/main/java
```

对 `TrainingRecord` 重复一次即可。

## 安全设计要点（论文 2.2）

- **权限分层**：`owner` 部署者保管，`awarders/submitters` 白名单只允许后端服务地址调用敏感函数。
- **防重入**：所有状态改变在外部调用之前完成（Checks-Effects-Interactions）。
- **防刷奖**：`BreathToken.awardUser` 内置「同人同任务同日只能领一次」检查。
- **链下存大数据，链上存哈希**：节省 Gas、加快查询。
