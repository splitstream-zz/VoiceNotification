package org.stream.split.voicenotification.Enities;

import org.stream.split.voicenotification.Interfaces.BundleKeyOwner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by split on 2016-02-03.
 */
public class BundleKeyList<T extends BundleKeyEntity & Serializable> extends BaseEntity implements Serializable {
    private List<T> mBundleKeys;
    public void add(T bundleKey)
    {
        mBundleKeys.add(bundleKey);
    }

    public BundleKeyList()
    {
        mBundleKeys = new ArrayList<>();
    }

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
        Collections.sort(result, Collections.reverseOrder(BundleKeyEntity.Comparators.DEFAULT));
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

    @Override
    public void clearIsModified() {
        this.setIsModified(false);
        for(T entity:mBundleKeys)
        {
            entity.clearIsModified();
        }
    }
}
