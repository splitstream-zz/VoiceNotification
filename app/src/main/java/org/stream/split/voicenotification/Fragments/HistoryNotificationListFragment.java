package org.stream.split.voicenotification.Fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

import org.stream.split.voicenotification.Adapters.NotificationsAdapter;
import org.stream.split.voicenotification.Enities.HistoryNotificationEntity;
import org.stream.split.voicenotification.Enities.NotificationEntity;
import org.stream.split.voicenotification.Helpers.NotificationServiceConnection;
import org.stream.split.voicenotification.NotificationService;
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
public class HistoryNotificationListFragment extends BaseFragment {

    private final static String TAG = "HistoryNotificationListFragment";
    public final static String ARG_NOTIFICATION_LIST = "NotificationListArg";

    private NotifyBroadcastReceiver mReceiver;
    private NotificationServiceConnection mConnection;
    private RecyclerView mRecyclerView;
    private NotificationsAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public NotificationsAdapter getAdapter() {
        return mAdapter;
    }

    public HistoryNotificationListFragment() {
    }

    public static HistoryNotificationListFragment newInstance(
            ArrayList<HistoryNotificationEntity> notificationEntities)
    {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_NOTIFICATION_LIST, notificationEntities);

        HistoryNotificationListFragment fragment = new HistoryNotificationListFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(getTitle());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        List entities = new ArrayList();
        if (getArguments() != null) {
            entities = (List<HistoryNotificationEntity>) getArguments().getSerializable(ARG_NOTIFICATION_LIST);
        }

        mAdapter = new NotificationsAdapter(entities, getActivity());

        mReceiver = new NotifyBroadcastReceiver();
        mConnection = NotificationServiceConnection.getInstance();

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
        mConnection.registerReceiver(mReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG,"onPause()");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
        mConnection.unregisterReceiver(mReceiver);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        LOGGER.d(TAG, "onDetach()");

    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
//                     View emptyView = mRecyclerView.
//
//        if (emptyView instanceof TextView) {
//            ((TextView) emptyView).setText(emptyText);
//        }
    }

    public class NotifyBroadcastReceiver extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent) {
            LOGGER.d(TAG, "HISTORY FRAGMENT onReceive()");
            Bundle extras = intent.getExtras();
            String gsonToJson;
            if(extras != null) {
                gsonToJson = extras.getString(NotificationService.NEW_NOTIFICATION_OBJECT);
                HistoryNotificationEntity historyNotificationEntity = new Gson().fromJson(gsonToJson, HistoryNotificationEntity.class);
                getAdapter().addItem(historyNotificationEntity);
            }
        }
    }
}
