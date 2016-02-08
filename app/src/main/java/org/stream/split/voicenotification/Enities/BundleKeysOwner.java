package org.stream.split.voicenotification.Enities;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by split on 2016-02-03.
 */
public abstract class BundleKeysOwner<T extends BundleKeyEntity> extends BaseEntity {
    private List<T> mBundleKeys = new ArrayList<>();

    protected void add(T bundleKey)
    {
        mBundleKeys.add(bundleKey);
    }
    public abstract void addBundleKey(BundleKeyEntity entity);

    public List<T> getBundleKeys() {
        return getBundleKeys(false);
    }
    public List<T> getBundleKeys(boolean onlyFollowed) {
        List<T> result = mBundleKeys;
        if(onlyFollowed) {
            result = new ArrayList<>();
            for(T entity:mBundleKeys)
                if(entity.isFollowed())
                    result.add(entity);
        }
        return result;
    }
    public List<T> getBundleKeys(String key) {
        List<T> result = new ArrayList<>();
        for (T entity : mBundleKeys) {
            if (entity.getKey().equals(key)) {
                result.add(entity);
            }
        }
        return result;
    }

    public void setBundleKeys(List<T> bundleKeys) {
        this.mBundleKeys = bundleKeys;
    }
}
