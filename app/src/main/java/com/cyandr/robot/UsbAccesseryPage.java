package com.cyandr.robot;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.TextView;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;


public class UsbAccesseryPage extends Activity {

    private static final String ACTION_USB_PERMISSION = "com.cyandr.robot.USB_PERMISSION";
    private static final String TAG = "USBAccessory";

    UsbAccessory accessory = null;
    private PendingIntent mPermissionIntent;
    private UsbManager mUsbManager;
    private UsbAccessory mAccessory;
    private ParcelFileDescriptor mFileDescriptor;
    private FileInputStream mInputStream;
    private FileOutputStream mOutputStream;
    private TextView m_text_view;
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (accessory != null) {
                            UsbAccessory[] accessoryList = mUsbManager.getAccessoryList();
                            StringBuilder str = new StringBuilder();
                            m_text_view.setText(Integer.toString(accessoryList.length));

                            if (accessoryList != null) {
                                for (UsbAccessory accesso : accessoryList) {
                                    if (accesso != null) {
                                        str.append(accesso.getDescription()).append(' ').append(accesso.getModel()).append(' ').append(accesso.getSerial());

                                    }
                                }
                            }
                            m_text_view.setText(str.toString());
                            m_text_view.setText("链接成功 ！");
                            openAccessory();
                            //call method to set up accessory communication
                        }
                    } else {
                        Log.d("USBAccessory", "permission denied for accessory " + accessory);
                    }
                }
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.accessory_page);

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);
        mUsbManager.requestPermission(accessory, mPermissionIntent);
        m_text_view = findViewById(R.id.usbDescribtextView);


    }

    private void openAccessory() {
        Log.d(TAG, "openAccessory: " + accessory);
        mFileDescriptor = mUsbManager.openAccessory(mAccessory);
        if (mFileDescriptor != null) {
            FileDescriptor fd = mFileDescriptor.getFileDescriptor();
            mInputStream = new FileInputStream(fd);
            mOutputStream = new FileOutputStream(fd);

            new Thread(new Runnable() {
                @Override
                public void run() {

                }
            }).start();

        }
    }
}