package org.stream.split.voicenotification.Adapters;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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
public class InstalledAppAdapter extends RecyclerView.Adapter<InstalledAppAdapter.ViewHolder> implements View.OnCreateContextMenuListener
{
    static final public String TAG = "InstalledAppAdapter";
    private Context mContext;
    private List<AppInfoEntity> mDataset;

    public InstalledAppAdapter(Context context, ArrayList<AppInfoEntity> objects) {
        mDataset = objects;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_installed_app_list,parent,false);
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


    private class ViewHolder extends RecyclerView.ViewHolder
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
            cbx.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    entity.setSelected(((CheckBox)v).isChecked());
                }
            });
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
        }

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

    }
}