package com.cyandr.robot.hardware;

import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import com.cyandr.robot.hardware.LGPSolver.LGPSDesk;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cyandr on 2017/4/6.
 */


public class LGPSMap extends SurfaceView implements Callback {
    public static final int POSITION_CONFIRMED = 5421;
    int m_Canvaswidth;
    int m_Canvasheight;
    private Paint mPaint;
    private LGPSDesk m_desk;
    private LGPSolver m_LGPSolver;
    private LGPSolver.DeskPoint positionedPoint;
    private Handler mHandler = new Handler();
    private List<Float> m_sensorValue;
    private Handler textHandler;
    private final Runnable mDrawCube = new Runnable() {
        public void run() {
            getResult();


            drawFrame();
        }
    };

    public LGPSMap(Context context, Handler handler) {
        super(context);
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(2);
        mPaint.setStrokeCap(Paint.Cap.SQUARE);
        mPaint.setStyle(Paint.Style.STROKE);
        getHolder().addCallback(this);
        m_sensorValue = new ArrayList<>();
        textHandler = handler;
    }

    public LGPSMap(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();

    }

    void InitLGPSDesk(Rect deskRect, int[] offX, int[] offY) {

        m_desk = new LGPSDesk(deskRect.width(), deskRect.height());
        m_desk.setOffx(offX);
        m_desk.setOffy(offY);
        m_LGPSolver = new LGPSolver(m_desk);

    }

    private void drawFrame() {
        final SurfaceHolder holder = getHolder();
        final Rect frame = holder.getSurfaceFrame();
        m_Canvaswidth = frame.width();
        m_Canvasheight = frame.height();

        Canvas c = null;
        try {

            c = holder.lockCanvas();
            if (c != null) {
                c.save();
                // draw something
                DrawMap(c);

                c.restore();
            }
        } finally {
            if (c != null) holder.unlockCanvasAndPost(c);
        }

        mHandler.removeCallbacks(mDrawCube);
        mHandler.postDelayed(mDrawCube, 1000 / 25);
    }

    public void setResultPoint(List<Float> data) {
        m_sensorValue = data;
        postInvalidate();
    }

    private void getResult() {

        if (m_sensorValue.size() < 1) return;
        m_LGPSolver.setDataFromSensorArray(m_sensorValue);
        m_LGPSolver.Solve();
        positionedPoint = m_LGPSolver.getResult();
        Message msg = Message.obtain();
        msg.what = POSITION_CONFIRMED;
        msg.obj = positionedPoint;
        if (textHandler != null)
            textHandler.sendMessage(msg);
    }

    private void transformPoint(Matrix matrix, Point point) {
        float mat[] = new float[9];
        matrix.getValues(mat);
        point.x = (int) (point.x * mat[0] + point.y * mat[1] + mat[2]);
        point.y = (int) (point.x * mat[3] + point.y * mat[4] + mat[5]);
    }


    private void DrawMap(Canvas canvas) {

        if (m_desk == null)
            return;
        Matrix matrix = new Matrix();

        matrix.setTranslate((float) (0.5 * m_Canvaswidth * 0.1), (float) (0.5 * m_Canvasheight * 0.1));
        matrix.postScale((float) (m_Canvaswidth * 0.8 / m_desk.width), (float) (m_Canvasheight * 0.8 / m_desk.height));

        mPaint.setColor(Color.YELLOW);
        Point ptLT = new Point(0, 0);
        Point ptRb = new Point(m_desk.width, m_desk.height);
        transformPoint(matrix, ptLT);

        transformPoint(matrix, ptRb);

        Rect rect = new Rect(ptLT.x, ptLT.y, ptRb.x, ptRb.y);


        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawRect(rect, mPaint);


        mPaint.setColor(Color.RED);
        int x1 = m_desk.legoffsetX[0];
        int y1 = m_desk.legoffsetY[0];
        Point pt1 = new Point(x1, y1);
        transformPoint(matrix, pt1);

        canvas.drawCircle(pt1.x, pt1.y, 5, mPaint);

        int x2 = m_desk.width - m_desk.legoffsetX[1];
        int y2 = m_desk.legoffsetY[1];
        Point pt2 = new Point(x2, y2);
        transformPoint(matrix, pt2);

        canvas.drawCircle(pt2.x, pt2.y, 5, mPaint);

        int x3 = m_desk.legoffsetX[2];
        int y3 = m_desk.height - m_desk.legoffsetY[2];
        Point pt3 = new Point(x3, y3);
        transformPoint(matrix, pt3);

        canvas.drawCircle(pt3.x, pt3.y, 5, mPaint);

        int x4 = m_desk.width - m_desk.legoffsetX[3];
        int y4 = m_desk.height - m_desk.legoffsetY[3];
        Point pt4 = new Point(x4, y4);
        transformPoint(matrix, pt4);

        canvas.drawCircle(pt4.x, pt4.y, 5, mPaint);

        if (positionedPoint != null)
            canvas.drawPoint(positionedPoint.posX, positionedPoint.posY, mPaint);

    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawFrame();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
