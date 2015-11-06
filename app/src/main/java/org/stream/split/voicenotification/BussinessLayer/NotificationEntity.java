package org.stream.split.voicenotification.BussinessLayer;


import java.util.Date;

/**
 * Created by B on 2015-11-05.
 */
public class NotificationEntity
{
    String mPackageName;
    String mApplicationName;
    Date mOcuranceTime;
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

    public Date getOcuranceTime() {
        return mOcuranceTime;
    }

    public void setOcuranceTime(Date mOcuranceTime) {
        this.mOcuranceTime = mOcuranceTime;
    }

    public int getUtteranceId() {
        return mUtteranceId;
    }

    public void setUtteranceId(int mUtteranceId) {
        this.mUtteranceId = mUtteranceId;
    }

    public NotificationEntity(String packageName, String aplicationName, Date occuranceTime, int utteranceId)
    {
        mPackageName = packageName;
        mApplicationName = aplicationName;
        mOcuranceTime = occuranceTime;
        mUtteranceId = utteranceId;
    }
    public NotificationEntity(String packageName, String aplicationName, Date occuranceTime)
    {
        this(packageName,aplicationName,occuranceTime,0);
    }


}
