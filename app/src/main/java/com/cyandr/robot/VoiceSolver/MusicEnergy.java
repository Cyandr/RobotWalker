package com.cyandr.robot.VoiceSolver;

import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import java.util.Arrays;

public class MusicEnergy extends SurfaceView implements Callback {

    private static final int frequency = 1000 / 25;
    private final Handler mHandler = new Handler();
    private final Paint mPaint = new Paint();
    private AudioCapture mAudioCapture = null;
    private int[] mVizData = new int[1024];
    private int mWidth = 0;
    private int mCenterY = 0;

    private float soundMa = 0;
    private int drawtime = 0;
    private final Runnable mDrawCube = new Runnable() {
        public void run() {

            drawFrame();
        }
    };
    private int m_oxL, m_oxR;
    private int draw_times = 0;
    private boolean isINCREASING = true;

    public MusicEnergy(Context context) {
        super(context);

        mAudioCapture = new AudioCapture(AudioCapture.TYPE_PCM, 1024);
        mAudioCapture.start();
        mPaint.setColor(Color.WHITE);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(2);
        mPaint.setStrokeCap(Paint.Cap.SQUARE);
        mPaint.setStyle(Paint.Style.STROKE);
        getHolder().addCallback(this);
    }

    private void drawFrame() {
        final SurfaceHolder holder = getHolder();
        final Rect frame = holder.getSurfaceFrame();
        final int width = frame.width();
        final int height = frame.height();

        Canvas c = null;
        try {
            drawtime++;
            c = holder.lockCanvas();
            if (c != null) {
                c.save();
                // draw something
                drawCube(c);
                drawFace(c, frame);
                DrawEye(c, frame);
                DrawMouse(c, frame);
                c.restore();
            }
        } finally {
            if (c != null) holder.unlockCanvasAndPost(c);
        }

        mHandler.removeCallbacks(mDrawCube);
        mHandler.postDelayed(mDrawCube, frequency);
    }

    private void drawCube(Canvas c) {

        c.drawColor(0xff000000);

        if (mAudioCapture != null) {
            mVizData = mAudioCapture.getFormattedData(1, 1);
        } else {
            Arrays.fill(mVizData, 0);
        }

        for (int i = 0; i < mVizData.length; i += 10) {

            c.drawCircle(mWidth / 2, mCenterY, mVizData[i] * 10, mPaint);
            c.drawPoint(i, mCenterY + mVizData[i], mPaint);
            c.drawLine(i, mCenterY, i, mCenterY + mVizData[i], mPaint);
            if (mVizData[i] > 0) {

                soundMa++;
            } else {
                soundMa--;
            }
        }


    }

    private void drawFace(Canvas canvas, Rect rect) {

        mPaint.setColor(Color.YELLOW);

        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(rect, mPaint);


    }

    @Override
    protected void finalize() throws Throwable {
        // TODO Auto-generated method stub
        super.finalize();
        if (mAudioCapture != null) {

            mAudioCapture.stop();
            mAudioCapture.release();
            mAudioCapture = null;
        }
    }

    void onClose() {
        if (mAudioCapture != null) {

            mAudioCapture.stop();
            mAudioCapture.release();
            mAudioCapture = null;
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // TODO Auto-generated method stub
        mCenterY = height / 2;
        mWidth = width;
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        // setBackgroundColor(Color.WHITE);
        drawFrame();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub

    }

    public void DrawEye(Canvas canvas, Rect rect) {

        mPaint.setColor(Color.YELLOW);
        mPaint.setStyle(Paint.Style.FILL);


        int left = rect.width() / 6;
        int right = rect.width() * 2 / 3;
        int top = rect.top + 20;
        int bottom = top + left;

        RectF rect11 = new RectF(left + 10, top + 10, rect.width() / 3 - 10, bottom - 10);
        RectF rect12 = new RectF(right + 10, top + 10, rect.width() * 5 / 6 - 10, bottom - 10);

        RectF rect1 = new RectF(left, top, rect.width() / 3, bottom);
        RectF rect2 = new RectF(right, top, rect.width() * 5 / 6, bottom);

        canvas.drawOval(rect11, mPaint);
        canvas.drawOval(rect12, mPaint);

        mPaint.setStrokeWidth(10);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setARGB(255, (int) (left * soundMa), 0, 128);

        canvas.drawRect(rect1, mPaint);
        canvas.drawRect(rect2, mPaint);

        mPaint.setColor(Color.CYAN);
        //canvas.drawText("asd", getWidth() / 2 - mBound.width() / 2, getHeight() / 2 + mBound.height() / 2, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2);

    }

    private void DrawMouse(Canvas canvas, Rect rect) {
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.FILL);

        int wi = 80;
        int left = rect.width() / 2 - wi;
        int right = left + wi * 2;
        int top = rect.bottom - 120;
        int bottom = top + 100;

        RectF rect11 = new RectF(left + 10, top + 10, right - 10, bottom - 10);

        RectF rect12 = new RectF(left, top, right, bottom);
        mPaint.setARGB(255, (int) (left * soundMa), 0, 128);
        canvas.drawOval(rect11, mPaint);

        mPaint.setColor(Color.CYAN);
        mPaint.setStrokeWidth(10);
        mPaint.setStyle(Paint.Style.STROKE);


        canvas.drawRect(rect12, mPaint);


        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2);


    }

    protected void DrawFacth(Canvas canvas) {

        if (draw_times > 50000) {
            draw_times = 0;
        }
        int left = getWidth() / 6;
        int right = getWidth() * 2 / 3;
        if (draw_times == 0) {
            m_oxL = left;
            m_oxR = right;
        }
        m_oxL = (int) ((3 * left / 2) + (left / 2) * Math.sin(draw_times / 2)) - left / 12;
        m_oxR = (int) (right + left / 2 + (left / 2) * Math.sin(draw_times / 2)) - left / 12;
        mPaint.setARGB(draw_times % 255, 25, 0, 0);


        canvas.drawRect(left, 0, left * 2, 100, mPaint);
        canvas.drawRect(right, 0, getWidth() * 5 / 6, 100, mPaint);

        mPaint.setARGB(200, 100, draw_times % 200, 0);
        canvas.drawCircle(m_oxL + left / 12, 100 / 2, 100 / 2, mPaint);

        mPaint.setARGB(200, draw_times % 200, 0, 0);
        canvas.drawCircle(m_oxR + left / 12, 100 / 2, 100 / 2, mPaint);
        mPaint.setARGB(255, 0, 255, 0);
        float mmm = (float) Math.abs(getWidth() * 0.5 * 0.8 * Math.cos(draw_times * 0.2)) - 100;
        canvas.drawRect(getWidth() / 2 - mmm,
                180,
                getWidth() / 2 + mmm,
                200, mPaint);
        mmm = (float) Math.abs(getWidth() * 0.5 * 0.7 * Math.cos(draw_times * 0.3 + 2)) - 100;
        canvas.drawRect(getWidth() / 2 - mmm,
                210,
                getWidth() / 2 + mmm,
                230, mPaint);

        mmm = (float) Math.abs(getWidth() * 0.5 * Math.cos(draw_times * 0.5 + 2)) - 200;
        canvas.drawRect(getWidth() / 2 - mmm,
                240,
                getWidth() / 2 + mmm,
                260, mPaint);

        if (isINCREASING) {
            draw_times++;
        }

    }
}
