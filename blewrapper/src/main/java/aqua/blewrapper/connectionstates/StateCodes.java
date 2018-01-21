package aqua.blewrapper.connectionstates;

import android.Manifest;

import aqua.blewrapper.BuildConfig;

/**
 * Created by Saurabh on 27-12-2017.
 *
 * Constants used in the wrapper.
 */

public class StateCodes {

    /* devices' bluetooth connection states */
    public static final int STATE_ENABLED = 101;
    public static final int STATE_DISABLED = 102;
    public static final int STATE_ENABLED_ALREADY = 103;
    public static final int STATE_DISBALED_ALREADY = 104;
    /* permission results */
    public static final int PermissionGranted = 201;
    public static final int PermissionError = 202;
    public static final int PermissionDenied = 203;
    /* gps connection state */
    public static final int GPSEnabled = 204;
    public static final int GPSDisabled = 205;
    /* runtime permission codes */
    public static final int Request_Loction_Resolution = 302;
    public static final int Request_Enable_Bluetooth = 301;
    public static String[] PermissionLocation = {Manifest.permission.ACCESS_FINE_LOCATION};
    public static final int RC_LOCATION = 126;
    /* BLEGatt connection state */
    public final static String ACTION_GATT_CONNECTED = BuildConfig.APPLICATION_ID+".bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = BuildConfig.APPLICATION_ID+".bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = BuildConfig.APPLICATION_ID+".bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = BuildConfig.APPLICATION_ID+".bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = BuildConfig.APPLICATION_ID+".bluetooth.le.EXTRA_DATA";
    /* bluetooth of device */
    public static final int BluetoothTurnedOn = 401;
    public static final int BluetoothTurnedOff = 402;
    /* ble device which is connected and in use. its connection state */
    public static final int DeviceConnected = 501;
    public static final int DeviceDisconnected = 502;
    public static final int RetryConnection = 503;
    /* log tag */
    public static String LOGTAG = "log_bluetooth_manager";
    /* retry connection count for connected device */
    public static final int RetryCount = 3;
    /* scanning period defined for ble scan */
    private static long ScanPeriod = 10;

    public static long getScanPeriod() {
        return ScanPeriod * 1000;
    }

    /* @param scanPeriod should be integer value */
    public static void setScanPeriod(long scanPeriod) {
        ScanPeriod = scanPeriod;
    }
}
