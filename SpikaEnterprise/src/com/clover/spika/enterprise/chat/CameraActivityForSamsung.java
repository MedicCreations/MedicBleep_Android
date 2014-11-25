package com.clover.spika.enterprise.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;

import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;

import java.io.File;

public class CameraActivityForSamsung extends BaseActivity {
	
	public static void start(String path, Context c){
		Intent intent = new Intent(c, CameraActivityForSamsung.class);
		intent.putExtra("path", path);
		c.startActivity(intent);
	}
	
	public static void startForResult(String path, int request, Activity a){
		Intent intent = new Intent(a, CameraActivityForSamsung.class);
		intent.putExtra("path", path);
		a.startActivityForResult(intent, request);
	}
	
	private String path;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(new View(this));
		
		path = Environment.getExternalStorageDirectory()+"/"+"temp.jpg";
		
		if(SpikaEnterpriseApp.getInstance().samsungImagePath() == null){
			File file = new File(path);
			Uri outputFileUri = Uri.fromFile(file);
			
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
			startActivityForResult(intent, 1);
		}else{
			setResult(RESULT_OK);
			finish();
			return;
		}
		
		SpikaEnterpriseApp.getInstance().setSamsungImagePath(path);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_OK) {
			setResult(RESULT_OK);
			finish();
		} else {
			setResult(RESULT_CANCELED);
			SpikaEnterpriseApp.getInstance().setSamsungImagePath("-1");
			finish();
		}
	}
	
}