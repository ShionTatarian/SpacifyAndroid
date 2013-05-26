package fi.android.spacify.db;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import fi.android.spacify.view.BubbleView;
import fi.android.spacify.view.BubbleView.BubbleContexts;
import fi.android.spacify.view.BubbleView.BubbleJSON;
import fi.spacify.android.util.StaticUtils;

public class BubbleDatabase extends SQLiteOpenHelper {

	private final String TAG = "SmartSpaceDatabase";

	private static BubbleDatabase instance;
	private Context ctx;

	private static final int VERSION = 1;
	private static final String DB_NAME = "smartspace.db";
	private static final String BUBBLE_TABLE = "bubble_tbl";
	private static final String ANALYTICS_TABLE = "analytics_tbl";


	@SuppressWarnings("javadoc")
	public static class BubbleColumns {
		public static final String ID = "_id";
		public static final String TITLE = "title";
		public static final String NAME = "name";
		public static final String STYLE = "style";
		public static final String CONTENTS = "contents";
		public static final String SIZE = "priority";
		public static final String TITLE_IMAGE_URL = "title_image_url";
		public static final String LINKS = "links";
		public static final String STYLE_OVERRIDES = "style_overrides";
		public static final String TYPE = "type";
		public static final String DEBUG_ID = "debug_id";
		public static final String CONTENT_IMAGE_URL = "contents_image_url";
		public static final String LATITUDE = "latitude";
		public static final String LONGITUDE = "longitude";
		public static final String X = "position_x";
		public static final String Y = "position_y";
		public static final String ALWAYS_ON_SCREEN = "always_on_screen";

		// Custom fields
		public static final String CONTEXT = "context";
	}

	public static class AnalyticsColumns {
		public static final String TIME = "_id";
		public static final String JSON = "title";
	}

	private BubbleDatabase(Context context) {
		super(context, DB_NAME, null, VERSION);
		this.ctx = context;
	}

	public static void init(Context context) {
		if (instance != null) {
			throw new IllegalStateException("SmartSpaceDatabase is already initialized");
		} else {
			instance = new BubbleDatabase(context);
		}
	}

	public static BubbleDatabase getInstance() {
		if (instance == null) {
			throw new IllegalStateException("SmartSpaceDatabase is not initialized");
		} else {
			return instance;
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		StringBuilder sql = new StringBuilder();
		sql.append("CREATE TABLE ").append(BUBBLE_TABLE).append(" (");
		sql.append(BubbleColumns.ID).append(" TEXT PRIMARY KEY,");
		sql.append(BubbleColumns.TITLE).append(" TEXT,");
		sql.append(BubbleColumns.NAME).append(" TEXT,");
		sql.append(BubbleColumns.STYLE).append(" TEXT,");
		sql.append(BubbleColumns.CONTENTS).append(" TEXT,");
		sql.append(BubbleColumns.SIZE).append(" INTEGER,");
		sql.append(BubbleColumns.TITLE_IMAGE_URL).append(" TEXT,");
		sql.append(BubbleColumns.LINKS).append(" TEXT,");
		sql.append(BubbleColumns.STYLE_OVERRIDES).append(" TEXT,");
		sql.append(BubbleColumns.TYPE).append(" TEXT,");
		sql.append(BubbleColumns.DEBUG_ID).append(" TEXT,");
		sql.append(BubbleColumns.CONTENT_IMAGE_URL).append(" TEXT,");
		sql.append(BubbleColumns.LATITUDE).append(" INTEGER,");
		sql.append(BubbleColumns.LONGITUDE).append(" INTEGER,");
		sql.append(BubbleColumns.X).append(" INTEGER,");
		sql.append(BubbleColumns.Y).append(" INTEGER,");
		sql.append(BubbleColumns.ALWAYS_ON_SCREEN).append(" INTEGER,");
		sql.append(BubbleColumns.CONTEXT).append(" TEXT");
		sql.append(")");

		db.execSQL(sql.toString());

		sql = new StringBuilder();
		sql.append("CREATE TABLE ").append(ANALYTICS_TABLE).append(" (");
		sql.append(AnalyticsColumns.TIME).append(" INTEGER PRIMARY KEY,");
		sql.append(AnalyticsColumns.JSON).append(" TEXT");
		sql.append(")");

		db.execSQL(sql.toString());
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	public void storeBubbleViews(List<BubbleView> bubbles) {
		SQLiteDatabase db = getWritableDatabase();

		db.beginTransaction();

		for (BubbleView b : bubbles) {
			ContentValues values = new ContentValues();
			String id = b.getID();
			if (!TextUtils.isEmpty(id)) {
				values.put(BubbleColumns.ID, id);
				values.put(BubbleColumns.TITLE, b.getTitle());
				values.put(BubbleColumns.STYLE, b.getStyle());
				values.put(BubbleColumns.CONTENTS, b.getContents());
				values.put(BubbleColumns.SIZE, b.getPriority());
				values.put(BubbleColumns.TITLE_IMAGE_URL, b.getTitleImageUrl());
				values.put(BubbleColumns.LINKS, b.getLinksJSONArray().toString());
				values.put(BubbleColumns.TYPE, b.getType());
				values.put(BubbleColumns.DEBUG_ID, b.getDebugID());
				values.put(BubbleColumns.CONTENT_IMAGE_URL, b.getContentImageUrl());
				values.put(BubbleColumns.LATITUDE, b.getLattitude());
				values.put(BubbleColumns.LONGITUDE, b.getLongitude());
				int[] position = b.getViewPosition();
				values.put(BubbleColumns.X, position[0]);
				values.put(BubbleColumns.Y, position[1]);
				values.put(BubbleColumns.ALWAYS_ON_SCREEN, b.isAlwaysVisible());
				values.put(BubbleColumns.CONTEXT, b.getContextJSON());

				long change = -1;

				try {
					change = db.insertOrThrow(BUBBLE_TABLE, null, values);
				} catch (SQLException e) {
					String where = BubbleColumns.ID + " = '" + b.getID() + "'";
					change = db.update(BUBBLE_TABLE, values, where, null);
				}
			}
		}

		db.setTransactionSuccessful();
		db.endTransaction();
	}

	public Cursor getTopLevelBubblesCursor() {
		SQLiteDatabase db = getReadableDatabase();
		String selection = "";
		selection = BubbleColumns.SIZE + " > 0";

		return db.query(BUBBLE_TABLE, null, selection, null, null, null, BubbleColumns.TITLE);
	}

	public Cursor getBubblesWithPriority(int priority) {
		SQLiteDatabase db = getReadableDatabase();
		String selection = "";
		selection = BubbleColumns.SIZE + " > " + priority;

		return db.query(BUBBLE_TABLE, null, selection, null, null, null, BubbleColumns.TITLE);
	}

	public Cursor getBubblesAlwaysOnScreen() {
		SQLiteDatabase db = getReadableDatabase();
		String selection = "";
		selection = BubbleColumns.ALWAYS_ON_SCREEN + " = 1";

		return db.query(true, BUBBLE_TABLE, null, selection, null, null, null, BubbleColumns.SIZE, "10");
	}

	public Cursor getBubblesInContext(String context) {
		SQLiteDatabase db = getReadableDatabase();
		String sql = "SELECT * FROM " + BUBBLE_TABLE + " WHERE " + BubbleColumns.CONTEXT + " LIKE '%" + context + "%'";
		Cursor c = db.rawQuery(sql, null);
		return c;
	}

	public Cursor getLinkedBubblesCursor(List<String> links) {
		SQLiteDatabase db = getReadableDatabase();

		String selection = null;
		if (links != null) {
			String separator = "";
			String linkString = "";
			for (String link : links) {
				linkString += separator + "'" + link + "'";
				separator = ", ";
			}

			selection = BubbleColumns.ID + " IN (" + linkString + ")";
		}
		return db.query(BUBBLE_TABLE, null, selection, null, null, null, BubbleColumns.TITLE);
	}

	public void storeBubbleJson(JSONObject json) {
		SQLiteDatabase db = getWritableDatabase();

		db.beginTransaction();

		try {
			JSONArray jArray = json.getJSONArray("add");

			for (int i = 0; i < jArray.length(); i++) {
				JSONObject b = jArray.getJSONObject(i);
				ContentValues values = new ContentValues();
				String id = StaticUtils.parseStringJSON(b, BubbleJSON.id, "");
				if (!TextUtils.isEmpty(id)) {
					values.put(BubbleColumns.ID, id);
					values.put(BubbleColumns.TITLE, StaticUtils.parseStringJSON(b, BubbleJSON.title, ""));
					values.put(BubbleColumns.NAME, StaticUtils.parseStringJSON(b, BubbleJSON.name, ""));
					values.put(BubbleColumns.STYLE, StaticUtils.parseStringJSON(b, BubbleJSON.style, ""));
					values.put(BubbleColumns.CONTENTS, StaticUtils.parseStringJSON(b, BubbleJSON.contents, ""));
					if (b.has(BubbleJSON.priority)) {
						values.put(BubbleColumns.SIZE, StaticUtils.parseIntJSON(b, BubbleJSON.priority, -1));
					} else if (b.has(BubbleJSON.size)) {
						values.put(BubbleColumns.SIZE, StaticUtils.parseIntJSON(b, BubbleJSON.size, -1));
					}
					values.put(BubbleColumns.TITLE_IMAGE_URL,
							StaticUtils.parseStringJSON(b, BubbleJSON.titleImageUrl, ""));
					if (b.has(BubbleJSON.links)) {
						values.put(BubbleColumns.LINKS, b.getJSONArray(BubbleJSON.links).toString());
					}
					if (b.has(BubbleJSON.context)) {
						values.put(BubbleColumns.CONTEXT, b.getJSONArray(BubbleJSON.context).toString());
					} else {
						values.put(BubbleColumns.CONTEXT, "[" + BubbleContexts.CMS + "]");
					}
					values.put(BubbleColumns.TYPE, StaticUtils.parseStringJSON(b, BubbleJSON.type, ""));
					values.put(BubbleColumns.DEBUG_ID, StaticUtils.parseStringJSON(b, BubbleJSON.debugID, ""));
					values.put(BubbleColumns.CONTENT_IMAGE_URL,
							StaticUtils.parseStringJSON(b, BubbleJSON.contentsImageUrl, ""));
					values.put(BubbleColumns.LATITUDE, 0);
					values.put(BubbleColumns.LONGITUDE, 0);
					values.put(BubbleColumns.ALWAYS_ON_SCREEN,
							StaticUtils.parseBooleanJSON(b, BubbleJSON.alwaysOnScreen, false) ? 1 : 0);
					values.put(BubbleColumns.STYLE_OVERRIDES, b.getJSONObject(BubbleJSON.styleOverrides).toString());

					try {
						db.insertOrThrow(BUBBLE_TABLE, null, values);
					} catch (SQLException e) {
						String where = BubbleColumns.ID + " = '" + id + "'";
						db.update(BUBBLE_TABLE, values, where, null);
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		db.setTransactionSuccessful();
		db.endTransaction();
	}

	public Cursor getBubbleSearch(CharSequence constraint) {
		SQLiteDatabase db = getReadableDatabase();
		String sql = "SELECT * FROM " + BUBBLE_TABLE + " WHERE " + 
		BubbleColumns.TITLE + " LIKE '%"+ constraint + "%' OR " + 
		BubbleColumns.CONTENTS + " LIKE '%" + constraint+ "%' OR " + 
				// BubbleColumns.CONTEXT + " LIKE '%" + constraint + "%' OR" +
		BubbleColumns.NAME + " LIKE '%" + constraint + "%'";
		Cursor c = db.rawQuery(sql, null);
		return c;
	}

	public void updateAvatarLink(String avatarBubbleID, JSONArray avatarLinkJSON) {
		SQLiteDatabase db = getWritableDatabase();

		db.beginTransaction();

		ContentValues values = new ContentValues();
		values.put(BubbleColumns.LINKS, avatarLinkJSON.toString());

		String where = BubbleColumns.ID + " = '" + avatarBubbleID + "'";
		db.update(BUBBLE_TABLE, values, where, null);

		db.setTransactionSuccessful();
		db.endTransaction();
	}

	private long previousAnalyticTimeStamp = Long.MIN_VALUE;

	public void analyticMessage(JSONObject message) {
		long time = System.currentTimeMillis();
		if(time == previousAnalyticTimeStamp) {
			time = previousAnalyticTimeStamp+1;
		}
		previousAnalyticTimeStamp = time;
		ContentValues values = new ContentValues();
		values.put(AnalyticsColumns.TIME, time);
		values.put(AnalyticsColumns.JSON, message.toString());

		SQLiteDatabase db = getWritableDatabase();
		db.insertOrThrow(ANALYTICS_TABLE, null, values);
	}

	public void removeAvatarLink(String avatarBubbleID, BubbleView bv) {
		// TODO Auto-generated method stub

	}

	public Cursor getAnalyticsCursor() {
		SQLiteDatabase db = getReadableDatabase();

		return db.query(ANALYTICS_TABLE, null, null, null, null, null, AnalyticsColumns.TIME);
	}

}
