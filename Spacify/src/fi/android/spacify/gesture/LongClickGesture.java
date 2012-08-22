package fi.android.spacify.gesture;

import android.view.MotionEvent;

public class LongClickGesture<T> extends SimpleTouchGesture<T> {

	private final int LONG_CLICK_MOVEMENT = 20;
	private final int LONG_CLICK_TIME = 1000;

	private float downX, downY;
	private long downTime = 0;
	private double distance = 0;
	
	private boolean cancel = false;

	private final Thread longClick = new Thread(new Runnable() {

		@Override
		public void run() {
			while (!cancel && System.currentTimeMillis() < downTime + LONG_CLICK_TIME) {
				synchronized (this) {
					try {
						wait(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			GestureInterface<T> callback = weakInterface.get();
			if (callback != null && !cancel) {
				callback.onGestureDetected(weakObject.get(), null);
			}
		}
	});;

	/**
	 * 
	 * @param gIf
	 */
	public LongClickGesture(GestureInterface<T> gIf) {
		super(gIf);
	}
	
	@Override
	public void onTouchDown(T object, android.view.MotionEvent event) {
		downX = event.getRawX();
		downY = event.getRawY();
		downTime = System.currentTimeMillis();
		
		longClick.start();

		super.onTouchDown(object, event);
	};
	
	public void onMove(T object, MotionEvent event) {
		distance = distance + distance(downX, downY, event.getRawX(), event.getRawY());
		if (downTime + LONG_CLICK_TIME < System.currentTimeMillis() || distance > LONG_CLICK_MOVEMENT) {
			cancel = true;
		} else {
			downX = event.getRawX();
			downY = event.getRawY();
		}

	}
	
	@Override
	public void onTouchUp(T object, android.view.MotionEvent event) {
		cancel = true;
	};

	private double distance(double sx, double sy, double ex, double ey) {
		double dx = Math.pow(sx - ex, 2);
		double dy = Math.pow(sy - ey, 2);

		return Math.sqrt(dx + dy);
		
	}

	public void cancel() {
		cancel = true;
	}

}
