package com.medicbleep.app.chat.caching.robospice;

import android.app.Activity;

import com.medicbleep.app.chat.caching.CategoryCaching;
import com.medicbleep.app.chat.caching.CategoryCaching.OnCategoryDBChanged;
import com.medicbleep.app.chat.caching.CategoryCaching.OnCategoryNetworkResult;
import com.medicbleep.app.chat.models.CategoryList;
import com.medicbleep.app.chat.services.robospice.CustomSpiceRequest;
import com.octo.android.robospice.SpiceManager;

public class CategoryCacheSpice {

	public static class GetData extends CustomSpiceRequest<CategoryList> {

		private Activity activity;
		private SpiceManager spiceManager;
		private OnCategoryDBChanged onDBChangeListener;
		private OnCategoryNetworkResult onNetworkListener;

		public GetData(Activity activity, SpiceManager spiceManager, OnCategoryDBChanged onDBChangeListener,
				OnCategoryNetworkResult onNetworkListener) {
			super(CategoryList.class);

			this.activity = activity;
			this.spiceManager = spiceManager;
			this.onDBChangeListener = onDBChangeListener;
			this.onNetworkListener = onNetworkListener;
		}

		@Override
		public CategoryList loadDataFromNetwork() throws Exception {

			return CategoryCaching.getData(activity, spiceManager, onDBChangeListener, onNetworkListener);
		}
	}
	
}
