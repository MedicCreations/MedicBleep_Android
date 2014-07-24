package com.clover.spika.enterprise.chat.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;

public class AnimUtils {

    public static void fadeAnim(View view, float from, float to, int duration) {
        ObjectAnimator fadeObjectAnim = ObjectAnimator.ofFloat(view, "alpha", from, to);
        fadeObjectAnim.setDuration(duration);
        fadeObjectAnim.start();
    }

    private static int counter;
    public static void blinkView(View view, int singleDuration, final int times, final AnimatorListenerAdapter listener){
    	counter=1;
    	ObjectAnimator firstFade=ObjectAnimator.ofFloat(view, "alpha", 1.0f, 0.0f).setDuration(singleDuration);
    	ObjectAnimator secondFade=ObjectAnimator.ofFloat(view, "alpha", 0.0f, 1.0f).setDuration(singleDuration);
    	
    	final AnimatorSet animatorSet = new AnimatorSet();
    	
    	animatorSet.addListener(new AnimatorListenerAdapter() {
    		
    		@Override
    		public void onAnimationEnd(Animator animation) {
    			if(counter==times-1){
    				animatorSet.start();
    				animatorSet.addListener(listener);
    			}else if(counter<times){
    				animatorSet.start();
    			}
    			counter++;
    			super.onAnimationEnd(animation);
    		}
    		
		});
    	
    	animatorSet.play(firstFade).before(secondFade);
    	animatorSet.start();
    	
    }

    public static AnimatorSet fadeInOutLoopAnim(final View view, float from, float to, int duration) {
        ObjectAnimator fadeOutAnim = ObjectAnimator.ofFloat(view, "alpha", from, to).setDuration(duration);
        ObjectAnimator fadeInAnim = ObjectAnimator.ofFloat(view, "alpha", to, from).setDuration(duration);

        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if(view.isShown()){
                	animatorSet.start();
                }
            }
        });
        animatorSet.play(fadeInAnim).before(fadeOutAnim);
        animatorSet.start();
        
        return animatorSet;
    }
    
    public static void bouncingYAxisAnim(final View view, float from, float to, int duration, float k) {
    	
    	AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
    	
        ObjectAnimator translationDown = ObjectAnimator.ofFloat(view, "translationY", from*k, to*k).setDuration(duration);
        translationDown.setInterpolator(interpolator);
        ObjectAnimator translationUp = ObjectAnimator.ofFloat(view, "translationY", to*k, from*k).setDuration(duration);
        translationDown.setInterpolator(interpolator);

        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (view.isShown()) {
                    animatorSet.start();
                }
            }
        });
        animatorSet.play(translationDown).before(translationUp);
        animatorSet.start();
    }

    public static AnimatorSet translationY(View view, float from, float to, int duration, AnimatorListenerAdapter listener) {
        ObjectAnimator translationY = ObjectAnimator.ofFloat(view, "translationY", from, to);
        translationY.setDuration(duration);

        AnimatorSet animatorSet = new AnimatorSet();
        if (listener != null) {
            animatorSet.addListener(listener);
        }
        animatorSet.play(translationY);
        animatorSet.start();
        
        return animatorSet;
    }

    public static void translationX(View view, float from, float to, int duration, AnimatorListenerAdapter listener) {
        ObjectAnimator translationX = ObjectAnimator.ofFloat(view, "translationX", from, to);
        translationX.setDuration(duration);

        AnimatorSet animatorSet = new AnimatorSet();
        if (listener != null) {
            animatorSet.addListener(listener);
        }
        animatorSet.play(translationX);
        animatorSet.start();
    }

    public static void rotation(View view, float from, float to, int duration) {
        ObjectAnimator rotation = ObjectAnimator.ofFloat(view, "rotation", from, to);
        rotation.setDuration(duration);
        rotation.start();
    }

    public static void rotationInfinite(View view, boolean clockwise, int cycleDuration) {
        ObjectAnimator rotation = ObjectAnimator.ofFloat(view, "rotation", 0, clockwise ? 360 : -360);
        rotation.setDuration(cycleDuration);
        rotation.setRepeatMode(Animation.RESTART);
        rotation.setRepeatCount(Animation.INFINITE);
        rotation.setInterpolator(new LinearInterpolator());
        rotation.start();
    }

    public static void relativeRotation(final View view, final float addRotation, int duration) {
        final float viewRotation = view.getRotation();
        final float resultRotation = viewRotation + addRotation;
        ObjectAnimator rotation = ObjectAnimator.ofFloat(view, "rotation", viewRotation, resultRotation);
        rotation.setDuration(duration);
        rotation.start();
    }

    public static void scaleX(final View view, final float from, final float to, final int duration,
    							AnimatorListenerAdapter listener) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", from, to);
        scaleX.setDuration(duration);
        
        AnimatorSet animatorSet = new AnimatorSet();
        if (listener != null) {
            animatorSet.addListener(listener);
        }

        animatorSet.play(scaleX);
        animatorSet.start();
    }

    public static void scaleY(final View view, final float from, final float to, final int duration,
    		 					AnimatorListenerAdapter listener) {
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", from, to);
        scaleY.setDuration(duration);
        
        AnimatorSet animatorSet = new AnimatorSet();
        if (listener != null) {
            animatorSet.addListener(listener);
        }

        animatorSet.play(scaleY);
        animatorSet.start();
    }

    public static void scaleShape(final View view, final float from, final float to, final int duration,
                                  AnimatorListenerAdapter listener) {
        scaleX(view, from, to, duration, listener);
        scaleY(view, from, to, duration, listener);
    }
    
    public static AnimatorSet goToLeftThenToRightAndBackInPosition(final View view, final int offset,
    								final int duration, AnimatorListenerAdapter listener){
    	ObjectAnimator goLeft = ObjectAnimator.ofFloat(view, "translationX", 0, offset).setDuration(duration/2);
    	ObjectAnimator goRight = ObjectAnimator.ofFloat(view, "translationX", offset, -offset).setDuration(duration);
    	ObjectAnimator backInPlace = ObjectAnimator.ofFloat(view, "translationX", -offset, 0).setDuration(duration/2);
    	backInPlace.setStartDelay(duration);
    	
    	AnimatorSet set = new AnimatorSet();
    	
    	set.play(goLeft).before(goRight).before(backInPlace);
    	set.start();
    	
    	if (listener != null) set.addListener(listener);
    	
    	return set;
    }
    
}
