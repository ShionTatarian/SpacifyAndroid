package fi.android.spacify.adapter;

import android.view.View;
import fi.android.spacify.activity.BubbleActivity;
import fi.android.spacify.view.BubbleView;

public class MeContextAdapter extends WheelAdapter {

	public MeContextAdapter(BubbleActivity context) {
		super(context);
	}

	@Override
	public void onSingleClick(View from, BubbleView bv) {
		bubbleAct.setTierZeroFromMeContext(from, bv);
	}

}
