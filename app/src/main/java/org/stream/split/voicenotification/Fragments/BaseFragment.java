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
    private android.support.design.widget.FloatingActionButton mfab;
    public static BaseLogger LOGGER = BaseLogger.getInstance();


    public String getTitle() {
        return mTitle;
    }

    protected void setTitle(String title) {
        mTitle = title;
    }

    public abstract boolean isModified();
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

    @Override
    public void onPause() {
        super.onPause();
        if(this instanceof FabOwner && mfab != null)
            mfab.hide();
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
        if(this instanceof FabOwner) {
            mfab = (android.support.design.widget.FloatingActionButton) getActivity().findViewById(R.id.fab);
            ((FabOwner) this).setUpFab(mfab);
            mfab.show();
        }
    }
}
