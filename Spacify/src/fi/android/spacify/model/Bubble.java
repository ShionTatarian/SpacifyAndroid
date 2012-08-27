package fi.android.spacify.model;

import org.json.JSONObject;

import android.graphics.Color;
import android.graphics.Paint;

public class Bubble {

	public int movement = BubbleMovement.INERT;

	public static class BubbleMovement {
		public static final int INERT = 0;
		public static final int MOVING = 1;
		public static final int AUTOMATIC = 2;
	}

	public static final double SPEED = 0.3;

	public long id;
	public int x, y;
	public float radius = 50;
	public Paint paint = new Paint();;

	public Bubble(long id) {
		this.id = id;

		int red = (int) (Math.random() * 255);
		int green = (int) (Math.random() * 255);
		int blue = (int) (Math.random() * 255);
		paint.setColor(Color.rgb(red, green, blue));
	}

	public Bubble(JSONObject json) {

	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof Bubble) {
			Bubble b = (Bubble) o;
			if(b.id == id) {
				return true;
			} else {
				return false;
			}
		}
		return super.equals(o);
	}

}
