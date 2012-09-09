package fi.android.spacify.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import fi.android.service.WorkService;
import fi.android.spacify.gesture.GestureInterface;
import fi.android.spacify.gesture.LongClickGesture;
import fi.android.spacify.gesture.SimpleTouchGesture;
import fi.android.spacify.model.Bubble;
import fi.android.spacify.model.Bubble.BubbleMovement;

/**
 * 
 * 
 * @author Tommy
 * 
 */
public class BubbleSurface extends SurfaceView implements SurfaceHolder.Callback {

	private final String TAG = "BubbleSurface";
	private final WorkService ws = WorkService.getInstance();

	/**
	 * Maximum refresh rate is 30 frames per second.
	 */
	private final int MAX_REFRESH_RATE = 1000 / 60;

	private final int DOUBLE_TAP_INTERVAL = 500;

	private GraphicThread graphicThread;
	private final HashMap<Integer, SimpleTouchGesture<Bubble>> gestureList = new HashMap<Integer, SimpleTouchGesture<Bubble>>();
	private final HashMap<String, GestureInterface<Bubble>> gestureMap = new HashMap<String, GestureInterface<Bubble>>();

	private int maxX = 0;
	private int minX = 0;
	private int maxY = 0;
	private int minY = 0;

	private List<Bubble> bubbles = new ArrayList<Bubble>();
	private final HashMap<Integer, Bubble> movingBubbles = new HashMap<Integer, Bubble>();

	private boolean changingLists = false;
	private boolean hitDetection = false;
	private boolean drawing = false;
	private boolean movementChange = false;

	/**
	 * Events supported by BubbleSurface.
	 * 
	 * @author Tommy
	 * 
	 */
	public static class BubbleEvents {
		/**
		 * Touch a single Bubble. This is launched on action up.
		 */
		public static final String SINGLE_TOUCH = "singleTouch";

		/**
		 * Long clicking a bubble.
		 */
		public static final String LONG_CLICK = "longClick";

		/**
		 * Double clicking a bubble.
		 */
		public static final String DOUBLE_CLICK = "doubleClick";
	}

	/**
	 * Constructor.
	 * 
	 * @param context
	 * @param attrs
	 */
	public BubbleSurface(Context context, AttributeSet attrs) {
		super(context, attrs);

		getHolder().addCallback(this);
		getHolder().setFormat(PixelFormat.RGBA_8888);

	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		clearCanvas(canvas);

		if(changingLists) {
			return;
		}
		drawing = true;
		for(Bubble b : bubbles) {
			b.onDraw(canvas);
		}
		drawing = false;
	}

	private void calculateSize(Canvas c) {
		maxX = c.getWidth();
		maxY = c.getHeight();
	}

	private void clearCanvas(Canvas c) {
		c.drawColor(Color.BLACK);
	}

	/**
	 * Start movement and graphic threads if they have been stopped.
	 */
	public void startThreads() {
		startGraphics();
	}

	private void startGraphics() {
		if(graphicThread == null) {
			graphicThread = new GraphicThread(getHolder(), this);
			graphicThread.setRunning(true);
			graphicThread.start();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {

		holder.setFormat(PixelFormat.RGBA_8888);
		Canvas c = holder.lockCanvas();
		calculateSize(c);
		holder.unlockCanvasAndPost(c);

		startGraphics();
	}

	/**
	 * Stop graphic and movement threads. Use this when putting the activity to
	 * pause.
	 */
	public void stopThreads() {
		stopGraphics();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		stopThreads();
	}

	private void stopGraphics() {
		if(graphicThread != null) {
			graphicThread.setRunning(false);
			graphicThread = null;
		}
	}

	class GraphicThread extends Thread {
		private final SurfaceHolder holder;
		private final BubbleSurface surface;
		private long lastUpdate = 0;

		private boolean run = false;

		public GraphicThread(SurfaceHolder surfaceHolder, BubbleSurface surface) {
			holder = surfaceHolder;
			this.surface = surface;
		}

		public void setRunning(boolean run) {
			this.run = run;
		}

		public SurfaceHolder getSurfaceHolder() {
			return holder;
		}

		@Override
		public void run() {
			Canvas c;
			while(run) {
				c = null;
				try {
					if(lastUpdate + MAX_REFRESH_RATE <= System.currentTimeMillis()) {
						if(holder != null) {
							c = holder.lockCanvas(null);
							if(c != null && surface != null) {
								surface.onDraw(c);
								lastUpdate = System.currentTimeMillis();
							}
						}
					}
				} catch(Exception e) {
					Log.e(TAG, "Error drawing!", e);
				} finally {
					// make sure to always release canvas
					if(c != null && holder != null) {
						holder.unlockCanvasAndPost(c);
					}
				}
			}
		}
	}

	private SurfaceTouchInterface callback;

	/**
	 * Add {@link SurfaceTouchInterface} callback to this {@link BubbleSurface}.
	 * 
	 * @param callback
	 */
	public void addCallback(SurfaceTouchInterface callback) {
		this.callback = callback;
	}

	/**
	 * Set Gesture for event. Event corresponds to {@link BubbleEvents}.
	 * 
	 * @param event
	 * 
	 * @param gesture
	 */
	public void setGesture(String event, GestureInterface<Bubble> gesture) {
		gestureMap.put(event, gesture);
	}

	private LongClickGesture<Bubble> longClickGesture;

	private long doubleClickFirstDown = 0;
	private int doubleClickFirstClickID = -1;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(callback != null) {
			callback.onSurfaceTouch(event);
		}
		@SuppressWarnings("deprecation")
		int pointerIndex = ((event.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT);
		// int pointerID = event.getPointerId(pointerIndex);
		int action = (event.getAction() & MotionEvent.ACTION_MASK);

		switch (action) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
				if(longClickGesture != null) {
					longClickGesture.cancel();
					longClickGesture = null;
				}

				Bubble bHit = hitBubble((int) event.getX(pointerIndex),
						(int) event.getY(pointerIndex));
				GestureInterface<Bubble> longClick = gestureMap.get(BubbleEvents.LONG_CLICK);
				if(bHit != null) {
					bHit.setTouchOffset(((int) event.getX(pointerIndex)),
							(int) event.getY(pointerIndex));
					GestureInterface<Bubble> doubleClick = gestureMap
							.get(BubbleEvents.DOUBLE_CLICK);
					if(doubleClick != null) {
						if(bHit.getID() == doubleClickFirstClickID
								&& doubleClickFirstDown + DOUBLE_TAP_INTERVAL >= System
										.currentTimeMillis()) {
							doubleClick.onGestureDetected(bHit, event);

							doubleClickFirstDown = 0;
							doubleClickFirstClickID = -1;
						} else {
							doubleClickFirstDown = System.currentTimeMillis();
							doubleClickFirstClickID = bHit.getID();
						}
					}
					bHit.movement = BubbleMovement.MOVING;

					synchronized(movingBubbles) {
						movingBubbles.put(pointerIndex, bHit);
					}
				} else {
					// Bubble b = new Bubble(id);
					// id += 1;
					// b.x = x;
					// b.y = y;
					//
					// bubbles.add(b);
				}
				GestureInterface<Bubble> singleTouch = gestureMap.get(BubbleEvents.SINGLE_TOUCH);
				if(singleTouch != null) {
					SimpleTouchGesture<Bubble> gesture = new SimpleTouchGesture<Bubble>(singleTouch);
					gesture.onTouchDown(bHit, event);
					gestureList.put(pointerIndex, gesture);
				}

				if(longClick != null) {
					longClickGesture = new LongClickGesture<Bubble>(longClick);
					longClickGesture.onTouchDown(bHit, event);
				}
				break;
			case MotionEvent.ACTION_MOVE:
				synchronized(movingBubbles) {
					for(Integer key : movingBubbles.keySet()) {
						Bubble b = movingBubbles.get(key);
						if(b != null && key < event.getPointerCount()) {
							b.x = (int) event.getX(key) + b.offsetX;
							b.y = (int) event.getY(key) + b.offsetY;

							if(longClickGesture != null) {
								longClickGesture.onMove(b, event);
							}

							moving(b);
						}
					}
				}

				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				if(longClickGesture != null) {
					longClickGesture.cancel();
				}

				final SimpleTouchGesture<Bubble> gesture = gestureList.remove(pointerIndex);
				final Bubble b;
				synchronized(movingBubbles) {
					b = movingBubbles.remove(pointerIndex);
				}
				if(gesture != null) {
					gesture.onTouchUp(b, event);
				}
				if(b != null) {
					b.movement = BubbleMovement.INERT;
				}

				if(event.getPointerCount() == 1) {
					synchronized(movingBubbles) {
						movingBubbles.clear();
					}

					ws.postWork(new Runnable() {

						@Override
						public void run() {
							if(changingLists) {
								return;
							}
							movementChange = true;
							for(Bubble b : bubbles) {
								b.movement = BubbleMovement.INERT;
							}
							movementChange = false;
						}
					});
				}
				break;
		}

		return true;
	}

	private void moving(final Bubble bubble) {
		if(changingLists) {
			return;
		}
		ws.postWork(new Runnable() {

			@Override
			public void run() {
				for(Bubble b : bubbles) {
					if(changingLists) {
						return;
					}

					if(b.movement != BubbleMovement.MOVING && isCollision(bubble, b)) {
						autoMove(bubble, b);
					}
				}
			}
		});
	}

	private void autoMove(Bubble moving, Bubble pushed) {
		double distance = distance(moving, pushed);

		if(distance == 0
				|| moving.movement == BubbleMovement.INERT
				|| (moving.movement == BubbleMovement.MOVING && pushed.movement == BubbleMovement.MOVING)) {
			return;
		}
		double dx = moving.x - pushed.x;
		double dy = moving.y - pushed.y;
		float radius = pushed.radius > moving.radius ? pushed.radius : moving.radius;
		double move = Math.abs(distance - radius);

		double factor = 1;
		if(dx > 0 && dy > 0) {
			factor = dx < dy ? Math.abs(dx / dy) : 1 - Math.abs(dy / dx);
		} else if(dx > 0 && dy < 0) {
			factor = dx < Math.abs(dy) ? Math.abs(dx / dy) : 1 - Math.abs(dy / dx);
		} else if(dx < 0 && dy > 0) {
			factor = Math.abs(dx) < dy ? Math.abs(dx / dy) : 1 - Math.abs(dy / dx);
		} else {
			factor = dx > dy ? Math.abs(dx / dy) : 1 - Math.abs(dy / dx);
		}


		if(dx > 0) {
			pushed.x -= move * factor;
		} else if(dx < 0) {
			pushed.x += move * factor;
		}

		double rFactor = 1 - factor;
		if(dy > 0) {
			pushed.y -= move * rFactor;
		} else if(dy < 0) {
			pushed.y += move * rFactor;
		}

		// Don't let it go over
		if(pushed.x + pushed.radius > maxX) {
			pushed.x = (int) (maxX - pushed.radius);
		} else if(pushed.x - pushed.radius < minX) {
			pushed.x = minX + (int) pushed.radius;
		}

		if(pushed.y + pushed.radius > maxY) {
			pushed.y = (int) (maxY - pushed.radius);
		} else if(pushed.y - pushed.radius < minY) {
			pushed.y = minY + (int) pushed.radius;
		}

		pushed.movement = BubbleMovement.AUTOMATIC;
		moving(pushed);
	}

	private Bubble hitBubble(int x, int y) {
		if(changingLists) {
			return null;
		}
		hitDetection = true;
		for(int i = bubbles.size() - 1; i > -1; i--) {
			Bubble b = bubbles.get(i);
			if(distance(x, y, b.x, b.y) < b.radius) {
				hitDetection = false;
				return b;
			}
		}
		hitDetection = false;
		return null;
	}

	private boolean isCollision(Bubble b1, Bubble b2) {
		final double a = b1.radius + b2.radius;
		final double dx = b1.x - b2.x;
		final double dy = b1.y - b2.y;
		return a * a > (dx * dx + dy * dy);
	}

	/**
	 * Inform BubbleSurface that this Bubble should be removed.
	 * 
	 * @param removed
	 *            Bubble to be removed
	 */
	public void removeBubble(final Bubble removed) {
		ws.postWork(new Runnable() {
			
			@Override
			public void run() {
				changingLists = true;
				List<Bubble> shorterList = new ArrayList<Bubble>();

				for(Bubble b : bubbles) {
					if(b.getID() != removed.getID()) {
						shorterList.add(b);
					}
				}
				
				while(changingLists) {
					if(!hitDetection && !drawing && !movementChange) {
						bubbles = shorterList;
						changingLists = false;
					} else {
						synchronized(this) {
							try {
								wait(50);
							} catch(InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		});
		
		

	}

	/**
	 * Add bubble to BubbleSurface.
	 * 
	 * @param b
	 */
	public void addBubble(final Bubble b) {
		ws.postWork(new Runnable() {

			@Override
			public void run() {
				b.x = (int) (maxX * Math.random());
				b.y = (int) (maxY * Math.random());

				/*
				 * Check that the bubble is not already in BubbleSpace.
				 */
				for(Bubble bub : bubbles) {
					if(bub.getID() == b.getID()) {
						return;
					}
				}

				while(changingLists) {
					synchronized(this) {
						try {
							wait(100);
						} catch(InterruptedException e) {
							e.printStackTrace();
						}
					}
				}

				bubbles.add(b);
			}
		});
	}

	private double distance(int x, int y, int bx, int by) {
		double dx2 = Math.pow(x - bx, 2);
		double dy2 = Math.pow(y - by, 2);

		return Math.sqrt(dx2 + dy2);
	}

	private double distance(Bubble b1, Bubble b2) {
		return distance(b1.x, b1.y, b2.x, b2.y);
	}

}
