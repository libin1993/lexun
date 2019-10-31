package exocr.exocrengine;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import io.cordova.lexuncompany.R;


/**
 * description: 初始化字典
 * create by kalu on 2018/11/16 9:34
 */
public class EXOCRDict {

    private static void clearDict() {

        int code = EXOCREngine.nativeDone();
        Log.e("libin", "clearDict ==> code = " + code);
    }

    private static boolean checkSign(final Context context) {
        int code = EXOCREngine.nativeCheckSignature(context);
        Log.e("libin", "checkSign ==> code = " + code);
        return code == 1;
    }

    private static boolean checkDict(final String path) {

        byte[] bytes = path.getBytes();
        int code = EXOCREngine.nativeInit(bytes);
        Log.e("libin", "checkDict ==> code = " + code);
        return code >= 0;
    }

    private static boolean checkFile(final Context context, final String pathname) {

        File file = new File(pathname);
        if (!file.exists() || file.isDirectory()) {

            file.delete();

            try {
                file.createNewFile();

                int byteread;
                final byte[] buffer = new byte[1024];
                final InputStream is = context.getResources().openRawResource(R.raw.zocr0);
                final OutputStream fs = new FileOutputStream(file);// to为要写入sdcard中的文件名称

                while ((byteread = is.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteread);
                }
                is.close();
                fs.close();
                return true;
            } catch (Exception e) {
                Log.e("libin", "checkFile ==> message = " + e.getMessage(), e);
                return false;
            }
        } else {
            return true;
        }
    }

    public static boolean InitDict(Activity activity) {

         String name = "/zocr0.lib";
         String path = activity.getCacheDir().getAbsolutePath();
         String pathname = path + name;

        // step1: 检测字典是否存在
        boolean okFile = checkFile(activity, pathname);
        if (!okFile) {
            clearDict();
            return false;
        }

        // step2: 检测字典是否正确
        boolean okDict = checkDict(path);
        if (!okDict) {
            clearDict();
            return false;
        }

        // step3: 检测字典签名
        boolean okSign = checkSign(activity);
        if (!okSign) {
            clearDict();
        }

        return okSign;

    }
}
