package org.stream.split.voicenotification.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.stream.split.voicenotification.Controls.ImageCheckBox;
import org.stream.split.voicenotification.Enities.BundleKeyEntity;
import org.stream.split.voicenotification.Interfaces.ItemTouchHelperAdapter;
import org.stream.split.voicenotification.Interfaces.ItemTouchHelperViewHolder;
import org.stream.split.voicenotification.Interfaces.OnStartDragListener;
import org.stream.split.voicenotification.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//TODO disable dismiss feature

/**
 * Created by split on 2015-10-20.
 */
public class BundleKeysAdapter extends RecyclerView.Adapter<BundleKeysAdapter.ViewHolder> implements ItemTouchHelperAdapter {

    final public String TAG = this.getClass().getSimpleName();
    private Context mContext;
    private List<BundleKeyEntity> mDataset;
    private final OnStartDragListener mDragStartListener;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener, ItemTouchHelperViewHolder {

        public LinearLayout mTextLayout;
        public TextView mKeyTextView;
        public TextView mValueTextView;
        public CheckBox mCheckBox;
        public ImageCheckBox mShowAlwaysCheckBox;
        public BundleKeyEntity mBundleKeyEntity;


        public ViewHolder(View v) {
            super(v);
            mTextLayout = (LinearLayout) v.findViewById(R.id.bundlekey_text);
            mKeyTextView = (TextView) v.findViewById(R.id.bundlekey_name);
            mValueTextView = (TextView) v.findViewById(R.id.bundlekey_value);
            mCheckBox = (CheckBox) v.findViewById(R.id.bundlekey_cbx);
            mShowAlwaysCheckBox = (ImageCheckBox) v.findViewById(R.id.bundlekey_show_always_cbx);
        }

        public void Initialize(BundleKeyEntity bundleKeyEntity) {
            mBundleKeyEntity = bundleKeyEntity;
            StringBuilder builder = new StringBuilder(bundleKeyEntity.getKey() + " [" + mBundleKeyEntity.getPriority() + "]");

            mKeyTextView.setText(builder.toString());
            if (bundleKeyEntity.getValue() == null || bundleKeyEntity.getValue().isEmpty())
                mValueTextView.setVisibility(View.GONE);
            else {
                mValueTextView.setText(bundleKeyEntity.getValue());
                mValueTextView.setVisibility(View.VISIBLE);
            }

            mCheckBox.setChecked(bundleKeyEntity.isFollowed());
            mCheckBox.setOnClickListener(this);

            Drawable showAlwaysDrawable = mContext.getResources().getDrawable(R.drawable.ic_show_always);
            initializeShowAlwaysCbx(mBundleKeyEntity.isFollowed(), showAlwaysDrawable);

            mTextLayout.setOnLongClickListener(this);
        }

        private void initializeShowAlwaysCbx(boolean isFollowed, Drawable showAlwaysDrawable) {
            if (isFollowed) {
                RoundRectShape backgroundShape = new RoundRectShape(new float[] { 45, 45, 45, 45, 45, 45, 45, 45 }, null,null);
                mShowAlwaysCheckBox.initialize(showAlwaysDrawable, 40, 40, mContext.getResources().getColor(R.color.colorAccent),backgroundShape);
                mShowAlwaysCheckBox.setVisibility(View.VISIBLE);
                boolean isShownAlways = mBundleKeyEntity.isShowAlways();
                mShowAlwaysCheckBox.setChecked(isShownAlways);
                mShowAlwaysCheckBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mBundleKeyEntity.setIsModified(true);
                        mBundleKeyEntity.setIsShowAlways(((CheckBox) v).isChecked());
                    }
                });
            } else
                mShowAlwaysCheckBox.setVisibility(View.GONE);

        }

        @Override
        public void onClick(View v) {
            mBundleKeyEntity.setIsModified(true);
            mBundleKeyEntity.setIsFollowed(((CheckBox) v).isChecked());
            if (((CheckBox) v).isChecked()) {
                int maxPriority = getMaxPriority();
                mBundleKeyEntity.setPriority(++maxPriority);
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
            itemView.setBackgroundColor(0);
        }

        @Override
        public boolean onLongClick(View v) {
            mDragStartListener.onStartDrag(this);
            return false;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public BundleKeysAdapter(List<BundleKeyEntity> bundleKeys, OnStartDragListener onStartDragListener, Context context) {
        Collections.sort(bundleKeys, Collections.reverseOrder());
        mDataset = bundleKeys;
        mContext = context;
        mDragStartListener = onStartDragListener;
    }

    public List<BundleKeyEntity> getItems() {
        return mDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public BundleKeysAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

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

    public List<BundleKeyEntity> getModifiedItems() {
        List<BundleKeyEntity> bundleKeys = new ArrayList<>();
        for (BundleKeyEntity entity : mDataset) {
            if (entity.isModified()) {
                bundleKeys.add(entity);
            }
        }
        return bundleKeys;
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        BundleKeyEntity fromPositionEntity = mDataset.get(fromPosition);
        BundleKeyEntity toPositionEntity = mDataset.get(toPosition);

        //log(fromPosition,toPosition);
        if (fromPositionEntity.isFollowed() && toPositionEntity.isFollowed()) {
            int tempPriority = toPositionEntity.getPriority();
            toPositionEntity.setPriority(fromPositionEntity.getPriority());
            fromPositionEntity.setPriority(tempPriority);

            Collections.swap(mDataset, fromPosition, toPosition);
            notifyItemMoved(fromPosition, toPosition);

            fromPositionEntity.setIsModified(true);
            toPositionEntity.setIsModified(true);
        } else
            return true;

        //log(fromPosition,toPosition);
        return false;
    }

    @Override
    public void onItemDismiss(int position) {
        //TODO implemetn function
    }

    public void refresh(List<BundleKeyEntity> entities) {
        mDataset.clear();
        mDataset.addAll(entities);

    }

    private void log(int fromPosition, int toPosition) {
        BundleKeyEntity fromPositionEntity = mDataset.get(fromPosition);
        BundleKeyEntity toPositionEntity = mDataset.get(toPosition);
        StringBuilder builder = new StringBuilder();
        builder.append("================");
        builder.append("\n");
        builder.append("From key name: ");
        builder.append(fromPositionEntity.getKey());
        builder.append("\tpriority: ");
        builder.append(fromPositionEntity.getPriority());
        builder.append("\n");
        builder.append("to key name: ");
        builder.append(toPositionEntity.getKey());
        builder.append("\tpriority: ");
        builder.append(toPositionEntity.getPriority());
        builder.append("\n");
        builder.append("moving form position: ");
        builder.append(fromPosition);
        builder.append("\tmoving to position: ");
        builder.append(toPosition);
        builder.append("\n");
        Log.d(TAG, builder.toString());
    }

    private int getMaxPriority() {
        int maxPriority = 0;
        for (BundleKeyEntity e : mDataset) {
            if (e.getPriority() > maxPriority)
                maxPriority = e.getPriority();
        }
        return maxPriority;
    }

}
