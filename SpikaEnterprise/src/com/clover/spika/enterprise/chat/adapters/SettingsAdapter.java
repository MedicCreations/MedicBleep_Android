package com.clover.spika.enterprise.chat.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.models.SettingsItem;
import com.clover.spika.enterprise.chat.utils.Const;

public class SettingsAdapter extends BaseAdapter {

	Context ctx;
	int chatType = 0;

	List<SettingsItem> data = new ArrayList<SettingsItem>();

	public SettingsAdapter(Context context) {
		this.ctx = context;

		String[] items = context.getResources().getStringArray(R.array.chat_settings_array);

		for (String item : items) {
			this.data.add(new SettingsItem(item, false));
		}
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

		if (item.isDisabled()) {
			holder.rootView.setBackgroundColor(ctx.getResources().getColor(R.color.button_gray));
		} else {
			holder.rootView.setBackgroundColor(ctx.getResources().getColor(android.R.color.transparent));
		}

		return convertView;
	}

	public void disableItem(int type) {
		switch (type) {

		case Const.C_TEAM:
			data.get(1).setDisabled(true);
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
