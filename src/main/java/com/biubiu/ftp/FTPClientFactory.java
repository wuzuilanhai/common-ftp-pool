package com.biubiu.ftp;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.io.IOException;

/**
 * Created by Haibiao.Zhang on 2019-04-23 14:40
 */
@Slf4j
public class FTPClientFactory extends BasePooledObjectFactory<FTPClient> {

    private FTPClientProperties config;

    public FTPClientFactory(FTPClientProperties config) {
        this.config = config;
    }

    public FTPClientProperties getConfig() {
        return config;
    }

    /**
     * 创建FTPClient对象
     */
    @Override
    public FTPClient create() throws Exception {
        FTPClient ftpClient = new FTPClient();
        ftpClient.setControlEncoding(config.getEncoding());
        ftpClient.setConnectTimeout(config.getConnectTimeout());
        ftpClient.setDataTimeout(config.getDataTimeout());
        try {

            ftpClient.connect(config.getHost(), config.getPort());
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                ftpClient.disconnect();
                log.warn("FTPServer refused connection,replyCode:{}", replyCode);
                return null;
            }

            ftpClient.setSoTimeout(config.getSoTimeout());

            if (!ftpClient.login(config.getUsername(), config.getPassword())) {
                log.warn("ftpClient login failed... username is {}; password: {}", config.getUsername(), config.getPassword());
            }

            ftpClient.setBufferSize(config.getBufferSize());
            ftpClient.setFileType(config.getTransferFileType());
            if (config.isPassiveMode()) {
                ftpClient.enterLocalPassiveMode();
            }
            log.info("create ftp connection success!", ftpClient);
        } catch (IOException e) {
            log.error("create ftp connection failed...", e.getLocalizedMessage());
            throw e;
        }
        return ftpClient;
    }

    /**
     * 用PooledObject封装对象放入池中
     */
    @Override
    public PooledObject<FTPClient> wrap(FTPClient obj) {
        return new DefaultPooledObject<>(obj);
    }

    /**
     * 销毁FTPClient对象
     */
    @Override
    public void destroyObject(PooledObject<FTPClient> p) throws Exception {
        if (p == null) return;

        FTPClient ftpClient = p.getObject();

        try {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
            }
        } catch (IOException io) {
            log.error("ftp client logout failed...{}", io.getLocalizedMessage());
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException io) {
                    log.error("close ftp client failed...{}", io.getLocalizedMessage());
                }
            }
        }
    }

    /**
     * 验证FTPClient对象
     */
    @Override
    public boolean validateObject(PooledObject<FTPClient> p) {
        try {
            FTPClient ftpClient = p.getObject();
            return ftpClient.sendNoOp();
        } catch (IOException e) {
            log.error("Failed to validate client: {}", e.getLocalizedMessage());
        }
        return false;
    }

}
