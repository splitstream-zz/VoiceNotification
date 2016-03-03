package org.stream.split.voicenotification.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;

import org.stream.split.voicenotification.DataAccessLayer.DBHelper;
import org.stream.split.voicenotification.Enities.BaseEntity;
import org.stream.split.voicenotification.Enities.HistoryNotificationEntity;
import org.stream.split.voicenotification.Helpers.NotificationServiceConnection;
import org.stream.split.voicenotification.NotificationService;
import org.stream.split.voicenotification.Interfaces.OnFragmentInteractionListener;

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
public class HistoryNotificationListFragment extends NotificationListFragment {

    public final static String TAG = "HistoryNotifiListFragment";

    private NotifyBroadcastReceiver mReceiver;
    private NotificationServiceConnection mConnection;

    @Override
    public String getTAG() {
        return TAG;
    }

    @Override
    public void refresh() {
        mLoading = new LoadHistoryNotificationsAsync().execute();
    }

    public HistoryNotificationListFragment() {
    }

    public static HistoryNotificationListFragment newInstance()
    {
        HistoryNotificationListFragment fragment = new HistoryNotificationListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        mReceiver = new NotifyBroadcastReceiver();
        mConnection = NotificationServiceConnection.getInstance();

    }

    @Override
    public boolean isModified() {
        return false;
    }


    @Override
    public void onResume() {
        super.onResume();
        mConnection.registerReceiver(mReceiver);
    }

    @Override
    public void onPause() {
        super.onPause();
        mConnection.unregisterReceiver(mReceiver);
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
                gsonToJson = extras.getString(NotificationService.EXTRA_NEW_NOTIFICATION_OBJECT);
                HistoryNotificationEntity historyNotificationEntity = new Gson().fromJson(gsonToJson, HistoryNotificationEntity.class);
                getAdapter().addItem(historyNotificationEntity);
            }
        }
    }
    private class LoadHistoryNotificationsAsync extends AsyncTask<Void,Void,List<? extends BaseEntity>>
    {
        @Override
        protected List<HistoryNotificationEntity> doInBackground(Void... params) {
            DBHelper db = new DBHelper(getActivity());
            List entities = db.getAllHistoryNotification(false);
            db.close();
            return entities;
        }

        @Override
        protected void onPostExecute(List<? extends BaseEntity> historyNotificationEntities) {
            super.onPostExecute(historyNotificationEntities);
            getAdapter().setDataset(historyNotificationEntities);
            getAdapter().notifyDataSetChanged();
        }

    }
}
