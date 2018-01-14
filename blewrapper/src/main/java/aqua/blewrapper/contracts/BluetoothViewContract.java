package aqua.blewrapper.contracts;

import android.bluetooth.BluetoothDevice;

/**
 * Created by (saurabh) on 28-12-2017.
 *
 * Callback for views as per the BLE operations.
 */

public interface BluetoothViewContract {

    /* callbacks related to bluetooth turning ON requirements */
    interface ConnectionStateCallbacks{
        void askLocationPermission();
        void gpsState(@aqua.blewrapper.connectionstates.ConnectionStateCallbacks.GPSConnectionState int connectionState);
        void bleConnectionState(@aqua.blewrapper.connectionstates.ConnectionStateCallbacks.BLEConnectionState int connectionState);
        void permissionState(@aqua.blewrapper.connectionstates.ConnectionStateCallbacks.PermissionState int permissionState);
        void bleDeviceConnectionState(@aqua.blewrapper.connectionstates.ConnectionStateCallbacks.DeviceBLEConnectionState int deviceConnectionState);
    }
    /* callbacks related to scanning */
    interface DiscoveryCallbacks{
        void onDeviceDiscovered(BluetoothDevice device);
        void onDeviceDiscoveryStopped();
    }

    interface CommunicationCallbacks{
        void onDataReceived(String data);
    }

    interface ConnectedDeviceStateCallbacks {
        void connectedDeviceState(@aqua.blewrapper.connectionstates.ConnectionStateCallbacks.ConnectedDeviceConnectionState
                                          int connectedDeviceState);
    }
}
