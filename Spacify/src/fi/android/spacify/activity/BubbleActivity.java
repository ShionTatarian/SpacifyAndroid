package fi.android.spacify.activity;

import java.util.Random;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import fi.android.spacify.R;
import fi.android.spacify.adapter.BubbleCursorAdapter;
import fi.android.spacify.adapter.MeContextAdapter;
import fi.android.spacify.adapter.TierOneAdapter;
import fi.android.spacify.adapter.WheelAdapter;
import fi.android.spacify.animation.ReverseInterpolator;
import fi.android.spacify.fragment.BubbleFragment;
import fi.android.spacify.fragment.HistoryLayerFragment;
import fi.android.spacify.fragment.ThirdLayerBaseFragment;
import fi.android.spacify.fragment.TierZeroFragment;
import fi.android.spacify.fragment.WheelListFragment;
import fi.android.spacify.service.ContentManagementService;
import fi.android.spacify.view.BubbleView;
import fi.android.spacify.view.BubbleView.BubbleContexts;
import fi.spacify.android.util.SpacifyEvents;
import fi.spacify.android.util.StaticUtils;

public class BubbleActivity extends BaseActivity {

	private final String TAG = "BubbleActivity";

	private final ContentManagementService cms = ContentManagementService.getInstance();
	private ViewGroup root;
	private View bg, searchLayout;
	private EditText searchEdit;
	private BubbleCursorAdapter bcAdapter;
	private Gallery searchGallery;
	public static int height, width;
	private ImageView seachButton;
	private BubbleFragment activeBubbleFragment;
	private ValueAnimator va;
	private Animation closeFragmentAnimation;
	private Button meBubble;

	private int contextLarge, contextSmall;

	private WheelListFragment meContextFragment, tierOne, tierTwo;
	private WheelAdapter meContextAdapter;

	private boolean tierOneOpen = false;
	private boolean tierTwoOpen = false;

	private WheelAdapter tierOneAdapter;

	private View t0, t1, t2, t3, meContextView;

	private TierZeroFragment tierZero;

	private ThirdLayerBaseFragment thirdLayer;
	private HistoryLayerFragment historyLayer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bubble_layout);
		cms.fetchBubbles();
		
		DisplayMetrics metrics = new DisplayMetrics();
		Display display = getWindowManager().getDefaultDisplay();
		display.getMetrics(metrics);
		height = metrics.heightPixels;
		width = metrics.widthPixels;

		historyLayer = new HistoryLayerFragment();
		changeFragment(R.id.history_layer, historyLayer);

		t0 = findViewById(R.id.tier_zero);
		t1 = findViewById(R.id.tier_one);
		t2 = findViewById(R.id.tier_two);
		t3 = findViewById(R.id.third_layer);
		meContextView = findViewById(R.id.round_context_list);

		meContextAdapter = new MeContextAdapter(this);
		meContextAdapter.addAll(cms.getBubblesFromCursor(cms.getBubblesAlwaysOnScreen()));
		meContextAdapter.setBubbleSize((int) getResources().getDimension(R.dimen.tier_two_bubble));
		meContextFragment = new WheelListFragment();
		meContextFragment.setTierSize((int) getResources().getDimension(R.dimen.context_round_list));
		meContextFragment.setAdapter(meContextAdapter);
		changeFragment(R.id.round_context_list, meContextFragment);

		thirdLayer = new ThirdLayerBaseFragment();
		changeFragment(R.id.third_layer, thirdLayer);

		contextLarge = (int) getResources().getDimension(R.dimen.context_me);
		contextSmall = (int) getResources().getDimension(R.dimen.context_me_side);
		bg = findViewById(R.id.bubble_background);
		searchLayout = findViewById(R.id.search_layout);
		seachButton = (ImageView) findViewById(R.id.search_button);
		searchEdit = (EditText) findViewById(R.id.search_edit);
		searchGallery = (Gallery) findViewById(R.id.search_bubble_gallery);
		bcAdapter = new BubbleCursorAdapter(getApplicationContext(), cms.getBubblesWithPriority(0),
				false);
		bcAdapter.setFilterQueryProvider(filterQuery);

		searchGallery.setAdapter(bcAdapter);

		searchEdit.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				bcAdapter.getFilter().filter(s.toString());
			}
		});
		
		float searchButtonWidth = getResources().getDimension(R.dimen.search_button_width);
		LayoutParams searchParams = (LayoutParams) searchLayout.getLayoutParams();
		searchParams.width = (int) (width - searchButtonWidth);
		searchParams.setMargins(0, 0, -searchParams.width, 0);
		searchLayout.setLayoutParams(searchParams);

		activeBubbleFragment = new BubbleFragment();
		
		root = (ViewGroup) findViewById(R.id.bubble_root);
		changeFragment(R.id.bubble_root, activeBubbleFragment);
		// cms.fetchBubbles();

		meBubble = (Button) findViewById(R.id.button_me);

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private FilterQueryProvider filterQuery = new FilterQueryProvider() {

		@Override
		public Cursor runQuery(CharSequence constraint) {
			return cms.getBubbleSearch(constraint);
		}
	};

	public void onMeClick(final View view) {
		meContextAdapter.setSelected(null);
		meContextFragment.redraw();
		if(tierZero != null) {
			removeFragment(tierZero);
			tierZero = null;
		}
		if(tierOne != null) {
			removeFragment(tierOne);
			tierOne = null;
		}
		if(tierTwo != null) {
			removeFragment(tierTwo);
			tierTwo = null;
		}

		// activeBubbleFragment.saveBubbles();
		// activeBubbleFragment = new BubbleFragment();
		// activeBubbleFragment.setBubbleCursor(cms.getBubblesInContext(BubbleContexts.PEOPLE),
		// BubbleActivity.this);
		// animateFragmentChange(view);
		//
		// contextAdapter.clear();
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
//		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) v.getLayoutParams();
//		float xPosition = (((float) v.getLeft() + (float) (params.width / 2)) / width);
//		float yPosition = (((float) v.getTop() + (float) (params.height / 2)) / height);
		
		float xPosition = 1f;
		float yPosition = 1f;

		final Animation scaleAnim = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF,
				xPosition, Animation.RELATIVE_TO_SELF, yPosition);
		scaleAnim.setFillBefore(true);
		scaleAnim.setFillAfter(true);
		scaleAnim.setInterpolator(new LinearInterpolator());
		scaleAnim.setDuration(StaticUtils.ANIMATION_DURATION);
		
		closeFragmentAnimation = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF,
				xPosition, Animation.RELATIVE_TO_SELF, yPosition);
		closeFragmentAnimation.setFillBefore(true);
		closeFragmentAnimation.setFillAfter(true);
		closeFragmentAnimation.setInterpolator(new ReverseInterpolator());
		closeFragmentAnimation.setDuration(StaticUtils.ANIMATION_DURATION);

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
		va.setDuration(StaticUtils.ANIMATION_DURATION);
		va.setIntValues(current, target);
		va.addUpdateListener(new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				params.rightMargin = (Integer) animation.getAnimatedValue();
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
			}, StaticUtils.ANIMATION_DURATION);
		}
	}

	public void onEmptyClick() {
		final LayoutParams searchParams = (LayoutParams) searchLayout.getLayoutParams();
		if(searchParams.rightMargin == 0) {
			onSearchClick(null);
		}
	}

	public void dropDown(BubbleView bv) {
	}

	@Override
	protected void onDestroy() {
		activeBubbleFragment.saveBubbles();
		super.onDestroy();
	}

	private double distanceFromCenter(View v1, View v2) {
		float x1 = (v1.getLeft() + (v1.getWidth() / 2));
		float y1 = (v1.getTop() + (v1.getHeight() / 2));
		float x2 = (v2.getLeft() + (v2.getWidth() / 2));
		float y2 = (v2.getTop() + (v2.getHeight() / 2));

		return BubbleFragment.distance(x1, y1, x2, y2);
	}

	public void setTierZero(BubbleView bv, Animation anim, boolean addToHistory) {
		if(tierOne != null) {
			removeFragment(tierOne);
			tierOne = null;
		}
		if(tierTwo != null) {
			removeFragment(tierTwo);
			tierTwo = null;
		}

		if(addToHistory && tierZero != null && tierZero.getBubbleView() != null) {
			float pX = 100;
			float pY = 1000;

			Animation animateHistory = new ScaleAnimation(1.5f, 1, 1.5f, 1,
					Animation.ABSOLUTE, pX,
					Animation.ABSOLUTE, pY);
			animateHistory.setDuration(StaticUtils.ANIMATION_DURATION);
			animateHistory.setInterpolator(new AccelerateInterpolator());

			historyLayer.addToHistory(tierZero.getBubbleView(), animateHistory);
		}

		t0.startAnimation(anim);

		tierZero = new TierZeroFragment();
		tierZero.setBubbleView(bv);
		changeFragment(R.id.tier_zero, tierZero);
	}

	public void setTierZeroFromMeContext(View from, BubbleView bv) {
		tierOne = null;
		setTierOne(bv);

		from = (View) from.getParent();

		float rotation = (from.getRotation() / 90);
		float size = from.getHeight();
		float pivotX = (rotation * (size / 2)) / width;
		float pivotY = 1f - (((1f - rotation) * (size / 2)) / height);

		Animation anim = new ScaleAnimation(0, 1, 0, 1, 
				Animation.RELATIVE_TO_PARENT, pivotX,
				Animation.RELATIVE_TO_PARENT, pivotY);
		anim.setDuration(StaticUtils.ANIMATION_DURATION);
		anim.setInterpolator(new AccelerateInterpolator());

		meContextAdapter.setSelected(bv);
		meContextFragment.redraw();
		setTierZero(bv, anim, true);
	}

	public void setTierOne(BubbleView bv) {
		if(tierOne != null) {
			openTierZeroContent();
			return;
		}
		
		Animation anim = new ScaleAnimation(3, 1, 3, 1);
		anim.setInterpolator(new LinearInterpolator());
		anim.setDuration(StaticUtils.ANIMATION_DURATION);
		t1.startAnimation(anim);

		tierOneAdapter = new TierOneAdapter(this);
		tierOneAdapter.setBubbleSize((int) getResources().getDimension(R.dimen.tier_one_bubble));
		tierOneAdapter.addAll(cms.getBubbles(bv.getLinks()));
		tierOne = new WheelListFragment();
		tierOne.setAdapter(tierOneAdapter);
		tierOne.setTierSize((int) getResources().getDimension(R.dimen.tier_one));
		changeFragment(R.id.tier_one, tierOne);
	}

	private void openTierZeroContent() {
		Random r = new Random();
		int count = 1 + r.nextInt(5);
		for(int i = 0; i < count; i++) {
			thirdLayer.onImageClick(tierZero.getBubbleView());
		}
		
		Animation anim = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_PARENT, 0.5f,
				Animation.RELATIVE_TO_PARENT, 0.75f);
		anim.setInterpolator(new AccelerateInterpolator());
		anim.setDuration(StaticUtils.ANIMATION_DURATION);

		t3.startAnimation(anim);
	}

	public void onTierOneClick(View from, BubbleView bv) {
		from = (View) from.getParent();

		float pivotX = (0.5f);
		float pivotY = (0.2f);

		Animation anim = new ScaleAnimation(0, 1, 0, 1, 
				Animation.RELATIVE_TO_SELF, pivotX,
				Animation.RELATIVE_TO_SELF, pivotY);
		anim.setDuration(StaticUtils.ANIMATION_DURATION);
		anim.setInterpolator(new AccelerateInterpolator());

		setTierZero(bv, anim, true);
		tierOne = null;
		setTierOne(bv);
		
		

//		tierOneAdapter.setSelected(bv);
//		tierOne.redraw();
//
//		Animation anim = new ScaleAnimation(3, 1, 3, 1);
//		anim.setInterpolator(new LinearInterpolator());
//		anim.setDuration(StaticUtils.ANIMATION_DURATION);
//		t2.startAnimation(anim);
//
//		WheelAdapter tierTwoAdapter = new TierOneAdapter(this);
//		tierTwoAdapter.setBubbleSize((int) getResources().getDimension(R.dimen.tier_two_bubble));
//		tierTwoAdapter.addAll(cms.getBubbles(bv.getLinks()));
//		tierTwo = new WheelListFragment();
//		tierTwo.setAdapter(tierTwoAdapter);
//		tierTwo.setTierSize((int) getResources().getDimension(R.dimen.tier_two));
//		changeFragment(R.id.tier_two, tierTwo);
	}

	@Override
	public boolean handleMessage(Message msg) {

		switch (SpacifyEvents.values()[msg.what]) {
			case ALL_BUBBLES_FETCHED:
				meContextAdapter.addAll(cms.getBubblesFromCursor(cms.getBubblesAlwaysOnScreen()));
				meContextFragment.redraw();
				break;
			default:
				break;
		}

		return super.handleMessage(msg);
	}

}
