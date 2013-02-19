package fi.android.spacify.service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import fi.android.spacify.R;
import fi.android.spacify.db.BubbleDatabase;
import fi.android.spacify.view.BubbleView;
import fi.qvik.service.http.HttpJSONListener;
import fi.qvik.service.http.QvikException;
import fi.qvik.service.http.QvikHttpService;
import fi.qvik.service.http.QvikHttpServiceInterface;
import fi.spacify.android.util.BaseSettings;
import fi.spacify.android.util.BaseSettings.Preferences;
import fi.spacify.android.util.SpacifyEvents;
import fi.spacify.android.util.StaticUtils;

public class AccountService extends BaseService {

	private final static String TAG = "AccountService";

	private final BaseSettings settings = BaseSettings.getInstance();
	private final QvikHttpServiceInterface web = QvikHttpService.getInstance();
	private final BubbleDatabase db = BubbleDatabase.getInstance();

	private Context context;
	private List<String> favorites = new ArrayList<String>();
	private static AccountService instance;
	private String userNick = null;
	private String userFirstName = null;
	private String userLastName = null;
	private String avatarBubbleID = null;

	private AccountService(Context ctx) {
		context = ctx;
		userNick = settings.loadString(Preferences.USER_NICK, null);
		userFirstName = settings.loadString(Preferences.USER_FIRST_NAME, null);
		userLastName = settings.loadString(Preferences.USER_LAST_NAME, null);
		avatarBubbleID = settings.loadString(Preferences.AVATAR_ID, null);

		loadAvatarLinks();
	}

	public static void init(Context ctx) {
		if(instance != null) {
			throw new IllegalStateException(TAG + " already initialized!");
		}
		instance = new AccountService(ctx);
	}

	public static AccountService getInstance() {
		if(instance == null) {
			throw new IllegalStateException(TAG + " has not been initialized!");
		}
		return instance;
	}

	public boolean isLoggedIn() {
		return avatarBubbleID != null;
	}

	public String getAvatarBubbleID() {
		return avatarBubbleID;
	}

	public void login(final String nick) {

		JSONObject avatarJSON = new JSONObject();
		try {
			avatarJSON.put("nick", nick);
		} catch(JSONException e) {
		}
		
		HttpPost avatarPost = new HttpPost(context.getString(R.string.url_login));

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("json", avatarJSON.toString()));
		try {
			avatarPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		} catch(UnsupportedEncodingException e) {
			Log.w(TAG, "Could not set Entity name value pairs.", e);
		}
		eb.dispatchEvent(SpacifyEvents.AVATAR_LOGIN_STARTED.ordinal());
		web.requestJSON(avatarPost, new HttpJSONListener() {

			@Override
			public void handleErrorResponse(QvikException exception) {
				eb.dispatchEvent(SpacifyEvents.AVATAR_LOGIN_FAIL.ordinal());
			}

			@Override
			public void handleSuccessJsonResponse(JSONObject jsonResponse) {
				Log.d(TAG, "Login response: " + jsonResponse);
				if(StaticUtils.parseBooleanJSON(jsonResponse, "success", false)) {
					userNick = nick;
					settings.storeString(Preferences.USER_NICK, userNick);
					setFirstName(StaticUtils.parseStringJSON(jsonResponse, "firstName", null));
					setLastName(StaticUtils.parseStringJSON(jsonResponse, "lastName", null));
					setAvatarBubble(StaticUtils.parseStringJSON(jsonResponse, "bubbleId", null));

					eb.dispatchEvent(SpacifyEvents.AVATAR_LOGIN_SUCCESS.ordinal());
				} else {
					eb.dispatchEvent(SpacifyEvents.AVATAR_LOGIN_FAIL.ordinal());
				}

			}
		});

	}

	private void setAvatarBubble(String avatarID) {
		settings.storeString(Preferences.AVATAR_ID, avatarID);
		this.avatarBubbleID = avatarID;
	}

	private void setLastName(String lastName) {
		settings.storeString(Preferences.USER_LAST_NAME, lastName);
		this.userLastName = lastName;
	}

	private void setFirstName(String firstName) {
		settings.storeString(Preferences.USER_FIRST_NAME, firstName);
		this.userFirstName = firstName;
	}

	public Cursor getAvatarBubbleCursor() {
		List<String> links = new ArrayList<String>();
		links.add(avatarBubbleID);
		Cursor c = db.getLinkedBubblesCursor(links);
		c.moveToFirst();
		return c;
	}

	public void toggleFavorite(BubbleView bv) {
		if(isFavorite(bv)) {
			favorites.remove(bv.getID());
			sendServerFavouriteToggle(bv.getID(), false);
		} else {
			favorites.add(bv.getID());
			sendServerFavouriteToggle(bv.getID(), true);
		}

		storeFavoritesToDatabase();
	}

	private void sendServerFavouriteToggle(String id, boolean addLink) {
		if(TextUtils.isEmpty(userNick)) {
			return;
		}
		JSONObject avatarJSON = new JSONObject();
		try {
			avatarJSON.put("nick", userNick);
			if(addLink) {
				avatarJSON.put("addFavourite", id);
			} else {
				avatarJSON.put("delFavourite", id);
			}
		} catch(JSONException e) {
		}

		HttpPost addToFavoritesPost = new HttpPost(context.getString(R.string.url_toggle_favorite));

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("json", avatarJSON.toString()));
		try {
			addToFavoritesPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		} catch(UnsupportedEncodingException e) {
			Log.w(TAG, "Could not set Entity name value pairs.", e);
		}
		web.requestJSON(addToFavoritesPost, new HttpJSONListener() {

			@Override
			public void handleErrorResponse(QvikException exception) {
			}

			@Override
			public void handleSuccessJsonResponse(JSONObject jsonResponse) {
			}
		});

	}

	public void storeFavoritesToDatabase() {
		JSONArray avatarLinks = new JSONArray();
		for(String link : favorites) {
			avatarLinks.put(link);
		}

		settings.storeString(Preferences.USER_FAVORITES, avatarLinks.toString());
		db.updateAvatarLink(avatarBubbleID, avatarLinks);
	}

	public boolean isFavorite(BubbleView bv) {
		return favorites.contains(bv.getID());
	}

	public List<String> getFavorites() {
		return favorites;
	}

	private void loadAvatarLinks() {
		if(TextUtils.isEmpty(avatarBubbleID)) {
			return;
		}
		try {
			String linksJSON = settings.loadString(Preferences.USER_FAVORITES, null);
			if(linksJSON != null) {
				favorites = parseJsonLinks(new JSONArray(linksJSON));
			}
		} catch(JSONException e) {
			e.printStackTrace();
		}
	}

	private List<String> parseJsonLinks(JSONArray jArray) {
		List<String> links = new ArrayList<String>();
		for(int i = 0; i < jArray.length(); i++) {
			try {
				links.add(jArray.getString(i));
			} catch(JSONException e) {
				e.printStackTrace();
			}
		}
		return links;
	}

	public void callToScreen(double x, double y, boolean show) {
		if(TextUtils.isEmpty(userNick)) {
			return;
		}
		JSONObject avatarJSON = new JSONObject();
		try {
			avatarJSON.put("nick", userNick);
			avatarJSON.put("showAvatar", show);
			avatarJSON.put("relX", "" + x);
			avatarJSON.put("relY", "" + y);
		} catch(JSONException e) {
		}

		HttpPost callToScreenPost = new HttpPost(context.getString(R.string.url_call_to_screen));

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("json", avatarJSON.toString()));
		try {
			callToScreenPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		} catch(UnsupportedEncodingException e) {
			Log.w(TAG, "Could not set Entity name value pairs.", e);
		}
		web.requestJSON(callToScreenPost, new HttpJSONListener() {

			@Override
			public void handleErrorResponse(QvikException e) {
				Log.w(TAG, "CallToScreen failed!", e);
			}

			@Override
			public void handleSuccessJsonResponse(JSONObject jsonResponse) {
				Log.d(TAG, "CallToScreenSuccess: " + jsonResponse);
			}
		});
	}

}
