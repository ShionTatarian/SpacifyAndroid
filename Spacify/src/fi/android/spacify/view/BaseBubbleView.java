package fi.android.spacify.view;

import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import fi.android.spacify.R;
import fi.android.spacify.activity.BubbleActivity;
import fi.android.spacify.fragment.BubbleFragment;
import fi.android.spacify.view.BubbleView.BubbleMovement;

@SuppressWarnings("javadoc")
public abstract class BaseBubbleView extends FrameLayout {

	public static final int MOVEMENT_TOUCH_TRESHOLD = 10;

	private final int DOUBLE_CLICK_DELAY = 300;
	
	protected String id;
	protected Activity activity;
	
	public View bubbleText;
	public TextView links;
	protected ImageView background;
	public int movement = BubbleMovement.INERT;
	public float diameter = 150;
	public int x, y;
	private int startX = 0, startY = 0;
	public double moved = 0;
	public boolean asMainContext = false;

	public BaseBubbleView(Activity act) {
		super(act);
		this.activity = act;
		LayoutInflater.from(act).inflate(getLayout(), this, true);
		background = (ImageView) findViewById(R.id.base_bubble_background);
		bubbleText = findViewById(R.id.base_bubble_bubble);
		links = (TextView) findViewById(R.id.base_bubble_link_count);

		setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP));
	}

	protected abstract int getLayout();

	protected void setText(String text) {
		if(bubbleText != null && bubbleText instanceof TextView) {
			((TextView) bubbleText).setText(text);
		}
	}
	
	public boolean onTouchDown() {
		boolean value = false;
		long currentTime = System.currentTimeMillis();
		if(currentTime - previousTouchDown <= DOUBLE_CLICK_DELAY) {
			value = true;
		}
		
		previousTouchDown = System.currentTimeMillis();
		
		moved = 0;
		movement = BubbleMovement.MOVING;
		LayoutParams params = (LayoutParams) getLayoutParams();
		startX = params.leftMargin;
		startY = params.topMargin;
		
		return value;
	}

	private long previousTouchDown = 0;

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
		int w = getWidth();
		if(w != 0) {
			diameter = w;
		}
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

		if(bubbleText != null) {
			RelativeLayout.LayoutParams bParams = (RelativeLayout.LayoutParams) bubbleText
					.getLayoutParams();
			bParams.width = d;
			bParams.height = d;
			bubbleText.setLayoutParams(bParams);
		}
	}

	public void move(int x, int y, int maxX, int maxY) {
		int radius = getRadius();
		this.x = (x + offsetX - radius);
		this.y = (y + offsetY - radius);

		if(this.x < 0) {
			this.x = 0;
		} else if((this.x + (radius * 2)) > maxX) {
			this.x = maxX - radius * 2;
		}
		if(this.y < 0) {
			this.y = 0;
		} else if((this.y + (radius * 2)) > maxY) {
			this.y = maxY - radius * 2;
		}

		LayoutParams params = (LayoutParams) getLayoutParams();
		
		if(this.x < 0) {
			this.x = 0;
		} else if(this.x > BubbleActivity.width) {
			this.x = BubbleActivity.width - (radius * 2);
		}
		if(this.y < 0) {
			this.y = 0;
		} else if(this.y > BubbleActivity.height) {
			this.y = BubbleActivity.height - (radius * 2);
		}
		
		Log.d("BVmove", "move[" + id + "]: x: " + this.x + " y: " + this.y);
		
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
		int radius = (getWidth() / 2);
		if(radius == 0) {
			radius = (int) (diameter / 2);
		}
		return radius;
	}

	/**
	 * Get id of this Bubble.
	 * 
	 * @return ID as integer
	 */
	public String getID() {
		return id;
	}

}
