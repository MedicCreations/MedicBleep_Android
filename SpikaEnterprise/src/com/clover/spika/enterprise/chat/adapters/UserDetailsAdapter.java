package com.clover.spika.enterprise.chat.adapters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.models.UserDetail;
import com.clover.spika.enterprise.chat.views.RobotoThinEditText;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Switch;

public class UserDetailsAdapter extends BaseAdapter{

	private Context mContext;
	private List<UserDetail> mUserDetailValues = new ArrayList<UserDetail>();
	private List<Map<String, String>> mUserDetails = new ArrayList<Map<String,String>>();
	
	
	public UserDetailsAdapter(Context context, List<UserDetail> userDetailValues, List<Map<String, String>> userDetails){
		
		this.mContext = context;
		this.mUserDetailValues = userDetailValues;
		this.mUserDetails = userDetails;
	}
	
	@Override
	public int getCount() {
		return mUserDetailValues.size();
	}

	@Override
	public Object getItem(int position) {
		return mUserDetailValues.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolderDetail holder;
		if (convertView == null) {

			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_detail_values, parent, false);

			holder = new ViewHolderDetail(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolderDetail) convertView.getTag();
		}
		
		UserDetail userDetail = mUserDetailValues.get(position);
		
		for (int i = 0; i < mUserDetails.size(); i++) {
			
			Map<String, String> detail = mUserDetails.get(i);
			if (detail.containsKey(userDetail.getKey()) && !detail.get(userDetail.getKey()).equals("")){
				holder.editDetail.setText(detail.get(userDetail.getKey()));
				if (detail.get("public").equals("1")){
					holder.switchDetailPublic.setChecked(true);
				} else {
					holder.switchDetailPublic.setChecked(false);
				}
				
				break;
			} else {
				holder.editDetail.setHint(userDetail.getLabel());
				holder.switchDetailPublic.setChecked(false);
			}
			
		}
		
		return convertView;
	}
	
	public class ViewHolderDetail {

		public RobotoThinEditText editDetail;
		public Switch switchDetailPublic;
		
		public ViewHolderDetail(View view) {

			editDetail = (RobotoThinEditText) view.findViewById(R.id.editDetail);
			switchDetailPublic = (Switch) view.findViewById(R.id.switchDetailPublic);
			
		}

	}

}
