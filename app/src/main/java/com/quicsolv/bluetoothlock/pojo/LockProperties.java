package com.quicsolv.bluetoothlock.pojo;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class LockProperties implements Serializable {

    private String account;
    private String barcode;
    private String deviceId;
    private String electricity;
    private String firmwareVersion;
    private String gsmVersion;
    private Integer id;
    private Integer isAdmin;
    private String lockKey;
    private String lockPwd;
    private String mac;
    private String name;
    private String nickName;
    private Integer pId;
    private String remakeName;
    private Integer uId;
    private String updateAt;
    private String useLock;
    private String useLockName;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getElectricity() {
        return electricity;
    }

    public void setElectricity(String electricity) {
        this.electricity = electricity;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getGsmVersion() {
        return gsmVersion;
    }

    public void setGsmVersion(String gsmVersion) {
        this.gsmVersion = gsmVersion;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Integer isAdmin) {
        this.isAdmin = isAdmin;
    }

    public String getLockKey() {
        return lockKey;
    }

    public void setLockKey(String lockKey) {
        this.lockKey = lockKey;
    }

    public String getLockPwd() {
        return lockPwd;
    }

    public void setLockPwd(String lockPwd) {
        this.lockPwd = lockPwd;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Integer getPId() {
        return pId;
    }

    public void setPId(Integer pId) {
        this.pId = pId;
    }

    public String getRemakeName() {
        return remakeName;
    }

    public void setRemakeName(String remakeName) {
        this.remakeName = remakeName;
    }

    public Integer getUId() {
        return uId;
    }

    public void setUId(Integer uId) {
        this.uId = uId;
    }

    public String getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(String updateAt) {
        this.updateAt = updateAt;
    }

    public String getUseLock() {
        return useLock;
    }

    public void setUseLock(String useLock) {
        this.useLock = useLock;
    }

    public String getUseLockName() {
        return useLockName;
    }

    public void setUseLockName(String useLockName) {
        this.useLockName = useLockName;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}