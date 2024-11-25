package com.example.bluetoothlelib;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.unity3d.player.UnityPlayer;

import java.util.Arrays;
import java.util.UUID;

public class BluetoothLE {
    //    private static final String RECEIVE_OBJECT_NAME = "BluetoothLEReceiver";
    private static final String RECEIVE_OBJECT_NAME = "ControllerInfoDisplayUI";
    //    private static final String RECEIVE_OBJECT_NAME = "GameObject";
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("994e94d2-5ef5-46a2-8423-05ecfbe06a18");
    final private BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothLeScanner bluetoothLeScanner = adapter.getBluetoothLeScanner();

    private BluetoothGatt gatt;
    private BluetoothDevice device;

    static final int REQUEST_CODE = 1;
    //    private Context context;
    private Activity activity;
    private Context context;
    private int REQUEST_ENABLE_BT = 1;
    private boolean scanning;
    private Handler handler = new Handler();
    long SCAN_PERIOD = 20000;
    boolean isPassStatus133 = false;
    boolean gattConnected = false;


//    private void checkBluetoothLEPermission() {
//        try {
//            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(activity, new String[]{
//                        Manifest.permission.CAMERA
//                }, REQUEST_CODE);
//            }
//
//
//        } catch (Exception e) {
//            unityDebugMessage("permissionCheck is Failed");
//            unityDebugMessage(e.toString());
//        }
//        unityDebugMessage(String.valueOf(Build.VERSION.SDK_INT));
//    }

    // 初期化.
    public void initialize() {
        //Bluetoothアダプターを初期化
        try {

            unityDebugMessage("start plugin initialize");
            activity = UnityPlayer.currentActivity;
            context = activity.getApplicationContext();
//            BluetoothManager manager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
//            adapter = BluetoothAdapter.getDefaultAdapter();

            if (adapter != null && !adapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(activity, enableBtIntent, REQUEST_ENABLE_BT, null);
            }
            scanning = false;

            unitySendMessage("InitializeCallback");
            unityDebugMessage("Finish BluetoothLE.initialize()");
        } catch (Exception e) {
            unityDebugMessage("BluetoothLE.initialize is Failed");
            unityDebugMessage(e.toString());
        }
    }

    // スキャン開始.
    @RequiresApi(api = Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission")
    public void startScan() {

        ScanSettings.Builder scanSettings = new ScanSettings.Builder();
        scanSettings.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        ScanSettings settings = scanSettings.build();


//        unityDebugMessage("permissionCheck"
//                + activity.checkSelfPermission("android.permission.BLUETOOTH_CONNECT") + ", "
//                + activity.checkSelfPermission("android.permission.BLUETOOTH_SCAN") + ", "
//                + activity.checkSelfPermission("android.permission.BLUETOOTH_ADVERTISE") + ", "
//                + activity.checkSelfPermission("android.permission.BLUETOOTH_ADMIN") + ", "
//                + activity.checkSelfPermission("android.permission.ACCESS_BACKGROUND_LOCATION") + ", "
//                + activity.checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") + ", "
//                + activity.checkSelfPermission("android.permission.ACCESS_FINE_LOCATION") + ", "
//        );


        try {

            if (!scanning) {
                handler.postDelayed(() -> {
                    unityDebugMessage("scanTimeout");
                    scanning = false;
                    bluetoothLeScanner.stopScan(scanCallback);
                }, SCAN_PERIOD);
                scanning = true;
                new Thread(() -> bluetoothLeScanner.startScan(null, settings, scanCallback), "StartScan()").start();
//                bluetoothLeScanner.startScan(null, settings, scanCallback);
            } else {
                scanning = false;
                bluetoothLeScanner.stopScan(scanCallback);
            }

            unityDebugMessage("Finish BluetoothLE.startScan()");


        } catch (Exception e) {
            unityDebugMessage("BluetoothLE.startScan() is Failed");
            unityDebugMessage(e.toString());
        }

//        try {
//            bluetoothLeScanner.startScan(null, settings, scanCallback);
//            unityDebugMessage("Finish BluetoothLE.startScan()");
//        } catch (Exception e) {
//            unityDebugMessage("BluetoothLE.startScan() is Failed");
//            unityDebugMessage(e.toString());
//        }
    }


    // スキャンの停止.
    @SuppressLint("MissingPermission")
    public void stopScan() {
        unityDebugMessage("start BluetoothLe.scanner.stopScan()");
        bluetoothLeScanner.stopScan(scanCallback);
        unityDebugMessage("finish BluetoothLe.scanner.stopScan()");

    }

    // デバイス接続.
    @SuppressLint("MissingPermission")
    public void connectToDevice(String address) {
        unityDebugMessage("start BluetoothLe.connectToDevice()");
        device = adapter.getRemoteDevice(address);
        if (device == null) {
            return;
        }
        if (gatt != null) {
            gatt.disconnect();
        }
        gatt = device.connectGatt(activity, false, gattCallback, BluetoothDevice.TRANSPORT_LE); // BluetoothDevice.TRANSPORT_LE
        unityDebugMessage("finish BluetoothLe.connectToDevice()");

    }

    // デバイス接続解除.
    @SuppressLint("MissingPermission")
    public void disconnectDevice() {
        if (gatt != null) {
            gatt.disconnect();
            gatt = null;
        }
    }


    @SuppressLint("MissingPermission")
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
//            unitySendMessage("start scanCallback()");
            if (result.getDevice() == null) {
                return;
            }

            // 検出したデバイス情報を通知.
            String deviceName = result.getDevice().getName();
            String address = result.getDevice().getAddress();
            unityDebugMessage("ScanCallback deviceName:" + deviceName + ", address:" + address);
            unitySendMessage("ScanCallback", deviceName, address);
        }
    };
    @SuppressLint("MissingPermission")
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int state) {
            if (gatt.requestMtu(512)) {
                unityDebugMessage("Requested MTU successfully");
            } else {
                unityDebugMessage("Failed to request MTU");
            }

            if (!isPassStatus133) {
                unityDebugMessage("gattCallback.onConnectionStateChange()");

            }
            if (state == BluetoothProfile.STATE_CONNECTED) {
                // 接続成功.

                unitySendMessage("ConnectCallback");
            } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                // 接続解除.
                if (status == 133) {
                    if (!isPassStatus133) {

                        unityDebugMessage("133");
                        isPassStatus133 = true;
                    }
                    device.connectGatt(activity, false, gattCallback);
                } else {
                    unityDebugMessage("" + status);
                    unitySendMessage("DisconnectCallback");
                }
//                unityDebugMessage("" + status);
//                unitySendMessage("DisconnectCallback");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            unityDebugMessage("gattCallback.onServicesDiscovered");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // 検出したサービスとCharacteristicを通知.
                gattConnected = true;
                for (BluetoothGattService service : gatt.getServices()) {
                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
//                        unityDebugMessage(service.getUuid().toString() + characteristic.getUuid().toString());
                        unitySendMessage("DiscoverCharacteristicCallback", service.getUuid().toString(), characteristic.getUuid().toString());
                    }
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
//            unityDebugMessage("onCharacteristicChanged");
            byte[] data = characteristic.getValue();
            StringBuilder receiveData = new StringBuilder();

            // 必要であればデータの処理を行う
            for (byte i : data) {
//                unityDebugMessage(new String(i));
                receiveData.append(String.valueOf(i));
//                unityDebugMessage(String.valueOf(i));
            }
//            unityDebugMessage(receiveData + "\n");
//            unitySendMessage("onCharacteristicChanged", receiveData + "\n");
            unitySendMessage("onCharacteristicChanged", new String(data).replace(",", " ，"));
        }
    };

    // サービスを検出.
    @SuppressLint("MissingPermission")
    public void discoverServices() {
//        unityDebugMessage("discoverService");
        try {

            Thread.sleep(600); // 10秒(1万ミリ秒)間だけ処理を止める
        } catch (Exception e) {
            unityDebugMessage(e.toString());
        }
        if (gatt.discoverServices()) {
            unityDebugMessage("discoverService success");
//            unityDebugMessage(gatt.getDevice().getName());
        } else {
            unityDebugMessage("discoverService is failed");
        }

    }

    // Characteristicに対してNotificationの受信を要求.
    @SuppressLint("MissingPermission")
    public void requestNotification(String serviceUUID, String notificationUUID) {

        BluetoothGattService service = gatt.getService(UUID.fromString(serviceUUID));
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(notificationUUID));
        gatt.setCharacteristicNotification(characteristic, true);
        BluetoothGattDescriptor notification_descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
//        notification_descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(notification_descriptor);
        unityDebugMessage("finish requestNotification");
    }

    // discoverに関する対応の一つだったけど、ビルドできない原因の疑惑があるので一旦コメントアウト

//    @SuppressLint("MissingPermission")
//    private void discoverServices() {
//        if (!gattConnected) { //just a boolean
//            new Handler(Looper.getMainLooper()).post(new Runnable() {
//                @Override
//                public void run() {
//                    gatt.discoverServices();
//                }
//            });
//            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    discoverServices();
//                }
//            }, 5000);
//        }
//    }


    // Unity側にメッセージ通知.
    private void unitySendMessage(String... params) {
        String param = String.join(",", params);
//        UnityPlayer.UnitySendMessage(RECEIVE_OBJECT_NAME, "PluginMessageBU", param);
//        UnityPlayer.UnitySendMessage(RECEIVE_OBJECT_NAME, "PluginMessage", param);
        UnityPlayer.UnitySendMessage("GameObject", "ProviderLog", param);
    }

    private void unityDebugMessage(String message) {
//        UnityPlayer.UnitySendMessage(RECEIVE_OBJECT_NAME, "PluginLog", message);
        UnityPlayer.UnitySendMessage("GameObject", "ProviderLog", message);

    }

    // メッセージ送信.
    @SuppressLint("MissingPermission")
    public boolean sendMessage(String serviceUUID, String writeCharacteristicUUID, String message) {
        try {
            byte[] bytes = message.getBytes("UTF-8");
            // 書き込み.
            BluetoothGattService service = gatt.getService(UUID.fromString(serviceUUID));
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(writeCharacteristicUUID));
            characteristic.setValue(bytes);
            return gatt.writeCharacteristic(characteristic);
        } catch (Exception e) {
            unitySendMessage("ErrorCallback", e.getMessage());
        }
        return false;
    }
}