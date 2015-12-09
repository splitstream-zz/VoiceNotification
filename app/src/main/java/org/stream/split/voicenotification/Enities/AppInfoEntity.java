package org.stream.split.voicenotification.Enities;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by split on 2015-11-01.
 */
public class AppInfoEntity {

    //TODO is selected should be eliminated and data should be stored in viewholder?
    private boolean mIsSelected = false;
    private boolean mIsFollowed = false;
    private String mPackageName;
    private List<BundleKeyEntity> mBundleKeys;

    public List<BundleKeyEntity> getBundleKeys() {
        return mBundleKeys;
    }
    public void setBundleKeys(List<BundleKeyEntity> bundleKeys) {
        this.mBundleKeys = bundleKeys;
    }
    public void addBundleKey(BundleKeyEntity bundleKeys) {
        mBundleKeys.add(bundleKeys);
    }

    public boolean isFollowed() {
        return mIsFollowed;
    }
    public void setIsFollowed(boolean isFollowed) {
        this.mIsFollowed = isFollowed;
    }

    public AppInfoEntity setPackageName(String mPackageName) {
        this.mPackageName = mPackageName;
        return this;
    }
    public String getPackageName() {
        return mPackageName;
    }

    public boolean isSelected() {
        return mIsSelected;
    }
    public void setSelected(Boolean bool)
    {
        mIsSelected = bool;
    }

    public AppInfoEntity(String packageName, boolean selected)
    {
        mBundleKeys = new ArrayList<>();
        mPackageName= packageName;
        mIsSelected = selected;

    }
    public AppInfoEntity(String packageName)
    {
        this(packageName,false);
    }

}
