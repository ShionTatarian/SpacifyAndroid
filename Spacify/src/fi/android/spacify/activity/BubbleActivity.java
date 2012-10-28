package fi.android.spacify.activity;

import java.util.ArrayList;
import java.util.List;

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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import fi.android.spacify.R;
import fi.android.spacify.service.ContentManagementService;
import fi.android.spacify.view.BubbleView;

public class BubbleActivity extends BaseActivity {

	private final ContentManagementService cms = ContentManagementService.getInstance();
	private final int ANIMATION_DURATION = 300;
	private List<BubbleView> list = new ArrayList<BubbleView>();
	private ViewGroup root;
	private View bg, searchLayout;
	private EditText searchEdit;
	private int height, width;
	private ImageView seachButton;
	private BubbleFragment activeBubbleFragment;
	private ValueAnimator va;

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
	}

	public void onMeClick(View view) {
		activeBubbleFragment = new BubbleFragment();
		activeBubbleFragment.setBubbleCursor(cms.getBubblesWithPriority(2), this);
		changeFragment(R.id.bubble_root, activeBubbleFragment, R.anim.grow_from_middle,
				R.anim.shrink_to_middle);
	}

	public void onInfoClick(View view) {
		activeBubbleFragment = new BubbleFragment();
		activeBubbleFragment.setBubbleCursor(cms.getBubblesWithPriority(4), this);
		changeFragment(R.id.bubble_root, activeBubbleFragment, R.anim.grow_from_middle,
				R.anim.shrink_to_middle);
	}

	public void onEventsClick(View view) {
		activeBubbleFragment = new BubbleFragment();
		activeBubbleFragment.setBubbleCursor(cms.getBubblesWithPriority(6), this);
		changeFragment(R.id.bubble_root, activeBubbleFragment, R.anim.grow_from_middle,
				R.anim.shrink_to_middle);
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
