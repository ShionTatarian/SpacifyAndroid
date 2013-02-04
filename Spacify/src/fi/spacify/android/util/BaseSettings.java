package fi.spacify.android.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.preference.PreferenceManager;
import fi.qvik.android.util.WorkService;

/**
 * Settings class. Stores and loads values to {@link SharedPreferences}.
 * 
 * @author Tommy
 * 
 */
public class BaseSettings {

	private static final String TAG = "BaseSettings";

	private WorkService ws = WorkService.getInstance();

	/**
	 * Static property for setting debug value.
	 */
	public static final Boolean DEBUG = true;

	private static BaseSettings instance;
	private Context context;
	private SharedPreferences preferences;

	public interface Preferences {
		public static final String USER_NICK = "user_nick";
		public static final String USER_PASSWORD = "user_password";
		public static final String AVATAR_ID = "avatar_bubble_id";
		public static final String USER_FIRST_NAME = "user_first_name";
		public static final String USER_LAST_NAME = "user_last_name";
		public static final String USER_FAVORITES = "user_favorites";
	}

	/**
	 * Constructor.
	 * 
	 * @param ctx
	 */
	private BaseSettings(Context ctx) {
		this.context = ctx;
		this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
	}

	/**
	 * Initialize {@link BaseSettings}.
	 * 
	 * @param context
	 */
	public static void init(Context context) {
		if(instance != null) {
			throw new IllegalStateException(TAG + " is already initialized");
		}

		instance = new BaseSettings(context);
	}

	/**
	 * Get singleton instance of {@link BaseSettings}.
	 * 
	 * @return Instance of {@link BaseSettings}
	 */
	public static BaseSettings getInstance() {
		if(instance == null) {
			throw new IllegalStateException(TAG + " has not been initialized");
		}

		return instance;
	}

	/**
	 * Convenience method for storing String values to applications
	 * {@link SharedPreferences}.
	 * 
	 * @param key
	 * @param value
	 */
	public void storeString(String key, String value) {
		Editor editor = this.preferences.edit();
		editor.putString(key, value);
		apply(editor);
	}

	/**
	 * Load String from SharedPreferenses. If no String can be found with given
	 * key defValue is returned.
	 * 
	 * @param key
	 * @param defValue
	 * @return Stored String or defValue.
	 */
	public String loadString(String key, String defValue) {
		return this.preferences.getString(key, defValue);
	}

	/**
	 * Store boolean value to SharedPreferenses.
	 * 
	 * @param key
	 * @param value
	 */
	public void storeBoolean(String key, boolean value) {
		Editor editor = this.preferences.edit();
		editor.putBoolean(key, value);
		apply(editor);
	}

	/**
	 * Load stored Boolean value from SharedPreferences or given defValue.
	 * 
	 * @param key
	 * @param defValue
	 * @return Stored Boolean value or given defValue if key is not found.
	 */
	public boolean loadBoolean(String key, boolean defValue) {
		return this.preferences.getBoolean(key, defValue);
	}

	/**
	 * Store Integer value to SharedPreferenses.
	 * 
	 * @param key
	 * @param value
	 */
	public void storeInt(String key, int value) {
		Editor editor = this.preferences.edit();
		editor.putInt(key, value);
		apply(editor);
	}

	/**
	 * Load stored Integer value from SharedPreferences or given defValue.
	 * 
	 * @param key
	 * @param defValue
	 * @return Stored Integer value or given defValue if key is not found.
	 */
	public int loadInt(String key, int defValue) {
		return this.preferences.getInt(key, defValue);
	}

	/**
	 * Store Float value to SharedPreferenses.
	 * 
	 * @param key
	 * @param value
	 */
	public void storeFloat(String key, float value) {
		Editor editor = this.preferences.edit();
		editor.putFloat(key, value);
		apply(editor);
	}

	/**
	 * Load stored Float value from SharedPreferences or given defValue.
	 * 
	 * @param key
	 * @param defValue
	 * @return Stored Float value or given defValue if key is not found.
	 */
	public float loadFloat(String key, float defValue) {
		return this.preferences.getFloat(key, defValue);
	}

	/**
	 * Store Long value to SharedPreferenses.
	 * 
	 * @param key
	 * @param value
	 */
	public void storeLong(String key, long value) {
		Editor editor = this.preferences.edit();
		editor.putLong(key, value);
		apply(editor);
	}

	/**
	 * Load stored Long value from SharedPreferences or given defValue.
	 * 
	 * @param key
	 * @param defValue
	 * @return Stored Long value or given defValue if key is not found.
	 */
	public long loadLong(String key, long defValue) {
		return this.preferences.getLong(key, defValue);
	}

	/**
	 * Stores the editors stored values. If SDK version >= 9 then apply(editor)
	 * is called. Else editor.commit() is called inside semaphore lock so that
	 * editor commits do not clash.
	 * 
	 * @param editor
	 */
	@TargetApi(9)
	private void apply(final Editor editor) {
		if(Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
			editor.apply();
		} else {
			ws.postWork(new Runnable() {

				@Override
				public void run() {
					editor.commit();
				}
			});
		}
	}

}