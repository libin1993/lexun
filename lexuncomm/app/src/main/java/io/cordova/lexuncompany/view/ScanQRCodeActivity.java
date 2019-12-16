package io.cordova.lexuncompany.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bertsir.zbar.CameraPreview;
import cn.bertsir.zbar.Qr.ScanResult;
import cn.bertsir.zbar.Qr.Symbol;
import cn.bertsir.zbar.QrConfig;
import cn.bertsir.zbar.ScanCallback;
import cn.bertsir.zbar.utils.QRUtils;
import cn.bertsir.zbar.view.ScanView;

import io.cordova.lexuncompany.R;
import io.cordova.lexuncompany.inter.QrCodeScanInter;
import io.cordova.lexuncompany.units.ViewUnits;


/**
 * Author：Libin on 2019/5/10 14:31
 * Email：1993911441@qq.com
 * Describe：
 */
public class ScanQRCodeActivity extends BaseActivity {
    @BindView(R.id.cp_scan)
    CameraPreview cpScan;
    @BindView(R.id.scan_view)
    ScanView scanView;
    @BindView(R.id.ll_scan_back)
    LinearLayout llScanBack;
    @BindView(R.id.tv_select_photo)
    TextView tvSelectPhoto;
    @BindView(R.id.cb_flash_light)
    CheckBox cbFlashLight;


    private static QrCodeScanInter mQrCodeScanInter;  //在AndroidForJSUnits中使用
    private String mCallBack;
    private String scanResult;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Symbol.is_only_scan_center = true;
        Symbol.scanType =  QrConfig.TYPE_CUSTOM;
        Symbol.doubleEngine = true;
        setContentView(R.layout.activity_scan_qrcode);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        mCallBack = super.getIntent().getStringExtra("callBack");
        cbFlashLight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cpScan.setFlash(isChecked);
            }
        });
    }


    @OnClick({R.id.ll_scan_back, R.id.tv_select_photo})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_scan_back:
                finish();
                break;
            case R.id.tv_select_photo:
                PictureSelector.create(this)
                        .openGallery(PictureMimeType.ofImage())
                        .selectionMode(PictureConfig.SINGLE)
                        .isCamera(false)
                        .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cpScan != null) {
            cpScan.setScanCallback(resultCallback);
            cpScan.start();
        }

    }


    private ScanCallback resultCallback = new ScanCallback() {
        @Override
        public void onScanResult(ScanResult result) {
            scanResult(result.getContent());
        }

    };


    /**
     * 在AndroidForJSUnits中动态设置扫描结果监听类
     *
     * @param qrCodeScanInter
     */
    public static void setQrCodeScanInter(QrCodeScanInter qrCodeScanInter) {
        mQrCodeScanInter = qrCodeScanInter;
    }


    private void scanResult(String result) {
        vibrate();
        Intent intent = new Intent();
        intent.putExtra("result", result);
        this.setResult(RESULT_OK, intent);
        //如果QrCodeScan不为空，这执行相关回调，同时销毁对象，防止内存堆积
        if (mQrCodeScanInter != null && !TextUtils.isEmpty(result)) {
            mQrCodeScanInter.getQrCodeScanResult(mCallBack, result);
            mQrCodeScanInter = null;
        }
        finish();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (cpScan != null) {
            cpScan.stop();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (cpScan != null) {
            cpScan.setFlash(false);
            cpScan.stop();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        if (cpScan != null) {
            cpScan.setFlash(false);
            cpScan.stop();
        }

    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    scanResult(scanResult);
                    break;
                case 2:
                    ViewUnits.getInstance().showToast("识别失败！");
                    finish();
                    break;
            }


        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == PictureConfig.CHOOSE_REQUEST) {
            String imagePath = PictureSelector.obtainMultipleResult(data).get(0).getPath();

            if (TextUtils.isEmpty(imagePath)) {
                ViewUnits.getInstance().showToast("获取图片失败！");
                return;
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //优先使用zbar识别一次二维码
                        String qrContent = QRUtils.getInstance().decodeQRcode(imagePath);
                        if (!TextUtils.isEmpty(qrContent)) {
                            scanResult = qrContent;
                            handler.sendEmptyMessage(1);
                        } else {
                            //尝试用zxing再试一次识别二维码
                            String qrCode = QRUtils.getInstance().decodeQRcodeByZxing(imagePath);
                            if (!TextUtils.isEmpty(qrCode)) {
                                scanResult = qrCode;
                                handler.sendEmptyMessage(1);
                            } else {
                                //再试试是不是条形码
                                try {
                                    String barCode = QRUtils.getInstance().decodeBarcode(imagePath);
                                    if (!TextUtils.isEmpty(barCode)) {
                                        scanResult = barCode;
                                        handler.sendEmptyMessage(1);
                                    } else {
                                        handler.sendEmptyMessage(2);
                                    }
                                } catch (Exception e) {
                                    handler.sendEmptyMessage(2);
                                    e.printStackTrace();
                                }
                            }

                        }
                    } catch (Exception e) {
                        handler.sendEmptyMessage(2);
                    }
                }
            }).start();
        }
    }

}
