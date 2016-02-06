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
import org.stream.split.voicenotification.Enities.NotificationEntity;
import org.stream.split.voicenotification.Fragments.NotificationDetailsFragment;
import org.stream.split.voicenotification.Helpers.Helper;
import org.stream.split.voicenotification.R;

import java.util.List;

/**
 * Created by split on 2015-10-20.
 */
public class NotificationsAdapter<T extends NotificationEntity> extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {

    static final public String TAG = "NotificationsAdapter";
    private Context mContext;
    private List<T> mDataset;
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
        public T notificationEntity;


        public ViewHolder(View v) {
            super(v);
            mTextLayout = (LinearLayout) v.findViewById(R.id.history_text_layout);
            mNotificationID = (TextView) v.findViewById(R.id.history_notification_sbn_id);
            mLabelTextView = (TextView) v.findViewById(R.id.history_app_name);
            mTimestampTextView = (TextView) v.findViewById(R.id.history_app_timestamp);
            mCbx = (CheckBox) v.findViewById(R.id.history_app_add);
        }
        public void Initialize(T entity)
        {
            notificationEntity = entity;
            mNotificationID.setText(String.valueOf(entity.getSbnId()));
            mLabelTextView.setText(Helper.getApplicationLabel(entity.getPackageName(), mContext));

            if(entity instanceof HistoryNotificationEntity)
                mTimestampTextView.setText(Helper.convertTime(((HistoryNotificationEntity)entity).getOccurrenceTime()));
            else
                mTimestampTextView.setVisibility(View.GONE);

            mCbx.setChecked(entity.isFollowed());

            mTextLayout.setOnClickListener(this);
            mCbx.setOnCheckedChangeListener(this);
        }

        @Override
        public void onClick(View v) {
            if(mContext instanceof Activity)
            {
                DBHelper db = new DBHelper(mContext);
                notificationEntity.setBundleKeys(db.getBundleKeys(notificationEntity));
                db.close();
                Fragment fragment = NotificationDetailsFragment.newInstance(new Gson().toJson(notificationEntity));
                FragmentManager fragmentManager = ((Activity)mContext).getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_content, fragment)
                        .addToBackStack("notification details")
                .commit();
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            DBHelper db = new DBHelper(buttonView.getContext());

            if(isChecked != notificationEntity.isFollowed()) {
                notificationEntity.setIsFollowed(isChecked);
                if (isChecked)
                    db.addFollowedNotification(notificationEntity);
                else
                    db.deleteFollowedNotificaiton(notificationEntity);

                refresh();
            }
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public NotificationsAdapter(List<T> notificationHistory, Context context) {
        mDataset = notificationHistory;
        mContext = context;

    }

    // Create new views (invoked by the layout manager)
    @Override
    public NotificationsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_history_list_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        T entity = mDataset.get(position);
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

    public void addItem(T entity)
    {
        if(mDataset.size() > 50)
            mDataset.remove(mDataset.size()-1);
        mDataset.add(0,entity);
        mAnimationFlag = true;
    }

    public void refresh()
    {
        //todo most likely we will need to load data from database but we do not now what kind of object we need to load notificationEntites/historynotificationEntities
        //we can check what kind of object there were in list but what if there is null?
        mDataset.clear();
        DBHelper db = new DBHelper(mContext);
        db.close();
        this.notifyDataSetChanged();
    }


}
