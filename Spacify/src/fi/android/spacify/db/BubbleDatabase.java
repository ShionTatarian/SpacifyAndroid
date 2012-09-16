package fi.android.spacify.db;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import fi.android.spacify.model.Bubble;

public class BubbleDatabase extends SQLiteOpenHelper {

	private final String TAG = "SmartSpaceDatabase";
	
	private static BubbleDatabase instance;
	private Context ctx;
	
	private static final int VERSION = 1;
	private static final String DB_NAME = "smartspace.db";
	private static final String BUBBLE_TABLE = "bubble_tbl";

	
	@SuppressWarnings("javadoc")
	public static class BubbleColumns {
		public static final String ID = "_id";
		public static final String TITLE = "title";
		public static final String STYLE = "style";
		public static final String CONTENTS = "contents";
		public static final String PRIORITY = "priority";
		public static final String TITLE_IMAGE_URL = "title_image_url";
		public static final String LINKS = "links";
		public static final String TYPE = "type";
		public static final String DEBUG_ID = "debug_id";
		public static final String CONTENT_IMAGE_URL = "contents_image_url";
		public static final String LATITUDE = "latitude";
		public static final String LONGITUDE = "longitude";
		public static final String X = "position_x";
		public static final String Y = "position_y";
	}
	
	private BubbleDatabase(Context context) {
		super(context, DB_NAME, null, VERSION);
		this.ctx = context;
	}
	
	public static void init(Context context) {
		if(instance != null) {
			throw new IllegalStateException("SmartSpaceDatabase is already initialized");
		} else {
			instance = new BubbleDatabase(context);
		}
	}
	
	public static BubbleDatabase getInstance() {
		if(instance == null) {
			throw new IllegalStateException("SmartSpaceDatabase is not initialized");
		} else {
			return instance;
		}
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		StringBuilder sql = new StringBuilder();
		sql.append("CREATE TABLE ").append(BUBBLE_TABLE).append(" (");
		sql.append(BubbleColumns.ID).append(" INTEGER PRIMARY KEY,");
		sql.append(BubbleColumns.TITLE).append(" TEXT,");
		sql.append(BubbleColumns.STYLE).append(" TEXT,");
		sql.append(BubbleColumns.CONTENTS).append(" TEXT,");
		sql.append(BubbleColumns.PRIORITY).append(" INTEGER,");
		sql.append(BubbleColumns.TITLE_IMAGE_URL).append(" TEXT,");
		sql.append(BubbleColumns.LINKS).append(" TEXT,");
		sql.append(BubbleColumns.TYPE).append(" TEXT,");
		sql.append(BubbleColumns.DEBUG_ID).append(" TEXT,");
		sql.append(BubbleColumns.CONTENT_IMAGE_URL).append(" TEXT,");
		sql.append(BubbleColumns.LATITUDE).append(" INTEGER,");
		sql.append(BubbleColumns.LONGITUDE).append(" INTEGER,");
		sql.append(BubbleColumns.X).append(" INTEGER,");
		sql.append(BubbleColumns.Y).append(" INTEGER");
		sql.append(")");
		
		db.execSQL(sql.toString());
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}
	
	public void storeBubble(Bubble bubble) {
		SQLiteDatabase db = getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(BubbleColumns.ID, bubble.getID());
		values.put(BubbleColumns.TITLE, bubble.getTitle());
		values.put(BubbleColumns.STYLE, bubble.getStyle());
		values.put(BubbleColumns.CONTENTS, bubble.getContents());
		values.put(BubbleColumns.PRIORITY, bubble.getPriority());
		values.put(BubbleColumns.TITLE_IMAGE_URL, bubble.getTitleImageUrl());
		values.put(BubbleColumns.LINKS, bubble.getLinksJSONArray().toString());
		values.put(BubbleColumns.TYPE, bubble.getType());
		values.put(BubbleColumns.DEBUG_ID, bubble.getDebugID());
		values.put(BubbleColumns.CONTENT_IMAGE_URL, bubble.getContentImageUrl());
		values.put(BubbleColumns.LATITUDE, bubble.getLattitude());
		values.put(BubbleColumns.LONGITUDE, bubble.getLongitude());
		values.put(BubbleColumns.X, bubble.x);
		values.put(BubbleColumns.Y, bubble.y);
		
		long change = -1;
		
		try {
			change = db.insertOrThrow(BUBBLE_TABLE, null, values);
		} catch (SQLException e) {
			String where = BubbleColumns.ID + " = " + bubble.getID();
			change = db.update(BUBBLE_TABLE, values, where, null);
		}
		
		if(change != -1) {
			Log.v(TAG, "Bubble ["+bubble.getTitle()+"] stored to database.");
		}
	}
	
	public void storeBubbles(List<Bubble> bubbles) {
		SQLiteDatabase db = getWritableDatabase();

		db.beginTransaction();

		for(Bubble bubble : bubbles) {
			ContentValues values = new ContentValues();
			values.put(BubbleColumns.ID, bubble.getID());
			values.put(BubbleColumns.TITLE, bubble.getTitle());
			values.put(BubbleColumns.STYLE, bubble.getStyle());
			values.put(BubbleColumns.CONTENTS, bubble.getContents());
			values.put(BubbleColumns.PRIORITY, bubble.getPriority());
			values.put(BubbleColumns.TITLE_IMAGE_URL, bubble.getTitleImageUrl());
			values.put(BubbleColumns.LINKS, bubble.getLinksJSONArray().toString());
			values.put(BubbleColumns.TYPE, bubble.getType());
			values.put(BubbleColumns.DEBUG_ID, bubble.getDebugID());
			values.put(BubbleColumns.CONTENT_IMAGE_URL, bubble.getContentImageUrl());
			values.put(BubbleColumns.LATITUDE, bubble.getLattitude());
			values.put(BubbleColumns.LONGITUDE, bubble.getLongitude());
			values.put(BubbleColumns.X, bubble.x);
			values.put(BubbleColumns.Y, bubble.y);

			long change = -1;

			try {
				change = db.insertOrThrow(BUBBLE_TABLE, null, values);
			} catch(SQLException e) {
				String where = BubbleColumns.ID + " = " + bubble.getID();
				change = db.update(BUBBLE_TABLE, values, where, null);
			}
		}

		db.setTransactionSuccessful();
		db.endTransaction();
	}

	public Cursor getTopLevelBubblesCursor() {
		SQLiteDatabase db = getReadableDatabase();
		String selection = "";
		selection = BubbleColumns.PRIORITY + " > 2";
		
		return db.query(BUBBLE_TABLE, null, selection, null, null, null,
				BubbleColumns.TITLE);
	}

	public Cursor getLinkedBubblesCursor(List<Integer> links) {
		SQLiteDatabase db = getReadableDatabase();
		
		String separator = "";
		String linkString = "";
		for(Integer i : links) {
			linkString += separator + i;
			separator = ", ";
		}
		
		String selection = BubbleColumns.ID + " IN (" + linkString + ")";
		
		return db.query(BUBBLE_TABLE, null, selection, null, null, null,
				BubbleColumns.TITLE);
	}

}
