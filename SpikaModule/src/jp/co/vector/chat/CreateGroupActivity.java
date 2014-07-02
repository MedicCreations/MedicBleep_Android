package jp.co.vector.chat;

import jp.co.vector.chat.extendables.BaseActivity;
import jp.co.vector.chat.utils.Const;
import jp.co.vector.chat.R;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class CreateGroupActivity extends BaseActivity implements OnClickListener {

    private static final int ADD_MEMBERS = 2048;

    ImageView headerEditBack;
    EditText etTalkTitle;

    LinearLayout btnCreate;
    LinearLayout btnCancel;

    @Override
    protected void onCreate(Bundle arg0) {
	super.onCreate(arg0);
	setContentView(R.layout.activity_create_group);

	headerEditBack = (ImageView) findViewById(R.id.headerEditBack);
	headerEditBack.setOnClickListener(this);

	etTalkTitle = (EditText) findViewById(R.id.etTalkTitle);

	btnCreate = (LinearLayout) findViewById(R.id.btnCreate);
	btnCreate.setOnClickListener(this);

	btnCancel = (LinearLayout) findViewById(R.id.btnCancel);
	btnCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

	int id = view.getId();
	if (id == R.id.btnCreate) {
	    if (TextUtils.isEmpty(etTalkTitle.getText().toString())) {
		return;
	    }
	    createGroup(etTalkTitle.getText().toString());
	} else if (id == R.id.headerEditBack || id == R.id.btnCancel) {
	    finish();
	} else {
	}
    }

    private void createGroup(String groupName) {
	Intent intent = new Intent(this, AddMembers.class);
	intent.putExtra(Const.GROUP_NAME, groupName);
	startActivityForResult(intent, ADD_MEMBERS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	super.onActivityResult(requestCode, resultCode, data);

	if (resultCode == RESULT_OK) {
	    switch (requestCode) {
	    case ADD_MEMBERS:
		if (data.getExtras().getInt(Const.CODE) == Const.E_SUCCESS) {
		    finish();
		}
		break;

	    default:
		break;
	    }
	}
    }
}
