package com.clover.spika.enterprise.chat.views;

import static com.clover.spika.enterprise.chat.utils.ProfileUtils.mapToKey;

import java.util.Map;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.R;

public class DetailsView extends LinearLayout {

	public DetailsView(Context context) {
		super(context);
		init(context);
	}

	public DetailsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public DetailsView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		setOrientation(LinearLayout.VERTICAL);
	}

	private LinearLayout createRow() {

		LinearLayout row = new LinearLayout(getContext());
		row.setOrientation(LinearLayout.VERTICAL);
		addView(row, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

		return row;
	}

	private TextView createTextView(String text, LinearLayout parent) {
		TextView textView = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.layout_detail_textview, parent, false);
		parent.addView(textView);
		textView.setText(text);
		return textView;
	}

	public void createDetailsView(Map<String, String> detailsMap) {

		removeAllViews();

		for (String key : detailsMap.keySet()) {

			LinearLayout row = createRow();

			createTextView(mapToKey(key, getContext()), row);

			TextView value = createTextView(detailsMap.get(key), row);
			value.setTypeface(value.getTypeface(), Typeface.BOLD);
		}
	}

}
