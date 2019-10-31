package exocr.exocrengine;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.hardware.Camera;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.cordova.lexuncompany.R;
import io.cordova.lexuncompany.bean.IDCardBean;
import io.cordova.lexuncompany.view.BaseActivity;
import io.cordova.lexuncompany.view.ScanResultActivity;


/**
 * Author：Li Bin on 2019/8/19 09:24
 * Description：
 */
public class CaptureActivity extends BaseActivity implements SurfaceHolder.Callback {
    @BindView(R.id.surface_view)
    SurfaceView surfaceView;
    @BindView(R.id.capture_scan)
    CaptureView captureView;
    @BindView(R.id.iv_card_back)
    ImageView ivCardBack;

    public boolean isFront;
    private String callback;
    public static final int RESULT_CODE = 2001;

    public static final int SCAN_RESULT = 2019;

    private CaptureHandler handler;
    private boolean hasSurface;

    public static final int PHOTO_ID = 0x1025;
    private boolean bPhotoReco;

    private boolean bCamera;

    private boolean initSuccess;

    private final Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
            AudioManager mgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            mgr.playSoundEffect(AudioManager.FLAG_PLAY_SOUND);
        }
    };
    private IDCardBean idCardBean;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_idcard);
        ButterKnife.bind(this);

        isFront = getIntent().getBooleanExtra("is_front", true);
        callback = getIntent().getStringExtra("callback");
        captureView.setFront(isFront);

        // 检测摄像头权限
        bCamera = hardwareSupportCheck();
        // CameraManager
        CameraManager.init(getApplication());
        // 横屏
        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        // FLAG_TRANSLUCENT_NAVIGATION
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        if (bCamera) {
            hasSurface = false;
            bPhotoReco = false;
        }

        while (!initSuccess) {
            initSuccess = EXOCRDict.InitDict(this);
            if (initSuccess) {
                break;
            }
        }

    }


    @OnClick(R.id.iv_card_back)
    public void onViewClicked() {
        finish();
    }


    public static boolean hardwareSupportCheck() {
        // Camera needs to open
        Camera c = null;
        boolean ret = true;
        try {
            c = Camera.open();
        } catch (RuntimeException e) {
            // throw new RuntimeException();
            ret = false;
        }
        if (c == null) { // 没有背摄像头
            return false;
        }
        if (ret) {
            c.release();
        }
        return ret;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bCamera && !bPhotoReco) {
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            if (hasSurface) {
                initCamera(surfaceHolder);
            } else {
                surfaceHolder.addCallback(this);
                surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openCamera(surfaceHolder);
        } catch (Exception ioe) {
            return;
        }
        if (handler == null) {
            handler = new CaptureHandler(this);
        }
    }

    public Handler getHandler() {
        return handler;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            if (bCamera) {
                float x = event.getX();
                float y = event.getY();
                Point res = CameraManager.get().getResolution();

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (x > res.x * 8 / 10 && y < res.y / 4) {
                        return false;
                    }

                    handleDecode(null);

                    // 点击重新聚焦
                    if (handler != null) {
                        handler.restartAutoFocus();
                    }
                    return true;
                }
            }
        } catch (NullPointerException e) {

        }
        return false;
    }

    public Camera.ShutterCallback getShutterCallback() {
        return shutterCallback;
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface && !bPhotoReco) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }


    public void handleDecode(EXOCRModel result) {

        if (null == result)
            return;
        if (result.isDecodeSucc()) {
            idCardBean = new IDCardBean();
            if (isFront) {
                idCardBean.setPath(result.bitmapPath);
                idCardBean.setOrientation(1);

                idCardBean.setName(result.name);
                idCardBean.setGender(result.sex);
                idCardBean.setAddress(result.address);
                idCardBean.setNation(result.nation);
                idCardBean.setNumber(result.cardnum);
            } else {
                idCardBean = new IDCardBean();
                idCardBean.setPath(result.bitmapPath);
                idCardBean.setOrientation(2);

                idCardBean.setPolice(result.office);
                idCardBean.setDate(result.validdate);
            }
            idCardBean.setCallback(callback);
            Intent intent = new Intent(CaptureActivity.this, ScanResultActivity.class);
            intent.putExtra("id_card", idCardBean);
            startActivityForResult(intent, SCAN_RESULT);

        } else {
            Message message = Message.obtain(this.getHandler(), PreviewCallback.PARSE_FAIL);
            message.sendToTarget();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCAN_RESULT && resultCode == Activity.RESULT_OK) {

            Intent intent = new Intent();
            intent.putExtra("id_card", idCardBean);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

}
