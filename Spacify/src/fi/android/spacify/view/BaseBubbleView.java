package fi.android.spacify.view;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import fi.android.spacify.R;
import fi.android.spacify.activity.BubbleActivity;
import fi.android.spacify.fragment.BubbleFragment;
import fi.android.spacify.view.BubbleView.BubbleMovement;

@SuppressWarnings("javadoc")
public class BaseBubbleView extends FrameLayout {

	public static final int MOVEMENT_TOUCH_TRESHOLD = 10;

	public TextView bubble, links;
	public int movement = BubbleMovement.INERT;
	public float diameter = 100;
	public int x, y;
	private int startX, startY;
	public double moved = 0;
	public boolean asMainContext = false;

	public BaseBubbleView(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.base_bubble, this, true);
		bubble = (TextView) findViewById(R.id.base_bubble_bubble);
		links = (TextView) findViewById(R.id.base_bubble_link_count);

		setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				Gravity.CENTER));
	}

	protected void setText(String text) {
		bubble.setText(text);
	}
	
	public void onTouchDown() {
		moved = 0;
		movement = BubbleMovement.MOVING;
		LayoutParams params = (LayoutParams) getLayoutParams();
		startX = params.leftMargin;
		startY = params.topMargin;
	}

	public void onTouchUp() {
		offsetX = 0;
		offsetY = 0;
		movement = BubbleMovement.INERT;
		endZoom();
	}

	public int offsetX = 0;
	public int offsetY = 0;

	public void setTouchOffset(int tX, int tY) {
		int[] pos = getViewPosition();
		offsetX = pos[0] - tX;
		offsetY = pos[1] - tY;
	}

	public void endZoom() {
		diameter = bubble.getWidth();
	}

	public void setLinkCount(int count) {
		if(count == 0 || asMainContext) {
			links.setVisibility(View.GONE);
		} else {
			links.setVisibility(View.VISIBLE);
			links.setText("" + count);
		}
	}

	public void setSize(int d) {
		LayoutParams params = (LayoutParams) getLayoutParams();
		params.width = d;
		params.height = d;
		setLayoutParams(params);

		bubble.setWidth(d);
		bubble.setHeight(d);
		bubble.postInvalidate();
	}

	public void move(int x, int y) {
		int radius = getRadius();
		this.x = (x + offsetX - radius);
		this.y = (y + offsetY - radius);

		if(this.x < 0) {
			this.x = 0;
		} else if((this.x + (radius * 2)) > BubbleActivity.width) {
			this.x = BubbleActivity.width - radius * 2;
		}
		if(this.y < 0) {
			this.y = 0;
		} else if((this.y + (radius * 2)) > BubbleActivity.height) {
			this.y = BubbleActivity.height - radius * 2;
		}

		LayoutParams params = (LayoutParams) getLayoutParams();
		params.leftMargin = this.x;
		params.topMargin = this.y;
		setLayoutParams(params);

		double m = BubbleFragment.distance(this.x, this.y, startX, startY);
		if(m >= moved) {
			moved = m;
		}
	}

	public void zoom(double d) {
		moved = MOVEMENT_TOUCH_TRESHOLD * 2;
		double size = (diameter * d);
		setSize((int) size);
	}

	public int[] getViewPosition() {
		int[] position = new int[2];

		int radius = getRadius();
		position[0] = (getLeft() + radius);
		position[1] = (getTop() + radius);

		return position;
	}

	public int getRadius() {
		int radius = (bubble.getWidth() / 2);
		if(radius == 0) {
			radius = (int) (diameter / 2);
		}
		return radius;
	}

}
