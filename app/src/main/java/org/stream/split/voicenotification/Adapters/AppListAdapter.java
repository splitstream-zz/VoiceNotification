package org.stream.split.voicenotification.Adapters;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import org.stream.split.voicenotification.Enities.AppInfoEntity;
import org.stream.split.voicenotification.Helpers.Helper;
import org.stream.split.voicenotification.R;

import java.util.ArrayList;

/**
 * Created by B on 2015-12-13.
 */
private class CustomListAdapter extends ArrayAdapter<AppInfoEntity>
{

    public CustomListAdapter(Context context, int resource, ArrayList<AppInfoEntity> objects) {
        super(context, resource, objects);
        this.addAll(objects);

    }

    private class ViewHolder
    {
        ImageView icon;
        TextView name;
        CheckBox cbx;

    }
    @Override
    public View getView(int position, View convertView, final ViewGroup parent)
    {
        ViewHolder holder;

        if(convertView == null)
        {
            LayoutInflater vi =  (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.fragment_app_item,null);

            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.app_name);
            holder.icon = (ImageView) convertView.findViewById(R.id.app_icon);
            holder.cbx = (CheckBox) convertView.findViewById(R.id.app_cbx);

            convertView.setTag(holder);

        }
        else
            holder = (ViewHolder) convertView.getTag();


        AppInfoEntity appInfoEntity = this.getItem(position);
        holder.name.setText(Helper.getApplicationLabel(appInfoEntity.getPackageName(), getContext()));
        holder.cbx.setChecked(appInfoEntity.isSelected());
        try {
            holder.icon.setImageDrawable(manager.getApplicationIcon(appInfoEntity.getPackageName()));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        holder.name.setTag(appInfoEntity);
        holder.cbx.setTag(appInfoEntity);


        holder.cbx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppInfoEntity appInfoEntity = (AppInfoEntity) v.getTag();
                appInfoEntity.setSelected(((CheckBox)v).isChecked());
                Boolean setDeleteVisibility = false;
                if(mApplictaionsToShow == APPLICATIONS_TO_SHOW.SHOW_FOLLOWED ) {
                    for(int i = 0;i< mAdapter.getCount();i++) {
                        AppInfoEntity app = mAdapter.getItem(i);
                        if (app.isSelected()) {
                            setDeleteVisibility = true;
                            break;
                        }
                    }
                    mDeleteMenuItem.setVisible(setDeleteVisibility);
                }
            }
        });

        holder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppInfoEntity appInfoEntity = (AppInfoEntity) v.getTag();
                Snackbar.make(parent, "Clicked on Checkbox: " + appInfoEntity.getPackageName() + " is " + appInfoEntity.isSelected(),
                        Snackbar.LENGTH_SHORT).show();
            }
        });

        return convertView;

    }
}