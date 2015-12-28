package org.stream.split.voicenotification.Fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import org.stream.split.voicenotification.Adapters.InstalledAppAdapter;
import org.stream.split.voicenotification.DataAccessLayer.DBHelper;
import org.stream.split.voicenotification.Enities.AppInfoEntity;
import org.stream.split.voicenotification.Helpers.Helper;
import org.stream.split.voicenotification.Interfaces.OnFragmentInteractionListener;
import org.stream.split.voicenotification.R;
import org.stream.split.voicenotification.VoiceNotificationActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class InstalledAppFragment extends Fragment {

    private final String TAG = this.getClass().getSimpleName();
    private RecyclerView mRecyclerView;
    private OnFragmentInteractionListener mListener;
    private InstalledAppAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;


    /**
     * progressBar indicating application loading
     */
    private ProgressBar mProgressBar;
    private int mProgressBarVisibility;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public InstalledAppFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        new LoadApplicationsAsync().execute();
        mAdapter = new InstalledAppAdapter(getActivity(), new ArrayList<AppInfoEntity>() );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_installed_app_list, container, false);
        mProgressBar = (ProgressBar) view.findViewById(R.id.loading_apps);
        mProgressBar.setVisibility(mProgressBarVisibility);
        // Set the adapter
        mRecyclerView = (RecyclerView) view.findViewById(R.id.list);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        SetUpFabInstalled();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        VoiceNotificationActivity.CURRENT_FRAGMENT = this;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.installed_apps_menu, menu);
        mAdapter.onCreateMenu(menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_app_menu_item:
                AddSelectedAppsToDB();
                moveToFollowedFragment();
                return true;
            default:
                return false;
        }

    }

    private void AddSelectedAppsToDB() {
        List<AppInfoEntity> apps = mAdapter.getSelectedItems();
        if (!apps.isEmpty()) {
            DBHelper db = new DBHelper(getActivity());
            db.addApps(apps);
            db.close();
        }
    }
    private void moveToFollowedFragment()
    {
        getFragmentManager().popBackStack();
        getFragmentManager().beginTransaction()
                .replace(R.id.frame_content, new FollowedAppFragment())
                .commit();
    }

    private void SetUpFabInstalled() {
        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_apply_applications);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddSelectedAppsToDB();
                moveToFollowedFragment();
            }
        });
    }
    private class LoadApplicationsAsync extends AsyncTask<Void,Void,ArrayList<AppInfoEntity>>
    {

        @Override
        protected ArrayList<AppInfoEntity> doInBackground(Void... params) {

            ArrayList<AppInfoEntity> appsInfo = new ArrayList<>();

            PackageManager packageManager = getActivity().getPackageManager();

            List<ApplicationInfo> installedApplications =
                    packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

            DBHelper db = new DBHelper(getActivity());
            for (ApplicationInfo info : installedApplications) {
                boolean isFollowed = db.isAppFollowed(info.packageName);
                if (!isFollowed) {
                    AppInfoEntity appInfoEntity = new AppInfoEntity(info.packageName);
                    appInfoEntity.setApplicationLabel(Helper.getApplicationLabel(info.packageName, getActivity()));
                    appInfoEntity.setIsFollowed(isFollowed);
                    appsInfo.add(appInfoEntity);
                }
            }
            db.close();
            return appsInfo;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBarVisibility = View.VISIBLE;

            if(mProgressBar != null)
                mProgressBar.setVisibility(mProgressBarVisibility);

        }

        @Override
        protected void onPostExecute(ArrayList<AppInfoEntity> apps)
        {
            mProgressBarVisibility = View.GONE;
            mProgressBar.setVisibility(mProgressBarVisibility);
            mAdapter.addAll(apps);
            mAdapter.notifyDataSetChanged();
        }
    }
}