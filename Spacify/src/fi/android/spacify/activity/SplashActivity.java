package fi.android.spacify.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import fi.android.spacify.R;
import fi.android.spacify.db.BubbleDatabase;
import fi.android.spacify.db.BubbleDatabase.BubbleColumns;
import fi.android.spacify.service.AccountService;
import fi.android.spacify.service.ContentManagementService;
import fi.android.spacify.view.BubbleView.BubbleJSON;
import fi.qvik.android.util.ImageService;
import fi.qvik.android.util.ImageServiceEventListener;
import fi.qvik.android.util.WorkService;
import fi.spacify.android.util.SpacifyEvents;
import fi.spacify.android.util.StaticUtils;

public class SplashActivity extends BaseActivity implements ImageServiceEventListener {

	private final String TAG = "SplashActivity";

	private final BubbleDatabase db = BubbleDatabase.getInstance();
	private final ImageService is = ImageService.getInstance();
	private final WorkService ws = WorkService.getInstance();
	private ContentManagementService cms = ContentManagementService.getInstance();

	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);

		handler = new Handler();
		is.setEventListener(this);
		cms.fetchBubbles();
	}

	@Override
	public boolean handleMessage(Message msg) {

		switch (SpacifyEvents.values()[msg.what]) {
			case BUBBLE_FETCH_FAILED:
			case ALL_BUBBLES_FETCHED:
				AccountService.getInstance().storeFavoritesToDatabase();
				loadImages();
				// openBubbleActivityWithDelay(1000);
				break;
			default:
				break;
		}

		return super.handleMessage(msg);
	}

	private void openBubbleActivityWithDelay(int delay) {
		handler.removeCallbacks(goToBubbleActivity);
		handler.postDelayed(goToBubbleActivity, delay);
	}

	private Runnable goToBubbleActivity = new Runnable() {

		@Override
		public void run() {
			is.setEventListener(null);
			Intent intent = new Intent(SplashActivity.this, BubbleActivity.class);
			startActivity(intent);
			finish();
		}
	};

	private void loadImages() {
		ws.postWork(new Runnable() {
			
			@Override
			public void run() {
				Cursor c = db.getLinkedBubblesCursor(null);
				if(c == null) {
					return;
				}
				while(c.moveToNext()) {
					JSONObject styleOverrides = null;
					try {
						String style = c.getString(c.getColumnIndex(BubbleColumns.STYLE_OVERRIDES));
						if(style != null) {
							styleOverrides = new JSONObject(style);
						}
					} catch(JSONException e) {
						Log.w(TAG, "Could not get StyleOverrides", e);
					}

					if(styleOverrides != null) {
						String imageUrl = StaticUtils.parseStringJSON(styleOverrides,
								BubbleJSON.titleImageUrl, null);

						if(!TextUtils.isEmpty(imageUrl) && !is.hasImage(imageUrl)) {
							is.loadImage(imageUrl, imageUrl, StaticUtils.IMAGE_NORMAL, null, null);
						}
					}
				}

				openBubbleActivityWithDelay(2000);
			}
		});
	}

	@Override
	public void allImagesReady() {
		openBubbleActivityWithDelay(2000);
	}

	@Override
	public void imageAssignStarted(int tasksStarted) {
	}

	@Override
	public void imageDownloadStarted(int tasksStarted) {
		handler.removeCallbacks(goToBubbleActivity);
	}

}



