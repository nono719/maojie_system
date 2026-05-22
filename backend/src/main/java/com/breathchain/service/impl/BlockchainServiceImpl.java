package com.breathchain.service.impl;

import com.breathchain.common.BusinessException;
import com.breathchain.common.ResultCode;
import com.breathchain.config.Web3jConfig;
import com.breathchain.entity.TrainingRecord;
import com.breathchain.service.BlockchainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 区块链交互实现：直接通过 Web3j 的 Function 编码调用合约。
 * <p>
 * 生产环境建议用 `web3j generate solidity` 生成强类型 Wrapper，
 * 这里保持骨架阶段的最小依赖。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BlockchainServiceImpl implements BlockchainService {

    private static final BigInteger TOKEN_DECIMALS = BigInteger.TEN.pow(6); // 与合约 decimals 一致

    private final Web3j web3j;
    private final Credentials serviceCredentials;
    private final Web3jConfig blockchainProps;

    private TransactionManager txManager;

    private TransactionManager txManager() {
        if (txManager == null) {
            txManager = new RawTransactionManager(web3j, serviceCredentials, blockchainProps.getChainId());
        }
        return txManager;
    }

    @Override
    public String computeHash(TrainingRecord r) {
        // 与论文 4.3 / 5.2.3 一致：拼接关键字段后 Keccak-256
        String payload = String.format("%d|%d|%d|%s|%d|%d",
            r.getUserId(),
            r.getTaskId(),
            r.getDuration(),
            r.getCompletionRate() == null ? "0" : r.getCompletionRate().toPlainString(),
            r.getScore(),
            System.currentTimeMillis() / 1000);
        byte[] hashed = Hash.sha3(payload.getBytes(StandardCharsets.UTF_8));
        return Numeric.toHexString(hashed);
    }

    @Override
    public String submitTrainingRecord(TrainingRecord r, String dataHash) {
        if (!blockchainProps.isEnabled()) {
            log.warn("Blockchain disabled, skip submitTrainingRecord");
            return null;
        }
        Function fn = new Function(
            "addRecord",
            Arrays.asList(
                new Uint256(BigInteger.valueOf(r.getId())),
                new Uint256(BigInteger.valueOf(r.getUserId())),
                new Uint256(BigInteger.valueOf(r.getTaskId())),
                new Uint256(BigInteger.valueOf(r.getDuration())),
                new Uint256(toUint(r.getCompletionRate().multiply(BigDecimal.valueOf(100)))),
                new Uint256(BigInteger.valueOf(r.getScore())),
                new Bytes32(Numeric.hexStringToByteArray(dataHash))
            ),
            Collections.emptyList()
        );
        return sendTransaction(blockchainProps.getTrainingRecordAddress(), fn);
    }

    @Override
    public String awardUser(String toAddress, Long taskId, BigDecimal amount) {
        if (!blockchainProps.isEnabled()) {
            log.warn("Blockchain disabled, skip awardUser");
            return null;
        }
        BigInteger amountInUnits = amount.multiply(new BigDecimal(TOKEN_DECIMALS)).toBigInteger();
        Function fn = new Function(
            "awardUser",
            Arrays.asList(
                new Address(toAddress),
                new Uint256(BigInteger.valueOf(taskId)),
                new Uint256(amountInUnits)
            ),
            Collections.emptyList()
        );
        return sendTransaction(blockchainProps.getBreathTokenAddress(), fn);
    }

    @Override
    public boolean verifyRecord(Long recordId, String expectedHash) {
        if (!blockchainProps.isEnabled()) return true;
        Function fn = new Function(
            "verifyRecord",
            Arrays.asList(
                new Uint256(BigInteger.valueOf(recordId)),
                new Bytes32(Numeric.hexStringToByteArray(expectedHash))
            ),
            Collections.singletonList(new TypeReference<Bool>() {})
        );
        return callBoolean(blockchainProps.getTrainingRecordAddress(), fn);
    }

    @Override
    public BigInteger queryBalance(String address) {
        if (!blockchainProps.isEnabled()) return BigInteger.ZERO;
        Function fn = new Function(
            "balanceOf",
            Collections.singletonList(new Address(address)),
            Collections.singletonList(new TypeReference<Uint256>() {})
        );
        String encoded = FunctionEncoder.encode(fn);
        try {
            EthCall response = web3j.ethCall(
                Transaction.createEthCallTransaction(
                    serviceCredentials.getAddress(),
                    blockchainProps.getBreathTokenAddress(),
                    encoded),
                DefaultBlockParameterName.LATEST
            ).send();
            if (response.hasError()) {
                throw new BusinessException(ResultCode.BLOCKCHAIN_ERROR, response.getError().getMessage());
            }
            List<Type> decoded = FunctionReturnDecoder.decode(response.getValue(), fn.getOutputParameters());
            return decoded.isEmpty() ? BigInteger.ZERO : (BigInteger) decoded.get(0).getValue();
        } catch (Exception ex) {
            log.error("queryBalance failed", ex);
            throw new BusinessException(ResultCode.BLOCKCHAIN_ERROR, ex.getMessage());
        }
    }

    // ----------------- helpers -----------------

    private String sendTransaction(String contractAddress, Function fn) {
        try {
            String encoded = FunctionEncoder.encode(fn);
            EthSendTransaction tx = txManager().sendTransaction(
                blockchainProps.getGasPrice(),
                blockchainProps.getGasLimit(),
                contractAddress,
                encoded,
                BigInteger.ZERO
            );
            if (tx.hasError()) {
                throw new BusinessException(ResultCode.BLOCKCHAIN_ERROR, tx.getError().getMessage());
            }
            String txHash = tx.getTransactionHash();
            // 等待回执
            Optional<TransactionReceipt> receipt = web3j.ethGetTransactionReceipt(txHash).send().getTransactionReceipt();
            receipt.ifPresent(r -> log.info("tx {} block={}, status={}", txHash, r.getBlockNumber(), r.getStatus()));
            return txHash;
        } catch (BusinessException be) {
            throw be;
        } catch (Exception ex) {
            log.error("sendTransaction to {} failed", contractAddress, ex);
            throw new BusinessException(ResultCode.BLOCKCHAIN_ERROR, ex.getMessage());
        }
    }

    private boolean callBoolean(String contractAddress, Function fn) {
        try {
            String encoded = FunctionEncoder.encode(fn);
            EthCall response = web3j.ethCall(
                Transaction.createEthCallTransaction(
                    serviceCredentials.getAddress(), contractAddress, encoded),
                DefaultBlockParameterName.LATEST
            ).send();
            if (response.hasError()) {
                throw new BusinessException(ResultCode.BLOCKCHAIN_ERROR, response.getError().getMessage());
            }
            List<Type> decoded = FunctionReturnDecoder.decode(response.getValue(), fn.getOutputParameters());
            return !decoded.isEmpty() && (Boolean) decoded.get(0).getValue();
        } catch (BusinessException be) {
            throw be;
        } catch (Exception ex) {
            log.error("callBoolean to {} failed", contractAddress, ex);
            throw new BusinessException(ResultCode.BLOCKCHAIN_ERROR, ex.getMessage());
        }
    }

    private static BigInteger toUint(BigDecimal d) {
        return d == null ? BigInteger.ZERO : d.toBigInteger();
    }
}
