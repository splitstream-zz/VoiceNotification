package Settings.Conditions;

import android.content.Context;

import Settings.Settings;

/**
 * Created by split on 2016-03-01.
 */
public abstract class BaseCondition {
    private final Context mContext;
    boolean mValue;

    public Context getContext() {
        return mContext;
    }
    public boolean getValue() {
        return mValue;
    }
    public void setValue(boolean mValue) {
        this.mValue = mValue;
    }

    protected BaseCondition(boolean value, Context context)
    {
        mContext = context;
        mValue = value;
    }

    public abstract boolean evaluate();
}
