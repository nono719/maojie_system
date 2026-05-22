package com.breathchain.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

import java.math.BigInteger;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "breathchain.blockchain")
public class Web3jConfig {

    private boolean enabled = true;
    private String nodeUrl;
    private long chainId;
    private BigInteger gasPrice;
    private BigInteger gasLimit;
    private String servicePrivateKey;
    private String breathTokenAddress;
    private String trainingRecordAddress;

    @Bean
    public Web3j web3j() {
        log.info("Connecting to blockchain node: {}", nodeUrl);
        return Web3j.build(new HttpService(nodeUrl));
    }

    @Bean
    public Credentials serviceCredentials() {
        return Credentials.create(servicePrivateKey);
    }

    @Bean
    public ContractGasProvider gasProvider() {
        return new StaticGasProvider(gasPrice, gasLimit);
    }
}
