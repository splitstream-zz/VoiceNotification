package org.stream.split.voicenotification.Adapters;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.stream.split.voicenotification.DataAccessLayer.DBHelper;
import org.stream.split.voicenotification.Enities.AppInfoEntity;
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
            if(entity.isSelected())
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
        mDataset = db.getAllApps(true);
        db.close();
        notifyDataSetChanged();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener
    {
        ImageView icon;
        TextView name;
        CheckBox cbx;
        AppInfoEntity entity;

        public ViewHolder(View v)
        {
            super(v);
            name = (TextView) v.findViewById(R.id.app_name);
            icon = (ImageView) v.findViewById(R.id.app_icon);
            cbx = (CheckBox) v.findViewById(R.id.app_cbx);
        }
        public void Initialize(AppInfoEntity entity)
        {
            this.entity = entity;
            name.setText(Helper.getApplicationLabel(entity.getPackageName(), mContext));
            try {
                icon.setImageDrawable(mContext.getPackageManager().getApplicationIcon(entity.getPackageName()));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            cbx.setChecked(entity.isSelected());
            cbx.setOnCheckedChangeListener(this);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            entity.setSelected(isChecked);
            boolean isAnyItemSelected = Helper.isAnyItemSelected(mDataset);
            mDeleteMenuItem.setVisible(isAnyItemSelected);
        }
    }
}