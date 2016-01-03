package org.stream.split.voicenotification.Enities;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by split on 2015-11-01.
 */
public class AppInfoEntity {

    private boolean mIsModified = false;
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

    public String getBundleKeyValue(String key) {
        String value = null;
        for (BundleKeyEntity entity : mBundleKeys) {
            if (entity.getKey().equals(key))
                value = entity.getValue();
        }
        return value;
    }
    public BundleKeyEntity getBundleKey(String key) {
        BundleKeyEntity result = null;
        for (BundleKeyEntity entity : mBundleKeys) {
            if (entity.getKey().equals(key)) {
                result = entity;
                break;
            }
        }
        return result;
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

    public void setPackageName(String mPackageName) {
        this.mPackageName = mPackageName;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public boolean isSelected() {
        return mIsModified;
    }

    public void setSelected(Boolean bool) {
        mIsModified = bool;
    }

    public AppInfoEntity(String packageName, String applicationLabel) {
        mApplicationLabel = applicationLabel;
        mPackageName = packageName;
    }

    protected AppInfoEntity() {
    }

}
