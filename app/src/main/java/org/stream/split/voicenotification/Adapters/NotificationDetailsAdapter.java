package org.stream.split.voicenotification.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.stream.split.voicenotification.DataAccessLayer.DBHelper;
import org.stream.split.voicenotification.Enities.BundleKeyEntity;
import org.stream.split.voicenotification.Enities.NotificationEntity;
import org.stream.split.voicenotification.Interfaces.ItemTouchHelperAdapter;
import org.stream.split.voicenotification.Interfaces.ItemTouchHelperViewHolder;
import org.stream.split.voicenotification.Interfaces.OnStartDragListener;
import org.stream.split.voicenotification.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by split on 2015-10-20.
 */
public class NotificationDetailsAdapter extends RecyclerView.Adapter<NotificationDetailsAdapter.ViewHolder> implements ItemTouchHelperAdapter {

    final public String TAG = this.getClass().getSimpleName();
    private Context mContext;
    private List<BundleKeyEntity> mDataset;
    private final OnStartDragListener mDragStartListener;

    //TODO make priority work properly. List should be refreshed and priority fixed after item checked.
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, ItemTouchHelperViewHolder {

        public LinearLayout mTextLayout;
        public TextView mKeyTextView;
        public TextView mValueTextView;
        public CheckBox mCheckBox;
        public BundleKeyEntity mBundleKeyEntity;


        public ViewHolder(View v) {
            super(v);
            mTextLayout = (LinearLayout) v.findViewById(R.id.bundlekey_text);
            mKeyTextView = (TextView) v.findViewById(R.id.bundlekey_name);
            mValueTextView = (TextView) v.findViewById(R.id.bundlekey_value);
            mCheckBox = (CheckBox) v.findViewById(R.id.bundlekey_cbx);
        }

        public void Initialize(BundleKeyEntity bundleKeyEntity)
        {
            mBundleKeyEntity = bundleKeyEntity;
            mKeyTextView.setText(bundleKeyEntity.getKey() +" ["+ mBundleKeyEntity.getPriority() + "]");
            mValueTextView.setText(bundleKeyEntity.getValue());
            DBHelper db = new DBHelper(mContext);
            mCheckBox.setChecked(bundleKeyEntity.isFollowed());
            db.close();
            mCheckBox.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            mBundleKeyEntity.setIsModified(true);
            mBundleKeyEntity.setIsFollowed(((CheckBox) v).isChecked());
            if(((CheckBox) v).isChecked())
            {
                mBundleKeyEntity.setPriority(getAdapterPosition());
            }
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            mBundleKeyEntity.setIsModified(true);
            mBundleKeyEntity.setPriority(getAdapterPosition());
            itemView.setBackgroundColor(0);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public NotificationDetailsAdapter(List<BundleKeyEntity> bundleKeys, OnStartDragListener onStartDragListener, Context context) {
        Collections.sort(bundleKeys, new Comparator<BundleKeyEntity>() {
            @Override
            public int compare(BundleKeyEntity lhs, BundleKeyEntity rhs) {
                int result = 0;
                if(lhs.isFollowed() && rhs.isFollowed())
                    if(lhs.getPriority()>rhs.getPriority())
                        result = 1;
                    else
                        result = -1;
                else if(lhs.isFollowed())
                    result = 1;
                else if(rhs.isFollowed())
                    result = -1;

                return result;
            }
        });
        mDataset = bundleKeys;
        mContext = context;
        mDragStartListener = onStartDragListener;
    }

    public List<BundleKeyEntity> getItems()
    {
        return mDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public NotificationDetailsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_notification_details_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        BundleKeyEntity entity = mDataset.get(position);
        holder.Initialize(entity);

        holder.mKeyTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                    mDragStartListener.onStartDrag(holder);
                    holder.mBundleKeyEntity.setIsModified(true);

                return false;
            }
        });

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
    public List<BundleKeyEntity> getModifiedItems()
    {
        List<BundleKeyEntity> bundleKeys = new ArrayList<>();
        for(BundleKeyEntity entity:mDataset)
        {
            if(entity.isModified())
            {
                bundleKeys.add(entity);
            }
        }
        return bundleKeys;
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mDataset,fromPosition,toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return false;
    }

    @Override
    public void onItemDismiss(int position) {
        //TODO implemetn function
    }

}
