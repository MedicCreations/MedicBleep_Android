package com.clover.spika.enterprise.chat.adapters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.R;

public class SettingsAdapter extends BaseAdapter {

	Context ctx;

	List<String> data = new ArrayList<String>();

	public SettingsAdapter(Context context) {
		this.ctx = context;
		this.data.addAll(Arrays.asList(context.getResources().getStringArray(
				R.array.chat_settings_array)));
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
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

			convertView = LayoutInflater.from(ctx).inflate(
					R.layout.item_chat_settings, parent, false);

			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.item.setText((String) getItem(position));

		return convertView;
	}

	private class ViewHolder {
		public TextView item;

		public ViewHolder(View view) {
			item = (TextView) view.findViewById(R.id.settings_item);
		}
	}

}
