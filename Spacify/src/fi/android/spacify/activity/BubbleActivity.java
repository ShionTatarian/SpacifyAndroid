package fi.android.spacify.activity;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import fi.android.spacify.R;
import fi.android.spacify.adapter.BubbleCursorAdapter;
import fi.android.spacify.animation.ReverseInterpolator;
import fi.android.spacify.fragment.BubbleFragment;
import fi.android.spacify.fragment.RoundListFragment;
import fi.android.spacify.service.ContentManagementService;
import fi.android.spacify.view.BaseBubbleView;
import fi.android.spacify.view.BubbleView;
import fi.android.spacify.view.BubbleView.BubbleContexts;
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
	private FrameLayout contextLayout;
	private Button meBubble;
	private FrameLayout.LayoutParams meParams;
	private View roundList;
	private RoundListFragment roundListFragment;

	private View viewInMainContext;

	private int contextLarge, contextSmall;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bubble_layout);
		
		roundListFragment = new RoundListFragment();
		roundList = findViewById(R.id.context_round_list);
		roundList.setOnTouchListener(roundListFragment);
		changeFragment(R.id.context_round_list, roundListFragment);

		contextLarge = (int) getResources().getDimension(R.dimen.context_me);
		contextSmall = (int) getResources().getDimension(R.dimen.context_me_side);
		contextLayout = (FrameLayout) findViewById(R.id.context_layout);
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

		meBubble = (Button) findViewById(R.id.button_me);
		meParams = (FrameLayout.LayoutParams) meBubble
				.getLayoutParams();
		meParams.width = contextLarge;
		meParams.height = contextLarge;
		meParams.leftMargin = ((width / 2) - (contextLarge / 2));
		meParams.topMargin = ((height) - (contextLarge * 2 / 3));
		meBubble.setLayoutParams(meParams);

		viewInMainContext = meBubble;
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
		activeBubbleFragment.saveBubbles();

		activeBubbleFragment = new BubbleFragment();
		activeBubbleFragment.setBubbleCursor(cms.getBubblesInContext(BubbleContexts.PEOPLE),
				BubbleActivity.this);
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
		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) v.getLayoutParams();
//		float xPosition = (((float) v.getLeft() + (float) (params.width / 2)) / width);
//		float yPosition = (((float) v.getTop() + (float) (params.height / 2)) / height);
		
		float xPosition = 0.5f;
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
		if(BubbleFragment.isHit(bv, meBubble)) {
			Log.d("DROP DOWN", "Me bubble HIT");
			bv.setContext(BubbleContexts.ME);
			animateInAndOut(bv);
		}
	}

	private void animateInAndOut(final BubbleView bv) {
		int meX = (meBubble.getLeft() + (meBubble.getWidth() / 2));
		int meY = (meBubble.getTop() + (meBubble.getHeight() / 2));

		Animation in = new ScaleAnimation(1, 0, 1, 0, Animation.ABSOLUTE, (meX - bv.x),
				Animation.ABSOLUTE, (meY - bv.y));
		in.setInterpolator(new LinearInterpolator());
		in.setDuration(StaticUtils.ANIMATION_DURATION);

		final Animation out = new ScaleAnimation(1, 0, 1, 0, Animation.ABSOLUTE, (meX - bv.x),
				Animation.ABSOLUTE, (meY - bv.y));
		out.setInterpolator(new ReverseInterpolator());
		out.setDuration(StaticUtils.ANIMATION_DURATION);

		in.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				bv.startAnimation(out);
			}
		});

		bv.startAnimation(in);
	}

	@Override
	protected void onDestroy() {
		activeBubbleFragment.saveBubbles();
		super.onDestroy();
	}

	private boolean meHalfWay = false;
	private boolean meOnSide = false;

	public void onDrag(BaseBubbleView bv) {
		if(!meOnSide && !meHalfWay && BubbleFragment.isHit(bv, meBubble)) {

		}
	}

	public boolean onDrop(BubbleView bv) {
		boolean value = false;
		if(BubbleFragment.isHit(bv, meBubble)) {

		}
		return value;
	}

	public void moveViewInContext() {
		
	}

	public void onSideContextClick(View view) {

	}

	public void onMainContextClick(View view) {
		onMeClick(view);
	}

	private void animateMeButtonHalfWay() {
		float pivotX = 0;
		float pivotY = 0;

		Animation anim = new ScaleAnimation(1, 0.5f, 1, 0.5f, Animation.ABSOLUTE, pivotX,
				Animation.ABSOLUTE, pivotY);
		anim.setDuration(StaticUtils.ANIMATION_DURATION);
		anim.setInterpolator(new LinearInterpolator());
		anim.setFillAfter(true);
		anim.setFillBefore(true);

		meBubble.startAnimation(anim);
	}

}
