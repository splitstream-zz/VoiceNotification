package org.stream.split.voicenotification.Enities;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by B on 2015-11-05.
 */
public class NotificationEntity
{

    long mId;

    long mSbnId;
    long mOccurrenceTime;

    String mPackageName;
    String mApplicationLabel;
    String mTinkerText;
    String mUtteranceId;

    boolean mIsFollowed;

    List<BundleKeyEntity> mMessages;

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

    public List<BundleKeyEntity> getMessages() {
        return mMessages;
    }
    public void setMessages(List<BundleKeyEntity> Messages) {
        this.mMessages = Messages;
    }
    public String getMessage(String key) throws IllegalArgumentException
    {
        String value = null;
        for(BundleKeyEntity entity:mMessages)
        {
            if(entity.getKey() == key)
                value = entity.getValue();
        }
        if(value == null)
            throw new IllegalArgumentException("podanego klucza nie znaleziono");
        return value;
    }
    public void addMessage(String key, String value)
    {
        mMessages.add(new BundleKeyEntity(mPackageName,key,value));
    }

    public String getPackageName() {
        return mPackageName;
    }
    public void setPackageName(String PackageName) {
        this.mPackageName = PackageName;
    }

    public String getApplicationLabel() {
        return mApplicationLabel;
    }
    public void setApplicationLabel(String ApplicationName) {
        this.mApplicationLabel = ApplicationName;
    }

    public boolean isFollowed() {
        return mIsFollowed;
    }
    public void setIsFollowed(boolean isFollowed) {
        this.mIsFollowed = isFollowed;
    }

    public long getOccurrenceTime() {
        return mOccurrenceTime;
    }
    public void setOccurrenceTime(long OccurrenceTime) {
        this.mOccurrenceTime = OccurrenceTime;
    }


    public String getUtteranceId() {
        return mUtteranceId;
    }
    public void setUtteranceId(String UtteranceId) {
        this.mUtteranceId = UtteranceId;
    }

    public NotificationEntity( long sbnId, String packageName, String applicationName, long occurrenceTime, String utteranceId)
    {
        mSbnId = sbnId;
        mPackageName = packageName;
        mApplicationLabel = applicationName;
        mOccurrenceTime = occurrenceTime;
        mUtteranceId = utteranceId;
        mMessages = new ArrayList<>();
    }
    public NotificationEntity( long sbnId, String packageName, String applicationName, long occurrenceTime)
    {
        this(sbnId,packageName,applicationName,occurrenceTime,"");
    }
    public NotificationEntity(long ID)
    {
        mId = ID;
    }


}
