package org.stream.split.voicenotification.Enities;

/**
 * Created by split on 2015-11-26.
 */
public class BundleKeyEntity {
    String mPackageName;
    String mKey;
    String mValue;
    int mPriority;
    boolean mIsFollowed;
    boolean mIsModified;

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
}
