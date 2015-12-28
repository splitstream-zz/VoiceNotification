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
    private String mApplicationLabel;
    private List<BundleKeyEntity> mBundleKeys = new ArrayList<>();

    public String getApplicationLabel() {
        return mApplicationLabel;
    }

    public void setApplicationLabel(String ApplicationName) {
        this.mApplicationLabel = ApplicationName;
    }

    public List<BundleKeyEntity> getBundleKeys() {
        return mBundleKeys;
    }

    public void setBundleKeys(List<BundleKeyEntity> bundleKeys) {
        this.mBundleKeys = bundleKeys;
    }

    public String getBundleKeyValue(String key) throws IllegalArgumentException {
        String value = null;
        for (BundleKeyEntity entity : mBundleKeys) {
            if (entity.getKey().equals(key))
                value = entity.getValue();
        }
        return value;
    }

    public void addBundleKey(String key, String value) {
        mBundleKeys.add(new BundleKeyEntity(mPackageName, key, value));
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

    public void setSelected(Boolean bool) {
        mIsSelected = bool;
    }

    public AppInfoEntity(String packageName, String applicationLabel) {
        this(packageName);
        mApplicationLabel = applicationLabel;
    }

//    public AppInfoEntity(String packageName, boolean selected) {
//        this(packageName);
//        mIsModified = selected;
//    }

    public AppInfoEntity(String packageName) {
        mPackageName = packageName;
    }

    protected AppInfoEntity() {
    }

}
