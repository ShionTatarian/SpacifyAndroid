package fi.android.spacify.activity;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import fi.android.spacify.R;
import fi.android.spacify.animation.ReverseInterpolator;
import fi.android.spacify.fragment.BubbleFragment;
import fi.android.spacify.service.ContentManagementService;
import fi.android.spacify.view.BubbleView.BubbleContexts;

public class BubbleActivity extends BaseActivity {

	private final ContentManagementService cms = ContentManagementService.getInstance();
	private final int ANIMATION_DURATION = 300;
	private ViewGroup root;
	private View bg, searchLayout;
	private EditText searchEdit;
	public static int height, width;
	private ImageView seachButton;
	private BubbleFragment activeBubbleFragment;
	private ValueAnimator va;
	private Animation closeFragmentAnimation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bubble_layout);
		
		bg = findViewById(R.id.bubble_background);
		searchLayout = findViewById(R.id.search_layout);
		seachButton = (ImageView) findViewById(R.id.search_button);
		searchEdit = (EditText) findViewById(R.id.search_edit);

		DisplayMetrics metrics = new DisplayMetrics();
		Display display = getWindowManager().getDefaultDisplay();
		display.getMetrics(metrics);
		height = metrics.heightPixels;
		width = metrics.widthPixels;
		
		float searchButtonWidth = getResources().getDimension(R.dimen.search_button_width);
		LayoutParams searchParams = (LayoutParams) searchLayout.getLayoutParams();
		searchParams.width = (int) (width - searchButtonWidth);
		searchParams.setMargins(0, 0, -searchParams.width, 0);
		searchLayout.setLayoutParams(searchParams);

		activeBubbleFragment = new BubbleFragment();
		
		root = (ViewGroup) findViewById(R.id.bubble_root);
		changeFragment(R.id.bubble_root, activeBubbleFragment);
		cms.fetchBubbles();
	}

	public void onMeClick(final View view) {
		activeBubbleFragment.saveBubbles();

		activeBubbleFragment = new BubbleFragment();
		activeBubbleFragment.setBubbleCursor(cms.getBubblesWithPriority(2), BubbleActivity.this);
		animateFragmentChange(view);
	}

	public void onPeopleClick(View view) {
		activeBubbleFragment.saveBubbles();

		activeBubbleFragment = new BubbleFragment();
		activeBubbleFragment.setBubbleCursor(cms.getBubblesInContext(BubbleContexts.PEOPLE), this);
		animateFragmentChange(view);
	}

	public void onEventsClick(View view) {
		activeBubbleFragment.saveBubbles();

		activeBubbleFragment = new BubbleFragment();
		activeBubbleFragment.setBubbleCursor(cms.getBubblesInContext(BubbleContexts.EVENTS), this);
		animateFragmentChange(view);
	}

	private void animateFragmentChange(final View v) {
		if(closeFragmentAnimation != null) {
			closeFragmentAnimation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					changeFragment(R.id.bubble_root, activeBubbleFragment);
					openAnimation(v);
				}
			});
			root.startAnimation(closeFragmentAnimation);
		} else {
			changeFragment(R.id.bubble_root, activeBubbleFragment);
			openAnimation(v);
		}
	}

	private void openAnimation(View v) {
		LayoutParams params = (LayoutParams) v.getLayoutParams();
		float xPosition = (((float) v.getLeft() + (float) (params.width / 2)) / width);
		float yPosition = (((float) v.getTop() + (float) (params.height / 2)) / height);

		final Animation scaleAnim = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF,
				xPosition, Animation.RELATIVE_TO_SELF, yPosition);
		scaleAnim.setFillBefore(true);
		scaleAnim.setFillAfter(true);
		scaleAnim.setInterpolator(new LinearInterpolator());
		scaleAnim.setDuration(ANIMATION_DURATION);
		
		closeFragmentAnimation = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF,
				xPosition, Animation.RELATIVE_TO_SELF, yPosition);
		closeFragmentAnimation.setFillBefore(true);
		closeFragmentAnimation.setFillAfter(true);
		closeFragmentAnimation.setInterpolator(new ReverseInterpolator());
		closeFragmentAnimation.setDuration(ANIMATION_DURATION);

		root.startAnimation(scaleAnim);
	}

	public void onSearchClick(View view) {
		if(va != null) {
			va.cancel();
		}

		final LayoutParams params = (LayoutParams) searchLayout.getLayoutParams();
		int current = params.rightMargin;
		int target = current != 0 ? 0 : -params.width;
		toggleSearchKeyboard(target != 0);

		va = new ValueAnimator();
		va.setInterpolator(new AccelerateInterpolator());
		va.setDuration(ANIMATION_DURATION);
		va.setIntValues(current, target);
		va.addUpdateListener(new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				params.rightMargin = (Integer) animation.getAnimatedValue();
				Log.d("anim", "value: " + params.rightMargin);
				seachButton.post(new Runnable() {

					@Override
					public void run() {
						searchLayout.setLayoutParams(params);
					}
				});
			}
		});
		va.start();
	}

	private void toggleSearchKeyboard(boolean show) {
		final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if(show) {
			imm.hideSoftInputFromWindow(searchEdit.getWindowToken(),
					InputMethodManager.RESULT_UNCHANGED_SHOWN);
		} else {
			searchEdit.postDelayed(new Runnable() {

				@Override
				public void run() {
					searchEdit.requestFocus();
					imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
							InputMethodManager.RESULT_UNCHANGED_SHOWN);
				}
			}, ANIMATION_DURATION);
		}
	}

	public void onEmptyClick() {
		final LayoutParams searchParams = (LayoutParams) searchLayout.getLayoutParams();
		if(searchParams.rightMargin == 0) {
			onSearchClick(null);
		}
	}
}
