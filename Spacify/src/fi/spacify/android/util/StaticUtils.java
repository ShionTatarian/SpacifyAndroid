package fi.spacify.android.util;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class StaticUtils {

	private static final String TAG = "StaticUtils";

	public static String parseStringJSON(JSONObject json, String key, String defValue) {
		String value = defValue;

		if(json.has(key)) {
			try {
				value = json.getString(key);
			} catch(JSONException e) {
				Log.w(TAG, "Could not parse String value for key[" + key + "] from json: " + json);
			}
		}

		return value;
	}

	public static double parseDoubleJSON(JSONObject json, String key, double defValue) {
		double value = defValue;

		if(json.has(key)) {
			try {
				value = json.getDouble(key);
			} catch(JSONException e) {
				Log.w(TAG, "Could not parse Double value for key[" + key + "] from json: " + json);
			}
		}

		return value;
	}

	public static int parseIntJSON(JSONObject json, String key, int defValue) {
		int value = defValue;

		if(json.has(key)) {
			try {
				value = json.getInt(key);
			} catch(JSONException e) {
				Log.w(TAG, "Could not parse Integer value for key[" + key + "] from json: " + json);
			}
		}

		return value;
	}

}
