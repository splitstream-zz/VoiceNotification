package org.stream.split.voicenotification.Enities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by split on 2016-02-03.
 */
public abstract class BundleKeyList<T extends BundleKeyEntity & Serializable> extends BaseEntity implements Serializable {
    private List<T> mBundleKeys = new ArrayList<>();

    public void add(T bundleKey)
    {
        mBundleKeys.add(bundleKey);
    }
    public abstract void addBundleKey(BundleKeyEntity entity);

    public List<T> get() {
        return get(false);
    }
    public List<T> get(boolean onlyFollowed) {
        List<T> result = mBundleKeys;
        if(onlyFollowed) {
            result = new ArrayList<>();
            for(T entity:mBundleKeys)
                if(entity.isFollowed())
                    result.add(entity);
        }
        return result;
    }
    public List<T> get(String key) {
        List<T> result = new ArrayList<>();
        for (T entity : mBundleKeys) {
            if (entity.getKey().equals(key)) {
                result.add(entity);
            }
        }
        return result;
    }

    public void set(List<T> bundleKeys) {
        this.mBundleKeys = bundleKeys;
    }

    @Override
    public boolean isModified()
    {
        boolean result = false;
        for(BundleKeyEntity entity:mBundleKeys)
        {
            if(entity.isModified()) {
                result = true;
                break;
            }
        }
        return result;
    }
}
