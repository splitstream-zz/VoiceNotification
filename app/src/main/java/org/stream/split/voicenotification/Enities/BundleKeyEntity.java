package org.stream.split.voicenotification.Enities;

import java.io.Serializable;

/**
 * Created by split on 2015-11-26.
 */
public class BundleKeyEntity extends BaseEntity implements Comparable, Serializable {

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
    public int compareTo(Object another) {
        int result = 0;
        if(another instanceof BundleKeyEntity) {
            BundleKeyEntity another1 = (BundleKeyEntity)another;
            if (this.isFollowed() && another1.isFollowed())
                if (this.getPriority() < another1.getPriority())
                    result = 1;
                else
                    result = -1;
            else if (this.isFollowed())
                result = 1;
            else if (another1.isFollowed())
                result = -1;
        }
        return result;
    }

}
