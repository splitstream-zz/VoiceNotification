package org.stream.split.voicenotification.Enities;

/**
 * Created by split on 2016-01-07.
 */
public class SettingEntity<T> {
    private int mId;
    private String mLabel;
    private T mValue;

    public T getValue() {
        return mValue;
    }

    public void setValue(T value) {
        this.mValue = value;
    }

    public String getLabel() {
        return mLabel;
    }

    public void setLabel(String label) {
        this.mLabel = label;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

}
