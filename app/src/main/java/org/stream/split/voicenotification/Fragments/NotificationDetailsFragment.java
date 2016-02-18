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

import org.stream.split.voicenotification.Adapters.BundleKeysAdapter;
import org.stream.split.voicenotification.DataAccessLayer.DBHelper;
import org.stream.split.voicenotification.Enities.NotificationBundleKeyEntity;
import org.stream.split.voicenotification.Enities.NotificationEntity;
import org.stream.split.voicenotification.Helpers.Helper;
import org.stream.split.voicenotification.Helpers.SimpleItemTouchHelperCallback;
import org.stream.split.voicenotification.Interfaces.OnStartDragListener;
import org.stream.split.voicenotification.R;
import org.stream.split.voicenotification.VoiceNotificationActivity;

import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.

 */
public class NotificationDetailsFragment<T extends NotificationEntity> extends BaseFragment implements OnStartDragListener {

    public static String TAG = NotificationDetailsFragment.class.getSimpleName();

    private static final String ARG_NOTIFICATION_GSON_OBJECT = "NotificationObject";
    private static final String ARG_NOTIFICATION_GSON_TYPE = "NotificationType";

    private T mEntity;
    private ItemTouchHelper mItemTouchHelper;

    private TextView mLabelTextView;
    private TextView mPackageNameTextView;
    private TextView mNotificationSbnID;
    private CheckBox mAddDeleteCbx;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private BundleKeysAdapter mAdapter;

    public static <T extends NotificationEntity> NotificationDetailsFragment newInstance(T notificationEntity) {
        NotificationDetailsFragment<T> fragment = new NotificationDetailsFragment<>();
        Bundle args = new Bundle();
        args.putSerializable(ARG_NOTIFICATION_GSON_OBJECT, notificationEntity);
        //args.putString(ARG_NOTIFICATION_GSON_TYPE, notificationEntity.getClass().getName());
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
//            String className = getArguments().getString(ARG_NOTIFICATION_GSON_TYPE);
//            try {
//                Class x = Class.forName(className);
//                String gsonToJson = getArguments().getString(ARG_NOTIFICATION_GSON_OBJECT);
//                mEntity = (T) new Gson().fromJson(gsonToJson, x);
//            }
//            catch (ClassNotFoundException e) {
//                LOGGER.e(TAG,"Cannot get Class for notification object from string Bundle",e);
//            }
            mEntity = (T)getArguments().getSerializable(ARG_NOTIFICATION_GSON_OBJECT);
        }
        Helper.getAllNotificationBundleKeys(mEntity);
        mAdapter = new BundleKeysAdapter(mEntity.getBundleKeys(),this,getActivity());
        setTitle(Helper.getApplicationLabel(mEntity.getPackageName(), getActivity()));
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

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);

        mNotificationSbnID = (TextView) view.findViewById(R.id.notification_sbn_id_text);
        mLabelTextView = (TextView) view.findViewById(R.id.label_text);
        mPackageNameTextView = (TextView) view.findViewById(R.id.packagename_text);
        mAddDeleteCbx = (CheckBox) view.findViewById(R.id.add_delete_ImgBtn);

        initialize(mEntity);
        setUpFab();
        return view;
    }

    private void initialize(final T entity)
    {
        mNotificationSbnID.setText(String.valueOf(entity.getSbnId()));
        mLabelTextView.setText(Helper.getApplicationLabel(entity.getPackageName(), getActivity()));
        mPackageNameTextView.setText(entity.getPackageName());
        mAddDeleteCbx.setChecked(entity.isFollowed());
        mAddDeleteCbx.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                entity.setIsModified(true);
                entity.setIsFollowed(isChecked);
                if (isChecked)
                    mRecyclerView.setVisibility(View.VISIBLE);
                else
                    mRecyclerView.setVisibility(View.INVISIBLE);
            }
        });

        if (entity.isFollowed())
            mRecyclerView.setVisibility(View.VISIBLE);
        else
            mRecyclerView.setVisibility(View.INVISIBLE);

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

                String result  = updateDatabase(mEntity);
                Snackbar.make(v, result, Snackbar.LENGTH_SHORT).show();
                getFragmentManager().popBackStack();
            }
        });
    }

    private String updateDatabase(T entity)
    {
        DBHelper db = new DBHelper(getActivity());
        boolean isFallowed = db.isFollowed(entity);
        StringBuilder snackBarText = new StringBuilder();

        snackBarText.append(Helper.getApplicationLabel(entity.getPackageName(), getActivity()));
        snackBarText.append(" ");
        if(entity.isFollowed())
        {
            if(!isFallowed) {
                db.updateOrInsert(entity,false);
                snackBarText.append("has been added");
            }
            else
                snackBarText.append("has been modified");

            List<NotificationBundleKeyEntity> bundleKeyEntities = mAdapter.getModifiedItems();
            for(NotificationBundleKeyEntity entity1:bundleKeyEntities) {

                    if(entity.isFollowed())
                        db.updateOrInsert(entity1);
                    else
                        db.deleteFollowedBundleKey(entity1);
            }
        }
        else
        {
            if (isFallowed) {
                db.delete(entity);
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
