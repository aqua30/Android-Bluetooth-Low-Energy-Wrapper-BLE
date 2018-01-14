package aqua.blewrapper.service;

import aqua.blewrapper.connectionstates.ConnectionStateCallbacks;

/**
 * Created by Saurabh on 28-12-2017.
 */

public interface BLEServiceCallbacks {

    interface PermissionCallback {
        void onPermissionResult(@ConnectionStateCallbacks.GPSConnectionState int gpsConnectionState);
    }

    interface ServiceCallbacks {
        void onDataReceived(String data);
        void onGattConnectionState(@ConnectionStateCallbacks.GattConnectionState String connectionState);
    }
}
