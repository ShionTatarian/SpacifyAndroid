package fi.android.spacify.service;

import java.io.File;
import java.io.FileWriter;

import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.os.Environment;
import android.util.Log;
import fi.android.spacify.db.BubbleDatabase;
import fi.android.spacify.db.BubbleDatabase.AnalyticsColumns;
import fi.android.spacify.view.BubbleView;
import fi.qvik.android.util.WorkService;

public class AnalyticsService {

	private static final String TAG = "AnalyticsService";

	private final BubbleDatabase db = BubbleDatabase.getInstance();
	private final WorkService ws = WorkService.getInstance();
	private static AnalyticsService instance;

	public static class ANALYTICS {
		public static final String EVENT = "event";
		public static final String BUBBLE = "bubble";
		public static final String BUBBLE_NAME = "bubble name";
		public static final String MESSAGE = "message";
	}

	public static class EVENTS {
		public static final String APPLICATION_STARTED = "application started";
		public static final String BUBBLE_OPENED = "bubble opened";
		public static final String BUBBLE_CLOSED = "bubble closed";
		public static final String CHILDREN_CLOSED = "children closed";
		public static final String CHILDREN_OPENED = "children opened";
		public static final String ME_CLICKED = "ME button clicked";
		public static final String CUSTOM_MESSAGE = "custom message";
	}

	private AnalyticsService() {
		applicationStarted();
	}

	public static void init() {
		if(instance != null) {
			throw new IllegalStateException(TAG + "SmartSpaceDatabase is already initialized");
		} else {
			instance = new AnalyticsService();
		}
	}

	public static AnalyticsService getInstance() {
		if(instance == null) {
			throw new IllegalStateException(TAG + " is not initialized");
		} else {
			return instance;
		}
	}

	private void applicationStarted() {
		ws.postWork(new Runnable() {

			@Override
			public void run() {
				JSONObject message = new JSONObject();
				try {
					message.put(ANALYTICS.EVENT, EVENTS.APPLICATION_STARTED);
				} catch(JSONException e) {
					// TODO: handle exception
				}
				db.analyticMessage(message);
			}
		});
	}

	public void bubbleOpened(final BubbleView bv) {
		ws.postWork(new Runnable() {

			@Override
			public void run() {
				JSONObject message = new JSONObject();
				try {
					message.put(ANALYTICS.BUBBLE, bv.getID());
					message.put(ANALYTICS.BUBBLE_NAME, bv.getTitle());
					message.put(ANALYTICS.EVENT, EVENTS.BUBBLE_OPENED);
				} catch(JSONException e) {
					// TODO: handle exception
				}
				db.analyticMessage(message);
			}
		});
	}

	public void bubbleChildrenClosed(final BubbleView bv) {
		ws.postWork(new Runnable() {

			@Override
			public void run() {
				JSONObject message = new JSONObject();
				try {
					message.put(ANALYTICS.BUBBLE, bv.getID());
					message.put(ANALYTICS.BUBBLE_NAME, bv.getTitle());
					message.put(ANALYTICS.EVENT, EVENTS.CHILDREN_CLOSED);
				} catch(JSONException e) {
					// TODO: handle exception
				}
				db.analyticMessage(message);
			}
		});
	}

	public void bubbleChildrenOpened(final BubbleView bv) {
		ws.postWork(new Runnable() {

			@Override
			public void run() {
				JSONObject message = new JSONObject();
				try {
					message.put(ANALYTICS.BUBBLE, bv.getID());
					message.put(ANALYTICS.BUBBLE_NAME, bv.getTitle());
					message.put(ANALYTICS.EVENT, EVENTS.CHILDREN_OPENED);
				} catch(JSONException e) {
					// TODO: handle exception
				}
				db.analyticMessage(message);
			}
		});
	}

	public void meClicked() {
		ws.postWork(new Runnable() {

			@Override
			public void run() {
				JSONObject message = new JSONObject();
				try {
					message.put(ANALYTICS.EVENT, EVENTS.ME_CLICKED);
				} catch(JSONException e) {
					// TODO: handle exception
				}
				db.analyticMessage(message);
			}
		});
	}


	public void writeAnalyticsToFile() {
		Log.d(TAG, "Analytics writing started");
		ws.postWork(new Runnable() {

			@Override
			public void run() {
				Log.d(TAG, "Analytics writing thread started");
				File sdCard = Environment.getExternalStorageDirectory();
				File dir = new File(sdCard.getAbsolutePath() + "/Spaceify");
				dir.mkdirs();
				File file = new File(dir, "analytics.txt");

				Cursor c = db.getAnalyticsCursor();

				try {
					FileWriter write = new FileWriter(file.getAbsolutePath());

					while(c.moveToNext()) {
						long time = c.getLong(c.getColumnIndex(AnalyticsColumns.TIME));
						String json = c.getString(c.getColumnIndex(AnalyticsColumns.JSON));
						write.append(time + ": " + json);
						write.append("\n");
					}
					write.flush();
					Log.d(TAG, "Analytics written");
				} catch(Exception e) {
					Log.e(TAG, "Analytic writing error: ", e);
				}
				c.close();
			}
		});
	}

	public void storeAnalyticMessage(final String customMessage) {
		ws.postWork(new Runnable() {

			@Override
			public void run() {
				JSONObject message = new JSONObject();
				try {
					message.put(ANALYTICS.MESSAGE, customMessage);
					message.put(ANALYTICS.EVENT, EVENTS.CUSTOM_MESSAGE);
				} catch(JSONException e) {
					// TODO: handle exception
				}
				db.analyticMessage(message);
			}
		});
	}

}
