package com.clover.spika.enterprise.chat.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.R;

import java.util.Map;

public class DetailsScrollView extends ScrollView {

    private LinearLayout mHolder;

    public DetailsScrollView(Context context) {
        super(context);
        init(context);
    }

    public DetailsScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DetailsScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mHolder = new LinearLayout(context);
        mHolder.setOrientation(LinearLayout.VERTICAL);
        this.addView(mHolder, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout createRow() {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.VERTICAL);
        mHolder.addView(row, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        return row;
    }

    private TextView createTextView(String text, LinearLayout parent) {
        TextView textView = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.layout_detail_textview, parent, false);
        parent.addView(textView);
        textView.setText(text);
        return textView;
    }

    public void createDetailsView(Map<String, String> detailsMap) {
        mHolder.removeAllViews();

        for (String key : detailsMap.keySet()) {
            LinearLayout row = createRow();

            createTextView(key, row);

            TextView value = createTextView(detailsMap.get(key), row);
            value.setTypeface(value.getTypeface(), Typeface.BOLD);
        }
    }
}
