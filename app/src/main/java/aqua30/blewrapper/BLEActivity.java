package aqua30.blewrapper;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.List;

import aqua.blewrapper.connectionstates.ConnectionStateCallbacks;
import aqua.blewrapper.connectionstates.StateCodes;
import aqua.blewrapper.contracts.BluetoothManager;
import aqua.blewrapper.contracts.BluetoothViewContract;
import aqua.blewrapper.helper.BluetoothController;
import aqua.blewrapper.model.PreferenceClass;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import static aqua.blewrapper.connectionstates.StateCodes.RC_LOCATION;
import static aqua.blewrapper.helper.BluetoothController.log;

/**
 * Created by Saurabh on 27-12-2017.
 */
public class BLEActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks,
        BluetoothViewContract.ConnectionStateCallbacks, BluetoothViewContract.CommunicationCallbacks, BluetoothViewContract.ConnectedDeviceStateCallbacks {

    /* Views */
    private TextView dataView;
    private Button button_bleMode, button_manualMode;
    /* objects for BLE wrapper */
    private GoogleApiClient mGoogleApiClient;
    private BluetoothManager bluetoothManager;
    /* constants for demo working */
    public static final String deviceName = "deviceName";
    public static final String deviceAddress = "deviceAddress";
    public static final String Manual = "manual";
    public static final String BLE = "ble";
    public static final String mode = "mode";
    /* variables */
    private StringBuilder dataBuilder;
    private String connectedDeviceName, connectedDeviceAddress;
    private String selectedMode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_test);
        dataBuilder = new StringBuilder();
        dataView = findViewById(R.id.input_text);
        button_bleMode = findViewById(R.id.bleMode);
        button_manualMode = findViewById(R.id.manualMode);
        /*  getting any presaved or pre connected device name and address
        *   This details are automatically saved by the wrapper */
        connectedDeviceName = PreferenceClass.getInstance(this).getString(deviceName, "");
        connectedDeviceAddress = PreferenceClass.getInstance(this).getString(deviceAddress, "");
        selectedMode = PreferenceClass.getInstance(this).getString(mode, Manual);
        /* only for demo */
        setMode();
        /* required for ble wrapper */
        buildGoogleApiClient();
        /* ble wrapper main object via which we'll do all the operations */
        bluetoothManager = new BluetoothController(this);
        bluetoothManager.setGoogleApiClient(mGoogleApiClient);
        /* set this if you want to receive the callbacks related to bluetooth permission, gps state for
        *  device > M, bluetooth on device is connected or not*/
        bluetoothManager.setConnectionCallbacks(this);
        /* set this callback if you want to received the data from BLE device */
        bluetoothManager.setDataCallbacks(this);
        /* set this callback if you want to know device is connected or disconnected during runtime */
        bluetoothManager.setConnectedDeviceStateCallbacks(this);
        /* it checks all the required permission for Bluetooth activation */
        bluetoothManager.checkBluetoothRequirements();
    }

    private void setMode() {
        switch (selectedMode) {
            case Manual:
                button_manualMode.setBackgroundColor(ContextCompat.getColor(this, R.color.blue));
                button_bleMode.setBackgroundColor(ContextCompat.getColor(this, R.color.grey));
                break;
            case BLE:
                button_manualMode.setBackgroundColor(ContextCompat.getColor(this, R.color.grey));
                button_bleMode.setBackgroundColor(ContextCompat.getColor(this, R.color.blue));
                break;
        }
    }

    /* fragment to scan the ble devices. */
    private void scan() {
        ScanDeviceFragment scanDeviceFragment = new ScanDeviceFragment();
        scanDeviceFragment.setScanDeviceListener(new ScanDeviceFragment.ScanDeviceListener() {
            @Override
            public void onDeviceSelected(BluetoothDevice device) {
                /* once a device is connected, we can connect to it and stop scanning */
                if (device.getName() == null)
                    connectedDeviceName = "Device";
                connectedDeviceName = device.getName();
                connectedDeviceAddress = device.getAddress();
                PreferenceClass.getEditor(BLEActivity.this).putString(deviceName, connectedDeviceName).apply();
                PreferenceClass.getEditor(BLEActivity.this).putString(deviceAddress, connectedDeviceAddress).apply();
                setDataOnScreen("Connecting to " + connectedDeviceName + "...");
                bluetoothManager.connectToDevice(device);
            }
        });
        scanDeviceFragment.show(getFragmentManager(), "ScanDeviceFragment");
    }

    public void Connect(View view) {
        selectedMode = BLE;
        PreferenceClass.getEditor(this).putString(mode, selectedMode).apply();
        setMode();
        if (!connectedDeviceAddress.isEmpty()) {
            setDataOnScreen("Connecting to " + connectedDeviceName + "...");
            bluetoothManager.connectToDevice(connectedDeviceAddress);
        } else {
            setDataOnScreen("Scan required");
            scan();
        }
    }

    public void Disconnect(View view) {
        selectedMode = Manual;
        PreferenceClass.getEditor(this).putString(mode, selectedMode).apply();
        setMode();
        if (!connectedDeviceAddress.isEmpty()){
            setDataOnScreen("Manual mode activated. Disconnected "+ connectedDeviceAddress);
        }
        bluetoothManager.disconnect();
    }

    public void Clear(View view) {
        dataBuilder.setLength(0);
        dataView.setText("");
    }

    /* must call onActivityResult for bluetoothManager object to set the required results */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        bluetoothManager.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressLint("LongLogTag")
    protected synchronized void buildGoogleApiClient() {
        log( "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        /* if we want to know bluetooth on device is turned on or off */
        bluetoothManager.registerStateDetection();
    }

    @Override
    protected void onPause() {
        super.onPause();
        bluetoothManager.unregisterStateDetection();
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
        bluetoothManager.stopScanning();
        bluetoothManager.disconnect();
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {}

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        new AppSettingsDialog.Builder(this)
                .setTitle(getString(R.string.rationale_ask_again))
                .setPositiveButton(getString(R.string.setting))
                .setNegativeButton(getString(R.string.cancel))
                .setRequestCode(RC_LOCATION)
                .build()
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(StateCodes.RC_LOCATION)
    public void afterPermissionGranted() {
        try {
            if (!EasyPermissions.hasPermissions(this, StateCodes.PermissionLocation)) {
                EasyPermissions.requestPermissions(this,
                        getString(R.string.rationale_phone_state),
                        StateCodes.RC_LOCATION,
                        StateCodes.PermissionLocation);
            } else {
                bluetoothManager.checkLocationRequirements();
            }
        }catch (Exception e) {
            Log.e("scan permission error","error: "+e.getMessage());
        }
    }

    @Override
    public void askLocationPermission() {
        afterPermissionGranted();
    }

    @Override
    public void gpsState(@ConnectionStateCallbacks.GPSConnectionState int connectionState) {
        switch (connectionState) {
            case StateCodes.GPSEnabled:
                setDataOnScreen("All conditions for Bluetooth satisfied.");
                if (selectedMode.equals(BLE))
                    bluetoothManager.retryConnection();
                break;
            case StateCodes.GPSDisabled:
                bluetoothManager.checkLocationRequirements();
                break;
        }
    }

    @Override
    public void bleConnectionState(@ConnectionStateCallbacks.BLEConnectionState int connectionState) {
        switch (connectionState) {
            case StateCodes.STATE_ENABLED:
                /* not used. handled by default */
                break;
            case StateCodes.STATE_DISABLED:
                bluetoothManager.checkBluetoothRequirements();
                break;
        }
    }

    @Override
    public void permissionState(@ConnectionStateCallbacks.PermissionState int permissionState) {
        switch (permissionState) {
            case StateCodes.PermissionGranted:
                /* not used. handled by default */
                break;
            case StateCodes.PermissionDenied:
                bluetoothManager.checkLocationRequirements();
                break;
            case StateCodes.PermissionError:
                log("location permission error");
                break;
        }
    }

    @Override
    public void bleDeviceConnectionState(int deviceConnectionState) {
        switch (deviceConnectionState) {
            case StateCodes.BluetoothTurnedOn:
                Toast.makeText(BLEActivity.this, "BLE connected on device", Toast.LENGTH_SHORT).show();
                setDataOnScreen("Bluetooth turned on");
                bluetoothManager.retryConnection();
                break;
            case StateCodes.BluetoothTurnedOff:
                setDataOnScreen("Bluetooth turned off");
                Toast.makeText(BLEActivity.this, "BLE disconnected on device", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onDataReceived(String data) {
        setDataOnScreen("Data received: " + data);
    }

    @Override
    public void connectedDeviceState(int connectedDeviceState) {
        switch (connectedDeviceState) {
            case StateCodes.DeviceConnected:
                setDataOnScreen("Connected " + connectedDeviceName);
                break;
            case StateCodes.DeviceDisconnected:
                setDataOnScreen("Disconnected " + connectedDeviceName);
                bluetoothManager.retryConnection();
                break;
            case StateCodes.RetryConnection:
                if (!connectedDeviceAddress.isEmpty() && selectedMode.equals(BLE))
                    setDataOnScreen("Retrying connecting " + connectedDeviceName + "...");
                checkPreviousState();
                break;
        }
    }

    /* Checking once the BLE is connected that*/
    private void checkPreviousState() {
        if (selectedMode.equals(BLE)) {
            if (!connectedDeviceAddress.isEmpty()) {
                setDataOnScreen("Last saved device " + connectedDeviceName);
                bluetoothManager.setDiscoveryCallbacks(new BluetoothViewContract.DiscoveryCallbacks() {
                    @Override
                    public void onDeviceDiscovered(BluetoothDevice device) {
                        if (device.getAddress().equals(connectedDeviceAddress)) {
                            bluetoothManager.stopScanning();
                            bluetoothManager.setDiscoveryCallbacks(null);
                            setDataOnScreen("Connecting to " + connectedDeviceName + "...");
                            bluetoothManager.connectToDevice(device);
                        }
                    }

                    @Override
                    public void onDeviceDiscoveryStopped() {
                        if (!bluetoothManager.isDeviceConnected()) {
                            setDataOnScreen(connectedDeviceName + " is offline. Restart the device");
                        }
                    }
                });
                bluetoothManager.scanDevices();
            } else {
                setDataOnScreen("Connect a device.");
            }
        }
    }

    private void setDataOnScreen(String message) {
        dataBuilder.append(message + "\n");
        dataView.setText(dataBuilder.toString());
    }
}