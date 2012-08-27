package fi.android.spacify.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import fi.android.service.web.WebJsonListener;
import fi.android.service.web.WebService;
import fi.android.service.web.WebServiceException;
import fi.android.spacify.R;
import fi.android.spacify.model.Bubble;
import fi.spacify.android.util.Events;
import fi.spacify.android.util.WebPipes;


/**
 * This singleton class handler connection between server and application.
 * 
 * @author Tommy
 * 
 */
public class ContentManagementService extends BaseService {

	private static final String TAG = "ContentManagementService";
	private final WebService WEB = WebService.getInstance();

	private static ContentManagementService instance;
	private List<Bubble> bubbles = new ArrayList<Bubble>();

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
	}

	public void fetchBubbles() {
		HttpGet get = new HttpGet(context.getResources().getString(R.string.url_cms_all_bubbles));
		
		WEB.requestJSON(get, bubbleParser, WebPipes.BUBBLE_REQUEST_PIPE);
	}

	private WebJsonListener bubbleParser = new WebJsonListener() {

		@Override
		public void successResult(JSONObject json) {
			Log.v(TAG, "Got bubbles: " + json);
			
			try {
				JSONArray jArray = json.getJSONArray("add");

				for(int i = 0; i < jArray.length(); i++) {
					bubbles.add(new Bubble(jArray.getJSONObject(i)));
				}
			} catch(JSONException e) {
				e.printStackTrace();
			}

			es.dispatchEvent(Events.ALL_BUBBLES_FETCHED.ordinal());
		}

		@Override
		public void error(WebServiceException e) {
			Log.e(TAG, "Problem getting bubbles");
			e.printStackTrace();
		}
	};

	/**
	 * Get list of bubbles. May be empty.
	 * 
	 * @return List of Bubble objects.
	 */
	public List<Bubble> getBubbles() {
		return new ArrayList<Bubble>(bubbles);
	}

}
