package org.stream.split.voicenotification;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.stream.split.voicenotification.BussinessLayer.AppInfoEntity;
import org.stream.split.voicenotification.DataAccessLayer.DBHelper;

import java.util.ArrayList;

/**
 * Created by split on 2015-10-20.
 */
public class NotificationsHistoryAdapter extends RecyclerView.Adapter<NotificationsHistoryAdapter.ViewHolder> {

    static final public String TAG = "NotificationsHistoryAdapter";
    public DBHelper mDb;
    private ArrayList<AppInfoEntity> mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mTextView;
        public ImageButton mAddAppImgBtn;
        public AppInfoEntity mAppinfo;


        public ViewHolder(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.history_app_name);
            mAddAppImgBtn = (ImageButton) v.findViewById(R.id.history_app_add);



        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public NotificationsHistoryAdapter(ArrayList<AppInfoEntity> historyApps) {
            mDataset = historyApps;

    }

    // Create new views (invoked by the layout manager)
    @Override
    public NotificationsHistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_history_item, parent, false);
        mDb = new DBHelper(parent.getContext());
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        String packageName = mDataset.get(position).getPackageName();
        holder.mAppinfo = mDataset.get(position);
        holder.mTextView.setText(packageName);
        if(mDb.isAppFollowed(packageName))
        {
            holder.mAddAppImgBtn.setImageResource(R.drawable.ic_apply_applications);
        }
        else {
            holder.mAddAppImgBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDb.addApp(holder.mAppinfo);
                    Log.d(TAG, "mAppinfo == null");
                }
            });
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void addItem(AppInfoEntity app)
    {
        mDataset.add(app);
    }


}
