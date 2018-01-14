package aqua.blewrapper.helper;

import android.arch.lifecycle.LiveData;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import static aqua.blewrapper.connectionstates.StateCodes.BluetoothTurnedOff;
import static aqua.blewrapper.connectionstates.StateCodes.BluetoothTurnedOn;

/**
 * Created by Saurabh on 03-01-2018.
 *
 * This is a LiveData for detecting the Bluetooth connectivity on device at runtime.
 */

public class BLEStateReceiver extends LiveData {

    private Context mContext;

    public BLEStateReceiver(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    protected void onActive() {
        super.onActive();
        mContext.registerReceiver(BluetoothStateReceiver,new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        mContext.unregisterReceiver(BluetoothStateReceiver);
    }

    private BroadcastReceiver BluetoothStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        setValue(BluetoothTurnedOff);
                        break;
                    case BluetoothAdapter.STATE_ON:
                        setValue(BluetoothTurnedOn);
                        break;
                }
            }
        }
    };
}
