package com.clover.spika.enterprise.chat.adapters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.util.TextUtils;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Switch;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.dialogs.AppDialog.OnDismissDialogListener;
import com.clover.spika.enterprise.chat.models.HelperModel;
import com.clover.spika.enterprise.chat.models.UserDetail;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Utils;

public class UserDetailsAdapter extends BaseAdapter {

	private Context mContext;
	private List<UserDetail> mUserDetailValues = new ArrayList<UserDetail>();
	private boolean isShowNotEdit = false;

	public UserDetailsAdapter(Context context, List<UserDetail> userDetailValues, List<Map<String, String>> userDetails, boolean isShowNotEdit) {

		this.mContext = context;

		setNewData(userDetailValues, userDetails, isShowNotEdit);

	}

	public void setNewData(List<UserDetail> userDetailValues, List<Map<String, String>> userDetails, boolean isShowNotEdit) {
		mUserDetailValues.clear();
		for (UserDetail usDet : userDetailValues) {

			boolean isAdd = !isShowNotEdit;

			if (userDetails != null) {
				for (Map<String, String> val : userDetails) {
					if (val.containsKey(usDet.getKey())) {

						if (!TextUtils.isEmpty(val.get(usDet.getKey()))) {

							usDet.setValue(val.get(usDet.getKey()));

							if (val.get(Const.PUBLIC).equals("1") || val.get(Const.PUBLIC).equals("true")) {
								usDet.setPublicValue(true);
								isAdd = true;
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
			}

			if (isAdd) {
				mUserDetailValues.add(usDet);
			}
		}
	}

	public void setShowNotEdit(boolean b) {
		isShowNotEdit = b;
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

		holder.editLabel.setText(userDetail.getLabel());
		holder.editValue.setText(userDetail.getValue());

		if (!isShowNotEdit) {
			holder.editValue.setOnClickListener(new View.OnClickListener() {

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
		} else {
			holder.editValue.setClickable(false);
		}

		if (isShowNotEdit) {
			((View) holder.switchDetailPublic.getParent()).setVisibility(View.GONE);
			if (userDetail.getKey().equals("email")) {
				holder.editValue.setTextColor(mContext.getResources().getColor(R.color.default_blue));
				holder.editValue.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Utils.emailIntent(mContext, userDetail.getValue(), "", "");
					}
				});
			} else if (userDetail.getKey().equals("mobile_number") || userDetail.getKey().equals("phone_number") || userDetail.getKey().equals("fax")) {
				holder.editValue.setTextColor(mContext.getResources().getColor(R.color.default_blue));
				holder.editValue.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Utils.phoneIntent(mContext, userDetail.getValue());
					}
				});
			} else if (userDetail.getKey().equals("home_address") || userDetail.getKey().equals("job_address")) {
				holder.editValue.setTextColor(mContext.getResources().getColor(R.color.default_blue));
				holder.editValue.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Utils.mapIntent(mContext, userDetail.getValue());
					}
				});
			} else if (userDetail.getKey().equals("web_site")) {
				holder.editValue.setTextColor(mContext.getResources().getColor(R.color.default_blue));
				holder.editValue.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Utils.browserIntent(mContext, userDetail.getValue());
					}
				});
			} else {
				holder.editValue.setTextColor(Color.BLACK);
				holder.editValue.setOnClickListener(null);
			}
		} else {
			holder.switchDetailPublic.setChecked(userDetail.isPublicValue());
			((View) holder.switchDetailPublic.getParent()).setVisibility(View.VISIBLE);
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
		}

		return convertView;
	}

	public class ViewHolderDetail {

		public TextView editLabel;
		public TextView editValue;
		public Switch switchDetailPublic;
		public View switchSwitcher;

		public ViewHolderDetail(View view) {

			editLabel = (TextView) view.findViewById(R.id.editDetailLabel);
			editValue = (TextView) view.findViewById(R.id.editDetailValue);
			switchDetailPublic = (Switch) view.findViewById(R.id.switchDetailPublic);
			switchSwitcher = view.findViewById(R.id.switchSwitcher);
		}
	}

}
