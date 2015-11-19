package org.stream.split.voicenotification.Enities;

/**
 * Created by split on 2015-11-01.
 */
public class AppInfoEntity {

    boolean mIsSelected = false;
    String mPackageName;

    public AppInfoEntity setPackageName(String mPackageName) {
        this.mPackageName = mPackageName;
        return this;
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

    public AppInfoEntity(String packageName, boolean selected)
    {
        mPackageName= packageName;
        mIsSelected = selected;
    }
    public AppInfoEntity(String packageName)
    {
        this(packageName,false);
    }
    public AppInfoEntity()
    {

    }
}
