package com.clover.spika.enterprise.chat.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.models.Category;
import com.clover.spika.enterprise.chat.views.RobotoRegularTextView;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends BaseAdapter {

	public Context cntx;
	public List<Category> data;
	private int activeCatId;

	public CategoryAdapter(Context context) {
		this.cntx = context;
		data = new ArrayList<Category>();
	}

	public void setData(List<Category> list) {
		data = list;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		final ViewHolderGroup holder;
		if (convertView == null) {

			convertView = LayoutInflater.from(cntx).inflate(R.layout.item_category, parent, false);

			holder = new ViewHolderGroup(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolderGroup) convertView.getTag();
		}

		// Assign values
		final Category category = (Category) getItem(position);

		if (activeCatId < 1) {
			if (Integer.valueOf(category.id) == 0)
				holder.catName.setTextColor(cntx.getResources().getColor(R.color.default_blue));
			else
				holder.catName.setTextColor(Color.BLACK);
		} else {
			if (Integer.valueOf(category.id) == activeCatId)
				holder.catName.setTextColor(cntx.getResources().getColor(R.color.default_blue));
			else
				holder.catName.setTextColor(Color.BLACK);
		}

		holder.catName.setText(category.name);

		return convertView;
	}

	@Override
	public Category getItem(int position) {
		return data.get(position);
	}

	@Override
	public int getCount() {
		return this.data.size();
	}

	public void setActiveCategory(int active) {
		activeCatId = active;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	public class ViewHolderGroup {

		public RelativeLayout itemLayout;
		public RelativeLayout clickLayout;
		public RobotoRegularTextView catName;

		public ViewHolderGroup(View view) {

			itemLayout = (RelativeLayout) view.findViewById(R.id.itemLayout);
			clickLayout = (RelativeLayout) view.findViewById(R.id.clickLayout);
			catName = (RobotoRegularTextView) view.findViewById(R.id.categoryName);

		}
	}

}