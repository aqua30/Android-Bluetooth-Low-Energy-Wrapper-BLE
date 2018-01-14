package aqua.blewrapper.connectionstates;

import android.support.annotation.IntDef;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static aqua.blewrapper.connectionstates.StateCodes.BluetoothTurnedOff;
import static aqua.blewrapper.connectionstates.StateCodes.BluetoothTurnedOn;
import static aqua.blewrapper.connectionstates.StateCodes.DeviceConnected;
import static aqua.blewrapper.connectionstates.StateCodes.DeviceDisconnected;
import static aqua.blewrapper.connectionstates.StateCodes.RetryConnection;

/**
 * Created by Saurabh on 27-12-2017.
 */

public class ConnectionStateCallbacks {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({StateCodes.STATE_ENABLED, StateCodes.STATE_DISABLED, StateCodes.STATE_ENABLED_ALREADY, StateCodes.STATE_DISBALED_ALREADY})
    public @interface BLEConnectionState{}

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({StateCodes.GPSEnabled, StateCodes.GPSDisabled})
    public @interface GPSConnectionState {}

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({StateCodes.PermissionGranted, StateCodes.PermissionDenied, StateCodes.PermissionError})
    public @interface PermissionState {}

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({StateCodes.ACTION_DATA_AVAILABLE, StateCodes.ACTION_GATT_CONNECTED, StateCodes.ACTION_GATT_DISCONNECTED, StateCodes.ACTION_GATT_SERVICES_DISCOVERED})
    public @interface GattConnectionState {}

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({BluetoothTurnedOn, BluetoothTurnedOff})
    public @interface DeviceBLEConnectionState {}

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DeviceConnected, DeviceDisconnected, RetryConnection})
    public @interface ConnectedDeviceConnectionState {}
}
