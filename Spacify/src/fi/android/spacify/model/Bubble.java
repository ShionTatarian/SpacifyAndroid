package fi.android.spacify.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.Log;
import fi.android.spacify.db.BubbleDatabase.BubbleColumns;

@SuppressWarnings("javadoc")
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
	private final int ZOOM_CAP = 3;
	
	public int movement = BubbleMovement.INERT;
	public int x = 0, y = 0;
	private float originalRadius = 30;
	public float radius = 30;
	public Paint bubblePaint, titlePaint;
	public boolean linkStatusChanged = false;

	private int priority, id;
	private String debugID = "", type = "", style = "", title = "", contents = "",
			titleImageUrl = "", contentImageUrl = "";
	private List<Integer> links = new ArrayList<Integer>();
	private long latitude = 0, longitude = 0;

	private void init() {
		int red = (int) (Math.random() * 255);
		int green = (int) (Math.random() * 255);
		int blue = (int) (Math.random() * 255);
		bubblePaint = new Paint();
		bubblePaint.setColor(Color.rgb(red, green, blue));
		bubblePaint.setAntiAlias(true);

		originalRadius = radius = (float) (radius + (priority * SIZE_FACTOR));
		titlePaint = new Paint();
		titlePaint.setColor(Color.WHITE);
		titlePaint.setStyle(Style.FILL);
		titlePaint.setTextSize(getTextSize());

	}

	public Bubble(int id) {
		this.id = id;
		init();
	}

	public Bubble(JSONObject json) {
		try {
			if(json.has(BubbleJSON.id)) {
				this.id = json.getInt(BubbleJSON.id);
			}
			if(json.has(BubbleJSON.priority)) {
				this.priority = json.getInt(BubbleJSON.priority);
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
				parseJsonLinks(jArray);
			}
		} catch(JSONException e) {
			Log.w(TAG, "Error parsing JSON", e);
		}

		init();
	}

	public Bubble(Cursor c) {
		id = c.getInt(c.getColumnIndex(BubbleColumns.ID));
		title = c.getString(c.getColumnIndex(BubbleColumns.TITLE));
		style = c.getString(c.getColumnIndex(BubbleColumns.STYLE));
		contents = c.getString(c.getColumnIndex(BubbleColumns.CONTENTS));
		priority = c.getInt(c.getColumnIndex(BubbleColumns.PRIORITY));
		titleImageUrl = c.getString(c.getColumnIndex(BubbleColumns.TITLE_IMAGE_URL));
		try {
			parseJsonLinks(new JSONArray(c.getString((c.getColumnIndex(BubbleColumns.LINKS)))));
		} catch(JSONException e) {
			e.printStackTrace();
		}
		type = c.getString(c.getColumnIndex(BubbleColumns.TYPE));
		debugID = c.getString(c.getColumnIndex(BubbleColumns.DEBUG_ID));
		contentImageUrl = c.getString(c.getColumnIndex(BubbleColumns.CONTENT_IMAGE_URL));
		latitude = c.getLong(c.getColumnIndex(BubbleColumns.LATITUDE));
		longitude = c.getLong(c.getColumnIndex(BubbleColumns.LONGITUDE));
		// x = c.getInt(c.getColumnIndex(BubbleColumns.X));
		// y = c.getInt(c.getColumnIndex(BubbleColumns.Y));
		
		init();
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
	public String getContents() {
		return contents;
	}

	public void setContents(String content) {
		this.contents = content;
	}

	/**
	 * Zoom bubble in or out.
	 * 
	 * @param d
	 */
	public void zoom(double d) {
		if(d > ZOOM_CAP) {
			// cap zoom at ZOOM_CAP
			return;
		}
		if(d < 0.5) {
			return;
		}

		radius = (float) (originalRadius * d);

		titlePaint.setTextSize(getTextSize());
		titleSplit.clear();
	}

	/**
	 * Sets the current size as the default size.
	 */
	public void endZoom() {
		originalRadius = radius;
	}

	private float getTextSize() {
		return(radius / 2.75f);
	}

	private int textFactor = 0;
	private List<String> titleSplit = new ArrayList<String>();

	private void calculateTextSizes() {
		textFactor = (int) (radius / Math.sqrt(2));

		float[] charWidths = new float[title.length()];
		titlePaint.getTextWidths(title, charWidths);

		String line = "";
		float total = 0;
		for(int i=0; i<title.length(); i++) {
			char c = title.charAt(i);
			if(total + charWidths[i] < textFactor * 2) {
				total += charWidths[i];
				line += c;
			} else {
				titleSplit.add(line);
				total = charWidths[i];
				line = "" + c;
			}
		}

		if(line.length() > 0) {
			titleSplit.add(line);
		}
	}

	public void onDraw(Canvas canvas) {
		if(titleSplit.size() == 0) {
			calculateTextSizes();
		}

		canvas.drawCircle(x, y, radius, bubblePaint);

		if(title != null) {
			int i = 0;
			for(String line : titleSplit) {
				int dx = x - textFactor;
				int dy = (int) ((y - (textFactor / 2)) + (i * getTextSize()));
				canvas.drawText(line, dx, dy, titlePaint);
				i += 1;

				if(i * getTextSize() >= textFactor * 2) {
					break;
				}
			}
		}
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getDebugID() {
		return debugID;
	}

	public void setDebugID(String debugID) {
		this.debugID = debugID;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public String getTitleImageUrl() {
		return titleImageUrl;
	}

	public void setTitleImageUrl(String titleImageUrl) {
		this.titleImageUrl = titleImageUrl;
	}

	public String getContentImageUrl() {
		return contentImageUrl;
	}

	public void setContentImageUrl(String contentImageUrl) {
		this.contentImageUrl = contentImageUrl;
	}

	public List<Integer> getLinks() {
		return links;
	}

	public void setLinks(List<Integer> links) {
		this.links = links;
	}

	public void addLink(int link) {
		if(!links.contains(link)) {
			this.links.add(link);
		}
	}

	public void removeLink(Integer link) {
		this.links.remove(link);
	}

	public JSONArray getLinksJSONArray() {
		JSONArray jArray = new JSONArray();
		for(int link : links) {
			jArray.put(link);
		}
		return jArray;
	}

	public void parseJsonLinks(JSONArray jArray) {
		for(int i = 0; i < jArray.length(); i++) {
			try {
				links.add(jArray.getInt(i));
			} catch(JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public long getLattitude() {
		return latitude;
	}

	public void setLattitude(long lattitude) {
		this.latitude = lattitude;
	}

	public long getLongitude() {
		return longitude;
	}

	public void setLongitude(long longitude) {
		this.longitude = longitude;
	}

	public int offsetX = 0;
	public int offsetY = 0;

	public void setTouchOffset(int tX, int tY) {
		offsetX = x - tX;
		offsetY = y - tY;
	}

	private final long ANIMATE_TOUCH = 2000;

	public void animateOnTouch() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO make better
				for(double i = 1; i < 50; i++) {
					double zoom = 1 + (i / 100);
					zoom(zoom);

					try {
						Thread.sleep(5);
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	public void animateOnUp() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO make better
				for(double i = 1; i < 50; i++) {
					double zoom = 1.5 - (i / 100);
					zoom(zoom);

					try {
						Thread.sleep(5);
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

}
