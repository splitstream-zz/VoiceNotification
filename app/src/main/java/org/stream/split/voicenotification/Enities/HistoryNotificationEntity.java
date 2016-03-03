package org.stream.split.voicenotification.Enities;


import org.stream.split.voicenotification.Interfaces.BundleKeyOwner;

import java.io.Serializable;
import java.util.List;

/**
 * Created by B on 2015-11-05.
 */
public class HistoryNotificationEntity extends NotificationEntity<HistoryBundleKeyEntity> implements Serializable, BundleKeyOwner {
    long mId;
    long mOccurrenceTime;
    String mTinkerText;

    public long getID() {
        return mId;
    }

    public void setID(long ID) {
        this.mId = ID;
    }


    public String getTinkerText() {
        return mTinkerText;
    }

    public void setTinkerText(String TinkerText) {
        this.mTinkerText = TinkerText;
    }

    public long getOccurrenceTime() {
        return mOccurrenceTime;
    }

    public void setOccurrenceTime(long OccurrenceTime) {
        this.mOccurrenceTime = OccurrenceTime;
    }

    public String getBundleKeyValue(String key) {
        StringBuilder value = new StringBuilder();
        List<HistoryBundleKeyEntity> bundleKeys = getBundleKeyList().get();
        for (HistoryBundleKeyEntity entity : bundleKeys) {
            if (!entity.getValue().isEmpty() && entity.getKey().equals(key)) {
                value.append(entity.getValue());
                value.append(". ");
            }
        }
        return value.toString();
    }

    public HistoryNotificationEntity( String packageName,int sbnId, long occurrenceTime) {
        super(sbnId, packageName);
        mOccurrenceTime = occurrenceTime;
    }
    @Override
    public void addBundleKey(BundleKeyEntity entity) {
        HistoryBundleKeyEntity historyBundleKeyEntity = new HistoryBundleKeyEntity(getPackageName(),
                getSbnId(),
                "",
                entity);
        getBundleKeyList().add(historyBundleKeyEntity);
    }

}
