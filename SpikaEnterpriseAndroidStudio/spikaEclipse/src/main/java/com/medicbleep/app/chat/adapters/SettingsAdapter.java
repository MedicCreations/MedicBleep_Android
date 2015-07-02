package com.medicbleep.app.chat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.medicbleep.app.chat.R;
import com.medicbleep.app.chat.models.SettingsItem;
import com.medicbleep.app.chat.utils.Const;

import java.util.ArrayList;
import java.util.List;

public class SettingsAdapter extends BaseAdapter {

	Context ctx;
	int chatType = 0;

	List<SettingsItem> data = new ArrayList<SettingsItem>();

	public SettingsAdapter(Context context) {
		this.ctx = context;

	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public SettingsItem getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		final ViewHolder holder;
		if (convertView == null) {

			convertView = LayoutInflater.from(ctx).inflate(R.layout.item_chat_settings, parent, false);

			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		SettingsItem item = getItem(position);

		holder.item.setText(item.getName());

		return convertView;
	}

	public void setSettings(int type) {
		
		String[] items;
		switch (type) {

		case Const.C_PRIVATE:
			this.data.clear();
			items = ctx.getResources().getStringArray(R.array.private_chat_settings_array);

			for (String item : items) {
				this.data.add(new SettingsItem(item));
			}
			notifyDataSetChanged();
			break;

		case Const.C_ROOM:
			this.data.clear();
			items = ctx.getResources().getStringArray(R.array.room_chat_settings_array);

			for (String item : items) {
				this.data.add(new SettingsItem(item));
			}
			notifyDataSetChanged();
			break;
		case Const.C_ROOM_ADMIN_ACTIVE:
			this.data.clear();
			items = ctx.getResources().getStringArray(R.array.room_chat_admin_active_settings_array);

			for (String item : items) {
				this.data.add(new SettingsItem(item));
			}
			notifyDataSetChanged();
			break;
		case Const.C_ROOM_ADMIN_INACTIVE:
			this.data.clear();
			items = ctx.getResources().getStringArray(R.array.room_chat_admin_inactive_settings_array);

			for (String item : items) {
				this.data.add(new SettingsItem(item));
			}
			notifyDataSetChanged();
			break;
		case Const.C_GROUP:
			this.data.clear();
			items = ctx.getResources().getStringArray(R.array.group_chat_settings_array);

			for (String item : items) {
				this.data.add(new SettingsItem(item));
			}
			notifyDataSetChanged();
			break;

		default:
			break;
		}
	}

	private class ViewHolder {
		public RelativeLayout rootView;
		public TextView item;

		public ViewHolder(View view) {
			item = (TextView) view.findViewById(R.id.settings_item);
			rootView = (RelativeLayout) view.findViewById(R.id.rootView);
		}
	}

}
