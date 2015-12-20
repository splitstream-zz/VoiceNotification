package org.stream.split.voicenotification.Adapters;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.Gson;

import org.stream.split.voicenotification.DataAccessLayer.DBHelper;
import org.stream.split.voicenotification.Enities.BundleKeyEntity;
import org.stream.split.voicenotification.Enities.NotificationEntity;
import org.stream.split.voicenotification.Fragments.AppDetailsFragment;
import org.stream.split.voicenotification.R;

import java.util.List;

/**
 * Created by split on 2015-10-20.
 */
public class AppDetailsAdapter extends RecyclerView.Adapter<AppDetailsAdapter.ViewHolder> {

    static final public String TAG = "AppDetailsAdapter";
    private Context mContext;
    private List<BundleKeyEntity> mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView mKeyTextView;
        public TextView mValueTextView;
        public CheckBox mCheckBox;
        public BundleKeyEntity mBundleKeyEntity;


        public ViewHolder(View v) {
            super(v);
            mKeyTextView = (TextView) v.findViewById(R.id.bundlekey_name);
            mValueTextView = (TextView) v.findViewById(R.id.bundlekey_value);
            mCheckBox = (CheckBox) v.findViewById(R.id.bundlekey_cbx);
            mCheckBox.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
           mBundleKeyEntity.setIsFollowed(((CheckBox)v).isChecked());
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public AppDetailsAdapter(List<BundleKeyEntity> bundleKeys, Context context) {
        mDataset = bundleKeys;
        mContext = context;

    }

    // Create new views (invoked by the layout manager)
    @Override
    public AppDetailsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_history_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        BundleKeyEntity entity = mDataset.get(position);
        holder.mBundleKeyEntity = entity;

        holder.mKeyTextView.setText(entity.getKey());

        holder.mImgBtn.setTag(entity);
        if(entity.isFollowed())
            holder.mImgBtn.setImageResource(R.drawable.ic_delete_app);
        else
            holder.mImgBtn.setImageResource(R.drawable.ic_add_applications);


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void addItem(NotificationEntity entity)
    {
        mDataset.add(entity);
    }

    public void refresh()
    {
        mDataset.clear();
        DBHelper db = new DBHelper(mContext);
        mDataset.addAll(db.getAllNotification());
        db.close();
        this.notifyDataSetChanged();
    }


}
