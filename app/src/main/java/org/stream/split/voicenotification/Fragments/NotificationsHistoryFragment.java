package org.stream.split.voicenotification.Fragments;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

import org.stream.split.voicenotification.DataAccessLayer.DBHelper;
import org.stream.split.voicenotification.Enities.NotificationEntity;
import org.stream.split.voicenotification.Helpers.Helper;
import org.stream.split.voicenotification.Helpers.NotificationServiceConnection;
import org.stream.split.voicenotification.NotificationService;
import org.stream.split.voicenotification.Adapters.NotificationsHistoryAdapter;
import org.stream.split.voicenotification.Interfaces.OnFragmentInteractionListener;
import org.stream.split.voicenotification.R;
import org.stream.split.voicenotification.VoiceNotificationActivity;

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
public class NotificationsHistoryFragment extends BaseFragment {

    private final static String TAG = "NotificationsHistoryFragment";
    private final int mTestingNotificationID = 6879;

    private RecyclerView mRecyclerView;
    private NotificationsHistoryAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private NotifyBroadcastReceiver mReceiver;
    private OnFragmentInteractionListener mListener;
    private NotificationServiceConnection mConnection;
    private NotificationManager mNotificationManager;


    public NotificationsHistoryFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(getTitle());

        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        mNotificationManager =(NotificationManager) getActivity().getSystemService(Activity.NOTIFICATION_SERVICE);
        DBHelper db = new DBHelper(getActivity());
        List<NotificationEntity> entities = db.getAllNotification(false);
        db.close();
        mAdapter = new NotificationsHistoryAdapter(entities,getActivity());
        mReceiver = new NotifyBroadcastReceiver();
        mConnection = NotificationServiceConnection.getInstance();
        mConnection.registerReceiver(mReceiver);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_list, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_notification_history);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.setAdapter(mAdapter);

        setUpFab();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"onStart()");
        VoiceNotificationActivity.CURRENT_FRAGMENT = this;
        mAdapter.refresh();

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
        mListener = null;

    }

//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        if (null != mListener) {
//            // Notify the active callbacks interface (the activity, if the
//            // fragment is attached to one) that an item has been selected.
//            mListener.onFragmentInteraction("sda");
//        }
//    }

    /***
     * setting up Floating Action button to issue notification for testing purposes
     */
    void setUpFab()
    {

        android.support.design.widget.FloatingActionButton fab = (android.support.design.widget.FloatingActionButton) getActivity().findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_test_notification);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), VoiceNotificationActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 01, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                Notification notification = Helper.createNotification(getActivity(), pendingIntent, "Tytuł", "Na tydzień przed wyborami parlamentarnymi Andrzej Duda był gościem specjalnego wydania programu \"Kawa na ławę\". Bogdan Rymanowski pytał prezydenta m.in. o relacje z rządem, politykę zagraniczną i ocenę dobiegającej końca kampanii wyborczej.", "subtext", false);
                mNotificationManager.notify(mTestingNotificationID, notification);
                Snackbar.make(v, "test notification was send ", Snackbar.LENGTH_SHORT).show();

            }
        });
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
                gsonToJson = extras.getString(NotificationService.NOTIFICATION_OBJECT);
                NotificationEntity notificationEntity = new Gson().fromJson(gsonToJson, NotificationEntity.class);

                mAdapter.addItem(notificationEntity);
                mAdapter.refresh();
            }
        }
    }


}
