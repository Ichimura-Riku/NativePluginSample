package com.example.bluetoothlelib;

import android.Manifest;
import android.app.Activity;
import android.content.Context;

import java.lang.Exception;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.*;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

import com.unity3d.player.UnityPlayer;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

public class BluetoothLE {
    //    private static final String RECEIVE_OBJECT_NAME = "BluetoothLEReceiver";
    private static final String RECEIVE_OBJECT_NAME = "ControllerInfoDisplayUI";
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("566fd0bd-0e40-42fa-a0c2-3a54433220f8");
    private BluetoothAdapter adapter;
    private BluetoothLeScanner scanner;

    private BluetoothGatt gatt;
    private BluetoothDevice device;

    static final int REQUEST_CODE = 1;
    //    private Context context;
    private Activity activity;

    private void checkBluetoothLEPermission() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.CAMERA
            }, REQUEST_CODE);
        }
    }

    // 初期化.
    public void initialize() {
        //Bluetoothアダプターを初期化
        unityDebugMessage("start plugin initialize");
        activity = UnityPlayer.currentActivity;
        BluetoothManager manager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        adapter = manager.getAdapter();

        scanner = adapter.getBluetoothLeScanner();

        unitySendMessage("InitializeCallback");
    }

    // スキャン開始.
    public void startScan() {
        try {

            ScanSettings.Builder scanSettings = new ScanSettings.Builder();
            scanSettings.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
            ScanSettings settings = scanSettings.build();
            // NOTE: Target Android9 API28まではマニフェスト追加のみで動作 Android10以降はユーザー許可が必要.
            checkBluetoothLEPermission();
            scanner.startScan(null, settings, scanCallback);
        }catch (Exception e){
            unityDebugMessage("BluetoothLE.startScan is Failed");
        }
    }


    // スキャンの停止.
    public void stopScan(Context context, Activity activity) {
        checkBluetoothLEPermission();
        scanner.stopScan(scanCallback);
    }

    // デバイス接続.
    public void connectToDevice(String address) {
        checkBluetoothLEPermission();
        device = adapter.getRemoteDevice(address);
        if (device == null) {
            return;
        }
        if (gatt != null) {
            gatt.disconnect();
        }
        gatt = device.connectGatt(UnityPlayer.currentActivity, true, gattCallback);
    }

    // デバイス接続解除.
    public void disconnectDevice() {
        checkBluetoothLEPermission();
        if (gatt != null) {
            gatt.disconnect();
            gatt = null;
        }
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            checkBluetoothLEPermission();
            if (result.getDevice() == null) {
                return;
            }

            // 検出したデバイス情報を通知.
            String deviceName = result.getDevice().getName();
            String address = result.getDevice().getAddress();
            unitySendMessage("ScanCallback", deviceName, address);
        }
    };

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int state) {
            if (state == BluetoothProfile.STATE_CONNECTED) {
                // 接続成功.
                unitySendMessage("ConnectCallback");
            } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                // 接続解除.
                unitySendMessage("DisconnectCallback");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // 検出したサービスとCharacteristicを通知.
                for (BluetoothGattService service : gatt.getServices()) {
                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                        unitySendMessage("DiscovereCharacteristicCallback", service.getUuid().toString(), characteristic.getUuid().toString());
                    }
                }
            }
        }
    };

    // サービスを検出.
    public void discoverServices() {
        checkBluetoothLEPermission();
        gatt.discoverServices();
    }

    // Characteristicに対してNotificationの受信を要求.
    public void requestNotification(String serviceUUID, String notificationUUID) {
        checkBluetoothLEPermission();
        BluetoothGattService service = gatt.getService(UUID.fromString(serviceUUID));
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(notificationUUID));

        gatt.setCharacteristicNotification(characteristic, true);
        BluetoothGattDescriptor notification_descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
        notification_descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(notification_descriptor);
    }

    // Unity側にメッセージ通知.
    private void unitySendMessage(String... params) {
        String param = String.join(",", params);
        UnityPlayer.UnitySendMessage(RECEIVE_OBJECT_NAME, "PluginMessageBU", param);
    }

    private void unityDebugMessage(String message) {
        UnityPlayer.UnitySendMessage(RECEIVE_OBJECT_NAME, "OnError", message);
    }

    // メッセージ送信.
    public boolean sendMessage(String serviceUUID, String writeCharacteristicUUID, String message) {
        checkBluetoothLEPermission();
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