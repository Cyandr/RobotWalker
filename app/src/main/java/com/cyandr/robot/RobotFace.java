package com.cyandr.robot;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.*;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.baidu.tts.client.SpeechSynthesizer;

import java.util.ArrayList;

/**
 * Created by cyandr on 2017/3/25.
 */
public class RobotFace extends Activity {


    /**
     * 开始识别
     */
    static final int MSG_RECOGNIZE_RESULT = 11;
    /**
     * 开始识别
     */
    static final int MSG_RECOGNIZE_START = 12;
    static final int MSG_WAKEUP_SUCCESS = 16;
    /**
     * 返回结果，开始说话
     */
    static final int MSG_WAKEING_UP_AWAITING = 15;
    private static final int MSG_SPEECH_START = 10;
    /**
     * 网络请求回调
     */
    private final int[] QuestionAnsered = {0, 0, 0};
    private SpeechSpeaker mSpeechSpeaker = null;
    private MusicEnergy mMusicEnergy = null;

    private BDASR mAsrListener;

    private RobotApp myapp;
    private TuringInterface turingInterface;

    private EditText mInput;
    private TextView mShowText;
    private int m_speaking_num = 0;
    private RobotScrollView m_scrollView;
    private MyHandler mHandler;
    private HttpRequestListener myHttpConnectionListener = new HttpRequestListener() {

        @Override
        public void onSuccess(String result) {
            if (result != null) {
                //  try {
                mHandler.obtainMessage(MSG_SPEECH_START,
                        result).sendToTarget();

                //    JSONObject result_obj = new JSONObject(result);
//                    if (result_obj.has("text")) {
//                        scrollText
//                        mHandler.obtainMessage(MSG_SPEECH_START,
//                                result_obj.get("text")).sendToTarget();
                // }
                //   } catch (JSONException e) {
                //       mSpeechSpeaker.speak("呀,小辉，收到的都是什么鬼东西！");
                //   }
            }
        }

        @Override
        public void onFail(int code, String error) {

            myapp.showText("网络慢脑袋不灵了");

        }
    };

    public RobotFace() {

    }

    @Override
    protected void onDestroy() {
        mSpeechSpeaker.release();
        mAsrListener.destroy();

        mMusicEnergy.onClose();
        super.onDestroy();
    }

    void Speak(String str) {
        if (m_speaking_num % 2 == 0)
            mSpeechSpeaker.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
        else
            mSpeechSpeaker.setParam(SpeechSynthesizer.PARAM_SPEAKER, "2");

        mSpeechSpeaker.speak(str);
        m_speaking_num++;

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.robot_head);
        mHandler = new MyHandler();
        mMusicEnergy = new MusicEnergy(this);
        myapp = (RobotApp) getApplication();
        myapp.setHandler(mHandler);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            initPermission();
        }
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                300);
        params.gravity = Gravity.TOP;
        params.topMargin = 100;
        addContentView(mMusicEnergy, params);

        mInput = this.findViewById(R.id.input);
        mShowText = this.findViewById(R.id.show_text);
        m_scrollView = findViewById(R.id.scroll_text);


        init();
        InitUI();
        mSpeechSpeaker.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");


        m_scrollView.setOnTouchListener(new View.OnTouchListener() {
            float DownX, DownY, moveY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        DownX = event.getX();// float DownX DownY =
                        // event.getY();//float DownY
                        DownY = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float moveX = event.getX() - DownX;// X轴距离
                        moveY = event.getY() - DownY;// y轴距离

                        break;
                    case MotionEvent.ACTION_UP:
                        m_scrollView.performClick();
                        break;
                }
                return false;
            }

        });

    }

    private void InitUI() {
        final Button btn_speak = findViewById(R.id.btn_speak);
        btn_speak.setText("----");
        btn_speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlphaAnimation alphaAnimation = new AlphaAnimation(.5f, 1.0f);
                // alphaAnimation.setDuration(2000);

                btn_speak.setAnimation(alphaAnimation);

                String text = mInput.getText().toString();

                scrollText(text);
                Message msg = Message.obtain();
                msg.what = MSG_RECOGNIZE_START;

                mHandler.sendMessage(msg);

            }
        });


        Button m_btnBle = findViewById(R.id.btn_bleMode);
        Button m_btnAccessory = findViewById(R.id.btn_accessoryMode);
        m_btnAccessory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RobotFace.this, UsbAccesseryPage.class);
                startActivity(intent);
            }
        });

        m_btnBle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RobotFace.this, BlueToothPage.class);
                startActivity(intent);
            }
        });
    }

    private void SwitchListener() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (QuestionAnsered) {
                    synchronized (mAsrListener) {
                        if (QuestionAnsered[0] != 0) {

                        }
                    }
//                    synchronized (m_wakeupListener) {
//
//                    }
                }

            }
        }).start();

    }

    private void autoListen() {
        mAsrListener.start();

    }


    @Override
    protected void onPause() {
        mSpeechSpeaker.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAsrListener.cancel();
        mSpeechSpeaker.resume();
    }

    private void scrollText(String message) {
        Spannable colorMessage = new SpannableString(message + "\n");
        colorMessage.setSpan(new ForegroundColorSpan(0xff0000ff), 0, message.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mShowText.append(colorMessage);
        m_scrollView.smoothScrollBy(0, 120);
    }

    /**
     * 初始化turingSDK、识别和tts
     */
    private void init() {
        mSpeechSpeaker = new SpeechSpeaker(this, myapp);
        mAsrListener = new BDASR();

        mAsrListener.init(this);
        //m_wakeupListener.init(this);
        /** 支持百度，需自行去相关平台申请appid，并导入相应的jar和so文件 */
        turingInterface = new TuringInterface();

        turingInterface.setHttpRequestListener(myHttpConnectionListener);
        mSpeechSpeaker.speak("你好啊，boss！");


    }

    @Override
    protected void onStop() {
        mHandler.removeCallbacksAndMessages(null);
        mAsrListener.stop();
        mSpeechSpeaker.stop();
        //m_wakeupListener.stop();
        super.onStop();
    }

    private void initPermission() {

        ArrayList<String> toApplyList = new ArrayList<String>();

        if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.RECORD_AUDIO)) {
            toApplyList.add(Manifest.permission.RECORD_AUDIO);
            // 进入到这里代表没有权限.
        }
        if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE)) {
            toApplyList.add(Manifest.permission.ACCESS_NETWORK_STATE);
            // 进入到这里代表没有权限.
        }
        if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.INTERNET)) {
            toApplyList.add(Manifest.permission.INTERNET);
            // 进入到这里代表没有权限.
        }
        if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.READ_PHONE_STATE)) {
            toApplyList.add(Manifest.permission.READ_PHONE_STATE);
            // 进入到这里代表没有权限.
        }
        if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            toApplyList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            // 进入到这里代表没有权限.
        }
        if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            toApplyList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            // 进入到这里代表没有权限.
        }
        if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            toApplyList.add(Manifest.permission.ACCESS_FINE_LOCATION);
            // 进入到这里代表没有权限.
        }
        if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.BLUETOOTH)) {
            toApplyList.add(Manifest.permission.BLUETOOTH);
            // 进入到这里代表没有权限.
        }
        if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN)) {
            toApplyList.add(Manifest.permission.BLUETOOTH_ADMIN);
            // 进入到这里代表没有权限.
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            requestPermissions(toApplyList.toArray(tmpList), 123);
        }

    }

    private void restartWakeUp() {

        mAsrListener.destroy();

    }

    private void restartASR() {


        mAsrListener.init(this);
        mAsrListener.start();
    }

    private void runCommand(String cmd) {
        if (cmd.contains("退出") || cmd.contains("停止")) {
            myapp.BeginAwaitingWakeUp();
        } else
            turingInterface.getStr(cmd);

    }

    class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {//此方法在ui线程运行


            switch (msg.what) {
                case 1:
                    scrollText((String) msg.obj);
                    break;
                case 2:


                    break;
                case 0:
                    scrollText((String) msg.obj);
                    break;
                case MSG_SPEECH_START:
                    scrollText("沃克尔：" + msg.obj);
                    mSpeechSpeaker.speak((String) msg.obj);
                    mAsrListener.cancel();
                    break;
                case MSG_RECOGNIZE_RESULT:
                    scrollText("您：" + msg.obj);
                    QuestionAnsered[0]++;
                    runCommand((String) msg.obj);
                    break;
                case MSG_RECOGNIZE_START:

                    QuestionAnsered[0]--;
                    if (!mAsrListener.isAvailable())
                        restartASR();
                    autoListen();
                    break;
                case MSG_WAKEING_UP_AWAITING:

                    restartWakeUp();
                    break;
                case MSG_WAKEUP_SUCCESS:
                    restartASR();

                    break;
                default:

                    break;

            }


        }
    }

}