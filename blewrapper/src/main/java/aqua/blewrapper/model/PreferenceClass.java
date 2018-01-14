package aqua.blewrapper.model;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Saurabh on 04-01-2018.
 */

public class PreferenceClass {

    private static SharedPreferences sharedPreferences;

    public static SharedPreferences getInstance(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences("ble", Context.MODE_PRIVATE);
        }
        return sharedPreferences;
    }

    public static SharedPreferences.Editor getEditor(Context context) {
        return getInstance(context).edit();
    }
}
