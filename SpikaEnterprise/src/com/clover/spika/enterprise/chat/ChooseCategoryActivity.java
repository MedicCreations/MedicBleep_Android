package com.clover.spika.enterprise.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.ChatApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.clover.spika.enterprise.chat.fragments.CategoryFragment;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.utils.Const;
import java.util.HashMap;

public class ChooseCategoryActivity extends BaseActivity {

	private String chatId = null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose_category);

		Bundle extras = getIntent().getExtras();

		if (extras != null && extras.containsKey(Const.CHAT_ID)) {
			chatId = extras.getString(Const.CHAT_ID);
		}

		CategoryFragment frag = CategoryFragment.newInstance(CategoryFragment.UseType.CHOOSE_CATEGORY);

		FragmentTransaction fTrans = getSupportFragmentManager().beginTransaction();
		fTrans.add(R.id.flForChooseCategoryFragment, frag, "fragment_choose_category");
		fTrans.commit();

		TextView screenTitle = (TextView) findViewById(R.id.screenTitle);
		screenTitle.setText(R.string.select_category);

		findViewById(R.id.goBack).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				setResult(RESULT_OK, new Intent().putExtra(Const.CATEGORY_ID, 0));
				finish();
			}
		});
	}

	public void returnCategoryIdToActivity(final String categoryId, final String categoryName) {

		if (chatId != null) {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put(Const.CHAT_ID, chatId);
			params.put(Const.CATEGORY_ID, categoryId);
			new ChatApi().updateChatAll(params, true, this, new ApiCallback<BaseModel>() {

				@Override
				public void onApiResponse(Result<BaseModel> result) {
					if (result.isSuccess()) {
						Intent data = new Intent();
						data.putExtra(Const.CATEGORY_ID, categoryId);
						data.putExtra(Const.CATEGORY_NAME, categoryName);
						setResult(RESULT_OK, data);
						finish();
					} else {
						AppDialog dialog = new AppDialog(ChooseCategoryActivity.this, false);
						dialog.setFailed(result.getResultData().getCode());
					}
				}
			});
		} else {
			setResult(RESULT_OK, new Intent().putExtra(Const.CATEGORY_ID, categoryId).putExtra(Const.CATEGORY_NAME, categoryName));
			finish();
		}
	}
}
