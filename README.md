# Android-Bluetooth-Low-Energy-Wrapper-BLE
This sample project demostrate how an Android app communicate with a Bluetooth Low Energy Device.

## Purpose of BLE Wrapper
While integrating the BLE device with Android app, the overall documentation was clustering the code.
So, it was felt that it would be better to create a callback mechanism wherein we just set up the BLE device with a single object.
Because its a wrapper, it mainly follows the google documentation and has some added scenarios over it.

## What it simplifies
**_BluetoothManager_** is the object that the app deals with. It helps in handling the following
- handles the permission for Android device running both above and below Marshmallow
- detects the device bluetooth state (ON/OFF)
- automatically saves the last connected device and we can retrieve it anytime as
```
Device lastDevice = new Device(context);
Log.d("last connected device", String.format("name: %s, "address: %s", lastDevice.getDeviceName(), lastDevice.getDeviceAddress()));
```
- a clean pop for scan devices and connect to the selected device.
- creates a session for a connected device and destroy it when not in use or not communicating.
- returns a list of paired devices
- returns if a device is connected or not.

## Easy set up
Create an instance of *BluetoothManager* object and get started as
```
bluetoothManager = new BluetoothController(this);
bluetoothManager.setGoogleApiClient(mGoogleApiClient);

/* must call onActivityResult for bluetoothManager object to set the required results */
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
   super.onActivityResult(requestCode, resultCode, data);
   bluetoothManager.onActivityResult(requestCode, resultCode, data);
}
```

## Callbacks provided
The wrapper provides callbacks for managing the various actions to get running with a BLE device.

**Permission Callbacks:**
Set this to observe any state related to bluetooth permission, location permission(for device > M), device bluetooth state and 
do we need to ask for permission, set the *ConnectionCallback* as
```
bluetoothManager.setConnectionCallbacks(this);

@Override
public void askLocationPermission() {}

@Override
public void gpsState(int connectionState) {}

@Override
public void bleConnectionState(int connectionState) {}

@Override
public void permissionState(int permissionState) {}

@Override
public void bleDeviceConnectionState(int deviceConnectionState) {}
```

**Communication Callback:**
For to receive data in your parent Activity/Fragment, set 
```
bluetoothManager.setDataCallbacks(this);

@Override
public void onDataReceived(String data) {
  log("Data received: "+ data);
}
```

**Discovery Callback:**
Mainly used in ScanDeviceFragment, can be used to find BLE devices and connect to them.
```
bluetoothManager.setDiscoveryCallbacks(this);

@Override
public void onDeviceDiscovered(BluetoothDevice device) {}

@Override
public void onDeviceDiscoveryStopped() {}
```

**ConnectedDevice Callbacks:**
Set this callback to know if device is connected or disconnected during runtime
```
bluetoothManager.setConnectedDeviceStateCallbacks(this);

@Override
public void connectedDeviceState(int connectedDeviceState) {}
```

## Optional feature

**Bluetooth State Detection:**
Set this to know bluetooth on device is turned ON or OFF at runtime. This is a broadcast receiver with defined codes and must be
unregistered in _onPause()_.
```
@Override
protected void onResume() {
  super.onResume();
  bluetoothManager.registerStateDetection();
}

```

**Scan Device Fragment**
A fragment which is just required to be called and it'll handle the scan itself. It mainly scans for 10 seconds and then it 
stops scanning but we can rescan.
```
ScanDeviceFragment scanDeviceFragment = new ScanDeviceFragment();
scanDeviceFragment.setScanDeviceListener(new ScanDeviceFragment.ScanDeviceListener() {
    @Override
    public void onDeviceSelected(BluetoothDevice device) {
        /* once a device is connected, we can connect to it and stop scanning */
    });
scanDeviceFragment.show(getFragmentManager(), "ScanDeviceFragment");
```
