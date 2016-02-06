package org.stream.split.voicenotification.Enities;

/**
 * Created by split on 2015-11-26.
 */
public class HistoryBundleKeyEntity extends NotificationBundleKeyEntity {
    String mUtteranceId;
    String mValue;

    public String getUtteranceId() {
        return mUtteranceId;
    }
    public void setUtteranceId(String utteranceId) {
        this.mUtteranceId = utteranceId;
    }
    public String getValue() {
        return mValue;
    }

    public HistoryBundleKeyEntity(HistoryNotificationEntity notificationEntity, String key, String value, int priority, boolean isShownAlways)
    {
        super(notificationEntity,key,priority,isShownAlways);
        mValue = value;
    }
    public HistoryBundleKeyEntity(HistoryNotificationEntity notificationEntity, String key, String value)
    {
        super(notificationEntity,key);
        mValue = value;
    }

    public HistoryBundleKeyEntity(String packageName, int sbnId, String key, String value) {
        super(packageName, sbnId, key);
        mValue = value;
    }

    /**
     * constructor to copy: key, packagename, sbnId
     * @param entity
     */
    public HistoryBundleKeyEntity(HistoryBundleKeyEntity entity)
    {
        super(entity);
    }
}
