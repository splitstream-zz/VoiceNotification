package org.stream.split.voicenotification.Enities;

/**
 * Created by split on 2015-11-26.
 */
public class BundleKeyEntity {
    String mPackageName;

    public BundleKeyEntity(String packageName, String key, int priority) {
        this.mPackageName = packageName;
        this.mKey = key;
        this.mPriority = priority;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public void setPackageName(String packageName) {
        this.mPackageName = mPackageName;
    }

    String mKey;

    public String getKey() {
        return mKey;
    }

    public void setKey(String key) {
        this.mKey = mKey;
    }

    int mPriority;

    public int getPriority() {
        return mPriority;
    }

    public void setPriority(int priority) {
        this.mPriority = mPriority;
    }

}
