package org.stream.split.voicenotification.Adapters;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import org.stream.split.voicenotification.DataAccessLayer.DBHelper;
import org.stream.split.voicenotification.Enities.HistoryNotificationEntity;
import org.stream.split.voicenotification.Fragments.ApplicationDetailsFragment;
import org.stream.split.voicenotification.Helpers.Helper;
import org.stream.split.voicenotification.R;

import java.util.List;

/**
 * Created by split on 2015-10-20.
 */
public class HistoryNotificationsAdapter extends RecyclerView.Adapter<HistoryNotificationsAdapter.ViewHolder> {

    static final public String TAG = "HistoryNotificationsAdapter";
    private Context mContext;
    private List<HistoryNotificationEntity> mDataset;
    private boolean mAnimationFlag = false;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, CheckBox.OnCheckedChangeListener {

        public LinearLayout mTextLayout;
        public TextView mLabelTextView;
        public TextView mTimestampTextView;
        public TextView mNotificationID;
        public CheckBox mCbx;
        public HistoryNotificationEntity mHistoryNotificationEntity;


        public ViewHolder(View v) {
            super(v);
            mTextLayout = (LinearLayout) v.findViewById(R.id.history_text_layout);
            mNotificationID = (TextView) v.findViewById(R.id.history_notification_sbn_id);
            mLabelTextView = (TextView) v.findViewById(R.id.history_app_name);
            mTimestampTextView = (TextView) v.findViewById(R.id.history_app_timestamp);
            mCbx = (CheckBox) v.findViewById(R.id.history_app_add);
        }
        public void Initialize(HistoryNotificationEntity entity)
        {
            mHistoryNotificationEntity = entity;
            mNotificationID.setText(String.valueOf(entity.getSbnId()));
            mLabelTextView.setText(entity.getApplicationLabel());
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
                mHistoryNotificationEntity.setBundleKeys(db.getHistoryBundleKeys(mHistoryNotificationEntity.getID()));
                db.close();
                Fragment fragment = ApplicationDetailsFragment.newInstance(new Gson().toJson(mHistoryNotificationEntity));
                FragmentManager fragmentManager = ((Activity)mContext).getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_content, fragment)
                        .addToBackStack("notification details"+ mHistoryNotificationEntity.getID())
                .commit();
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            DBHelper db = new DBHelper(buttonView.getContext());

            if(isChecked != mHistoryNotificationEntity.isFollowed()) {
                mHistoryNotificationEntity.setIsFollowed(isChecked);
                if (isChecked)
                    db.addFollowedApp(mHistoryNotificationEntity);
                else
                    db.deleteFollowedApp(mHistoryNotificationEntity, true);

                refresh();
            }
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public HistoryNotificationsAdapter(List<HistoryNotificationEntity> notificationHistory, Context context) {
        mDataset = notificationHistory;
        mContext = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public HistoryNotificationsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_history_list_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        HistoryNotificationEntity entity = mDataset.get(position);
        holder.Initialize(entity);
        setAnimation(holder.itemView, position);
    }

    /**
     * Here is the key method to apply the animation
     */
    private void setAnimation(View viewToAnimate, int position)
    {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position == 0 && mAnimationFlag)
        {
            mAnimationFlag = false;
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.anim_insertion);
            viewToAnimate.startAnimation(animation);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void addItem(HistoryNotificationEntity entity)
    {
        if(mDataset.size() > 50)
            mDataset.remove(mDataset.size()-1);
        mDataset.add(0,entity);
        mAnimationFlag = true;
    }

    public void refresh()
    {
        mDataset.clear();
        DBHelper db = new DBHelper(mContext);
        mDataset.addAll(db.getAllHistoryNotification(false));
        db.close();
        this.notifyDataSetChanged();
    }


}
