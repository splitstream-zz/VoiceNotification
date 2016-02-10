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
    public void setValue(String value) {
        this.mValue = value;
    }

    public HistoryBundleKeyEntity(String packageName, int sbnId, String value, String key, int priority, boolean isShownAlways)
    {
        super(packageName,sbnId,key,priority,isShownAlways);
        mValue = value;
    }

    public HistoryBundleKeyEntity(String packageName, int sbnId, String value, String key) {
        super(packageName, sbnId, key);
        mValue = value;
    }

    /**
     * constructor with bundlekey entity
     * @param entity
     */
    public HistoryBundleKeyEntity(String packageName, int sbnId, String value, BundleKeyEntity entity)
    {
        super(packageName,sbnId,entity);
        mValue = value;
    }


}
