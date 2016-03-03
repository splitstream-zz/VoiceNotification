package org.stream.split.voicenotification.Fragments;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.stream.split.voicenotification.Adapters.SwipeViewsAdapter;
import org.stream.split.voicenotification.DataAccessLayer.DBHelper;
import org.stream.split.voicenotification.Enities.AppInfoEntity;
import org.stream.split.voicenotification.Enities.BundleKeyEntity;
import org.stream.split.voicenotification.Enities.NotificationEntity;
import org.stream.split.voicenotification.Interfaces.FabOwner;
import org.stream.split.voicenotification.R;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.

 */
public class ApplicationDetailsFragment extends BaseFragment implements FabOwner {

    public static final String TAG = "AppDetailsFragment";
    private static final String ARG_APP_GSON_OBJECT = "NotificationObject";
    private AppInfoEntity mEntity;

    private ViewPager mViewPager;
    private SwipeViewsAdapter mAdapter;
    BundleKeyListFragment mBundleKeysFragment;
    NotificationListFragment mNotificationFragment;

    private TextView mLabelTextView;
    private TextView mPackageNameTextView;
    private CheckBox mAddDeleteCbx;

    @Override
    public String getTAG() {
        return TAG;
    }

    public static ApplicationDetailsFragment newInstance(AppInfoEntity entity) {
        ApplicationDetailsFragment fragment = new ApplicationDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_APP_GSON_OBJECT, entity);
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

        AppInfoEntity entity = null;
        if (getArguments() != null) {
            entity = (AppInfoEntity) getArguments().getSerializable(ARG_APP_GSON_OBJECT);
        }

        Resources res = getResources();
        ArrayList<BundleKeyEntity> bundleKeyEntities = (ArrayList) entity.getBundleKeyList().get();
        mBundleKeysFragment = BundleKeyListFragment.newInstance(bundleKeyEntities);
        mBundleKeysFragment.setTitle(res.getString(R.string.BUNDLE_KEYS_FRAGMENT_TITLE));

        ArrayList<NotificationEntity> entities = (ArrayList) entity.getNotifications();
        mNotificationFragment = NotificationListFragment.newInstance(entities);
        mNotificationFragment.setTitle(res.getString(R.string.NOTIFICATIONS_FRAGMENT_TITLE));

        mAdapter = new SwipeViewsAdapter(getChildFragmentManager());
        mAdapter.AddView(mNotificationFragment, "Notifications");
        mAdapter.AddView(mBundleKeysFragment, "BundleKeys");


        setTitle(entity.getApplicationLabel());
        mEntity = entity;
    }
    @Override
     public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app_details, container, false);

        mLabelTextView = (TextView) view.findViewById(R.id.label_text);
        mPackageNameTextView = (TextView) view.findViewById(R.id.packagename_text);
        mAddDeleteCbx = (CheckBox) view.findViewById(R.id.add_delete_ImgBtn);
        mViewPager = (ViewPager) view.findViewById(R.id.pager);
        mViewPager.setAdapter(mAdapter);

        initialize(mEntity);
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

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void setUpFab(FloatingActionButton fab) {

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

    private String updateDatabase() {
        //TODO updateOrInsert update of adapters in swipe screens
        DBHelper db = new DBHelper(getActivity());
        StringBuilder snackBarText = new StringBuilder();
        mEntity.setNotifications(mNotificationFragment.getAdapter().getItems());
        mEntity.getBundleKeyList().set(mBundleKeysFragment.getAdapter().getItems());

        snackBarText.append(mEntity.getApplicationLabel());
        snackBarText.append(" ");
        if (mEntity.isFollowed()) {
            db.updateOrInsert(mEntity, true, true);
            snackBarText.append("has been modified");
        } else {
            db.delete(mEntity);
            snackBarText.append("has been deleted");
        }
        db.close();
        return snackBarText.toString();
    }

    @Override
    public boolean isModified() {
        return mEntity.isModified() || mAdapter.isModified();
    }

}
