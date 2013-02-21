package fi.android.spacify.activity;

import org.json.JSONObject;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextUtils;
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
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import fi.android.spacify.R;
import fi.android.spacify.adapter.BubbleCursorAdapter;
import fi.android.spacify.adapter.MeContextAdapter;
import fi.android.spacify.adapter.TierOneAdapter;
import fi.android.spacify.adapter.WheelAdapter;
import fi.android.spacify.animation.ReverseInterpolator;
import fi.android.spacify.fragment.BubbleControlFragment;
import fi.android.spacify.fragment.BubbleFragment;
import fi.android.spacify.fragment.CallToScreenFragment;
import fi.android.spacify.fragment.ContentFragment;
import fi.android.spacify.fragment.HistoryLayerFragment;
import fi.android.spacify.fragment.LoginFragment;
import fi.android.spacify.fragment.ThirdLayerBaseFragment;
import fi.android.spacify.fragment.TierOneWheelListFragment;
import fi.android.spacify.fragment.TierZeroFragment;
import fi.android.spacify.fragment.WheelListFragment;
import fi.android.spacify.service.AccountService;
import fi.android.spacify.service.ContentManagementService;
import fi.android.spacify.view.AvatarBubble;
import fi.android.spacify.view.BubbleView;
import fi.android.spacify.view.BubbleView.BubbleJSON;
import fi.qvik.android.util.ImageCache;
import fi.spacify.android.util.SpacifyEvents;
import fi.spacify.android.util.StaticUtils;

public class BubbleActivity extends BaseActivity {

	private final String TAG = "BubbleActivity";

	private final ContentManagementService cms = ContentManagementService.getInstance();
	private final AccountService account = AccountService.getInstance();

	private ViewGroup root;
	private View bg, searchLayout;
	private EditText searchEdit;
	private BubbleCursorAdapter bcAdapter;
	private Gallery searchGallery;
	public static int height, width;
	private ImageView seachButton;
	private BubbleFragment activeBubbleFragment;
	private ValueAnimator va;
	private Button meBubble;

	private int contextLarge, contextSmall;

	private WheelListFragment meContextFragment, tierOne;
	private WheelAdapter meContextAdapter;

	private boolean tierOneOpen = false;

	private WheelAdapter tierOneAdapter;

	private View t0, t1, t3, meContextView, contentLayer, history, popup, extraPopupFragment;

	private TierZeroFragment tierZero;
	private ContentFragment contentFragment;
	private ThirdLayerBaseFragment thirdLayer;
	private HistoryLayerFragment historyLayerFragment;
	private BubbleControlFragment controls;
	private LoginFragment loginFragment;
	private Animation closePopupAnimation, closeLoginAnimation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bubble_layout);

		DisplayMetrics metrics = new DisplayMetrics();
		Display display = getWindowManager().getDefaultDisplay();
		display.getMetrics(metrics);
		height = metrics.heightPixels;
		width = metrics.widthPixels;

		historyLayerFragment = new HistoryLayerFragment();
		changeFragment(R.id.history_layer, historyLayerFragment);

		extraPopupFragment = findViewById(R.id.login_layer);
		extraPopupFragment.setVisibility(View.GONE);
		t0 = findViewById(R.id.tier_zero);
		t1 = findViewById(R.id.tier_one);
		t3 = findViewById(R.id.third_layer);
		t3.setVisibility(View.GONE);
		meContextView = findViewById(R.id.round_context_list);
		contentLayer = findViewById(R.id.content_layer);
		contentLayer.setVisibility(View.GONE);
		history = findViewById(R.id.history_layer);
		popup = findViewById(R.id.control_popup);

		meContextAdapter = new MeContextAdapter(this);
		meContextAdapter.addAll(cms.getBubblesFromCursor(this, cms.getBubblesAlwaysOnScreen()));
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
		searchGallery.setOnItemClickListener(onSearchGalleryClick);
		bcAdapter = new BubbleCursorAdapter(this, cms.getBubblesWithPriority(0), false);
		bcAdapter.setFilterQueryProvider(filterQuery);

		searchGallery.setAdapter(bcAdapter);

		searchEdit.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
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

		root = (ViewGroup) findViewById(R.id.bubble_root);
		meBubble = (Button) findViewById(R.id.button_me);

		controls = new BubbleControlFragment();
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.control_popup, controls);
		ft.commit();

		openBubbleFragment();
	}

	private void openBubbleFragment() {
		if (activeBubbleFragment == null) {
			activeBubbleFragment = new BubbleFragment();
			changeFragment(R.id.bubble_root, activeBubbleFragment);

			if (account.isLoggedIn()) {
				// set avatar bubble and bubbles linked to it
				AvatarBubble avatar = new AvatarBubble(BubbleActivity.this, account.getAvatarBubbleCursor());
				if (avatar.x <= 0 && avatar.y <= 0) {
					avatar.x = (width / 2);
					avatar.y = (height / 5);
				}
				activeBubbleFragment.addBubble(avatar);
				activeBubbleFragment.setBubbleCursor(cms.getBubblesCursor(account.getFavorites()), BubbleActivity.this);
				activeBubbleFragment.setBubbleCursor(cms.getBubblesCursor(avatar.getLinks()), BubbleActivity.this);
			} else {
				// set login bubble
				AvatarBubble avatar = new AvatarBubble(this, "");
				if (avatar.x <= 0 && avatar.y <= 0) {
					avatar.x = (width / 2);
					avatar.y = (height / 5);
				}
				activeBubbleFragment.addBubble(avatar);
			}
		}

		openRootAnimation(null);
	}

	@Override
	public void onBackPressed() {
		boolean skipBack = anythingOpen();

		if (skipBack) {
			onMeClick(null);
		} else {
			ImageCache.getInstance().clearCache();

			// Go to Home screen instead of closing the app. App crashes on
			// every other startup on OOM error :(
			Intent i = new Intent(Intent.ACTION_MAIN);
			i.addCategory(Intent.CATEGORY_HOME);
			startActivity(i);
			// super.onBackPressed();
		}
	}

	private boolean anythingOpen() {
		LayoutParams searchParams = (LayoutParams) searchLayout.getLayoutParams();
		boolean anythingOpen = false;
		if (searchParams.rightMargin == 0) {
			onSearchClick(null);
			anythingOpen = true;
		}
		if (contentLayer.getVisibility() == View.VISIBLE) {
			closeContentView();
			anythingOpen = true;
		}
		if (t3.getVisibility() == View.VISIBLE) {
			closeThirdLayer();
			thirdLayer.clearThirdLayerImages();
			anythingOpen = true;
		}
		if (!anythingOpen && tierZero != null && tierZero.getBubbleView() != null) {
			anythingOpen = true;
		}
		return anythingOpen;
	}

	private OnItemClickListener onSearchGalleryClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> gallery, View view, int position, long id) {
			toggleSearchKeyboard(true);
			onSearchClick(null);

			float pivotX = view.getX() / width;
			float pivotY = 0.1f;

			Animation anim = new ScaleAnimation(0.2f, 1, 0.2f, 1, Animation.RELATIVE_TO_PARENT, pivotX,
					Animation.RELATIVE_TO_PARENT, pivotY);
			anim.setDuration(StaticUtils.ANIMATION_DURATION);
			anim.setInterpolator(new AccelerateInterpolator());

			Cursor c = bcAdapter.getCursor();
			c.moveToPosition(position);
			BubbleView bv = new BubbleView(BubbleActivity.this, c);

			meContextAdapter.setSelected(bv);
			meContextFragment.redraw();
			setTierZero(bv, anim, true);
		}
	};

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
		if (!anythingOpen()) {
			return;
		}

		openBubbleFragment();
		closeContentView();
		closeControlPopup();
		historyLayerFragment.clearHistory();

		meContextAdapter.setSelected(null);
		meContextFragment.redraw();
		if (tierZero != null) {
			tierZero.clear();
			removeFragment(tierZero);
		}
		if (tierOne != null) {
			removeFragment(tierOne);
		}
		// activeBubbleFragment.saveBubbles();
		// activeBubbleFragment = new BubbleFragment();
		// activeBubbleFragment.setBubbleCursor(cms.getBubblesInContext(BubbleContexts.PEOPLE),
		// BubbleActivity.this);
		// animateFragmentChange(view);
		//
		// contextAdapter.clear();
	}

	private void openRootAnimation(View v) {
		// FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)
		// v.getLayoutParams();
		// float xPosition = (((float) v.getLeft() + (float) (params.width / 2))
		// / width);
		// float yPosition = (((float) v.getTop() + (float) (params.height / 2))
		// / height);

		float xPosition = 0f;
		float yPosition = 1f;

		final Animation scaleAnim = openFromAnimation(xPosition, yPosition);

		root.setVisibility(View.VISIBLE);
		root.startAnimation(scaleAnim);
	}

	private Animation closeToAnimation(float x, float y) {
		Animation closeAnim = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, x, Animation.RELATIVE_TO_SELF,
				y);
		closeAnim.setFillBefore(true);
		closeAnim.setFillAfter(true);
		closeAnim.setInterpolator(new ReverseInterpolator());
		closeAnim.setDuration(StaticUtils.ANIMATION_DURATION);

		return closeAnim;
	}

	private Animation openFromAnimation(float x, float y) {
		Animation scaleAnim = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, x, Animation.RELATIVE_TO_SELF,
				y);
		scaleAnim.setFillBefore(true);
		scaleAnim.setFillAfter(true);
		scaleAnim.setInterpolator(new AccelerateInterpolator());
		scaleAnim.setDuration(StaticUtils.ANIMATION_DURATION);

		return scaleAnim;
	}

	public void onSearchClick(View view) {
		if (va != null) {
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

	private void toggleSearchKeyboard(boolean hide) {
		final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (hide) {
			imm.hideSoftInputFromWindow(searchEdit.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
		} else {
			searchEdit.postDelayed(new Runnable() {

				@Override
				public void run() {
					searchEdit.requestFocus();
					imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.RESULT_UNCHANGED_SHOWN);
				}
			}, StaticUtils.ANIMATION_DURATION);
		}
	}

	public void onEmptyClick() {
		final LayoutParams searchParams = (LayoutParams) searchLayout.getLayoutParams();
		if (searchParams.rightMargin == 0) {
			onSearchClick(null);
		}

		if (contentLayer.getVisibility() == View.VISIBLE) {
			closeContentView();
		}

		if (t3.getVisibility() == View.VISIBLE) {
			closeThirdLayer();
			thirdLayer.clearThirdLayerImages();
		}
	}

	public void dropDown(BubbleView bv) {
	}

	@Override
	protected void onDestroy() {
		if (activeBubbleFragment != null) {
			activeBubbleFragment.saveBubbles();
		}
		super.onDestroy();
	}

	private AnimationListener hideRootAnimationListener = new AnimationListener() {

		@Override
		public void onAnimationStart(Animation animation) {
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			root.setVisibility(View.GONE);
			activeBubbleFragment.saveBubbles();
			removeFragment(activeBubbleFragment);
			activeBubbleFragment = null;
		}
	};

	private CallToScreenFragment callToScreenFragment;

	public void setTierZero(final BubbleView bv, Animation anim, boolean addToHistory) {
		if (root.getVisibility() == View.VISIBLE) {
			Animation closeFragmentAnimation = closeToAnimation(0, 1);
			closeFragmentAnimation.setAnimationListener(hideRootAnimationListener);
			root.startAnimation(closeFragmentAnimation);
		}
		if (contentLayer.getVisibility() == View.VISIBLE) {
			closeContentView();
		}

		if (addToHistory && tierZero != null && tierZero.getBubbleView() != null) {
			float pX = 50;
			float pY = 550;

			Animation animateHistory = new ScaleAnimation(1.4f, 1, 1.4f, 1, Animation.ABSOLUTE, pX, Animation.ABSOLUTE,
					pY);
			animateHistory.setDuration(StaticUtils.ANIMATION_DURATION);
			animateHistory.setInterpolator(new AccelerateInterpolator());

			historyLayerFragment.addToHistory(tierZero.getBubbleView(), animateHistory);
		}

		t0.startAnimation(anim);

		if (tierZero == null) {
			tierZero = new TierZeroFragment();
		}
		tierZero.setBubbleView(bv);
		changeFragment(R.id.tier_zero, tierZero);

		setTierOne(bv);

		if (closePopupAnimation != null) {
			closeControlPopup();
		}
		popup.postDelayed(new Runnable() {

			@Override
			public void run() {
				openControlPopup(bv);
			}
		}, StaticUtils.ANIMATION_DURATION);
	}

	public void closeControlPopup() {
		if (closePopupAnimation != null) {
			popup.setVisibility(View.GONE);
			popup.startAnimation(closePopupAnimation);
			closePopupAnimation = null;
		}
	}

	private void openControlPopup(final BubbleView bv) {
		controls.setAdapter(bv.getControlAdapter(this));

		popup.setVisibility(View.VISIBLE);
		controls.openMenu(bv);
		int tZero = (int) getResources().getDimension(R.dimen.tier_zero_controls);
		controls.setSize(tZero);

		float dx = 0.5f;
		float dy = 0.5f;
		final Animation anim = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, dx,
				Animation.RELATIVE_TO_SELF, dy);
		anim.setDuration(StaticUtils.ANIMATION_DURATION);
		anim.setInterpolator(new LinearInterpolator());

		if (closePopupAnimation != null) {
			popup.setVisibility(View.GONE);
			closePopupAnimation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					popup.setVisibility(View.VISIBLE);
					popup.startAnimation(anim);
				}
			});
			popup.startAnimation(closePopupAnimation);
			closePopupAnimation = null;
		} else {
			popup.startAnimation(anim);
		}

		closePopupAnimation = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, dx,
				Animation.RELATIVE_TO_SELF, dy);
		closePopupAnimation.setDuration(StaticUtils.ANIMATION_DURATION);
		closePopupAnimation.setInterpolator(new ReverseInterpolator());
	}

	private void setContent(final BubbleView bv) {
		if (contentFragment == null) {
			contentFragment = new ContentFragment();
		}
		if (contentLayer.getVisibility() == View.VISIBLE) {
			closeContentView();
			return;
		}
		contentLayer.setVisibility(View.VISIBLE);
		contentFragment.setBubbleView(bv);
		changeFragment(R.id.content_layer, contentFragment);

		Animation anim = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_PARENT, 0.5f,
				Animation.RELATIVE_TO_PARENT, 0.7f);
		anim.setDuration(StaticUtils.ANIMATION_DURATION);
		anim.setInterpolator(new AccelerateInterpolator());
		contentLayer.startAnimation(anim);
	}

	private void closeContentView() {
		if (contentLayer.getVisibility() != View.VISIBLE) {
			return;
		}

		Animation anim = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_PARENT, 0.5f,
				Animation.RELATIVE_TO_PARENT, 0.7f);
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
				contentLayer.setVisibility(View.GONE);
			}
		});
		contentLayer.startAnimation(anim);
	}

	public void setTierZeroFromMeContext(View from, BubbleView bv) {
		if (tierZero != null && tierZero.getBubbleView() != null && tierZero.getBubbleView().getID().equals(bv.getID())) {
			// can't open two same bubbles
			return;
		}

		from = (View) from.getParent();

		float rotation = (from.getRotation() / 90);
		float size = from.getHeight();
		float pivotX = (rotation * (size / 2)) / width;
		float pivotY = 1f - (((1f - rotation) * (size / 2)) / height);

		Animation anim = new ScaleAnimation(0.1f, 1, 0.1f, 1, Animation.RELATIVE_TO_PARENT, pivotX,
				Animation.RELATIVE_TO_PARENT, pivotY);
		anim.setDuration(StaticUtils.ANIMATION_DURATION);
		anim.setInterpolator(new AccelerateInterpolator());

		meContextAdapter.setSelected(bv);
		meContextFragment.redraw();
		setTierZero(bv, anim, true);
	}

	public void setTierOne(BubbleView bv) {
		Animation anim = new ScaleAnimation(3, 1, 3, 1, Animation.ABSOLUTE, width / 2, Animation.ABSOLUTE, height / 2);
		anim.setInterpolator(new LinearInterpolator());
		anim.setDuration(StaticUtils.ANIMATION_DURATION);

		tierOneAdapter = new TierOneAdapter(this);
		tierOneAdapter.setBubbleSize((int) getResources().getDimension(R.dimen.tier_one_bubble));
		tierOneAdapter.addAll(cms.getBubbles(this, bv.getLinks()));
		tierOne = new TierOneWheelListFragment();
		tierOne.setAdapter(tierOneAdapter);
		tierOne.setTierSize((int) getResources().getDimension(R.dimen.tier_one));
		changeFragment(R.id.tier_one, tierOne);
		t1.startAnimation(anim);
	}

	public void onTierOneClick(View from, BubbleView bv) {
		from = (View) from.getParent();

		float rotation = from.getRotation();

		// setting rotation from 0 to 180
		if (rotation <= 90) {
			rotation += 90;
		} else if (rotation >= 270) {
			rotation = rotation - 270;
		}

		float pivotX = (2f * (rotation / 180f)) - 0.5f;
		float pivotY = (0.2f);

		if (rotation < 90) {
			pivotX -= pivotX / 5;
			pivotY = pivotY + (1 - (rotation / 90f)) / 15;
		} else if (rotation > 90) {
			pivotX += pivotX / 10;
			pivotY = pivotY + (1 - ((rotation - 90) / 90f)) / 15;
		}

		// Log.e(TAG, "rotation: " + rotation + " pivotX: " + pivotX +
		// " pivotY: " + pivotY);

		Animation anim = new ScaleAnimation(0.4f, 1, 0.4f, 1, Animation.RELATIVE_TO_SELF, pivotX,
				Animation.RELATIVE_TO_SELF, pivotY);
		anim.setDuration(StaticUtils.ANIMATION_DURATION);
		anim.setInterpolator(new AccelerateInterpolator());

		setTierZero(bv, anim, true);
		setTierOne(bv);

	}

	public void onImageClick(BubbleView bv) {
		if (t3.getVisibility() == View.VISIBLE) {
			closeThirdLayer();
			thirdLayer.clearThirdLayerImages();
			return;
		}
		thirdLayer.onImageClick(bv);

		t3.setVisibility(View.VISIBLE);
		Animation anim = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_PARENT, 0.5f,
				Animation.RELATIVE_TO_PARENT, 0.75f);
		anim.setInterpolator(new AccelerateInterpolator());
		anim.setDuration(StaticUtils.ANIMATION_DURATION);

		t3.startAnimation(anim);
	}

	public void closeThirdLayer() {
		Animation anim = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_PARENT, 0.5f,
				Animation.RELATIVE_TO_PARENT, 0.75f);
		anim.setInterpolator(new ReverseInterpolator());
		anim.setDuration(StaticUtils.ANIMATION_DURATION);
		anim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				t3.setVisibility(View.GONE);
			}
		});

		t3.startAnimation(anim);
	}

	@Override
	public boolean handleMessage(Message msg) {

		switch (SpacifyEvents.values()[msg.what]) {
		case AVATAR_LOGIN_SUCCESS:
			closeLoginFragment(null);
			break;
		case ALL_BUBBLES_FETCHED:
			meContextAdapter.addAll(cms.getBubblesFromCursor(this, cms.getBubblesAlwaysOnScreen()));
			meContextFragment.redraw();
			break;
		default:
			break;
		}

		return super.handleMessage(msg);
	}

	public void onCallToScreenClick(View v) {

		double y = 1;
		double x = 0;
		boolean show = true;

		switch (v.getId()) {
			case R.id.call_bubble_to_left:
				x = 0.1;
				break;
			case R.id.call_bubble_to_center:
				x = 0.5;
				break;
			default:
			case R.id.call_bubble_to_right:
				x = 0.9;
				break;
			case R.id.call_bubble_off_screen:
				show = false;
				break;
		}

		account.callToScreen(x, y, show);
		closeLoginFragment(null);

	}

	public void openLoginFragment(View bv) {
		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) bv.getLayoutParams();
		float xPosition = (((float) bv.getLeft() + (float) (params.width / 2)) / width);
		float yPosition = (((float) bv.getTop() + (float) (params.height / 2)) / height);

		if (loginFragment == null) {
			loginFragment = new LoginFragment();
		}
		changeFragment(R.id.login_layer, loginFragment);

		extraPopupFragment.setVisibility(View.VISIBLE);
		Log.d(TAG, "Opening Login. x: " + xPosition + " y: " + yPosition);
		Animation anim = openFromAnimation(xPosition, yPosition);
		closeLoginAnimation = closeToAnimation(xPosition, yPosition);
		extraPopupFragment.startAnimation(anim);
	}

	public void closeLoginFragment(View view) {
		if (extraPopupFragment.getVisibility() == View.VISIBLE && closeLoginAnimation != null) {
			closeLoginAnimation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					extraPopupFragment.setVisibility(View.GONE);
					if (loginFragment != null) {
						removeFragment(loginFragment);
					}
					if (callToScreenFragment != null) {
						removeFragment(callToScreenFragment);
					}
				}
			});

			extraPopupFragment.startAnimation(closeLoginAnimation);
			closeLoginAnimation = null;
		}
	}

	public void onOpenContentClick(View view) {
		final Button button = (Button) view;
		final double minHeight = (int) getResources().getDimension(R.dimen.content_height);
		final double startHeight = contentLayer.getHeight();
		final double fullHeight = history.getHeight();

		new Thread(new Runnable() {

			@Override
			public void run() {
				double startTime = System.currentTimeMillis();

				final LayoutParams params = (LayoutParams) contentLayer.getLayoutParams();

				double pulse = 0;
				while (pulse <= 1) {
					double diff = System.currentTimeMillis() - startTime;
					pulse = (diff / StaticUtils.ANIMATION_DURATION);

					if (startHeight > minHeight) {
						params.height = (int) ((startHeight * (1d - pulse)) + (minHeight * pulse));
					} else {
						params.height = (int) ((startHeight * (1d - pulse)) + (fullHeight * pulse));
					}
					contentLayer.post(new Runnable() {

						@Override
						public void run() {
							contentLayer.setLayoutParams(params);
						}
					});
					try {
						Thread.sleep(25);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (startHeight > minHeight) {
							params.height = (int) minHeight;
							button.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.arrow_down_float, 0);
							button.setText(R.string.content_open);
						} else {
							params.height = (int) fullHeight;
							button.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.arrow_up_float, 0);
							button.setText(R.string.content_close);
						}
					}
				});

			}
		}).start();
	}

	public void onTierZeroClick(View view) {
		boolean skipOpenContent = false;
		if (contentLayer.getVisibility() == View.VISIBLE) {
			closeContentView();
			skipOpenContent = true;
		}
		if (t3.getVisibility() == View.VISIBLE) {
			closeThirdLayer();
			thirdLayer.clearThirdLayerImages();
			skipOpenContent = true;
		}

		if (!skipOpenContent && tierZero != null) {
			BubbleView bv = tierZero.getBubbleView();
			JSONObject json = bv.getStyleOverrides();
			if (!TextUtils.isEmpty(bv.getContents())
					|| (json != null && StaticUtils.parseStringJSON(json, BubbleJSON.contentsImageUrl, null) != null)) {
				onImageClick(bv);
			}
		}
	}

	public void openCallToScreenPopup(View v) {
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();

		View parent = (View) v.getParent();
		// float xPosition = ((v.getX() + (params.width / 2)) / width);
		// float yPosition = ((v.getY() + (params.height / 2)) / height);
		float xPosition = 0.5f;
		float yPosition = 0.75f;

		callToScreenFragment = new CallToScreenFragment();
		changeFragment(R.id.login_layer, callToScreenFragment);

		extraPopupFragment.setVisibility(View.VISIBLE);
		// Log.d(TAG, "Opening call to screen. x: " + xPosition + " y: " +
		// yPosition);
		Animation anim = openFromAnimation(xPosition, yPosition);

		closeLoginAnimation = closeToAnimation(xPosition, yPosition);
		extraPopupFragment.startAnimation(anim);
	}

}
