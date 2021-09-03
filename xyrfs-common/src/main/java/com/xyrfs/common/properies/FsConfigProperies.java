package com.xyrfs.common.properies;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @author zxh
 */
@Component
@ConfigurationProperties(prefix = "fs.config")
public class FsConfigProperies {

    private boolean simpleModel;

    private boolean logSaveDb;

    private boolean logPrint;

    private boolean sysLog;

    private boolean operateLog;

    private boolean operateLogAll;

    /**
     * 上传url
     */
    private String fsUrl;

    private String fsAppid;
    private String fsUsername;
    private String fsGroupid;

    public boolean isSimpleModel() {
        return simpleModel;
    }

    public void setSimpleModel(boolean simpleModel) {
        this.simpleModel = simpleModel;
    }

    public boolean isLogSaveDb() {
        return logSaveDb;
    }

    public void setLogSaveDb(boolean logSaveDb) {
        this.logSaveDb = logSaveDb;
    }

    public boolean isLogPrint() {
        return logPrint;
    }

    public void setLogPrint(boolean logPrint) {
        this.logPrint = logPrint;
    }

    public boolean isSysLog() {
        return sysLog;
    }

    public void setSysLog(boolean sysLog) {
        this.sysLog = sysLog;
    }

    public String getFsUrl() {
        return fsUrl;
    }

    public void setFsUrl(String fsUrl) {
        this.fsUrl = fsUrl;
    }

    public String getFsAppid() {
        return fsAppid;
    }

    public void setFsAppid(String fsAppid) {
        this.fsAppid = fsAppid;
    }

    public String getFsUsername() {
        return fsUsername;
    }

    public void setFsUsername(String fsUsername) {
        this.fsUsername = fsUsername;
    }

    public String getFsGroupid() {
        return fsGroupid;
    }

    public void setFsGroupid(String fsGroupid) {
        this.fsGroupid = fsGroupid;
    }

    public int getRedisCacheExpiredMin() {
        return redisCacheExpiredMin;
    }

    public void setRedisCacheExpiredMin(int redisCacheExpiredMin) {
        this.redisCacheExpiredMin = redisCacheExpiredMin;
    }

    public boolean isOperateLog() {
        return operateLog;
    }

    public void setOperateLog(boolean operateLog) {
        this.operateLog = operateLog;
    }

    public boolean isOperateLogAll() {
        return operateLogAll;
    }

    public void setOperateLogAll(boolean operateLogAll) {
        this.operateLogAll = operateLogAll;
    }

    /**
     * redis 过期时间
     */
    private int redisCacheExpiredMin = 30;
}
