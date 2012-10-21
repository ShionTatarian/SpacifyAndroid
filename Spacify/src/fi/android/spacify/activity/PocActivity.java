package fi.android.spacify.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.os.Bundle;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import fi.android.spacify.R;
import fi.android.spacify.service.ContentManagementService;
import fi.android.spacify.view.BubbleView;
import fi.spacify.android.util.Events;

public class PocActivity extends BaseActivity implements OnTouchListener {

	private final ContentManagementService cms = ContentManagementService.getInstance();
	private List<BubbleView> list = new ArrayList<BubbleView>();
	private ViewGroup root;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bubble_layout);
		root = (ViewGroup) findViewById(R.id.bubble_root);

		updateBubbles();
	}

	private void updateBubbles() {
		Random r = new Random();
		for(BubbleView b : cms.getTopLevelBubbles()) {
			list.add(b);
			b.move(100 * r.nextInt(5), 100 * r.nextInt(5));
			b.setBackgroundResource(R.drawable.greenball);
			b.setOnTouchListener(this);
			root.addView(b);
		}

		root.invalidate();
	}

	@Override
	public boolean handleMessage(Message msg) {

		switch (Events.values()[msg.what]) {
			case COMICS_UPDATED:
			case ALL_BUBBLES_FETCHED:
				updateBubbles();
				return true;
			default:
				break;
		}

		return super.handleMessage(msg);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		float x = event.getRawX();
		float y = event.getRawY();

		BubbleView bv = (BubbleView) v;
		LayoutParams params = ((LayoutParams) v.getLayoutParams());
		float dx = 0;
		float dy = 0;
		int action = (event.getAction() & MotionEvent.ACTION_MASK);
		
		switch (action) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
				dx = params.leftMargin - x + (params.width / 2);
				dy = params.topMargin - y + (params.height / 2);
				bv.offsetX = (int) dx;
				bv.offsetY = (int) dy;
				break;
			case MotionEvent.ACTION_MOVE:
				bv.move((int) (x + bv.offsetX - (bv.getWidth() / 2)),
						(int) (y + bv.offsetY - (bv.getHeight() / 2)));
				testHit(bv);
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				break;
		}

		return true;
	}

	private void testHit(final BubbleView bv) {
		ws.postWork(new Runnable() {

			@Override
			public void run() {
				for(BubbleView b : list) {
					LayoutParams p1 = (LayoutParams) bv.getLayoutParams();
					LayoutParams p2 = (LayoutParams) b.getLayoutParams();
					if(isHit(p1, p2)) {
						autoMove(b, p1, p2);
					}
				}

			}
		});
	}

	private boolean isHit(LayoutParams p1, LayoutParams p2) {
		final double a = (p1.width / 2) + (p2.width / 2);
		final double dx = p1.leftMargin - p2.leftMargin;
		final double dy = p1.topMargin - p2.topMargin;
		return a * a > (dx * dx + dy * dy);
	}

	private void autoMove(final View v, LayoutParams moving, final LayoutParams pushed) {
		double distance = distance(moving, pushed);

		double dx = moving.leftMargin - pushed.leftMargin;
		double dy = moving.topMargin - pushed.topMargin;
		float radius = pushed.width / 2 > moving.width / 2 ? pushed.width / 2 : moving.width / 2;
		double move = Math.abs(distance - radius);

		double factor = 1;
		if(dx > 0 && dy > 0) {
			factor = dx < dy ? Math.abs(dx / dy) : 1 - Math.abs(dy / dx);
		} else if(dx > 0 && dy < 0) {
			factor = dx < Math.abs(dy) ? Math.abs(dx / dy) : 1 - Math.abs(dy / dx);
		} else if(dx < 0 && dy > 0) {
			factor = Math.abs(dx) < dy ? Math.abs(dx / dy) : 1 - Math.abs(dy / dx);
		} else {
			factor = dx > dy ? Math.abs(dx / dy) : 1 - Math.abs(dy / dx);
		}

		if(dx > 0) {
			pushed.leftMargin -= move * factor;
		} else if(dx < 0) {
			pushed.leftMargin += move * factor;
		}

		double rFactor = 1 - factor;
		if(dy > 0) {
			pushed.topMargin -= move * rFactor;
		} else if(dy < 0) {
			pushed.topMargin += move * rFactor;
		}

		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				v.setLayoutParams(pushed);
			}
		});
	}

	private double distance(LayoutParams b1, LayoutParams b2) {
		return distance(b1.leftMargin, b1.topMargin, b2.leftMargin, b2.topMargin);
	}

	private double distance(int x, int y, int bx, int by) {
		double dx2 = Math.pow(x - bx, 2);
		double dy2 = Math.pow(y - by, 2);

		return Math.sqrt(dx2 + dy2);
	}
}
