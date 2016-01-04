package org.stream.split.voicenotification.Adapters;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.stream.split.voicenotification.Enities.AppInfoEntity;
import org.stream.split.voicenotification.Helpers.Helper;
import org.stream.split.voicenotification.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by B on 2015-12-13.
 */
public class InstalledAppAdapter extends RecyclerView.Adapter<InstalledAppAdapter.ViewHolder>
{
    final public String TAG = this.getClass().getSimpleName();
    private Context mContext;
    private List<AppInfoEntity> mDataset;
    private MenuItem mAddAppBtn;

    public InstalledAppAdapter(Context context, List<AppInfoEntity> objects) {
        mDataset = objects;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_installed_app_item,parent,false);
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

    public void onCreateMenu(Menu menu) {

        mAddAppBtn = menu.findItem(R.id.add_app_menu_item);
        mAddAppBtn.setVisible(Helper.isAnyItemSelected(mDataset));
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener
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
            name.setText(entity.getApplicationLabel());
            try {
                icon.setImageDrawable(mContext.getPackageManager().getApplicationIcon(entity.getPackageName()));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            cbx.setChecked(entity.isModified());
            cbx.setOnCheckedChangeListener(this);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            entity.setIsModified(isChecked);
            mAddAppBtn.setVisible(Helper.isAnyItemSelected(mDataset));
        }
    }
}