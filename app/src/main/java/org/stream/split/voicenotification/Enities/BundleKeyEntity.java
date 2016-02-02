package org.stream.split.voicenotification.Enities;

/**
 * Created by split on 2015-11-26.
 */
public class BundleKeyEntity implements Comparable {
    String mPackageName;
    int mSbnId = -1;
    String mKey;
    String mValue;
    int mPriority;
    boolean mIsFollowed = false;
    boolean mIsModified = false;
    boolean mIsShowAlways = false;


    public BundleKeyEntity(String packageName,int sbnId, String key, String value)
    {
        this(packageName,key);
        mSbnId = sbnId;
        mValue = value;
    }

    public BundleKeyEntity(String packageName, String key, int priority,boolean isShowAlways)
    {
        this(packageName,key,priority);
        mIsShowAlways = isShowAlways;
    }
    public BundleKeyEntity(String packageName,int snbId, String key, int priority,boolean isShowAlways)
    {
        this(packageName,key,priority);
        mIsShowAlways = isShowAlways;
        mSbnId = snbId;
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
    public boolean isShowAlways() {
        return mIsShowAlways;
    }

    public void setIsShowAlways(boolean isShowAlways) {
        this.mIsShowAlways = isShowAlways;
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

    public int getSbnId() {
        return mSbnId;
    }
}
