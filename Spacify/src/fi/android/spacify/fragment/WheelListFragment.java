package fi.android.spacify.fragment;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import fi.android.spacify.R;
import fi.android.spacify.activity.BubbleActivity;
import fi.android.spacify.adapter.WheelAdapter;
import fi.android.spacify.adapter.WheelAdapter.ViewHolder;

public class WheelListFragment extends BaseFragment implements OnTouchListener {

	private ViewGroup layout;
	private List<View> views = new ArrayList<View>();
	private WheelAdapter adapter;
	private float startX = 0, startY = 0;
	private int viewCount;
	private float angle;
	private float rotation = 0;
	private double padding = 55;
	private float pivotX, pivotY;
	private int tierSize;
	private int tierLevel;

	private int nextPosition = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		layout = (ViewGroup) inflater.inflate(R.layout.control_layout, container, false);

		pivotX = (BubbleActivity.width / 2);
		pivotY = BubbleActivity.height;

		setSize(tierSize);

		return layout;
	}

	public void setTierLevel(int level) {
		tierLevel = level;
	}

	public void setPadding(double padding) {
		this.padding = padding;
	}

	public void setTierSize(int tierSize) {
		this.tierSize = tierSize;
	}

	public void setAdapter(WheelAdapter adapter) {
		this.adapter = adapter;
	}

	public void setSize(double size) {
		layout.removeAllViews();

		int controlBubbleSize = adapter.getBubbleSize();
		double c = (Math.PI * size);
		viewCount = (int) Math.round(c / (controlBubbleSize + padding));
		angle = Math.round((360.0d / viewCount));

		for(int j = 0; j < viewCount; j++) {
			View v;
			ViewHolder h;
			if(adapter.getCount() > j) {
				v = adapter.getView(j, null, layout);
				nextPosition = j;
			} else {
				v = adapter.getEmptyView(layout);
				v.setVisibility(View.GONE);
			}
			v.setRotation((j * angle));
			h = (ViewHolder) v.getTag();
			h.touchArea.setOnTouchListener(this);
			h.touchArea.setTag(h);
			layout.addView(v);
			views.add(v);
		}
	}

	private float tempRot = 0;

	private boolean singleTouch = false;

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		float x = event.getRawX();
		float y = event.getRawY();

		float nX = x - startX;
		float nY = y - startY;
		tempRot = ((rotation + ((nX) / 30)) % 360);

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

	private View makeViewAtBottomInvisible(float rotation) {
		View retValue = getEmptyView();
		int target1 = (int) (180 - (angle / 2));
		int target2 = (int) (180 + (angle / 2));
		for(View v : views) {
			if(v != null && adapter.getCount() > viewCount) {
				if(retValue != null) {
					nextPosition += 1;
					if(nextPosition >= 0 && nextPosition < adapter.getCount()) {
						v = adapter.getView(nextPosition, v, layout);
						return retValue;
					}
				}

				float vRot = Math.abs((v.getRotation()) % 360);
				Boolean tag = (Boolean) v.getTag(R.drawable.ic_launcher);
				if(vRot >= target1 && vRot <= target2 && (tag == null || !tag)
						&& v.getVisibility() == View.VISIBLE) {
					v.setVisibility(View.INVISIBLE);
					if(this.rotation < rotation) {
						nextPosition += 1;
					} else {
						nextPosition -= 1;
					}

					if(nextPosition < 0) {
						nextPosition = adapter.getCount() - 1;
					} else if(nextPosition >= adapter.getCount()) {
						nextPosition = 0;
					}

					if(nextPosition > 0 && nextPosition < adapter.getCount()) {
						v = adapter.getView(nextPosition, v, layout);
					}

					Log.d("getFromAdapter", "next:" + nextPosition);
					v.setTag(R.drawable.ic_launcher, true);
					retValue = v;

				} else if((vRot < target1 || vRot > target2) && v.getVisibility() == View.INVISIBLE) {
					v.setVisibility(View.VISIBLE);
					v.setTag(R.drawable.ic_launcher, false);
				}
			}
		}
		return retValue;
	}

	private View getEmptyView() {
		for(View v : views) {
			ViewHolder h = (ViewHolder) v.getTag();
			if(h.position == -1) {
				return v;
			}
		}
		return null;
	}

	private void rotate(float rotation) {
		if(adapter.getCount() < viewCount) {
			int behindZero = visibleBehindZero();
			int afterZero = visiblePastZero();

			Log.d("rotate", "behind: " + behindZero + ", after: " + afterZero);

			if(rotation < 0 && afterZero > 0) {
				// return;
			} else if(rotation > 0 && behindZero > 0) {
				// return;
			} else {
				return;
			}
		}
		
		for(View v : views) {
			float rot = ((v.getRotation() + (rotation / 5)) % 360);
			v.setRotation(rot);
		}
	}

	private int visiblePastZero() {
		int visiblePastZero = 0;
		for(View v : views) {
			float rot = v.getRotation();
			if(v.getVisibility() == View.VISIBLE && rot > 0 && rot < 180) {
				Log.d("after", "HIT");
				visiblePastZero += 1;
			}
		}
		
		return visiblePastZero;
	}

	private int visibleBehindZero() {
		int behindZero = 0;
		for(View v : views) {
			float rot = v.getRotation();
			Log.d("behind", "rot: " + rot);
			if(v.getVisibility() == View.VISIBLE && ((rot < 360 && rot > 300) || rot < 0)) {
				behindZero += 1;
				Log.d("behind", "HIT");
			}
		}

		return behindZero;
	}

	public void redraw() {
		for(View v : views) {
			ViewHolder h = (ViewHolder) v.getTag();
			v = adapter.getView(h.position, v, layout);
		}
	}

}
