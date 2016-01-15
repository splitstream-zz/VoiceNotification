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



    public void setBundleKeys(List<BundleKeyEntity> bundleKeys) {
        this.mBundleKeys = bundleKeys;
    }

    public String getBundleKeyValue(String key) {
        StringBuilder value = new StringBuilder();
        for (BundleKeyEntity entity : mBundleKeys) {
            if (entity.getKey().equals(key)) {
                value.append(entity.getValue());
                value.append(".\n");
            }
        }
        return value.toString();
    }
    public List<BundleKeyEntity> getBundleKeys() {
        return getBundleKeys(false);
    }
    public List<BundleKeyEntity> getBundleKeys(boolean onlyFollowed) {
        List<BundleKeyEntity> result = mBundleKeys;
        if(onlyFollowed) {
            result = new ArrayList<>();
            for(BundleKeyEntity entity:mBundleKeys)
                if(entity.isFollowed())
                    result.add(entity);
        }
        return result;
    }
    public List<BundleKeyEntity> getBundleKeys(String key) {
        List<BundleKeyEntity> result = new ArrayList<>();
        for (BundleKeyEntity entity : mBundleKeys) {
            if (entity.getKey().equals(key)) {
                result.add(entity);
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

    public boolean isModified() {
        return mIsModified;
    }

    public void setIsModified(Boolean bool) {
        mIsModified = bool;
    }

    public AppInfoEntity(String packageName, String applicationLabel) {
        mApplicationLabel = applicationLabel;
        mPackageName = packageName;
    }

    protected AppInfoEntity() {
    }

}
