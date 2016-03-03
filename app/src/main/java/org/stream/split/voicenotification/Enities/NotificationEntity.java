package org.stream.split.voicenotification.Enities;

import org.stream.split.voicenotification.Enums.NotificationPolicy;
import org.stream.split.voicenotification.Interfaces.BundleKeyOwner;

import java.io.Serializable;
import java.util.List;

/**
 * Created by split on 2016-01-30.
 */
public class NotificationEntity<T extends NotificationBundleKeyEntity & Serializable> extends BaseEntity implements Serializable, BundleKeyOwner {
    private String mPackageName;
    private int mSbnId;
    private NotificationPolicy mPolicy;
    private BundleKeyList<T> mBundleKeyList;

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
        mBundleKeyList = new BundleKeyList<>();
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

    @Override
    public void clearIsModified() {
        setIsModified(false);
        mBundleKeyList.clearIsModified();
    }

    @Override
    public void addBundleKey(BundleKeyEntity entity) {
        NotificationBundleKeyEntity notificationBundleKeyEntity =
                new NotificationBundleKeyEntity(getPackageName(),getSbnId(),entity.getKey());
        getBundleKeyList().add((T)notificationBundleKeyEntity);
    }
}
