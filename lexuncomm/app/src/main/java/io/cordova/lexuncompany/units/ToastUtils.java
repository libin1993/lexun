package io.cordova.lexuncompany.units;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Author：Libin on 2019/5/7 10:27
 * Email：1993911441@qq.com
 * Describe：Toast重复弹出问题
 */
public class ToastUtils {

    private static Toast mToast;

    public static void showToast(Context context, String content) {
        if (mToast == null) {
            mToast = Toast.makeText(context.getApplicationContext(), content, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(content);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.setGravity(Gravity.CENTER, 0, 0);
        mToast.show();
    }
}
