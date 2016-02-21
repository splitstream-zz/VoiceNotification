package org.stream.split.voicenotification.Adapters;



import android.app.FragmentManager;
import android.app.Fragment;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTitleStrip;

import org.stream.split.voicenotification.Enities.AppBundleKeyEntity;
import org.stream.split.voicenotification.Enities.AppInfoEntity;
import org.stream.split.voicenotification.Enities.BundleKeyEntity;
import org.stream.split.voicenotification.Enities.NotificationEntity;
import org.stream.split.voicenotification.Fragments.BaseFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by B on 2016-02-10.
 */
public class SwipeViewsAdapter extends FragmentPagerAdapter {

    List<BaseFragment> mFragmentList = new ArrayList<>();
    List<String> mPageStripTitleList = new ArrayList<>();


    public SwipeViewsAdapter(FragmentManager fm)
    {
        super(fm);
    }

    public void AddView(BaseFragment fragment, String pageStripTitle)
    {
        mFragmentList.add(fragment);
        mPageStripTitleList.add(pageStripTitle);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentList.get(position).getTitle();
    }

    public boolean isModified()
    {
        boolean isModified = false;
        for(BaseFragment fragment:mFragmentList)
        {
            if(fragment.isModified()) {
                isModified = true;
                break;
            }
        }

        return isModified;
    }
}
