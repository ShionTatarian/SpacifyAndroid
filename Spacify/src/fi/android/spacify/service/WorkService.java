package fi.android.spacify.service;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class WorkService {

	private static final String TAG = "WorkService";
	private static WorkService instance;
	private static int threadCount;
	private int nextPost = 0;
	
	private static Handler[] handlers;

	private WorkService() {
		
	}
	
	public static WorkService getInstance() {
		if (instance == null) {
			throw new IllegalStateException(TAG + " not initialized!");
		}
		return instance;
	}

	public static void init(int count) {
		if(instance != null) {
			throw new IllegalStateException(TAG +" has already been initialized!");
		}
		
		threadCount = count;
		handlers = new Handler[threadCount];
		for (int i = 0; i < threadCount; i++) {
			final int number = i;
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						Looper.prepare();

						// now, the handler will automatically bind to the
						// Looper that is attached to the current thread
						// You don't need to specify the Looper explicitly
						handlers[number] = new Handler();

						// After the following line the thread will start
						// running the message loop and will not normally
						// exit the loop unless a problem happens or you
						// quit() the looper (see below)
						Looper.loop();
					} catch (Throwable t) {
						Log.e(TAG, "halted due to an error", t);
					}
				}
			}).start();
		}

		instance = new WorkService();
	}
	
	public void postWork(Runnable r) {
		handlers[nextPost].post(r);
		next();
	}

	public void postWork(Runnable r, int delay) {
		handlers[nextPost].postDelayed(r, delay);
		next();
	}

	public void postWork(int thread, Runnable r) {
		if (thread >= 0 && thread < threadCount) {
			handlers[thread].post(r);
		} else {
			postWork(r);
		}
	}

	public void postWork(int thread, Runnable r, int delay) {
		if (thread >= 0 && thread < threadCount) {
			handlers[thread].postDelayed(r, delay);
		} else {
			postWork(r, delay);
		}
	}

	private void next() {
		nextPost += 1;
		if(nextPost == threadCount) {
			nextPost = 0;
		}
	}

}
