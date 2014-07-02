package com.example.gamevector;

import jp.co.vector.chat.CharacterListActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener {

    Button go;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_main);

	go = (Button) findViewById(R.id.go);
	go.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
	switch (view.getId()) {
	case R.id.go:

	    Intent intent = new Intent(this, CharacterListActivity.class);
	    startActivity(intent);

	    break;

	default:
	    break;
	}
    }

}
