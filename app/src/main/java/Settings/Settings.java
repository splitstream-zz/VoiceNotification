package Settings;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashMap;

import Settings.Conditions.BaseCondition;

/**
 * Created by split on 2016-03-01.
 */
public class Settings implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static Settings SINGLETON;
    Context mContext;
    HashMap<String,Class<? extends BaseCondition>> mConditions = new HashMap<>();

    public Settings getInstance(Context context)
    {
        if(SINGLETON == null)
            SINGLETON = new Settings(context);
        return SINGLETON;
    }
    private Settings(Context context)
    {
        mContext = context;
        PreferenceManager.getDefaultSharedPreferences(context);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }
}
