package org.stream.split.voicenotification.Fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
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
import org.stream.split.voicenotification.Adapters.NotificationsAdapter;
import org.stream.split.voicenotification.DataAccessLayer.DBHelper;
import org.stream.split.voicenotification.Enities.AppInfoEntity;
import org.stream.split.voicenotification.Enities.BundleKeyEntity;
import org.stream.split.voicenotification.Helpers.SimpleItemTouchHelperCallback;
import org.stream.split.voicenotification.R;
import org.stream.split.voicenotification.VoiceNotificationActivity;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.

 */
public class ApplicationDetailsFragment extends BaseFragment {

    private static final String ARG_APP_GSON_OBJECT = "NotificationObject";
    private AppInfoEntity mEntity;

    private ViewPager mViewPager;
    private BundleKeysAdapter mBundleKeysAdapter;
    private NotificationsAdapter mNotificationAdapter;
    private TextView mLabelTextView;
    private TextView mPackageNameTextView;
    private CheckBox mAddDeleteCbx;


    public static ApplicationDetailsFragment newInstance(String gsonNotificationEntity) {
        ApplicationDetailsFragment fragment = new ApplicationDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_APP_GSON_OBJECT, gsonNotificationEntity);
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
            String gsonToJson = getArguments().getString(ARG_APP_GSON_OBJECT);
            mEntity = new Gson().fromJson(gsonToJson, AppInfoEntity.class);
        }

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
        View view = inflater.inflate(R.layout.fragment_app_details, container, false);

        mLabelTextView = (TextView) view.findViewById(R.id.label_text);
        mPackageNameTextView = (TextView) view.findViewById(R.id.packagename_text);
        mAddDeleteCbx = (CheckBox) view.findViewById(R.id.add_delete_ImgBtn);
        mViewPager = (ViewPager) view.findViewById(R.id.pager);

        initialize(mEntity);
        setUpFab();
        return view;
    }
    private void initialize(final AppInfoEntity entity)
    {
        mLabelTextView.setText(entity.getApplicationLabel());
        mPackageNameTextView.setText(entity.getPackageName());
        mAddDeleteCbx.setChecked(entity.isFollowed());
        mAddDeleteCbx.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                entity.setIsModified(true);
                entity.setIsFollowed(isChecked);
            }
        });
        mViewPager.
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

                String result = updateDatabase();
                Snackbar.make(v, result, Snackbar.LENGTH_SHORT).show();
                getFragmentManager().popBackStack();
            }
        });
    }

    private String updateDatabase()
    {
        DBHelper db = new DBHelper(getActivity());
        boolean isFallowed = db.isFollowed(mEntity.getPackageName());
        StringBuilder snackBarText = new StringBuilder();

        snackBarText.append(mEntity.getApplicationLabel());
        snackBarText.append(" ");
        if(mEntity.isFollowed())
        {
            if(!isFallowed) {
                db.add(mEntity);
                snackBarText.append("has been added");
            }
            else
                snackBarText.append("has been modified");

        }
        else
        {
            if (isFallowed) {
                db.delete(mEntity);
                snackBarText.append("has been deleted");
            }
        }
        db.close();
        return snackBarText.toString();
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
