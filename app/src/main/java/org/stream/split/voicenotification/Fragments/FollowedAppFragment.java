package org.stream.split.voicenotification.Fragments;

import android.app.FragmentTransaction;
import android.content.Context;
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

import org.stream.split.voicenotification.Adapters.FollowedAppAdapter;
import org.stream.split.voicenotification.DataAccessLayer.DBHelper;
import org.stream.split.voicenotification.Enities.AppInfoEntity;
import org.stream.split.voicenotification.Interfaces.OnFragmentInteractionListener;
import org.stream.split.voicenotification.Interfaces.FabOwner;
import org.stream.split.voicenotification.R;
import org.stream.split.voicenotification.VoiceNotificationActivity;

import java.util.ArrayList;
import java.util.List;

//todo updateOrInsert empty listview textview  recommend to updateOrInsert new apps to followed list
/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class FollowedAppFragment extends BaseFragment implements FabOwner {

    private final String TAG = this.getClass().getSimpleName();
    private RecyclerView mRecyclerView;
    private FollowedAppAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    /**
     * progressBar indicating application loading
     */
    private ProgressBar mProgressBar;
    private int mProgressBarVisibility;
    private MenuItem mDeleteMenuItem;

    @Override
    public String getTAG() {
        return TAG;
    }
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FollowedAppFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_followed_app_list, container, false);
        mAdapter = new FollowedAppAdapter(getActivity(), new ArrayList<AppInfoEntity>() );
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
        DBHelper db = new DBHelper(getActivity());
        List<AppInfoEntity> apps = db.getAllFollowedApps(true,true);
        db.close();
        mAdapter.addAll(apps);
        mProgressBarVisibility = View.GONE;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.followed_apps_menu, menu);
        mDeleteMenuItem = menu.findItem(R.id.delete_app_menu_item);
        mAdapter.onCreateMenu(menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_app_menu_item:
                deleteSelectedApps();
                mAdapter.refresh();
                mDeleteMenuItem.setVisible(!mAdapter.getSelectedItems().isEmpty());
                return true;
            default:
                return true;
    }

    }

    private void deleteSelectedApps()
    {
        List<AppInfoEntity> apps = mAdapter.getSelectedItems();
        if (!apps.isEmpty()) {
            DBHelper db = new DBHelper(getActivity());
            db.delete(apps);
            db.close();
        }
    }

    @Override
    public void setUpFab(FloatingActionButton fab) {
        fab.setImageResource(R.drawable.ic_add_applications);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
                InstalledAppFragment fragment = new InstalledAppFragment();
                ft.replace(R.id.frame_content, fragment)
                        .addToBackStack("updateOrInsert App to followed")
                        .commit();
            }
        });
    }

    @Override
    public boolean isModified() {
        return !mAdapter.getSelectedItems().isEmpty();
    }
}