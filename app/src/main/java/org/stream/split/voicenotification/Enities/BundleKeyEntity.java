package org.stream.split.voicenotification.Enities;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by split on 2015-11-26.
 */
public class BundleKeyEntity extends BaseEntity implements Comparable<BundleKeyEntity>, Serializable {

    String mKey;
    int mPriority;
    boolean mIsShowAlways = false;

    /**
     * This Constructor should be used to initialize ONLY followed bundle keys
     * @param key
     * @param priority
     */
    public BundleKeyEntity(String key, int priority,boolean isShownAlways)
    {
        mKey = key;
        mPriority = priority;
        mIsShowAlways = isShownAlways;
        setIsFollowed(true);
    }
    protected BundleKeyEntity(BundleKeyEntity entity)
    {
        mKey = entity.mKey;
        mPriority = entity.mPriority;
        mIsShowAlways = entity.mIsShowAlways;
        setIsFollowed(entity.isFollowed());
    }
    public BundleKeyEntity(String key)
    {
        mKey = key;
    }

    public String getKey() {
        return mKey;
    }
    public void setKey(String key) {
        this.mKey = key;
    }

    public boolean isShowAlways() {
        return mIsShowAlways;
    }
    public void setIsShowAlways(boolean isShowAlways) {
        this.mIsShowAlways = isShowAlways;
    }

    public int getPriority() {
        return mPriority;
    }
    public void setPriority(int priority) {
        this.mPriority = priority;
    }

    @Override
    public int compareTo(BundleKeyEntity another) {
        int result = 0;
        if (this.isFollowed() && another.isFollowed())
            if (this.getPriority() < another.getPriority())
                result = 1;
            else
                result = -1;
        else if (this.isFollowed())
            result = 1;
        else if (another.isFollowed())
            result = -1;

        return result;
    }

    public static class Comparators
    {
        public static Comparator<BundleKeyEntity> DEFAULT = new Comparator<BundleKeyEntity>() {
            @Override
            public int compare(BundleKeyEntity lhs, BundleKeyEntity rhs) {
                return lhs.compareTo(rhs);
            }
        };
    }

}
