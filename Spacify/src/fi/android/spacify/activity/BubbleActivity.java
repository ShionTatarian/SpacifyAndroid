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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout.LayoutParams;
import fi.android.spacify.R;
import fi.android.spacify.adapter.BubbleCursorAdapter;
import fi.android.spacify.adapter.ContextAdapter;
import fi.android.spacify.animation.ReverseInterpolator;
import fi.android.spacify.fragment.BubbleFragment;
import fi.android.spacify.service.ContentManagementService;
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
	private Button meBubble;

	private ContextAdapter contextAdapter;
	private ListView contextList;

	private int contextLarge, contextSmall;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bubble_layout);
		
		contextAdapter = new ContextAdapter(this);
		contextList = (ListView) findViewById(R.id.context_list);
		contextList.setAdapter(contextAdapter);
		contextList.setOnItemClickListener(onContextListClick);

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

		contextAdapter.clear();
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

	public void changeContext(BubbleView bv) {
		activeBubbleFragment.saveBubbles();

		activeBubbleFragment = new BubbleFragment();
		activeBubbleFragment.setBubbleCursor(cms.getBubblesCursor(bv.getLinks()),
				BubbleActivity.this);
		animateSideContextChange(bv);
	}

	private void animateSideContextChange(final BubbleView bv) {
		BubbleView previousCotext = contextAdapter.getSelected();
		Animation anim = getCloseAnimation(previousCotext);
		if(anim != null) {
			anim.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					changeFragment(R.id.bubble_root, activeBubbleFragment);
					openBubbleSideAnimation(bv);
				}
			});
			root.startAnimation(anim);
			return;
		}
	}

	private Animation getCloseAnimation(BubbleView bv) {
		int wantedPosition = 0;
		if(bv != null) {
			wantedPosition = contextAdapter.getPosition(bv);
		}
		int firstPosition = contextList.getFirstVisiblePosition()
				- contextList.getHeaderViewsCount(); // This is the same as
														// child #0
		int wantedChild = wantedPosition - firstPosition;
		// Say, first visible position is 8, you want position 10, wantedChild
		// will now be 2
		// So that means your view is child #2 in the ViewGroup:
		View wantedView = null;
		if(wantedChild < 0 || wantedChild >= contextList.getChildCount()) {
			Log.w(TAG,
					"Unable to get view for desired position, because it's not being displayed on screen.");
		} else {
			wantedView = contextList.getChildAt(wantedChild);
		}
		// Could also check if wantedPosition is between
		// listView.getFirstVisiblePosition() and
		// listView.getLastVisiblePosition() instead.
		
		// float toX = ((left + (wantedView.getWidth() / 2)) / width);
		float toX = 0.95f;
		float toY = 1f;

		if(wantedView != null) {
			float left = wantedView.getLeft();
			float top = wantedView.getTop();
			toY = ((top + (wantedView.getHeight() / 2) + seachButton.getHeight()) / (height));
		}
		

		Animation anim = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, toX,
				Animation.RELATIVE_TO_SELF, toY);
		anim.setFillBefore(true);
		anim.setFillAfter(true);
		anim.setInterpolator(new ReverseInterpolator());
		anim.setDuration(StaticUtils.ANIMATION_DURATION);
		return anim;
	}

	private void openBubbleSideAnimation(BubbleView bv) {
		int wantedPosition = contextAdapter.getPosition(bv); // Whatever position you're looking for
		int firstPosition = contextList.getFirstVisiblePosition() - contextList.getHeaderViewsCount(); // This is the same as child #0
		int wantedChild = wantedPosition - firstPosition;
		// Say, first visible position is 8, you want position 10, wantedChild will now be 2
		// So that means your view is child #2 in the ViewGroup:
		if (wantedChild < 0 || wantedChild >= contextList.getChildCount()) {
		  Log.w(TAG, "Unable to get view for desired position, because it's not being displayed on screen.");
		  return;
		}
		// Could also check if wantedPosition is between listView.getFirstVisiblePosition() and listView.getLastVisiblePosition() instead.
		View wantedView = contextList.getChildAt(wantedChild);
		
		float left = wantedView.getLeft();
		float top = wantedView.getTop();
		// float toX = ((left + (wantedView.getWidth() / 2)) / width);
		float toX = 0.95f;
		float toY = ((top + (wantedView.getHeight() / 2) + seachButton.getHeight()) / (height));

		final Animation scaleAnim = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, toX,
				Animation.RELATIVE_TO_SELF, toY);
		scaleAnim.setFillBefore(true);
		scaleAnim.setFillAfter(true);
		scaleAnim.setInterpolator(new LinearInterpolator());
		scaleAnim.setDuration(StaticUtils.ANIMATION_DURATION);
		
		root.startAnimation(scaleAnim);
		
		contextAdapter.setSelected(bv);
	}

	private double distanceFromCenter(View v1, View v2) {
		float x1 = (v1.getLeft() + (v1.getWidth() / 2));
		float y1 = (v1.getTop() + (v1.getHeight() / 2));
		float x2 = (v2.getLeft() + (v2.getWidth() / 2));
		float y2 = (v2.getTop() + (v2.getHeight() / 2));

		return BubbleFragment.distance(x1, y1, x2, y2);
	}

	public void addContext(BubbleView bv) {
		contextAdapter.add(bv);
		contextAdapter.notifyDataSetChanged();
		int position = contextAdapter.getPosition(bv);
		contextList.smoothScrollToPosition(position);
		changeContext(bv);
	}

	private OnItemClickListener onContextListClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
			BubbleView bv = contextAdapter.getItem(position);
			if(contextAdapter.getSelected().getID() != bv.getID()) {
				addContext(bv);
			}
		}
	};

}
