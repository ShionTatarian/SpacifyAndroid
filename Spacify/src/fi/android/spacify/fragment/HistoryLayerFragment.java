package fi.android.spacify.fragment;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import fi.android.spacify.R;
import fi.android.spacify.activity.BubbleActivity;
import fi.android.spacify.view.BubbleView;
import fi.android.spacify.view.BubbleView.BubbleJSON;
import fi.qvik.android.util.ImageService;
import fi.spacify.android.util.StaticUtils;

public class HistoryLayerFragment extends BaseFragment implements OnTouchListener {

	private ImageService is = ImageService.getInstance();

	private final String TAG = "HistoryLayer";

	private View frame;
	public int height, width;

	private BubbleActivity parentActivity;
	private TextView previousText, animatingText;
	private View animating, previous;
	private ImageView animatingBackground, previousBackground;
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

		previous = frame.findViewById(R.id.history_previous_layout);
		previousText = (TextView) frame.findViewById(R.id.history_previous_text);
		previousBackground = (ImageView) frame.findViewById(R.id.history_previous_background);
		previousParams = (LayoutParams) previous.getLayoutParams();
		animating = frame.findViewById(R.id.history_animating_layout);
		animatingText = (TextView) frame.findViewById(R.id.history_animating_text);
		animatingBackground = (ImageView) frame.findViewById(R.id.history_animating_background);
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

			animating.setLayoutParams(animatingParams);
			previous.setLayoutParams(previousParams);

			if(prev != null) {
				previous.setVisibility(View.VISIBLE);
				previousText.setText(prev.getTitle());
				setPreviousImage(prev);
			} else {
				previous.setVisibility(View.GONE);
			}
			animatingText.setText(last.getTitle());
			setAnimatingImage(last);
		}
	}

	private int offsetX = 0;
	private int offsetY = 0;
	private float downX = 0, downY = 0;

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
				if(BubbleFragment.distance(downX, downY, x, y) >= 10) {
					touchUp();
				} else {
					animateToCenter();
				}
				break;
		}

		return true;
	}

	private void touchUp() {
		int[] pos = getViewPosition(animating);
		
		double disMiddle = BubbleFragment.distance(pos[0], pos[1], BubbleActivity.width/2, BubbleActivity.height/2);
		double disBottom = BubbleFragment.distance(pos[0], pos[1], BubbleActivity.width, BubbleActivity.height)-100;
		Log.e(TAG, "disMid: " + disMiddle + ", disBot: " + disBottom + "");
		
		BubbleView bv = history.get(history.size() - 1);
		if(disMiddle > disBottom) {
			// animate to bottom
			float pX = (pos[0] - (BubbleActivity.width - (historySize)));
			float pY = (pos[1] - (BubbleActivity.height - (historySize)));

			// float pX = 0.1f;
			// float pY = 0.1f;
			
			Animation animateHistory = new TranslateAnimation(pX, 1, pY, 1);
			animateHistory.setDuration(StaticUtils.ANIMATION_DURATION);
			animateHistory.setInterpolator(new AccelerateInterpolator());
			animateHistory.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					reset();
				}
			});

			animatingParams.topMargin = BubbleActivity.height - (historySize);
			animatingParams.leftMargin = (BubbleActivity.width / 2) - (historySize / 2);
			animating.setLayoutParams(animatingParams);

			animating.startAnimation(animateHistory);
		} else {
			// animate to center
			animateToCenter();
		}
		reset();
	}

	private void animateToCenter() {
		BubbleView bv = history.get(history.size() - 1);
		if(bv == null) {
			return;
		}
		int[] pos = getViewPosition(animating);
		float pivotX = ((float) pos[0] / (float) BubbleActivity.width);
		float pivotY = ((float) pos[1] / (float) BubbleActivity.height);

		Animation anim = new ScaleAnimation(0f, 1, 0f, 1, Animation.RELATIVE_TO_PARENT, pivotX,
				Animation.RELATIVE_TO_PARENT, pivotY);
		anim.setDuration(StaticUtils.ANIMATION_DURATION);
		anim.setInterpolator(new AccelerateInterpolator());
		anim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				reset();
			}
		});
		history.remove(history.size() - 1);
		reset();

		parentActivity.setTierZero(bv, anim, false);
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
		downX = x;
		downY = y;
	}

	public int[] getViewPosition(View v) {
		int[] position = new int[2];

		int radius = (historySize / 2);
		position[0] = (v.getLeft() + radius);
		position[1] = (v.getTop() + radius);

		return position;
	}

	public void addToHistory(BubbleView bv, Animation anim) {
		history.add(bv);

		reset();
		setAnimatingImage(bv);
		animating.startAnimation(anim);
	}

	private void setPreviousImage(BubbleView bv) {
		JSONObject style = bv.getStyleOverrides();
		String imageUrl = null;
		try {
			if(style.has(BubbleJSON.titleImageUrl)) {
				imageUrl = style.getString(BubbleJSON.titleImageUrl);
			}
		} catch(JSONException e) {
			e.printStackTrace();
		}

		previousBackground.setImageResource(R.drawable.lightblueball);
		is.assignHelpMethod(getActivity(), previousBackground, StaticUtils.IMAGE_MEDIUM, imageUrl,
				R.drawable.lightblueball, null);
	}

	private void setAnimatingImage(BubbleView bv) {
		JSONObject style = bv.getStyleOverrides();
		String imageUrl = null;
		try {
			if(style != null && style.has(BubbleJSON.titleImageUrl)) {
				imageUrl = style.getString(BubbleJSON.titleImageUrl);
			}
		} catch(JSONException e) {
			e.printStackTrace();
		}

		animatingBackground.setImageResource(R.drawable.lightblueball);
		is.assignHelpMethod(getActivity(), animatingBackground, StaticUtils.IMAGE_MEDIUM, imageUrl,
				R.drawable.lightblueball, null);
	}

	public void clearHistory() {
		history.clear();
		reset();
	}

}
