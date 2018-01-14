package aqua.blewrapper.helper;

import android.arch.lifecycle.LiveData;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import aqua.blewrapper.connectionstates.GattAttributes;
import aqua.blewrapper.connectionstates.StateCodes;
import aqua.blewrapper.service.BLEService;

import static android.content.Context.BIND_AUTO_CREATE;
import static aqua.blewrapper.connectionstates.StateCodes.ACTION_DATA_AVAILABLE;
import static aqua.blewrapper.connectionstates.StateCodes.ACTION_GATT_CONNECTED;
import static aqua.blewrapper.connectionstates.StateCodes.ACTION_GATT_DISCONNECTED;
import static aqua.blewrapper.connectionstates.StateCodes.ACTION_GATT_SERVICES_DISCOVERED;
import static aqua.blewrapper.helper.BluetoothController.log;

/**
 * Created by Saurabh on 02-01-2018.
 *
 * A session is created for the device in connection. The connection lasts
 * as long as this session lasts. For every connection, a new session is created.
 * Once we are done with BLE connection , we destroy this session.
 *
 */

public class BLESession extends LiveData {

    private BLEService mBLEService;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private ServiceConnection mServiceConnection;
    private Context mContext;

    private final String LIST_NAME = "NAME";
    private String mDeviceAddress;
    private final String LIST_UUID = "UUID";
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<>();
    public static HashMap<UUID,ArrayList<BluetoothGattCharacteristic>> servicemap = new HashMap<>();

    public BLESession(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    protected void onActive() {
        super.onActive();
        log("BLE session Active");
        Intent gattServiceIntent = new Intent(mContext, BLEService.class);
        mServiceConnection = new mServiceConnection();
        mContext.bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        mContext.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        log("BLE session Inactive");
        mContext.unregisterReceiver(mGattUpdateReceiver);
        mContext.unbindService(mServiceConnection);
        mServiceConnection = null;
        mBLEService.disconnect();
    }

    public boolean connectTo(String deviceAddress) {
        mDeviceAddress = deviceAddress;
        if (mBLEService != null) {
            log("connecting to device "+ deviceAddress);
            final boolean result = mBLEService.connect(mDeviceAddress);
            log("connected to device "+ result);
            return result;
        }
        return false;
    }

    private class mServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            log("service connected");
            mBLEService = ((BLEService.LocalBinder) service).getService();
            if (!mBLEService.initialize()) {
                Log.e(StateCodes.LOGTAG, "Unable to initialize Bluetooth");
            }
            // Automatically connects to the device upon successful start-up initialization.
            log("device address in connection: "+ mDeviceAddress);
            if (!mDeviceAddress.isEmpty()) {
                mBLEService.connect(mDeviceAddress);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            log("service disconnected");
            mBLEService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (ACTION_GATT_CONNECTED.equals(action)) {
                setValue(ACTION_GATT_CONNECTED);
            } else if (ACTION_GATT_DISCONNECTED.equals(action)) {
                setValue(ACTION_GATT_DISCONNECTED);
            } else if (ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBLEService.getSupportedGattServices());
                if (mGattCharacteristics != null) {
                    BluetoothGattCharacteristic characteristic = null;
                    for (Map.Entry<UUID, ArrayList<BluetoothGattCharacteristic>> entry : servicemap.entrySet()) {
                        UUID key = entry.getKey();
                        ArrayList<BluetoothGattCharacteristic> value = entry.getValue();
                        /* this part was specific to our requirement. One can modify or delete this part
                        *  as per their requirement.
                        * */
                        if(key.toString().contains("1809") && value.size() > 0) {
                            characteristic = value.get(1);
                            Log.e("characteristics", characteristic.getValue()+"");
                            break;
                        }
                    }
                    final int charaProp = characteristic.getProperties();
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                        if (mNotifyCharacteristic != null) {
                            mBLEService.setCharacteristicIndication(mNotifyCharacteristic, false);
                            mNotifyCharacteristic = null;
                        }
                        log("reading characteristics");
                        mBLEService.readCharacteristic(characteristic);
                    }
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        mNotifyCharacteristic = characteristic;
                        mBLEService.setCharacteristicNotification(characteristic, true);
                    }
                } else {
                    Log.e("characteristics", "mGattCharacteristics == null");
                }
                setValue(ACTION_GATT_SERVICES_DISCOVERED);
            } else if (ACTION_DATA_AVAILABLE.equals(action)) {
                setValue(intent);
            }
        }
    };

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = "Unknown Service";
        String unknownCharaString = "Unknown Characteristics";
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<>();
        mGattCharacteristics = new ArrayList<>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(LIST_NAME, GattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<>();
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(LIST_NAME, GattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
            servicemap.put(gattService.getUuid(),charas);
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_GATT_CONNECTED);
        intentFilter.addAction(ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}