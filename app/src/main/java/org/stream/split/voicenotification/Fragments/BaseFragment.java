package org.stream.split.voicenotification.Fragments;

import android.app.Fragment;

import org.stream.split.voicenotification.Logging.BaseLogger;

/**
 * Created by split on 2016-01-04.
 */
public abstract class BaseFragment extends Fragment{

    String mTitle = "Voice Notification";
    boolean mIsModified;
    public static BaseLogger LOGGER = BaseLogger.getInstance();

    public String getTitle() {
        return mTitle;
    }

    protected void setTitle(String title) {
        this.mTitle = title;
    }
    public boolean isModified()
    {
        return mIsModified;
    }
    protected void setIsModified(boolean isModified)
    {
        mIsModified = isModified;
    }

    public void finish()
    {}


}
