package com.cyandr.robot.VoiceSolver;

import android.content.Context;
import android.media.AudioManager;
import android.os.Environment;
import android.os.Message;
import com.baidu.tts.auth.AuthInfo;
import com.baidu.tts.client.*;
import com.cyandr.robot.RobotApp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.cyandr.robot.Constant.MSG_RECOGNIZE_START;

/**
 * Created by xiaoh on 2017/5/1.
 */
public class SpeechSpeaker implements SpeechSynthesizerListener {

    public static final String VOICE_FEMALE = "F";
    public static final String VOICE_MALE = "M";
    public static final String VOICE_DUYY = "Y";
    public static final String VOICE_DUXY = "X";
    private static final String ROBOTWALKER_DIR_NAME = "RobotWalker";
    String TEXT_FILE = "bd_etts_text.dat";
    String MODEL_FILE = null;
    // TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； 没有纯离线
    private TtsMode ttsMode = TtsMode.MIX;
    private SpeechSynthesizer mSpeechSpeaker;
    private RobotApp myapp;
    private String mSampleDirPath;
    private Context mContext;

    public SpeechSpeaker(Context context, RobotApp app) {
        mContext = context;
        myapp = app;
        setOfflineVoiceType(VOICE_DUXY);
        initialTts();

    }

    public void setOfflineVoiceType(String voiceType) {

        if (VOICE_MALE.equals(voiceType)) {
            MODEL_FILE = "bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat";
        } else if (VOICE_FEMALE.equals(voiceType)) {
            MODEL_FILE = "bd_etts_common_speech_f7_mand_eng_high_am-mix_v3.0.0_20170512.dat";
        } else if (VOICE_DUXY.equals(voiceType)) {
            MODEL_FILE = "bd_etts_common_speech_yyjw_mand_eng_high_am-mix_v3.0.0_20170512.dat";
        } else if (VOICE_DUYY.equals(voiceType)) {
            MODEL_FILE = "bd_etts_common_speech_as_mand_eng_high_am_v3.0.0_20170516.dat";
        } else {

        }

        if (mSampleDirPath == null) {
            String sdcardPath = Environment.getExternalStorageDirectory().toString();
            mSampleDirPath = sdcardPath + "/" + ROBOTWALKER_DIR_NAME;
        }

        makeDir(mSampleDirPath);
        copyFromAssetsToSdcard(false, TEXT_FILE, mSampleDirPath + "/" + TEXT_FILE);
        copyFromAssetsToSdcard(false, MODEL_FILE, mSampleDirPath + "/" + MODEL_FILE);
    }


    private void makeDir(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * 将sample工程需要的资源文件拷贝到SD卡中使用（授权文件为临时授权文件，请注册正式授权）
     *
     * @param isCover 是否覆盖已存在的目标文件
     * @param source
     * @param dest
     */
    private void copyFromAssetsToSdcard(boolean isCover, String source, String dest) {
        File file = new File(dest);
        if (isCover || (!isCover && !file.exists())) {
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = mContext.getResources().getAssets().open(source);
                fos = new FileOutputStream(dest);
                byte[] buffer = new byte[1024];
                int size = 0;
                while ((size = is.read(buffer, 0, 1024)) >= 0) {
                    fos.write(buffer, 0, size);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void initialTts() {
        boolean isMix = ttsMode.equals(TtsMode.MIX);

        mSpeechSpeaker = SpeechSynthesizer.getInstance();
        mSpeechSpeaker.setContext(mContext);
        mSpeechSpeaker.setSpeechSynthesizerListener(this);
        // 文本模型文件路径 (离线引擎使用)
        mSpeechSpeaker.setParam(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, mSampleDirPath + "/"
                + TEXT_FILE);
        // 声学模型文件路径 (离线引擎使用)
        mSpeechSpeaker.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, mSampleDirPath + "/"
                + MODEL_FILE);
        // 本地授权文件路径,如未设置将使用默认路径.设置临时授权文件路径，LICENCE_FILE_NAME请替换成临时授权文件的实际路径，仅在使用临时license文件时需要进行设置，如果在[应用管理]中开通了正式离线授权，不需要设置该参数，建议将该行代码删除（离线引擎）
        // 如果合成结果出现临时授权文件将要到期的提示，说明使用了临时授权文件，请删除临时授权即可。
        //mSpeechSpeaker.setParam(SpeechSynthesizer.PARAM_TTS_LICENCE_FILE, mSampleDirPath + "/"
        //        + LICENSE_FILE_NAME);
        // 请替换为语音开发者平台上注册应用得到的App ID (离线授权)
        mSpeechSpeaker.setAppId("9501235"/*这里只是为了让Demo运行使用的APPID,请替换成自己的id。*/);
        // 请替换为语音开发者平台注册应用得到的apikey和secretkey (在线授权)
        mSpeechSpeaker.setApiKey("25URaTWtSfU2uNTEdVMppkxB",
                "151058ea5c91d651ec891fd46f9f59ba"/*这里只是为了让Demo正常运行使用APIKey,请替换成自己的APIKey*/);
        // 发音人（在线引擎），可用参数为0,1,2,3。。。（服务器端会动态增加，各值含义参考文档，以文档说明为准。0--普通女声，1--普通男声，2--特别男声，3--情感男声。。。）
        //mSpeechSpeaker.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置Mix模式的合成策略
        //mSpeechSpeaker.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
        // 授权检测接口(只是通过AuthInfo进行检验授权是否成功。)
        // AuthInfo接口用于测试开发者是否成功申请了在线或者离线授权，如果测试授权成功了，可以删除AuthInfo部分的代码（该接口首次验证时比较耗时），不会影响正常使用（合成使用时SDK内部会自动验证授权）
        AuthInfo authInfo = mSpeechSpeaker.auth(TtsMode.MIX);

        if (authInfo.isSuccess()) {
            toPrint("auth success");
        } else {
            String errorMsg = authInfo.getTtsError().getDetailMessage();
            toPrint("auth failed errorMsg=" + errorMsg);
        }
        // 5. 以下setParam 参数选填。不填写则默认值生效
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        mSpeechSpeaker.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置合成的音量，0-9 ，默认 5
        mSpeechSpeaker.setParam(SpeechSynthesizer.PARAM_VOLUME, "9");
        // 设置合成的语速，0-9 ，默认 5
        mSpeechSpeaker.setParam(SpeechSynthesizer.PARAM_SPEED, "5");
        // 设置合成的语调，0-9 ，默认 5
        mSpeechSpeaker.setParam(SpeechSynthesizer.PARAM_PITCH, "5");

        mSpeechSpeaker.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
        // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线

        mSpeechSpeaker.setAudioStreamType(AudioManager.MODE_IN_CALL);

        // 初始化tts
        mSpeechSpeaker.initTts(TtsMode.MIX);
        // 加载离线英文资源（提供离线英文合成功能）
        // int result =mSpeechSpeaker.loadModel(mSampleDirPath + "/" + ENGLISH_TEXT_MODEL_NAME, mSampleDirPath
        //                 + "/" + ENGLISH_SPEECH_FEMALE_MODEL_NAME);
        // toPrint("loadEnglishModel result=" + result);

        //打印引擎信息和model基本信息
        // printEngineInfo();
    }

    /**
     * 打印引擎so库版本号及基本信息和model文件的基本信息
     */
    private void printEngineInfo() {
        toPrint("EngineVersioin=" + SynthesizerTool.getEngineVersion());
        toPrint("EngineInfo=" + SynthesizerTool.getEngineInfo());
        String textModelInfo = SynthesizerTool.getModelInfo(mSampleDirPath + "/" + TEXT_FILE);
        toPrint("textModelInfo=" + textModelInfo);
        String speechModelInfo = SynthesizerTool.getModelInfo(mSampleDirPath + "/" + MODEL_FILE);
        toPrint("speechModelInfo=" + speechModelInfo);
    }


    private void synthesize(String tring) {

        //需要合成的文本text的长度不能超过1024个GBK字节。
        int result = this.mSpeechSpeaker.synthesize(tring);
        if (result < 0) {
            toPrint("error,please look up error code in doc or URL:http://yuyin.baidu.com/docs/tts/122 ");
        }
    }

    private void batchSpeak() {
        List<SpeechSynthesizeBag> bags = new ArrayList<SpeechSynthesizeBag>();
        bags.add(getSpeechSynthesizeBag("123456", "0"));
        bags.add(getSpeechSynthesizeBag("你好", "1"));
        bags.add(getSpeechSynthesizeBag("使用百度语音合成SDK", "2"));
        bags.add(getSpeechSynthesizeBag("hello", "3"));
        bags.add(getSpeechSynthesizeBag("这是一个demo工程", "4"));
        int result = this.mSpeechSpeaker.batchSpeak(bags);
        if (result < 0) {
            toPrint("error,please look up error code in doc or URL:http://yuyin.baidu.com/docs/tts/122 ");
        }
    }

    private SpeechSynthesizeBag getSpeechSynthesizeBag(String text, String utteranceId) {
        SpeechSynthesizeBag speechSynthesizeBag = new SpeechSynthesizeBag();
        //需要合成的文本text的长度不能超过1024个GBK字节。
        speechSynthesizeBag.setText(text);
        speechSynthesizeBag.setUtteranceId(utteranceId);
        return speechSynthesizeBag;
    }

    private void toPrint(String string) {

        myapp.showText(string);
    }

    public void setParam(String key, String value) {

        mSpeechSpeaker.setParam(key, value);


    }

    public void release() {

        mSpeechSpeaker.release();

    }

    public void speak(String string) {


        mSpeechSpeaker.speak(string);

    }

    public void pause() {


        mSpeechSpeaker.pause();
    }

    public void resume() {
        mSpeechSpeaker.resume();

    }

    public void stop() {

        mSpeechSpeaker.stop();
    }

    @Override
    public void onSynthesizeStart(String s) {

    }

    @Override
    public void onSynthesizeDataArrived(String s, byte[] bytes, int i) {

    }

    @Override
    public void onSynthesizeFinish(String s) {

    }

    @Override
    public void onSpeechStart(String s) {

    }

    @Override
    public void onSpeechProgressChanged(String s, int i) {

    }

    @Override
    public void onSpeechFinish(String s) {
        Message message = Message.obtain();
        message.what = MSG_RECOGNIZE_START;
        myapp.getHandler().sendMessage(message);
    }

    @Override
    public void onError(String s, SpeechError speechError) {

        myapp.showText(s);
    }

}
