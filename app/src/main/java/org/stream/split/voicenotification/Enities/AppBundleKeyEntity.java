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
     * @param appInfoEntity
     * @param key
     * @param priority
     * @param isShowAlways
     */
    public AppBundleKeyEntity(AppInfoEntity appInfoEntity, String key, int priority,boolean isShowAlways)
    {
        this(appInfoEntity.getPackageName(),key,priority,isShowAlways);
    }

    public AppBundleKeyEntity(String packageName, String key, int priority, boolean isShownAlways) {
        super(key,priority,isShownAlways);
        mPackageName = packageName;
    }
    public AppBundleKeyEntity(String packageName, String key)
    {
        super(key);
        mPackageName = packageName;
    }
    public AppBundleKeyEntity(AppBundleKeyEntity entity)
    {
        super(entity.mKey);
        mPackageName = entity.mPackageName;
    }



}
