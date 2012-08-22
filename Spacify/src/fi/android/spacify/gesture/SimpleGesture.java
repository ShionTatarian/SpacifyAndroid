package fi.android.spacify.gesture;

import android.view.MotionEvent;

public abstract class SimpleGesture<T> {

	/**
	 * 
	 * 
	 * @param object
	 */
	public abstract void onTouchDown(T object, MotionEvent event);
	
	/**
	 * On touch up. Compares passed object with the original and that onTouchDown 
	 * event has not happened more than given touch delay before this event.
	 * 
	 * @param object
	 */
	public abstract void onTouchUp(T object, MotionEvent event);
	
}
