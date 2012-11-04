package fi.android.spacify.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import fi.android.spacify.R;
import fi.android.spacify.db.BubbleDatabase.BubbleColumns;

/**
 * Adapter to show Bubbles in a list trough a cursor.
 * 
 * @author Tommy
 * 
 */
public class BubbleCursorAdapter extends CursorAdapter {

	/**
	 * Constructor
	 * 
	 * @param context
	 * @param c
	 * @param autoRequery
	 */
	public BubbleCursorAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
	}

	@Override
	public void bindView(View v, Context ctx, Cursor c) {
		ViewHolder holder = (ViewHolder) v.getTag();
		holder.bubble.setText(c.getString(c.getColumnIndex(BubbleColumns.TITLE)));
	}

	@Override
	public View newView(Context ctx, Cursor c, ViewGroup parent) {
		View v = LayoutInflater.from(ctx).inflate(R.layout.search_bubble, parent, false);
		v.setTag(new ViewHolder(v));
		return v;
	}

	protected class ViewHolder {

		TextView bubble;

		public ViewHolder(View v) {
			bubble = (TextView) v.findViewById(R.id.search_bubble_textview);
		}
	}

}
