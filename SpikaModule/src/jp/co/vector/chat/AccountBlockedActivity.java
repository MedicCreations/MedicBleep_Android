package jp.co.vector.chat;

import jp.co.vector.chat.extendables.BaseActivity;
import jp.co.vector.chat.R;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class AccountBlockedActivity extends BaseActivity implements OnClickListener {

    ImageView headerEditBack;

    @Override
    protected void onCreate(Bundle arg0) {
	super.onCreate(arg0);
	setContentView(R.layout.activity_blocked_account);

	headerEditBack = (ImageView) findViewById(R.id.headerEditBack);
	headerEditBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
	int id = view.getId();
	if (id == R.id.headerEditBack) {
	    goBack();
	} else {
	}
    }

    @Override
    public void onBackPressed() {
	goBack();
    }

    private void goBack() {
	Intent intent = new Intent(this, CharacterListActivity.class);
	startActivity(intent);
	finish();
    }

}