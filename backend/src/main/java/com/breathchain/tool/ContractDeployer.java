package com.breathchain.tool;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * 一次性合约部署器。
 * 用法：mvn exec:java -Dexec.mainClass=com.breathchain.tool.ContractDeployer \
 *      -Dexec.args="<rpcUrl> <chainId> <privateKeyHex> <breathTokenBinPath> <trainingRecordBinPath> [<backendAddr>]"
 */
public class ContractDeployer {

    public static void main(String[] args) throws Exception {
        if (args.length < 5) {
            System.err.println("用法: <rpcUrl> <chainId> <privateKeyHex> <BreathToken.bin> <TrainingRecord.bin> [backendAddr]");
            System.exit(1);
        }
        String rpcUrl = args[0];
        long chainId = Long.parseLong(args[1]);
        String pk = args[2].startsWith("0x") ? args[2] : "0x" + args[2];
        String tokenBin = new String(Files.readAllBytes(Path.of(args[3]))).trim();
        String recordBin = new String(Files.readAllBytes(Path.of(args[4]))).trim();
        if (!tokenBin.startsWith("0x")) tokenBin = "0x" + tokenBin;
        if (!recordBin.startsWith("0x")) recordBin = "0x" + recordBin;
        String backendAddr = args.length > 5 ? args[5] : null;

        Web3j web3 = Web3j.build(new HttpService(rpcUrl));
        Credentials credentials = Credentials.create(pk);
        System.out.println("Deployer: " + credentials.getAddress());

        TransactionManager tm = new RawTransactionManager(web3, credentials, chainId);

        System.out.println(">>> 部署 BreathToken ...");
        String tokenAddr = deploy(web3, tm, tokenBin);
        System.out.println(">>> BreathToken 地址: " + tokenAddr);

        System.out.println(">>> 部署 TrainingRecord ...");
        String recordAddr = deploy(web3, tm, recordBin);
        System.out.println(">>> TrainingRecord 地址: " + recordAddr);

        if (backendAddr != null) {
            System.out.println(">>> 把 " + backendAddr + " 加入 BreathToken 白名单 ...");
            callSet(web3, tm, tokenAddr, "setAwarder", backendAddr);
            System.out.println(">>> 把 " + backendAddr + " 加入 TrainingRecord 白名单 ...");
            callSet(web3, tm, recordAddr, "setSubmitter", backendAddr);
        }

        System.out.println();
        System.out.println("============ 部署完成 ============");
        System.out.println("export BREATH_TOKEN_ADDR=" + tokenAddr);
        System.out.println("export TRAINING_RECORD_ADDR=" + recordAddr);

        web3.shutdown();
    }

    private static String deploy(Web3j web3, TransactionManager tm, String binary) throws Exception {
        EthSendTransaction tx = tm.sendTransaction(
            BigInteger.valueOf(1_000_000_000L),
            BigInteger.valueOf(6_721_975L),
            "",
            binary,
            BigInteger.ZERO
        );
        if (tx.hasError()) throw new IOException("send tx error: " + tx.getError().getMessage());
        String txHash = tx.getTransactionHash();
        TransactionReceipt receipt = waitReceipt(web3, txHash);
        if (!"0x1".equals(receipt.getStatus())) {
            throw new IOException("deploy failed, status=" + receipt.getStatus());
        }
        return receipt.getContractAddress();
    }

    private static void callSet(Web3j web3, TransactionManager tm, String contract, String method, String addr) throws Exception {
        Function fn = new Function(method,
            List.of(new Address(addr), new Bool(true)),
            List.of());
        String encoded = FunctionEncoder.encode(fn);
        EthSendTransaction tx = tm.sendTransaction(
            BigInteger.valueOf(1_000_000_000L),
            BigInteger.valueOf(500_000L),
            contract,
            encoded,
            BigInteger.ZERO);
        if (tx.hasError()) throw new IOException("call err: " + tx.getError().getMessage());
        TransactionReceipt r = waitReceipt(web3, tx.getTransactionHash());
        if (!"0x1".equals(r.getStatus())) throw new IOException(method + " failed");
        System.out.println("  ok  txHash=" + r.getTransactionHash());
    }

    private static TransactionReceipt waitReceipt(Web3j web3, String txHash) throws Exception {
        for (int i = 0; i < 60; i++) {
            Optional<TransactionReceipt> r = web3.ethGetTransactionReceipt(txHash).send().getTransactionReceipt();
            if (r.isPresent()) return r.get();
            Thread.sleep(1000);
        }
        throw new IOException("timeout waiting receipt: " + txHash);
    }
}
