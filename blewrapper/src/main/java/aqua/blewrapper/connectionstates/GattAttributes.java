package aqua.blewrapper.connectionstates;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes.
 */
public class GattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String DATA_NOTIFY = "c9320001-d7f2-4d5e-7953-e086be6a2dc2";

    static {
        // Services.
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        // Characteristics.
        attributes.put(CLIENT_CHARACTERISTIC_CONFIG, "Manufacturer Name String");
        attributes.put(DATA_NOTIFY, "Data Service");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
