package org.stream.split.voicenotification.Enities;

/**
 * Created by split on 2015-11-26.
 */
public class AppBundleKeyEntity extends BundleKeyEntity {
    String mPackageName;

    public String getPackageName() {
        return mPackageName;
    }
    public void setPackageName(String packageName) {
        this.mPackageName = packageName;
    }

    /**
     * Constructor used to to initialize !followed! bundle key
     * @param packageName
     * @param key
     * @param priority
     * @param isShownAlways
     */
    public AppBundleKeyEntity(String packageName, String key, int priority, boolean isShownAlways) {
        super(key,priority,isShownAlways);
        mPackageName = packageName;
    }
    public AppBundleKeyEntity(String packageName, BundleKeyEntity entity)
    {
        super(entity);
        mPackageName = packageName;
    }


    public AppBundleKeyEntity(String packageName, String key)
    {
        super(key);
        mPackageName = packageName;
    }




}
