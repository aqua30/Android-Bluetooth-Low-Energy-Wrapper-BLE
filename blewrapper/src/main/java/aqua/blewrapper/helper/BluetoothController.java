package aqua.blewrapper.helper;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

import aqua.blewrapper.connectionstates.ConnectionStateCallbacks;
import aqua.blewrapper.connectionstates.StateCodes;
import aqua.blewrapper.contracts.BluetoothManager;
import aqua.blewrapper.contracts.BluetoothViewContract;
import aqua.blewrapper.model.Device;
import aqua.blewrapper.service.BLEServiceCallbacks;
import aqua.blewrapper.service.BLEServiceHelper;
import aqua.blewrapper.service.BLEServiceManager;

import static android.app.Activity.RESULT_OK;
import static aqua.blewrapper.connectionstates.StateCodes.ACTION_GATT_CONNECTED;
import static aqua.blewrapper.connectionstates.StateCodes.ACTION_GATT_DISCONNECTED;
import static aqua.blewrapper.connectionstates.StateCodes.ACTION_GATT_SERVICES_DISCOVERED;
import static aqua.blewrapper.connectionstates.StateCodes.BluetoothTurnedOff;
import static aqua.blewrapper.connectionstates.StateCodes.BluetoothTurnedOn;
import static aqua.blewrapper.connectionstates.StateCodes.DeviceConnected;
import static aqua.blewrapper.connectionstates.StateCodes.DeviceDisconnected;
import static aqua.blewrapper.connectionstates.StateCodes.GPSDisabled;
import static aqua.blewrapper.connectionstates.StateCodes.GPSEnabled;
import static aqua.blewrapper.connectionstates.StateCodes.LOGTAG;
import static aqua.blewrapper.connectionstates.StateCodes.PermissionGranted;
import static aqua.blewrapper.connectionstates.StateCodes.Probe_UK;
import static aqua.blewrapper.connectionstates.StateCodes.Probe_US;
import static aqua.blewrapper.connectionstates.StateCodes.RC_LOCATION;
import static aqua.blewrapper.connectionstates.StateCodes.Request_Enable_Bluetooth;
import static aqua.blewrapper.connectionstates.StateCodes.Request_Loction_Resolution;
import static aqua.blewrapper.connectionstates.StateCodes.RetryConnection;
import static aqua.blewrapper.connectionstates.StateCodes.STATE_DISABLED;
import static aqua.blewrapper.connectionstates.StateCodes.getScanPeriod;

/**
 * Created by Saurabh on 27-12-2017.
 *
 * Implementation class for BluetoothManager.
 * This class holds complete control of all operations.
 * It manages the life cycle for BLE operations.
 */

@SuppressWarnings("deprecation")
public class BluetoothController implements BluetoothManager, BLEServiceCallbacks.PermissionCallback,
        BLEServiceCallbacks.ServiceCallbacks {

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothViewContract.ConnectionStateCallbacks connectionStateCallbacks;
    private BluetoothViewContract.DiscoveryCallbacks discoveryCallbacks;
    private BluetoothViewContract.CommunicationCallbacks communicationCallbacks;
    private BluetoothViewContract.ConnectedDeviceStateCallbacks connectedDeviceStateCallbacks;
    private BLEServiceManager bleServiceManager;
    private BLEStateReceiver bleStateReceiver;
    /* weak reference is provided to prevent any context leak. */
    private WeakReference<Activity> mActivity;
    /* only required for devices running on M or above */
    private static GoogleApiClient googleApiClient;
    /* the last connected device the wrapper communicated with */
    private Device lastConnectedDevice;
    /* indicates if the scanning is started already or not. */
    private boolean isScanRequested = false;
    /* indicates if a device is connected or not. */
    private boolean isDeviceConnected = false;
    /* indicates that retry should be done if connection fails with the device. */
    private boolean retryPolicy = false;
    private Set<BluetoothDevice> scannedDevices;
    private Handler mHandler;

    public BluetoothController(Activity context) {
        this.mActivity = new WeakReference<>(context);
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bleServiceManager = new BLEServiceHelper(mActivity.get());
        scannedDevices = new HashSet<>();
        lastConnectedDevice = new Device(mActivity.get());
    }

    @Override
    public void setConnectionCallbacks(BluetoothViewContract.ConnectionStateCallbacks connectionCallbacks) {
        this.connectionStateCallbacks = connectionCallbacks;
    }

    @Override
    public void setDiscoveryCallbacks(BluetoothViewContract.DiscoveryCallbacks discoveryCallbacks) {
        this.discoveryCallbacks = discoveryCallbacks;
    }

    @Override
    public void setDataCallbacks(BluetoothViewContract.CommunicationCallbacks communicationCallbacks) {
        this.communicationCallbacks = communicationCallbacks;
    }

    @Override
    public void setConnectedDeviceStateCallbacks(BluetoothViewContract.ConnectedDeviceStateCallbacks connectedDeviceStateCallbacks) {
        this.connectedDeviceStateCallbacks = connectedDeviceStateCallbacks;
    }

    @Override
    public void registerStateDetection() {
        log("State detection receiver connected.");
        bleStateReceiver = new BLEStateReceiver(mActivity.get());
        bleStateReceiver.observe((LifecycleOwner) mActivity.get(), BleStateObserver);
    }

    @Override
    public void unregisterStateDetection() {
        if (bleStateReceiver != null) {
            log("State detection receiver removed.");
            bleStateReceiver.removeObserver(BleStateObserver);
        }
    }

    @Override
    public void scanDevices() {
        if (mBluetoothAdapter.isEnabled()) {
            scannedDevices.clear();
            if (isScanRequested) {
                log("Scanning was in process. Stopped");
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
            if (isDeviceConnected) {
                log("Already " + lastConnectedDevice.getDeviceName() + " connected. Disconnect first.");
                return;
            }
            isScanRequested = true;
            log("Scanning started");
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            if (mHandler != null) {
                mHandler.removeCallbacks(scanRunnable);
                mHandler = null;
            }
            if (!retryPolicy) {
                mHandler = new Handler();
                mHandler.postDelayed(scanRunnable, getScanPeriod());
            }
        } else {
            Log.e(LOGTAG, "BLE is disbaled");
            checkBluetoothRequirements();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void stopScanning() {
        retryPolicy = false;
        if (isScanRequested) {
            log("Scanning stopped");
            if (mHandler != null) {
                mHandler.removeCallbacks(scanRunnable);
                mHandler = null;
            }
            isScanRequested = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            if (discoveryCallbacks != null) {
                discoveryCallbacks.onDeviceDiscoveryStopped();
            }
        }
    }

    @Override
    public void checkBluetoothRequirements() {
        if (!mBluetoothAdapter.isEnabled()) {
            Log.e(LOGTAG, "Enabling BLE");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mActivity.get().startActivityForResult(enableBtIntent, Request_Enable_Bluetooth);
        }else {
            checkLocationRequirements();
        }
    }

    @Override
    public void checkLocationRequirements() {
        if (BluetoothPermission.isLocationPermissionGranted(mActivity.get())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                BluetoothPermission.createLocationRequest(mActivity.get(), this, googleApiClient);
            } else {
                log("Build.Version < M, GPS permission not required");
                if (connectionStateCallbacks != null)
                    connectionStateCallbacks.gpsState(GPSEnabled);
            }
        } else {
            log("request for permission");
            if (connectionStateCallbacks != null)
                connectionStateCallbacks.askLocationPermission();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(LOGTAG, "onActivityResult");
        switch (requestCode) {
            /* when bluetooth start intent returns */
            case Request_Enable_Bluetooth:
                if (resultCode == RESULT_OK) {
                    checkLocationRequirements();
                } else {
                    Log.e(LOGTAG, "BLE not enabled");
                    if (connectionStateCallbacks != null)
                        connectionStateCallbacks.bleConnectionState(STATE_DISABLED);
                }
                break;
            /* when GPS resolution returns */
            case Request_Loction_Resolution:
                if (resultCode == RESULT_OK) {
                    log("GPS enabled");
                    if (connectionStateCallbacks != null)
                        connectionStateCallbacks.gpsState(GPSEnabled);
                } else {
                    log("GPS disabled");
                    if (connectionStateCallbacks != null)
                        connectionStateCallbacks.gpsState(GPSDisabled);
                }
                break;
            /* when Location permission granted */
            case RC_LOCATION:
                if (resultCode == RESULT_OK) {
                    log("location permission granted");
                    if (connectionStateCallbacks != null)
                        connectionStateCallbacks.permissionState(PermissionGranted);
                } else {
                    log("location permission denied");
                    if (connectionStateCallbacks != null)
                        connectionStateCallbacks.permissionState(StateCodes.PermissionDenied);
                }
                break;
        }
    }

    @Override
    public Set<BluetoothDevice> getPairedDevices() {
        if (mBluetoothAdapter != null) return mBluetoothAdapter.getBondedDevices();
        return null;
    }

    @Override
    public void connectToDevice(Object device) {
        stopScanning();
        if (mBluetoothAdapter.isEnabled()) {
            String deviceAddress = "";
            BluetoothDevice bleDevice = null;
            if (device instanceof BluetoothDevice) {
                bleDevice = (BluetoothDevice) device;
                deviceAddress = bleDevice.getAddress();
            } else if (device instanceof String) {
                deviceAddress = (String) device;
            }
            if (isDeviceConnected) {
                if (deviceAddress.equals(lastConnectedDevice.getDeviceAddress())) {
                    return;
                } else {
                    bleServiceManager.disconnectService();
                    isDeviceConnected = false;
                }
            }
            lastConnectedDevice.setDeviceName(bleDevice != null ? bleDevice.getName() : "BLE Device");
            lastConnectedDevice.setDeviceAddress(deviceAddress);
            bleServiceManager.initialize();
            bleServiceManager.setServiceCallback(this);
            bleServiceManager.connectTo(deviceAddress);
        } else {
            checkBluetoothRequirements();
        }
    }

    @Override
    public void disconnect() {
        retryPolicy = false;
        stopScanning();
        if (isDeviceConnected) {
            bleServiceManager.disconnectService();
            isDeviceConnected = false;
        }
    }

    @Override
    public boolean isDeviceConnected() {
        return isDeviceConnected;
    }

    @Override
    public void setGoogleApiClient(GoogleApiClient googleApiClient) {
        this.googleApiClient = googleApiClient;
    }

    @Override
    public void retryConnection() {
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            if (mActivity != null && BluetoothPermission.isLocationPermissionGranted(mActivity.get())) {
                log("retry callback");
                retryPolicy = true;
                if (connectedDeviceStateCallbacks != null)
                    connectedDeviceStateCallbacks.connectedDeviceState(RetryConnection);
            } else {
                checkLocationRequirements();
            }
        } else {
            log("mBluetoothAdapter = null");
//            checkBluetoothRequirements();
        }
    }

    @Override
    public Device getSavedDevice() {
        return lastConnectedDevice;
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    log("Device discoverd");
                    if (device.getName() != null && device.getName().contains(Probe_UK) ||
                            device.getName() != null && device.getName().contains(Probe_US)) {
                        if (!scannedDevices.contains(device)) {
                            log("device: "+ device.getName() + ", " + device.getAddress());
                            if (discoveryCallbacks != null) {
                                discoveryCallbacks.onDeviceDiscovered(device);
                            }
                            scannedDevices.add(device);
                        }
                    }
                }
            };

    @Override
    public void onPermissionResult(@ConnectionStateCallbacks.GPSConnectionState int permissionResult) {
        switch (permissionResult) {
            case GPSEnabled:
                log("GPS enabled after permission");
                if (connectionStateCallbacks != null)
                    connectionStateCallbacks.gpsState(GPSEnabled);
                break;
            case GPSDisabled:
                log("GPS disabled after permission");
                if (connectionStateCallbacks != null)
                    connectionStateCallbacks.gpsState(GPSDisabled);
                break;
        }
    }

    public static void log(String message) {
        Log.e(LOGTAG, message);
    }

    @Override
    public void onDataReceived(String data) {
        log("Data received: "+ data);
        if (communicationCallbacks != null)
            communicationCallbacks.onDataReceived(data);
    }

    @Override
    public void onGattConnectionState(@ConnectionStateCallbacks.GattConnectionState String connectionState) {
        switch (connectionState) {
            case ACTION_GATT_CONNECTED:
                log("ACTION_GATT_CONNECTED");
                isDeviceConnected = true;
                if (connectedDeviceStateCallbacks != null)
                    connectedDeviceStateCallbacks.connectedDeviceState(DeviceConnected);
                break;
            case ACTION_GATT_DISCONNECTED:
                log("ACTION_GATT_DISCONNECTED");
                isDeviceConnected = false;
                bleServiceManager.disconnectService();
                if (connectedDeviceStateCallbacks != null)
                    connectedDeviceStateCallbacks.connectedDeviceState(DeviceDisconnected);
                break;
            case ACTION_GATT_SERVICES_DISCOVERED:
                log("ACTION_GATT_SERVICES_DISCOVERED");
                break;
        }
    }

    private Observer BleStateObserver = new Observer() {
        @Override
        public void onChanged(@Nullable Object state) {
            if (state instanceof Integer) {
                switch ((int)state) {
                    case BluetoothTurnedOn:
                        log("BLE turned on");
                        if (connectionStateCallbacks != null)
                            connectionStateCallbacks.bleDeviceConnectionState(BluetoothTurnedOn);
                        break;
                    case BluetoothTurnedOff:
                        log("BLE turned off");
                        isDeviceConnected = false;
                        stopScanning();
                        bleServiceManager.disconnectService();
                        if (connectionStateCallbacks != null)
                            connectionStateCallbacks.bleDeviceConnectionState(BluetoothTurnedOff);
                        break;
                }
            }
        }
    };

    private Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            stopScanning();
        }
    };
}
