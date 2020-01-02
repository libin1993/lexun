package io.cordova.lexuncompany.units;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.facebook.stetho.common.LogUtil;
import com.tencent.bugly.beta.Beta;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import exocr.exocrengine.CaptureActivity;
import io.cordova.lexuncompany.R;
import io.cordova.lexuncompany.application.MyApplication;
import io.cordova.lexuncompany.bean.base.Request;
import io.cordova.lexuncompany.inter.QrCodeScanInter;
import io.cordova.lexuncompany.view.CardContentActivity;
import io.cordova.lexuncompany.inter.CityPickerResultListener;
import io.cordova.lexuncompany.view.ScanQRCodeActivity;

import static cn.bertsir.zbar.QrConfig.REQUEST_CAMERA;


/**
 * Created by JasonYao on 2018/9/3
 */
public class AndroidtoJS implements QrCodeScanInter, CityPickerResultListener {
    private static final String TAG = "libin";

    //    //巡逻相关
//    private LocationClient mBaiduLocationClient;
//    private LocationClientOption mBaiduOption;
//    //百度定位相关
//    private LocationClient mBaiduLocationClient1;
//    private LocationClientOption mBaiduOption1;

    //高德巡逻相关
    private AMapLocationClient mLocationClient;
    //高德定位相关
    private AMapLocationClient mLocationClient1;

    private AndroidToJSCallBack mCallBack;

    private CardContentActivity mContext;



    public AndroidtoJS(Context context,AndroidToJSCallBack callBack) {
        mContext = (CardContentActivity) context;
        this.mCallBack = callBack;
    }


//    private LocationClient getBaiduLocationClient1(String callBack) {
//        if (mBaiduOption1 == null) mBaiduOption1 = new LocationClientOption();
//        if (mBaiduLocationClient1 == null)
//            mBaiduLocationClient1 = new LocationClient(MyApplication.getInstance());
//        mBaiduOption1.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
//        mBaiduOption1.setCoorType("bd09ll");
//        mBaiduOption1.setIgnoreKillProcess(false);
//        mBaiduOption1.setScanSpan(0);
//
//        mBaiduOption1.setOpenGps(true);
//        mBaiduLocationClient1.setLocOption(mBaiduOption1);
//        mBaiduLocationClient1.registerLocationListener(new BDAbstractLocationListener() {
//            @Override
//            public void onReceiveLocation(BDLocation bdLocation) {
//
//                sendCallBack(callBack, "200", "success", bdLocation.getLatitude() + "," + bdLocation.getLongitude());
//
//                mBaiduLocationClient1.stop();
//                mBaiduOption1 = null;
//                mBaiduLocationClient1 = null;
//
//            }
//        });
//        return mBaiduLocationClient1;
//    }

    @JavascriptInterface
    public void hello(Map<String, String> object) {

    }

    /**
     * 获取设备唯一标识码
     *
     * @return
     */
    @JavascriptInterface
    public void getDeviceId(String callBack) {
        Log.d(TAG, "getDeviceId: "+callBack);
        sendCallBack(callBack, "200", "success", BaseUnits.getInstance().getPhoneKey());
    }

    /**
     * 获取设备硬件信息
     *
     * @param callBack
     */
    @JavascriptInterface
    public void getPhoneInfo(String callBack) {
        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            data.put("mac", MacUtils.getMobileMAC(MyApplication.getInstance()));
            data.put("imei", BaseUnits.getInstance().getIMEI());
            data.put("imsi", BaseUnits.getInstance().getIMSI());
            data.put("phone", BaseUnits.getInstance().getTel());
            jsonObject.put("status", "200");
            jsonObject.put("msg", "success");
            jsonObject.put("data", data);
            Log.e(TAG, String.valueOf(jsonObject));
            mCallBack.callBackResult(callBack, jsonObject.toString());
        } catch (JSONException e) {
            mCallBack.callBackResult(callBack, "未知错误，联系管理员");
            e.printStackTrace();
        }
    }

    /**
     * 获取用户UserId（在该项目中，userid使用Token标识）
     *
     * @return
     */
    @JavascriptInterface
    public void getUserId(String callBack) {
        sendCallBack(callBack, "200", "success", UserUnits.getInstance().getToken());
    }

    /**
     * 获取当前定位城市
     *
     * @return
     */
    @JavascriptInterface
    public void getLocation(String callBack) {
        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            data.put("cityName", UserUnits.getInstance().getLocation());
            data.put("cityCode", ConfigUnits.getInstance().getCityIdByName(UserUnits.getInstance().getLocation()));
            jsonObject.put("status", "200");
            jsonObject.put("msg", "success");
            jsonObject.put("data", data);
            Log.e(TAG, String.valueOf(jsonObject));
            mCallBack.callBackResult(callBack, jsonObject.toString());
        } catch (JSONException e) {
            mCallBack.callBackResult(callBack, "未知错误，联系管理员");
            e.printStackTrace();
        }
    }

    /**
     * 获取版本号
     *
     * @return
     */
    @JavascriptInterface
    public void getVersionNO(String callBack) {
        sendCallBack(callBack, "200", "success", "v." + BaseUnits.getInstance().getVerName(MyApplication.getInstance()));
    }

    /**
     * 版本号
     */
    @JavascriptInterface
    public void versionUpdate() {
        Beta.checkUpgrade();
    }

    /**
     * 扫描二维码
     *
     * @return
     */
    @JavascriptInterface
    public void qrScan(String callBack) {
        Intent intent = new Intent(MyApplication.getInstance().getBaseContext(), ScanQRCodeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("callBack", callBack);
        ScanQRCodeActivity.setQrCodeScanInter(AndroidtoJS.this);
        MyApplication.getInstance().getBaseContext().startActivity(intent);
    }

    /**
     * 获取当前定位城市
     *
     * @param callBack
     */
    @JavascriptInterface
    public void getCurrentCity(String callBack) {
        Log.d(TAG, "getCurrentCity: " + callBack);
        if (TextUtils.isEmpty(UserUnits.getInstance().getSelectCity())) {
            sendCallBack(callBack, "200", "success", ConfigUnits.getInstance().getCityIdByName(UserUnits.getInstance().getSelectCity()));
        } else {
            sendCallBack(callBack, "500", "fail", "");
        }

    }

    @JavascriptInterface
    public void getUserInfo(String callBack) {
        Log.e(TAG, "getUserInfo");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("clientId", BaseUnits.getInstance().getPhoneKey());
            jsonObject.put("token", UserUnits.getInstance().getToken());
            jsonObject.put("loginStatus", FormatUtils.getIntances().isEmpty(UserUnits.getInstance().getToken()) ? "0" : "1");
            jsonObject.put("phone", UserUnits.getInstance().getPhone());
            jsonObject.put("realName", UserUnits.getInstance().getRealName());
            jsonObject.put("status", UserUnits.getInstance().getStatus());
            jsonObject.put("headImgPath", UserUnits.getInstance().getHeadImgPath());
            jsonObject.put("nickName", UserUnits.getInstance().getNickName());
            jsonObject.put("sex", UserUnits.getInstance().getSex());
            jsonObject.put("birthDay", UserUnits.getInstance().getBirthDay());
            jsonObject.put("address", UserUnits.getInstance().getAddress());
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }

        Log.e(TAG, jsonObject.toString());

        sendCallBackJson(callBack, "200", "success", jsonObject);
    }

    /**
     * 拨打电话
     *
     * @param number
     */
    @JavascriptInterface
    public void call(String number) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MyApplication.getInstance().startActivity(intent);
    }

    /**
     * 退出页面
     */
    @JavascriptInterface
    public void finish() {
        if (CardContentActivity.getInstance() != null) {
            CardContentActivity.getInstance().finish();
        }
    }


    /**
     * 是否有定位权限
     *
     * @param value
     */
    @JavascriptInterface
    public void locationServicesEnabled(String value) {
        if (ActivityCompat.checkSelfPermission(CardContentActivity.getInstance(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("code", "001");
                jsonObject.put("msg", "没有定位权限");

                sendCallBackJson(value, "500", "error", jsonObject);

                return;
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return;
        }


        LocationManager locationManager = (LocationManager) CardContentActivity.getInstance().getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("code", "002");
                jsonObject.put("msg", "GPS未开启");
                sendCallBackJson(value, "500", "error", jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return;
        }

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code", "000");
            jsonObject.put("msg", "success");
            sendCallBackJson(value, "200", "success", jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取高德定位点
     *
     * @return
     */
    @JavascriptInterface
    public void getBaiduCoordinate(String callBack) {
        Log.d(TAG, "高德定位: "+callBack);
        if (mLocationClient1 == null) {
            mLocationClient1 = new AMapLocationClient(MyApplication.getInstance());
            AMapLocationClientOption mLocationOption = new AMapLocationClientOption();

            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mLocationOption.setInterval(0);
            // 地址信息
            mLocationOption.setNeedAddress(true);
            //获取速度
            mLocationOption.setSensorEnable(true);
            mLocationClient1.setLocationOption(mLocationOption);

            mLocationClient1.setLocationListener(new AMapLocationListener() {
                @Override
                public void onLocationChanged(AMapLocation aMapLocation) {
                    if (aMapLocation != null && aMapLocation.getLatitude() != 0 && aMapLocation.getLongitude() != 0) {
                        Log.d(TAG, "定位: " + aMapLocation.getLatitude() + "," + aMapLocation.getLongitude());
                        sendCallBack(callBack, "200", "success", aMapLocation.getLatitude() + "," + aMapLocation.getLongitude());
                        if (mLocationClient1 != null) {
                            mLocationClient1.stopLocation();
                            mLocationClient1 = null;
                        }
                    }

                }


            });
            mLocationClient1.startLocation();
        }


    }
//    /**
//     * 获取百度定位点
//     *
//     * @return
//     */
//    @JavascriptInterface
//    public void getBaiduCoordinate(String callBack) {
//        getBaiduLocationClient1(callBack).start();
//    }

    /**
     * 设置标题栏
     *
     * @param titleBar 0：黑色返回按钮，1：白色返回按钮，2：灰色返回按钮
     */
    @JavascriptInterface
    public void setTitleBar(String titleBar) {
        try {
            JSONObject jsonObject = new JSONObject(titleBar);
            Log.e(TAG, jsonObject.toString());
            String btnBackKey = jsonObject.getString("btnBackKey");
            String title = jsonObject.getString("tit");
            String textColor = jsonObject.getString("textColor");
            String bgColor = jsonObject.getString("bgColor");
            String isDisplay = jsonObject.getString("isShow");  //0:不显示  1:显示
            Log.e(TAG, "设置标题栏：" + btnBackKey + "," + title + "," + textColor + "," + bgColor);
            mCallBack.setTitleBar(btnBackKey, title, FormatUtils.getIntances().colorTo6Color(textColor), FormatUtils.getIntances().colorTo6Color(bgColor), isDisplay);
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
    }


    /**
     * 开始巡逻（高德坐标系统）
     */
    @JavascriptInterface
    public void beginPatrol(String callBack) {
        Log.d(TAG, "beginPatrol2: " + callBack);
        if (mLocationClient == null) {
            Log.d(TAG, "巡逻:13");
            mLocationClient = new AMapLocationClient(MyApplication.getInstance());
            AMapLocationClientOption mLocationOption = new AMapLocationClientOption();

            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mLocationOption.setInterval(10000);
            mLocationOption.setLocationCacheEnable(false);
            mLocationClient.setLocationOption(mLocationOption);
            mLocationClient.startLocation();
            //启动后台定位，第一个参数为通知栏ID，建议整个APP使用一个
            mLocationClient.enableBackgroundLocation(2001, mContext.buildNotification());
            mLocationClient.setLocationListener(new AMapLocationListener() {
                @Override
                public void onLocationChanged(AMapLocation aMapLocation) {
                    if (aMapLocation != null && aMapLocation.getLatitude() != 0 && aMapLocation.getLongitude() != 0) {

                        sendCallBack(callBack, "200", "success", aMapLocation.getLatitude() + "," + aMapLocation.getLongitude());
                        Log.d(TAG, "onLocationChanged: "+aMapLocation.getLatitude() + "," + aMapLocation.getLongitude());
                    }

                }
            });

        } else if (!mLocationClient.isStarted()) {
            mLocationClient.startLocation();
        }


    }




    /**
     * 结束巡逻（高德坐标系统）
     */
    @JavascriptInterface
    public void endPatrol() {
        Log.d(TAG, "endPatrol: ");
        if (mLocationClient != null) {
            //关闭后台定位，参数为true时会移除通知栏，为false时不会移除通知栏，但是可以手动移除
            mLocationClient.disableBackgroundLocation(true);
            mLocationClient.stopLocation();
            mLocationClient = null;
        }
    }

//
//    @JavascriptInterface
//    public void beginPatrol(String callBack) {
//
//        if (mBaiduLocationClient == null) {
//
//            mBaiduLocationClient = new LocationClient(MyApplication.getInstance());
//
//            mBaiduOption = new LocationClientOption();
//
//            mBaiduOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
//            mBaiduOption.setCoorType("bd09ll");
//
//            mBaiduOption.setIgnoreKillProcess(false);
//            mBaiduOption.setScanSpan(7000);
//
//            mBaiduOption.setOpenGps(true);
//            mBaiduLocationClient.setLocOption(mBaiduOption);
//            mBaiduLocationClient.start();
//            mBaiduLocationClient.registerLocationListener(new BDAbstractLocationListener() {
//                @Override
//                public void onReceiveLocation(BDLocation bdLocation) {
//
//                    sendCallBack(callBack, "200", "success", bdLocation.getLatitude() + "," + bdLocation.getLongitude());
//                }
//            });
//        } else if (!mBaiduLocationClient.isStarted()) {
//            mBaiduLocationClient.start();
//        }
//
//    }
//
//
//
//    /**
//     * 结束巡逻（百度坐标系统）
//     */
//    @JavascriptInterface
//    public void endPatrol() {
//        if (mBaiduLocationClient != null) {
//            mBaiduLocationClient.stop();
//        }
//
//        mBaiduOption = null;
//    }

    /**
     * 获取照片（拍照、相册选择）
     *
     * @param callBack
     */
    @JavascriptInterface
    public void getPhoto(String callBack) {
        CardContentActivity.getInstance().takePhoto(imageData -> {
            Log.e(TAG, imageData);
            sendCallBack(callBack, "200", "success", "data:image/jpeg;base64," + imageData);
        });

    }

    /**
     * 获取推荐码
     *
     * @return
     */
    @JavascriptInterface
    public void getLexunReferralCode(String callBack) {
        Log.e(TAG, "推广码为：" + ConfigUnits.getInstance().getLexunReferralCode());
        sendCallBack(callBack, "200", "success", ConfigUnits.getInstance().getLexunReferralCode());
        ConfigUnits.getInstance().setLexunReferralCode("");
    }

    /**
     * 打开相册
     *
     * @param callBack
     */
    @JavascriptInterface
    public void OpenGallery(String callBack) {
        CardContentActivity.getInstance().openGallery(imageData -> {
            Log.e(TAG, imageData);
            sendCallBack(callBack, "200", "success", "data:image/jpeg;base64," + imageData);
        });
    }

    /**
     * 打开相机
     *
     * @param callBack
     */
    @JavascriptInterface
    public void OpenTheCamera(String callBack) {
        CardContentActivity.getInstance().openTheCamera(imageData -> {
            Log.e(TAG, imageData);
            sendCallBack(callBack, "200", "success", "data:image/jpeg;base64," + imageData);
        });
    }


    /**
     * 身份证正面
     *
     * @param value
     */
    @JavascriptInterface
    public void IDCard_front(String value) {
        if (BaseUnits.getInstance().checkPermission(CardContentActivity.getInstance(), Manifest.permission.CAMERA)) {
            Intent intent = new Intent(CardContentActivity.getInstance(), CaptureActivity.class);

            intent.putExtra("is_front", true);
            intent.putExtra("callback", value);
            CardContentActivity.getInstance().startActivityForResult(intent, Request.StartActivityRspCode.SCAN_ID_CARD);
        } else {
            ActivityCompat.requestPermissions(CardContentActivity.getInstance(),
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        }


    }


    /**
     * 身份证反面
     *
     * @param value
     */
    @JavascriptInterface
    public void IDCard_reverseSide(String value) {
        if (BaseUnits.getInstance().checkPermission(CardContentActivity.getInstance(), Manifest.permission.CAMERA)) {
            Intent intent = new Intent(CardContentActivity.getInstance(), CaptureActivity.class);
            intent.putExtra("is_front", false);
            intent.putExtra("callback", value);
            CardContentActivity.getInstance().startActivityForResult(intent, Request.StartActivityRspCode.SCAN_ID_CARD);
        } else {
            ActivityCompat.requestPermissions(CardContentActivity.getInstance(),
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        }
    }

    /**
     * 语音合成
     *
     * @param value
     */
    @JavascriptInterface
    public void textToSpeech(String value) {
        try {
            JSONObject jsonObject = new JSONObject(value);
            SpeechCompoundUnits.getInstance().speakText(jsonObject.getString("value"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    public void sendCallBack(String callBack, String status, String msg, String value) {
        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            data.put("value", value);
            jsonObject.put("status", status);
            jsonObject.put("msg", msg);
            jsonObject.put("data", data);
            mCallBack.callBackResult(callBack, jsonObject.toString());
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            mCallBack.callBackResult(callBack, "未知错误，联系管理员");
            e.printStackTrace();
        }
    }

    public void sendCallBackJson(String callBack, String status, String msg, JSONObject value) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("status", status);
            jsonObject.put("msg", msg);
            jsonObject.put("data", value);
            Log.e(TAG, String.valueOf(jsonObject));
            mCallBack.callBackResult(callBack, jsonObject.toString());
        } catch (JSONException e) {
            mCallBack.callBackResult(callBack, "未知错误，联系管理员");
            e.printStackTrace();
        }
    }

    @Override
    public void getQrCodeScanResult(String result) {

    }

    @Override
    public void getQrCodeScanResult(String callBack, String result) {
        Log.e(TAG, result);
        sendCallBack(callBack, "200", "success", result);
    }

    @Override
    public void getCityPickerResultListener(String callBack, JSONObject result) {
        sendCallBackJson(callBack, "200", "success", result);
    }
}

