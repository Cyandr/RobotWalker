/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cyandr.robot;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */

public class BlueToothPage extends Activity implements OnClickListener {
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    LGPSClient mlgpsClient;
    private LGPSolver m_lGPSSolver;
    private BluetoothLeScanner mBluetoothScanner;
    private boolean mScanning;
    private Handler mHandler;
    private LGPSMap m_lgpsMap;
    private DeviceListAdapter mDevListAdapter;
    private String ipADRESS = "192.168.31.165";
    private int portNumer = 5556;
    private ScanCallback mLeScanCallback = new ScanCallback() {


        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    BluetoothDevice device = result.getDevice();

                    mDevListAdapter.addDevice(device);
                    mDevListAdapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onBatchScanResults(final List<ScanResult> results) {
            super.onBatchScanResults(results);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (ScanResult result : results) {
                        BluetoothDevice device = result.getDevice();
                        String strprompt = "已找到设备";
                        strprompt += device.getName();
                        Toast.makeText(BlueToothPage.this, strprompt, Toast.LENGTH_SHORT).show();

                        mDevListAdapter.addDevice(device);
                        mDevListAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            String error = " ";
            switch (errorCode) {
                case ScanCallback.SCAN_FAILED_ALREADY_STARTED:
                    error = "SCAN_FAILED_ALREADY_STARTED";
                    break;
                case ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                    error = "SCAN_FAILED_APPLICATION_REGISTRATION_FAILED";
                    break;
                case ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED:
                    error = "SCAN_FAILED_FEATURE_UNSUPPORTED";
                    break;
                case ScanCallback.SCAN_FAILED_INTERNAL_ERROR:
                    error = "SCAN_FAILED_INTERNAL_ERROR";
                    break;
                default:
                    error = "UNKOWN";
                    break;
            }
            String strprompt = "扫描失败";
            strprompt += error;
            Toast.makeText(BlueToothPage.this, strprompt, Toast.LENGTH_SHORT).show();
        }
    };
    private String TAG = "Bluetooth devices scanning";
    private String mDeviceName;
    private boolean m_rx_hex = false;
    private String mDeviceAddress;
    private EditText m_bleInput;
    private RobotBluetoothService mBluetoothLeService;
    // Code to manage Service lifecycle.


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((RobotBluetoothService.LocalBinder) service).getService();
            if (mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    private RobotScrollView m_scrollView;
    private TextView mShowText;
    private boolean connect_status_bit = false;
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (RobotBluetoothService.ACTION_GATT_CONNECTED.equals(action)) {
                invalidateOptionsMenu();
                connect_status_bit = true;
            } else if (RobotBluetoothService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Button btn = findViewById(R.id.ble_connect);
                btn.setText(R.string.disconnected);
                connect_status_bit = false;
                invalidateOptionsMenu();

            } else if (RobotBluetoothService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {

                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if ((RobotBluetoothService.ACTION_GATT_ALREADY_CONNECTED.equals(action))) {
                textOut("got you");
            } else if (RobotBluetoothService.ACTION_DATA_AVAILABLE.equals(action)) //接收FFE1串口透传数据通道数据
            {

                displayData(intent.getByteArrayExtra(RobotBluetoothService.EXTRA_DATA));

            } else if (RobotBluetoothService.ACTION_DATA_AVAILABLE1.equals(action)) //接收FFE2功能配置返回的数据
            {
                Toast.makeText(BlueToothPage.this, "接收到数据", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RobotBluetoothService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(RobotBluetoothService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(RobotBluetoothService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(RobotBluetoothService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(RobotBluetoothService.ACTION_DATA_AVAILABLE1);
        intentFilter.addAction(RobotBluetoothService.ACTION_GATT_ALREADY_CONNECTED);
        return intentFilter;
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {


        if (gattServices == null) return;
        mBluetoothLeService.Delay_ms(300);
        if (gattServices.size() > 0 && mBluetoothLeService.get_connected_status(gattServices) == 1)//表示为JDY-06、JDY-08系列蓝牙模块
        {
            if (connect_status_bit) {
                textOut(getResources().getString(R.string.connected));
            }
        } else if (gattServices.size() > 0/*&&mBluetoothLeService.get_connected_status( gattServices )==1*/)//表示为JDY-09、JDY-10系列蓝牙模块
        {

            if (connect_status_bit) {

                mBluetoothLeService.Delay_ms(100);
                mBluetoothLeService.enable_JDY_ble(0);
                mBluetoothLeService.Delay_ms(100);
                mBluetoothLeService.enable_JDY_ble(1);

                textOut(getResources().getString(R.string.connected));


            }
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        m_lgpsMap = new LGPSMap(this, mHandler);
        Rect rect = new Rect(0, 0, 500, 300);
        int[] xoff = {20, 20, 20, 20};
        int[] yoff = {10, 10, 10, 10};
        m_lgpsMap.InitLGPSDesk(rect, xoff, yoff);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                500);
        params.gravity = Gravity.TOP;
        params.topMargin = 100;
        addContentView(m_lgpsMap, params);
        mHandler = new LGPSDeskHandler();

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        mDevListAdapter = new DeviceListAdapter();


        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        Intent gattServiceIntent = new Intent(BlueToothPage.this, RobotBluetoothService.class);
        boolean sg = bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        Button m_send = findViewById(R.id.ble_send);
        m_send.setOnClickListener(this);


        m_bleInput = findViewById(R.id.ble_inputbox);
        m_scrollView = findViewById(R.id.ble_logscroll);
        Button m_btnConnect = findViewById(R.id.ble_connect);
        m_btnConnect.setOnClickListener(this);
        mShowText = findViewById(R.id.ble_logTxt);
        mlgpsClient = new LGPSClient(mHandler);
        mlgpsClient.InitLGPSServerConnection(ipADRESS, portNumer);
        initBleConnect();

        m_scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float DownX = 0, DownY = 0, moveY;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        DownX = event.getX();// float DownX DownY =
                        // event.getY();//float DownY
                        DownY = event.getY();
                        m_scrollView.performClick();
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

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ble_connect:
                OnScanningBloothdevieces();
                break;
            case R.id.ble_send: {
                if (connect_status_bit) {
                    String tx_string = m_bleInput.getText().toString().trim();
                    int tx_count = mBluetoothLeService.txxx(tx_string, true);//发送字符串数据
                    textOut("发送数据：" + tx_count);
                } else {
                    new Thread(mlgpsClient).start();
                    textOut("网络已启动：");
                }
                // mB
                // luetoothLeService.toReadFromBle();
            }
            break;
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bluetooth_main, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);

            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    private void textOut(String message) {
        Spannable colorMessage = new SpannableString(message + "\n");
        colorMessage.setSpan(new ForegroundColorSpan(0xff0000ff), 0, message.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mShowText.append(colorMessage);
        m_scrollView.smoothScrollBy(0, 60);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                scanLeDevice(true);
                mDevListAdapter.clear();
                mDevListAdapter.notifyDataSetChanged();
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
            case R.id.menu_rxhex:
                m_rx_hex = !m_rx_hex;
                break;
        }
        return true;
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothScanner.stopScan(mLeScanCallback);
                    BlueToothPage.this.invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothScanner.startScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothScanner.stopScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }

    @Override
    protected void onResume() {//打开APP时扫描设备
        super.onResume();
        scanLeDevice(true);
    }

    @Override
    protected void onPause() {//停止扫描
        super.onPause();
        scanLeDevice(false);
        unregisterReceiver(mGattUpdateReceiver);
        mBluetoothLeService.disconnect();
    }

    @Override
    protected void onDestroy() {

        unbindService(mServiceConnection);
        mBluetoothLeService.disconnect();
        mHandler.removeCallbacksAndMessages(null);
        mBluetoothLeService = null;
        super.onDestroy();
    }

    private void parseResult(String strLine) {


        List<Float> floats = new ArrayList<>();
        int pIndex = -1, last_index = 0;
        try {
            for (int i = 0; i < strLine.length(); i++) {
                char ch = strLine.charAt(i);
                if (ch == '-') {
                    pIndex = i;
                    if (pIndex == last_index) continue;
                    String seg = strLine.substring(last_index, pIndex);
                    floats.add(Float.valueOf(seg));
                    last_index = pIndex + 1;
                }

            }
        } catch (Exception e) {
            textOut(e.toString());
            e.printStackTrace();
        }
        for (Float wei : floats) {
            textOut(wei.toString());
        }
        m_lgpsMap.setResultPoint(floats);


    }

    private void displayData(byte[] data1) //接收FFE1串口透传数据通道数据
    {
        if (data1 != null && data1.length > 0) {

            if (m_rx_hex) {
                StringBuilder sbValues = new StringBuilder();
                for (byte byteChar : data1) {
                    sbValues.append(String.format("%02X", byteChar));
                }
                String hexString = sbValues.toString();
                int weight = Integer.parseInt(hexString, 16);
                textOut(String.valueOf(weight));
            } else {

                String res = new String(data1);
                textOut(res);
                // parseResult(res);
            }
        }

    }

    private void initBleConnect() {
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        // private LeDeviceListAdapter mLeDeviceListAdapter;
        if (bluetoothManager == null) {
            Toast.makeText(this, "蓝牙设备未找到！", Toast.LENGTH_SHORT).show();
            finish();
        }
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, RESULT_FIRST_USER);
            }
            mBluetoothScanner = mBluetoothAdapter.getBluetoothLeScanner();
        } else {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
        }

    }

    private void OnScanningBloothdevieces() {

        LayoutInflater inflater_long = (LayoutInflater) BlueToothPage.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout popList = (LinearLayout) inflater_long.inflate(R.layout.data_list, null);
        ListView data_date = popList.findViewById(R.id.data_date);

        data_date.setAdapter(mDevListAdapter);
        PopupWindow history = new PopupWindow(popList, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        history.setFocusable(true);
        history.setOutsideTouchable(true);
        history.update();
        history.setBackgroundDrawable(getResources().getDrawable(R.color.touming));
        history.showAtLocation(BlueToothPage.this.findViewById(R.id.ble_inputbox), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, m_bleInput.getHeight() + 5);
        data_date.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mDevListAdapter.getCount() > 0) {

                    BluetoothDevice device1 = mDevListAdapter.getItem(position);
                    if (device1 == null) return;
                    mDeviceName = device1.getName();
                    mDeviceAddress = device1.getAddress();

                    textOut("try to Connect device" + mDeviceName);

                    if (mBluetoothLeService != null) {

                        boolean result = mBluetoothLeService.connect(mDeviceAddress);
                        Log.d(TAG, "Connect request result=" + result);
                        mDevListAdapter.setAttchedIndex(position);
                        mBluetoothLeService.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

                        if (result) {
                            textOut("连接成功！");
                        }
                    } else
                        Log.d(TAG, "Bluetooth services is null");

                    if (mScanning) {
                        mBluetoothScanner.stopScan(mLeScanCallback);
                        mScanning = false;
                    }

                }

            }
        });

    }

    class LGPSDeskHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case LGPSClient.LGPS_CLIENT_DATARECIEVED:
                    String strData = (String) msg.obj;
                    textOut(strData);
                    parseResult(strData);
                    break;
                case LGPSClient.LGPS_CLIENT_:
                    String string = String.format("客户端已向%s发送数据请求。。。", msg.obj);
                    textOut(string);
                    break;
                case LGPSMap.POSITION_CONFIRMED:
                    LGPSolver.DeskPoint point = (LGPSolver.DeskPoint) msg.obj;
                    textOut("x方向：" + String.valueOf(point.posX));
                    textOut("y方向：" + String.valueOf(point.posY));
                    textOut("力大小：" + String.valueOf(point.force));
                    break;
                default:
                    super.handleMessage(msg);
                    break;

            }

        }
    }

    class DeviceListAdapter extends BaseAdapter {

        private List<BluetoothDevice> mBleArray;

        private int m_deviceAttchedIndex;

        DeviceListAdapter() {
            mBleArray = new ArrayList<>();
            m_deviceAttchedIndex = -1;
        }

        void addDevice(BluetoothDevice device) {
            if (!mBleArray.contains(device)) {
                mBleArray.add(device);
            }
        }

        void clear() {
            mBleArray.clear();
        }

        @Override
        public int getCount() {
            return mBleArray.size();
        }

        @Override
        public BluetoothDevice getItem(int position) {
            return mBleArray.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(BlueToothPage.this).inflate(
                        R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.tv_devName = convertView.findViewById(R.id.device_name);
                viewHolder.tv_devAddress = convertView.findViewById(R.id.device_address);
                //convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            // add-Parameters
            BluetoothDevice device = mBleArray.get(position);
            String devName = device.getName();
            if (devName != null && devName.length() > 0) {
                viewHolder.tv_devName.setText(devName);
            } else {
                viewHolder.tv_devName.setText(getString(R.string.unknowDevice));
            }
            viewHolder.tv_devAddress.setText(device.getAddress());

            convertView.setTag(viewHolder);
            if (position == m_deviceAttchedIndex) {
                convertView.setBackgroundColor(Color.GREEN);
            }
            return convertView;
        }

        public void setAttchedIndex(int m_deviceAttchedIndex) {
            this.m_deviceAttchedIndex = m_deviceAttchedIndex;
        }
    }

    class ViewHolder {
        TextView tv_devName, tv_devAddress;
    }


}