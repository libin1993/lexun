package io.cordova.lexuncompany.view;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;


import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.UpgradeInfo;
import com.tencent.bugly.beta.ui.UILifecycleListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

import cn.jpush.android.api.JPushInterface;
import io.cordova.lexuncompany.R;
import io.cordova.lexuncompany.bean.IDCardBean;
import io.cordova.lexuncompany.bean.ScanResultBean;
import io.cordova.lexuncompany.bean.base.App;
import io.cordova.lexuncompany.bean.base.Request;
import io.cordova.lexuncompany.databinding.ActivityCardContentBinding;
import io.cordova.lexuncompany.inter.GetImageListener;
import io.cordova.lexuncompany.inter.TakePicOnClick;
import io.cordova.lexuncompany.units.AndroidBug5497Workaround;
import io.cordova.lexuncompany.units.AndroidToJSCallBack;
import io.cordova.lexuncompany.units.AndroidtoJS;
import io.cordova.lexuncompany.units.Base64;
import io.cordova.lexuncompany.units.BaseUnits;
import io.cordova.lexuncompany.units.ConfigUnits;
import io.cordova.lexuncompany.units.FormatUtils;
import io.cordova.lexuncompany.units.ImageUtils;
import io.cordova.lexuncompany.units.LogUtils;
import io.cordova.lexuncompany.units.PermissionUtils;

import static io.cordova.lexuncompany.bean.base.Request.Permissions.REQUEST_ALL_PERMISSIONS;


/**
 * 卡详情页面
 * Created by JasonYao on 2018/4/3.
 */

public class CardContentActivity extends BaseActivity implements AndroidToJSCallBack {
    private static final String TAG = "libin";
    private ActivityCardContentBinding mBinding;

    private GetImageListener mListener; //获取图片监听类

    private static boolean isFirstLoaded = true;  //标记是否为第一次加载
    private IntentFilter mFilter = new IntentFilter();

    //开启gps
    private AlertDialog alertDialog;

    private AndroidtoJS androidtoJS;

    private static final String NOTIFICATION_CHANNEL_NAME = "BackgroundLocation";
    public NotificationManager notificationManager = null;
    boolean isCreateChannel = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_card_content);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        AndroidBug5497Workaround.assistActivity(mBinding.getRoot());  //解决在沉浸式菜单栏中，软键盘不能顶起页面的bug
        date();  //每次打开APP都获取剪切板数据,检查是否有推广码

        mFilter.addAction(Request.Broadcast.RELOADURL);
        this.registerReceiver(mBroadcastReceiver, mFilter);

        initView();

        getAllPermission();

        setListener();

        checkUpdate();

    }

    /**
     * @return 定位前台通知
     */
    public Notification buildNotification() {
        Notification.Builder builder = null;
        Notification notification = null;
        if(android.os.Build.VERSION.SDK_INT >= 26) {
            //Android O上对Notification进行了修改，如果设置的targetSDKVersion>=26建议使用此种方式创建通知栏
            if (null == notificationManager) {
                notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            }
            String channelId = getPackageName();
            if(!isCreateChannel) {
                NotificationChannel notificationChannel = new NotificationChannel(channelId,
                        NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.enableLights(true);//是否在桌面icon右上角展示小圆点
                notificationChannel.setLightColor(Color.BLUE); //小圆点颜色
                notificationChannel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知
                notificationChannel.enableVibration(false);
                notificationChannel.setVibrationPattern(new long[]{0});
                notificationChannel.setSound(null, null);
                notificationManager.createNotificationChannel(notificationChannel);
                isCreateChannel = true;
            }
            builder = new Notification.Builder(getApplicationContext(), channelId);
        } else {
            builder = new Notification.Builder(getApplicationContext());
        }
        builder.setSmallIcon(R.mipmap.logo)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("正在后台运行")
                .setVibrate(new long[]{0})
                .setSound(null)
                .setWhen(System.currentTimeMillis());

        notification = builder.build();
        return notification;

    }

    /**
     * ji
     */
    private void checkUpdate() {
        Beta.upgradeDialogLayoutId = R.layout.layout_update_popup;
        Beta.strNetworkTipsCancelBtn = "";
        Beta.strUpgradeDialogCancelBtn = "     ";
        Beta.initDelay = 2 * 1000;
        Beta.canShowUpgradeActs.add(CardContentActivity.class);

        Beta.upgradeDialogLifecycleListener = new UILifecycleListener<UpgradeInfo>() {
            @Override
            public void onCreate(Context context, View view, UpgradeInfo upgradeInfo) {
                Log.d("libin", "onResume111" + upgradeInfo.upgradeType);
                // 注：可通过这个回调方式获取布局的控件，如果设置了id，可通过findViewById方式获取，如果设置了tag，可以通过findViewWithTag，具体参考下面例子:

                // 通过id方式获取控件

                ImageView ivCancel = (ImageView) view.findViewById(R.id.iv_cancel_update);

                // 更多的操作：比如设置控件的点击事件
                ivCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (upgradeInfo.upgradeType == 2) {
                            finish();
                            System.exit(0);
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    }
                });
            }

            @Override
            public void onStart(Context context, View view, UpgradeInfo upgradeInfo) {

            }

            @Override
            public void onResume(Context context, View view, UpgradeInfo upgradeInfo) {


            }

            @Override
            public void onPause(Context context, View view, UpgradeInfo upgradeInfo) {

            }

            @Override
            public void onStop(Context context, View view, UpgradeInfo upgradeInfo) {

            }

            @Override
            public void onDestroy(Context context, View view, UpgradeInfo upgradeInfo) {

            }

        };

        Bugly.init(getApplicationContext(), App.LexunCard.BUGLY_APPID, false);
    }



    /**
     * 二维码扫描回调
     *
     * @param scanResultBean
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void scanResult(ScanResultBean scanResultBean) {
        sendCallback(scanResultBean.getCallback(),"200","success",scanResultBean.getScanResult());
    }



    /**
     * @param callback
     * @param status
     * @param msg
     * @param value
     */
    private void sendCallback(String callback, String status, String msg, String value) {

        try {
            JSONObject jsonObject = new JSONObject();
            JSONObject data = new JSONObject();
            data.put("value", value);
            jsonObject.put("status", status);
            jsonObject.put("msg", msg);
            jsonObject.put("data", data);
            LogUtils.log(jsonObject.toString());
            callBackResult(callback, jsonObject.toString());
        } catch (JSONException e) {
            LogUtils.log( e.toString());
            callBackResult(callback, "未知错误，联系管理员");
            e.printStackTrace();
        }
    }



    /**
     * 是否开启gps
     */
    private void isOpenGps() {
        if (!ConfigUnits.getInstance().isOpenGps()) {
            Log.d(TAG, "gps未开启");
            if (alertDialog != null) {
                alertDialog.show();
                return;
            }
            alertDialog = new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("该应用需要开启GPS定位服务，请检查是否开启并选择高精确度模式")
                    .setIcon(R.mipmap.logo)
                    .setPositiveButton("去开启", (dialogInterface, i1) -> openGps())
                    .setNegativeButton("取消", (dialogInterface, i12) -> cancelGps()).create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setCancelable(false);
            alertDialog.show();
        } else {
            Log.d(TAG, "gps开启");
            if (alertDialog != null && alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
        }
    }

    /**
     * 下拉状态栏开启gps,点击取消按钮作判断
     */
    public void cancelGps() {
        if (ConfigUnits.getInstance().isOpenGps()) {
            if (alertDialog != null && alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
        } else {
            finish();
        }
    }

    /**
     * 开启gps
     */
    private void openGps() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(settingsIntent);
    }


    @Override
    protected void onResume() {
        super.onResume();
        JPushInterface.setAlias(this, new Random().nextInt(900) + 100, BaseUnits.getInstance().getPhoneKey());
        isOpenGps();

    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String url = intent.getStringExtra("url");
        if (!TextUtils.isEmpty(url)) {
            mBinding.webView.loadUrl(url);
        }
    }


    public void initView() {
        WebSettings webSettings = mBinding.webView.getSettings();
        webSettings.setJavaScriptEnabled(true);  //设置与JS交互权限
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //设置运行JS弹窗
        webSettings.setUserAgentString(webSettings.getUserAgentString() + "-Android");  //设置用户代理
        webSettings.setDomStorageEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        //启用地理定位
        webSettings.setGeolocationEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        webSettings.setUseWideViewPort(true);
        webSettings.setMediaPlaybackRequiresUserGesture(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        mBinding.webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (isFirstLoaded) {
                    mBinding.layoutLoading.setVisibility(View.VISIBLE);
                    if (newProgress >= 100) {
                        isFirstLoaded = false;
                        mBinding.layoutLoading.startAnimation(AnimationUtils.loadAnimation(CardContentActivity.this, R.anim.layout_card_loading_close));
                        mBinding.layoutLoading.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
                super.onGeolocationPermissionsShowPrompt(origin, callback);
            }

        });

        mBinding.webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d("libin", "shouldOverrideUrlLoading: "+url);
                if (url == null) return false;
                if (url.startsWith("http:") || url.startsWith("https:")) {
                    view.loadUrl(url);
                } else {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    } catch (Exception e) {
                        return true;
                    }
                }
                return true;
            }

        });

        androidtoJS = new AndroidtoJS(this,this);
        mBinding.webView.addJavascriptInterface(androidtoJS, "NativeForJSUnits");
        mBinding.webView.loadUrl(App.LexunCard.CardUrl);

    }

    private void setListener() {
        mBinding.imgBtnBack.setOnClickListener(view -> {
            if (mBinding.webView.canGoBack()) {
                mBinding.webView.goBack();
            } else {
                finish();
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (mBinding.webView.canGoBack()) {
            mBinding.webView.goBack();
        } else {
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.addCategory(Intent.CATEGORY_HOME);
            startActivity(home);
        }
    }

    /**
     * Android6.0动态获取所有权限
     */
    private void getAllPermission() {
        ActivityCompat.requestPermissions(this, App.mPermissionList, REQUEST_ALL_PERMISSIONS);
    }

    /**
     * @param view 退出app
     */
    public void closeApp(View view) {
        if (mBinding.webView.canGoBack()) {
            mBinding.webView.goBack();
        } else {
            finish();
        }
    }


    /**
     * 拍照或选择照片
     */
    public void takePhoto(GetImageListener listener) {
        mListener = listener;
        if (PermissionUtils.getInstance().hasPermission(this, App.pictureSelect)) {
            PictureSelector.create(this)
                    .openGallery(PictureMimeType.ofImage())
                    .selectionMode(PictureConfig.SINGLE)
                    .isCamera(true)
                    .enableCrop(true)// 是否裁剪 true or false
                    .compress(true)// 是否压缩 true or false
                    .previewImage(false)
                    .withAspectRatio(1, 1)// int 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                    .freeStyleCropEnabled(true)// 裁剪框是否可拖拽 true or false
                    .circleDimmedLayer(false)// 是否圆形裁剪 true or false
                    .showCropGrid(false)
                    .minimumCompressSize(100)// 小于100kb的图片不压缩
                    .scaleEnabled(true)// 裁剪是否可放大缩小图片 true or false
                    .isDragFrame(true)// 是否可拖动裁剪框(固定)
                    .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code
        } else {
            ActivityCompat.requestPermissions(this, App.pictureSelect, Request.Permissions.REQUEST_CAMERA);
        }
    }

    public void openGallery(GetImageListener listener) {
        mListener = listener;
        PictureSelector.create(this)
                .openGallery(PictureMimeType.ofImage())
                .selectionMode(PictureConfig.SINGLE)
                .previewImage(false)
                .isCamera(false)
                .enableCrop(true)// 是否裁剪 true or false
                .compress(true)// 是否压缩 true or false
                .withAspectRatio(1, 1)// int 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                .freeStyleCropEnabled(true)// 裁剪框是否可拖拽 true or false
                .circleDimmedLayer(false)// 是否圆形裁剪 true or false
                .showCropGrid(false)
                .minimumCompressSize(100)// 小于100kb的图片不压缩
                .scaleEnabled(true)// 裁剪是否可放大缩小图片 true or false
                .isDragFrame(true)// 是否可拖动裁剪框(固定)
                .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code
    }

    public void openTheCamera(GetImageListener listener) {
        mListener = listener;
        if (PermissionUtils.getInstance().hasPermission(this, App.pictureSelect)) {
            PictureSelector.create(this)
                    .openCamera(PictureMimeType.ofImage())
                    .selectionMode(PictureConfig.SINGLE)
                    .previewImage(false)
                    .enableCrop(true)// 是否裁剪 true or false
                    .compress(true)// 是否压缩 true or false
                    .withAspectRatio(1, 1)// int 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                    .freeStyleCropEnabled(true)// 裁剪框是否可拖拽 true or false
                    .circleDimmedLayer(false)// 是否圆形裁剪 true or false
                    .showCropGrid(false)
                    .minimumCompressSize(100)// 小于100kb的图片不压缩
                    .scaleEnabled(true)// 裁剪是否可放大缩小图片 true or false
                    .isDragFrame(true)// 是否可拖动裁剪框(固定)
                    .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code
        } else {
            ActivityCompat.requestPermissions(this, App.pictureSelect, Request.Permissions.REQUEST_CAMERA);
        }
    }

    @Override
    public void callBackResult(String callBack, String value) {
        if (FormatUtils.getIntances().isEmpty(callBack)) {
            return;
        }

        Log.d(TAG, callBack + ":" + value);

        runOnUiThread(() -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mBinding.webView.post(() -> mBinding.webView.evaluateJavascript("javascript:" + callBack + "('" + value + "')", s -> Log.e(TAG, s)));
            } else {
                mBinding.webView.post(() -> mBinding.webView.loadUrl("javascript:" + callBack + "('" + value + "')"));
            }
        });
    }

    @Override
    public void setTitleBar(String btnBackType, String title, String textColor, String bgColor, String isDisplay) {

    }


    @Override
    protected void onDestroy() {
        mInstance = null;
        mListener = null;
        mFilter = null;
        isFirstLoaded = true;
        unregisterReceiver(mBroadcastReceiver);
        destroyWebView();
        mBinding = null;
        EventBus.getDefault().unregister(this);

        super.onDestroy();

    }

    public void destroyWebView() {
        mBinding.webView.loadUrl("about:blank"); // clearView() should be changed to loadUrl("about:blank"), since clearView() is deprecated now
        mBinding.webView.onPause();
        mBinding.webView.clearCache(true);
        mBinding.webView.removeAllViews();
        mBinding.webView.destroy();
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Request.Broadcast.RELOADURL)) {
                String url = intent.getStringExtra("url");
                mBinding.webView.loadUrl(url);
            }
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == PictureConfig.CHOOSE_REQUEST) {
            //图片压缩成功
            String img = PictureSelector.obtainMultipleResult(data).get(0).getCompressPath();
            if (!TextUtils.isEmpty(img) && mListener != null) {
                mListener.getImage(Base64.encode(ImageUtils.getInstance().image2byte(img)));
            }

        } else if (resultCode == RESULT_OK && requestCode == Request.StartActivityRspCode.SCAN_ID_CARD) {

            try {
                IDCardBean idCardBean = (IDCardBean) data.getSerializableExtra("id_card");
                if (idCardBean != null) {
                    JSONObject jsonObject = new JSONObject();
                    JSONObject value = new JSONObject();
                    if (idCardBean.getOrientation() == 1) {
                        value.put("name", idCardBean.getName());
                        value.put("gender", idCardBean.getGender());
                        value.put("address", idCardBean.getAddress());
                        value.put("IDNum", idCardBean.getNumber());
                        value.put("nation", idCardBean.getNation());
                        value.put("frontimg",  Base64.encode(ImageUtils.
                                getInstance().image2byte(idCardBean.getPath())));
                    } else {
                        value.put("issue", idCardBean.getPolice());
                        value.put("valid", idCardBean.getDate());
                        value.put("backimg",  Base64.encode(ImageUtils.
                                getInstance().image2byte(idCardBean.getPath())));
                    }

                    jsonObject.put("status", "200");
                    jsonObject.put("msg", "success");
                    jsonObject.put("data", value);

                    callBackResult(idCardBean.getCallback(), jsonObject.toString());
                }

            } catch (JSONException e) {
                Log.e(TAG, e.toString());
                e.printStackTrace();
            }
        }
    }


    /**
     * 横竖屏切换
     *
     * @param requestedOrientation
     */
    @Override
    public void setRequestedOrientation(int requestedOrientation) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ALL_PERMISSIONS) {
            if (!BaseUnits.getInstance().checkPermission(this, Manifest.permission.READ_PHONE_STATE)) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
                        AlertDialog alertDialog = new AlertDialog.Builder(this)
                                .setTitle("提示")
                                .setMessage("未获得相应权限，无法正常使用APP，请前往安全中心>权限管理>乐巡，开启相关权限")
                                .setIcon(R.mipmap.logo)
                                .setPositiveButton("确定", (dialogInterface, i1) -> {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivityForResult(intent, REQUEST_ALL_PERMISSIONS);
                                })
                                .setNegativeButton("取消", (dialogInterface, i12) -> this.finish()).create();
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.setCancelable(false);
                        alertDialog.show();
                        return;
                    }
                }
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setTitle("提示")
                        .setMessage("未获得读取手机状态权限，无法正常使用APP，是否给予权限？")
                        .setIcon(R.mipmap.logo)
                        .setPositiveButton("是", (dialogInterface, i1) -> getAllPermission())
                        .setNegativeButton("否", (dialogInterface, i12) -> this.finish()).create();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.setCancelable(false);
                alertDialog.show();
            } else if (!BaseUnits.getInstance().checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        AlertDialog alertDialog = new AlertDialog.Builder(this)
                                .setTitle("提示")
                                .setMessage("未获得定位权限，无法正常使用APP，请前往安全中心>权限管理>智慧家园，开启相关权限")
                                .setIcon(R.mipmap.logo)
                                .setPositiveButton("确定", (dialogInterface, i1) -> {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivityForResult(intent, REQUEST_ALL_PERMISSIONS);
                                })
                                .setNegativeButton("取消", (dialogInterface, i12) -> this.finish()).create();
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.setCancelable(false);
                        alertDialog.show();
                        return;
                    }
                }
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setTitle("提示")
                        .setMessage("未获得定位权限，无法正常使用APP，是否给予权限？")
                        .setIcon(R.mipmap.logo)
                        .setPositiveButton("是", (dialogInterface, i1) -> getAllPermission())
                        .setNegativeButton("否", (dialogInterface, i12) -> this.finish()).create();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.setCancelable(false);
                alertDialog.show();
            }
        }
    }

    /**
     * 获取剪切板数据
     */
    private void date() {
        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData data = cm.getPrimaryClip();
        try {
            ClipData.Item item = data.getItemAt(0);
            String content = item.getText().toString();
            Log.e(TAG, content);
            if (content.split("=").length == 2 && content.split("=")[0].equals("lexunReferralCode")) {
                String mCode = content.split("=")[1];
                ConfigUnits.getInstance().setLexunReferralCode(mCode);
                Log.e(TAG, "推广码为1：" + mCode);
                Log.e(TAG, "推广码为2：" + ConfigUnits.getInstance().getLexunReferralCode());
                cm.setPrimaryClip(ClipData.newPlainText(null, ""));  //清空剪切板
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
}
