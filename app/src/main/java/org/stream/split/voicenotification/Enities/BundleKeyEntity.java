package org.stream.split.voicenotification.Enities;

import java.util.Comparator;

/**
 * Created by split on 2015-11-26.
 */
public class BundleKeyEntity implements Comparable {
    String mPackageName;
    String mKey;
    String mValue;
    int mPriority;
    String mUtteranceId;
    boolean mIsFollowed = false;
    boolean mIsModified = false;

    public BundleKeyEntity(String packageName, String key, String value)
    {
        this(packageName,key);
        mValue = value;
    }

    public BundleKeyEntity(String packageName, String key, int priority) {
        this(packageName,key);
        this.mPriority = priority;
        mIsFollowed = true;
    }

    public BundleKeyEntity(String packageName, String key)
    {
        mPackageName = packageName;
        mKey = key;
    }

    public String getUtteranceId() {
        return mUtteranceId;
    }
    public void setUtteranceId(String UtteranceId) {
        this.mUtteranceId = UtteranceId;
    }

    public boolean isModified() {
        return mIsModified;
    }
    public void setIsModified(boolean isModified) {
        this.mIsModified = isModified;
    }

    public boolean isFollowed() {
        return mIsFollowed;
    }
    public void setIsFollowed(boolean isFollowed) {
        this.mIsFollowed = isFollowed;
    }

    public String getPackageName() {
        return mPackageName;
    }
    public void setPackageName(String packageName) {
        this.mPackageName = packageName;
    }


    public String getKey() {
        return mKey;
    }
    public void setKey(String key) {
        this.mKey = key;
    }


    public int getPriority() {
        return mPriority;
    }
    public void setPriority(int priority) {
        this.mPriority = priority;
    }

    public String getValue() {
        return mValue;
    }
    public void setValue(String value) {
        this.mValue = value;
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
