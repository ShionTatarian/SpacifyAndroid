package fi.android.spacify.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import fi.android.spacify.R;
import fi.android.spacify.view.BubbleView;

public class RoundListAdapter extends CircularAdapter<BubbleView> {

	public RoundListAdapter(Context ctx, List<BubbleView> initialList) {
		super(ctx, initialList);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null) {
			convertView = getEmptyView(parent);
		}
		ViewHolder holder = (ViewHolder) convertView.getTag();
		holder.position = position;
		BubbleView bv = getItem(position);
		if(bv != null) {
			holder.text.setText(bv.getTitle());
		}
		
		return convertView;
	}

	public View getEmptyView(ViewGroup parent) {
		View v = LayoutInflater.from(getContext()).inflate(R.layout.control_bubble, parent, false);
		v.setTag(new ViewHolder(v));
		return v;
	}

	public class ViewHolder {
		public int position;
		public TextView text;

		public ViewHolder(View v) {
			text = (TextView) v.findViewById(R.id.control_bubble_text);
		}
	}

}
