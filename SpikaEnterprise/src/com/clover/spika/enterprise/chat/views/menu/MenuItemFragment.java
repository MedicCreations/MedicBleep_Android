package com.clover.spika.enterprise.chat.views.menu;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.extendables.CustomFragment;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.Utils;

public class MenuItemFragment extends CustomFragment {
	
	private int position=-1;
	private List<String> filesList = new ArrayList<String>();
	private int width = 0;
	
	private Camera camera;
	private SurfaceHolder surfaceHolder;
	private int itemHeight;
	private int itemWidth;
	
	private SelectImageListener listener;
	
	public static MenuItemFragment newInstance(List<String> filesList, int position) {
		MenuItemFragment fragment = new MenuItemFragment();
		Bundle arguments = new Bundle();
		arguments.putInt(Const.POSITION, position);
		arguments.putStringArrayList(Const.FILE, (ArrayList<String>) filesList);
		fragment.setArguments(arguments);
		return fragment;
	}
	
	public void setListener(SelectImageListener lis){
		listener = lis;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View superView = inflater.inflate(R.layout.layout_menu_item_fragment, container, false);
		
		position = getArguments().getInt(Const.POSITION, 0);
		filesList = getArguments().getStringArrayList(Const.FILE);
		
		width = container.getContext().getResources().getDisplayMetrics().widthPixels - Helper.dpToPx(container.getContext(), 50);
		int itemSizeWidth = width / 4;
		int itemSizeHeight = Helper.dpToPx(getActivity(), 80);
		
		List<String> imagesToShow = new ArrayList<String>();
		if(position > 0) {
			if(filesList.size() > position * 4 - 1) imagesToShow.add(filesList.get(position*4 - 1));
		}else{
			imagesToShow.add(null);
		}
		if(filesList.size() > position * 4) imagesToShow.add(filesList.get(position*4)); 
		if(filesList.size() > position * 4 + 1) imagesToShow.add(filesList.get(position*4 + 1)); 
		if(filesList.size() > position * 4 + 2) imagesToShow.add(filesList.get(position*4 + 2)); 
		
		LinearLayout layout = (LinearLayout) superView.findViewById(R.id.layoutLinear);
		for(int i = 0; i < imagesToShow.size(); i++){
			
			if(position == 0 && i == 0){
				RelativeLayout rl = new RelativeLayout(getActivity());
				
				layout.addView(rl);
				
				setCamera(rl, itemSizeHeight - 10, itemSizeWidth - 10);
				
				rl.getLayoutParams().width = itemSizeWidth;
				rl.getLayoutParams().height = itemSizeHeight;
				rl.setGravity(Gravity.CENTER);
				
				rl.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(camera != null) stop_camera();
						if(listener != null) listener.onSelectImage("camera");
					}
				});
			}else{
				LinearLayout ll = new LinearLayout(getActivity());
				
				layout.addView(ll);
				
				ImageView iv = new ImageView(getActivity());
				ll.addView(iv);
				
				int imageSize = itemSizeHeight > itemSizeWidth ? itemSizeHeight : itemSizeWidth;
				
				iv.getLayoutParams().height = itemSizeHeight - 10;
				iv.getLayoutParams().width = itemSizeWidth - 10;
				iv.setScaleType(ScaleType.CENTER_CROP);
				
				iv.setImageBitmap(Utils.resizeBitmap(imageSize, imageSize, imagesToShow.get(i)));
				
				ll.getLayoutParams().width = itemSizeWidth;
				ll.getLayoutParams().height = itemSizeHeight;
				ll.setGravity(Gravity.CENTER);
				
				final String path = imagesToShow.get(i);
				ll.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(listener != null) listener.onSelectImage(path);
					}
				});
			}
			
		}
		
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				if(position == 0) startCamera();
			}
		}, 200);
		
		return superView;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void onDestroyView() {
		stop_camera();
		super.onDestroyView();
	}
	
	private SurfaceView setCamera(RelativeLayout rl, final int height, final int width) {
		SurfaceView surfaceView = new SurfaceView(getActivity());
		rl.addView(surfaceView);
		
		surfaceView.getLayoutParams().height = height;
		surfaceView.getLayoutParams().width = width;
		
		View viewCamera = new View(getActivity()); 
		viewCamera.setBackgroundResource(R.drawable.camera_icon);
		rl.addView(viewCamera);
		viewCamera.getLayoutParams().height = height / 2;
		viewCamera.getLayoutParams().width = width / 2;
		((LayoutParams)viewCamera.getLayoutParams()).addRule(RelativeLayout.CENTER_IN_PARENT);

		itemHeight = height;
		itemWidth = width;
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(new Callback() {
			
			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {}
			
			@Override
			public void surfaceCreated(SurfaceHolder holder) {}
			
			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
		});
        
        return surfaceView;
        
	}
	
	public void startCamera(){
		start_camera(surfaceHolder, itemHeight, itemWidth);
	}
	
	private void start_camera(SurfaceHolder sf, int height, int width)
    {
        try{
            camera = Camera.open(0);
            
    		Camera.Size camSize = getOptimalPreviewSize(width, height);
            
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewSize(camSize.width, camSize.height);
            camera.setDisplayOrientation(90);
            camera.setParameters(parameters);
        }catch(RuntimeException e){
        	Log.e("LOG", e.toString());
            return;
        }
        
        try {
            camera.setPreviewDisplay(sf);
            camera.startPreview();
        } catch (Exception e) {
        	Log.e("LOG", e.toString());
            return;
        }
    }
	
	private Camera.Size getOptimalPreviewSize(int w, int h) {

		List<Camera.Size> sizes = camera.getParameters().getSupportedPreviewSizes();

		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio = (double) h / w;

		if (sizes == null) {
			return null;
		}

		Camera.Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		for (Camera.Size size : sizes) {

			double ratio = (double) size.width / size.height;

			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;

			if (Math.abs(size.height - targetHeight) < minDiff) {

				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		if (optimalSize == null) {

			minDiff = Double.MAX_VALUE;

			for (Camera.Size size : sizes) {

				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

    private void stop_camera()
    {
        if(camera != null) {
        	camera.stopPreview();
        	camera.release();
        	camera = null;
        }
    }
	
}
