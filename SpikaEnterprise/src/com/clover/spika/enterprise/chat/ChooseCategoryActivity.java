package com.clover.spika.enterprise.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.fragments.CategoryFragment;
import com.clover.spika.enterprise.chat.utils.Const;

public class ChooseCategoryActivity extends BaseActivity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose_category);

        CategoryFragment frag = CategoryFragment.newInstance(CategoryFragment.UseType.CHOOSE_CATEGORY);

		FragmentTransaction fTrans = getSupportFragmentManager().beginTransaction();
		fTrans.add(R.id.flForChooseCategoryFragment, frag, "fragment_choose_category");
		fTrans.commit();
		
		TextView screenTitle = (TextView) findViewById(R.id.screenTitle);
		screenTitle.setText(R.string.choose_category);
		
		findViewById(R.id.goBack).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setResult(RESULT_OK, new Intent().putExtra(Const.CATEGORY_ID, 0));
				finish();
			}
		});
	}
	
	public void returnCategoryIdToActivity(String categoryId, String categoryName){
		setResult(RESULT_OK, new Intent()
								.putExtra(Const.CATEGORY_ID, categoryId)
								.putExtra(Const.CATEGORY_NAME, categoryName));
		finish();
	}

}
