package org.stream.split.voicenotification.Adapters;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.stream.split.voicenotification.Controls.ImageCheckBox;
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
        mAddAppBtn.setVisible(Helper.isAnyItemModified(mDataset));
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener
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
            this.entity = entity;
            name.setText(entity.getApplicationLabel());
            try{
                Drawable icon = mContext.getPackageManager().getApplicationIcon(entity.getPackageName());
                cbx.initialize(icon);
                cbx.setChecked(entity.isModified());
                cbx.setOnCheckedChangeListener(this);
            }
            catch(PackageManager.NameNotFoundException ex)
            {
                mDataset.remove(entity);
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            entity.setIsModified(isChecked);
            mAddAppBtn.setVisible(Helper.isAnyItemModified(mDataset));
        }
    }
}