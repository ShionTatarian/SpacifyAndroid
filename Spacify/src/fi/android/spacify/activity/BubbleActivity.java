package fi.android.spacify.activity;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import fi.android.spacify.R;
import fi.android.spacify.service.ContentManagementService;
import fi.android.spacify.view.BubbleView;

public class BubbleActivity extends BaseActivity {

	private final ContentManagementService cms = ContentManagementService.getInstance();
	private List<BubbleView> list = new ArrayList<BubbleView>();
	private ViewGroup root;
	private View background;
	private int height, width;
	private BubbleFragment activeBubbleFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bubble_layout);
		
		background = findViewById(R.id.bubble_background);

		DisplayMetrics metrics = new DisplayMetrics();
		Display display = getWindowManager().getDefaultDisplay();
		display.getMetrics(metrics);

		height = metrics.heightPixels;
		width = metrics.widthPixels;
		
		activeBubbleFragment = new BubbleFragment();
		
		root = (ViewGroup) findViewById(R.id.bubble_root);
		changeFragment(R.id.bubble_root, activeBubbleFragment);
	}

	public void onMeClick(View view) {
		activeBubbleFragment = new BubbleFragment();
		activeBubbleFragment.setBubbleCursor(cms.getBubblesWithPriority(2), this);
		changeFragment(R.id.bubble_root, activeBubbleFragment, R.anim.shrink_to_middle,
				R.anim.grow_from_middle);
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
}
