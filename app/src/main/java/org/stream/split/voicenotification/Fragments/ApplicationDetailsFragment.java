package org.stream.split.voicenotification.Fragments;

import android.app.Activity;
import android.os.Bundle;
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

import org.stream.split.voicenotification.Adapters.ApplicationDetailsAdapter;
import org.stream.split.voicenotification.DataAccessLayer.DBHelper;
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
public class ApplicationDetailsFragment extends BaseFragment implements OnStartDragListener {

    private static final String ARG_NOTIFICATION_GSON_OBJECT = "NotificationObject";
    private NotificationEntity mEntity;
    private ItemTouchHelper mItemTouchHelper;

    private TextView mLabelTextView;
    private TextView mPackageNameTextView;
    private TextView mNotificationSbnID;
    private CheckBox mAddDeleteCbx;

    /**
     * The fragment's ListView/GridView.
     */
    private RecyclerView mRecyclerView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ApplicationDetailsAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public static ApplicationDetailsFragment newInstance(String gsonNotificationEntity) {
        ApplicationDetailsFragment fragment = new ApplicationDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NOTIFICATION_GSON_OBJECT, gsonNotificationEntity);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ApplicationDetailsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (getArguments() != null) {
            String gsonToJson = getArguments().getString(ARG_NOTIFICATION_GSON_OBJECT);
            mEntity = new Gson().fromJson(gsonToJson, NotificationEntity.class);
        }

        mAdapter = new ApplicationDetailsAdapter(mEntity.getBundleKeys(),this,getActivity());
        setTitle(mEntity.getApplicationLabel());
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

        mNotificationSbnID = (TextView) view.findViewById(R.id.notification_sbn_id_text);

        if(mEntity.getSbnId() != -1)
        {
            mNotificationSbnID.setText(String.valueOf(mEntity.getSbnId()));
            mNotificationSbnID.setVisibility(View.VISIBLE);
        }

        mLabelTextView = (TextView) view.findViewById(R.id.label_text);
        mLabelTextView.setText(mEntity.getApplicationLabel());

        mPackageNameTextView = (TextView) view.findViewById(R.id.packagename_text);
        mPackageNameTextView.setText(mEntity.getPackageName());

        mAddDeleteCbx = (CheckBox) view.findViewById(R.id.add_delete_ImgBtn);
        mAddDeleteCbx.setChecked(mEntity.isFollowed());
        mAddDeleteCbx.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mEntity.setIsModified(true);
                mEntity.setIsFollowed(isChecked);
                if (isChecked)
                    mRecyclerView.setVisibility(View.VISIBLE);
                else
                    mRecyclerView.setVisibility(View.INVISIBLE);
            }
        });

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter);

        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
        if(mEntity.isFollowed())
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

    void setUpFab() {

        android.support.design.widget.FloatingActionButton fab = (android.support.design.widget.FloatingActionButton) getActivity().findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_apply_applications);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String result  = updateDatabase();
                Snackbar.make(v, result, Snackbar.LENGTH_SHORT).show();
                getFragmentManager().popBackStack();
            }
        });
    }

    private String updateDatabase()
    {
        DBHelper db = new DBHelper(getActivity());
        boolean isFallowed = db.isAppFollowed(mEntity.getPackageName());
        StringBuilder snackBarText = new StringBuilder();

        snackBarText.append(mEntity.getApplicationLabel());
        snackBarText.append(" ");
        if(mEntity.isFollowed())
        {
            if(!isFallowed) {
                db.addApp(mEntity);
                snackBarText.append("has been added");
            }
            else
                snackBarText.append("has been modified");

            for(BundleKeyEntity entity:mAdapter.getModifiedItems()) {

                    if(entity.isFollowed())
                        db.addUpdateBundleKey(entity);
                    else
                        db.deleteFollowedBundleKey(entity);
            }
        }
        else
        {
            if (isFallowed) {
                db.deleteApp(mEntity, true);
                snackBarText.append("has been deleted");
            }
        }
        db.close();
        return snackBarText.toString();
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public boolean isModified() {
        boolean isModified = false;
        if(mEntity.isModified())
            isModified = true;
        if(!mAdapter.getModifiedItems().isEmpty())
            isModified = true;
        return isModified;
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
