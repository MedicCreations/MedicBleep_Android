package com.medicbleep.app.chat.adapters;

import java.util.ArrayList;
import java.util.List;

import com.medicbleep.app.chat.R;
import com.medicbleep.app.chat.models.Organization;
import com.medicbleep.app.chat.views.RobotoRegularTextView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;

public class OrganizationAdapter extends BaseAdapter{
	
	private List<Organization> organizationList;
	private Context context;
	
	public OrganizationAdapter(Context context){
		this.context = context;
		organizationList = new ArrayList<Organization>();
	}
	
	public void setData(List<Organization> list){
		this.organizationList = list;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return organizationList.size();
	}

	@Override
	public Object getItem(int position) {
		return organizationList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		final ViewHolderOrganization holder;
		if (convertView == null) {

			convertView = LayoutInflater.from(context).inflate(R.layout.item_organization, parent, false);

			holder = new ViewHolderOrganization(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolderOrganization) convertView.getTag();
		}

		// Assign values
		final Organization organization = (Organization) getItem(position);

		holder.organizationName.setText(organization.name);

		return convertView;
	}
	
	
	public class ViewHolderOrganization {

		public RobotoRegularTextView organizationName;
		public RelativeLayout itemLayout;

		public ViewHolderOrganization(View view) {

			organizationName = (RobotoRegularTextView) view.findViewById(R.id.organizationName);
			itemLayout = (RelativeLayout) view.findViewById(R.id.itemLayout);
		}
	}

}
