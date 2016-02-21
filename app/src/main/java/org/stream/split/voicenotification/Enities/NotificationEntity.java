package org.stream.split.voicenotification.Enities;

import org.stream.split.voicenotification.Enums.NotificationPolicy;

import java.io.Serializable;
import java.util.List;

/**
 * Created by split on 2016-01-30.
 */
public class NotificationEntity<T extends NotificationBundleKeyEntity & Serializable> extends BaseEntity implements Serializable {
    private String mPackageName;
    private int mSbnId;
    private NotificationPolicy mPolicy;

    private BundleKeyList<T> mBundleKeyList = new BundleKeyList<T>() {
        @Override
        public void addBundleKey(BundleKeyEntity entity) {
            NotificationBundleKeyEntity notificationBundleKeyEntity = new NotificationBundleKeyEntity(getPackageName(),getSbnId(),entity);
            add((T)notificationBundleKeyEntity);
        }
    };

    public BundleKeyList<T> getBundleKeyList()
    {
        return mBundleKeyList;
    }

    public int getSbnId() {
        return mSbnId;
    }

    public void setPackageName(String mPackageName) {
        this.mPackageName = mPackageName;
    }
    public String getPackageName() {
        return mPackageName;
    }

    public NotificationPolicy getPolicy()
    {
        if(mPolicy == null)
            return NotificationPolicy.NONE;
        else
            return mPolicy;
    }
    public void setPolicy(NotificationPolicy policy)
    {
        mPolicy = policy;
    }

    public NotificationEntity(int sbnId, String packageName, NotificationPolicy policy)
    {
        this(sbnId,packageName);
        mPolicy = policy;
    }
    public NotificationEntity(int sbnId, String packageName)
    {
        mPackageName = packageName;
        mSbnId = sbnId;
    }
    @Override
    public boolean isModified()
    {
        boolean result = false;
        if(super.isModified() || mBundleKeyList.isModified())
        {
            result = true;
        }
        return result;
    }

}
