package org.stream.split.voicenotification.Fragments;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.stream.split.voicenotification.Enities.AppInfoEntity;
import org.stream.split.voicenotification.DataAccessLayer.DBHelper;
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
public class AppListFragment extends Fragment implements AbsListView.OnItemClickListener {

    private final String TAG = this.getClass().getSimpleName();

    private static final String ApplicationsToshow = "ApplicationsToshow";

    private APPLICATIONS_TO_SHOW mApplictaionsToShow;

    private OnFragmentInteractionListener mListener;


    public enum APPLICATIONS_TO_SHOW
    {
        /**
         * show installed applications on device
         */
        SHOW_INSTALLED,
        /**
         * show applications added to notification reading
         */
        SHOW_FOLLOWED
    }

    /**
     * The fragment's ListView/GridView.
     */
    private ListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private CustomListAdapter mAdapter;

    private ArrayList<AppInfoEntity> mAppsList = new ArrayList<>();

    /**
     * progressBar indicating application loading
     */
    private ProgressBar mProgressBar;
    private int mPrograsBarVisibility;

    private MenuItem mDeleteMenuItem;

    Runnable mRefreshListView;

    public static AppListFragment newInstance(APPLICATIONS_TO_SHOW x) {
        AppListFragment fragment = new AppListFragment();
        Bundle args = new Bundle();
        args.putString(ApplicationsToshow, x.name());
        fragment.setArguments(args);
        return fragment;

    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AppListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            mApplictaionsToShow = APPLICATIONS_TO_SHOW.valueOf(getArguments().getString(ApplicationsToshow));
        }

        mAdapter = new CustomListAdapter(getActivity(), R.layout.fragment_app_item,mAppsList);
        LoadApplicationsAsync loadingApps = new LoadApplicationsAsync();
        loadingApps.execute(mApplictaionsToShow);
        mPrograsBarVisibility = View.VISIBLE;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app, container, false);
        mProgressBar = (ProgressBar) view.findViewById(R.id.loading_apps);
        mProgressBar.setVisibility(mPrograsBarVisibility);
        // Set the adapter
        mListView = (ListView) view.findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);

        switch (mApplictaionsToShow) {
            case SHOW_FOLLOWED:
                SetUpFabFollowed();
                break;
            case SHOW_INSTALLED:
                SetUpFabInstalled();
                break;

        }

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
        //super.onCreateOptionsMenu(menu, inflater);
        switch (mApplictaionsToShow) {
            case SHOW_FOLLOWED:
                inflater.inflate(R.menu.followed_apps_menu, menu);
                break;
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        mDeleteMenuItem = menu.findItem(R.id.delete_app);
        if(mDeleteMenuItem != null)
            mDeleteMenuItem.setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.delete_app:

                DBHelper db = new DBHelper(getActivity());
                for(int i = 0;i< mAdapter.getCount();i++)
                {
                    AppInfoEntity app = mAdapter.getItem(i);
                    if(app.isSelected())
                    {
                        db.deleteApp(app);
                    }

                }
                db.close();
                new LoadApplicationsAsync().execute(mApplictaionsToShow);
                mDeleteMenuItem.setVisible(false);

                return true;
            default:
                return false;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            //Snackbar.make(view,mListView.getCheckedItemCount(),Snackbar.LENGTH_SHORT).show();
            mListener.onFragmentInteraction(new long[]{1,2,3} );
        }
    }

    private void SetUpFabFollowed()
    {
        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_add_applications);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
                AppListFragment fragment = newInstance(APPLICATIONS_TO_SHOW.SHOW_INSTALLED);
                ft.replace(R.id.frame_content, fragment).addToBackStack("kleks").commit();
            }
        });
    }

    private void SetUpFabInstalled()
    {
        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_apply_applications);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DBHelper db = new DBHelper(getActivity());
                for(int i = 0;i< mAdapter.getCount();i++)
                {
                    AppInfoEntity app = mAdapter.getItem(i);
                    if(app.isSelected()) {
                        long row  = db.addApp(app);
                        Log.d(TAG, "dodano applikacje do bazy danych: " + app.getPackageName() +" row#: " + row);
                    }
                }
                db.close();
                FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
                AppListFragment fragment = newInstance(APPLICATIONS_TO_SHOW.SHOW_FOLLOWED);
                ft.replace(R.id.frame_content, fragment).commit();
            }
        });
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    private class CustomListAdapter extends ArrayAdapter<AppInfoEntity>
    {

        public CustomListAdapter(Context context, int resource, ArrayList<AppInfoEntity> objects) {
            super(context, resource, objects);
            this.addAll(objects);

        }

        private class ViewHolder
        {
            ImageView icon;
            TextView name;
            CheckBox cbx;

        }
        @Override
        public View getView(int position, View convertView, final ViewGroup parent)
        {
            ViewHolder holder;

            if(convertView == null)
            {
                LayoutInflater vi =  (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.fragment_app_item,null);

                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.app_name);
                holder.icon = (ImageView) convertView.findViewById(R.id.app_icon);
                holder.cbx = (CheckBox) convertView.findViewById(R.id.app_cbx);

                convertView.setTag(holder);


            }
            else
                holder = (ViewHolder) convertView.getTag();
            PackageManager manager = getActivity().getPackageManager();

            AppInfoEntity appInfoEntity = this.getItem(position);
            holder.name.setText(Helper.getApplicationLabel(appInfoEntity.getPackageName(),getContext()));
            holder.cbx.setChecked(appInfoEntity.isSelected());
            try {
                holder.icon.setImageDrawable(manager.getApplicationIcon(appInfoEntity.getPackageName()));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            holder.name.setTag(appInfoEntity);
            holder.cbx.setTag(appInfoEntity);


            holder.cbx.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppInfoEntity appInfoEntity = (AppInfoEntity) v.getTag();
                    appInfoEntity.setSelected(((CheckBox)v).isChecked());
                    Boolean setDeleteVisibility = false;
                    if(mApplictaionsToShow == APPLICATIONS_TO_SHOW.SHOW_FOLLOWED ) {
                        for(int i = 0;i< mAdapter.getCount();i++) {
                            AppInfoEntity app = mAdapter.getItem(i);
                            if (app.isSelected()) {
                                setDeleteVisibility = true;
                                break;
                            }
                        }
                        mDeleteMenuItem.setVisible(setDeleteVisibility);
                    }
                }
            });

            holder.name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppInfoEntity appInfoEntity = (AppInfoEntity) v.getTag();
                    Snackbar.make(parent, "Clicked on Checkbox: " + appInfoEntity.getPackageName() + " is " + appInfoEntity.isSelected(),
                            Snackbar.LENGTH_SHORT).show();
                }
            });

            return convertView;

        }
    }

    private class LoadApplicationsAsync extends AsyncTask<APPLICATIONS_TO_SHOW,Void,ArrayList<AppInfoEntity>>
    {

        @Override
        protected ArrayList<AppInfoEntity> doInBackground(APPLICATIONS_TO_SHOW... params) {

            ArrayList<AppInfoEntity> appsInfo = new ArrayList<>();
            DBHelper db = new DBHelper(getActivity());

            switch (params[0]) {
                case SHOW_INSTALLED:
                    PackageManager packageManager = getActivity().getPackageManager();

                    List<ApplicationInfo> instaledApplications =
                            packageManager.getInstalledApplications(PackageManager.GET_META_DATA);


                    for (ApplicationInfo info : instaledApplications) {
                        if(!db.isAppFollowed(info.packageName)) {
                            AppInfoEntity appInfoEntity = new AppInfoEntity(info.packageName);
                            appsInfo.add(appInfoEntity);
                        }
                    }
                    break;

                case SHOW_FOLLOWED:
                    appsInfo.addAll(db.getAllApps(true));
                    break;
            }
            db.close();
            return appsInfo;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPrograsBarVisibility = View.VISIBLE;
            if(mProgressBar != null)
                mProgressBar.setVisibility(mPrograsBarVisibility);

        }

        @Override
        protected void onPostExecute(ArrayList<AppInfoEntity> apps)
        {
            mPrograsBarVisibility = View.GONE;
            mProgressBar.setVisibility(mPrograsBarVisibility);

            mAdapter.clear();
            mAdapter.addAll(apps);
            mAdapter.notifyDataSetInvalidated();

        }
    }

}
