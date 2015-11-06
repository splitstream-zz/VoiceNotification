package org.stream.split.voicenotification;

import android.app.Activity;
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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.stream.split.voicenotification.BussinessLayer.AppInfo;
import org.stream.split.voicenotification.DataAccessLayer.DBHelper;

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
public class AppFragment extends Fragment implements AbsListView.OnItemClickListener {

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

    private ArrayList<AppInfo> mAppsList = new ArrayList<>();

    /**
     * progressBar indicating application loading
     */
    private ProgressBar mProgressBar;

    private MenuItem mDeleteMenuItem;

    Runnable mRefreshListView;

    public static AppFragment newInstance(APPLICATIONS_TO_SHOW x) {
        AppFragment fragment = new AppFragment();
        Bundle args = new Bundle();
        args.putString(ApplicationsToshow, x.name());
        fragment.setArguments(args);
        return fragment;

    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AppFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            mApplictaionsToShow = APPLICATIONS_TO_SHOW.valueOf(getArguments().getString(ApplicationsToshow));
        }

        mAdapter = new CustomListAdapter(getActivity(),R.layout.apps_layout,mAppsList);
        LoadApplicationsAsync loadingApps = new LoadApplicationsAsync();
        loadingApps.execute(mApplictaionsToShow);

        mRefreshListView = new Runnable() {
            @Override
            public void run() {
                mAdapter.clear();
                mAdapter.notifyDataSetChanged();
                mListView.invalidateViews();
                mListView.refreshDrawableState();
            }
        };

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app, container, false);
        mProgressBar = (ProgressBar) view.findViewById(R.id.loading_apps);

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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    //TODO design custom menu accordingly to applications shown
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
            case R.id.action_settings:
                Snackbar.make(getView().getRootView(),"AppFragment settings pressed",Snackbar.LENGTH_SHORT).show();
                return true;
            case R.id.delete_app:

                DBHelper db = new DBHelper(getActivity());
                for(int i = 0;i< mAdapter.getCount();i++)
                {
                    AppInfo app = mAdapter.getItem(i);
                    if(app.IsSelected())
                    {
                        db.deleteApp(app);
                    }

                }
                new LoadApplicationsAsync().execute(mApplictaionsToShow);
                return true;
            default:
                return false;
        }

    }

    // TODO find a way to change appbar accordingly to what is chosen or what kind of list is shown
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
        fab.setImageResource(R.drawable.ic_fab_plus);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
                AppFragment fragment = newInstance(APPLICATIONS_TO_SHOW.SHOW_INSTALLED);
                ft.replace(R.id.frame_content, fragment).commit();
            }
        });
    }

    //TODO implement adding checked positions in listview to database
    private void SetUpFabInstalled()
    {
        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_add_applications);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DBHelper db = new DBHelper(getActivity());
                for(int i = 0;i< mAdapter.getCount();i++)
                {
                    AppInfo app = mAdapter.getItem(i);
                    if(app.IsSelected()) {
                        long row  = db.addApp(app);
                        Log.d(TAG, "dodano applikacje do bazy danych: " + app.getPackageName() +" row#: " + row);
                    }
                }
                FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
                AppFragment fragment = newInstance(APPLICATIONS_TO_SHOW.SHOW_FOLLOWED);
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(long[] id);
    }

    private class CustomListAdapter extends ArrayAdapter<AppInfo>
    {

        public CustomListAdapter(Context context, int resource, ArrayList<AppInfo> objects) {
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
                convertView = vi.inflate(R.layout.apps_layout,null);

                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.app_name);
                holder.icon = (ImageView) convertView.findViewById(R.id.app_icon);
                holder.cbx = (CheckBox) convertView.findViewById(R.id.app_cbx);

                convertView.setTag(holder);


            }
            else
                holder = (ViewHolder) convertView.getTag();
            PackageManager manager = getActivity().getPackageManager();

            AppInfo appInfo = this.getItem(position);
            holder.name.setText(appInfo.getPackageName());
            holder.cbx.setChecked(appInfo.IsSelected());
            try {
                holder.icon.setImageDrawable(manager.getApplicationIcon(appInfo.getPackageName()));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            holder.name.setTag(appInfo);
            holder.cbx.setTag(appInfo);


            holder.cbx.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppInfo appInfo = (AppInfo) v.getTag();
                    appInfo.setSelected(((CheckBox)v).isChecked());
                    Boolean setDeleteVisibility = false;
                    if(mApplictaionsToShow == APPLICATIONS_TO_SHOW.SHOW_FOLLOWED ) {
                        for(int i = 0;i< mAdapter.getCount();i++) {
                            AppInfo app = mAdapter.getItem(i);
                            if (app.IsSelected()) {
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
                    AppInfo appInfo = (AppInfo) v.getTag();
                    Snackbar.make(parent, "Clicked on Checkbox: " + appInfo.getPackageName() + " is " + appInfo.IsSelected(),
                            Snackbar.LENGTH_SHORT).show();
                }
            });

            return convertView;

        }
    }

    private class LoadApplicationsAsync extends AsyncTask<APPLICATIONS_TO_SHOW,Void,ArrayList<AppInfo>>
    {

        @Override
        protected ArrayList<AppInfo> doInBackground(APPLICATIONS_TO_SHOW... params) {

            ArrayList<AppInfo> appsInfo = new ArrayList<>();
            DBHelper db = new DBHelper(getActivity());

            switch (params[0]) {
                case SHOW_INSTALLED:
                    PackageManager packageManager = getActivity().getPackageManager();
                    List<ApplicationInfo> instaledApplications =
                            packageManager.getInstalledApplications(PackageManager.GET_META_DATA);


                    for (ApplicationInfo info : instaledApplications) {
                        if(!db.isAppFollowed(info.packageName)) {
                            AppInfo appInfo = new AppInfo();
                            appInfo.setPackageName(info.packageName);
                            appsInfo.add(appInfo);
                        }
                    }
                    break;

                case SHOW_FOLLOWED:
                    appsInfo.addAll(db.getAllApps());
                    break;
            }

            return appsInfo;
        }

        @Override
        protected void onPostExecute(ArrayList<AppInfo> apps)
        {
            mProgressBar.setVisibility(View.GONE);
            mAdapter.clear();
            mAdapter.addAll(apps);
            mAdapter.notifyDataSetInvalidated();


//            mAdapter = new CustomListAdapter(getActivity().getApplication(),R.layout.apps_layout,apps);
//            // Set the adapter
//            mListView = (ListView) getActivity().findViewById(android.R.id.list);
//            ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);
//
//            // Set OnItemClickListener so we can be notified on item clicks
//            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    AppInfo info = (AppInfo) parent.getItemAtPosition(position);
//                    Snackbar.make(parent, "Clicked on row: " + info.getPackageName(), Snackbar.LENGTH_SHORT).show();
//                }
//            });

        }
    }

}
