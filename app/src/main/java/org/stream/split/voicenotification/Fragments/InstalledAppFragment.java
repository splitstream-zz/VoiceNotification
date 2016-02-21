package org.stream.split.voicenotification.Fragments;

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
import org.stream.split.voicenotification.Interfaces.OnFragmentInteractionListener;
import org.stream.split.voicenotification.Interfaces.FabOwner;
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
public class InstalledAppFragment extends BaseFragment implements FabOwner {

    private final String TAG = this.getClass().getSimpleName();
    private RecyclerView mRecyclerView;
    private InstalledAppAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private LoadApplicationsAsync loading;


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
        loading = new LoadApplicationsAsync();
        loading.execute();
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

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        VoiceNotificationActivity.CURRENT_FRAGMENT = this;
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
                getFragmentManager().popBackStack();
                return true;
            default:
                return false;
        }

    }
    @Override
    public void setUpFab(FloatingActionButton fab) {
        fab.setImageResource(R.drawable.ic_apply_applications);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(loading.getStatus() == AsyncTask.Status.RUNNING) {
                    finish();
                }
                else {
                    AddSelectedAppsToDB();
                }
                getFragmentManager().popBackStack();
            }
        });
    }

    private void AddSelectedAppsToDB() {
        List<AppInfoEntity> apps = mAdapter.getSelectedItems();
        if (!apps.isEmpty()) {
            DBHelper db = new DBHelper(getActivity());
            db.updateOrInsert(apps,false,false);
            db.close();
        }
    }

    @Override
    public boolean isModified() {

        return !mAdapter.getSelectedItems().isEmpty();
    }

    @Override
    public void finish() {
        super.finish();
        loading.cancel(true);
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
            List<AppInfoEntity> followed = db.getAllFollowedApps(false,false);
            db.close();

            for (ApplicationInfo info : installedApplications) {
                boolean isFollowed = false;
                for(AppInfoEntity f:followed)
                {
                    if(f.getPackageName().equals(info.packageName)) {
                        isFollowed = true;
                        break;
                    }
                }

                if(!isFollowed) {
                    AppInfoEntity appInfoEntity = new AppInfoEntity(info.packageName,
                            info.loadLabel(packageManager).toString());
                    appsInfo.add(appInfoEntity);
                }
            }
            return appsInfo;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setIsModified(true);
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
            setIsModified(false);
        }
    }
}