package com.clover.spika.enterprise.chat.fragments;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.ChooseCategoryActivity;
import com.clover.spika.enterprise.chat.GroupsActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.adapters.CategoryAdapter;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.CategoryApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.CustomFragment;
import com.clover.spika.enterprise.chat.models.Category;
import com.clover.spika.enterprise.chat.models.CategoryList;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;

public class CategoryFragment extends CustomFragment implements OnItemClickListener {

	TextView noItems;

	PullToRefreshListView mainListView;
	public CategoryAdapter adapter;
	
	private boolean mIsToChooseCategoryForRoom = false;

	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		adapter = new CategoryAdapter(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_category_list, container, false);
		
		if(getArguments() != null) mIsToChooseCategoryForRoom = getArguments().getBoolean(Const.IS_CHOOSE_CATEGORY, false);

		noItems = (TextView) rootView.findViewById(R.id.noItems);

		mainListView = (PullToRefreshListView) rootView.findViewById(R.id.mainListView);
		mainListView.getRefreshableView().setMotionEventSplittingEnabled(false);
		mainListView.setOnItemClickListener(this);

		mainListView.setAdapter(adapter);
		
		getCategory();

		return rootView;
	}

	private void setData(List<Category> data) {
		List<Category> allData = new ArrayList<Category>();
		if(mIsToChooseCategoryForRoom){
			allData.add(new Category(0, getString(R.string.none)));
		}else{
			allData.add(new Category(0, getString(R.string.all)));
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
		CategoryApi catApi = new CategoryApi();
		catApi.getCategory(getActivity(), true, new ApiCallback<CategoryList>() {
			
			@Override
			public void onApiResponse(Result<CategoryList> result) {
				if (result.isSuccess()) {
					setData(result.getResultData().getCategoryList());
				}else{
					AppDialog dialog = new AppDialog(getActivity(), false);
					dialog.setInfo(result.getResultData().getMessage());
				}
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		position = position - 1;

		if (position != -1 && position != adapter.getCount()) {
			Category category = adapter.getItem(position);
			if(mIsToChooseCategoryForRoom){
				if(getActivity() instanceof ChooseCategoryActivity){
					((ChooseCategoryActivity)getActivity()).returnCategoryIdToActivity(category.getId(), category.getName());
				}
			}else{
				GroupsActivity.startActivity(String.valueOf(category.getId()), getActivity());
			}
			
		}
	}
}
