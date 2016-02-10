package org.stream.split.voicenotification.Fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.stream.split.voicenotification.Enities.AppBundleKeyEntity;
import org.stream.split.voicenotification.Enities.AppInfoEntity;
import org.stream.split.voicenotification.Enities.BundleKeyEntity;
import org.stream.split.voicenotification.Enities.NotificationEntity;

import java.util.List;

/**
 * Created by B on 2016-02-10.
 */
public class SwipeViewsAdapter extends FragmentPagerAdapter {

    private List<AppBundleKeyEntity> mBundleKeyEntities;
    private List<NotificationEntity> mNotificationEntities;

    public SwipeViewsAdapter(AppInfoEntity entity,FragmentManager fm)
    {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return null;
    }

    @Override
    public int getCount() {
        return 0;
    }
}
