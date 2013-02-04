package fi.android.spacify.adapter;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import fi.android.spacify.R;
import fi.android.spacify.db.BubbleDatabase.BubbleColumns;
import fi.android.spacify.view.BubbleView.BubbleJSON;
import fi.qvik.android.util.ImageService;
import fi.spacify.android.util.StaticUtils;

/**
 * Adapter to show Bubbles in a list trough a cursor.
 * 
 * @author Tommy
 * 
 */
public class BubbleCursorAdapter extends CursorAdapter {

	private ImageService is = ImageService.getInstance();
	private Activity act;

	/**
	 * Constructor
	 * 
	 * @param context
	 * @param c
	 * @param autoRequery
	 */
	public BubbleCursorAdapter(Activity act, Cursor c, boolean autoRequery) {
		super(act, c, autoRequery);
		this.act = act;
	}

	@Override
	public void bindView(View v, Context ctx, Cursor c) {
		ViewHolder holder = (ViewHolder) v.getTag();
		String title = c.getString(c.getColumnIndex(BubbleColumns.TITLE));
		if(TextUtils.isEmpty(title) || title.equals("null")) {
			title = "";
		}
		holder.text.setText(title);
		
		JSONObject styleOverride;
		String url = null;
		try {
			styleOverride = new JSONObject(c.getString(c.getColumnIndex(BubbleColumns.STYLE_OVERRIDES)));
			url = styleOverride.getString(BubbleJSON.titleImageUrl);
		} catch(JSONException e) {
		}
		holder.image.setImageResource(R.drawable.lightblueball);
		is.assignHelpMethod(act, holder.image, StaticUtils.IMAGE_MEDIUM, url,
				R.drawable.lightblueball, null);
	}

	@Override
	public View newView(Context ctx, Cursor c, ViewGroup parent) {
		View v = LayoutInflater.from(ctx).inflate(R.layout.search_bubble, parent, false);
		v.setTag(new ViewHolder(v));
		return v;
	}

	protected class ViewHolder {

		ImageView image;
		TextView text;

		public ViewHolder(View v) {
			text = (TextView) v.findViewById(R.id.search_bubble_textview);
			image = (ImageView) v.findViewById(R.id.search_bubble_image);
		}
	}

}
