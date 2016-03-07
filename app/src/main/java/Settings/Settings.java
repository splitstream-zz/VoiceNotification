package Settings;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import org.stream.split.voicenotification.R;

import java.util.HashMap;

import Settings.Conditions.ActiveCallCondition;
import Settings.Conditions.BaseCondition;

/**
 * Created by split on 2016-03-01.
 */
public class Settings implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static Settings SINGLETON;
    Context mContext;
    static HashMap<String,Class<? extends BaseCondition>> CONDITIONS = new HashMap<>();
    HashMap<String,? extends BaseCondition> mConditions = new HashMap<>();

    public Settings getInstance(Context context)
    {
        if(SINGLETON == null)
            SINGLETON = new Settings(context);
        return SINGLETON;
    }
    private Settings(Context context)
    {
        initialize();
        mContext = context;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }
    public void initialize()
    {
        Resources res = mContext.getResources();
        CONDITIONS.put(res.getString(R.string.SPEAK_DURING_ACTIVE_CALL_PREFERENCE_KEY), ActiveCallCondition.class);
    }

    public void addCondition(String key,boolean value)
    {
        Class<? extends BaseCondition> condition = mConditions.get(key);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }
}
