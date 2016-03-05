package org.stream.split.voicenotification.Enities;

import org.stream.split.voicenotification.Interfaces.BundleKeyOwner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by split on 2015-11-01.
 */
public class AppInfoEntity extends BaseEntity implements Serializable, BundleKeyOwner {

    private String mPackageName;
    private String mApplicationLabel;
    private List<NotificationEntity> mNotifications = new ArrayList<>();

    private BundleKeyList<AppBundleKeyEntity>  mBundleKeyList = new BundleKeyList<>();

    public BundleKeyList getBundleKeyList()
    {
        return mBundleKeyList;
    }
    public String getApplicationLabel() {
        return mApplicationLabel;
    }

    public void setApplicationLabel(String ApplicationName) {
        this.mApplicationLabel = ApplicationName;
    }

    public List<NotificationEntity> getNotifications()
    {
        return mNotifications;
    }

    public void setNotifications(List<NotificationEntity> followedNotification) {
        mNotifications = followedNotification;
    }

    public void setPackageName(String mPackageName) {
        this.mPackageName = mPackageName;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public AppInfoEntity(String packageName, String applicationLabel) {
        mApplicationLabel = applicationLabel;
        mPackageName = packageName;
    }

    @Override
    public boolean isModified() {
        boolean result = false;
        for(NotificationEntity entity:mNotifications)
        {
            if(entity.isModified()) {
                result = true;
                break;
            }

        }

        if(super.isModified() || mBundleKeyList.isModified())
        {
            result = true;
        }

        return result;
    }

    @Override
    public void clearIsModified() {
        for(NotificationEntity entity:mNotifications)
        {
            entity.clearIsModified();
        }
        mBundleKeyList.clearIsModified();

    }

    @Override
    public void addBundleKey(BundleKeyEntity entity) {
        AppBundleKeyEntity appBundleKeyEntity = new AppBundleKeyEntity(getPackageName(),entity);
        getBundleKeyList().add(appBundleKeyEntity);
    }
}
