package org.stream.split.voicenotification.BussinessLayer;

import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;

/**
 * Created by split on 2015-11-01.
 */
public class AppInfo {

    boolean mIsSelected = false;
    String mPackageName;

    public void setPackageName(String mPackageName) {
        this.mPackageName = mPackageName;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public boolean IsSelected() {
        return mIsSelected;
    }
    public void setSelected(Boolean bool)
    {
        mIsSelected = bool;
    }

    public AppInfo(String packageName, boolean selected)
    {
        mPackageName= packageName;
        mIsSelected = selected;
    }
    public AppInfo()
    {

    }
}
