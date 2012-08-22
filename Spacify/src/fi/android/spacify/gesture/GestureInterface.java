package fi.android.spacify.gesture;

import android.view.MotionEvent;

public interface GestureInterface<T> {

	public void onGestureDetected(T item, MotionEvent event);
	
}
