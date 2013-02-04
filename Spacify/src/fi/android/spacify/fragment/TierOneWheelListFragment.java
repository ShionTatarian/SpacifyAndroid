package fi.android.spacify.fragment;

import android.view.MotionEvent;
import android.view.View;
import fi.android.spacify.adapter.WheelAdapter.ViewHolder;

public class TierOneWheelListFragment extends WheelListFragment {

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		float x = event.getRawX();
		float y = event.getRawY();

		float nX = x - startX;
//		float nY = y - startY;
		tempRot = (((rotation + nX) / 30) % 360);

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				startX = x;
				startY = y;
				singleTouch = true;
				break;
			case MotionEvent.ACTION_MOVE:
				if(Math.abs(nX) >= 5) {
					singleTouch = false;
					startX = x;
					startY = y;
					rotate(nX);
					makeViewAtBottomInvisible(tempRot);
				}
				break;
			case MotionEvent.ACTION_UP:
				if(singleTouch) {
					ViewHolder h = (ViewHolder) v.getTag();
					if(h.position != -1) {
						adapter.onSingleClick(v, adapter.getItem(h.position));
					}
				}
				rotation = tempRot;
				break;
			default:
				break;
		}

		return true;
	}

}
