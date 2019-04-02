package com.cyandr.robot;

import android.app.Application;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import static com.cyandr.robot.Constant.*;
import static com.cyandr.robot.RobotFace.*;

/**
 * Created by xiaoh on 2017/5/1.
 */
public class RobotApp extends Application {

    private Handler myHandler = null;

    public Handler getHandler() {

        return myHandler;

    }

    public void setHandler(Handler handler) {
        this.myHandler = handler;
    }

    public void showText(String string) {

        if (myHandler == null) {
            Toast.makeText(this, "还未初始化！", Toast.LENGTH_SHORT).show();
            return;
        }
        Message msg = Message.obtain();
        msg.what = 1;
        msg.obj = string;
        myHandler.sendMessage(msg);

    }

    public void ASRResult(String string) {

        if (myHandler == null) {
            Toast.makeText(this, "还未初始化！", Toast.LENGTH_SHORT).show();
            return;
        }
        Message msg = Message.obtain();
        msg.what = MSG_RECOGNIZE_RESULT;
        msg.obj = string;
        myHandler.sendMessage(msg);

    }

    public void BeginAwaitingWakeUp() {

        if (myHandler == null) {
            Toast.makeText(this, "还未初始化！", Toast.LENGTH_SHORT).show();
            return;
        }
        Message msg = Message.obtain();
        msg.what = MSG_WAKEING_UP_AWAITING;

        myHandler.sendMessage(msg);

    }

    public void EndAwaitingWakeUp() {

        if (myHandler == null) {
            Toast.makeText(this, "还未初始化！", Toast.LENGTH_SHORT).show();
            return;
        }
        Message msg = Message.obtain();
        msg.what = MSG_WAKEUP_SUCCESS;

        myHandler.sendMessage(msg);

    }
}
