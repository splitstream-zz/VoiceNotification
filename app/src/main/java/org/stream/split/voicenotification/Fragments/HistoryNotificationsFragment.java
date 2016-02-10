package org.stream.split.voicenotification.Fragments;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
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

import org.stream.split.voicenotification.DataAccessLayer.DBHelper;
import org.stream.split.voicenotification.Enities.HistoryNotificationEntity;
import org.stream.split.voicenotification.Helpers.Helper;
import org.stream.split.voicenotification.Helpers.NotificationServiceConnection;
import org.stream.split.voicenotification.NotificationService;
import org.stream.split.voicenotification.Adapters.NotificationsAdapter;
import org.stream.split.voicenotification.Interfaces.OnFragmentInteractionListener;
import org.stream.split.voicenotification.R;
import org.stream.split.voicenotification.VoiceNotificationActivity;

import java.lang.reflect.Type;
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
public class HistoryNotificationsFragment extends NotificationsFragment {

    private final static String TAG = "HistoryNotificationsFragment";
    private NotifyBroadcastReceiver mReceiver;
    private NotificationServiceConnection mConnection;

    public HistoryNotificationsFragment() {
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
        mReceiver = new NotifyBroadcastReceiver();
        mConnection = NotificationServiceConnection.getInstance();
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

//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        if (null != mListener) {
//            // Notify the active callbacks interface (the activity, if the
//            // fragment is attached to one) that an item has been selected.
//            mListener.onFragmentInteraction("sda");
//        }
//    }

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
                mAdapter.addItem(historyNotificationEntity);
                mAdapter.refresh();
            }
        }
    }
}
