package io.cordova.lexuncompany.units;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import io.cordova.lexuncompany.application.MyApplication;

/**
 * 页面工具类
 * Created by JasonYao on 2018/3/1.
 */

public class ViewUnits {
    public static ViewUnits mIntances = null;

    //缓存等待框
    private LoadingDialog.Builder mLoadBuilder;
    private static LoadingDialog mDialog;
    private Toast mToast;
    private boolean isReflectedHandler = false;

    private ViewUnits() {
    }

    public static ViewUnits getInstance() {
        if (mIntances == null) {
            synchronized (ViewUnits.class) {
                if (mIntances == null) {
                    mIntances = new ViewUnits();
                }
            }
        }

        return mIntances;
    }

    /**
     * 显示弹吐司
     *
     * @param message
     */
    public void showToast(String message) {

        if (TextUtils.isEmpty(message)) {
            return;
        }

        try {
            if (mToast == null) {
                mToast = Toast.makeText(MyApplication.getInstance().getApplicationContext(), message, Toast.LENGTH_SHORT);
                mToast.setGravity(Gravity.CENTER, 0, 0);
            } else {
                mToast.setText(message);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && Build.VERSION.SDK_INT < Build.VERSION_CODES.O && !isReflectedHandler) {
                reflectTNHandler(mToast);
                //这里为了避免多次反射，使用一个标识来限制
                isReflectedHandler = true;
                return;
            }

            mToast.show();

        } catch (Exception e) {
        }

    }
    /**
     * 显示等待框
     */
    public void showLoading(Context context, String message) {
        mLoadBuilder = new LoadingDialog.Builder(context)
                .setMessage(message)
                .setCancelable(true)
                .setCancelOutside(true);
        mDialog = mLoadBuilder.create();
        mDialog.show();
    }

    /**
     * 隐藏等待框
     */
    public void missLoading() {
        if (mDialog == null) {
            return;
        }

        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    /**
     * 设置标题栏高度（适配各种异性全面屏、各种状态栏高度）
     *
     * @param view
     */
    public void setTitleHeight(View view) {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        layoutParams.height = getStatusBarHeight();
        view.setLayoutParams(layoutParams);
    }


    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = MyApplication.getInstance().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = MyApplication.getInstance().getResources().getDimensionPixelSize(resourceId);
        }

        return result;
    }


    /**
     * dp转px
     *
     * @param context
     * @param dp
     * @return
     */
    public int dp2px(Context context, int dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    /**
     * px转换dp
     */
    public int px2dip(int px) {
        final float scale = MyApplication.getInstance().getResources().getDisplayMetrics().density;

        return (int) (px / scale + 0.5f);
    }

    /**
     * 防止在android7.x系统会偶发Toast显示异常报BadTokenException
     *
     * @param toast
     */
    private static void reflectTNHandler(Toast toast) {
        try {
            Field tNField = toast.getClass().getDeclaredField("mTN");
            if (tNField == null) {
                return;
            }
            tNField.setAccessible(true);
            Object TN = tNField.get(toast);
            if (TN == null) {
                return;
            }
            Field handlerField = TN.getClass().getDeclaredField("mHandler");
            if (handlerField == null) {
                return;
            }
            handlerField.setAccessible(true);
            handlerField.set(TN, new ProxyTNHandler(TN));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    private static class ProxyTNHandler extends Handler {
        private Object tnObject;
        private Method handleShowMethod;
        private Method handleHideMethod;

        ProxyTNHandler(Object tnObject) {
            this.tnObject = tnObject;
            try {
                this.handleShowMethod = tnObject.getClass().getDeclaredMethod("handleShow", IBinder.class);
                this.handleShowMethod.setAccessible(true);
                this.handleHideMethod = tnObject.getClass().getDeclaredMethod("handleHide");
                this.handleHideMethod.setAccessible(true);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0: {
                    //SHOW
                    IBinder token = (IBinder) msg.obj;
                    if (handleShowMethod != null) {
                        try {
                            handleShowMethod.invoke(tnObject, token);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        } catch (WindowManager.BadTokenException e) {
                            //显示Toast时添加BadTokenException异常捕获
                            e.printStackTrace();
                        }
                    }
                    break;
                }

                case 1:
                case 2: {
                    //HIDE
                    if (handleHideMethod != null) {
                        try {
                            handleHideMethod.invoke(tnObject);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                }//CANCEL

            }
            super.handleMessage(msg);
        }
    }
}
