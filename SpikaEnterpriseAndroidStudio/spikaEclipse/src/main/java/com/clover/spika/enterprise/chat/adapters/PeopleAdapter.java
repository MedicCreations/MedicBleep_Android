package com.clover.spika.enterprise.chat.adapters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.MainActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.animation.AnimUtils;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.fragments.PeopleFragment;
import com.clover.spika.enterprise.chat.lazy.ImageLoaderSpice;
import com.clover.spika.enterprise.chat.listeners.OnSwipeTouchListener;
import com.clover.spika.enterprise.chat.models.GlobalModel;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.clover.spika.enterprise.chat.views.RobotoRegularTextView;
import com.clover.spika.enterprise.chat.views.RoundImageView;
import com.octo.android.robospice.SpiceManager;

public class PeopleAdapter extends BaseAdapter {

	public static final int CLOSE_STATE = 0;
	public static final int OPEN_STATE = 1;
	public static final int ANIMATING_STATE = 2;

	private Context mContext;
	private List<GlobalModel> data = new ArrayList<GlobalModel>();

	private ImageLoaderSpice imageLoaderSpice;
	private int marginLeftForAnimation = 10;

	public PeopleAdapter(SpiceManager manager, Context context, Collection<GlobalModel> users, int defaultImage) {
		this.mContext = context;
		this.data.addAll(users);

		marginLeftForAnimation = Utils.getPxFromDp(10, context.getResources());

		imageLoaderSpice = ImageLoaderSpice.getInstance(context);
		imageLoaderSpice.setSpiceManager(manager);
	}
	
	public void setSpiceManager(SpiceManager manager) {
		imageLoaderSpice.setSpiceManager(manager);
	}

	public Context getContext() {
		return mContext;
	}

	public void setData(List<GlobalModel> list) {
		data = list;
		notifyDataSetChanged();
	}

	public void addData(List<GlobalModel> list) {
		data.addAll(list);
		notifyDataSetChanged();
	}

	public List<GlobalModel> getData() {
		return data;
	}

	public void manageData(String manageWith, List<GlobalModel> allData) {
		data.clear();
		data.addAll(allData);
		for (int i = 0; i < data.size(); i++) {
			String firstName = ((User) data.get(i).getModel()).getFirstName();
			String lastName = ((User) data.get(i).getModel()).getLastName();
			if (firstName.toLowerCase(Locale.getDefault()).contains(manageWith.toLowerCase())) {
				continue;
			} else if (lastName.toLowerCase(Locale.getDefault()).contains(manageWith.toLowerCase())) {
				continue;
			} else if ((firstName + " " + lastName).toLowerCase(Locale.getDefault()).contains(manageWith.toLowerCase())) {
				continue;
			} else {
				data.remove(i);
				i--;
			}
		}
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public GlobalModel getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).hashCode();
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		final ViewHolderCharacter holder;
		if (convertView == null) {

			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_people, parent, false);

			holder = new ViewHolderCharacter(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolderCharacter) convertView.getTag();
		}

		// set image to null
		holder.itemImage.setImageDrawable(null);

		holder.controlHolder.setVisibility(View.INVISIBLE);
		holder.controlHolder.setTag(CLOSE_STATE);
		holder.dataHolder.setX(marginLeftForAnimation);

		GlobalModel item = getItem(position);

        holder.itemImage.setTag(false);
		imageLoaderSpice.displayImage(holder.itemImage, item.getImageThumb(), ImageLoaderSpice.DEFAULT_USER_IMAGE);
		((RoundImageView) holder.itemImage).setBorderColor(convertView.getContext().getResources().getColor(R.color.light_light_gray));

		holder.itemName.setText(((User) getItem(position).getModel()).getFirstName() + " " + ((User) getItem(position).getModel()).getLastName());

		if (mContext.getResources().getBoolean(R.bool.enable_web_rtc)) {
			convertView.setOnTouchListener(new OnSwipeTouchListener(mContext) {

				@Override
				public void onSwipeLeft() {
					if ((Integer) holder.controlHolder.getTag() != CLOSE_STATE)
						return;
					holder.controlHolder.setTag(ANIMATING_STATE);
					holder.controlHolder.setVisibility(View.VISIBLE);
					animateToLeft(holder.dataHolder, holder.controlHolder);
				}

				@Override
				public void onSwipeRight() {
					if ((Integer) holder.controlHolder.getTag() != OPEN_STATE)
						return;
					holder.controlHolder.setTag(ANIMATING_STATE);
					animateToRight(holder.dataHolder, holder.controlHolder);
				}
			});

			holder.videoCall.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					((BaseActivity) mContext).callUser((User) getItem(position).getModel(), true);
				}
			});

			holder.voiceCall.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					((BaseActivity) mContext).callUser((User) getItem(position).getModel(), false);
				}
			});
		}

		final PeopleFragment frag = ((MainActivity) mContext).getPeopleFragment();
		final AdapterView<?> adView = (AdapterView<?>) parent;
		final View cv = convertView;

		holder.openChat.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int tempPosition = position + 1;
				frag.onItemClick(adView, cv, tempPosition, getItemId(position));
			}
		});

		if (frag != null)
			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if ((Integer) holder.controlHolder.getTag() == ANIMATING_STATE) {
						// wait to animating finished
					} else if ((Integer) holder.controlHolder.getTag() != CLOSE_STATE) {
						holder.controlHolder.setTag(ANIMATING_STATE);
						animateToRight(holder.dataHolder, holder.controlHolder);
					} else {
						int tempPosition = position + 1;
						frag.onItemClick(adView, cv, tempPosition, getItemId(position));
					}
				}
			});

		return convertView;
	}

	private void animateToLeft(View viewToAnimate, final View viewForWidth) {
		int width = viewForWidth.getWidth();
		AnimUtils.translationX(viewToAnimate, 0, -width, 300, new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				viewForWidth.setTag(OPEN_STATE);
			}
		});
	}

	private void animateToRight(View viewToAnimate, final View viewForWidth) {
		int width = viewForWidth.getWidth();
		AnimUtils.translationX(viewToAnimate, -width, marginLeftForAnimation, 300, new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				viewForWidth.setTag(CLOSE_STATE);
				viewForWidth.setVisibility(View.INVISIBLE);
			}
		});
	}

	public class ViewHolderCharacter {

		public ImageView itemImage;
		public TextView itemName;
		public LinearLayout controlHolder;
		public RelativeLayout dataHolder;
		public TextView videoCall;
		public TextView voiceCall;
		public TextView openChat;

		public ViewHolderCharacter(View view) {
			itemImage = (ImageView) view.findViewById(R.id.item_image);
			itemName = (TextView) view.findViewById(R.id.item_name);
			controlHolder = (LinearLayout) view.findViewById(R.id.callControl);
			videoCall = (TextView) view.findViewById(R.id.videoCall);
			voiceCall = (TextView) view.findViewById(R.id.voiceCall);
			dataHolder = (RelativeLayout) view.findViewById(R.id.clickLayout);
			openChat = (TextView) view.findViewById(R.id.openChat);
		}
	}

}
