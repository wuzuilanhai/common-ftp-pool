package com.biubiu.ftp;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.io.*;

/**
 * Created by Haibiao.Zhang on 2019-04-23 9:25
 */
@Slf4j
public class FTPUtil {

    private GenericObjectPool<FTPClient> ftpClientPool;

    public FTPUtil(FTPClientFactory ftpClientFactory) {
        GenericObjectPoolConfig poolConfig = ftpClientFactory.getConfig();
        this.ftpClientPool = new GenericObjectPool<>(ftpClientFactory, poolConfig);
    }

    /**
     * 上传文件
     *
     * @param uri  文件路径
     * @param name 文件名称
     * @return true or false
     */
    public boolean uploadFile(String uri, String name) {
        FTPClient mFtpClient = null;
        File file = new File(uri);
        try (FileInputStream srcFileStream = new FileInputStream(file)) {
            //从池中取ftp连接
            mFtpClient = ftpClientPool.borrowObject();
            if (!checkClient(mFtpClient)) return false;

            boolean status = mFtpClient.storeFile(name, srcFileStream);
            log.info("upload status: {}", String.valueOf(status));
            return status;

        } catch (Exception e) {
            log.error("upload file error: {}", e.getLocalizedMessage());
        } finally {
            //将对象放回池中
            ftpClientPool.returnObject(mFtpClient);
        }
        return false;
    }

    /**
     * 以流方式上传文件
     *
     * @param srcFileStream 文件流
     * @param name          文件名称
     * @return true or false
     */
    public boolean uploadFile(InputStream srcFileStream, String name) {
        FTPClient mFtpClient = null;
        try {
            //从池中取ftp连接
            mFtpClient = ftpClientPool.borrowObject();
            if (!checkClient(mFtpClient)) return false;

            boolean status = mFtpClient.storeFile(name, srcFileStream);
            log.info("upload status: {}", String.valueOf(status));
            srcFileStream.close();
            return status;
        } catch (Exception e) {
            log.error("upload file error: {}", e.getLocalizedMessage());
        } finally {
            //将对象放回池中
            ftpClientPool.returnObject(mFtpClient);
        }
        return false;
    }

    /**
     * 下载文件
     *
     * @param remoteFilePath 远程文件名称
     * @param dest           下载文件存放路径+文件名称
     * @return true or false
     */
    public boolean downloadFile(String remoteFilePath, String dest) {
        FTPClient mFtpClient = null;
        File downloadFile = new File(dest);
        File parentDir = downloadFile.getParentFile();
        if (!parentDir.exists()) parentDir.mkdir();
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile))) {
            //从池中取ftp连接
            mFtpClient = ftpClientPool.borrowObject();
            if (!checkClient(mFtpClient)) return false;

            boolean status = mFtpClient.retrieveFile(remoteFilePath, outputStream);
            log.info("download status: {}", String.valueOf(status));
            return status;
        } catch (Exception e) {
            log.error("download file error: {}", e.getLocalizedMessage());
        } finally {
            //将对象放回池中
            ftpClientPool.returnObject(mFtpClient);
        }
        return false;
    }

    /**
     * 删除文件
     *
     * @param remotePath FTP服务器保存目录
     * @param fileName   要删除的文件名称
     * @return true or false
     */
    public boolean deleteFile(String remotePath, String fileName) {
        FTPClient mFtpClient = null;
        try {
            //从池中取ftp连接
            mFtpClient = ftpClientPool.borrowObject();
            if (!checkClient(mFtpClient)) return false;

            // 切换FTP目录
            mFtpClient.changeWorkingDirectory(remotePath);
            int delCode = mFtpClient.dele(fileName);
            log.debug("delete file reply code:{}", delCode);
            return true;
        } catch (Exception e) {
            log.error("delete file failure!", e);
        } finally {
            //将对象放回池中
            ftpClientPool.returnObject(mFtpClient);
        }
        return false;
    }

    /**
     * 获取指定目录下的所有文件
     *
     * @param remotePath 指定目录
     * @return 指定目录下的所有文件
     */
    public String[] listName(FTPClient mFtpClient, String remotePath) {
        try {
            // 切换FTP目录
            mFtpClient.changeWorkingDirectory(remotePath);
            return mFtpClient.listNames();
        } catch (Exception e) {
            log.error("list file failure!", e);
        }
        return null;
    }

    /**
     * 获取ftp连接
     *
     * @return ftp连接
     */
    public FTPClient getFTPClient() throws Exception {
        FTPClient mFtpClient;
        try {
            //从池中取ftp连接
            mFtpClient = ftpClientPool.borrowObject();
            if (!checkClient(mFtpClient)) return null;
            return mFtpClient;
        } catch (Exception e) {
            log.error("get ftp client failure!", e);
            throw e;
        }
    }

    /**
     * 获取某个文件的输入流
     *
     * @param file 文件名称
     * @return 文件的输入流
     */
    public InputStream getFileInputStream(ByteArrayOutputStream fos, FTPClient mFtpClient, String file) {
        if (!checkClient(mFtpClient)) return null;
        try {
            mFtpClient.retrieveFile(file, fos);
            return new ByteArrayInputStream(fos.toByteArray());
        } catch (Exception e) {
            log.error("get stream failure!", e);
        }
        return null;
    }

    /**
     * 将对象放回池中
     *
     * @param ftpClient ftp连接
     */
    public void returnObject(FTPClient ftpClient) {
        ftpClientPool.returnObject(ftpClient);
    }

    private boolean checkClient(FTPClient mFtpClient) {
        // 验证FTP服务器是否登录成功
        int replyCode = mFtpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(replyCode)) {
            log.warn("ftpServer refused connection, replyCode:{}", replyCode);
            return false;
        }
        return true;
    }

}
