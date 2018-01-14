package aqua.blewrapper.service;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import aqua.blewrapper.helper.BLESession;

import static aqua.blewrapper.connectionstates.StateCodes.ACTION_GATT_CONNECTED;
import static aqua.blewrapper.connectionstates.StateCodes.ACTION_GATT_DISCONNECTED;
import static aqua.blewrapper.connectionstates.StateCodes.ACTION_GATT_SERVICES_DISCOVERED;
import static aqua.blewrapper.connectionstates.StateCodes.EXTRA_DATA;
import static aqua.blewrapper.helper.BluetoothController.log;

/**
 * Created by Saurabh on 28-12-2017.
 */

public class BLEServiceHelper implements BLEServiceManager {

    private Context mContext;
    private BLESession bleSession;
    private BLEServiceCallbacks.ServiceCallbacks serviceCallback;

    public BLEServiceHelper(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void initialize() {
        disconnectService();
        bleSession = new BLESession(mContext.getApplicationContext());
        bleSession.observe((LifecycleOwner) mContext, BleObserver);
    }

    @Override
    public boolean connectTo(String deviceAddress) {
        return bleSession.connectTo(deviceAddress);
    }

    @Override
    public void disconnectService() {
        if (bleSession != null) {
            log("disconnecting bleSession..");
            bleSession.removeObserver(BleObserver);
            bleSession = null;
            serviceCallback = null;
        }
    }

    @Override
    public void setServiceCallback(BLEServiceCallbacks.ServiceCallbacks serviceCallback) {
        this.serviceCallback = serviceCallback;
    }

    private Observer BleObserver = new Observer() {
        @Override
        public void onChanged(@Nullable Object data) {
            if (data instanceof String) {
                switch ((String) data) {
                    case ACTION_GATT_CONNECTED:
                        if (serviceCallback != null)
                            serviceCallback.onGattConnectionState(ACTION_GATT_CONNECTED);
                        break;
                    case ACTION_GATT_DISCONNECTED:
                        if (serviceCallback != null)
                            serviceCallback.onGattConnectionState(ACTION_GATT_DISCONNECTED);
                        break;
                    case ACTION_GATT_SERVICES_DISCOVERED:
                        if (serviceCallback != null)
                            serviceCallback.onGattConnectionState(ACTION_GATT_SERVICES_DISCOVERED);
                        break;
                }
            } else if (data instanceof Intent) {
                try {
                    Intent bleDataIntent = (Intent) data;
                    String input = bleDataIntent.getStringExtra(EXTRA_DATA);
                    if (serviceCallback != null)
                        serviceCallback.onDataReceived(input);
                } catch (Exception exception) {
                    log("exception in getting data: " + exception.getMessage());
                }
            } else {
                log("unknow input in BLE observer");
            }
        }
    };
}
