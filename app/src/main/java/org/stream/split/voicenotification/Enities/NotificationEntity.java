package org.stream.split.voicenotification.Enities;

/**
 * Created by split on 2016-01-30.
 */
public class NotificationEntity extends AppInfoEntity {
    int mSbnId = -1;
    public long getSbnId() {
        return mSbnId;
    }
    public void setSbnId(int sbnId) {
        this.mSbnId = sbnId;
    }
    public NotificationEntity(int sbnId, String packageName, String applicationLabel)
    {
        super(packageName,applicationLabel);
        mSbnId = sbnId;
    }

}
