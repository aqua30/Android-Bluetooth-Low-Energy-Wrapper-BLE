package aqua.blewrapper.helper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import aqua.blewrapper.service.BLEServiceCallbacks;

import static aqua.blewrapper.connectionstates.StateCodes.GPSDisabled;
import static aqua.blewrapper.connectionstates.StateCodes.GPSEnabled;
import static aqua.blewrapper.connectionstates.StateCodes.LOGTAG;
import static aqua.blewrapper.connectionstates.StateCodes.Request_Loction_Resolution;

/**
 * Created by Saurabh on 27-12-2017.
 */

public class BluetoothPermission {

    public BluetoothPermission() {}

    public static void createLocationRequest(final Activity mActivityContext,
                                             final BLEServiceCallbacks.PermissionCallback permissionCallback,
                                             final GoogleApiClient mGoogleApiClient) {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @SuppressLint("LongLogTag")
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.e(LOGTAG, "GPS Enabled.");
                        if (permissionCallback != null)
                            permissionCallback.onPermissionResult(GPSEnabled);
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.e(LOGTAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");
                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(mActivityContext, Request_Loction_Resolution);
                        } catch (IntentSender.SendIntentException e) {
                            Log.e(LOGTAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.e(LOGTAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        if (permissionCallback != null)
                            permissionCallback.onPermissionResult(GPSDisabled);
                        break;
                }
            }
        });
    }

    public static boolean isLocationPermissionGranted (Context context) {
        return PackageManager.PERMISSION_GRANTED ==
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
    }
}
