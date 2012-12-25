package fi.android.spacify.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;
import fi.android.spacify.R;
import fi.android.spacify.activity.BubbleActivity;
import fi.android.spacify.view.BubbleView;
import fi.spacify.android.util.StaticUtils;

public class HistoryLayerFragment extends BaseFragment implements OnTouchListener {

	private final String TAG = "HistoryLayer";

	private View frame;
	public int height, width;

	private BubbleActivity parentActivity;
	private TextView previous, animating;
	
	private int historySize;

	private LayoutParams animatingParams, previousParams;

	private List<BubbleView> history = new ArrayList<BubbleView>();

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		parentActivity = (BubbleActivity) activity;
		historySize = (int) getResources().getDimension(R.dimen.history_bubble);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		frame = inflater.inflate(R.layout.history_layer, container, false);

		previous = (TextView) frame.findViewById(R.id.history_previous);
		previousParams = (LayoutParams) previous.getLayoutParams();
		animating = (TextView) frame.findViewById(R.id.history_animating_view);
		animating.setOnTouchListener(this);
		animatingParams = (LayoutParams) animating.getLayoutParams();

		reset();

		return frame;
	}


	private void reset() {
		if(history.isEmpty()) {
			previous.setVisibility(View.GONE);
			animating.setVisibility(View.GONE);
		} else {
			BubbleView last = history.get(history.size() - 1);
			BubbleView prev = null;

			if(history.size() > 1) {
				prev = history.get(history.size() - 2);
			}

			animating.setVisibility(View.VISIBLE);

			previousParams.topMargin = BubbleActivity.height - (historySize);
			animatingParams.topMargin = BubbleActivity.height - (historySize);

			previousParams.leftMargin = (BubbleActivity.width / 2) - (historySize / 2);
			animatingParams.leftMargin = (BubbleActivity.width / 2) - (historySize / 2);

			if(prev != null) {
				previous.setVisibility(View.VISIBLE);
				previous.setText(prev.getTitle());
			} else {
				previous.setVisibility(View.GONE);
			}
			animating.setText(last.getTitle());
		}
	}

	private int offsetX = 0;
	private int offsetY = 0;

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		float x = event.getRawX();
		float y = event.getRawY();

		int action = (event.getAction() & MotionEvent.ACTION_MASK);

		switch (action) {
			case MotionEvent.ACTION_DOWN:
				touchDown(x, y);
				break;
			case MotionEvent.ACTION_MOVE:
				move(x, y);
				break;
			case MotionEvent.ACTION_UP:
				touchUp();
				break;
		}

		return true;
	}

	private void touchUp() {
		int[] pos = getViewPosition(animating);
		
		double disMiddle = BubbleFragment.distance(pos[0], pos[1], BubbleActivity.width/2, BubbleActivity.height/2);
		double disBottom = BubbleFragment.distance(pos[0], pos[1], BubbleActivity.width, BubbleActivity.height);
		
		if(disMiddle > disBottom) {
			// animate to
		} else {
			// animate to center
			float pivotX = ((float) pos[0] / (float) BubbleActivity.width);
			float pivotY = ((float) pos[1] / (float) BubbleActivity.height);

			Animation anim = new ScaleAnimation(0.5f, 1, 0.5f, 1, 
					Animation.RELATIVE_TO_PARENT, pivotX,
					Animation.RELATIVE_TO_PARENT, pivotY);
			anim.setDuration(StaticUtils.ANIMATION_DURATION);
			BubbleView bv = history.get(history.size() - 1);
			history.remove(history.size() - 1);
			reset();
			parentActivity.setTierZero(bv, anim, false);
		}
		
		
	}

	private void move(float x, float y) {
		animatingParams.topMargin = (int) (y + offsetY - (historySize / 2));
		animatingParams.leftMargin = (int) (x + offsetX - (historySize / 2));
		animating.setLayoutParams(animatingParams);
	}

	private void touchDown(float x, float y) {
		int[] pos = getViewPosition(animating);
		offsetX = (int) (pos[0] - x);
		offsetY = (int) (pos[1] - y);
	}

	public int[] getViewPosition(View v) {
		int[] position = new int[2];

		int radius = (historySize / 2);
		position[0] = (v.getLeft() + radius);
		position[1] = (v.getTop() + radius);

		return position;
	}

	public void addToHistory(BubbleView bv, Animation anim) {
		BubbleView previous = null;
		if(!history.isEmpty()) {
			previous = history.get(history.size() - 1);
		}

		history.add(bv);

		reset();
		animating.setText(bv.getTitle());
		animating.startAnimation(anim);

		if(previous != null) {
			this.previous.setVisibility(View.VISIBLE);
			this.previous.setText(previous.getTitle());
		}
	}

}
