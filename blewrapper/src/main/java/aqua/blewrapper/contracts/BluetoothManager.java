package aqua.blewrapper.contracts;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;

import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Set;

import aqua.blewrapper.model.Device;

/**
 * Created by Saurabh on 27-12-2017.
 *
 * This is main entry point for setting up and running the BLE communication.
 * Main interaface which is used for all the BLE related operation.
 * Any activity/fragment wants to communicate with BLE has to use this
 * class object to do any BLE related task.
 */

public interface BluetoothManager<T> {
    void setConnectionCallbacks(BluetoothViewContract.ConnectionStateCallbacks connectionCallbacks);
    void setDiscoveryCallbacks(BluetoothViewContract.DiscoveryCallbacks discoveryCallbacks);
    void setDataCallbacks(BluetoothViewContract.CommunicationCallbacks communicationCallbacks);
    void setConnectedDeviceStateCallbacks(BluetoothViewContract.ConnectedDeviceStateCallbacks connectedDeviceStateCallbacks);
    void registerStateDetection();
    void unregisterStateDetection();
    void scanDevices();
    void stopScanning();
    void connectToDevice(T device);
    void disconnect();
    boolean isDeviceConnected();
    void checkBluetoothRequirements();
    void checkLocationRequirements();
    void onActivityResult(int requestCode, int resultCode, Intent data);
    Set<BluetoothDevice> getPairedDevices();
    void setGoogleApiClient(GoogleApiClient googleApiClient);
    void retryConnection();
    Device getSavedDevice();
}
