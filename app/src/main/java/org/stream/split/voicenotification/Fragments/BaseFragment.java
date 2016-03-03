package org.stream.split.voicenotification.Fragments;

import android.app.Fragment;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;

import org.stream.split.voicenotification.Enities.BaseEntity;
import org.stream.split.voicenotification.Interfaces.FabOwner;
import org.stream.split.voicenotification.Logging.BaseLogger;
import org.stream.split.voicenotification.R;

import java.util.List;

/**
 * Created by split on 2016-01-04.
 */
public abstract class BaseFragment extends Fragment {

    private String mTitle = "";
    private boolean mIsModified;
    private android.support.design.widget.FloatingActionButton mfab;
    protected AsyncTask<Void,Void,List<? extends BaseEntity>> mLoading;

    public static BaseLogger LOGGER = BaseLogger.getInstance();

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public abstract boolean isModified();

    protected void setIsModified(boolean isModified) {
        mIsModified = isModified;
    }

    public abstract String getTAG();

    public void refresh() {

    }

    public void finish() {
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
        BaseFragment fragment = (BaseFragment) getActivity().getFragmentManager().findFragmentById(R.id.frame_content);
        if (this == fragment) {
            setImmediatelyTitle(mTitle);
            setUpFab();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        String defaultTitle = getActivity().getResources().getString(R.string.DEFAULT_FRAGMENT_TITLE);
        setImmediatelyTitle(defaultTitle);
        if(mLoading != null && mLoading.getStatus() == AsyncTask.Status.RUNNING)
        {
            mLoading.cancel(true);
        }
    }

    private void setImmediatelyTitle(String title) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(title);
    }

    private void setUpFab() {
        mfab = (android.support.design.widget.FloatingActionButton) getActivity().findViewById(R.id.fab);
        if (this instanceof FabOwner) {
            ((FabOwner) this).setUpFab(mfab);
            mfab.show();
        } else
            mfab.hide();
    }
}
