package aqua.blewrapper.contracts;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;

import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Set;

import aqua.blewrapper.model.Device;

/**
 * Created by Saurabh on 27-12-2017.
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
