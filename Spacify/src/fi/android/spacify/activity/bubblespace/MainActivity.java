package fi.android.spacify.activity.bubblespace;

import android.os.Bundle;
import android.view.View;
import fi.android.spacify.R;
import fi.android.spacify.activity.BaseActivity;

public class MainActivity extends BaseActivity {

	public enum OpenTab {
		ME_TAB, MAP_TAB, SERVER_TAB;
	}

	private MeBubbleSpaceFragment meTab = new MeBubbleSpaceFragment();
	private OpenTab openTab = OpenTab.ME_TAB;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		
		onMeTabClick(null);
	}

	public void onMapTabClick(View view) {
		changeFragment(R.id.main_content, meTab);
	}

	public void onMeTabClick(View view) {
		changeFragment(R.id.main_content, meTab);
	}

	public void onServerTabClick(View view) {
		changeFragment(R.id.main_content, meTab);
	}

	@Override
	protected void onResume() {
		super.onResume();

		switch (openTab) {
		case MAP_TAB:

			break;
		case SERVER_TAB:

			break;
		case ME_TAB:
			meTab.updateBubbles();
		default:
			break;
		}

	}

	@Override
	protected void onPause() {
		super.onPause();

		switch (openTab) {
		case MAP_TAB:

			break;
		case SERVER_TAB:

			break;
		case ME_TAB:
			meTab.pauseSurface();
		default:
			break;
		}
	}

	@Override
	public void onBackPressed() {
		if (meTab.onBackPressed()) {
			return;
		}

		super.onBackPressed();
	}

}
