package com.breathchain.tool;

import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.utils.Numeric;

/**
 * 一次性钱包生成器。
 * 用法：mvn exec:java -Dexec.mainClass=com.breathchain.tool.WalletGenerator -Dexec.args="3"
 * 默认生成 1 个；secp256k1 + EVM 地址（兼容 FISCO BCOS / Ethereum）
 */
public class WalletGenerator {
    public static void main(String[] args) throws Exception {
        int n = args.length > 0 ? Integer.parseInt(args[0]) : 1;
        for (int i = 0; i < n; i++) {
            ECKeyPair pair = Keys.createEcKeyPair();
            String pk = Numeric.toHexStringWithPrefixZeroPadded(pair.getPrivateKey(), 64);
            String addr = "0x" + Keys.getAddress(pair);
            System.out.println("=== Wallet #" + (i + 1) + " ===");
            System.out.println("Address    : " + addr);
            System.out.println("PrivateKey : " + pk);
            System.out.println();
        }
    }
}
