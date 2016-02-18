package org.stream.split.voicenotification.Enities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by split on 2015-11-01.
 */
public class AppInfoEntity extends BundleKeysOwner<AppBundleKeyEntity> implements Serializable {

    private String mPackageName;
    private String mApplicationLabel;
    private List<NotificationEntity> mNotifications = new ArrayList<>();

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
    public void addBundleKey(BundleKeyEntity entity) {
        AppBundleKeyEntity appBundleKeyEntity = new AppBundleKeyEntity(this.getPackageName(),entity);
        super.add(appBundleKeyEntity);
    }
}
