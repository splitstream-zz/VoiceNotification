package org.stream.split.voicenotification.Enities;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by split on 2016-02-03.
 */
public abstract class BaseEntity {

    private boolean mIsModified = false;
    private boolean mIsFollowed = false;
    private boolean isSelected = false;

    public boolean isFollowed() {
        return mIsFollowed;
    }
    public void setIsFollowed(boolean isFollowed) { this.mIsFollowed = isFollowed; }

    public boolean isModified() {
        return mIsModified;
    }
    public void setIsModified(Boolean bool) {
        mIsModified = bool;
    }

    public boolean isSelected() {
        return isSelected;
    }
    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }
}
