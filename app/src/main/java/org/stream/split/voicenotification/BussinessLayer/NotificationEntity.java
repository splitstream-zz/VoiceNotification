package org.stream.split.voicenotification.BussinessLayer;


import android.app.Notification;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by B on 2015-11-05.
 */
public class NotificationEntity
{
    String mPackageName;
    String mApplicationName;
    String mText;
    String mTitle;
    Timestamp mOcuranceTime;
    int mUtteranceId;

    public String getPackageName() {
        return mPackageName;
    }

    public void setPackageName(String mPackageName) {
        this.mPackageName = mPackageName;
    }

    public String getApplicationName() {
        return mApplicationName;
    }

    public void setApplicationName(String mApplicationName) {
        this.mApplicationName = mApplicationName;
    }

    public Timestamp getOcuranceTime() {
        return mOcuranceTime;
    }

    public void setOcuranceTime(Timestamp mOcuranceTime) {
        this.mOcuranceTime = mOcuranceTime;
    }

    public int getUtteranceId() {
        return mUtteranceId;
    }

    public void setUtteranceId(int mUtteranceId) {
        this.mUtteranceId = mUtteranceId;
    }

    public NotificationEntity(Notification notification)
    {
StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(notification.)
    }
    public NotificationEntity(String packageName, String aplicationName, Timestamp occuranceTime, int utteranceId)
    {
        mPackageName = packageName;
        mApplicationName = aplicationName;
        mOcuranceTime = occuranceTime;
        mUtteranceId = utteranceId;
    }
    public NotificationEntity(String packageName, String aplicationName, Timestamp occuranceTime)
    {
        this(packageName,aplicationName,occuranceTime,0);
    }
    public NotificationEntity()
    {}


}
