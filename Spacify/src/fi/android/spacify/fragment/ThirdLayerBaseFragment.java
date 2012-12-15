package fi.android.spacify.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import fi.android.spacify.R;
import fi.android.spacify.activity.BubbleActivity;
import fi.android.spacify.activity.VideoActivity;
import fi.android.spacify.animation.ReverseInterpolator;
import fi.android.spacify.service.ContentManagementService;
import fi.android.spacify.view.BaseBubbleView;
import fi.android.spacify.view.BubbleView;
import fi.android.spacify.view.ConnectionLayout;
import fi.android.spacify.view.ThirdLayer;
import fi.spacify.android.util.StaticUtils;

public class ThirdLayerBaseFragment extends BaseFragment implements OnTouchListener {

	private final String TAG = "BubbleFragment";

	private final int UPDATE_DELAY = 300;

	private final ContentManagementService cms = ContentManagementService.getInstance();
	private Map<Integer, BubbleView> list = new HashMap<Integer, BubbleView>();
	private ConnectionLayout frame;
	public int height, width;
	private BubbleView singleTouched;
	private Handler handler;
	private boolean animationInProgress = false;

	private BubbleActivity parentActivity;
	
	private List<ThirdLayer> thirdLayer = new ArrayList<ThirdLayer>();

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		updateConnections();
		handler = new Handler();
		parentActivity = (BubbleActivity) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		frame = (ConnectionLayout) inflater.inflate(R.layout.bubble_frame, container, false);
		frame.setOnTouchListener(this);

		return frame;
	}


	private BaseBubbleView firstTouched = null;
	private double initialD = -1;

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		updateConnections();
		float x = event.getRawX();
		float y = event.getRawY();

		BaseBubbleView bv = null;
		if(v instanceof BaseBubbleView) {
			v.bringToFront();
			bv = (BaseBubbleView) v;
			if(event.getPointerCount() == 1) {
				firstTouched = bv;
			}
		} else {
			onEmptyClick();
			return false;
		}
		
		int action = (event.getAction() & MotionEvent.ACTION_MASK);

		switch (action) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
				if(bv != null) {
					if(bv.onTouchDown() && bv instanceof BubbleView) {
						doubleClick((BubbleView) bv);
					}
					int dx = (int) (v.getLeft() - x + (v.getWidth() / 2));
					int dy = (int) (v.getTop() - y + (v.getHeight() / 2));
					bv.offsetX = dx;
					bv.offsetY = dy;

					if(bv.getID() != firstTouched.getID()) {
						firstTouched = null;
					}
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if(bv != null) {
					bv.move((int) x, (int) y, width, height);
					if(bv instanceof BubbleView) {
						testHit((BubbleView) bv);
					}

				}

				if(firstTouched != null && event.getPointerCount() == 2) {
					float x1 = event.getX(event.getPointerId(0));
					float y1 = event.getY(event.getPointerId(0));
					float x2 = event.getX(event.getPointerId(1));
					float y2 = event.getY(event.getPointerId(1));

					double d = distance(x1, y1, x2, y2);
					if(initialD < 0) {
						initialD = d;
					} else {
						firstTouched.zoom(d / initialD);
					}
				}

				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:

				if(firstTouched != null) {
					firstTouched.endZoom();
					if(event.getPointerCount() == 1) {
						if(firstTouched.moved <= BaseBubbleView.MOVEMENT_TOUCH_TRESHOLD
								&& bv instanceof BubbleView) {
							onSingleTouch((BubbleView) bv);
						}
						firstTouched = null;
					}
				}
				initialD = -1;

				if(bv != null) {
					bv.onTouchUp();
				} else if(firstTouched == null) {
					onEmptyClick();
				}

				break;
		}

		return true;
	}

	private void doubleClick(BubbleView bv) {
		onBubbleViewClick(bv);
	}

	public void onSingleTouch(BubbleView bv) {
		if(bv != null && list.containsKey(bv.getID()) && !bv.asMainContext) {
			return;
		}
	}

	public void onBubbleViewClick(BubbleView bv) {
		if(!animationInProgress) {
			parentActivity.addContext(bv);

			// Random r = new Random();
			// if(hasChildsVisible(bv)) {
			// removeBubbles(bv.getLinks(), bv);
			// checkChildCount();
			// return;
			// }
			//
			// for(final BubbleView nBubble : cms.getBubbles(bv.getLinks())) {
			// if(!list.containsKey(nBubble.getID())) {
			// Log.d(TAG, "New Bubble [" + nBubble.getTitle() + "]");
			// animateBubbleAdd(nBubble, bv);
			// }
			// }
			// updateConnections();
			// checkChildCount();
		}
	}

	private void removeBubbles(List<Integer> links, BubbleView from) {
		for(int id : links) {
			if(from != null) {
				animateBubbleRemove(list.get(id), from);
			} else {
				removeBubble(list.get(id));
			}
		}
	}

	public void removeBubble(BubbleView bubbleView) {
		if(bubbleView != null) {
			frame.removeView(bubbleView);
			list.remove(bubbleView.getID());

			cms.saveBubble(bubbleView);

			updateConnections();
		}
	}

	private void animateBubbleRemove(final BubbleView bv, BubbleView to) {
		list.remove(bv.getID());
		updateConnections();
		Animation anim = new ScaleAnimation(0, 1, 0, 1, Animation.ABSOLUTE,
				(to.x - bv.x + (to.getRadius())), Animation.ABSOLUTE,
				(to.y - bv.y + (to.getRadius())));
		anim.setDuration(StaticUtils.ANIMATION_DURATION);
		anim.setInterpolator(new ReverseInterpolator());

		anim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				removeBubble(bv);
				animationInProgress = false;
				checkChildCount();
			}
		});

		bv.startAnimation(anim);
		animationInProgress = true;
	}

	private void animateBubbleAdd(final BubbleView bv, BubbleView from) {
		if(!list.containsKey(bv.getID())) {
			frame.addView(bv);

			Animation anim = new ScaleAnimation(0, 1, 0, 1, Animation.ABSOLUTE,
					(from.x - bv.x + (from.getRadius())), Animation.ABSOLUTE,
					(from.y - bv.y + (from.getRadius())));
			anim.setDuration(StaticUtils.ANIMATION_DURATION);
			anim.setInterpolator(new LinearInterpolator());

			anim.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					list.put(bv.getID(), bv);
					bv.setOnTouchListener(ThirdLayerBaseFragment.this);
					updateConnections();
					animationInProgress = false;
					checkChildCount();
				}
			});

			bv.startAnimation(anim);
			animationInProgress = true;
		} else {
			Log.w(TAG, "Could not add bubble [" + bv.getTitle() + "], already in view");
		}
	}

	public void addBubble(BubbleView bv) {
		ViewGroup parent = (ViewGroup) bv.getParent();
		if(parent != null) {
			parent.removeView(bv);
		}

		frame.addView(bv);
		bv.setOnTouchListener(ThirdLayerBaseFragment.this);
		list.put(bv.getID(), bv);
		updateConnections();

		checkChildCount();
	}

	private void checkChildCount() {
		for(BubbleView bv : list.values()) {
			int count = 0;
			List<Integer> links = bv.getLinks();
			for(int id : links) {
				if(list.containsKey(id)) {
					count += 1;
				}
			}
			bv.setLinkCount((links.size() - count));
		}
	}

	private void testHit(final BubbleView bv) {
		if(bv.asMainContext) {
			return;
		}
		for(BubbleView b : list.values()) {
			if(!b.asMainContext && isHit(bv, b) && b.getID() != bv.getID()) {
				autoMove(bv, b);
			}
		}
	}

	public static boolean isHit(View b1, View b2) {
		double radius1 = (b1.getWidth() / 2);
		double radius2 = (b2.getWidth() / 2);
		double a = (radius1 + radius2);
		double dx = (b1.getLeft() + radius1) - (b2.getLeft() + radius2);
		double dy = (b1.getTop() + radius1) - (b2.getTop() + radius2);
		return a * a > ((dx * dx) + (dy * dy));
	}

	private void autoMove(final BubbleView bv, BubbleView pushed) {
		int[] movingPos = bv.getViewPosition();
		int[] pushedPos = pushed.getViewPosition();
		double distance = distance(movingPos[0], movingPos[1], pushedPos[0], pushedPos[1]);

		int movingRadius = bv.getRadius();
		int pushedRadius = pushed.getRadius();

		double dx = movingPos[0] - pushedPos[0];
		double dy = movingPos[1] - pushedPos[1];
		float radius = pushedRadius > movingRadius ? pushedRadius : movingRadius;
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

		int x = 0;
		if(dx > 0) {
			x = (int) (pushedPos[0] - (move * factor));
		} else {
			x = (int) (pushedPos[0] + (move * factor));
		}

		int y = 0;
		double rFactor = 1 - factor;
		if(dy > 0) {
			y = (int) (pushedPos[1] - (move * rFactor));
		} else {
			y = (int) (pushedPos[1] + (move * rFactor));
		}

		// Don't let it go over
		if(x + pushedRadius > width) {
			x = (width - pushedRadius);
		} else if(x - pushedRadius < 0) {
			x = pushedRadius;
		}

		if(y + pushedRadius > height) {
			y = (height - pushedRadius);
		} else if(y - pushedRadius < 0) {
			y = pushedRadius;
		}

		pushed.move(x, y, width, height);
	}

	public static double distance(float x, float y, float bx, float by) {
		double dx2 = Math.pow(x - bx, 2);
		double dy2 = Math.pow(y - by, 2);

		return Math.sqrt(dx2 + dy2);
	}

	public void setBubbleCursor(Cursor c, Context context) {
		c.moveToFirst();
		while(!c.isAfterLast()) {
			BubbleView bv = new BubbleView(context, c);
			list.put(bv.getID(), bv);
			c.moveToNext();
		}
		c.close();
		checkChildCount();
		updateConnections();
	}
	
	public boolean hasChildsVisible(BubbleView bv) {
		for(int id : bv.getLinks()) {
			if(id != bv.getID()) {
				BubbleView child = list.get(id);
				if(child == null) {
					return false;
				}
			}
		}
		return true;
	}

	private void updateConnections() {
		int[][] connections = new int[list.size() * list.size()][4];
		int i = 0;
		Iterator<BubbleView> iterator = list.values().iterator();
		while(iterator.hasNext()) {
			BubbleView b1 = iterator.next();
			for(int id : b1.getLinks()) {
				BubbleView b2 = list.get(id);
				if(b2 != null && b1 != null && connections.length > i && !b1.asMainContext
						&& !b2.asMainContext) {
					int[] b1Position = b1.getViewPosition();
					int[] b2Position = b2.getViewPosition();

					connections[i][0] = b1Position[0];
					connections[i][1] = b1Position[1];
					connections[i][2] = b2Position[0];
					connections[i][3] = b2Position[1];
					i++;
				}
			}
		}
		if(frame != null) {
			frame.setConnections(connections);
		}
	}

	private void onEmptyClick() {
		Iterator<ThirdLayer> iterator = thirdLayer.iterator();
		while(iterator.hasNext()) {
			ThirdLayer third = iterator.next();
			frame.removeView(third);
			iterator.remove();
		}
		((BubbleActivity) getActivity()).onEmptyClick();
	}

	public void saveBubbles() {
		cms.saveBubbles(new ArrayList<BubbleView>(list.values()));
	}

	public boolean hasView(BubbleView bv) {
		return list.containsKey(bv.getID());
	}

	public void onImageClick(BubbleView bv) {
		ThirdLayer third = new ThirdLayer(parentActivity);
		third.setOnTouchListener(this);
		frame.addView(third);
		third.onTouchDown();

		Random r = new Random();
		int x = r.nextInt(BubbleActivity.width);
		int y = r.nextInt(BubbleActivity.height);
		if(x < 300) {
			x = 300;
		} else if(x > BubbleActivity.width - 300) {
			x = BubbleActivity.width - 300;
		}

		if(y < 300) {
			y = 300;
		} else if(y > BubbleActivity.height - 300) {
			y = BubbleActivity.height - 300;
		}

		third.move(x, y, width, height);
		third.onTouchUp();

		thirdLayer.add(third);
	}

	public void onPlayClick(BubbleView bv) {
		Intent intent = new Intent(getActivity(), VideoActivity.class);
		startActivity(intent);
	}

	public void onEditClick(BubbleView bv) {
		// TODO Auto-generated method stub

	}

}
