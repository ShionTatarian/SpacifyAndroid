package fi.android.spacify.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import fi.android.spacify.R;


public class RoundListFragment extends BubbleControlFragment implements OnTouchListener {

	float startX, startY;

	@Override
	public void setSize(int size) {
		layout.removeAllViews();

		int controlBubbleSize = (int) getResources().getDimension(R.dimen.popup_control);
		int c = (int) (Math.PI * size);
		int count = c / (controlBubbleSize * 3 / 2);
		int angle = 360 / count;

		for(int i = 0; i < count; i++) {
			View controlBubbleFrame = LayoutInflater.from(getActivity()).inflate(
					R.layout.control_bubble, layout, false);
			controlBubbleFrame.setRotation((i * angle));
			layout.addView(controlBubbleFrame);
			views.add(controlBubbleFrame);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		float x = event.getRawX();
		float y = event.getRawY();

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				startX = x;
				startY = y;
				break;
			case MotionEvent.ACTION_MOVE:
				float move = (x - startX) / 3;

				v.setRotation(move);
			default:
				break;
		}

		return true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);
		setSize((int) getResources().getDimension(R.dimen.context_round_list));

		return v;
	}

}
