package com.cyandr.robot;

import android.content.Context;
import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class BDASR {

    private RobotApp myapp;

    private EventManager eventManager;
    private EventListener m_eventListener = new EventListener() {
        @Override
        public void onEvent(String name, String params, byte[] data, int offset, int length) {
            if (name.equals(SpeechConstant.CALLBACK_EVENT_WAKEUP_SUCCESS)) {//唤醒已停止

            } else if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_READY)) {

            } else if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_FINISH)) {
                RecogResult recogResult = RecogResult.parseJson(params);
                if (recogResult.hasError()) {
                    int errorCode = recogResult.getError();
                    int subErrorCode = recogResult.getSubError();

                    // listener.onAsrFinishError(errorCode, subErrorCode, ErrorTranslation.recogError(errorCode), recogResult.getDesc(), recogResult);
                } else {


                    //myapp.showText(params);
                    // listener.onAsrFinish(recogResult);
                }


            } else if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_BEGIN)) {
                // myapp.ASRResult(nbest.get(0));
            } else if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL)) {

                if (params.contains("final_result")) {


                    RecogResult recogResult = RecogResult.parseJson2(params);

                    myapp.ASRResult(recogResult.getBestResult());

                }

                // myapp.ASRResult(params);
                // myapp.ASRResult(nbest.get(0));
            } else if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_EXIT)) {
                //myapp.showText("Asr 退出");
                //  myapp.BeginAwaitingWakeUp();
                // myapp.ASRResult(nbest.get(0));
            } else if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_LOADED)) {
                // myapp.ASRResult(nbest.get(0));
            } else if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_UNLOADED)) {
                // myapp.showText("Asr 引擎下线");

                // myapp.ASRResult(nbest.get(0));
            }
        }
    };
    private boolean enableOffline = false; // 测试离线命令词，需要改成true

    public boolean isAvailable() {
        return eventManager != null;
    }

    /**
     * 测试参数填在这里
     */
    public void start() {

        Map<String, Object> params = new LinkedHashMap<String, Object>();

        if (enableOffline) {
            params.put(SpeechConstant.DECODER, 2);
        }
        params.put(SpeechConstant.ACCEPT_AUDIO_DATA, false);
        params.put(SpeechConstant.DISABLE_PUNCTUATION, false);
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, true);
        params.put(SpeechConstant.NLU, "enable");

//        VAD_ENDPOINT_TIMEOUT=0 开启长语音
//        注意这个参数 >0 时，表示关闭长语音。作用是静音断句的时长设置，描述请见下文。 普通的录音限制60s, 长语音可以识别数小时的音频。注意选输入法模型。
        params.put(SpeechConstant.VAD_ENDPOINT_TIMEOUT, 5); // 长语音
        //params.put(SpeechConstant.IN_FILE, "res:///com/baidu/android/voicedemo/16k_test.pcm");
        params.put(SpeechConstant.VAD, SpeechConstant.VAD_DNN);
        params.put(SpeechConstant.PROP, 20000);
        params.put(SpeechConstant.PID, 1537); // 中文输入法模型，有逗号
        // 请先使用如‘在线识别’界面测试和生成识别参数。 params同ActivityRecog类中myRecognizer.start(params);
        String json = new JSONObject(params).toString(); // 可以替换成自己的json
        eventManager.send(SpeechConstant.ASR_START, json, null, 0, 0);

    }

    void stop() {

        eventManager.send(SpeechConstant.ASR_STOP, null, null, 0, 0); //

    }

    void cancel() {
        eventManager.send(SpeechConstant.ASR_CANCEL, null, null, 0, 0);

    }

    private void loadOfflineEngine() throws JSONException {
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        params.put(SpeechConstant.DECODER, 2);
        params.put(SpeechConstant.ASR_OFFLINE_ENGINE_GRAMMER_FILE_PATH, "assets://baidu_speech_grammar.bsg");
        // 下面这段可选，用于生成SLOT_DATA参数， 用于动态覆盖ASR_OFFLINE_ENGINE_GRAMMER_FILE_PATH文件的词条部分
        JSONObject json = new JSONObject();
        json.put("name", new JSONArray().put("王自强").put("叶问")).put("appname", new JSONArray().put("手百").put("度秘"));
        params.put(SpeechConstant.SLOT_DATA, json.toString());

        eventManager.send(SpeechConstant.ASR_KWS_LOAD_ENGINE, new JSONObject(params).toString(), null, 0, 0);
    }

    private void unloadOfflineEngine() {
        eventManager.send(SpeechConstant.ASR_KWS_UNLOAD_ENGINE, null, null, 0, 0); //
    }

    void init(Context context) {
        myapp = (RobotApp) context.getApplicationContext();

        eventManager = EventManagerFactory.create(context, "asr");
        eventManager.registerListener(m_eventListener); //  EventListener 中 onEvent方法
        if (enableOffline) {
            try {
                loadOfflineEngine(); // 测试离线命令词请开启, 测试 ASR_OFFLINE_ENGINE_GRAMMER_FILE_PATH 参数时开启
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


    }

    void destroy() {

        eventManager.send(SpeechConstant.ASR_STOP, "{}", null, 0, 0);
        if (enableOffline) {
            unloadOfflineEngine(); // 测试离线命令词请开启, 测试 ASR_OFFLINE_ENGINE_GRAMMER_FILE_PATH 参数时开启
        }

        eventManager.unregisterListener(m_eventListener);
        eventManager = null;

    }
}
