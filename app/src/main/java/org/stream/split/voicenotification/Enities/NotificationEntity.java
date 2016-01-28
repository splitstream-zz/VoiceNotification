package org.stream.split.voicenotification.Enities;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by B on 2015-11-05.
 */
public class NotificationEntity extends AppInfoEntity
{
        long mId;
        long mSbnId = -1;
        long mOccurrenceTime;
        String mTinkerText;

public long getID() {
        return mId;
        }
public void setID(long ID) {
        this.mId = ID;
        }

public long getSbnId() {
        return mSbnId;
        }
public void setSbnId(long sbnId) {
        this.mSbnId = sbnId;
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

public NotificationEntity( long sbnId, String packageName, String applicationLabel, long occurrenceTime)
        {
        super(packageName,applicationLabel);
        mSbnId = sbnId;
        mOccurrenceTime = occurrenceTime;
        }

public NotificationEntity(long ID)
        {
        mId = ID;
        }
        }
