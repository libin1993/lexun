package io.cordova.lexuncompany.bean.base;

import android.Manifest;
import android.content.Context;

import io.cordova.lexuncompany.application.MyApplication;

/**
 * Created by JasonYao on 2018/3/5.
 */

public class App {

    public static final String[] mPermissionList = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_LOGS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.SET_DEBUG_APP,
            Manifest.permission.SYSTEM_ALERT_WINDOW,
            Manifest.permission.GET_ACCOUNTS,
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
            Manifest.permission.WRITE_APN_SETTINGS,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA
    };

    public static final String[] pictureSelect = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    /**
     * 缓存数据库
     */
    public static class DB {
        public static final int version = 1;  //数据库版本号
        public static final String dbName = "db_data.db";  //数据库名称
    }

    public static class BaseDB {
        public static final int version = 1; //数据库版本号
        public static final String dbName = "db_base.db"; //数据库名称
    }

    public static class LexunCard {
        //研发环境
//        public static final String CardUrl = "http://183.129.130.119:12010/";
//        public static final String BUGLY_APPID ="b6722d0470";  //bugly


        //生产环境
        public static final String CardUrl = "http://lexun.yikahui.net/";
        public static final String BUGLY_APPID = "026a35dd56";  // bugly


        //测试环境
//        public static final String CardUrl = "http://183.129.130.119:13131/";
//        public static final String BUGLY_APPID = "cc5b9c4bdc";  // bugly


    }
}

