package org.stream.split.voicenotification.Enities;

import java.io.Serializable;

/**
 * Created by split on 2015-11-26.
 */
public class NotificationBundleKeyEntity extends AppBundleKeyEntity implements Serializable {
    int mSbnId;

    public int getSbnId() {
        return mSbnId;
    }

    /**
     * Constructor used to to initialize !followed! bundle key
     * @param packageName
     * @param sbnId
     * @param key
     * @param priority
     * @param isShownAlways
     */
    public NotificationBundleKeyEntity(String packageName, int sbnId, String key, int priority, boolean isShownAlways) {
        super(packageName,key,priority,isShownAlways);
        mSbnId = sbnId;

    }
    public NotificationBundleKeyEntity(String packageName, int sbnId, String key)
    {
        super(packageName,key);
        mSbnId = sbnId;
    }
    public NotificationBundleKeyEntity(String packageName, int sbnId, BundleKeyEntity entity)
    {
        super(packageName,entity);
        mSbnId = sbnId;
    }
}
