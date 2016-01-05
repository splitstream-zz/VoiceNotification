package org.stream.split.voicenotification.Adapters;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import org.stream.split.voicenotification.DataAccessLayer.DBHelper;
import org.stream.split.voicenotification.Enities.NotificationEntity;
import org.stream.split.voicenotification.Fragments.ApplicationDetailsFragment;
import org.stream.split.voicenotification.Helpers.Helper;
import org.stream.split.voicenotification.R;

import java.text.DateFormat;
import java.util.List;

/**
 * Created by split on 2015-10-20.
 */
public class NotificationsHistoryAdapter extends RecyclerView.Adapter<NotificationsHistoryAdapter.ViewHolder> {

    static final public String TAG = "NotificationsHistoryAdapter";
    private Context mContext;
    private List<NotificationEntity> mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, CheckBox.OnCheckedChangeListener {

        public LinearLayout mTextLayout;
        public TextView mLabelTextView;
        public TextView mTimestampTextView;
        public CheckBox mCbx;
        public NotificationEntity mNotificationEntity;


        public ViewHolder(View v) {
            super(v);
            mTextLayout = (LinearLayout) v.findViewById(R.id.history_text_layout);
            mLabelTextView = (TextView) v.findViewById(R.id.history_app_name);
            mTimestampTextView = (TextView) v.findViewById(R.id.history_app_timestamp);
            mCbx = (CheckBox) v.findViewById(R.id.history_app_add);
        }
        public void Initialize(NotificationEntity entity)
        {
            mNotificationEntity = entity;
            StringBuilder builder = new StringBuilder(entity.getApplicationLabel());
            builder.append("[" + getAdapterPosition() + "]");
            mLabelTextView.setText(builder.toString());
            mTimestampTextView.setText(Helper.convertTime(entity.getOccurrenceTime()));
            mCbx.setChecked(entity.isFollowed());

            mTextLayout.setOnClickListener(this);
            mCbx.setOnCheckedChangeListener(this);
        }

        @Override
        public void onClick(View v) {
            if(mContext instanceof Activity)
            {
                DBHelper db = new DBHelper(mContext);
                mNotificationEntity.setBundleKeys(db.getMessages(mNotificationEntity.getID()));
                db.close();
                Fragment fragment = ApplicationDetailsFragment.newInstance(new Gson().toJson(mNotificationEntity));
                FragmentManager fragmentManager = ((Activity)mContext).getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_content, fragment)
                        .addToBackStack("notification details"+ mNotificationEntity.getID())
                .commit();
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            DBHelper db = new DBHelper(buttonView.getContext());

            if(isChecked !=mNotificationEntity.isFollowed()) {
                mNotificationEntity.setIsFollowed(isChecked);
                if (isChecked)
                    db.addApp(mNotificationEntity);
                else
                    db.deleteApp(mNotificationEntity,true);

                refresh();
            }
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public NotificationsHistoryAdapter(List<NotificationEntity> notificationHistory, Context context) {
        mDataset = notificationHistory;
        mContext = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public NotificationsHistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_history_list_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        NotificationEntity entity = mDataset.get(position);
        holder.Initialize(entity);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void addItem(NotificationEntity entity)
    {
        if(mDataset.size() > 50)
            mDataset.remove(mDataset.size()-1);
        mDataset.add(0,entity);
    }

    public void refresh()
    {
        mDataset.clear();
        DBHelper db = new DBHelper(mContext);
        mDataset.addAll(db.getAllNotification(false));
        db.close();
        this.notifyDataSetChanged();
    }


}
