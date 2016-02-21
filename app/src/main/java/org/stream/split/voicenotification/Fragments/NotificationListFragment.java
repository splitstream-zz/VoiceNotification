package org.stream.split.voicenotification.Fragments;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.stream.split.voicenotification.Adapters.NotificationsAdapter;
import org.stream.split.voicenotification.Enities.NotificationEntity;
import org.stream.split.voicenotification.Helpers.Helper;
import org.stream.split.voicenotification.Interfaces.OnFragmentInteractionListener;
import org.stream.split.voicenotification.R;
import org.stream.split.voicenotification.VoiceNotificationActivity;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
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
public class NotificationListFragment extends BaseFragment {

    private final static String TAG = "NotListFrag";
    public final static String ARG_NOTIFICATION_LIST = "NotificationListArg";

    private RecyclerView mRecyclerView;
    private NotificationsAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public NotificationsAdapter getAdapter() {
        return mAdapter;
    }

    public NotificationListFragment() {
    }

    public static NotificationListFragment newInstance(
            ArrayList<NotificationEntity> notificationEntities)
    {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_NOTIFICATION_LIST, notificationEntities);

        NotificationListFragment fragment = new NotificationListFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        List entities = new ArrayList();
        if (getArguments() != null) {
            entities.addAll((List<NotificationEntity>) getArguments().getSerializable(ARG_NOTIFICATION_LIST));
        }

        mAdapter = new NotificationsAdapter(entities, getActivity());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification_list, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_notifications);

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
        Log.d(TAG, "onStart()");
        VoiceNotificationActivity.CURRENT_FRAGMENT = this;
        mAdapter.refresh();
    }

    @Override
    public boolean isModified() {
        return !mAdapter.getModifiedItems().isEmpty();
    }
}
