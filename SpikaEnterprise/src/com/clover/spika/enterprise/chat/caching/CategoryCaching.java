package com.clover.spika.enterprise.chat.caching;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.text.TextUtils;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.api.robospice.CategorySpice;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.models.Category;
import com.clover.spika.enterprise.chat.models.CategoryList;
import com.clover.spika.enterprise.chat.models.greendao.CategoryDao;
import com.clover.spika.enterprise.chat.models.greendao.CategoryDao.Properties;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceRequest;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;

public class CategoryCaching {

	public static CategoryList getData(final Activity activity, final SpiceManager spiceManager, final OnCategoryDBChanged onDBChangeListener,
			final OnCategoryNetworkResult onNetworkListener) {

		CategoryList resultArray = getDBData(activity);

		CategorySpice.GetCategory getCategory = new CategorySpice.GetCategory(activity);
		spiceManager.execute(getCategory, new CustomSpiceListener<CategoryList>() {

			@Override
			public void onRequestFailure(SpiceException ex) {
				super.onRequestFailure(ex);
				Utils.onFailedUniversal(null, activity);
			}

			@Override
			public void onRequestSuccess(final CategoryList result) {
				super.onRequestSuccess(result);

				String message = activity.getResources().getString(R.string.e_something_went_wrong);

				if (result.getCode() == Const.API_SUCCESS) {

					if (onNetworkListener != null) {
						onNetworkListener.onCategoryNetworkResult();
					}

					HandleNewData handleNewData = new HandleNewData(activity, result.categories, onDBChangeListener);
					spiceManager.execute(handleNewData, null);

				} else {

					if (result != null && !TextUtils.isEmpty(result.getMessage())) {
						message = result.getMessage();
					}

					Utils.onFailedUniversal(message, activity);
				}
			}

		});

		return resultArray;
	}

	private static CategoryList getDBData(Activity activity) {

		List<Category> resultArray = new ArrayList<Category>();

		if (activity instanceof BaseActivity) {

			CategoryDao categoryDao = ((BaseActivity) activity).getDaoSession().getCategoryDao();
			List<com.clover.spika.enterprise.chat.models.greendao.Category> lista = categoryDao.queryBuilder().orderAsc(Properties.Id).build().list();

			if (lista != null) {

				for (com.clover.spika.enterprise.chat.models.greendao.Category cat : lista) {
					resultArray.add(handleOldData(cat));
				}
			}
		}

		CategoryList holder = new CategoryList();
		holder.categories = resultArray;
		return holder;
	}

	private static Category handleOldData(com.clover.spika.enterprise.chat.models.greendao.Category category) {

		Category finalCategory = new Category();

		finalCategory.id = String.valueOf(category.getId());
		finalCategory.name = category.getName();

		return finalCategory;
	}

	private static class HandleNewData extends CustomSpiceRequest<Void> {

		private Activity activity;
		private List<Category> categories;
		private OnCategoryDBChanged onDBChangeListener;

		public HandleNewData(Activity activity, List<Category> categories, OnCategoryDBChanged onDBChangeListener) {
			super(Void.class);

			this.activity = activity;
			this.categories = categories;
			this.onDBChangeListener = onDBChangeListener;
		}

		@Override
		public Void loadDataFromNetwork() throws Exception {

			handleNewData(activity, categories);

			final CategoryList finalResult = getDBData(activity);

			activity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (onDBChangeListener != null) {
						onDBChangeListener.onCategoryDBChanged(finalResult);
					}
				}
			});

			return null;
		}
	}

	private static void handleNewData(Activity activity, List<Category> networkData) {

		if (activity instanceof BaseActivity) {

			CategoryDao categoryDao = ((BaseActivity) activity).getDaoSession().getCategoryDao();
			categoryDao.deleteAll();

			for (Category cat : networkData) {

				com.clover.spika.enterprise.chat.models.greendao.Category finalCatModel = new com.clover.spika.enterprise.chat.models.greendao.Category(
						Long.valueOf(cat.id), cat.name);

				categoryDao.insert(finalCatModel);
			}
		}
	}

	public interface OnCategoryDBChanged {
		public void onCategoryDBChanged(CategoryList categoryList);
	}

	public interface OnCategoryNetworkResult {
		public void onCategoryNetworkResult();
	}

}
