package org.stream.split.voicenotification.Enities;

/**
 * Created by split on 2015-11-26.
 */
public class UtteranceEntity {

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        this.mMessage = message;
    }

    String mUtteranceId;

    public String getUtteranceId() {
        return mUtteranceId;
    }

    public void setUtteranceId(String utteranceId) {
        this.mUtteranceId = utteranceId;
    }

    String mMessage;

    public UtteranceEntity(String utteranceId, String message) {
        this.mUtteranceId = utteranceId;
        this.mMessage = message;
    }

}
