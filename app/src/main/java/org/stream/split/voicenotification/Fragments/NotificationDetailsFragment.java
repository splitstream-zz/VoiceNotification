package org.stream.split.voicenotification.Fragments;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.gson.Gson;

import org.stream.split.voicenotification.Adapters.NotificationDetailsAdapter;
import org.stream.split.voicenotification.DataAccessLayer.DBHelper;
import org.stream.split.voicenotification.Enities.AppInfoEntity;
import org.stream.split.voicenotification.Enities.BundleKeyEntity;
import org.stream.split.voicenotification.Enities.NotificationEntity;
import org.stream.split.voicenotification.Helpers.SimpleItemTouchHelperCallback;
import org.stream.split.voicenotification.Interfaces.OnStartDragListener;
import org.stream.split.voicenotification.R;
import org.stream.split.voicenotification.VoiceNotificationActivity;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.

 */
public class NotificationDetailsFragment extends Fragment implements OnStartDragListener {

    private static final String ARG_NOTIFICATION_GSON_OBJECT = "NotificationObject";
    private NotificationEntity mNotificationEntity;
    private ItemTouchHelper mItemTouchHelper;

    private TextView mLabelTextView;
    private TextView mPackageNameTextView;
    private CheckBox mAddDeleteCbx;

    /**
     * The fragment's ListView/GridView.
     */
    private RecyclerView mRecyclerView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private NotificationDetailsAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public static NotificationDetailsFragment newInstance(String gsonNotificationEntity) {
        NotificationDetailsFragment fragment = new NotificationDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NOTIFICATION_GSON_OBJECT, gsonNotificationEntity);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NotificationDetailsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (getArguments() != null) {
            String gsonToJson = getArguments().getString(ARG_NOTIFICATION_GSON_OBJECT);
            mNotificationEntity = new Gson().fromJson(gsonToJson, NotificationEntity.class);
        }

        mAdapter = new NotificationDetailsAdapter(mNotificationEntity.getBundleKeys(),this,getActivity());
    }
    @Override
    public void onStart() {
        super.onStart();
        VoiceNotificationActivity.CURRENT_FRAGMENT = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification_details, container, false);

        // Set the adapter
        mRecyclerView = (RecyclerView) view.findViewById(R.id.list_budlekeys);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mLabelTextView = (TextView) view.findViewById(R.id.label_text);
        mLabelTextView.setText(mNotificationEntity.getApplicationLabel());

        mPackageNameTextView = (TextView) view.findViewById(R.id.packagename_text);
        mPackageNameTextView.setText(mNotificationEntity.getPackageName());

        mAddDeleteCbx = (CheckBox) view.findViewById(R.id.add_delete_ImgBtn);
        mAddDeleteCbx.setChecked(mNotificationEntity.isFollowed());
        mAddDeleteCbx.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mNotificationEntity.setIsFollowed(isChecked);
                if(isChecked)
                    mRecyclerView.setVisibility(View.VISIBLE);
                else
                    mRecyclerView.setVisibility(View.INVISIBLE);
            }
        });

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
        if(mNotificationEntity.isFollowed())
            mRecyclerView.setVisibility(View.VISIBLE);
        else
            mRecyclerView.setVisibility(View.INVISIBLE);
        setUpFab();
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

//    /**
//     * The default content for this Fragment has a TextView that is shown when
//     * the list is empty. If you would like to change the text, call this method
//     * to supply the text it should use.
//     */
//    public void setEmptyText(CharSequence emptyText) {
//        View emptyView = mListView.getEmptyView();
//
//        if (emptyView instanceof TextView) {
//            ((TextView) emptyView).setText(emptyText);
//        }
//    }

    void setUpFab() {

        android.support.design.widget.FloatingActionButton fab = (android.support.design.widget.FloatingActionButton) getActivity().findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_apply_applications);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateDatabase();
                Snackbar.make(v, "Application has been added", Snackbar.LENGTH_SHORT).show();

                getFragmentManager().beginTransaction()
                        .replace(R.id.frame_content, new NotificationsHistoryFragment())
                        .commit();
            }
        });
    }

    private void updateDatabase()
    {
        DBHelper db = new DBHelper(getActivity());
        boolean isFallowed = db.isAppFollowed(mNotificationEntity.getPackageName());

        if(mNotificationEntity.isFollowed())
        {
            if(!isFallowed)
                db.addApp(new AppInfoEntity(mNotificationEntity.getPackageName()));

            for(BundleKeyEntity entity:mAdapter.getModifiedItems()) {

                    if(entity.isFollowed())
                        db.addUpdateBundleKey(entity);
                    else
                        db.deleteBundleKey(entity);
            }
        }
        else
        {
            if (isFallowed)
                db.deleteApp(new AppInfoEntity(mNotificationEntity.getPackageName()), true);
        }
        db.close();
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
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
        public void onFragmentInteraction(String id);
    }

}
