package org.stream.split.voicenotification.Fragments;

import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import org.stream.split.voicenotification.Interfaces.FabOwner;
import org.stream.split.voicenotification.Logging.BaseLogger;
import org.stream.split.voicenotification.R;

/**
 * Created by split on 2016-01-04.
 */
public abstract class BaseFragment extends Fragment{

    String mTitle = "";
    boolean mIsModified;
    public static BaseLogger LOGGER = BaseLogger.getInstance();

    public String getTitle() {
        return mTitle;
    }

    protected void setTitle(String title) {
        mTitle = title;
    }

    public boolean isModified()
    {
        return mIsModified;
    }
    protected void setIsModified(boolean isModified)
    {
        mIsModified = isModified;
    }

    public void finish()
    {}

    @Override
    public void onResume() {
        super.onResume();
        setImmediatelyTitle(mTitle);
        setUpFab();
    }
    private void setImmediatelyTitle(String title)
    {
        if(getActivity() instanceof  AppCompatActivity && ((AppCompatActivity)getActivity()).getSupportActionBar() != null) {
            if (!title.isEmpty())
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(mTitle);
            else
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.default_title);
        }
    }
    private void setUpFab()
    {
        android.support.design.widget.FloatingActionButton fab = (android.support.design.widget.FloatingActionButton) getActivity().findViewById(R.id.fab);
        if(this instanceof FabOwner) {
            ((FabOwner) this).setUpFab(fab);
            fab.show();
        }
        else
            fab.hide();
    }
}
