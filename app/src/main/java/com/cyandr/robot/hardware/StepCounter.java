package com.cyandr.robot.hardware;

/**
 * Created by cyandr on 2016/6/12 0012.
 */

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.cyandr.robot.RobotApp;

/**
 * 这是一个实现了信号监听的记步的类
 * 这是从谷歌找来的一个记步的算法，看不太懂
 *
 * @author Liyachao Date:2015-1-6
 */
public class StepCounter implements SensorEventListener {


    public static int CURRENT_SETP = 0;
    public static float SENSITIVITY = 10; // SENSITIVITY灵敏度
    private static long end = 0;
    private static long start = 0;
    float acc, preacc;
    float[] Mag = new float[3];
    long timesta = 0;
    float PRETIME = 0;
    boolean ISMOVING;
    int acci = 0;
    int local = 0;
    private RobotApp myapp;
    private float[] mLastValues = new float[3 * 2];
    private float[] mScale = new float[2];
    private float mYOffset;
    /**
     * 最后加速度方向
     */
    private float[] mLastDirections = new float[3 * 2];
    private float[][] mLastExtremes = {new float[3 * 2], new float[3 * 2]};
    private float[] mLastDiff = new float[3 * 2];
    private int mLastMatch = -1;
    private Handler handler;
    private Sensor AccSensor;
    private Sensor MagSensor;
    private float[] Acc = new float[3];
    private float PreAngle;
    private int[] Celdara;

    /**
     * 传入上下文的构造函数
     *
     * @param context
     */
    public StepCounter(Context context, RobotApp app) {
        super();
        myapp = app;
        handler = myapp.getHandler();
        int h = 480;
        mYOffset = h * 0.5f;
        mScale[0] = -(h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));
        mScale[1] = -(h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));
        ISMOVING = false;

    }

    private int calcSteps() {

        float vSum = 0;
        for (int i = 0; i < 3; i++) {
            final float v = mYOffset + Acc[i] * mScale[1];
            vSum += v;
        }
        int k = 0;
        float v = vSum / 3;

        float direction = (v > mLastValues[k] ? 1 : (v < mLastValues[k] ? -1 : 0));
        if (direction == -mLastDirections[k]) {// Direction changed
            int extType = (direction > 0 ? 0 : 1); // minumum or maximum?
            mLastExtremes[extType][k] = mLastValues[k];
            float diff = Math.abs(mLastExtremes[extType][k]
                    - mLastExtremes[1 - extType][k]);

            if (diff > SENSITIVITY) {
                boolean isAlmostAsLargeAsPrevious = diff > (mLastDiff[k] * 2 / 3);
                boolean isPreviousLargeEnough = mLastDiff[k] > (diff / 3);
                boolean isNotContra = (mLastMatch != 1 - extType);

                if (isAlmostAsLargeAsPrevious && isPreviousLargeEnough
                        && isNotContra) {
                    end = System.currentTimeMillis();
                    if (end - start > 500) {// 此时判断为走了一步

                        CURRENT_SETP++;
                        mLastMatch = extType;
                        start = end;
                    }
                } else {
                    mLastMatch = -1;
                }
            }
            mLastDiff[k] = diff;
        }
        mLastDirections[k] = direction;
        mLastValues[k] = v;
        return CURRENT_SETP;
    }


    //当传感器检测到的数值发生变化时就会调用这个方法
    public void onSensorChanged(SensorEvent sensorEvent) {
        synchronized (this) {
            //position_view.setText("运动中："+local);

        }
        if (sensorEvent.sensor == null) {
            Message sensormessage = Message.obtain();
            sensormessage.what = 21;
            handler.sendMessage(sensormessage);
        }
        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            Mag = sensorEvent.values;
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            Acc = sensorEvent.values;
        timesta = sensorEvent.timestamp;
        float TIME = (timesta * 1000);
        float[] values = new float[3];
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, Acc, Mag);
        SensorManager.getOrientation(R, values);// 要经过一次数据格式的转换，转换为度
        float value = (float) Math.toDegrees(values[0]);
        float RelativeAngle = value - 0;
        int NowDirection = (int) (RelativeAngle / 22.5);
        acc = Acc[1];

        float[] accrl = new float[10];
        float[] ACCS = new float[10];
        if (ISMOVING) {
            Celdara[0] = NowDirection;
        }
        if (TIME - PRETIME >= 0.1) {
            if (acci >= 10) {
                //position_view.setText("静止中："+local);
                ISMOVING = local > 4;
                //timeview.setText("加速度"+acci);
                if (Math.abs(RelativeAngle - PreAngle) > 20) {
                    Celdara[0] = NowDirection;              //参数1 方向标记

                    Celdara[2] = (int) (TIME % 1 / 0.2);    //参数3 时间标记
                    Message sensormessage = Message.obtain();
                    sensormessage.what = 23;
                    handler.sendMessage(sensormessage);
                    PreAngle = RelativeAngle;
                }
                local = 0;
                acci = 0;
            } else {
                accrl[acci] = (Acc[1] - preacc);
                if (accrl[acci] > 0.1) local++;
                acci++;
                preacc = Acc[1];
            }
            //Log.i("传感器", parcedirection(NowDirection));
            Log.i("传感器", +Acc[1] == 1 ? "走动中" : "停止" + Acc[1]);
            Log.i("传感器", Celdara[2] + "秒");
            PRETIME = TIME;
        }
    }

    private void parcedirection(int nowDirection) {
        Message sensormessage = Message.obtain();
        sensormessage.what = 20;
        sensormessage.arg1 = nowDirection;
        handler.sendMessage(sensormessage);

    }

    private int QuakeSort(float[] floats, int low, int high) {
        int highB = high;
        if (low < high) {
            int flag = partion(floats, low, high);
            QuakeSort(floats, low, flag - 1);
            QuakeSort(floats, flag + 1, high);
        }
        return highB;
    }

    private int partion(float[] subfloats, int low, int high) {
        float thelow = subfloats[low];
        while (low < high) {
            while (low < high && thelow < subfloats[high])
                high--;
            swap(subfloats, low, high);

            while (low < high && subfloats[low] < thelow)
                low++;
            swap(subfloats, low, high);
        }
        return low;
    }

    private void swap(float[] subfloats, int low, int high) {
        float temp = subfloats[high];
        subfloats[high] = subfloats[low];
        subfloats[low] = temp;
    }

    //当传感器的经度发生变化时就会调用这个方法，在这里没有用
    public void onAccuracyChanged(Sensor arg0, int arg1) {

    }

}
