package org.stream.split.voicenotification.Enities;

import org.stream.split.voicenotification.DataAccessLayer.DBContract;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by split on 2015-11-26.
 */
public class UtteranceEntity {

    String mUtteranceId;
    List<HistoryBundleKeyEntity> mMessages = new ArrayList<>();

    public List<HistoryBundleKeyEntity> getMessages() {
        return mMessages;
    }
    public void addMessage(HistoryBundleKeyEntity message) {
        this.mMessages.add(message);
    }
    public void addMessages(List<HistoryBundleKeyEntity> messages) {
        this.mMessages.addAll(messages);
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

    public String getFlatMessage()
    {
        StringBuilder message = new StringBuilder();
        for(HistoryBundleKeyEntity bundle:mMessages) {
            message.append(bundle.getValue());
            //message.append("\n");
        }
        return message.toString();
    }
    public String getFlatMessage(String key)
    {
        StringBuilder message = new StringBuilder();
        for(HistoryBundleKeyEntity bundle:mMessages) {
            if(bundle.getKey().equals(key))
            message.append(bundle.getValue());
            //message.append("\n");
        }
        return message.toString();
    }

}
