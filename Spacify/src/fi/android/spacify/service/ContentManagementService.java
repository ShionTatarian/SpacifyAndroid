package fi.android.spacify.service;

import org.apache.http.client.methods.HttpGet;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import fi.android.service.web.WebJsonListener;
import fi.android.service.web.WebService;
import fi.android.service.web.WebServiceException;
import fi.android.spacify.R;
import fi.spacify.android.util.WebPipes;


/**
 * This singleton class handler connection between server and application.
 * 
 * @author Tommy
 * 
 */
public class ContentManagementService {

	private static final String TAG = "ContentManagementService";
	private final WebService WEB = WebService.getInstance();

	private static ContentManagementService instance;

	private Context context;

	private ContentManagementService(Context context) {
		this.context = context;
	}

	/**
	 * Get singleton instance of CMS.
	 * 
	 * @return ContentManagementService
	 */
	public static ContentManagementService getInstance() {
		if(instance == null) {
			throw new IllegalStateException(TAG + " not initialized!");
		}
		return instance;
	}

	/**
	 * Initializes CMS.
	 * 
	 * @param context
	 * 
	 */
	public static void init(Context context) {
		if(instance != null) {
			throw new IllegalStateException(TAG + " has already been initialized!");
		}
		instance = new ContentManagementService(context);
		instance.WEB.openPipe(WebPipes.BUBBLE_REQUEST_PIPE);
		instance.getBubbles();
	}

	private void getBubbles() {
		HttpGet get = new HttpGet(context.getResources().getString(R.string.url_cms_all_bubbles));
		
		WEB.requestJSON(get, bubbleParser, WebPipes.BUBBLE_REQUEST_PIPE);
		
		
	}

	private WebJsonListener bubbleParser = new WebJsonListener() {

		@Override
		public void successResult(JSONObject json) {
			Log.v(TAG, "Got bubbles: " + json);
		}

		@Override
		public void error(WebServiceException e) {
			Log.e(TAG, "Problem getting bubbles", e);
		}
	};

}
