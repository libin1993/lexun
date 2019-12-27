package io.cordova.lexuncompany.units;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

import io.cordova.lexuncompany.application.MyApplication;


/**
 * 语音工具类
 * Created by JasonYao on 2017/12/5.
 */

public class SpeechCompoundUnits {
    private static final String TAG = "SpeechUnits---";
    private static SpeechCompoundUnits mInstances;

    private static SpeechSynthesizer mTts;

    private static SpeakListener mListener;

    private SpeechCompoundUnits() {
        Log.e(TAG, "初始化");
        if (mTts != null) {
            Log.e(TAG, "null");
            return;
        }
        mTts = SpeechSynthesizer.createSynthesizer(MyApplication.getInstance().getBaseContext(),
                errorcode -> {
                    if (ErrorCode.SUCCESS == errorcode) {
                        Log.e(TAG, "成功");
                    } else {
                        Log.e(TAG, "失败");
                        Toast.makeText(MyApplication.getInstance().getBaseContext(), "语音合成初始化失败!", Toast.LENGTH_SHORT);
                    }
                });

        init();

        mListener = new SpeakListener();
    }

    public void init() {
        //设置发音人
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
        //设置语速,值范围：[0, 100],默认值：50
        mTts.setParameter(SpeechConstant.SPEED, "45");
        //设置音量
        mTts.setParameter(SpeechConstant.VOLUME, "tts_volume");
        //设置语调
        mTts.setParameter(SpeechConstant.PITCH, "tts_pitch");
    }

    public static SpeechCompoundUnits getInstance() {
        if (mInstances == null) {
            synchronized (SpeechCompoundUnits.class) {
                if (mInstances == null) {
                    mInstances = new SpeechCompoundUnits();
                }
            }
        }

        return mInstances;
    }

    /**
     * 将文本转为语音
     *
     * @param text
     */
    public void

    speakText(String text) {
        Log.e(TAG, text);
        mTts.startSpeaking(text, mListener);
    }

    private class SpeakListener implements SynthesizerListener {

        @Override
        public void onSpeakBegin() {
            Log.e(TAG, "开始播放");
        }

        @Override
        public void onBufferProgress(int i, int i1, int i2, String s) {

        }

        @Override
        public void onSpeakPaused() {

        }

        @Override
        public void onSpeakResumed() {

        }

        @Override
        public void onSpeakProgress(int i, int i1, int i2) {

        }

        @Override
        public void onCompleted(SpeechError speechError) {

        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    }
}
