package com.cyandr.robot.hardware;

import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class LGPSClient implements Runnable {

    public static final int LGPS_CLIENT_DATARECIEVED = 369;
    public static final int LGPS_CLIENT_ = 385;
    private static boolean isruning = false;
    private String m_IPAdress;
    private int m_Port;
    private int m_timeout = 5000;
    private BufferedReader m_bufReader;
    private OutputStream m_os;
    private Socket m_socket = null;
    private Handler mHandler;

    public LGPSClient(Handler handler) {
        this.mHandler = handler;
    }

    public void InitLGPSServerConnection(String ipAdress, int portnum) {
        m_IPAdress = ipAdress;
        m_Port = portnum;

    }

    @Override
    public void run() {
        {
            Message msg = Message.obtain();
            msg.what = LGPS_CLIENT_;
            msg.obj = m_IPAdress;
            mHandler.sendMessage(msg);
        }
        m_socket = new Socket();
        try {
            m_socket.connect(new InetSocketAddress(m_IPAdress, m_Port), m_timeout);
            m_os = m_socket.getOutputStream();

            String strsend = "REQUEST";

            m_os.write(strsend.getBytes("utf-8"));

            m_os.flush();
            String content = null;
            m_bufReader = new BufferedReader(new InputStreamReader(m_socket.getInputStream(), "utf-8"));

            while (true) {
                content = m_bufReader.readLine();
                if (content != null) {
                    Message msg = Message.obtain();
                    msg.what = LGPS_CLIENT_DATARECIEVED;
                    msg.obj = content;
                    mHandler.sendMessage(msg);
                    break;
                }

            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
