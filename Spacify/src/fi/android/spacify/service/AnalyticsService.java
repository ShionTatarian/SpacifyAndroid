package fi.android.spacify.service;

import org.json.JSONException;
import org.json.JSONObject;

import fi.android.spacify.db.BubbleDatabase;
import fi.android.spacify.view.BubbleView;

public class AnalyticsService {

	private final BubbleDatabase db = BubbleDatabase.getInstance();
	private static AnalyticsService instance;

	public static class ANALYTICS {
		public static final String EVENT = "event";
		public static final String BUBBLE = "bubble";
		public static final String BUBBLE_NAME = "bubble name";
	}

	public static class EVENTS {
		public static final String APPLICATION_STARTED = "application started";
		public static final String BUBBLE_OPENED = "bubble opened";
	}

	private AnalyticsService() {
		applicationStarted();
	}

	public static void init() {
		if(instance != null) {
			throw new IllegalStateException("SmartSpaceDatabase is already initialized");
		} else {
			instance = new AnalyticsService();
		}
	}

	public static AnalyticsService getInstance() {
		if(instance == null) {
			throw new IllegalStateException("SmartSpaceDatabase is not initialized");
		} else {
			return instance;
		}
	}

	private void applicationStarted() {
		JSONObject message = new JSONObject();
		try {
			message.put(ANALYTICS.EVENT, EVENTS.APPLICATION_STARTED);
		} catch(JSONException e) {
			// TODO: handle exception
		}
		db.analyticMessage(message);
	}

	public void bubbleOpened(BubbleView bv) {
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

}
