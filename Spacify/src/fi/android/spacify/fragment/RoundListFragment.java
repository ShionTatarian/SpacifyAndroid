package fi.android.spacify.fragment;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import fi.android.service.WorkService;
import fi.android.spacify.R;
import fi.android.spacify.adapter.RoundListAdapter;
import fi.android.spacify.adapter.RoundListAdapter.ViewHolder;
import fi.android.spacify.service.ContentManagementService;
import fi.android.spacify.view.BubbleView;
import fi.spacify.android.util.BaseSettings;
import fi.spacify.android.util.StaticUtils;


public class RoundListFragment extends BubbleControlFragment implements OnTouchListener {

	private final String BUBBLE_LIST_KEY = "bubble_list_key";

	private final WorkService ws = WorkService.getInstance();
	private final BaseSettings settings = BaseSettings.getInstance();
	private final ContentManagementService cms = ContentManagementService.getInstance();

	private float startX, startY;
	private int angle, count;
	private int lastPosition = 0;
	private float rotation = 0;

	private RoundListAdapter adapter;

	private JSONArray bubbleLinks;

	@Override
	public void setSize(int size) {
		layout.removeAllViews();

		int controlBubbleSize = (int) getResources().getDimension(R.dimen.popup_control);
		int c = (int) (Math.PI * size);
		count = c / (controlBubbleSize * 3 / 2);
		angle = 360 / count;

		try {
			bubbleLinks = new JSONArray(settings.loadString(BUBBLE_LIST_KEY, "[]"));
		} catch(JSONException e) {
			e.printStackTrace();
		}
		adapter = new RoundListAdapter(getActivity(), new ArrayList<BubbleView>());
		List<Integer> idList = new ArrayList<Integer>();
		for(int i = 0; i < bubbleLinks.length(); i++) {
			try {
				int id = bubbleLinks.getInt(i);
				idList.add(id);
			} catch(JSONException e) {
				e.printStackTrace();
			}
		}

		if(idList.size() > 0) {
			adapter.addAll(cms.getBubbles(idList));
		}

		int midStart = adapter.getMiddle();
		lastPosition = midStart;
		for(int j = 0; j < count; j++) {
			if(adapter.getRealCount() > j) {
				int position = midStart + j;
				View controlBubbleFrame = adapter.getView(position, null, layout);
				controlBubbleFrame.setRotation((position * angle));
				layout.addView(controlBubbleFrame);
				views.add(controlBubbleFrame);
				lastPosition += 1;
			} else {
				View empty = LayoutInflater.from(getActivity()).inflate(R.layout.control_bubble, layout, false);
				empty.setVisibility(View.GONE);
			}
		}
	}

	private float tempRot = 0;

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
				tempRot = ((rotation + ((x - startX) / 3)) % 360);
				v.setRotation(tempRot);
				makeViewAtBottomInvisible(tempRot);
				break;
			case MotionEvent.ACTION_UP:
				rotation = tempRot;
				break;
			default:
				break;
		}

		return true;
	}

	private boolean openTop = false;
	private boolean animationInProgress = false;

	public void toggleOpen(boolean shouldOpen) {
		if(animationInProgress) {
			return;
		}
		if(!shouldOpen && openTop) {
			animationInProgress = true;
			View invisible = views.remove(0);

			if(views.size() < count) {
				views.add(views.size(), invisible);
			} else {
				views.add(count / 2, invisible);
			}

			allToVisible();
			openTop = false;
			ws.postWork(close);
			return;
		} else if(shouldOpen && !openTop) {
			animationInProgress = true;
			openTop = true;

			View invisible = makeViewAtBottomInvisible(0);
			views.remove(invisible);
			views.add(0, invisible);

			ws.postWork(open);
		}
		

	}
	
	private void allToVisible() {
		for(View v : views) {
			if(v != null) {
				v.setVisibility(View.VISIBLE);
			}
		}
	}

	private Runnable close = new Runnable() {
		
		@Override
		public void run() {
			long stop = System.currentTimeMillis() + StaticUtils.ANIMATION_DURATION * 2;
			float rotation = layout.getRotation();
			while(System.currentTimeMillis() <= stop) {
				final double pulse = ((stop - System.currentTimeMillis()) / (float) (StaticUtils.ANIMATION_DURATION * 2));

				Log.d("close", "pulse: " + pulse);
				for(int i = 0; i < views.size(); i++) {
					final int j = i;
					final View v = views.get(i);
					if(v != null) {
						final float vRot = rotation + v.getRotation();
						v.post(new Runnable() {

							@Override
							public void run() {
								if(vRot < 180) {
									v.setRotation((float) ((j + 1) * angle + ((angle) * pulse)));
								} else {
									v.setRotation((float) ((j == 0 ? 1 : j + 1) * angle - ((angle) * pulse)));
								}
							}
						});
					}
				}
				try {
					Thread.sleep(25);
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
			animationInProgress = false;
		}
	};

	private Runnable open = new Runnable() {

		@Override
		public void run() {
			long stop = System.currentTimeMillis() + StaticUtils.ANIMATION_DURATION * 2;
			float rotation = layout.getRotation();
			while(System.currentTimeMillis() <= stop) {
				final double pulse = ((stop - System.currentTimeMillis()) / (float) (StaticUtils.ANIMATION_DURATION * 2));

				Log.d("open", "pulse: " + pulse);

				for(int i = 0; i < views.size(); i++) {
					final int j = i;
					final View v = views.get(i);
					if(v != null) {
						final float vRot = rotation + v.getRotation();
						v.post(new Runnable() {

							@Override
							public void run() {
								if(vRot < 180) {
									v.setRotation((float) ((j + 1) * angle - ((angle) * pulse)));
								} else {
									v.setRotation((float) ((j == 0 ? 1 : j + 1) * angle + ((angle) * pulse)));
								}
							}
						});
					}
				}
				try {
					Thread.sleep(25);
				} catch(InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			animationInProgress = false;
		}

	};

	private View makeViewAtBottomInvisible(float rotation) {
		View retValue = null;
		int target1 = (180 - (angle / 2));
		int target2 = (180 + (angle / 2));
		for(View v : views) {
			if(v != null) {
				float vRot = Math.abs((rotation + v.getRotation()) % 360);
				Boolean tag = (Boolean) v.getTag(R.drawable.ic_launcher);
				if(vRot >= target1 && vRot <= target2 && (tag == null || !tag)
						&& v.getVisibility() == View.VISIBLE) {
					v.setVisibility(View.INVISIBLE);
					layout.removeView(v);
					if(this.rotation < rotation) {
						lastPosition += 1;
						v = adapter.getView(lastPosition, v, layout);
					} else {
						lastPosition -= 1;
						v = adapter.getView(lastPosition, v, layout);
					}
					Log.d("getFromAdapter", "next:" + lastPosition);
					layout.addView(v);
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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);
		setSize((int) getResources().getDimension(R.dimen.context_round_list));

		return v;
	}

	public void addBubble(BubbleView bv) {
		ViewGroup parent = (ViewGroup) bv.getParent();
		if(parent != null) {
			parent.removeView(bv);
		}
		View v = null;
		if(views.size() < count) {
			adapter.addTo(0, bv);
			v = adapter.getView(0, v, layout);
		} else {
			v = views.remove(0);
			ViewHolder holder = (ViewHolder) views.get(0).getTag();
			int realPos = ((holder.position + 1) % adapter.getRealCount());
			adapter.addTo(realPos, bv);
			v = adapter.getView(realPos, v, layout);
			v.setVisibility(View.VISIBLE);
		}
		views.add(0, v);
		openTop = false;

		bubbleLinks.put(bv.getID());
		settings.storeString(BUBBLE_LIST_KEY, bubbleLinks.toString());
	}

}
