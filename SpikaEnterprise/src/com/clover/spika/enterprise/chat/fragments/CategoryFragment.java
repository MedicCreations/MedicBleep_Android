package com.clover.spika.enterprise.chat.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.ChooseCategoryActivity;
import com.clover.spika.enterprise.chat.GroupsActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.RoomsActivity;
import com.clover.spika.enterprise.chat.adapters.CategoryAdapter;
import com.clover.spika.enterprise.chat.api.robospice.CategorySpice;
import com.clover.spika.enterprise.chat.extendables.CustomFragment;
import com.clover.spika.enterprise.chat.models.Category;
import com.clover.spika.enterprise.chat.models.CategoryList;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;
import com.octo.android.robospice.persistence.exception.SpiceException;

import java.util.ArrayList;
import java.util.List;

public class CategoryFragment extends CustomFragment implements OnItemClickListener {

	private static final String ARG_USE_TYPE = "com.clover.spika.enterprise.chat.arg_use_type";

	public static enum UseType {
		CHOOSE_CATEGORY, ROOM, GROUP
	}

	public static CategoryFragment newInstance(@NonNull UseType useType) {
		CategoryFragment fragment = new CategoryFragment();
		Bundle arguments = new Bundle();
		arguments.putSerializable(ARG_USE_TYPE, useType);
		fragment.setArguments(arguments);
		return fragment;
	}

	TextView noItems;

	PullToRefreshListView mainListView;
	public CategoryAdapter adapter;

	private UseType mUseType;

	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		adapter = new CategoryAdapter(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_category_list, container, false);

		unpackArguments();

		noItems = (TextView) rootView.findViewById(R.id.noItems);

		mainListView = (PullToRefreshListView) rootView.findViewById(R.id.mainListView);
		mainListView.getRefreshableView().setMotionEventSplittingEnabled(false);
		mainListView.setOnItemClickListener(this);

		mainListView.setAdapter(adapter);

		getCategory();

		return rootView;
	}

	private void unpackArguments() {
		mUseType = (UseType) getArguments().getSerializable(ARG_USE_TYPE);
	}

	private void setData(List<Category> data) {
		List<Category> allData = new ArrayList<Category>();

		if (isAdded()) {

			if (UseType.CHOOSE_CATEGORY.equals(mUseType)) {
				allData.add(new Category(0, getString(R.string.none)));
			} else {
				allData.add(new Category(0, getString(R.string.all)));
			}
		}

		allData.addAll(data);

		mainListView.setMode(PullToRefreshBase.Mode.DISABLED);
		adapter.setData(allData);
		adapter.notifyDataSetChanged();

		if (adapter.getCount() == 0) {
			noItems.setVisibility(View.VISIBLE);
		} else {
			noItems.setVisibility(View.GONE);
		}

	}

	public void getCategory() {

		handleProgress(true);
		CategorySpice.GetCategory getCategory = new CategorySpice.GetCategory(getActivity());
		spiceManager.execute(getCategory, new CustomSpiceListener<CategoryList>() {

			@Override
			public void onRequestFailure(SpiceException arg0) {
				super.onRequestFailure(arg0);
				handleProgress(false);
				Utils.onFailedUniversal(null, getActivity());
			}

			@Override
			public void onRequestSuccess(CategoryList result) {
				super.onRequestSuccess(result);
				handleProgress(false);

				if (result.getCode() == Const.API_SUCCESS) {
					setData(result.categories);
				} else {

					String message = "";

					if (result != null && result.getMessage() != null) {
						message = result.getMessage();
					} else {
						message = getString(R.string.e_something_went_wrong);
					}

					Utils.onFailedUniversal(message, getActivity());
				}
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		position = position - 1;

		if (position != -1 && position != adapter.getCount()) {
			Category category = adapter.getItem(position);

			switch (mUseType) {
			case CHOOSE_CATEGORY:
				if (getActivity() instanceof ChooseCategoryActivity) {
					((ChooseCategoryActivity) getActivity()).returnCategoryIdToActivity(String.valueOf(category.id), category.name);
				}
				break;

			case ROOM:
				RoomsActivity.startActivity(String.valueOf(category.id), category.name, getActivity());
				break;

			case GROUP:
				GroupsActivity.startActivity(String.valueOf(category.id), getActivity());
				break;
			}

		}
	}
}
