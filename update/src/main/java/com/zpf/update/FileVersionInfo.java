package com.zpf.update;

import org.json.JSONObject;

/**
 * @author Created by ZPF on 2021/3/29.
 */
public class FileVersionInfo {
    private String groupId;
    String fileName;
    int versionCode;
    String versionName;
    String downloadPath;
    String localPath;
    String fileIcon;
    String md5Str;
    int strategy;//1--后台下载；2--提示下载；3--
    long checkTime;

    public FileVersionInfo() {
    }

    public FileVersionInfo(String name, String spString) {
        try {
            JSONObject versionInfo = new JSONObject(spString);
            groupId = versionInfo.optString("groupId");
            fileName = versionInfo.optString("fileName");
            versionCode = versionInfo.optInt("versionCode");
            versionName = versionInfo.optString("versionName");
            md5Str = versionInfo.optString("md5Str");
            downloadPath = versionInfo.optString("downloadPath");
            localPath = versionInfo.optString("localPath");
            fileIcon = versionInfo.optString("fileIcon");
            strategy = versionInfo.optInt("strategy");
            checkTime = versionInfo.optLong("checkTime");
        } catch (Exception e) {
            //
        }
        if (fileName == null || fileName.length() == 0) {
            fileName = name;
        }
    }

    public String getFileName() {
        return fileName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public String getDownloadPath() {
        return downloadPath;
    }

    public String getLocalPath() {
        return localPath;
    }

    public String getFileIcon() {
        return fileIcon;
    }

    public String getMd5Str() {
        return md5Str;
    }

    public int getStrategy() {
        return strategy;
    }

    public String getGroupId() {
        if (groupId == null || groupId.length() == 0) {
            return fileName;
        }
        return groupId;
    }

    @Override
    public String toString() {
        return "{" +
                "groupId='" + groupId + '\'' +
                ", fileName='" + fileName + '\'' +
                ", versionCode=" + versionCode +
                ", versionName='" + versionName + '\'' +
                ", downloadPath='" + downloadPath + '\'' +
                ", localPath='" + localPath + '\'' +
                ", fileIcon='" + fileIcon + '\'' +
                ", md5Str='" + md5Str + '\'' +
                ", strategy=" + strategy +
                ", checkTime=" + checkTime +
                '}';
    }
}
