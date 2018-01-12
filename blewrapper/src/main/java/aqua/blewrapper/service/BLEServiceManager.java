package aqua.blewrapper.service;

/**
 * Created by HawkSafety(saurabh@hawksafety.com) on 28-12-2017.
 */

public interface BLEServiceManager {
    void initialize();
    boolean connectTo(String deviceAddress);
    void disconnectService();
    void setServiceCallback(BLEServiceCallbacks.ServiceCallbacks serviceCallback);
}
