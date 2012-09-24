package fi.android.spacify.activity.bubblespace;

import java.util.Stack;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import fi.android.service.WorkService;
import fi.android.spacify.R;
import fi.android.spacify.activity.BaseFragment;
import fi.android.spacify.gesture.GestureInterface;
import fi.android.spacify.model.Bubble;
import fi.android.spacify.service.ContentManagementService;
import fi.android.spacify.view.BubbleSurface;
import fi.android.spacify.view.BubbleSurface.BubbleEvents;
import fi.spacify.android.util.Events;

/**
 * Activity to show bubbles.
 * 
 * @author Tommy
 *
 */
public class MeBubbleSpaceFragment extends BaseFragment implements ControlCallback {
	
	private Vibrator vibrator;
	private final int VIBRATION_TIME = 200;
	private final ContentManagementService cms = ContentManagementService.getInstance();
	private final WorkService ws = WorkService.getInstance();

	private BubbleSurface bSurface;
	private PopupControlFragment controlPopup;
	private ViewPager popupPager;
	private PopupFragmentAdapter popupAdapter;

	private final Stack<Fragment> visibleFragments = new Stack<Fragment>();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

		View content = inflater.inflate(R.layout.bubble_space, container, false);
		
		popupPager = (ViewPager) content.findViewById(R.id.bubblespace_viewflipper);
		popupAdapter = new PopupFragmentAdapter(getActivity().getSupportFragmentManager());
		popupPager.setAdapter(popupAdapter);
		bSurface = (BubbleSurface) content.findViewById(R.id.bubblespace_surface);
		bSurface.setGesture(BubbleEvents.LONG_CLICK, onLongClick);
		bSurface.setGesture(BubbleEvents.DOUBLE_CLICK, onDoubleClick);
		bSurface.setGesture(BubbleEvents.SINGLE_TOUCH, onSingleTouch);
		
		controlPopup = new PopupControlFragment();
		controlPopup.setCallback(this);
		cms.fetchBubbles();

		return content;
	}

	private final GestureInterface<Bubble> onDoubleClick = new GestureInterface<Bubble>() {

		@Override
		public void onGestureDetected(Bubble b, MotionEvent ev) {
			if(b != null) {
				if(b.getStyle().contains("web")) {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(b
							.getContentImageUrl()));
					startActivity(browserIntent);

				} else {
					BaseBubblePopupFragment bubblePopup = new BaseBubblePopupFragment();
					bubblePopup.setBubble(b);

					// visibleFragments.add(bubblePopup);
					if(popupAdapter.addPopup(bubblePopup)) {
						vibrator.vibrate(VIBRATION_TIME);
						popupAdapter.notifyDataSetChanged();
						popupPager.setVisibility(View.VISIBLE);
						popupPager.bringToFront();
						popupPager.clearDisappearingChildren();

						int position = popupAdapter.getCount() - 1;
						if(position >= 0) {
							popupPager.setCurrentItem(position);
						}

						bSurface.pushBubblesVertically((int) (getResources().getDimension(
								R.dimen.popup_height) + b.radius));
					}
				}
			}
		}
	};

	private final GestureInterface<Bubble> onSingleTouch = new GestureInterface<Bubble>() {

		@Override
		public void onGestureDetected(Bubble b, MotionEvent ev) {
			if(b != null) {
				if(bSurface.hasChildsVisible(b)) {
					bSurface.removeChildren(b);
				} else {
					for(Bubble bubble : cms.getBubbles(b.getLinks())) {
						bSurface.addBubble(bubble);
					}
				}
			} else {
				tryClosingPopups();
			}
		}
	};

	private boolean tryClosingFragments() {
		if(tryClosingPopups()) {
			return true;
		} else if(visibleFragments.size() > 0) {
			FragmentManager fm = getActivity().getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			ft.remove(visibleFragments.pop());
			ft.commit();
			return true;
		}

		return false;
	}

	private boolean tryClosingPopups() {
		if(popupAdapter.getCount() > 0) {
			popupAdapter.removePopup(popupPager.getCurrentItem());
			popupAdapter.notifyDataSetChanged();
			popupPager.clearDisappearingChildren();
			if(popupAdapter.getCount() == 0) {
				popupAdapter.clear();
				popupPager.setVisibility(View.GONE);
			}
			return true;
		}
		return false;
	}

	private final GestureInterface<Bubble> onLongClick = new GestureInterface<Bubble>() {
		
		@Override
		public void onGestureDetected(Bubble b, MotionEvent ev) {
			controlPopup.setBubble(b);
			vibrator.vibrate(VIBRATION_TIME);
			FragmentManager fm = getActivity().getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			if(b != null) {
				ft.replace(R.id.popup_controls, controlPopup);
				visibleFragments.add(controlPopup);
			} else {
				ft.replace(R.id.popup_controls, controlPopup);
				visibleFragments.add(controlPopup);
			}
			ft.commit();
		}
	};
	
	public boolean onBackPressed() {
		if(tryClosingFragments()) {
			return true;
		} else {
			return false;
		}
	};

	public void updateBubbles() {
		ws.postWork(new Runnable() {

			@Override
			public void run() {
				synchronized(MeBubbleSpaceFragment.this) {
					for(Bubble b : cms.getTopLevelBubbles()) {
						bSurface.addBubble(b);
					}
				}
			}
		});
	}

	@Override
	public boolean handleMessage(Message msg) {


		switch (Events.values()[msg.what]) {
			case COMICS_UPDATED:
			case ALL_BUBBLES_FETCHED:
				updateBubbles();
				return true;
			default:
				break;
		}

		return super.handleMessage(msg);
	}
	
	/**
	 * Popup hider onClick.
	 * 
	 * @param view
	 */
	public void onHiderClick(View view) {
		onBackPressed();
	}

	/**
	 * Bring clicked view to front.
	 * 
	 * @param view
	 */
	public void onBringToFrontClick(View view) {
		view.bringToFront();
	}

	public void pauseSurface() {
		bSurface.stopThreads();
	}

	@Override
	public void remove(Bubble b) {
		bSurface.removeBubble(b);
	}

}
