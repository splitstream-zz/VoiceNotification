package org.stream.split.voicenotification.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.gson.Gson;

import org.stream.split.voicenotification.Controls.ImageCheckBox;
import org.stream.split.voicenotification.DataAccessLayer.DBHelper;
import org.stream.split.voicenotification.Enities.AppInfoEntity;
import org.stream.split.voicenotification.Enities.BundleKeyEntity;
import org.stream.split.voicenotification.Fragments.ApplicationDetailsFragment;
import org.stream.split.voicenotification.Helpers.Helper;
import org.stream.split.voicenotification.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by B on 2015-12-13.
 */
public class FollowedAppAdapter extends RecyclerView.Adapter<FollowedAppAdapter.ViewHolder>
{
    public String TAG = this.getClass().getSimpleName();
    private Context mContext;
    private List<AppInfoEntity> mDataset;
    private MenuItem mDeleteMenuItem;

    public FollowedAppAdapter(Context context, List<AppInfoEntity> objects) {

        mDataset = objects;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_followed_app_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.Initialize(mDataset.get(position));
    }
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public AppInfoEntity getItem(int position)
    {
        return mDataset.get(position);
    }
    public void clear()
    {
        mDataset.clear();
    }
    public void addAll(List<AppInfoEntity> apps)
    {
        mDataset.addAll(apps);
    }
    public List<AppInfoEntity> getSelectedItems()
    {
        List<AppInfoEntity> selectedApps = new ArrayList<>();
        for(AppInfoEntity entity:mDataset)
        {
            if(entity.isModified())
                selectedApps.add(entity);
        }
        return selectedApps;
    }

    public void onCreateMenu(Menu menu)
    {
        mDeleteMenuItem = menu.findItem(R.id.delete_app_menu_item);
        boolean visibility = false;
        if(Helper.isAnyItemSelected(mDataset))
            visibility = true;
        mDeleteMenuItem.setVisible(visibility);
    }
    public void refresh()
    {
        mDataset.clear();
        DBHelper db = new DBHelper(mContext);
        mDataset = db.getAllFollowedApps(true);
        db.close();
        notifyDataSetChanged();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener, View.OnClickListener
    {
        TextView name;
        ImageCheckBox cbx;
        AppInfoEntity entity;

        public ViewHolder(View v)
        {
            super(v);
            name = (TextView) v.findViewById(R.id.app_name);
            cbx = (ImageCheckBox) v.findViewById(R.id.app_Customcbx);
        }
        public void Initialize(AppInfoEntity entity)
        {

            Drawable icon;
            try{
                icon = mContext.getPackageManager().getApplicationIcon(entity.getPackageName());
                cbx.initialize(icon);
                cbx.setChecked(entity.isModified());
                cbx.setOnCheckedChangeListener(this);
            }
            catch(PackageManager.NameNotFoundException ex)
            {
                DBHelper db = new DBHelper(mContext);
                db.deleteApp(entity,true);
                db.close();
                StringBuilder builder = new StringBuilder(entity.getApplicationLabel());
                builder.append(" was removed");
                Snackbar.make(((Activity)mContext).findViewById(R.id.frame_content), builder.toString(),Snackbar.LENGTH_SHORT).show();
                this.itemView.setVisibility(View.GONE);
            }
            this.entity = entity;
            name.setText(entity.getApplicationLabel());
            name.setOnClickListener(this);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            entity.setIsModified(isChecked);
            mDeleteMenuItem.setVisible(Helper.isAnyItemSelected(mDataset));
        }

        @Override
        public void onClick(View v) {

            List<BundleKeyEntity> list = Helper.getAllNotificationBundleKeys(entity.getPackageName());
            DBHelper db = new DBHelper(mContext);
            for(BundleKeyEntity e1:list)
            {
                for(BundleKeyEntity e2:entity.getBundleKeys())
                    if(e1.getKey().equals(e2.getKey())) {
                        e1.setIsShowAlways(e2.isShowAlways());
                        e1.setIsFollowed(e2.isFollowed());
                    }
            }
            db.close();
            entity.setBundleKeys(list);

            ApplicationDetailsFragment fragment = ApplicationDetailsFragment.newInstance(new Gson().toJson(entity));
            ((Activity)mContext).getFragmentManager().beginTransaction()
                    .replace(R.id.frame_content,fragment)
                    .addToBackStack("app details")
                    .commit();
        }
    }
}