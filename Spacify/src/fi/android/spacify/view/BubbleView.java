package fi.android.spacify.view;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;
import fi.android.spacify.R;
import fi.android.spacify.activity.BubbleActivity;
import fi.android.spacify.db.BubbleDatabase.BubbleColumns;
import fi.android.spacify.fragment.BubbleFragment;

@SuppressWarnings("javadoc")
public class BubbleView extends TextView {

	private final String TAG = "Bubble";

	public static final double ANIMATION_TIME = 500d;
	public static final int MOVEMENT_TOUCH_TRESHOLD = 10;

	public static class BubbleJSON {
		public static final String debugID = "debugId";
		public static final String id = "id";
		public static final String type = "type";
		public static final String priority = "priority";
		public static final String size = "size";
		public static final String style = "style";
		public static final String title = "title";
		public static final String contents = "contents";
		public static final String links = "links";
		public static final String titleImageUrl = "titleImageUrl";
		public static final String contentsImageUrl = "contentsImageUrl";
		public static final String context = "context";
	}

	public static class BubbleContexts {
		public static final String CMS = "cms";
		public static final String ME = "me";
		public static final String EVENTS = "events";
		public static final String PEOPLE = "people";
		public static final String GROUP = "group";
		public static final String INFORMATION = "information";
		public static final String PLACES = "places";
	}

	public static class BubbleMovement {
		public static final int INERT = 0;
		public static final int MOVING = 1;
		public static final int AUTOMATIC = 2;
	}

	private final int PADDING = 15;

	public static final double SPEED = 0.3, SIZE_FACTOR = 10;

	public int movement = BubbleMovement.INERT;
	public float diameter = 100;
	public boolean linkStatusChanged = false;
	public boolean lockedToPlase = false;

	public int x, y;

	private int priority, id;
	private String debugID = "", type = "", style = "", title = "", contents = "", titleImageUrl = "",
			contentImageUrl = "";
	private List<Integer> links = new ArrayList<Integer>();
	private long latitude = 0, longitude = 0;
	private Set<String> contexts = new HashSet<String>();

	private void init() {
		setBackgroundResource(R.drawable.lightblueball);
		setTextColor(Color.WHITE);
		setGravity(Gravity.CENTER);
		setPadding(PADDING, PADDING, PADDING, PADDING);
		zoom(1);
	}

	public BubbleView(Context context, int id) {
		super(context);
		this.id = id;
		init();
	}


	public BubbleView(Context context, Cursor c) {
		super(context);
		super.setLayoutParams(new LayoutParams(100, 100));
		
		id = c.getInt(c.getColumnIndex(BubbleColumns.ID));
		title = c.getString(c.getColumnIndex(BubbleColumns.TITLE));
		setText(title);
		style = c.getString(c.getColumnIndex(BubbleColumns.STYLE));
		contents = c.getString(c.getColumnIndex(BubbleColumns.CONTENTS));
		priority = c.getInt(c.getColumnIndex(BubbleColumns.PRIORITY));
		titleImageUrl = c.getString(c.getColumnIndex(BubbleColumns.TITLE_IMAGE_URL));
		try {
			String linksJSON = c.getString(c.getColumnIndex(BubbleColumns.LINKS));
			if(linksJSON != null) {
				parseJsonLinks(new JSONArray(linksJSON));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			JSONArray array = new JSONArray(c.getString(c.getColumnIndex(BubbleColumns.CONTEXT)));
			setContexts(array);
		} catch(JSONException e) {
			e.printStackTrace();
		}

		type = c.getString(c.getColumnIndex(BubbleColumns.TYPE));
		debugID = c.getString(c.getColumnIndex(BubbleColumns.DEBUG_ID));
		contentImageUrl = c.getString(c.getColumnIndex(BubbleColumns.CONTENT_IMAGE_URL));
		latitude = c.getLong(c.getColumnIndex(BubbleColumns.LATITUDE));
		longitude = c.getLong(c.getColumnIndex(BubbleColumns.LONGITUDE));

		x = c.getInt(c.getColumnIndex(BubbleColumns.X));
		y = c.getInt(c.getColumnIndex(BubbleColumns.Y));

		if(x != -1 && y != -1) {
			move(x, y);
		}

		init();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof BubbleView) {
			BubbleView b = (BubbleView) o;
			if (b.id == id) {
				return true;
			} else {
				return false;
			}
		}
		return super.equals(o);
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
		if (!links.contains(link)) {
			this.links.add(link);
		}
	}

	public void removeLink(Integer link) {
		this.links.remove(link);
	}

	public JSONArray getLinksJSONArray() {
		JSONArray jArray = new JSONArray();
		for (int link : links) {
			jArray.put(link);
		}
		return jArray;
	}

	public void parseJsonLinks(JSONArray jArray) {
		for (int i = 0; i < jArray.length(); i++) {
			try {
				links.add(jArray.getInt(i));
			} catch (JSONException e) {
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
		int[] pos = getViewPosition();
		offsetX = pos[0] - tX;
		offsetY = pos[1] - tY;
	}

	public void move(int x, int y) {
		this.y = y;
		LayoutParams params = (LayoutParams) getLayoutParams();
		int radius = params.width / 2;
		if(x - radius < 0) {
			x = radius;
		} else if(x + radius > BubbleActivity.width) {
			x = BubbleActivity.width - radius;
		}
		if(y - radius < 0) {
			y = radius;
		} else if(y + radius > BubbleActivity.height) {
			y = BubbleActivity.height - radius;
		}
		
		this.x = (x + offsetX - radius);
		this.y = (y + offsetY - radius);
		params.leftMargin = this.x;
		params.topMargin = this.y;
		setLayoutParams(params);

		double m = BubbleFragment.distance(x, y, startX, startY);
		if(m >= moved) {
			moved = m;
		}
	}

	public void zoom(double d) {
		moved = MOVEMENT_TOUCH_TRESHOLD * 2;

		LayoutParams params = (LayoutParams) getLayoutParams();
		params.width = (int) (diameter * d);
		params.height = (int) (diameter * d);
		setLayoutParams(params);
	}

	private int startX, startY;
	public double moved = 0;

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

	public void endZoom() {
		LayoutParams params = (LayoutParams) getLayoutParams();
		diameter = params.width;
	}

	public void setContext(String context) {
		contexts.add(context);
	}

	public void removeContext(String context) {
		contexts.remove(context);
	}

	public String getContextJSON() {
		JSONArray json = new JSONArray();
		for(String s : contexts) {
			json.put(s);
		}

		return json.toString();
	}

	public void translateContexts(String contextJson) {
		try {
			JSONArray json = new JSONArray(contextJson);
			for(int i = 0; i < json.length(); i++) {
				contexts.add(json.getString(i));
			}

		} catch(JSONException e) {
			Log.w(TAG, "Could not translate Context information from: " + contextJson, e);
		}
	}

	public int[] getViewPosition() {
		int[] position = new int[2];
		LayoutParams params = (LayoutParams) getLayoutParams();

		position[0] = (getLeft() + (params.width / 2));
		position[1] = (getTop() + (params.height / 2));

		return position;
	}

	public int getRadius() {
		LayoutParams params = (LayoutParams) getLayoutParams();
		return(params.width / 2);
	}

	public void setContexts(JSONArray array) {
		for(int i = 0; i < array.length(); i++) {
			try {
				contexts.add(array.getString(i));
			} catch(JSONException e) {
				e.printStackTrace();
			}
		}
	}

}
