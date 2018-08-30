package com.cyandr.robot;


import android.app.Service;
import android.bluetooth.*;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class RobotBluetoothService extends Service {

    static final String ACTION_GATT_CONNECTED = "com.cyandr.robot.le.ACTION_GATT_CONNECTED";
    static final String ACTION_GATT_ALREADY_CONNECTED = "com.cyandr.robot.le.ALREADY_CONNECTED";
    static final String ACTION_GATT_DISCONNECTED = "com.cyandr.robot.le.ACTION_GATT_DISCONNECTED";
    static final String ACTION_GATT_SERVICES_DISCOVERED = "com.cyandr.robot.le.ACTION_GATT_SERVICES_DISCOVERED";
    static final String ACTION_DATA_AVAILABLE = "ccom.cyandr.robot.le.ACTION_DATA_AVAILABLE";
    static final String ACTION_DATA_AVAILABLE1 = "com.cyandr.robot.le.ACTION_DATA_AVAILABLE1";
    static final String EXTRA_DATA = "com.cyandr.robot.le.EXTRA_DATA";
    static final String EXTRA_DATA1 = "com.cyandr.robot.le.EXTRA_DATA1";


    private static final String TAG = RobotBluetoothService.class.getSimpleName();
    private static final UUID UUID_HEART_RATE_MEASUREMENT;
    private static String Service_uuid;
    private static String Characteristic_uuid_TX;
    private static String Characteristic_uuid_FUNCTION;

    static {
        UUID_HEART_RATE_MEASUREMENT = UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);
        Service_uuid = "0000ffe0-0000-1000-8000-00805f9b34fb";
        Characteristic_uuid_TX = "0000ffe1-0000-1000-8000-00805f9b34fb";
        Characteristic_uuid_FUNCTION = "0000ffe2-0000-1000-8000-00805f9b34fb";
    }

    private final IBinder mBinder = new RobotBluetoothService.LocalBinder();
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;

            if (newState == BluetoothGatt.STATE_CONNECTED) {

                intentAction = ACTION_GATT_CONNECTED;
                RobotBluetoothService.this.broadcastUpdate(intentAction);

                Log.i(RobotBluetoothService.TAG, "Attempting to start service discovery:" + RobotBluetoothService.this.mBluetoothGatt.discoverServices());
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                Log.i(RobotBluetoothService.TAG, "Disconnected from GATT server.");
                RobotBluetoothService.this.broadcastUpdate(intentAction);
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == 0) {

                RobotBluetoothService.this.broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);

            } else {

                Log.w(RobotBluetoothService.TAG, "onServicesDiscovered received: " + status);

            }

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            if (status == BluetoothGatt.GATT_SUCCESS) {

                if (UUID.fromString(RobotBluetoothService.Characteristic_uuid_TX).equals(characteristic.getUuid())) {

                    RobotBluetoothService.this.broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);


                } else if (UUID.fromString(RobotBluetoothService.Characteristic_uuid_FUNCTION).equals(characteristic.getUuid())) {

                    RobotBluetoothService.this.broadcastUpdate(ACTION_DATA_AVAILABLE1, characteristic);

                }
            }

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (UUID.fromString(RobotBluetoothService.Characteristic_uuid_TX).equals(characteristic.getUuid())) {

                RobotBluetoothService.this.broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);

            } else if (UUID.fromString(RobotBluetoothService.Characteristic_uuid_FUNCTION).equals(characteristic.getUuid())) {

                RobotBluetoothService.this.broadcastUpdate(ACTION_DATA_AVAILABLE1, characteristic);

            }

        }
    };

    public RobotBluetoothService() {


    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    private static byte[] getBytesByString(String data) {
        byte[] bytes = null;
        if (data != null) {
            data = data.toUpperCase();
            int length = data.length() / 2;
            char[] dataChars = data.toCharArray();
            bytes = new byte[length];

            for (int i = 0; i < length; ++i) {
                int pos = i * 2;
                bytes[i] = (byte) (charToByte(dataChars[pos]) << 4 | charToByte(dataChars[pos + 1]));
            }
        }

        return bytes;
    }

    int get_connected_status(List<BluetoothGattService> gattServices) {
        boolean jdy_ble_server = false;
        boolean jdy_ble_ffe1 = false;
        boolean jdy_ble_ffe2 = false;

        String uuid = null;
        String unknownServiceString = "unknownServiceString";
        String unknownCharaString = "unknownCharaString";
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList();

        for (BluetoothGattService gattService : gattServices) {
            uuid = gattService.getUuid().toString();
            HashMap currentServiceData = new HashMap();
            currentServiceData.put("NAME", SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put("UUID", uuid);
            gattServiceData.add(currentServiceData);
            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList();
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList();
            if (Service_uuid.equals(uuid)) {
                jdy_ble_server = true;
            }

            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put("NAME", SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put("UUID", uuid);
                gattCharacteristicGroupData.add(currentCharaData);
                if (jdy_ble_server) {
                    if (Characteristic_uuid_TX.equals(uuid)) {
                        jdy_ble_ffe1 = true;
                    } else if (Characteristic_uuid_FUNCTION.equals(uuid)) {
                        jdy_ble_ffe2 = true;
                    }
                }
            }

            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        if (jdy_ble_ffe1 && jdy_ble_ffe2) {
            return 2;
        } else if (jdy_ble_ffe1 && !jdy_ble_ffe2) {
            return 1;
        } else {
            return 0;
        }
    }

    void Delay_ms(int ms) {
        try {
            Thread.currentThread();
            Thread.sleep((long) ms);
        } catch (InterruptedException var3) {
            var3.printStackTrace();
        }

    }

    private void deley() {
        try {
            Thread.currentThread();
            Thread.sleep((long) 23);
        } catch (InterruptedException var3) {
            var3.printStackTrace();
        }

    }

    int txxx(String g, boolean string_or_hex_data) {
        int ic = 0;
        byte[] writeBytes = new byte[200];
        if (string_or_hex_data) {
            writeBytes = g.getBytes();
        } else {
            writeBytes = getBytesByString(g);
        }

        int length = writeBytes.length;
        int data_len_20 = length / 20;
        int data_len_0 = length % 20;
        int i = 0;
        byte[] da;
        int h;
        BluetoothGattCharacteristic gg;
        if (data_len_20 > 0) {
            while (i < data_len_20) {
                da = new byte[20];

                for (h = 0; h < 20; ++h) {
                    da[h] = writeBytes[20 * i + h];
                }

                gg = this.mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_TX));
                gg.setValue(da);
                this.mBluetoothGatt.writeCharacteristic(gg);
                this.deley();
                ic += 20;
                ++i;
            }
        }

        if (data_len_0 > 0) {
            da = new byte[data_len_0];

            for (h = 0; h < data_len_0; ++h) {
                da[h] = writeBytes[20 * i + h];
            }

            gg = this.mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_TX));
            gg.setValue(da);
            this.mBluetoothGatt.writeCharacteristic(gg);
            ic += data_len_0;
            this.deley();
        }

        return ic;
    }

    void enable_JDY_ble(int p) {
        try {
            BluetoothGattService service = this.mBluetoothGatt.getService(UUID.fromString(Service_uuid));
            BluetoothGattCharacteristic ale;
            switch (p) {
                case 0:
                    ale = service.getCharacteristic(UUID.fromString(Characteristic_uuid_TX));
                    break;
                case 1:
                    ale = service.getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
                    break;
                default:
                    ale = service.getCharacteristic(UUID.fromString(Characteristic_uuid_TX));
            }

            this.mBluetoothGatt.setCharacteristicNotification(ale, true);
            BluetoothGattDescriptor dsc = ale.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
            byte[] bytes = new byte[]{1, 0};
            dsc.setValue(bytes);
            this.mBluetoothGatt.writeDescriptor(dsc);
        } catch (NumberFormatException var8) {
            var8.printStackTrace();
        }

    }


    private void broadcastUpdate(String action) {
        Intent intent = new Intent(action);
        this.sendBroadcast(intent);
    }

    private void broadcastUpdate(String action, BluetoothGattCharacteristic characteristic) {
        Intent intent = new Intent(action);
        Log.d("getUuid", " len = " + characteristic.getUuid());

        byte[] data;
        if (UUID.fromString(Characteristic_uuid_TX).equals(characteristic.getUuid())) {
            data = characteristic.getValue();
            if (data != null && data.length > 0) {
                intent.putExtra(EXTRA_DATA, data);
            }
        } else if (UUID.fromString(Characteristic_uuid_FUNCTION).equals(characteristic.getUuid())) {
            data = characteristic.getValue();
            if (data != null && data.length > 0) {
                intent.putExtra(EXTRA_DATA1, data);
            }
        }
        this.sendBroadcast(intent);
    }

    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    public boolean onUnbind(Intent intent) {
        this.close();
        return super.onUnbind(intent);
    }

    boolean initialize() {
        if (this.mBluetoothManager == null) {
            this.mBluetoothManager = (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
            if (this.mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return true;
            }
        }

        this.mBluetoothAdapter = this.mBluetoothManager.getAdapter();
        if (this.mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return true;
        } else {
            return false;
        }
    }

    boolean connect(String address) {
        if (this.mBluetoothAdapter != null && address != null) {
            if (address.equals(this.mBluetoothDeviceAddress) && this.mBluetoothGatt != null) {
                RobotBluetoothService.this.broadcastUpdate(ACTION_GATT_ALREADY_CONNECTED);
                Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
                return this.mBluetoothGatt.connect();
            } else {
                BluetoothDevice device = this.mBluetoothAdapter.getRemoteDevice(address);
                if (device == null) {
                    Log.w(TAG, "Device not found.  Unable to connect.");
                    return false;
                } else {
                    this.mBluetoothGatt = device.connectGatt(this, false, this.mGattCallback);
                    Log.d(TAG, "Trying to create a new connection.");
                    this.mBluetoothDeviceAddress = address;
                    return true;
                }
            }
        } else {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
    }

    void disconnect() {
        if (this.mBluetoothAdapter != null && this.mBluetoothGatt != null) {
            this.mBluetoothGatt.disconnect();
        } else {
            Log.w(TAG, "BluetoothAdapter not initialized");
        }
    }


    private void close() {
        if (this.mBluetoothGatt != null) {
            this.mBluetoothGatt.close();
            this.mBluetoothGatt = null;
        }
    }

    List<BluetoothGattService> getSupportedGattServices() {
        return this.mBluetoothGatt == null ? null : this.mBluetoothGatt.getServices();
    }


    class LocalBinder extends Binder {
        LocalBinder() {
        }

        RobotBluetoothService getService() {
            return RobotBluetoothService.this;
        }
    }
}


