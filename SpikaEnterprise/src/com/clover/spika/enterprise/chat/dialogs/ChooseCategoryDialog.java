package com.clover.spika.enterprise.chat.dialogs;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.adapters.CategoryAdapter;
import com.clover.spika.enterprise.chat.caching.CategoryCaching.OnCategoryDBChanged;
import com.clover.spika.enterprise.chat.caching.CategoryCaching.OnCategoryNetworkResult;
import com.clover.spika.enterprise.chat.caching.robospice.CategoryCacheSpice;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.models.Category;
import com.clover.spika.enterprise.chat.models.CategoryList;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;
import com.octo.android.robospice.persistence.exception.SpiceException;

public class ChooseCategoryDialog extends Dialog implements OnItemClickListener, OnCategoryDBChanged, OnCategoryNetworkResult{

	public ChooseCategoryDialog(final Context context, UseType useType, int activeCategory) {
		super(context, R.style.Theme_Dialog);
		setOwnerActivity((Activity) context);
		
		this.mUseType = useType;
		this.activeCategory = activeCategory;
	}
	
	public static enum UseType {
		CHOOSE_CATEGORY, SEARCH
	}
	
	private UseType mUseType;
	
	TextView noItems;

	PullToRefreshListView mainListView;
	public CategoryAdapter adapter;
	
	private OnActionClick listener;
	
	private int activeCategory;
	
	private List<Category> listCategory = new ArrayList<Category>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_select_category);
		
		adapter = new CategoryAdapter(getOwnerActivity());
		
		noItems = (TextView) findViewById(R.id.noItems);

		mainListView = (PullToRefreshListView) findViewById(R.id.mainListView);
		mainListView.getRefreshableView().setMotionEventSplittingEnabled(false);
		mainListView.setOnItemClickListener(this);

		mainListView.setAdapter(adapter);

		getCategory();
		
		findViewById(R.id.closeBtn).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (listener != null) listener.onCloseClick(ChooseCategoryDialog.this);
				else dismiss();
			}
		});
		
		findViewById(R.id.acceptButton).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (listener != null) listener.onAcceptClick(ChooseCategoryDialog.this);
				else dismiss();
			}
		});

		
	}
	
	public void setListener (OnActionClick lis){
		listener = lis;
	}
	
	private void setData(List<Category> data) {
		List<Category> allData = new ArrayList<Category>();

		if (UseType.CHOOSE_CATEGORY.equals(mUseType)) {
			allData.add(new Category(0, getOwnerActivity().getResources().getString(R.string.none)));
		} else {
			allData.add(new Category(0, getOwnerActivity().getResources().getString(R.string.all)));
		}

		allData.addAll(data);

		mainListView.setMode(PullToRefreshBase.Mode.DISABLED);
		adapter.setActiveCategory(activeCategory);
		adapter.setData(allData);
		adapter.notifyDataSetChanged();

		if (adapter.getCount() == 0) {
			noItems.setVisibility(View.VISIBLE);
		} else {
			noItems.setVisibility(View.GONE);
		}

	}

	public void getCategory() {
		CategoryCacheSpice.GetData categoryCacheSpice = new CategoryCacheSpice.GetData(getOwnerActivity(), ((BaseActivity)getOwnerActivity()).spiceManager, this, this);
		((BaseActivity)getOwnerActivity()).spiceManager.execute(categoryCacheSpice, new CustomSpiceListener<CategoryList>(){
			
			@Override
			public void onRequestFailure(SpiceException ex) {
				super.onRequestFailure(ex);
				findViewById(R.id.progressLoading).setVisibility(View.GONE);
				Utils.onFailedUniversal(null, getOwnerActivity());
			}

			@Override
			public void onRequestSuccess(CategoryList result) {
				super.onRequestSuccess(result);
				
				listCategory.addAll(result.categories);
				
				if(result.categories.size() > 0){
					findViewById(R.id.progressLoading).setVisibility(View.GONE);
					setData(result.categories);
				}
				
			}
			
		});
		
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		//header is 0 position
		position = position - 1;

		if (position != -1 && position != adapter.getCount()) {
			Category category = adapter.getItem(position);
			
			if(listener != null) listener.onCategorySelect(String.valueOf(category.id), category.name, this);

		}
	}
	
	public interface OnActionClick{
		public void onAcceptClick(Dialog d);
		public void onCloseClick(Dialog d);
		public void onCategorySelect(String categoryId, String categoryName, Dialog d);
	}

	@Override
	public void onCategoryNetworkResult() {}

	@Override
	public void onCategoryDBChanged(CategoryList categoryList) {
		if(!categoryList.categories.equals(listCategory)){
			findViewById(R.id.progressLoading).setVisibility(View.GONE);
			setData(categoryList.categories);
		}
	}
	
}