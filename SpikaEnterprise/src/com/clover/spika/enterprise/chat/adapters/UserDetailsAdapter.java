package com.clover.spika.enterprise.chat.adapters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.util.TextUtils;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.models.UserDetail;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.views.RobotoThinEditText;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Switch;

public class UserDetailsAdapter extends BaseAdapter {

	private Context mContext;
	private List<UserDetail> mUserDetailValues = new ArrayList<UserDetail>();

	public UserDetailsAdapter(Context context, List<UserDetail> userDetailValues, List<Map<String, String>> userDetails) {

		this.mContext = context;

		// TODO Sniša mi mora objasniti, Vida
		if (userDetails != null && !userDetails.isEmpty()) {

			for (Map<String, String> detail : userDetails) {

				for (UserDetail userDetail : userDetailValues) {

					if (detail.containsKey(userDetail.getKey()) && !TextUtils.isEmpty(detail.get(userDetail.getKey()))) {

						userDetail.setValue(detail.get(userDetail.getKey()));

						if (detail.get(Const.PUBLIC).equals("1") || detail.get(Const.PUBLIC).equals("true")) {
							userDetail.setPublicValue(true);
						} else {
							userDetail.setPublicValue(false);
						}

						mUserDetailValues.add(userDetail);
					} else {
						mUserDetailValues.add(userDetail);
					}
				}
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
	public View getView(final int position, View convertView, ViewGroup parent) {

		final ViewHolderDetail holder;
		if (convertView == null) {

			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_detail_values, parent, false);

			holder = new ViewHolderDetail(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolderDetail) convertView.getTag();
		}

		UserDetail userDetail = mUserDetailValues.get(position);

		if (!TextUtils.isEmpty(userDetail.getValue())) {
			holder.editDetail.setText(userDetail.getValue());
		} else {
			holder.editDetail.setText("");
		}

		holder.editDetail.setHint(userDetail.getLabel());

		holder.editDetail.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					EditText edit = (EditText) v;
					mUserDetailValues.get(position).setValue(edit.getText().toString());
				}
			}
		});

		holder.switchDetailPublic.setChecked(userDetail.isPublicValue());
		holder.switchDetailPublic.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mUserDetailValues.get(position).setPublicValue(isChecked);
			}
		});

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
