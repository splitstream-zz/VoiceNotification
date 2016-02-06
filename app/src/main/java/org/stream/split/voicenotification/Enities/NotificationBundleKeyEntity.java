package org.stream.split.voicenotification.Enities;

/**
 * Created by split on 2015-11-26.
 */
public class NotificationBundleKeyEntity extends AppBundleKeyEntity {
    int mSbnId;

    public int getSbnId() {
        return mSbnId;
    }

    /**
     * Constructor used to to initialize !followed! bundle key
     * @param notificationEntity
     * @param key
     * @param priority
     * @param isShowAlways
     */
    public NotificationBundleKeyEntity(NotificationEntity notificationEntity, String key, int priority, boolean isShowAlways)
    {
        this(notificationEntity.getPackageName(), notificationEntity.getSbnId(),key,priority,isShowAlways);
    }
    public NotificationBundleKeyEntity(String packageName, int sbnId, String key, int priority, boolean isShownAlways) {
        super(packageName,key,priority,isShownAlways);
        mSbnId = sbnId;

    }

    public NotificationBundleKeyEntity(NotificationEntity notificationEntity, String key)
    {
        this(notificationEntity.getPackageName(), notificationEntity.getSbnId(),key);
    }
    public NotificationBundleKeyEntity(String packageName, int sbnId, String key)
    {
        super(packageName,key);
        mSbnId = sbnId;
    }
    public NotificationBundleKeyEntity(NotificationBundleKeyEntity entity)
    {
        super(entity);
        mSbnId = entity.mSbnId;
    }
}
