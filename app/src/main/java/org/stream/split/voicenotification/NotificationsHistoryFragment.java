package org.stream.split.voicenotification;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.Fragment;
import android.app.FragmentTransaction;

import org.stream.split.voicenotification.BussinessLayer.AppInfoEntity;
import org.stream.split.voicenotification.DataAccessLayer.DBHelper;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class NotificationsHistoryFragment extends Fragment {

    private final static String TAG = "NotificationsHistoryFragment";

    private RecyclerView mRecyclerView;
    private NotificationsHistoryAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private NotifyBroadcastReceiver mReceiver;
    private OnFragmentInteractionListener mListener;
    private NotificationServiceConnection mConnection;


    public NotificationsHistoryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private void registerNotificationReceiver(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_list, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        ArrayList<AppInfoEntity> apps = new ArrayList<>();
        apps.addAll(new DBHelper(this.getActivity()).getAllApps());
        mAdapter = new NotificationsHistoryAdapter(apps);
        mRecyclerView.setAdapter(mAdapter);
        return view;
    }


    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);

//        Intent intent = new Intent(this.getActivity(),NotificationCatcherService.class);
//        intent.setAction(NotificationCatcherService.CUSTOM_BINDING);
//        this.getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        IntentFilter filter = new IntentFilter(NotificationCatcherService.TAG);
        mReceiver = new NotifyBroadcastReceiver();
        mConnection = NotificationServiceConnection.getInstance();
        mConnection.registerReceiver(mReceiver,filter);

        try {
            mListener = (OnFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
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
            Log.d(TAG, "message received: " + intent.getAction());
            mAdapter.addItem(new AppInfoEntity(intent.getAction()));
            mAdapter.notifyDataSetChanged();
        }
    }


}
