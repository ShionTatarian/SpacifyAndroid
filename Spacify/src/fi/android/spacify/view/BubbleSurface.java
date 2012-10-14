package fi.android.spacify.view;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.Shader.TileMode;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import fi.android.service.WorkService;
import fi.android.spacify.R;
import fi.android.spacify.db.BubbleDatabase;
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
	private Bitmap background;
	public static Bitmap whiteBubble, greenBubble, blueBubble;

	private Paint bgPaint;

	/**
	 * Maximum refresh rate is 30 frames per second.
	 */
	private final int MAX_REFRESH_RATE = 1000 / 60;

	private final int DOUBLE_TAP_INTERVAL = 500;

	private final int ZOOM_DOWN_TO = 60;

	private GraphicThread graphicThread;
	private final HashMap<Integer, SimpleTouchGesture<Bubble>> gestureList = new HashMap<Integer, SimpleTouchGesture<Bubble>>();
	private final HashMap<String, GestureInterface<Bubble>> gestureMap = new HashMap<String, GestureInterface<Bubble>>();

	public static int maxX = 0;
	public static int maxY = 0;
	private int minX = 0;
	private int minY = 0;

	private Map<Integer, Bubble> bubbles = new HashMap<Integer, Bubble>();
	private final HashMap<Integer, Bubble> movingBubbles = new HashMap<Integer, Bubble>();
	private final BubbleDatabase db = BubbleDatabase.getInstance();

	private boolean changingLists = false;
	private boolean hitDetection = false;
	private boolean drawing = false;
	private boolean movementChange = false;
	private Paint linePaint;
	private Handler handler = new Handler();

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
		
		linePaint = new Paint();
		linePaint.setAntiAlias(true);
		linePaint.setColor(Color.GREEN);
		linePaint.setStrokeWidth(context.getResources().getDimension(R.dimen.margin_half));

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		background = BitmapFactory.decodeResource(getResources(), R.drawable.bg, options);
		whiteBubble = BitmapFactory.decodeResource(getResources(), R.drawable.transparentball,
				options);
		greenBubble = BitmapFactory.decodeResource(getResources(), R.drawable.greenball, options);
		blueBubble = BitmapFactory
				.decodeResource(getResources(), R.drawable.lightblueball, options);
		bgPaint = new Paint();
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		clearCanvas(canvas);
		drawTouchedGradient(canvas);

		if(changingLists) {
			return;
		}
		drawing = true;

		Iterator<Bubble> iterator = bubbles.values().iterator();
		while(iterator.hasNext()) {
			Bubble b1 = iterator.next();
			for(int id : b1.getLinks()) {
				Bubble b2 = bubbles.get(id);
				if(b2 != null) {
					if(b1 != null) {
						canvas.drawLine(b1.x, b1.y, b2.x, b2.y, linePaint);
					}
					b2.onDraw(canvas);
				}
			}
			b1.onDraw(canvas);
		}

		drawing = false;
	}

	private final int GRADIENT_HALO = 35;

	private void drawTouchedGradient(Canvas c) {
		for(Bubble b : movingBubbles.values()) {
			RadialGradient gradient = new RadialGradient(b.x, b.y, b.radius + GRADIENT_HALO,
					Color.WHITE, Color.TRANSPARENT, TileMode.CLAMP);
			Paint p = new Paint();
			p.setDither(true);
			p.setShader(gradient);
			c.drawCircle(b.x, b.y, b.radius + GRADIENT_HALO, p);
		}
	}

	private void calculateSize(Canvas c) {
		maxX = c.getWidth();
		maxY = c.getHeight();
	}

	private void clearCanvas(Canvas c) {
		c.drawBitmap(background, c.getMatrix(), null);
	}

	/**
	 * Start movement and graphic threads if they have been stopped.
	 */
	public void startThreads() {
		startGraphics();
	}

	private void startGraphics() {
		handler.removeCallbacks(pauseDrawing);
		if(graphicThread == null) {
			graphicThread = new GraphicThread(getHolder(), this);
			graphicThread.setRunning(true);
			graphicThread.start();
			Log.d(TAG, "Graphics started");
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {

		holder.setFormat(PixelFormat.RGBA_8888);
		Canvas c = holder.lockCanvas();
		if (maxX == 0 || maxY == 0) {
			calculateSize(c);
		}
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
			Log.d(TAG, "Graphics stopped");
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
								calculateSize(c);
								surface.onDraw(c);
								lastUpdate = System.currentTimeMillis();
							}
						}
					} else {
						synchronized(this) {
							sleep(MAX_REFRESH_RATE / 2);
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
	private int zoomPointerIndex = -1;
	private double firstZoomDistance = 0;
	private Bubble zooming = null;

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
				startGraphics();
				if(longClickGesture != null) {
					longClickGesture.cancel();
					longClickGesture = null;
				}

				Bubble bHit = hitBubble((int) event.getX(pointerIndex),
						(int) event.getY(pointerIndex));
				GestureInterface<Bubble> longClick = gestureMap.get(BubbleEvents.LONG_CLICK);
				if(bHit != null && bHit.movement != BubbleMovement.MOVING) {
					bHit.animateOnTouch();
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
				if(event.getPointerCount() == 2) {
					zoomPointerIndex = pointerIndex;
				} else {
					zoomPointerIndex = -1;
					firstZoomDistance = 0;
					zoomBack();
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

							if(zoomPointerIndex != -1 && movingBubbles.size() == 1) {
								// zoom happening
								double d = distance(event.getX(key), event.getY(key),
										event.getX(zoomPointerIndex), event.getY(zoomPointerIndex));
								if(firstZoomDistance == 0) {
									firstZoomDistance = d;
									zooming = b;
								} else {
									b.zoom(d / firstZoomDistance);
								}
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
					b.linkStatusChanged = false;
					b.movement = BubbleMovement.INERT;
					b.animateOnUp();
				}

				if(zoomPointerIndex != -1) {
					zoomPointerIndex = -1;
					firstZoomDistance = 0;
					zoomBack();
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
							for(Bubble b : bubbles.values()) {
								b.movement = BubbleMovement.INERT;
							}
							movementChange = false;
						}
					});

					handler.postDelayed(pauseDrawing, DRAW_PAUSE_DELAY);
				}
				break;
		}

		return true;
	}

	private final int DRAW_PAUSE_DELAY = 3000;

	private Runnable pauseDrawing = new Runnable() {

		@Override
		public void run() {
			stopGraphics();
		}
	};

	private void zoomBack() {
		if(zooming != null) {
			zooming.endZoom();
			zooming = null;
		}

		// if(zooming != null) {
		// zooming.zoom(1);
		// }
		// zooming = null;
	}

	private final double ANIMATION_DURATION = 300;

	/**
	 * Moves all bubbles down.
	 * 
	 * @param to
	 */
	public void pushBubblesVertically(final int to) {
		ws.postWork(new Runnable() {

			@Override
			public void run() {
				long start = System.currentTimeMillis();
				int top = 0;
				while(top < to) {
					long pulse = System.currentTimeMillis() - start;

					top = (int) (to * pulse / ANIMATION_DURATION);

					for(Bubble b : bubbles.values()) {
						if(b.y < top) {
							b.y = top;
						}
					}
				}
			}
		});
	}

	private double distance(float x, float y, float x2, float y2) {
		float dx = (float) (Math.pow(x - x2, 2));
		float dy = (float) (Math.pow(y - y2, 2));

		return Math.sqrt(dx + dy);
	}

	/**
	 * Move bubble and react to collisions.
	 * 
	 * @param bubble
	 */
	private void moving(final Bubble bubble) {
		if(changingLists) {
			return;
		}
		ws.postWork(new Runnable() {

			@Override
			public void run() {
				for(Bubble b : bubbles.values()) {
					if(changingLists) {
						return;
					}

					if(b.getID() != bubble.getID()) {
						if(b.movement != BubbleMovement.MOVING && isCollision(bubble, b)) {
							autoMove(bubble, b);
							b.lockedToPlase = false;
						} else if(b.movement == BubbleMovement.MOVING
								&& bubble.movement == BubbleMovement.MOVING
								&& isCollision(bubble, b)) {
							if(!bubble.linkStatusChanged && !bubble.getLinks().contains(b.getID())) {
								Log.d(TAG, "created link");
								bubble.linkStatusChanged = true;
								b.linkStatusChanged = true;
								b.addLink(bubble.getID());
								bubble.addLink(b.getID());

								db.storeBubble(bubble);
								db.storeBubble(b);
							} else if(!bubble.linkStatusChanged) {
								Log.d(TAG, "removed link");
								bubble.linkStatusChanged = true;
								b.linkStatusChanged = true;
								b.removeLink(bubble.getID());
								bubble.removeLink(b.getID());

								db.storeBubble(bubble);
								db.storeBubble(b);
							}
						}
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
		for(Bubble b : bubbles.values()) {
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
		removeBubble(removed.getID());
	}

	public void clear() {
		stopThreads();
		bubbles.clear();
	}

	/**
	 * Inform BubbleSurface that this Bubble should be removed.
	 * 
	 * @param removed
	 *            Bubble to be removed
	 */
	public void removeBubble(final int removed) {
		ws.postWork(new Runnable() {

			@Override
			public void run() {
				changingLists = true;
				while(changingLists) {
					if(!hitDetection && !drawing && !movementChange) {
						synchronized (bubbles) {
							bubbles.remove(removed);
						}
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
				startGraphics();
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
				/*
				 * Check that the bubble is not already in BubbleSpace.
				 */
				if(bubbles.containsKey(b.getID())) {
					return;
				}

				if(b.x == 0 && b.y == 0) {
					if(maxX != 0 || maxY != 0) {
						b.x = (int) (maxX * Math.random());
						b.y = (int) (maxY * Math.random());
					} else {
						b.x = (int) (400 * Math.random());
						b.y = (int) (400 * Math.random());
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

				synchronized (bubbles) {
					bubbles.put(b.getID(), b);
				}
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

	/**
	 * Returns true if all children are visible. Else false.
	 * 
	 * @param b
	 * @return
	 */
	public boolean hasChildsVisible(Bubble b) {
		for(int id : b.getLinks()) {
			if(id != b.getID()) {
				Bubble child = bubbles.get(id);
				if(child == null) {
					return false;
				}
			}
		}
		return true;
	}

	public void removeChildren(Bubble b) {
		for(int id : b.getLinks()) {
			Bubble child = bubbles.get(id);
			if (child != null && id != b.getID()) {
				removeBubble(id);
			}
		}
	}

	public void moveAllButTheseToCorner(List<Integer> links) {
		synchronized (bubbles) {
			for (Bubble b : bubbles.values()) {
				if (!links.contains(b.getID()) && !b.lockedToPlase) {
					b.lockedToPlase = true;
					int[] array = getFreeSidePosition();
					b.moveAndScaleTo(array[0], array[1], ZOOM_DOWN_TO);
				}
			}
		}
	}

	private int[] getFreeSidePosition() {
		int[] array = new int[2];
		// default position is top right corner if no other pace found
		array[0] = maxX - ZOOM_DOWN_TO;
		array[1] = ZOOM_DOWN_TO;

		for (int x = 1; x < (maxX / ZOOM_DOWN_TO); x++) {
			if (hitBubble(x * ZOOM_DOWN_TO, ZOOM_DOWN_TO / 2) == null) {
				array[0] = x * ZOOM_DOWN_TO;
				array[1] = ZOOM_DOWN_TO;
				return array;
			}
		}

		for (int y = 1; y < (maxY / ZOOM_DOWN_TO); y++) {
			if (hitBubble(y * ZOOM_DOWN_TO, ZOOM_DOWN_TO / 2) == null) {
				array[0] = maxX - ZOOM_DOWN_TO;
				array[1] = y * ZOOM_DOWN_TO;
				return array;
			}
		}

		return array;
	}

}
