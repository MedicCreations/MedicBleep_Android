package com.clover.spika.enterprise.chat.adapters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.util.TextUtils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Switch;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.R.color;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.dialogs.AppDialog.OnDismissDialogListener;
import com.clover.spika.enterprise.chat.models.HelperModel;
import com.clover.spika.enterprise.chat.models.UserDetail;
import com.clover.spika.enterprise.chat.utils.Const;

public class UserDetailsAdapter extends BaseAdapter {

	private Context mContext;
	private List<UserDetail> mUserDetailValues = new ArrayList<UserDetail>();

	public UserDetailsAdapter(Context context, List<UserDetail> userDetailValues, List<Map<String, String>> userDetails) {

		this.mContext = context;

		for (UserDetail usDet : userDetailValues) {

			boolean isAdd = true;

			for (Map<String, String> val : userDetails) {
				if (val.containsKey(usDet.getKey())) {

					if (!TextUtils.isEmpty(val.get(usDet.getKey()))) {

						usDet.setValue(val.get(usDet.getKey()));

						if (val.get(Const.PUBLIC).equals("1") || val.get(Const.PUBLIC).equals("true")) {
							usDet.setPublicValue(true);
						} else {
							usDet.setPublicValue(false);
						}

						break;
					} else {

						isAdd = false;
						break;
					}
				}
			}

			if (isAdd) {
				mUserDetailValues.add(usDet);
			}
		}
	}

	public List<UserDetail> getList() {
		return mUserDetailValues;
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

		final UserDetail userDetail = mUserDetailValues.get(position);
		userDetail.setPosition(position);

		if (!TextUtils.isEmpty(userDetail.getValue())) {
			holder.editDetail.setText(userDetail.getValue());
			holder.editDetail.setTextColor(mContext.getResources().getColor(R.color.default_blue));
		} else {
			holder.editDetail.setText(userDetail.getLabel());
			holder.editDetail.setTextColor(color.gray_in_adapter);
		}

		holder.editDetail.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				AppDialog dialog = new AppDialog(mContext, false);
				dialog.setEditDialog(userDetail, new OnDismissDialogListener() {

					@Override
					public void onDismissDialog(Object object) {

						if (object != null) {
							HelperModel realObject = (HelperModel) object;
							mUserDetailValues.get(realObject.getPosition()).setValue(realObject.getValue());
							notifyDataSetChanged();
						}
					}
				});
			}
		});

		holder.switchDetailPublic.setChecked(userDetail.isPublicValue());
		holder.switchSwitcher.setTag(position);
		holder.switchSwitcher.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				int position = (Integer) v.getTag();

				if (mUserDetailValues.get(position).isPublicValue()) {
					mUserDetailValues.get(position).setPublicValue(false);
				} else {
					mUserDetailValues.get(position).setPublicValue(true);
				}

				notifyDataSetChanged();
			}
		});

		return convertView;
	}

	public class ViewHolderDetail {

		public TextView editDetail;
		public Switch switchDetailPublic;
		public View switchSwitcher;

		public ViewHolderDetail(View view) {

			editDetail = (TextView) view.findViewById(R.id.editDetail);
			switchDetailPublic = (Switch) view.findViewById(R.id.switchDetailPublic);
			switchSwitcher = (View) view.findViewById(R.id.switchSwitcher);
		}
	}

}
