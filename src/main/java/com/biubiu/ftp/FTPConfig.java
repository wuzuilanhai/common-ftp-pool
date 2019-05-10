package com.biubiu.ftp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Haibiao.Zhang on 2019-04-23 15:36
 */
@Configuration
@EnableConfigurationProperties(FTPClientProperties.class)
public class FTPConfig {

    @Autowired
    private FTPClientProperties config;

    @Bean
    public FTPClientFactory ftpClientFactory() {
        return new FTPClientFactory(config);
    }

    @Bean
    public FTPUtil ftpUtil(FTPClientFactory ftpClientFactory) {
        return new FTPUtil(ftpClientFactory);
    }

}
