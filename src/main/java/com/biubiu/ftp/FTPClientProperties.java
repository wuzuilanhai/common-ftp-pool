package com.biubiu.ftp;

import lombok.Data;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by Haibiao.Zhang on 2019-04-23 14:38
 */
@Data
@ConfigurationProperties(prefix = "ftp.client")
public class FTPClientProperties extends GenericObjectPoolConfig {

    // ftp地址
    private String host;

    // 端口号
    private Integer port = 21;

    // 登录用户
    private String username;

    // 登录密码
    private String password;

    // 被动模式
    private boolean passiveMode = true;

    // 编码
    private String encoding = "UTF-8";

    // 连接超时时间(秒)
    private Integer connectTimeout = 10 * 1000;

    // 数据超时时间(秒)
    private Integer dataTimeout = 300 * 1000;

    // socket连接超时时间(秒)
    private Integer soTimeout = 3 * 1000;

    // 缓冲大小
    private Integer bufferSize = 1024 * 1024;

    // 传输文件类型
    private Integer transferFileType = FTP.BINARY_FILE_TYPE;

}
