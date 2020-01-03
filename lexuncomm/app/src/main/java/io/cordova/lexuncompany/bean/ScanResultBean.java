package io.cordova.lexuncompany.bean;

/**
 * Author：Libin on 2019/12/30 0030 16:50
 * Email：1993911441@qq.com
 * Describe：
 */
public class ScanResultBean {
    private String callback;
    private String scanResult;


    public ScanResultBean(String callback, String scanResult) {
        this.scanResult = scanResult;
        this.callback = callback;
    }

    public String getScanResult() {
        return scanResult;
    }

    public void setScanResult(String scanResult) {
        this.scanResult = scanResult;
    }


    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }
}
