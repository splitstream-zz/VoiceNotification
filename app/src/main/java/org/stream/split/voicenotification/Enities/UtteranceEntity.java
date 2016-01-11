package org.stream.split.voicenotification.Enities;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by split on 2015-11-26.
 */
public class UtteranceEntity {

    String mUtteranceId;
    List<String> mMessages = new ArrayList<>();

    public List<String> getMessages() {
        return mMessages;
    }
    public void addMessage(String message) {
        this.mMessages.add(message);
    }

    public String getUtteranceId() {
        return mUtteranceId;
    }
    public void setUtteranceId(String utteranceId) {
        this.mUtteranceId = utteranceId;
    }

    public UtteranceEntity(String utteranceId) {
        this.mUtteranceId = utteranceId;
    }
    public UtteranceEntity()
    {}

}
