package fi.android.spacify.view;

import java.util.List;

import android.app.Activity;
import android.database.Cursor;
import android.view.View;
import fi.android.spacify.R;
import fi.android.spacify.service.AccountService;

public class AvatarBubble extends BubbleView {

	protected View spinner;

	public AvatarBubble(Activity activity, Cursor c) {
		super(activity, c);
		spinner = findViewById(R.id.base_bubble_spinner);

		zoom(1.5d);
		diameter = 225;
	}

	public AvatarBubble(Activity activity, String id) {
		super(activity, id);
		spinner = findViewById(R.id.base_bubble_spinner);
		setText(activity.getString(R.string.login_login_button));

		zoom(1.5d);
		diameter = 225;
	}

	public void showSpinner(boolean show) {
		if(show) {
			spinner.setVisibility(View.VISIBLE);
		} else {
			spinner.setVisibility(View.GONE);
		}
	}

	@Override
	protected int getLayout() {
		return R.layout.avatar_bubble;
	}

	@Override
	public List<String> getLinks() {
		return AccountService.getInstance().getFavorites();
	}
}
