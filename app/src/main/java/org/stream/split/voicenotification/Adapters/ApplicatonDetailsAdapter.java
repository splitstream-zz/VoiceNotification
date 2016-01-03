package org.stream.split.voicenotification.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.stream.split.voicenotification.DataAccessLayer.DBHelper;
import org.stream.split.voicenotification.Enities.AppInfoEntity;
import org.stream.split.voicenotification.Enities.BundleKeyEntity;
import org.stream.split.voicenotification.Interfaces.ItemTouchHelperAdapter;
import org.stream.split.voicenotification.Interfaces.ItemTouchHelperViewHolder;
import org.stream.split.voicenotification.Interfaces.OnStartDragListener;
import org.stream.split.voicenotification.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by split on 2015-10-20.
 */
public class ApplicatonDetailsAdapter extends RecyclerView.Adapter<ApplicatonDetailsAdapter.ViewHolder> implements ItemTouchHelperAdapter {

    final public String TAG = this.getClass().getSimpleName();
    private Context mContext;
    private List<BundleKeyEntity> mDataset;
    private final OnStartDragListener mDragStartListener;

    //TODO make priority work properly. List should be refreshed and priority fixed after item checked.
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener ,View.OnClickListener, ItemTouchHelperViewHolder {

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
            StringBuilder builder = new StringBuilder(bundleKeyEntity.getKey() +" ["+ mBundleKeyEntity.getPriority() + "]");
            mKeyTextView.setText(builder.toString());
            mValueTextView.setText(bundleKeyEntity.getValue());

            mCheckBox.setChecked(bundleKeyEntity.isFollowed());
            mCheckBox.setOnClickListener(this);

            if(bundleKeyEntity.isFollowed()) {
                mKeyTextView.setOnLongClickListener(this);
                mValueTextView.setOnLongClickListener(this);
            }

        }

        @Override
        public void onClick(View v) {
            mBundleKeyEntity.setIsModified(true);
            mBundleKeyEntity.setIsFollowed(((CheckBox) v).isChecked());
            if(((CheckBox) v).isChecked())
            {
                mBundleKeyEntity.setPriority(getAdapterPosition());
            }
            Collections.sort(mDataset, Collections.reverseOrder());
            notifyDataSetChanged();
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

        @Override
        public boolean onLongClick(View v) {
            mDragStartListener.onStartDrag(this);
            mBundleKeyEntity.setIsModified(true);
            return false;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ApplicatonDetailsAdapter(List<BundleKeyEntity> bundleKeys, OnStartDragListener onStartDragListener, Context context) {
        Collections.sort(bundleKeys, Collections.reverseOrder());
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
    public ApplicatonDetailsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

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
    public void refresh(List<BundleKeyEntity> entities)
    {
        mDataset.clear();
        mDataset.addAll(entities);

    }

}
