package fi.android.spacify.activity.bubblespace;

import java.util.Stack;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ViewFlipper;
import fi.android.spacify.R;
import fi.android.spacify.activity.BaseActivity;
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
public class BubbleSpaceActivity extends BaseActivity {
	
	private Vibrator vibrator;
	private final int VIBRATION_TIME = 200;
	private final ContentManagementService cms = ContentManagementService.getInstance();

	private BubbleSurface bSurface;
	private PopupControlFragment controlPopup;
	private ViewFlipper viewFlipper;

	private final Stack<Fragment> visibleFragments = new Stack<Fragment>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		setContentView(R.layout.bubble_space);
		
		viewFlipper = (ViewFlipper) findViewById(R.id.bubblespace_viewflipper);
		bSurface = (BubbleSurface) findViewById(R.id.bubblespace_surface);
		bSurface.setGesture(BubbleEvents.LONG_CLICK, longClick);
		bSurface.setGesture(BubbleEvents.DOUBLE_CLICK, openBubblePopup);
		
		controlPopup = new PopupControlFragment();
		cms.fetchBubbles();
	}

	private final GestureInterface<Bubble> openBubblePopup = new GestureInterface<Bubble>() {

		@Override
		public void onGestureDetected(Bubble b, MotionEvent ev) {
			if(b != null) {
				vibrator.vibrate(VIBRATION_TIME);

				BaseBubblePopupFragment bubblePopup = new BaseBubblePopupFragment();
				bubblePopup.setBubble(b);

				// visibleFragments.add(bubblePopup);
			}
		}
	};


	private final GestureInterface<Bubble> longClick = new GestureInterface<Bubble>() {
		
		@Override
		public void onGestureDetected(Bubble b, MotionEvent ev) {
			if(b != null) {
				vibrator.vibrate(VIBRATION_TIME);

				FragmentManager fm = getSupportFragmentManager();
				FragmentTransaction ft = fm.beginTransaction();
				ft.replace(R.id.popup_controls, controlPopup);
				ft.commit();
				
				visibleFragments.add(controlPopup);
			} else {
				vibrator.vibrate(VIBRATION_TIME);

				FragmentManager fm = getSupportFragmentManager();
				FragmentTransaction ft = fm.beginTransaction();
				ft.replace(R.id.popup_controls, controlPopup);
				ft.commit();

				visibleFragments.add(controlPopup);
			}
		}
	};
	
	@Override
	public void onBackPressed() {
		if(visibleFragments.size() > 0) {
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			Fragment f = visibleFragments.pop();
			ft.remove(f);
			ft.commit();
		} else {
			super.onBackPressed();
		}
	};

	@Override
	public boolean handleMessage(Message msg) {


		switch (Events.values()[msg.what]) {
			case ALL_BUBBLES_FETCHED:
				for(Bubble b : cms.getBubbles()) {
					bSurface.addBubble(b);
				}
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

}
