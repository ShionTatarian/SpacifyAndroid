package fi.android.spacify.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.Log;

public class Bubble {

	private final String TAG = "Bubble";

	public static class BubbleJSON {
		public static final String debugID = "debugId";
		public static final String id = "id";
		public static final String type = "type";
		public static final String priority = "priority";
		public static final String style = "style";
		public static final String title = "title";
		public static final String contents = "contents";
		public static final String links = "links";
		public static final String titleImageUrl = "titleImageUrl";
		public static final String contentsImageUrl = "contentsImageUrl";
	}

	public static class BubbleMovement {
		public static final int INERT = 0;
		public static final int MOVING = 1;
		public static final int AUTOMATIC = 2;
	}

	public static final double SPEED = 0.3, SIZE_FACTOR = 10;
	private static final int TEXT_SIZE = 20;
	
	public int movement = BubbleMovement.INERT;
	public int x, y;
	public float radius = 30;
	public Paint bubblePaint, titlePaint;

	private int priority, id;
	private String debugID = "", type = "", style = "", title = "", contents = "",
			titleImageUrl = "", contentImageUrl = "";
	private List<Integer> links = new ArrayList<Integer>();

	public Bubble(int id) {
		this.id = id;

		int red = (int) (Math.random() * 255);
		int green = (int) (Math.random() * 255);
		int blue = (int) (Math.random() * 255);
		bubblePaint.setColor(Color.rgb(red, green, blue));

		titlePaint = new Paint();
		titlePaint.setColor(Color.WHITE);
		titlePaint.setStyle(Style.FILL);
		titlePaint.setTextSize(TEXT_SIZE);
	}

	public Bubble(JSONObject json) {
		try {
			if(json.has(BubbleJSON.id)) {
				this.id = json.getInt(BubbleJSON.id);
			}
			if(json.has(BubbleJSON.priority)) {
				this.priority = json.getInt(BubbleJSON.priority);
				
				radius = (float) (radius + (priority * SIZE_FACTOR));
			}
			if(json.has(BubbleJSON.contents)) {
				this.contents = json.getString(BubbleJSON.contents);
			}
			if(json.has(BubbleJSON.contentsImageUrl)) {
				this.contentImageUrl = json.getString(BubbleJSON.contentsImageUrl);
			}
			if(json.has(BubbleJSON.debugID)) {
				this.debugID = json.getString(BubbleJSON.debugID);
			}
			if(json.has(BubbleJSON.style)) {
				this.style = json.getString(BubbleJSON.style);
			}
			if(json.has(BubbleJSON.title)) {
				this.title = json.getString(BubbleJSON.title);
			}
			if(json.has(BubbleJSON.titleImageUrl)) {
				this.titleImageUrl = json.getString(BubbleJSON.titleImageUrl);
			}
			if(json.has(BubbleJSON.links)) {
				JSONArray jArray = json.getJSONArray(BubbleJSON.links);
				for(int i = 0; i < jArray.length(); i++) {
					links.add(jArray.getInt(i));
				}
			}
		} catch(JSONException e) {
			Log.w(TAG, "Error parsing JSON", e);
		}

		int red = (int) (Math.random() * 255);
		int green = (int) (Math.random() * 255);
		int blue = (int) (Math.random() * 255);
		bubblePaint = new Paint();
		bubblePaint.setColor(Color.rgb(red, green, blue));

		titlePaint = new Paint();
		titlePaint.setColor(Color.WHITE);
		titlePaint.setStyle(Style.FILL);
		titlePaint.setTextSize(TEXT_SIZE);
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

	public void animateMoveTo(int x, int y) {

	}

	/**
	 * Get id of this Bubble.
	 * 
	 * @return ID as integer
	 */
	public int getID() {
		return id;
	}

	/**
	 * Get content of the bubble.
	 * 
	 * @return String content of this Bubble.
	 */
	public String getContent() {
		return contents;
	}

	public void onDraw(Canvas canvas) {
		canvas.drawCircle(x, y, radius, bubblePaint);
		
		if(title != null) {
			canvas.drawText(title, x - radius, y, titlePaint);
		}
		
	}

}
