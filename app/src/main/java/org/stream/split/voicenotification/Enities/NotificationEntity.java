package org.stream.split.voicenotification.Enities;


import java.util.HashMap;
import java.util.Map;

/**
 * Created by B on 2015-11-05.
 */
public class NotificationEntity
{

    long mID;
    long mOccurrenceTime;

    String mPackageName;
    String mApplicationLabel;
    String mTinkerText;
    String mUtteranceId;



    boolean mIsFollowed;

    Map<String,String> mMessages = new HashMap<>();

    public long getID() {
        return mID;
    }
    public void setID(long ID) {
        this.mID = ID;
    }

    public String getTinkerText() {
        return mTinkerText;
    }
    public void setTinkerText(String TinkerText) {
        this.mTinkerText = TinkerText;
    }

    public Map<String, String> getMessages() {
        return mMessages;
    }
    public void setMessages(Map<String, String> Messages) {
        this.mMessages = Messages;
    }
    public String getMessage(String key)
    {
        return mMessages.get(key);
    }
    public void addMessage(String key, String Value)
    {
        mMessages.put(key, Value);
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

    public NotificationEntity( String packageName, String applicationName, long occurrenceTime, String utteranceId)
    {
        mPackageName = packageName;
        mApplicationLabel = applicationName;
        mOccurrenceTime = occurrenceTime;
        mUtteranceId = utteranceId;
    }
    public NotificationEntity( String packageName, String applicationName, long occurrenceTime)
    {
        this(packageName,applicationName,occurrenceTime,"");
    }
    public NotificationEntity(long ID)
    {
        mID = ID;
    }


}
